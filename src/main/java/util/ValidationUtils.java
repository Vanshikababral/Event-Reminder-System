package util;

public class ValidationUtils {
    public static void validateEventInput(String title, String dateTimeStr) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Event title cannot be empty");
        }
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Event time cannot be empty");
        }
    }
}