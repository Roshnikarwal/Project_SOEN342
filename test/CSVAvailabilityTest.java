
package test;

import AppointmentAndAvailability.CSVAvailabilityService;
import AppointmentAndAvailability.AvailabilitySlot;
import data.ClinicDataStore;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class CSVAvailabilityTest {

    public static void main(String[] args) {

        ClinicDataStore store = new ClinicDataStore();
        CSVAvailabilityService csvService =
                new CSVAvailabilityService(store);

        String inputFile = "csv/availability.csv";
        String outputFile = "csv/exported_availability.csv";

        // TEST 1: Import availability from CSV
        System.out.println("TEST 1: Import CSV");

        try {
            int imported = csvService.importAvailability(inputFile);

            if (imported == 8
                    && store.getAvailabilitySlots().size() == 8) {
                System.out.println("PASSED: 8 slots imported.");
            } else {
                System.out.println("FAILED: Expected 8 slots, got "
                        + imported);
            }

        } catch (Exception e) {
            System.out.println("FAILED: " + e.getMessage());
            return;
        }

        // TEST 2: Export availability to CSV
        System.out.println("\nTEST 2: Export CSV");

        try {
            csvService.exportAvailability(outputFile);

            Path path = Path.of(outputFile);
            List<String> lines = Files.readAllLines(path);

            // 1 header + 8 availability records
            if (Files.exists(path) && lines.size() == 9) {
                System.out.println(
                        "PASSED: CSV exported with 8 records.");
            } else {
                System.out.println("FAILED: Incorrect export.");
            }

        } catch (Exception e) {
            System.out.println("FAILED: " + e.getMessage());
        }

        // TEST 3: Prevent duplicate imports
        System.out.println("\nTEST 3: Duplicate Import");

        try {
            int importedAgain =
                    csvService.importAvailability(inputFile);

            if (importedAgain == 0
                    && store.getAvailabilitySlots().size() == 8) {
                System.out.println(
                        "PASSED: Duplicate availability skipped.");
            } else {
                System.out.println(
                        "FAILED: Duplicate slots were added.");
            }

        } catch (Exception e) {
            System.out.println("FAILED: " + e.getMessage());
        }

        // TEST 4: Invalid CSV path
        System.out.println("\nTEST 4: Invalid File Path");

        try {
            csvService.importAvailability("csv/nonexistent.csv");
            System.out.println(
                    "FAILED: Missing file was accepted.");

        } catch (java.io.IOException e) {
            System.out.println(
                    "PASSED: Missing CSV file rejected.");
        }

        // TEST 5: Verify imported professional information
        System.out.println("\nTEST 5: Verify Imported Data");

        List<AvailabilitySlot> slots =
                store.getAvailabilitySlots();

        AvailabilitySlot firstSlot = slots.get(0);

        boolean correct =
                firstSlot.getProfessional().getFirstName()
                        .equals("Amélie")
                && firstSlot.getProfessional().getLastName()
                        .equals("Tremblay")
                && firstSlot.getStartTime().getHour() == 9
                && firstSlot.getEndTime().getMinute() == 30;

        if (correct) {
            System.out.println(
                    "PASSED: Imported data matches sample CSV.");
        } else {
            System.out.println(
                    "FAILED: Imported data does not match CSV.");
        }

      try {
           Files.deleteIfExists(Path.of(outputFile));
           System.out.println("\nTest output file cleaned up.");
    } catch (java.io.IOException e) {
        System.out.println("Failed to delete exported CSV: " + e.getMessage());
    }
    }
}
