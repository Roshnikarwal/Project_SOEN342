package AppointmentAndAvailability;

public class Appointment {

    private Long appointmentId;
    private Patient patient;
    private HealthcareProfessional professional;
    private AvailabilitySlot slot;
    private String status;

    public Appointment(
            Long appointmentId,
            Patient patient,
            HealthcareProfessional professional,
            AvailabilitySlot slot) {

        this.appointmentId = appointmentId;
        this.patient = patient;
        this.professional = professional;
        this.slot = slot;
        this.status = "PENDING";
    }

    public Long getAppointmentId() {
        return appointmentId;
    }

    public Patient getPatient() {
        return patient;
    }

    public HealthcareProfessional getProfessional() {
        return professional;
    }

    public AvailabilitySlot getSlot() {
        return slot;
    }

    public String getStatus() {
        return status;
    }

    public void confirm() {
    this.status = "CONFIRMED";
}
}