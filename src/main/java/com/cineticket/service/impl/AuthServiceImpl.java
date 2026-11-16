package com.cineticket.service.impl;

import com.cineticket.dto.request.LoginRequest;
import com.cineticket.dto.request.RegisterRequest;
import com.cineticket.dto.response.LoginResponse;
import com.cineticket.dto.response.UserResponse;
import com.cineticket.entity.RefreshToken;
import com.cineticket.entity.User;
import com.cineticket.exception.UnauthorizedException;
import com.cineticket.repository.RefreshTokenRepository;
import com.cineticket.security.JwtUtil;
import com.cineticket.service.AuthService;
import com.cineticket.service.UserService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;

@Service
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final UserService userService;
    private final RefreshTokenRepository refreshTokenRepository;

    public AuthServiceImpl(AuthenticationManager authenticationManager, JwtUtil jwtUtil,
                           UserDetailsService userDetailsService, UserService userService,
                           RefreshTokenRepository refreshTokenRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.userService = userService;
        this.refreshTokenRepository = refreshTokenRepository;
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

        User user = userService.findUserEntityByUsername(userDetails.getUsername());
        persistRefreshToken(refreshToken, user);

        UserResponse userResponse = userService.getUserByUsername(userDetails.getUsername());

        return new LoginResponse(accessToken, refreshToken, userResponse);
    }

    @Override
    public UserResponse register(RegisterRequest request) {
        return userService.createUser(request);
    }

    @Override
    @Transactional
    public LoginResponse refreshToken(String refreshToken) {

        String username = jwtUtil.extractUsername(refreshToken);

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        if (!jwtUtil.validateToken(refreshToken, userDetails)) {
            throw new UnauthorizedException("Invalid refresh token");
        }

        RefreshToken storedToken = refreshTokenRepository.findByTokenHash(jwtUtil.hashToken(refreshToken))
            .orElseThrow(() -> new UnauthorizedException("Refresh token is unknown or has already been used"));

        if (storedToken.isRevoked()) {
            throw new UnauthorizedException("Refresh token has been revoked");
        }

        // Rotate: this refresh token can only be used once, so a replayed/stolen copy stops working.
        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);

        String newAccessToken = jwtUtil.generateAccessToken(userDetails);
        String newRefreshToken = jwtUtil.generateRefreshToken(userDetails);
        persistRefreshToken(newRefreshToken, storedToken.getUser());

        UserResponse user = userService.getUserByUsername(username);

        return new LoginResponse(newAccessToken, newRefreshToken, user);
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        refreshTokenRepository.findByTokenHash(jwtUtil.hashToken(refreshToken))
            .ifPresent(storedToken -> {
                storedToken.setRevoked(true);
                refreshTokenRepository.save(storedToken);
            });
    }

    private void persistRefreshToken(String rawToken, User user) {
        RefreshToken entity = new RefreshToken();
        entity.setUser(user);
        entity.setTokenHash(jwtUtil.hashToken(rawToken));
        entity.setExpiresAt(
            jwtUtil.extractExpiration(rawToken).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
        );
        refreshTokenRepository.save(entity);
    }
}
