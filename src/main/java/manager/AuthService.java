package manager;

import event.User;
import org.mindrot.jbcrypt.BCrypt;

import java.util.UUID;

public class AuthService {
    private final UserManager userManager;

    public AuthService(UserManager userManager) {
        this.userManager = userManager;
    }

    public boolean signup(String username, String password) {
        if (userManager.getUserByUsername(username).isPresent()) {
            return false; // User already exists
        }
        String hashedPassword = hashPassword(password);
        User newUser = new User(username, hashedPassword);
        userManager.addUser(newUser);
        return true;
    }

    public boolean login(String username, String password) {
        return userManager.getUserByUsername(username)
                .map(user -> verifyPassword(password, user.getPasswordHash()))
                .orElse(false);
    }

    public String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    public boolean verifyPassword(String password, String hashedPassword) {
        return BCrypt.checkpw(password, hashedPassword);
    }
    
    // A simple token for this example
    public String generateAuthToken() {
        return UUID.randomUUID().toString();
    }
}
   