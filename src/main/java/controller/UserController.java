package controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.AuthService;
import manager.UserManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.stream.Collectors;

public class UserController implements HttpHandler {
    private final AuthService authService;
    private final ObjectMapper mapper;

    public UserController(AuthService authService) {
        this.authService = authService;
        this.mapper = new ObjectMapper();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        
        try {
            // New: Handle the specific paths directly.
            if ("/api/signup".equals(path)) {
                handleSignup(exchange);
            } else if ("/api/login".equals(path)) {
                handleLogin(exchange);
            } else {
                sendError(exchange, 404, "Not Found");
            }
        } finally {
            exchange.close();
        }
    }

    public void handleSignup(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendError(exchange, 405, "Method Not Allowed");
            return;
        }

        String requestBody = new BufferedReader(new InputStreamReader(exchange.getRequestBody()))
                .lines().collect(Collectors.joining("\n"));
        UserDto userDto = mapper.readValue(requestBody, UserDto.class);

        if (userDto.username == null || userDto.password == null) {
            sendError(exchange, 400, "Username and password are required.");
            return;
        }

        boolean success = authService.signup(userDto.username, userDto.password);
        if (success) {
            sendResponse(exchange, 200, "{\"message\":\"Signup successful\"}");
        } else {
            sendError(exchange, 409, "User already exists");
        }
    }

    public void handleLogin(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendError(exchange, 405, "Method Not Allowed");
            return;
        }

        String requestBody = new BufferedReader(new InputStreamReader(exchange.getRequestBody()))
                .lines().collect(Collectors.joining("\n"));
        UserDto userDto = mapper.readValue(requestBody, UserDto.class);

        if (userDto.username == null || userDto.password == null) {
            sendError(exchange, 400, "Username and password are required.");
            return;
        }

        boolean authenticated = authService.login(userDto.username, userDto.password);
        if (authenticated) {
            String token = authService.generateAuthToken();
            sendResponse(exchange, 200, "{\"message\":\"Login successful\", \"token\":\"" + token + "\"}");
        } else {
            sendError(exchange, 401, "Invalid username or password");
        }
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(statusCode, response.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }

    private void sendError(HttpExchange exchange, int statusCode, String message) throws IOException {
        String errorJson = "{\"error\":\"" + message + "\"}";
        sendResponse(exchange, statusCode, errorJson);
    }
    
    public static class UserDto {
        public String username;
        public String password;
    }
}
