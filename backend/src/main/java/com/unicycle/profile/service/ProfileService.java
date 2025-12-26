package com.unicycle.profile.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unicycle.auth.dto.RegisterUserDto;
import com.unicycle.auth.entity.User;
import com.unicycle.auth.repository.UserRepository;
import com.unicycle.exception.ProfileNotFoundException;
import com.unicycle.listings.service.ImageUploadService;
import com.unicycle.profile.dto.MyProfileDto;
import com.unicycle.profile.dto.ProfileDto;
import com.unicycle.profile.dto.PublicProfileDto;
import com.unicycle.profile.dto.UserBasicDto;
import com.unicycle.profile.dto.UserProfileDto;
import com.unicycle.profile.dto.ProfileData;
import com.unicycle.profile.entity.Profile;
import com.unicycle.profile.repository.UserProfilesRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class ProfileService {
    private final UserRepository userRepository;
    private final UserProfilesRepository userProfilesRepository;
    private final ImageUploadService imageUploadService;

    public Profile initializeProfile(UUID userId) {
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
    }

    public Profile createProfile(ProfileDto input) {
        String imageUrl = null;
        if (input.getProfileImage() != null && !input.getProfileImage().isEmpty()) {
            imageUrl = imageUploadService.uploadSingleImage(input.getProfileImage(), "profile-images");
        }

        List<UUID> listings = isNotEmpty(input.getListings()) ? input.getListings() : new ArrayList<>();
        List<UUID> savedListings = isNotEmpty(input.getSavedListings()) ? input.getSavedListings() : new ArrayList<>();
        List<UUID> purchased = isNotEmpty(input.getPurchased()) ? input.getPurchased() : new ArrayList<>();
        List<UUID> followers = isNotEmpty(input.getFollowers()) ? input.getFollowers() : new ArrayList<>();
        List<UUID> following = isNotEmpty(input.getFollowing()) ? input.getFollowing() : new ArrayList<>();
        
        Profile profile = Profile.builder()
            .userId(input.getUserId())
            .profileImage(imageUrl)
            .listings(listings)
            .savedListings(savedListings)
            .purchased(purchased)
            .followers(followers)
            .followerCount(input.getFollowerCount())
            .following(following)
            .followingCount(input.getFollowingCount())
            .rating(input.getRating())
            .createdAt(LocalDateTime.now())
            .build();
        return userProfilesRepository.save(profile);
    }

    // TODO: should cache to avoid overhead each time user opens their profile
    public MyProfileDto getMyProfile(UUID userId) throws Exception {
        // TODO: should be put into try-catches
        ProfileData profileData = fetchUserProfileData(userId);

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

        return myProfileDto;
    }

    public PublicProfileDto getPublicProfile(UUID userId) throws Exception {
        // TODO: should be put into try-catches
        ProfileData profileData = fetchUserProfileData(userId);

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

        return publicProfileDto;
    }

    @Transactional
    public void updateProfileImage(UUID profileId, String newS3URL) {
        userProfilesRepository.updateS3URLInUserProfilesTableByProfileId(profileId, newS3URL);
    }

    @Transactional
    public void clearProfileImage(UUID profileId) {
        userProfilesRepository.clearProfileImageByProfileId(profileId);
    }

    private ProfileData fetchUserProfileData(UUID userId) {
        UserBasicDto userBasicDto = userRepository.getUserInUsersTableByUserId(userId);
        UserProfileDto userProfileDto = userProfilesRepository.getUserInUserProfilesTableByUserId(userId);
        
        if (userBasicDto == null || userProfileDto == null) {
            if (userBasicDto == null) System.out.println("userBasicDto is null");
            if (userProfileDto == null) System.out.println("userProfileDto is null");

            throw new ProfileNotFoundException("Profile not found for user: " + userId);
        }
        return new ProfileData(userBasicDto, userProfileDto);
    }

    private boolean isNotEmpty(List<?> list) {
        return list != null && !list.isEmpty();
    }
}
