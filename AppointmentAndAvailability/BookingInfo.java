
package AppointmentAndAvailability;

public class BookingInfo {

    private Long slotId;
    private HealthcareProfessional professional;

    public BookingInfo(Long slotId,
                       HealthcareProfessional professional) {
        this.slotId = slotId;
        this.professional = professional;
    }

    public Long getSlotId() {
        return slotId;
    }

    public HealthcareProfessional getProfessional() {
        return professional;
    }
}
