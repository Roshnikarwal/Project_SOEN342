package app;

import AppointmentAndAvailability.*;
import data.ClinicDataStore;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.DayOfWeek;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        System.out.println("Clinic Appointment Tracking System");
        ClinicDataStore store = new ClinicDataStore();
        CSVAvailabilityService csv = new CSVAvailabilityService(store);
        AvailabilityController controller = new AvailabilityController(store);
        AppointmentController appointments = new AppointmentController(store);
        try (Scanner input = new Scanner(System.in)) {
            boolean running = true;
            while (running) {
                System.out.println("\n1. Book appointment\n2. Search availability"
                        + "\n3. Make availability\n4. Import availability from CSV"
                        + "\n5. Export availability to CSV\n0. Exit");
                System.out.print("Choose an option: ");
                String choice = input.nextLine().trim();
                try {
                    switch (choice) {
                        case "1": book(input, controller, appointments, store); break;
                        case "2": search(input, controller); break;
                        case "3": make(input, controller); break;
                        case "4": importAvailability(input, csv); break;
                        case "5": exportAvailability(input, csv, store); break;
                        case "0": running = false; break;
                        default: System.out.println("Choose a listed option.");
                    }
                } catch (IllegalArgumentException | IllegalStateException e) {
                    System.out.println("Unable to complete request: " + e.getMessage());
                }
            }
        }
    }

    private static void search(Scanner input, AvailabilityController controller) {
        System.out.println("Leave a filter blank to include all values.");
        System.out.print("Professional name: ");
        String professional = input.nextLine();
        System.out.print("Specialty: ");
        String specialty = input.nextLine();
        LocalDate from = readOptionalDate(input, "From date (YYYY-MM-DD): ");
        LocalDate to = readOptionalDate(input, "To date (YYYY-MM-DD): ");

        List<AvailabilitySlot> slots = controller.searchAvailability(
                professional, specialty, from, to);
        if (slots.isEmpty()) {
            System.out.println("No available slots match those criteria.");
            return;
        }
        for (AvailabilitySlot slot : slots) {
            HealthcareProfessional provider = slot.getProfessional();
            System.out.printf("%d | %s (%s) | %s - %s%n", slot.getSlotId(),
                    provider.getFullName(), provider.getSpecialty(),
                    slot.getStartTime(), slot.getEndTime());
        }
    }

    private static void book(Scanner input, AvailabilityController availability,
                             AppointmentController appointments, ClinicDataStore store) {
        System.out.print("Patient ID: ");
        long patientId = Long.parseLong(input.nextLine().trim());
        Patient patient = store.findPatient(patientId);
        if (patient == null) {
            System.out.println("Patient is not registered. Enter patient details to register.");
            System.out.print("First name: ");
            String firstName = input.nextLine().trim();
            System.out.print("Last name: ");
            String lastName = input.nextLine().trim();
            System.out.print("Date of birth (YYYY-MM-DD, or blank): ");
            String dob = input.nextLine().trim();
            System.out.print("Phone: ");
            String phone = input.nextLine().trim();
            System.out.print("RAMQ number (or blank): ");
            String ramq = input.nextLine().trim();
            patient = new Patient(patientId, firstName, lastName,
                    dob.isEmpty() ? null : LocalDate.parse(dob), phone, ramq);
            store.addPatient(patient);
        }

        System.out.println("Search for a slot for this booking.");
        System.out.print("Professional name (blank for any): ");
        String professional = input.nextLine();
        System.out.print("Specialty (blank for any): ");
        String specialty = input.nextLine();
        LocalDate from = readOptionalDate(input, "From date (YYYY-MM-DD, blank for any): ");
        LocalDate to = readOptionalDate(input, "To date (YYYY-MM-DD, blank for any): ");
        List<AvailabilitySlot> slots = availability.searchAvailability(
                professional, specialty, from, to);
        if (slots.isEmpty()) {
            System.out.println("No available slots found. No appointment was booked.");
            return;
        }
        for (AvailabilitySlot slot : slots) {
            System.out.printf("%d | %s | %s - %s%n", slot.getSlotId(),
                    slot.getProfessional().getFullName(),
                    slot.getStartTime(), slot.getEndTime());
        }
        System.out.print("Slot ID to book: ");
        long slotId = Long.parseLong(input.nextLine().trim());
        AvailabilitySlot slot = store.findAvailabilitySlot(slotId);
        if (slot == null) throw new IllegalArgumentException("Availability slot not found.");
        Appointment appointment = appointments.bookAppointment(patientId,
                new BookingInfo(slotId, slot.getProfessional()));
        System.out.printf("Appointment %d confirmed for %s with %s at %s.%n",
                appointment.getAppointmentId(), patient.getFullName(),
                slot.getProfessional().getFullName(), slot.getStartTime());
    }

    private static void make(Scanner input, AvailabilityController controller) {
        System.out.print("Professional ID: ");
        long professionalId = Long.parseLong(input.nextLine().trim());
        System.out.print("First name: ");
        String firstName = input.nextLine().trim();
        System.out.print("Last name: ");
        String lastName = input.nextLine().trim();
        System.out.print("Title: ");
        String title = input.nextLine().trim();
        System.out.print("Specialty: ");
        String specialty = input.nextLine().trim();
        System.out.print("From date (YYYY-MM-DD): ");
        LocalDate fromDate = LocalDate.parse(input.nextLine().trim());
        System.out.print("To date (YYYY-MM-DD): ");
        LocalDate toDate = LocalDate.parse(input.nextLine().trim());
        System.out.print("Pattern (DAILY, WEEKLY, MONTHLY): ");
        AvailabilityPattern pattern = AvailabilityPattern.valueOf(
                input.nextLine().trim().toUpperCase());
        Set<DayOfWeek> weekDays = new HashSet<>();
        int monthDay = 0;
        if (pattern == AvailabilityPattern.WEEKLY) {
            System.out.print("Weekdays (comma-separated, e.g. MONDAY,WEDNESDAY): ");
            for (String day : input.nextLine().split(",")) {
                weekDays.add(DayOfWeek.valueOf(day.trim().toUpperCase()));
            }
        } else if (pattern == AvailabilityPattern.MONTHLY) {
            System.out.print("Day of month (1-31): ");
            monthDay = Integer.parseInt(input.nextLine().trim());
        }
        System.out.print("Start time (HH:MM): ");
        LocalTime start = LocalTime.parse(input.nextLine().trim());
        System.out.print("End time (HH:MM): ");
        LocalTime end = LocalTime.parse(input.nextLine().trim());
        System.out.print("Slot length in minutes (0 for one block per matching date): ");
        int slotLength = Integer.parseInt(input.nextLine().trim());

        HealthcareProfessional provider = new HealthcareProfessional(
                professionalId, firstName, lastName, title, specialty);
        List<AvailabilitySlot> created = controller.makeAvailability(
                provider, fromDate, toDate, pattern, weekDays, monthDay,
                start, end, slotLength);
        System.out.println("Created " + created.size() + " slot(s).");
    }

    private static void importAvailability(Scanner input, CSVAvailabilityService csv) {
        System.out.print("Source CSV path: ");
        String path = input.nextLine().trim();
        try {
            int imported = csv.importAvailability(path);
            System.out.println("Imported " + imported + " availability slot(s).");
        } catch (IOException e) {
            System.out.println("Could not import availability: " + e.getMessage());
        }
    }

    private static void exportAvailability(Scanner input, CSVAvailabilityService csv,
                                           ClinicDataStore store) {
        System.out.print("Output CSV path: ");
        String path = input.nextLine().trim();
        try {
            csv.exportAvailability(path);
            System.out.println("Exported " + store.getAvailabilitySlots().size()
                    + " availability slot(s).");
        } catch (IOException e) {
            System.out.println("Could not export availability: " + e.getMessage());
        }
    }

    private static LocalDate readOptionalDate(Scanner input, String prompt) {
        System.out.print(prompt);
        String value = input.nextLine().trim();
        if (value.isEmpty()) return null;
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Enter dates as YYYY-MM-DD.", e);
        }
    }
}
