package AppointmentAndAvailability;

import java.time.LocalDate;

/** Optional filters used when clinic staff search for availability. */
public class AvailabilityCriteria {
    private final String professional;
    private final String specialty;
    private final LocalDate fromDate;
    private final LocalDate toDate;

    public AvailabilityCriteria(String professional, String specialty,
                                LocalDate fromDate, LocalDate toDate) {
        if (fromDate != null && toDate != null && toDate.isBefore(fromDate)) {
            throw new IllegalArgumentException("End date must not be before start date.");
        }
        this.professional = normalize(professional);
        this.specialty = normalize(specialty);
        this.fromDate = fromDate;
        this.toDate = toDate;
    }

    private static String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    public boolean matches(AvailabilitySlot slot) {
        HealthcareProfessional provider = slot.getProfessional();
        LocalDate date = slot.getStartTime().toLocalDate();
        return (professional == null || provider.getFullName().toLowerCase().contains(professional.toLowerCase()))
                && (specialty == null || (provider.getSpecialty() != null
                    && provider.getSpecialty().toLowerCase().contains(specialty.toLowerCase())))
                && (fromDate == null || !date.isBefore(fromDate))
                && (toDate == null || !date.isAfter(toDate));
    }
}
