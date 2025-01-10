package SecurityChat.controller;


import SecurityChat.model.ChatMessage;
import SecurityChat.model.User;
import SecurityChat.repository.UserRepository;
import SecurityChat.service.ChatService;
import SecurityChat.service.KeyExchangeService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {
    private final ChatService chatService;
    private final KeyExchangeService keyExchangeService;
    private final UserRepository userRepository;

    public ChatController(ChatService chatService, KeyExchangeService keyExchangeService, UserRepository userRepository) {
        this.chatService = chatService;
        this.keyExchangeService = keyExchangeService;
        this.userRepository = userRepository;
    }

    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public ChatMessage sendMessage(@Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {
        User sender = (User) headerAccessor.getSessionAttributes().get("user");
        User recipient = userRepository.findByUsername(chatMessage.getRecipient());
        chatService.sendMessage(sender, recipient, chatMessage.getContent());
        return chatMessage;
    }

    @MessageMapping("/chat.addUser")
    @SendTo("/topic/public")
    public ChatMessage addUser(@Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {
        String username = chatMessage.getSender();
        User user = userRepository.findByUsername(username);
        headerAccessor.getSessionAttributes().put("user", user);
        return chatMessage;
    }
}
