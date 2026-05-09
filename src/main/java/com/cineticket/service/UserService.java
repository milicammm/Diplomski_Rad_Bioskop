package com.cineticket.service;

import com.cineticket.dto.request.RegisterRequest;
import com.cineticket.dto.response.UserResponse;
import com.cineticket.entity.User;

import java.util.List;

public interface UserService {

    UserResponse createUser(RegisterRequest request);
    
    UserResponse getUserById(Long id);
    
    UserResponse getUserByUsername(String username);
    
    List<UserResponse> getAllUsers();
    
    UserResponse updateUser(Long id, RegisterRequest request);
    
    void deleteUser(Long id);
    
    User findUserEntityById(Long id);
    
    User findUserEntityByUsername(String username);
}
