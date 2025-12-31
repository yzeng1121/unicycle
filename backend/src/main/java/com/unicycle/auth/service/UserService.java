package com.unicycle.auth.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.unicycle.auth.dto.CurrentUserDto;
import com.unicycle.auth.entity.User;
import com.unicycle.auth.repository.UserRepository;
import com.unicycle.exception.UserNotFoundException;
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

    // TODO: again consider removing this for all users to access, maybe keep
    // if admin would care to use this function
    public List<User> allUsers() {
        List<User> users = new ArrayList<>();
        userRepository.findAll().forEach(users::add);
        return users;
    }

    public String findUsername(UUID userId) {
        String result = userRepository.getUsernameByUserId(userId);
        if (result == null || result.isEmpty()) {
            throw new UsernameNotFoundException("Username not found.");
        }
        return result;
    }

    public User findByUserId(UUID userId) {
        return userRepository.findByUserId(userId)
            .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
    }

    public User findByEmail(String userEmail) {
        return userRepository.findByEmail(userEmail)
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
            .username(currentUser.getRealUsername())
            .firstName(currentUser.getFirstName())
            .lastName(currentUser.getLastName())
            .build();
        return userDto;
    }

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
}

