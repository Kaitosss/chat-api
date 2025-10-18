package com.example.chatapp.controller;


import com.example.chatapp.model.ChatMessage;
import com.example.chatapp.repository.ChatMessageRepository;
import com.example.chatapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

@Controller
public class ChatController {

    @Autowired
    private UserService userService;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.addUser")
    @SendTo("/topic/public")
    public ChatMessage addUser(@Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor){

            if (userService.userExists(chatMessage.getSender())){
                headerAccessor.getSessionAttributes().put("username",chatMessage.getSender());
                userService.setUserOnlineStatus(chatMessage.getSender(),true);

                System.out.println("User added Successfully" + chatMessage.getSender() + "with session ID" +
                        headerAccessor.getSessionId());
                chatMessage.setTimeStemp(LocalDateTime.now());
                if (chatMessage.getContent() == null){
                    chatMessage.setContent("");
                }
                return chatMessageRepository.save(chatMessage);
            }

        return null;
    }

    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public ChatMessage sendMessage(@Payload ChatMessage chatMessage){
        if (userService.userExists(chatMessage.getSender())){
            if(chatMessage.getTimeStemp() == null){
                chatMessage.setTimeStemp(LocalDateTime.now());
            }

            if (chatMessage.getContent() == null){
                chatMessage.setContent("");
            }

            return chatMessageRepository.save(chatMessage);
        }

        return null;
    }

    @MessageMapping("/chat.sendPrivateMessage")
    public void sendPrivateMessage(@Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor){
        if (userService.userExists(chatMessage.getSender()) && userService.userExists(chatMessage.getRecepient())){
            if(chatMessage.getTimeStemp() == null){
                chatMessage.setTimeStemp(LocalDateTime.now());
            }

            if (chatMessage.getContent() == null){
                chatMessage.setContent("");
            }

            ChatMessage saveMessage = chatMessageRepository.save(chatMessage);
            System.out.println("Message saved successfully with id" + saveMessage.getId());

            try{
                String recepientDestination = "/user/" + chatMessage.getRecepient() + "/queue/private";
                System.out.println("Sending message to recepient destination" + recepientDestination);
                messagingTemplate.convertAndSend(recepientDestination,saveMessage);

                String senderDestination = "/user/" + chatMessage.getSender() + "/queue/private";
                System.out.println("Sending to sender destination" + senderDestination);
                messagingTemplate.convertAndSend(senderDestination,saveMessage);
            }
            catch (Exception e) {
                System.out.println("Error occured while the message " + e.getMessage());
                e.printStackTrace();
            }
        }
        else{
            System.out.println("Error Sender " + chatMessage.getSender() + " or recepient " + chatMessage.getRecepient() + " does not exist");
        }
    }
}
