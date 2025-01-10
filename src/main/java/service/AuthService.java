package service;

import model.User;
import repository.UserRepository;
import util.SecurityLogger;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final SecurityLogger securityLogger;

    public AuthService(UserRepository userRepository, SecurityLogger securityLogger) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.securityLogger = securityLogger;
    }

    public void registerUser(String username, String password) {
        String hashedPassword = passwordEncoder.encode(password);
        User user = new User(username, hashedPassword);
        userRepository.save(user);
    }

    public boolean authenticateUser(String username, String password) {
        User user = userRepository.findByUsername(username);
        if (user != null) {
            boolean isValid = passwordEncoder.matches(password, user.getPassword());
            securityLogger.logLoginAttempt(username, isValid);
            return isValid;
        }
        return false;
    }
}
