package manager;

import event.Event;
import java.util.Timer;
import java.util.TimerTask;


public class NotificationService {
    private final ReminderManager manager;
    private final Timer timer;

    public NotificationService(ReminderManager manager) {
        this.manager = manager;
        this.timer = new Timer(true);
    }

    public void start() {
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                checkNotifications();
            }
        }, 0, 60000); // Check every minute
    }

    private void checkNotifications() {
        manager.getAllEvents().stream()
            .filter(event -> event.getEventTime().isBefore(
                java.time.LocalDateTime.now().plusMinutes(15)))
            .forEach(this::sendNotification);
    }

    private void sendNotification(Event event) {
        System.out.printf("NOTIFICATION: %s is coming up at %s%n",
            event.getTitle(), event.getFormattedTime());
    }

    public void stop() {
        timer.cancel();
    }
}