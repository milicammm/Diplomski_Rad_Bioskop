package com.cineticket.service.impl;

import com.cineticket.dto.request.LoginRequest;
import com.cineticket.dto.request.RegisterRequest;
import com.cineticket.dto.response.LoginResponse;
import com.cineticket.dto.response.UserResponse;
import com.cineticket.security.JwtUtil;
import com.cineticket.service.AuthService;
import com.cineticket.service.UserService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final UserService userService;

    public AuthServiceImpl(AuthenticationManager authenticationManager, JwtUtil jwtUtil,
                           UserDetailsService userDetailsService, UserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.userService = userService;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
     
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getUsername(),
                request.getPassword()
            )
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        String accessToken = jwtUtil.generateAccessToken(userDetails);
        String refreshToken = jwtUtil.generateRefreshToken(userDetails);

        UserResponse user = userService.getUserByUsername(userDetails.getUsername());

        return new LoginResponse(accessToken, refreshToken, user);
    }

    @Override
    public UserResponse register(RegisterRequest request) {
        return userService.createUser(request);
    }

    @Override
    public LoginResponse refreshToken(String refreshToken) {

        String username = jwtUtil.extractUsername(refreshToken);

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        if (!jwtUtil.validateToken(refreshToken, userDetails)) {
            throw new RuntimeException("Invalid refresh token");
        }

        String newAccessToken = jwtUtil.generateAccessToken(userDetails);
        String newRefreshToken = jwtUtil.generateRefreshToken(userDetails);

        UserResponse user = userService.getUserByUsername(username);

        return new LoginResponse(newAccessToken, newRefreshToken, user);
    }
}
