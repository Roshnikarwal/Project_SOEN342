
package AppointmentAndAvailability;

public class HealthcareProfessional {

    private Long professionalId;
    private String firstName;
    private String lastName;
    private String title;
    private String specialty;

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
}
