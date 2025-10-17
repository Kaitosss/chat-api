package com.example.chatapp.model;


import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "message")
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String content;

    private String sender;

    private String recepient;

    private String color;

    @Column(nullable = false)
    private LocalDateTime timeStemp;

    @Enumerated(EnumType.STRING)
    private MessageType type;

    public enum MessageType {
        CHAT ,PRIVATEMESSAGE ,JOIN ,LEAVE ,TYPING
    }

}
