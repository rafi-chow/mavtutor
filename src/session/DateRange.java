package session;

import java.util.Scanner;
import java.io.PrintStream;

/**
 * period of time on a single date
 * uses 24h time
 * for tutoring
 */

public class DateRange {
    private String date;
    private String startTime;
    private String endTime;
    /**
     * makes daterange with date, start and end time
     * @param date date of session
     * @param startTime time it starts (24h)
     * @param endTime time it ends (24h)
     */
    public DateRange(String date, String startTime, String endTime) {
        if (date == null || date.isEmpty()) {
            throw new IllegalArgumentException("date invalid");
        }
        if (startTime == null || startTime.isEmpty()) {
            throw new IllegalArgumentException("startTime invalid");
        }

        if (endTime == null || endTime.isEmpty()) {
            throw new IllegalArgumentException("endTime invalid");
        } 
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    /**
     * makes daterange and figures out end time from duration
     * @param date date of session
     * @param startTime time it starts (24h)
     * @param duration how long in minutes
     */
    public DateRange(String date, String startTime, long duration) {
        if (date == null || date.isEmpty()) {
            throw new IllegalArgumentException("date invalid");
        }
        if (startTime == null || startTime.isEmpty()) {
            throw new IllegalArgumentException("startTime invalid");
        }

        if (duration < 0) {
            throw new IllegalArgumentException("bad duration");
        }
        this.date = date;
        this.startTime = startTime;

        
        int startHour = Integer.parseInt(startTime.substring(0, 2));
        int startMinute = Integer.parseInt(startTime.substring(3));

        int totalStartMinutes = (startHour * 60) + startMinute;

        long totalEndMinutes = totalStartMinutes + duration;
        
        int endHour = (int) (totalEndMinutes / 60);
        int endMinute = (int) (totalEndMinutes % 60);

        this.endTime = String.format("%02d:%02d", endHour, endMinute);
    }
    /**
     * Writes the date, start time, and end time to a PrintStream.
     * @param out the stream to write to
     */
    public void save(PrintStream out) {
        out.println(date);
        out.println(startTime);
        out.println(endTime);
    }
    /**
     * Restores a DateRange by reading data from a Scanner.
     * Must read fields in the same order they were saved.
     * @param in the scanner to read from
     */
    public DateRange(Scanner in) {
        this(in.nextLine(), in.nextLine(), in.nextLine());

    }
     /**
     * finds how many minutes between start and end
     * @return duration in minutes
     */
    public long duration() {
        int startHour = Integer.parseInt(startTime.substring(0, 2));
        int startMinute = Integer.parseInt(startTime.substring(3));
        int endHour = Integer.parseInt(endTime.substring(0, 2));
        int endMinute = Integer.parseInt(endTime.substring(3));

        long totalStartMinutes = (startHour * 60L) + startMinute;
        long totalEndMinutes = (endHour * 60L) + endMinute;

        return totalEndMinutes - totalStartMinutes;

    }
    /**
     * prints date, start, end, and duration
     * @return date/time info as text
     */
    @Override
    public String toString() {
        return date + " " + startTime + " - " + endTime + " (" + duration() + " minutes)";
    }
}
