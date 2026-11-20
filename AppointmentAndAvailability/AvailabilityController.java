package AppointmentAndAvailability;

import data.ClinicDataStore;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.DayOfWeek;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/** Search and maintain clinic availability. */
public class AvailabilityController {
    private final ClinicDataStore dataStore;

    public AvailabilityController(ClinicDataStore dataStore) {
        this.dataStore = Objects.requireNonNull(dataStore, "Data store cannot be null.");
    }

    /** Search by optional provider name, specialty, and inclusive date range. */
    public List<AvailabilitySlot> searchAvailability(String professional,
            String specialty, LocalDate fromDate, LocalDate toDate) {
        AvailabilityCriteria criteria = new AvailabilityCriteria(
                professional, specialty, fromDate, toDate);
        return searchAvailability(criteria);
    }

    /** UML operation: search using a criteria object. */
    public List<AvailabilitySlot> searchAvailability(AvailabilityCriteria criteria) {
        if (criteria == null) {
            throw new IllegalArgumentException("Search criteria cannot be null.");
        }
        return dataStore.getAvailabilitySlots().stream()
                .filter(criteria::matches)
                .filter(slot -> !dataStore.isSlotBooked(slot.getSlotId()))
                .sorted(Comparator.comparing(AvailabilitySlot::getStartTime)
                        .thenComparing(AvailabilitySlot::getSlotId))
                .collect(Collectors.toList());
    }

    /** Update or add a professional's regular schedule for one weekday. */
    public void updateProfessionalSchedule(Long professionalId, Schedule schedule) {
        if (professionalId == null || schedule == null) {
            throw new IllegalArgumentException("Professional ID and schedule are required.");
        }
        HealthcareProfessional professional = dataStore.findHealthcareProfessional(professionalId);
        if (professional == null) {
            throw new IllegalArgumentException("Healthcare professional not found.");
        }
        synchronized (dataStore) {
            professional.updateSchedule(schedule);
        }
    }

    /** UML operation: create one availability block for each matching scheduled date. */
    public List<AvailabilitySlot> makeAvailability(Long professionalId,
            AvailabilityPattern pattern, LocalDate fromDate, LocalDate toDate) {
        if (professionalId == null || pattern == null || fromDate == null || toDate == null) {
            throw new IllegalArgumentException("Professional, pattern, and date range are required.");
        }
        if (toDate.isBefore(fromDate)) {
            throw new IllegalArgumentException("End date must not be before start date.");
        }
        HealthcareProfessional professional = dataStore.findHealthcareProfessional(professionalId);
        if (professional == null) {
            throw new IllegalArgumentException("Healthcare professional not found.");
        }
        List<Schedule> schedules = professional.getSchedules();
        if (schedules.isEmpty()) {
            throw new IllegalStateException("Professional has no schedule to create availability from.");
        }

        synchronized (dataStore) {
            List<AvailabilitySlot> existing = dataStore.getAvailabilitySlots();
            List<AvailabilitySlot> created = new ArrayList<>();
            long nextId = existing.stream().mapToLong(s -> s.getSlotId()).max().orElse(0L) + 1L;
            for (LocalDate date = fromDate;
                    !date.isAfter(toDate); date = date.plusDays(1)) {
                for (Schedule schedule : schedules) {
                    DayOfWeek scheduleDay = DayOfWeek.valueOf(schedule.getDayOfWeek());
                    boolean matches = pattern == AvailabilityPattern.DAILY
                            || (pattern == AvailabilityPattern.WEEKLY && date.getDayOfWeek() == scheduleDay)
                            || (pattern == AvailabilityPattern.MONTHLY
                                && date.getDayOfMonth() == fromDate.getDayOfMonth());
                    if (!matches) continue;

                    LocalDateTime start = date.atTime(schedule.getStartTime());
                    LocalDateTime end = date.atTime(schedule.getEndTime());
                    if (overlapsProfessionalSlot(existing, professional, start, end)
                            || overlapsProfessionalSlot(created, professional, start, end)) {
                        continue;
                    }
                    created.add(new AvailabilitySlot(nextId++, professional, start, end));
                }
            }
            for (AvailabilitySlot slot : created) dataStore.addAvailabilitySlot(slot);
            return created;
        }
    }

    /** Add a provider's availability. The slot is stored only if valid and conflict-free. */
    public AvailabilitySlot makeAvailability(HealthcareProfessional professional,
            LocalDateTime startTime, LocalDateTime endTime) {
        if (professional == null || startTime == null || endTime == null) {
            throw new IllegalArgumentException("Availability information cannot be null.");
        }
        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("End time must be after start time.");
        }

        synchronized (dataStore) {
            List<AvailabilitySlot> existing = dataStore.getAvailabilitySlots();
            for (AvailabilitySlot slot : existing) {
                if (slot.getProfessional().getProfessionalId().equals(professional.getProfessionalId())
                        && startTime.isBefore(slot.getEndTime())
                        && endTime.isAfter(slot.getStartTime())) {
                    throw new IllegalStateException("Availability overlaps an existing slot.");
                }
            }
            long slotId = existing.stream().mapToLong(s -> s.getSlotId()).max().orElse(0L) + 1L;
            AvailabilitySlot slot = new AvailabilitySlot(slotId, professional, startTime, endTime);
            dataStore.addAvailabilitySlot(slot);
            return slot;
        }
    }

    /**
     * Create one or more slots over an inclusive date range. A zero slotLength
     * creates one block per matching date; positive values split each day's
     * working period into slots of that many minutes.
     */
    public List<AvailabilitySlot> makeAvailability(
            HealthcareProfessional professional,
            LocalDate fromDate, LocalDate toDate,
            AvailabilityPattern pattern, Set<DayOfWeek> weekDays,
            int monthDay, LocalTime startTime, LocalTime endTime,
            int slotLength) {
        if (professional == null || fromDate == null || toDate == null
                || pattern == null || startTime == null || endTime == null) {
            throw new IllegalArgumentException("Availability information cannot be null.");
        }
        if (toDate.isBefore(fromDate)) {
            throw new IllegalArgumentException("End date must not be before start date.");
        }
        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("End time must be after start time.");
        }
        if (slotLength < 0) {
            throw new IllegalArgumentException("Slot length cannot be negative.");
        }
        if (pattern == AvailabilityPattern.WEEKLY
                && (weekDays == null || weekDays.isEmpty())) {
            throw new IllegalArgumentException("Select at least one weekday for a weekly pattern.");
        }
        if (pattern == AvailabilityPattern.MONTHLY && (monthDay < 1 || monthDay > 31)) {
            throw new IllegalArgumentException("Monthly day must be between 1 and 31.");
        }

        Set<DayOfWeek> selectedDays = weekDays == null
                ? Set.of() : new HashSet<>(weekDays);
        List<LocalDate> matchingDates = new ArrayList<>();
        for (LocalDate date = fromDate; !date.isAfter(toDate); date = date.plusDays(1)) {
            boolean matches = pattern == AvailabilityPattern.DAILY
                    || (pattern == AvailabilityPattern.WEEKLY && selectedDays.contains(date.getDayOfWeek()))
                    || (pattern == AvailabilityPattern.MONTHLY && date.getDayOfMonth() == monthDay);
            if (matches) matchingDates.add(date);
        }

        List<LocalDateTime[]> candidates = new ArrayList<>();
        long minutes = ChronoUnit.MINUTES.between(startTime, endTime);
        if (slotLength > minutes) {
            throw new IllegalArgumentException("Slot length cannot exceed the availability period.");
        }
        for (LocalDate date : matchingDates) {
            if (slotLength == 0) {
                candidates.add(new LocalDateTime[]{date.atTime(startTime), date.atTime(endTime)});
            } else {
                for (long offset = 0; offset + slotLength <= minutes; offset += slotLength) {
                    LocalDateTime start = date.atTime(startTime).plusMinutes(offset);
                    candidates.add(new LocalDateTime[]{start, start.plusMinutes(slotLength)});
                }
            }
        }

        synchronized (dataStore) {
            List<AvailabilitySlot> existing = dataStore.getAvailabilitySlots();
            List<AvailabilitySlot> created = new ArrayList<>();
            long nextId = existing.stream().mapToLong(s -> s.getSlotId()).max().orElse(0L) + 1L;
            for (LocalDateTime[] candidate : candidates) {
                boolean overlaps = overlapsProfessionalSlot(existing, professional, candidate[0], candidate[1])
                        || overlapsProfessionalSlot(created, professional, candidate[0], candidate[1]);
                if (overlaps) {
                    continue;
                } else {
                    created.add(new AvailabilitySlot(nextId++, professional, candidate[0], candidate[1]));
                }
            }
            for (AvailabilitySlot slot : created) dataStore.addAvailabilitySlot(slot);
            return created;
        }
    }

    private boolean overlapsProfessionalSlot(List<AvailabilitySlot> slots,
            HealthcareProfessional professional, LocalDateTime start, LocalDateTime end) {
        for (AvailabilitySlot slot : slots) {
            if (slot.getProfessional().getProfessionalId().equals(professional.getProfessionalId())
                    && start.isBefore(slot.getEndTime()) && end.isAfter(slot.getStartTime())) {
                return true;
            }
        }
        return false;
    }
}
