
package AppointmentAndAvailability;

import java.time.LocalDate;

public class Patient {

    private Long patientId;
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String phone;
    private String ramqNumber;

    public Patient(
            Long patientId,
            String firstName,
            String lastName,
            LocalDate dateOfBirth,
            String phone,
            String ramqNumber) {

        if (patientId == null ||
                firstName == null || firstName.isBlank() ||
                lastName == null || lastName.isBlank()) {
            throw new IllegalArgumentException(
                    "Patient information is invalid.");
        }

        this.patientId = patientId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.phone = phone;
        this.ramqNumber = ramqNumber;
    }

    public Long getPatientId() {
        return patientId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public String getPhone() {
        return phone;
    }

    public String getRamqNumber() {
        return ramqNumber;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
