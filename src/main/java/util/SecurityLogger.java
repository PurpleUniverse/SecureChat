package util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SecurityLogger {
    private static final Logger logger = LoggerFactory.getLogger(SecurityLogger.class);

    public void logLoginAttempt(String username, boolean success) {
        if (success) {
            logger.info("Successful login attempt for user: {}", username);
        } else {
            logger.warn("Failed login attempt for user: {}", username);
        }
    }

    public void logMessageSent(String sender, String recipient) {
        logger.info("Message sent from {} to {}", sender, recipient);
    }

    public void logMessageReceived(String sender, String recipient) {
        logger.info("Message received from {} by {}", sender, recipient);
    }

    public void logMessageIntegrityFailure(String sender, String recipient) {
        logger.warn("Message integrity check failed for message from {} to {}", sender, recipient);
    }
}

