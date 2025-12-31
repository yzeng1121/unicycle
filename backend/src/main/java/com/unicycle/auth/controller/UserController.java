package com.unicycle.auth.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.unicycle.auth.dto.CurrentUserDto;
import com.unicycle.auth.entity.User;
import com.unicycle.auth.service.UserService;
import com.unicycle.profile.dto.HeaderProfileDto;

import lombok.AllArgsConstructor;

@RequestMapping("/users")
@RestController
@AllArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<CurrentUserDto> authenticatedUser() {
        return ResponseEntity.ok(userService.getCurrentUserDto());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/")
    public ResponseEntity<List<User>> allUsers() {
        List <User> users = userService.allUsers();
        return ResponseEntity.ok(users);
    }

    // get user basic data (following list, listing page, etc.)
    @GetMapping("/{userId}/get-header")
    public ResponseEntity<HeaderProfileDto> getUserHeader(@PathVariable UUID userId) {
        return ResponseEntity.ok(userService.getHeaderProfileDto(userId));
    }

    @GetMapping("/{userId}/get-username")
    public ResponseEntity<String> getUsername(@PathVariable UUID userId) {
        return ResponseEntity.ok(userService.findUsername(userId));  
    }
}
