package com.unicycle.profile.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.unicycle.auth.entity.User;
import com.unicycle.auth.service.JwtService;
import com.unicycle.profile.dto.MyProfileDto;
import com.unicycle.profile.dto.PublicProfileDto;
import com.unicycle.profile.service.ProfileService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;

@RequestMapping("/profiles")
@RestController
@AllArgsConstructor
public class ProfileController {
    private final JwtService jwtService;
    private final ProfileService profileService;

    // TODO: CRUD operations + repo file to... add profile pic, add listings, etc.

    // READ method for OTHER users
    @GetMapping("/{userId}")
    public ResponseEntity<PublicProfileDto> getProfile(
        @PathVariable UUID userId, 
        HttpServletRequest request
    ) throws Exception {
        return ResponseEntity.ok(profileService.getPublicProfile(userId));
    }

    // READ method for CURRENT user
    // TODO: check if frontend calls this endpoint
    @GetMapping("/me")
    public ResponseEntity<MyProfileDto> getMyProfile(HttpServletRequest request) throws Exception {
        // TODO: CHECK that the token is VALID + an ACCESS TOKEN
        String token = profileService.extractTokenFromHeader(request);
        UUID userId = jwtService.extractUserId(token);
        return ResponseEntity.ok(profileService.getMyProfile(userId));  
    }

    // TODo: private + pre-signed URLs to GEt user profile images

    @GetMapping("/{profileId}/get-profile-image")
    public ResponseEntity<?> getProfileImage(@PathVariable UUID profileId) {
        return ResponseEntity.ok(profileService.getProfileImage(profileId));
    }

    // UPDATE a user's profile image
    // TODO: make sure user has proper perms to be able to update their own profile image and
    //       not another users'
    @PatchMapping("/{profileId}/update/profile-image")
    public ResponseEntity<MyProfileDto> updateProfileImage(
        @PathVariable UUID profileId, 
        @AuthenticationPrincipal User user,
        @RequestParam("profileImage") MultipartFile newProfileImage
    ) throws Exception {
        profileService.updateProfileImage(profileId, user.getUserId(), newProfileImage);
        return ResponseEntity.noContent().build();
    }

    // DELETE a user's profile image
    @PostMapping("/{profileId}/delete/profile-image")
    public ResponseEntity<MyProfileDto> deleteProfileImage(
        @PathVariable UUID profileId,
        @AuthenticationPrincipal User user
    ) throws Exception {
        profileService.deleteProfileImage(profileId, user.getUserId());
        return ResponseEntity.noContent().build();
    }


    // @PostMapping("/update/username")
}
