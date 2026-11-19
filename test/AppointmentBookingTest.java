
package test;

import AppointmentAndAvailability.*;
import data.ClinicDataStore;
import java.time.LocalDateTime;

public class AppointmentBookingTest {

    public static void main(String[] args) {

        ClinicDataStore store = new ClinicDataStore();

        // Sample patient from assignment
        Patient patient = new Patient(
                101L,
                "Émilie",
                "Lavoie",
                null,
                "514-555-0101",
                "LAVE99010101"
        );

        store.addPatient(patient);

        // Sample healthcare professional
        HealthcareProfessional doctor =
                new HealthcareProfessional(
                        1L,
                        "Amélie",
                        "Tremblay",
                        "Dr.",
                        "Family Medicine"
                );

        // Sample availability
        AvailabilitySlot slot = new AvailabilitySlot(
                1L,
                doctor,
                LocalDateTime.of(2026, 11, 2, 9, 0),
                LocalDateTime.of(2026, 11, 2, 9, 30)
        );

        store.addAvailabilitySlot(slot);

        AppointmentController controller =
                new AppointmentController(store);

        BookingInfo info = new BookingInfo(1L, doctor);

        // TEST 1: Successful booking
        try {
            Appointment appointment =
                    controller.bookAppointment(101L, info);

            if ("CONFIRMED".equals(appointment.getStatus())
                    && appointment.getPatient().getPatientId().equals(101L)) {
                System.out.println("TEST 1 PASSED: Appointment confirmed.");
            } else {
                System.out.println("TEST 1 FAILED.");
            }

        } catch (Exception e) {
            System.out.println("TEST 1 FAILED: " + e.getMessage());
        }

        // TEST 2: Double booking
        try {
            controller.bookAppointment(101L, info);
            System.out.println("TEST 2 FAILED: Double booking allowed.");

        } catch (IllegalStateException e) {
            System.out.println("TEST 2 PASSED: Double booking rejected.");
        }

        // TEST 3: Invalid patient
        try {
            controller.bookAppointment(999L, info);
            System.out.println("TEST 3 FAILED: Invalid patient accepted.");

        } catch (IllegalArgumentException e) {
            System.out.println("TEST 3 PASSED: Invalid patient rejected.");
        }
    }
}
