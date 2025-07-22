package event;

import java.util.Comparator;


public class EventComparator implements Comparator<Event> {
    @Override
    public int compare(Event e1, Event e2) {
        int timeCompare = e1.getEventTime().compareTo(e2.getEventTime());
        if (timeCompare != 0) return timeCompare;
        return e1.getPriority().compareTo(e2.getPriority());
    }
}