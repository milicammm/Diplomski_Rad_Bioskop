package com.cineticket.controller;

import com.cineticket.dto.request.LoginRequest;
import com.cineticket.dto.request.RegisterRequest;
import com.cineticket.dto.response.LoginResponse;
import com.cineticket.dto.response.MessageResponse;
import com.cineticket.dto.response.UserResponse;
import com.cineticket.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    //POST /api/auth/login
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    //POST /api/auth/register
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    //POST /api/auth/refresh
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refreshToken(@RequestHeader("Authorization") String refreshToken) {
        String token = refreshToken.replace("Bearer ", "");
        LoginResponse response = authService.refreshToken(token);
        return ResponseEntity.ok(response);
    }

    //POST /api/auth/logout
    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout(@RequestHeader(value = "Authorization", required = false) String refreshToken) {
        if (refreshToken != null) {
            authService.logout(refreshToken.replace("Bearer ", ""));
        }
        return ResponseEntity.ok(MessageResponse.success("Logged out"));
    }
}
