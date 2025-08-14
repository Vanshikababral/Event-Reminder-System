package event;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDateTime;

class EventReminderSystemTests {

    @Test
    void testEventCreation() {
        LocalDateTime now = LocalDateTime.now();
        
        // Corrected constructor call with the new "Personal" category
        Event event1 = new Event("Meeting", "Team meeting", now, EventPriority.HIGH, false, "Personal");
        Event event2 = new Event("Meeting", "Team meeting", now, EventPriority.HIGH, false, "Personal");

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
        // Corrected constructor call with the new "Urgent" category
        Event event = new Event("Urgent", "Fix production issue", 
                                LocalDateTime.now(), EventPriority.HIGH, false, "Urgent");
        assertEquals(EventPriority.HIGH, event.getPriority());
    }

    @Test
    void testRecurringEvent() {
        // Corrected constructor call with the new "Work" category
        Event event = new Event("Standup", "Daily meeting", 
                                LocalDateTime.now(), EventPriority.MEDIUM, true, "Work");
        assertTrue(event.isRecurring());
    }

    @Test
    void testEmptyTitle() {
        assertThrows(IllegalArgumentException.class, () -> {
            // Corrected constructor call with the new "Personal" category
            new Event("", "No title", LocalDateTime.now(), EventPriority.LOW, false, "Personal");
        });
    }

    @Test
    void testEventTimeComparison() {
        LocalDateTime earlier = LocalDateTime.now();
        LocalDateTime later = earlier.plusHours(1);
        
        // Corrected constructor calls with the new "Personal" category
        Event first = new Event("First", "Earlier event", earlier, EventPriority.MEDIUM, false, "Personal");
        Event second = new Event("Second", "Later event", later, EventPriority.MEDIUM, false, "Personal");
        
        assertTrue(first.getEventTime().isBefore(second.getEventTime()));
    }
}