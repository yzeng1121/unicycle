package com.unicycle.auth.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.unicycle.auth.dto.CurrentUserDto;
import com.unicycle.auth.entity.User;
import com.unicycle.auth.repository.UserRepository;
import com.unicycle.exception.UserNotFoundException;
import com.unicycle.exception.FailedToFetchUsernameException;
import com.unicycle.exception.InvalidCredentialsException;
import com.unicycle.profile.service.ProfileService;

import lombok.AllArgsConstructor;

import com.unicycle.profile.dto.HeaderProfileDto;
import com.unicycle.profile.dto.UserBasicDto;
import com.unicycle.profile.dto.UserProfileDto;

@Service
@AllArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final ProfileService profileService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    // TODO: again consider removing this for all users to access, maybe keep
    // if admin would care to use this function
    public List<User> allUsers() {
        List<User> users = new ArrayList<>();
        userRepository.findAll().forEach(users::add);
        return users;
    }

    // assumes that user can be found
    // TODO: still iffy abt the errors thrown
    public String findUsername(UUID userId) {
        if (userId == null) throw new InvalidCredentialsException("User id is null.");

        String result = userRepository.getUsernameByUserId(userId);
        
        if (result == null) {
            throw new FailedToFetchUsernameException("Failed to fetch username from user id.");
        }
        result = result.replaceAll("\\s+", "");
        if (result.isEmpty() || result.length() == 0) {
            throw new UsernameNotFoundException("Username not found.");
        }
        return result;
    }

    public User findByUserId(UUID userId) {
        return userRepository.findByUserId(userId)
            .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
    }

    public User findByEmail(String userEmail) {
        if (!emailService.isValidEmail(userEmail)) {
            throw new InvalidCredentialsException("User email is invalid.");
        } 

        return userRepository.findByEmail(userEmail.toLowerCase().replaceAll("\\s+", ""))
            .orElseThrow(() -> new UserNotFoundException("User not found with email: " + userEmail));
    }

    public UserBasicDto getUserBasicDto(UUID userId) {
        UserBasicDto dto = userRepository.getUserBasicDtoByUserId(userId);
        if (dto == null) throw new UserNotFoundException("User not found with id: " + userId);
        return dto;
    } 

    // TODO: consider params in the case getContext somehow fails :(
    public CurrentUserDto getCurrentUserDto() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        CurrentUserDto userDto = CurrentUserDto.builder()
            .userId(currentUser.getUserId())
            .email(currentUser.getEmail())
            .username(currentUser.getUsername())
            .firstName(currentUser.getFirstName())
            .lastName(currentUser.getLastName())
            .build();
        return userDto;
    }

    // TODO: can just fetch profile URL rather than the ENTIRE user profile
    // TODO: which user id?
    public HeaderProfileDto getHeaderProfileDto(UUID userId) {
        UserBasicDto userBasicDto = getUserBasicDto(userId); 
        UserProfileDto userProfileDto = profileService.getUserProfileDto(userId);
        HeaderProfileDto headerProfile = HeaderProfileDto.builder()
            .userId(userId)
            .username(userBasicDto.getUsername())
            .firstName(userBasicDto.getFirstName())
            .lastName(userBasicDto.getLastName())
            .profileImageUrl(userProfileDto.getProfileImage())
            .build();
        return headerProfile;
    }

    public void changePassword(UUID userId, String password) {
        String encodedPassword = passwordEncoder.encode(password);
        int updated = userRepository.changePassword(userId, encodedPassword);
        if (updated == 0) throw new UserNotFoundException("User not found");
        userRepository.changePassword(userId, encodedPassword);
    }
}

