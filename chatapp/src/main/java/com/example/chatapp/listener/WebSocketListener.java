package com.example.chatapp.listener;


import com.example.chatapp.model.ChatMessage;
import com.example.chatapp.service.UserService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.logging.Logger;

@Component
public class WebSocketListener {

    @Autowired
    private UserService userService;

    private SimpMessagingTemplate messagingTemplate;

    private static final Logger logger = (Logger) LoggerFactory.getLogger(WebSocketListener.class);

    @EventListener
    public void handleWebsocketConnectListener(SessionConnectedEvent event){
        logger.info("Connected to WebSocket");
    }

    public void handleWebsocketDisConnectListener(SessionDisconnectEvent event){
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String username = headerAccessor.getSessionAttributes().get("username").toString();

        System.out.println("User disconnected to WebSocket");
        userService.setUserOnlineStatus(username,false);
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setType(ChatMessage.MessageType.LEAVE);
        chatMessage.setSender(username);

        messagingTemplate.convertAndSend("/topic/public",chatMessage);
    }
}
