
package AppointmentAndAvailability;

import java.time.LocalDateTime;

public class AvailabilitySlot {

    private Long slotId;
    private HealthcareProfessional professional;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public AvailabilitySlot(
            Long slotId,
            HealthcareProfessional professional,
            LocalDateTime startTime,
            LocalDateTime endTime) {

        if (slotId == null || professional == null ||
                startTime == null || endTime == null) {
            throw new IllegalArgumentException(
                    "Availability slot information cannot be null.");
        }

        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException(
                    "End time must be after start time.");
        }

        this.slotId = slotId;
        this.professional = professional;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public Long getSlotId() {
        return slotId;
    }

    public HealthcareProfessional getProfessional() {
        return professional;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }
}
