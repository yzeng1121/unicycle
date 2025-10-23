package com.unicycle.profile.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.unicycle.auth.repository.UserRepository;
import com.unicycle.profile.dto.MyProfileDto;
import com.unicycle.profile.dto.PublicProfileDto;
import com.unicycle.profile.dto.UserBasicDto;
import com.unicycle.profile.dto.UserProfileDto;
import com.unicycle.profile.repository.UserProfilesRepository;
import com.unicycle.shared.exception.ProfileNotFoundException;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class ProfileService {
    private final UserRepository userRepository;
    private final UserProfilesRepository userProfilesRepository;

    public MyProfileDto getMyProfile(UUID userId) throws Exception {
        // TODO: should be put into try-catches
        
        UserBasicDto userBasicDto = userRepository.getUserInUsersTableByUserId(userId);
        UserProfileDto userProfileDto = userProfilesRepository.getUserInUserProfilesTableByUserId(userId);
       
        if (userBasicDto == null || userProfileDto == null) {
            if (userBasicDto == null) System.out.println("userBasicDto is null");
            if (userProfileDto == null) System.out.println("userProfileDto is null");

            throw new ProfileNotFoundException("Profile not found for user: " + userId);
        }

        MyProfileDto myProfileDto = MyProfileDto.builder()
            .profileId(userProfileDto.getProfileId())
            .userId(userId)
            .profileImage(userProfileDto.getProfileImage())
            .username(userBasicDto.getUsername())
            .firstName(userBasicDto.getFirstName())
            .lastName(userBasicDto.getLastName())
            .dorm(userBasicDto.getDorm())
            .listings(userProfileDto.getListings())
            .savedListings(userProfileDto.getSavedListings())
            .purchased(userProfileDto.getPurchased())
            .followers(userProfileDto.getFollowers())
            .followerCount(userProfileDto.getFollowerCount())
            .following(userProfileDto.getFollowing())
            .followingCount(userProfileDto.getFollowingCount())
            .rating(userProfileDto.getRating())
            .build();

        return myProfileDto;
    }

    public PublicProfileDto getPublicProfile(UUID userId) throws Exception {
        // TODO: should be put into try-catches
        
        UserBasicDto userBasicDto = userRepository.getUserInUsersTableByUserId(userId);
        UserProfileDto userProfileDto = userProfilesRepository.getUserInUserProfilesTableByUserId(userId);

        if (userBasicDto == null || userProfileDto == null) {
            throw new ProfileNotFoundException("Profile not found for user: " + userId);
        }

        PublicProfileDto publicProfileDto = PublicProfileDto.builder()
            .profileId(userProfileDto.getProfileId())
            .userId(userId)
            .profileImage(userProfileDto.getProfileImage())
            .username(userBasicDto.getUsername())
            .firstName(userBasicDto.getFirstName())
            .lastName(userBasicDto.getLastName())
            .dorm(userBasicDto.getDorm())
            .listings(userProfileDto.getListings())
            .followers(userProfileDto.getFollowers())
            .followerCount(userProfileDto.getFollowerCount())
            .following(userProfileDto.getFollowing())
            .followingCount(userProfileDto.getFollowingCount())
            .rating(userProfileDto.getRating())
            .build();

        return publicProfileDto;
    }
}
