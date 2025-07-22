package util;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateUtils {
    private static final DateTimeFormatter API_FORMAT = DateTimeFormatter.ISO_DATE_TIME;
    
    public static LocalDateTime parseApiDateTime(String dateTimeStr) {
        try {
            // First try ISO_DATE_TIME (with timezone)
            return ZonedDateTime.parse(dateTimeStr, API_FORMAT).toLocalDateTime();
        } catch (DateTimeParseException e) {
            // Fallback to ISO_LOCAL_DATE_TIME (without timezone)
            return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }
    }
}