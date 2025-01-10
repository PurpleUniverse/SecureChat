package SecurityChat.service;

import SecurityChat.model.ChatMessage;
import SecurityChat.model.User;
import SecurityChat.repository.ChatMessageRepository;
import SecurityChat.repository.UserRepository;
import SecurityChat.util.CryptoUtils;
import org.springframework.stereotype.Service;
import SecurityChat.util.SecurityLogger;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;

@Service
public class ChatService {
    private final ChatMessageRepository chatMessageRepository;
    private final KeyExchangeService keyExchangeService;
    private final UserRepository userRepository;
    private final SecurityLogger securityLogger;

    public ChatService(ChatMessageRepository chatMessageRepository,
                       KeyExchangeService keyExchangeService,
                       UserRepository userRepository,
                       SecurityLogger securityLogger) {
        this.chatMessageRepository = chatMessageRepository;
        this.keyExchangeService = keyExchangeService;
        this.userRepository = userRepository;
        this.securityLogger = securityLogger;
    }

    public void sendMessage(User sender, User recipient, String message) {
        SecretKey sharedSecret = keyExchangeService.generateSharedSecret(sender, recipient);
        byte[] encryptedContent = CryptoUtils.encrypt(message.getBytes(), sharedSecret);
        byte[] mac = CryptoUtils.computeMAC(encryptedContent, sharedSecret);

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setSender(sender.getUsername());
        chatMessage.setRecipient(recipient.getUsername());
        chatMessage.setContent(message);
        chatMessage.setTimestamp(LocalDateTime.now());
        chatMessage.setEncryptedContent(encryptedContent);
        chatMessage.setMac(mac);

        chatMessageRepository.save(chatMessage);
        securityLogger.logMessageSent(sender.getUsername(), recipient.getUsername());
    }

    public ChatMessage receiveMessage(User user, byte[] encryptedMessage, byte[] mac) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setEncryptedContent(encryptedMessage);
        chatMessage.setMac(mac);

        User senderUser = userRepository.findByUsername(chatMessage.getSender());
        SecretKey sharedSecret = keyExchangeService.generateSharedSecret(senderUser, user);

        if (CryptoUtils.verifyMAC(encryptedMessage, mac, sharedSecret)) {
            byte[] decryptedContent = CryptoUtils.decrypt(encryptedMessage, sharedSecret);
            chatMessage.setContent(new String(decryptedContent));
            securityLogger.logMessageReceived(chatMessage.getSender(), user.getUsername());
        } else {
            chatMessage.setContent("Message integrity check failed");
            securityLogger.logMessageIntegrityFailure(chatMessage.getSender(), user.getUsername());
        }

        return chatMessage;
    }
}

