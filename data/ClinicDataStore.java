
package data;

import AppointmentAndAvailability.Appointment;
import AppointmentAndAvailability.Patient;
import AppointmentAndAvailability.AvailabilitySlot;

import java.util.ArrayList;
import java.util.List;

public class ClinicDataStore {

    // Store the patients, availability slots, and appointments
    private final List<Patient> patients = new ArrayList<>();
    private final List<AvailabilitySlot> slots = new ArrayList<>();
    private final List<Appointment> appointments = new ArrayList<>();

    // Add a patient
    public void addPatient(Patient patient) {
        patients.add(patient);
    }

    // Find a patient by ID
    public Patient findPatient(Long patientId) {
        for (Patient patient : patients) {
            if (patient.getPatientId().equals(patientId)) {
                return patient;
            }
        }
        return null;
    }

    // Add an availability slot
    public void addAvailabilitySlot(AvailabilitySlot slot) {
        slots.add(slot);
    }

    // Find an availability slot by ID
    public AvailabilitySlot findAvailabilitySlot(Long slotId) {
        for (AvailabilitySlot slot : slots) {
            if (slot.getSlotId().equals(slotId)) {
                return slot;
            }
        }
        return null;
    }

    // Save an appointment
    public void saveAppointment(Appointment appointment) {
        appointments.add(appointment);
    }

    // Check whether a slot has already been booked
    public boolean isSlotBooked(Long slotId) {
        for (Appointment appointment : appointments) {
            if (appointment.getSlot().getSlotId().equals(slotId)
                    && appointment.getStatus().equals("CONFIRMED")) {
                return true;
            }
        }
        return false;
    }

    // Retrieve all availability slots for CSV export
    public List<AvailabilitySlot> getAvailabilitySlots() {
        return new ArrayList<>(slots);
    }
}
