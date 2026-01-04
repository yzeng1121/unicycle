package com.unicycle.profile.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.unicycle.auth.repository.UserRepository;
import com.unicycle.exception.ProfileNotFoundException;
import com.unicycle.exception.UnauthorizedProfileAccessException;
import com.unicycle.exception.UserNotFoundException;
import com.unicycle.exception.FailedToCreateProfileException;
import com.unicycle.exception.FailedToFetchProfileException;
import com.unicycle.exception.FailedToFetchUserProfilePhotoException;
import com.unicycle.exception.InvalidBearerTokenException;
import com.unicycle.exception.InvalidImageContentsException;
import com.unicycle.listings.entity.ImageFolder;
import com.unicycle.listings.s3.S3Service;
import com.unicycle.listings.service.ImageUploadService;
import com.unicycle.profile.dto.MyProfileDto;
import com.unicycle.profile.dto.PublicProfileDto;
import com.unicycle.profile.dto.UserBasicDto;
import com.unicycle.profile.dto.UserProfileDto;
import com.unicycle.profile.dto.ProfileData;
import com.unicycle.profile.entity.Profile;
import com.unicycle.profile.repository.UserProfilesRepository;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class ProfileService {
    private S3Service s3Service;
    private ImageUploadService imageUploadService;
    private final UserRepository userRepository;
    private final UserProfilesRepository userProfilesRepository;

    public Profile createProfile(UUID userId) {
        try {
            Profile profile = Profile.builder()
            .userId(userId)
            .profileImage(null)
            .listings(new ArrayList<>())
            .savedListings(new ArrayList<>())
            .purchased(new ArrayList<>())
            .followers(new ArrayList<>())
            .followerCount(0)
            .following(new ArrayList<>())
            .followingCount(0)
            .rating(0)
            .createdAt(LocalDateTime.now())
            .build();

            return userProfilesRepository.save(profile);
        } catch (Exception e) {
            throw new FailedToCreateProfileException("Failed to initialize profile: " + e.getMessage());
        }
    }

    public PublicProfileDto getPublicProfile(UUID userId) throws Exception {
        ProfileData profileData = fetchUserProfileData(userId);

        try {
            PublicProfileDto publicProfileDto = PublicProfileDto.builder()
                .profileId(profileData.getUserProfileDto().getProfileId())
                .userId(userId)
                .profileImage(profileData.getUserProfileDto().getProfileImage())
                .username(profileData.getUserBasicDto().getUsername())
                .firstName(profileData.getUserBasicDto().getFirstName())
                .lastName(profileData.getUserBasicDto().getLastName())
                .dorm(profileData.getUserBasicDto().getDorm())
                .listings(profileData.getUserProfileDto().getListings())
                .followers(profileData.getUserProfileDto().getFollowers())
                .followerCount(profileData.getUserProfileDto().getFollowerCount())
                .following(profileData.getUserProfileDto().getFollowing())
                .followingCount(profileData.getUserProfileDto().getFollowingCount())
                .rating(profileData.getUserProfileDto().getRating())
                .build();

            if (publicProfileDto == null) throw new UserNotFoundException("User not found.");
            return publicProfileDto;
        } catch (Exception e) {
            throw new FailedToFetchProfileException("Failed to fetch user profile: " + e.getMessage());
        }
    }

    // TODO: should cache to avoid overhead each time user opens their profile
    public MyProfileDto getMyProfile(UUID userId) throws Exception {
        ProfileData profileData = fetchUserProfileData(userId);

        try {
            MyProfileDto myProfileDto = MyProfileDto.builder()
                .profileId(profileData.getUserProfileDto().getProfileId())
                .userId(userId)
                .profileImage(profileData.getUserProfileDto().getProfileImage())
                .username(profileData.getUserBasicDto().getUsername())
                .firstName(profileData.getUserBasicDto().getFirstName())
                .lastName(profileData.getUserBasicDto().getLastName())
                .dorm(profileData.getUserBasicDto().getDorm())
                .listings(profileData.getUserProfileDto().getListings())
                .savedListings(profileData.getUserProfileDto().getSavedListings())
                .purchased(profileData.getUserProfileDto().getPurchased())
                .followers(profileData.getUserProfileDto().getFollowers())
                .followerCount(profileData.getUserProfileDto().getFollowerCount())
                .following(profileData.getUserProfileDto().getFollowing())
                .followingCount(profileData.getUserProfileDto().getFollowingCount())
                .rating(profileData.getUserProfileDto().getRating())
                .build();

            if (myProfileDto == null) throw new UserNotFoundException("User not found.");
            return myProfileDto;
        } catch (Exception e) {
            throw new FailedToFetchProfileException("Failed to fetch user profile: " + e.getMessage());
        }
    }

    public UserProfileDto getUserProfileDto(UUID userId) {
        UserProfileDto userProfileDto = userProfilesRepository.getUserProfileDtoByUserId(userId);
        if (userProfileDto == null) throw new ProfileNotFoundException("Profile not found with userId: " + userId);
        return userProfileDto;
    }

    public String getProfileImage(UUID profileId) {
        try {
            String imageUrl = userProfilesRepository.getProfileImage(profileId);
            // TODO: if none should return the default 
            if (imageUrl == null || imageUrl.isEmpty()) throw new Exception("User has no profile image");
            return imageUrl;
        } catch (Exception e) {
            throw new FailedToFetchUserProfilePhotoException("Failed to fetch profile photo: " + e.getMessage());
        }
    }

    @Transactional
    public MyProfileDto updateProfileImage(UUID profileId, UUID userId, MultipartFile newProfileImage) throws Exception {
        checkPerms(profileId, userId);

        String s3URL = userProfilesRepository.getS3URLInUserProfilesTableByProfileId(profileId);
        if (s3URL != null && s3URL.length() > 0) s3Service.deleteFileByUrl(s3URL);

        String newS3URL;
        if (newProfileImage == null || !newProfileImage.isEmpty()) throw new InvalidImageContentsException("Profile image not found or invalid.");
        newS3URL = imageUploadService.uploadSingleImage(newProfileImage, ImageFolder.PROFILE_IMAGES.getImagePath());
        userProfilesRepository.updateS3URLInUserProfilesTableByProfileId(profileId, newS3URL);  
        return userProfilesRepository.getMyProfileDtoByUserId(userId);
    }

    @Transactional
    public MyProfileDto deleteProfileImage(UUID profileId, UUID userId) {
        checkPerms(profileId, userId);

        String s3URL = userProfilesRepository.getS3URLInUserProfilesTableByProfileId(profileId);
        if (s3URL == null) throw new InvalidImageContentsException("Profile image not found or invalid.");
        s3Service.deleteFileByUrl(s3URL);
        userProfilesRepository.deleteProfileImageByProfileId(profileId);
        return userProfilesRepository.getMyProfileDtoByUserId(userId);
    }

    public String extractTokenFromHeader(HttpServletRequest request) throws Exception {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                return authHeader.substring(7);
            }
        } catch (Exception e) {
            throw new InvalidBearerTokenException("No valid token found: " + e.getMessage());
        }
        return "";
    }

    private ProfileData fetchUserProfileData(UUID userId) {
        UserBasicDto userBasicDto;
        UserProfileDto userProfileDto;

        try {
            userBasicDto = userRepository.getUserBasicDtoByUserId(userId);
            userProfileDto = userProfilesRepository.getUserProfileDtoByUserId(userId);
        } catch (Exception e) {
            throw new FailedToFetchProfileException("Failed to fetch user profile: " + e.getMessage());
        }
        
        if (userBasicDto == null || userProfileDto == null) {
            throw new ProfileNotFoundException("Profile not found for user: " + userId);
        }
        return new ProfileData(userBasicDto, userProfileDto);
    }

    private void checkPerms(UUID profileId, UUID userId) {
        if (userId != userProfilesRepository.getUserIdByProfileId(profileId)) {
            throw new UnauthorizedProfileAccessException("User can not modify a profile that's not theirs.");
        }
    }

}
