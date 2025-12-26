package com.unicycle.auth.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.unicycle.auth.entity.User;
import com.unicycle.auth.repository.UserRepository;
import com.unicycle.exception.UserNotFoundException;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository, EmailService emailService) {
        this.userRepository = userRepository;
    }

    // TODO: again consider removing this for all users to access, maybe keep
    // if admin would care to use this function
    public List<User> allUsers() {
        List<User> users = new ArrayList<>();
        userRepository.findAll().forEach(users::add);
        return users;
    }

    public User findByUserId(UUID userId) {
        return userRepository.findByUserId(userId)
            .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
    }

    public User findByEmail(String userEmail) {
        return userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new UserNotFoundException("User not found with email: " + userEmail));
    }
}

