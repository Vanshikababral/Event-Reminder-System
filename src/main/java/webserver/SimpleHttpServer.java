package webserver;

import com.fasterxml.jackson.databind.ObjectMapper;
import manager.ReminderManager;
import event.Event;
import event.EventPriority;
import util.DateUtils;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.*;
import java.net.InetSocketAddress;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;

import manager.UserManager;
import manager.AuthService;
import controller.UserController;


public class SimpleHttpServer {
    private static final int PORT = 8081;
    private static final ReminderManager manager = new ReminderManager();
    private static final UserManager userManager = new UserManager();
    private static final AuthService authService = new AuthService(userManager);
    private static final UserController userController = new UserController(authService);
    
    private static final ObjectMapper mapper = new ObjectMapper()
        .registerModule(new JavaTimeModule());
    private static HttpServer server;

    public static void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(PORT), 0);

        // API Contexts
        server.createContext("/api/events", exchange -> {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                setCorsHeaders(exchange);
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            try {
                setCorsHeaders(exchange);
                switch (exchange.getRequestMethod()) {
                    case "GET" -> handleGetEvents(exchange);
                    case "POST" -> handlePostEvent(exchange);
                    case "DELETE" -> handleDeleteEvent(exchange);
                    default -> exchange.sendResponseHeaders(405, -1);
                }
            } finally {
                exchange.close();
            }
        });
        
        // Corrected: Add a preflight check for the user contexts
        server.createContext("/api/signup", exchange -> {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                setCorsHeaders(exchange);
                exchange.sendResponseHeaders(204, -1);
            } else {
                userController.handleSignup(exchange);
            }
        });
        server.createContext("/api/login", exchange -> {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                setCorsHeaders(exchange);
                exchange.sendResponseHeaders(204, -1);
            } else {
                userController.handleLogin(exchange);
            }
        });
        
        // Static File Server Context
        server.createContext("/", SimpleHttpServer::serveStaticFile);

        server.setExecutor(null);
        server.start();
        System.out.println("Server running on http://localhost:" + PORT);
        
        // Add a shutdown hook to save data before the server closes
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Server shutting down. Saving data...");
            manager.saveEventsToFile();
            userManager.saveUsersToFile();
        }));
    }

    public static void stop() {
        if (server != null) {
            server.stop(0);
            System.out.println("Server stopped");
        }
    }

    private static void serveStaticFile(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if (path.equals("/")) {
            path = "/login.html"; // Redirect to login page
        }
        
        try (InputStream is = SimpleHttpServer.class.getResourceAsStream("/web" + path)) {
            if (is == null) {
                exchange.sendResponseHeaders(404, 0); // Not Found
            } else {
                exchange.sendResponseHeaders(200, is.available());
                OutputStream os = exchange.getResponseBody();
                is.transferTo(os);
            }
        } finally {
            exchange.close();
        }
    }
    
    private static void setCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET,POST,DELETE,OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
    }

    private static void handleGetEvents(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        String category = null;

        if (query != null && query.contains("=")) {
            String[] params = query.split("=");
            if (params.length > 1 && params[0].equalsIgnoreCase("category")) {
                category = params[1];
            }
        }

        List<Event> events;
        if (category == null || "all".equalsIgnoreCase(category)) {
            events = manager.getAllEvents();
        } else {
            events = manager.getEventsByCategory(category);
        }

        List<EventResponseDto> responseEvents = events.stream()
            .map(event -> new EventResponseDto(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getEventTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                event.getPriority().name(),
                event.isRecurring(),
                event.getCategory()
            ))
            .collect(Collectors.toList());

        String response = mapper.writeValueAsString(responseEvents);
        sendResponse(exchange, 200, response);
    }

    private static void handlePostEvent(HttpExchange exchange) throws IOException {
        try {
            String requestBody = new BufferedReader(new InputStreamReader(exchange.getRequestBody()))
                .lines().collect(Collectors.joining("\n"));

            EventDto eventDto = mapper.readValue(requestBody, EventDto.class);

            if (eventDto.title == null || eventDto.title.trim().isEmpty() ||
                eventDto.eventTime == null || eventDto.priority == null) {
                String errorJson = "{\"error\":\"Title, eventTime, and priority are required and cannot be empty\"}";
                sendResponse(exchange, 400, errorJson);
                return;
            }

            LocalDateTime eventTime;
            try {
                eventTime = DateUtils.parseApiDateTime(eventDto.eventTime);
            } catch (DateTimeParseException e) {
                eventTime = LocalDateTime.parse(eventDto.eventTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            }

            Event event = new Event(
                eventDto.title,
                eventDto.description != null ? eventDto.description : "",
                eventTime,
                EventPriority.valueOf(eventDto.priority.toUpperCase()),
                eventDto.isRecurring,
                eventDto.category
            );

            manager.addEvent(event);

            EventResponseDto responseDto = new EventResponseDto(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getEventTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                event.getPriority().name(),
                event.isRecurring(),
                event.getCategory()
            );

            sendResponse(exchange, 201, mapper.writeValueAsString(responseDto));
        } catch (DateTimeParseException e) {
            String errorJson = "{\"error\":\"Invalid dateTime format: " + e.getMessage().replace("\"", "\\\"") + "\"}";
            sendResponse(exchange, 400, errorJson);
        } catch (IllegalArgumentException e) {
            String errorJson = "{\"error\":\"Invalid priority: " + e.getMessage().replace("\"", "\\\"") + "\"}";
            sendResponse(exchange, 400, errorJson);
        } catch (Exception e) {
            e.printStackTrace();
            String errorJson = "{\"error\":\"Server error: " + e.getMessage().replace("\"", "\\\"") + "\"}";
            sendResponse(exchange, 500, errorJson);
        }
    }

    private static void handleDeleteEvent(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String eventId = path.substring(path.lastIndexOf('/') + 1);

        boolean deleted = manager.removeEvent(eventId);
        setCorsHeaders(exchange);
        exchange.sendResponseHeaders(deleted ? 204 : 404, -1);
    }

    private static void sendResponse(HttpExchange exchange, int statusCode, String response)
            throws IOException {
        setCorsHeaders(exchange);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, response.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }
    
    public static class EventDto {
        public String title;
        public String description;
        public String eventTime;
        public String priority;
        public String category;
        public boolean isRecurring;
    }

    public static class EventResponseDto {
        public String id;
        public String title;
        public String description;
        public String eventTime;
        public String priority;
        public boolean isRecurring;
        public String category;

        public EventResponseDto(String id, String title, String description,
                                String eventTime, String priority, boolean isRecurring,
                                String category) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.eventTime = eventTime;
            this.priority = priority;
            this.isRecurring = isRecurring;
            this.category = category;
        }
    }
}
