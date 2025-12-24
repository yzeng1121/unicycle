package com.unicycle.profile.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.unicycle.auth.service.JwtService;
import com.unicycle.listings.s3.S3Service;
import com.unicycle.listings.service.ImageUploadService;
import com.unicycle.profile.dto.MyProfileDto;
import com.unicycle.profile.dto.ProfileDto;
import com.unicycle.profile.dto.PublicProfileDto;
import com.unicycle.profile.entity.Profile;
import com.unicycle.profile.repository.UserProfilesRepository;
import com.unicycle.profile.service.ProfileService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;

@RequestMapping("/profiles")
@RestController
@AllArgsConstructor
public class ProfileController {
    private final JwtService jwtService;
    private final S3Service s3Service;
    private final ProfileService profileService;
    private final UserProfilesRepository userProfilesRepository;
    private final ImageUploadService imageUploadService;


    @PostMapping("/initialize")
    public ResponseEntity<?> initializeProfile(UUID userId) {
        try {
            Profile initializedProfile = profileService.initializeProfile(userId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "profile", initializedProfile,
                "message", "Default profile initialized."
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Failed to initialize profile: " + e.getMessage()
            ));
        }
    }

    // CREATE method
    @PostMapping("/create")
    public ResponseEntity<?> createProfile(ProfileDto input) {
        try {
            Profile savedProfile = profileService.createProfile(input);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "profile", savedProfile,
                "message", "Profile created with 1 profile image"
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Failed to create profile: " + e.getMessage()
            ));
        }
    }

    // TODO: CRUD operations + repo file to... add profile pic, add listings, etc.

    // READ method for OTHER users
    @GetMapping("/{userId}")
    public ResponseEntity<?> getProfile(@PathVariable UUID userId, HttpServletRequest request) throws Exception {
        System.out.println("Fetching user from userId... ");
        
        PublicProfileDto profile = profileService.getPublicProfile(userId);

        if (profile == null) {
            System.out.println("Profile not found for user: " + userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("User not found.");
        }
        return ResponseEntity.ok(profile);
    }

    // READ method for CURRENT user
    @GetMapping("/me")
    public ResponseEntity<?> getMyProfile(HttpServletRequest request) throws Exception {
        // check JWT access token
        String token;

        // TODO: FIRST and foremost CHECK that the token is VALID + an ACCESS TOKEN
        try {
            token = extractTokenFromHeader(request);
        } catch (Exception e) {
            System.out.println("Token extraction failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("Authentication required. Token not found or invalid.");
        }

        // get userId from JWT access token
        UUID userId = jwtService.extractUserId(token);
        MyProfileDto profile = profileService.getMyProfile(userId);

        if (profile == null) {
            System.out.println("Profile not found for user: " + userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("User not found.");
        }
        return ResponseEntity.ok(profile);  
        // get FULL user profile information needed
    }

    // TODo: private + pre-signed URLs to GEt user profile images

    @GetMapping("/{profileId}/get-profile-image")
    public ResponseEntity<?> getProfileImage(@PathVariable UUID userId) {
        try {
            String imageUrl = userProfilesRepository.getProfileImage(userId);
            System.out.println("Fetched cover image URL: " + imageUrl);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "imageUrl", imageUrl,
                "message", "Image with URL=" + imageUrl + " fetched."
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Failed to fetch profile photo: " + e.getMessage()
            ));
        }
    }

    // UPDATE a user's profile image
    @PatchMapping("/{profileId}/update/profile-image")
    public ResponseEntity<?> updateProfileImage(@PathVariable UUID profileId, @RequestParam("profileImage") MultipartFile newProfileImage) throws Exception {
        System.out.println("Entered the 'update profile image' endpoint...");
        String s3URL = userProfilesRepository.getS3URLInUserProfilesTableByProfileId(profileId);

        System.out.println("s3URL = " + s3URL);

        if (s3URL != null && s3URL.length() > 0) {
            System.out.println("Deleting old image from S3 bucket.");
            s3Service.deleteFileByUrl(s3URL);
        } else {
            System.out.println("This user has no profile image.");
        }

        if (newProfileImage != null && !newProfileImage.isEmpty()) {
            String newS3URL = imageUploadService.uploadSingleImage(newProfileImage, "profile-images");
            profileService.updateProfileImage(profileId, newS3URL);
        } else {
            System.out.println("No new profile image provided.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Please provide a valid image file. Profile image not found or invalid.");
        }

        UUID userId = userProfilesRepository.getUserIdByProfileId(profileId);
        MyProfileDto updatedProfile = profileService.getMyProfile(userId);
        return ResponseEntity.ok(updatedProfile);
    }

    // DELETE a user's profile image
    @PostMapping("/{profileId}/delete/profile-image")
    public ResponseEntity<?> deleteProfileImage(@PathVariable UUID profileId) throws Exception {
        String s3URL = userProfilesRepository.getS3URLInUserProfilesTableByProfileId(profileId);

        if (s3URL != null) {
            System.out.println("Image successfully deleted from S3 bucket.");
            s3Service.deleteFileByUrl(s3URL);
            profileService.clearProfileImage(profileId);
        } else {
            System.out.println("This user has no profile image.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("User profile is not found.");
        }

        UUID userId = userProfilesRepository.getUserIdByProfileId(profileId);
        MyProfileDto updatedProfile = profileService.getMyProfile(userId);
        return ResponseEntity.ok(updatedProfile);
    }


    // @PostMapping("/update/username")

    private String extractTokenFromHeader(HttpServletRequest request) throws Exception {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        // TODO: create unauthorized exception
        throw new Exception("No valid token found");
    }

    private boolean isNotEmpty(List<?> list) {
        return list != null && !list.isEmpty();
    }
    
}
