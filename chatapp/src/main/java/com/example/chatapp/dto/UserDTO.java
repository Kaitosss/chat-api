package com.example.chatapp.dto;


import jakarta.persistence.Column;
import lombok.Data;

@Data
public class UserDTO {
    private Long id;

    @Column(unique = true,nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(unique = true,nullable = false)
    private String email;

    @Column(nullable = false,name = "online")
    private boolean online;
}
