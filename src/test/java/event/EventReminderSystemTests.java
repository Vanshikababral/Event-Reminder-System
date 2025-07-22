package event;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDateTime;

class EventReminderSystemTests {

    @Test
    void testEventCreation() {
        LocalDateTime now = LocalDateTime.now();
        
        // Correct constructor call - matches your Event class
        Event event1 = new Event("Meeting", "Team meeting", now, EventPriority.HIGH, false);
        Event event2 = new Event("Meeting", "Team meeting", now, EventPriority.HIGH, false);

        // Test that auto-generated IDs are different
        assertNotEquals(event1.getId(), event2.getId());
        
        assertEquals(event1.getTitle(), event2.getTitle());
        assertEquals(event1.getPriority(), event2.getPriority());
    }

    @Test
    void testEventNull() {
        Event event = null;
        assertNull(event);
    }

    @Test
    void testEventPriority() {
        Event event = new Event("Urgent", "Fix production issue", 
                            LocalDateTime.now(), EventPriority.HIGH, false);
        assertEquals(EventPriority.HIGH, event.getPriority());
    }

    @Test
    void testRecurringEvent() {
        Event event = new Event("Standup", "Daily meeting", 
                            LocalDateTime.now(), EventPriority.MEDIUM, true);
        assertTrue(event.isRecurring());
    }

    @Test
    void testEmptyTitle() {
    assertThrows(IllegalArgumentException.class, () -> {
        new Event("", "No title", LocalDateTime.now(), EventPriority.LOW, false);
    });
    }

    @Test
    void testEventTimeComparison() {
    LocalDateTime earlier = LocalDateTime.now();
    LocalDateTime later = earlier.plusHours(1);
    
    Event first = new Event("First", "Earlier event", earlier, EventPriority.MEDIUM, false);
    Event second = new Event("Second", "Later event", later, EventPriority.MEDIUM, false);
    
    assertTrue(first.getEventTime().isBefore(second.getEventTime()));
    }
}