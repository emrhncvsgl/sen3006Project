package util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Utility class for date and time operations.
 */
public class DateUtils {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    
    /**
     * Format a LocalDateTime to a display-friendly string.
     * @param dateTime The LocalDateTime to format
     * @return Formatted date and time string
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "No deadline";
        }
        return dateTime.format(DATE_TIME_FORMATTER);
    }
    
    /**
     * Check if a given date and time is within 24 hours from now.
     * @param dateTime The date and time to check
     * @return true if within 24 hours, false otherwise
     */
    public static boolean isWithin24Hours(LocalDateTime dateTime) {
        if (dateTime == null) {
            return false;
        }
        
        LocalDateTime now = LocalDateTime.now();
        long hoursBetween = ChronoUnit.HOURS.between(now, dateTime);
        
        // Return true if the deadline is in the future and within 24 hours
        return hoursBetween >= 0 && hoursBetween <= 24;
    }
    
    /**
     * Check if a given date and time has passed.
     * @param dateTime The date and time to check
     * @return true if the deadline has passed, false otherwise
     */
    public static boolean isOverdue(LocalDateTime dateTime) {
        if (dateTime == null) {
            return false;
        }
        
        LocalDateTime now = LocalDateTime.now();
        return dateTime.isBefore(now);
    }
}
