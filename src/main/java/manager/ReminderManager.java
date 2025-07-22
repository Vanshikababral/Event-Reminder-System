package manager;

import event.Event;
import event.EventComparator;
import java.util.*;

public class ReminderManager {
    private final PriorityQueue<Event> eventQueue;
    private final Map<String, Event> eventMap;

    public ReminderManager() {
        this.eventQueue = new PriorityQueue<>(new EventComparator());
        this.eventMap = new HashMap<>();
    }

    public synchronized void addEvent(Event event) {
        if (event == null) {
            throw new IllegalArgumentException("Event cannot be null");
        }
        if (event.getTitle() == null || event.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be null or empty");
        }
        eventQueue.add(event);
        eventMap.put(event.getId(), event);
    }

    public synchronized boolean removeEvent(String eventId) {
        Event event = eventMap.remove(eventId);
        if (event != null) {
            return eventQueue.remove(event);
        }
        return false;
    }

    public synchronized List<Event> getAllEvents() {
    return Collections.unmodifiableList(new ArrayList<>(eventQueue));
}

    public synchronized Optional<Event> getNextEvent() {
        return Optional.ofNullable(eventQueue.peek());
    }
}