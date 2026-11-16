package com.cineticket.service;

import com.cineticket.dto.request.LoginRequest;
import com.cineticket.dto.request.RegisterRequest;
import com.cineticket.dto.response.LoginResponse;
import com.cineticket.dto.response.UserResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request);
    
    UserResponse register(RegisterRequest request);
    
    LoginResponse refreshToken(String refreshToken);

    void logout(String refreshToken);
}
