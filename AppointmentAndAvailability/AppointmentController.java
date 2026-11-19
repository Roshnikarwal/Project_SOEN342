
package AppointmentAndAvailability;

import data.ClinicDataStore;

public class AppointmentController {

    private final ClinicDataStore dataStore;
    private long nextAppointmentId = 1;

    public AppointmentController(ClinicDataStore dataStore) {
        this.dataStore = dataStore;
    }

    public Appointment bookAppointment(
            Long patientId,
            BookingInfo bookingInfo) {

        if (patientId == null || bookingInfo == null
                || bookingInfo.getSlotId() == null
                || bookingInfo.getProfessional() == null) {
            throw new IllegalArgumentException(
                    "Booking information is incomplete.");
        }

        synchronized (dataStore) {

            // 1. Find the patient
            Patient patient = dataStore.findPatient(patientId);

            if (patient == null) {
                throw new IllegalArgumentException(
                        "Patient not found.");
            }

            // 2. Find the selected availability slot
            AvailabilitySlot slot = dataStore.findAvailabilitySlot(
                    bookingInfo.getSlotId());

            if (slot == null) {
                throw new IllegalArgumentException(
                        "Availability slot not found.");
            }

            // 3. Ensure the professional matches the slot
            HealthcareProfessional professional =
                    bookingInfo.getProfessional();

            if (slot.getProfessional() == null
                    || !slot.getProfessional().getProfessionalId()
                            .equals(professional.getProfessionalId())) {
                throw new IllegalArgumentException(
                        "The selected professional does not match the slot.");
            }

            // 4. Prevent double booking
            if (dataStore.isSlotBooked(slot.getSlotId())) {
                throw new IllegalStateException(
                        "This appointment slot is already booked.");
            }

            // 5. Create appointment
            Appointment appointment = new Appointment(
                    nextAppointmentId++,
                    patient,
                    professional,
                    slot
            );

            // 6. Confirm appointment
            appointment.confirm();

            // 7. Store appointment
            dataStore.saveAppointment(appointment);

            return appointment;
        }
    }
}
