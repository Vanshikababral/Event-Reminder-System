package manager;

import event.User;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
/*import java.util.ArrayList;*/
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class UserManager {
    private static final String USERS_FILE = "users.json";
    private final ConcurrentHashMap<String, User> userMap;
    private final ObjectMapper mapper;

    public UserManager() {
        this.userMap = new ConcurrentHashMap<>();
        this.mapper = new ObjectMapper();
        loadUsersFromFile();
    }

    private void loadUsersFromFile() {
        File file = new File(USERS_FILE);
        if (file.exists() && file.length() > 0) {
            try {
                List<User> users = mapper.readValue(file,
                        mapper.getTypeFactory().constructCollectionType(List.class, User.class));
                for (User user : users) {
                    userMap.put(user.getUsername(), user);
                }
                System.out.println("Users loaded successfully from " + USERS_FILE);
            } catch (IOException e) {
                System.err.println("Failed to load users from file: " + e.getMessage());
            }
        }
    }

    public synchronized void saveUsersToFile() {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(USERS_FILE), userMap.values());
            System.out.println("Users saved successfully to " + USERS_FILE);
        } catch (IOException e) {
            System.err.println("Failed to save users to file: " + e.getMessage());
        }
    }

    public synchronized void addUser(User user) {
        userMap.put(user.getUsername(), user);
        saveUsersToFile();
    }

    public Optional<User> getUserByUsername(String username) {
        return Optional.ofNullable(userMap.get(username));
    }
}

