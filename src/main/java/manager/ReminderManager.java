package manager;

import event.Event;
import event.EventComparator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class ReminderManager {
    private static final String DATA_FILE = "events.json";
    private final PriorityQueue<Event> eventQueue;
    private final Map<String, Event> eventMap;
    private final ObjectMapper mapper;

    public ReminderManager() {
        this.mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        this.eventQueue = new PriorityQueue<>(new EventComparator());
        this.eventMap = new HashMap<>();
        loadEventsFromFile();
    }

    private void loadEventsFromFile() {
        File file = new File(DATA_FILE);
        if (file.exists() && file.length() > 0) {
            try {
                List<Event> loadedEvents = mapper.readValue(file,
                        mapper.getTypeFactory().constructCollectionType(List.class, Event.class));
                for (Event event : loadedEvents) {
                    eventQueue.add(event);
                    eventMap.put(event.getId(), event);
                }
                System.out.println("Events loaded successfully from " + DATA_FILE);
            } catch (IOException e) {
                System.err.println("Failed to load events from file: " + e.getMessage());
            }
        }
    }

    public synchronized void saveEventsToFile() {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(DATA_FILE), new ArrayList<>(eventQueue));
            System.out.println("Events saved successfully to " + DATA_FILE);
        } catch (IOException e) {
            System.err.println("Failed to save events to file: " + e.getMessage());
        }
    }

    public synchronized void addEvent(Event event) {
        if (event == null || event.getTitle() == null || event.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Event cannot be null and title cannot be empty.");
        }
        eventQueue.add(event);
        eventMap.put(event.getId(), event);
        saveEventsToFile();
    }

    public synchronized boolean removeEvent(String eventId) {
        Event event = eventMap.remove(eventId);
        if (event != null) {
            boolean removed = eventQueue.remove(event);
            if (removed) {
                saveEventsToFile();
            }
            return removed;
        }
        return false;
    }

    public synchronized List<Event> getAllEvents() {
        return Collections.unmodifiableList(new ArrayList<>(eventQueue));
    }

    public synchronized List<Event> getEventsByCategory(String category) {
        if ("all".equalsIgnoreCase(category)) {
            return getAllEvents();
        }
        return eventMap.values().stream()
                .filter(event -> event.getCategory().equalsIgnoreCase(category))
                .collect(Collectors.toList());
    }

    public synchronized Optional<Event> getNextEvent() {
        return Optional.ofNullable(eventQueue.peek());
    }

    public synchronized void markEventAsNotified(String eventId) {
        Event event = eventMap.get(eventId);
        if (event != null) {
            event.setNotified(true);
            saveEventsToFile();
        }
    }
}
