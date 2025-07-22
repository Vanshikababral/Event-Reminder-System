import manager.NotificationService;
import manager.ReminderManager;
import webserver.SimpleHttpServer;
import java.io.IOException;


public class Main {
    public static void main(String[] args) {
        ReminderManager manager = new ReminderManager();
        NotificationService notificationService = new NotificationService(manager);
        notificationService.start();

        try {
            SimpleHttpServer.start();
            
            // Add shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                notificationService.stop();
                SimpleHttpServer.stop();
            }));
        } catch (IOException e) {
            System.err.println("Failed to start server: " + e.getMessage());
            System.exit(1);
        }
    }
}