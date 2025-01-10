package service;

import model.ChatMessage;
import model.User;
import repository.ChatMessageRepository;
import repository.UserRepository;
import util.CryptoUtils;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;

@Service
public class ChatService {
    private final ChatMessageRepository chatMessageRepository;
    private final KeyExchangeService keyExchangeService;
    private final UserRepository userRepository;

    public ChatService(ChatMessageRepository chatMessageRepository, KeyExchangeService keyExchangeService, UserRepository userRepository) {
        this.chatMessageRepository = chatMessageRepository;
        this.keyExchangeService = keyExchangeService;
        this.userRepository = userRepository;
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
        } else {
            chatMessage.setContent("Message integrity check failed");
        }

        return chatMessage;
    }

    public void storeMessage(ChatMessage chatMessage) {
        chatMessageRepository.save(chatMessage);
    }

    public Iterable<ChatMessage> getMessageHistory(User user) {
        return chatMessageRepository.findAll();
    }
}

