package AppointmentAndAvailability;

import java.time.DayOfWeek;
import java.time.LocalTime;

/** A professional's regular working hours for one day of the week. */
public class Schedule {
    private final String dayOfWeek;
    private final LocalTime startTime;
    private final LocalTime endTime;

    public Schedule(String dayOfWeek, LocalTime startTime, LocalTime endTime) {
        if (dayOfWeek == null || startTime == null || endTime == null) {
            throw new IllegalArgumentException("Schedule information cannot be null.");
        }
        String normalizedDay = dayOfWeek.trim().toUpperCase();
        try {
            DayOfWeek.valueOf(normalizedDay);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Day must be a weekday name, such as MONDAY.", e);
        }
        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("Schedule end time must be after start time.");
        }
        this.dayOfWeek = normalizedDay;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getDayOfWeek() { return dayOfWeek; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }
}
