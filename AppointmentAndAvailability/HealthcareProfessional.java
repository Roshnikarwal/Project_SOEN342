
package AppointmentAndAvailability;

import java.util.ArrayList;
import java.util.List;

public class HealthcareProfessional {

    private Long professionalId;
    private String firstName;
    private String lastName;
    private String title;
    private String specialty;
    private final List<Schedule> schedules = new ArrayList<>();

    public HealthcareProfessional(
            Long professionalId,
            String firstName,
            String lastName,
            String title,
            String specialty) {

        if (professionalId == null ||
                firstName == null || firstName.isBlank() ||
                lastName == null || lastName.isBlank()) {
            throw new IllegalArgumentException(
                    "Healthcare professional information is invalid.");
        }

        this.professionalId = professionalId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.title = title;
        this.specialty = specialty;
    }

    public Long getProfessionalId() {
        return professionalId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getTitle() {
        return title;
    }

    public String getSpecialty() {
        return specialty;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public List<Schedule> getSchedules() {
        return new ArrayList<>(schedules);
    }

    /** Replace the regular schedule for this weekday, or add it if new. */
    public void updateSchedule(Schedule schedule) {
        if (schedule == null) {
            throw new IllegalArgumentException("Schedule cannot be null.");
        }
        schedules.removeIf(existing -> existing.getDayOfWeek().equals(schedule.getDayOfWeek()));
        schedules.add(schedule);
    }
}
