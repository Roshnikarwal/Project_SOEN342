
package test;

import AppointmentAndAvailability.*;
import data.ClinicDataStore;

public class BookingCSVIntegrationTest {

    public static void main(String[] args) {

        // Create shared storage
        ClinicDataStore store = new ClinicDataStore();

        // Create CSV service and booking controller
        CSVAvailabilityService csvService =
                new CSVAvailabilityService(store);

        AppointmentController bookingController =
                new AppointmentController(store);

        // TEST 1: Import availability from CSV
        System.out.println("TEST 1: Import availability");

        try {
            int imported = csvService.importAvailability(
                    "csv/availability.csv");

            if (imported != 8 ||
                    store.getAvailabilitySlots().size() != 8) {
                throw new AssertionError(
                        "Expected 8 availability slots.");
            }

            System.out.println("PASSED: 8 slots imported.");

        } catch (Exception e) {
            System.out.println("FAILED: " + e.getMessage());
            return;
        }

        // TEST 2: Book an appointment using imported availability
        System.out.println("\nTEST 2: Book imported slot");

        Patient patient = new Patient(
                101L,
                "Émilie",
                "Lavoie",
                null,
                "514-555-0101",
                "LAVE99010101"
        );

        store.addPatient(patient);

        AvailabilitySlot importedSlot =
                store.getAvailabilitySlots().get(0);

        HealthcareProfessional professional =
                importedSlot.getProfessional();

        BookingInfo bookingInfo = new BookingInfo(
                importedSlot.getSlotId(),
                professional
        );

        try {
            Appointment appointment =
                    bookingController.bookAppointment(
                            patient.getPatientId(),
                            bookingInfo
                    );

            boolean correct =
                    "CONFIRMED".equals(appointment.getStatus())
                    && appointment.getPatient().getPatientId()
                            .equals(101L)
                    && appointment.getSlot().getSlotId()
                            .equals(importedSlot.getSlotId())
                    && appointment.getProfessional()
                            .getProfessionalId()
                            .equals(professional.getProfessionalId());

            if (correct) {
                System.out.println(
                        "PASSED: Imported slot booked successfully.");

                System.out.println("Patient: "
                        + appointment.getPatient().getFullName());

                System.out.println("Professional: "
                        + appointment.getProfessional().getFullName());

                System.out.println("Start: "
                        + appointment.getSlot().getStartTime());

                System.out.println("Status: "
                        + appointment.getStatus());
            } else {
                throw new AssertionError(
                        "Incorrect appointment details.");
            }

        } catch (Exception e) {
            System.out.println("FAILED: " + e.getMessage());
            return;
        }

        // TEST 3: Attempt to book the same imported slot again
        System.out.println("\nTEST 3: Prevent double booking");

        try {
            bookingController.bookAppointment(
                    patient.getPatientId(),
                    bookingInfo
            );

            System.out.println(
                    "FAILED: Double booking was allowed.");

        } catch (IllegalStateException e) {
            System.out.println(
                    "PASSED: Double booking rejected.");
        }

        // TEST 4: Verify that the slot is marked as booked
        System.out.println("\nTEST 4: Verify booking storage");

        if (store.isSlotBooked(importedSlot.getSlotId())) {
            System.out.println(
                    "PASSED: Appointment stored successfully.");
        } else {
            System.out.println(
                    "FAILED: Appointment was not stored.");
        }
    }
}
