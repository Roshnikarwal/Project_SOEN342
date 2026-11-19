
package AppointmentAndAvailability;

import data.ClinicDataStore;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class  CSVAvailabilityService {

    private final ClinicDataStore dataStore;

    public CSVAvailabilityService(ClinicDataStore dataStore) {
        this.dataStore = dataStore;
    }

    // IMPORT: CSV file -> ClinicDataStore
    public int importAvailability(String filePath) throws IOException {

        List<String> lines = Files.readAllLines(Path.of(filePath));

        if (lines.isEmpty()) {
            throw new IllegalArgumentException("CSV file is empty.");
        }

        String expectedHeader =
                "First Name,Last Name,Title,Specialty,Date,Start,End";

        if (!lines.get(0).trim().equals(expectedHeader)) {
            throw new IllegalArgumentException(
                    "Invalid CSV header.");
        }

        List<AvailabilitySlot> existing =
                dataStore.getAvailabilitySlots();

        List<AvailabilitySlot> imported = new ArrayList<>();

        long nextSlotId = 1;
        long nextProfessionalId = 1;

        for (AvailabilitySlot slot : existing) {
            nextSlotId = Math.max(nextSlotId,
                    slot.getSlotId() + 1);

            nextProfessionalId = Math.max(nextProfessionalId,
                    slot.getProfessional().getProfessionalId() + 1);
        }

        // Skip the first row because it contains column names
        for (int i = 1; i < lines.size(); i++) {

            String line = lines.get(i).trim();

            if (line.isEmpty()) {
                continue;
            }

            String[] values = line.split(",", -1);

            if (values.length != 7) {
                throw new IllegalArgumentException(
                        "Invalid CSV format at line " + (i + 1));
            }

            try {
                String firstName = values[0].trim();
                String lastName = values[1].trim();
                String title = values[2].trim();
                String specialty = values[3].trim();

                LocalDate date = LocalDate.parse(values[4].trim());
                LocalTime start = LocalTime.parse(values[5].trim());
                LocalTime end = LocalTime.parse(values[6].trim());

                LocalDateTime startTime = date.atTime(start);
                LocalDateTime endTime = date.atTime(end);

                // Find an existing matching professional
                HealthcareProfessional professional = null;

                List<AvailabilitySlot> allSlots = new ArrayList<>(existing);
                allSlots.addAll(imported);

                for (AvailabilitySlot slot : allSlots) {
                    HealthcareProfessional p = slot.getProfessional();

                    if (p.getFirstName().equals(firstName)
                            && p.getLastName().equals(lastName)
                            && p.getTitle().equals(title)
                            && p.getSpecialty().equals(specialty)) {

                        professional = p;
                        break;
                    }
                }

                if (professional == null) {
                    professional = new HealthcareProfessional(
                            nextProfessionalId++,
                            firstName,
                            lastName,
                            title,
                            specialty
                    );
                }

                // Avoid importing duplicate availability
                boolean duplicate = false;

                for (AvailabilitySlot slot : allSlots) {

                    if (slot.getProfessional().getProfessionalId()
                            .equals(professional.getProfessionalId())
                            && slot.getStartTime().equals(startTime)
                            && slot.getEndTime().equals(endTime)) {

                        duplicate = true;
                        break;
                    }
                }

                if (!duplicate) {
                    AvailabilitySlot newSlot = new AvailabilitySlot(
                            nextSlotId++,
                            professional,
                            startTime,
                            endTime
                    );

                    imported.add(newSlot);
                }

            } catch (RuntimeException e) {
                throw new IllegalArgumentException(
                        "Invalid availability at line " + (i + 1)
                                + ": " + e.getMessage(), e);
            }
        }

        // Store only after the entire CSV has been validated
        for (AvailabilitySlot slot : imported) {
            dataStore.addAvailabilitySlot(slot);
        }

        return imported.size();
    }

    // EXPORT: ClinicDataStore -> CSV file
    public void exportAvailability(String filePath) throws IOException {

        List<AvailabilitySlot> slots =
                dataStore.getAvailabilitySlots();

        List<String> lines = new ArrayList<>();

        lines.add("First Name,Last Name,Title,Specialty,Date,Start,End");

        for (AvailabilitySlot slot : slots) {

            HealthcareProfessional professional =
                    slot.getProfessional();

            String line = String.join(",",
                    professional.getFirstName(),
                    professional.getLastName(),
                    professional.getTitle(),
                    professional.getSpecialty(),
                    slot.getStartTime().toLocalDate().toString(),
                    slot.getStartTime().toLocalTime().toString(),
                    slot.getEndTime().toLocalTime().toString()
            );

            lines.add(line);
        }

        Files.write(Path.of(filePath), lines);
    }
}
