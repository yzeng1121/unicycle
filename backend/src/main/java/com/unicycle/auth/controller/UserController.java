package com.unicycle.auth.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.unicycle.auth.entity.User;
import com.unicycle.auth.repository.UserRepository;
import com.unicycle.auth.service.UserService;
import com.unicycle.profile.dto.HeaderProfileDto;
import com.unicycle.profile.dto.UserBasicDto;
import com.unicycle.profile.dto.UserProfileDto;
import com.unicycle.profile.repository.UserProfilesRepository;

@RequestMapping("/users")
@RestController
public class UserController {
    private final UserService userService;
    private final UserRepository userRepository;
    private final UserProfilesRepository userProfilesRepository;
    
    public UserController(UserService userService, UserRepository userRepository, UserProfilesRepository userProfilesRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.userProfilesRepository = userProfilesRepository;
    }

    // TODO: this is mainly used for testing, so will need to modify to cater to
    // TODO: error username == email == 'user's email'
    // the purpose of my app
    @GetMapping("/me")
    public ResponseEntity<User> authenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        return ResponseEntity.ok(currentUser);
    }

    // TODO: this function should NOT be public for any users besides amin
    // is a big security issue
    @GetMapping("/")
    public ResponseEntity<List<User>> allUsers() {
        List <User> users = userService.allUsers();
        return ResponseEntity.ok(users);
    }

    // get user basic data (following list, listing page, etc.)
    @GetMapping("/{userId}/get-header")
    public ResponseEntity<?> getUserHeader(@PathVariable UUID userId) {
        try {
            UserBasicDto userBasicDto = userRepository.getUserInUsersTableByUserId(userId); 
            UserProfileDto userProfileDto = userProfilesRepository.getUserInUserProfilesTableByUserId(userId);

            HeaderProfileDto headerProfile = HeaderProfileDto.builder()
                .userId(userId)
                .username(userBasicDto.getUsername())
                .firstName(userBasicDto.getFirstName())
                .lastName(userBasicDto.getLastName())
                .profileImageUrl(userProfileDto.getProfileImage())
                .build();

            // return ResponseEntity.ok(Map.of(
            //     "success", true,
            //     "imageUrl", headerProfile,
            //     "message", "User: " + headerProfile + " fetched."
            // ));
            return ResponseEntity.ok(headerProfile);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Failed to fetch user: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/{userId}/get-username")
    public ResponseEntity<?> getUsername(@PathVariable UUID userId) {
        try {
            String username = userRepository.getUsernameByUserId(userId);
            System.out.println("Fetched cover username: " + username);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "imageUrl", username,
                "message", "Username: " + username + " fetched."
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Failed to fetch username: " + e.getMessage()
            ));
        }
    }
}
