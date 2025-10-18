package com.example.chatapp.service;

import com.example.chatapp.dto.LoginRequestDTO;
import com.example.chatapp.dto.LoginResponseDTO;
import com.example.chatapp.dto.RegisterRequestDTO;
import com.example.chatapp.dto.UserDTO;
import com.example.chatapp.jwt.JwtService;
import com.example.chatapp.model.User;
import com.example.chatapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtService jwtService;

    public UserDTO signup(RegisterRequestDTO registerRequestDTO){
        if(userRepository.findByUsername(registerRequestDTO.getUsername()).isPresent()){
            throw new RuntimeException("Username is already in use");
        }

        User user = new User();
        user.setUsername(registerRequestDTO.getUsername());
        user.setPassword(passwordEncoder.encode((registerRequestDTO.getPassword())));
        user.setEmail(registerRequestDTO.getEmail());

        User saveUser = userRepository.save(user);
        return convertUserDTO(saveUser);
    }

    public LoginResponseDTO login(LoginRequestDTO loginRequestDTO){
        User user = userRepository.findByUsername(loginRequestDTO.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequestDTO.getUsername(),loginRequestDTO.getPassword()));
        String jwtToken = jwtService.generateToken(user);
        return LoginResponseDTO
                .builder()
                .token(jwtToken)
                .userDTO(convertUserDTO(user))
                .build();
    }

    public ResponseEntity<String> logout(){
        ResponseCookie responseCookie = ResponseCookie.from("JWT","")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE,responseCookie.toString())
                .body("Logged out successfully");

    }

    public UserDTO convertUserDTO(User user){
        UserDTO userDTO = new UserDTO();
        userDTO.setEmail(user.getEmail());
        userDTO.setUsername(user.getUsername());
        userDTO.setId(user.getId());

        return userDTO;
    }
}
