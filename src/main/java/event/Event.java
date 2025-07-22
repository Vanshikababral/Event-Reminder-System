package event;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class Event {
    private static final DateTimeFormatter DATE_FORMAT = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    
    private String id;
    private String title;
    private String description;
    private LocalDateTime eventTime;
    private EventPriority priority;
    private boolean isRecurring;

    public Event(String title, String description, LocalDateTime eventTime, 
                EventPriority priority, boolean isRecurring) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be null or empty");
        }
        this.id = generateId();
        this.title = title;
        this.description = description;
        this.eventTime = eventTime;
        this.priority = priority;
        this.isRecurring = isRecurring;
    }

    private String generateId() {
    return "EVT-" + UUID.randomUUID().toString();
}

    // Getters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public LocalDateTime getEventTime() { return eventTime; }
    public EventPriority getPriority() { return priority; }
    public boolean isRecurring() { return isRecurring; }

    public String getFormattedTime() {
        return eventTime.format(DATE_FORMAT);
    }

    @Override
    public String toString() {
        return String.format("%s (Priority: %s, Time: %s)",
            title, priority, getFormattedTime());
    }
}