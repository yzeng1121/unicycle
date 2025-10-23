package com.unicycle.profile.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.unicycle.profile.dto.UserProfileDto;
import com.unicycle.profile.entity.Profile;

import jakarta.transaction.Transactional;

@Repository
public interface UserProfilesRepository extends JpaRepository<Profile, UUID> {
    // TODO: add querying methods here (if not too complicated may just be able to extend from CRUD repo)
    // fetch user profile data from user_profiles table in DB
    @Query("SELECT p.profileId, p.profileImage, p.listings, p.savedListings, p.purchased, p.followers, p.following, p.rating, p.followerCount, p.followingCount FROM Profile p WHERE p.userId = :id")
    UserProfileDto getUserInUserProfilesTableByUserId(@Param("id") UUID id);

    @Query("SELECT p.profileImage FROM Profile p WHERE p.profileId = :id")
    String getS3URLInUserProfilesTableByProfileId(@Param("id") UUID id);

    @Modifying
    @Query("UPDATE Profile p SET p.profileImage = :newUrl WHERE p.profileId = :profileId")
    void updateS3URLInUserProfilesTableByProfileId(@Param("profileId") UUID profileId, @Param("newUrl") String newUrl);

    @Query("SELECT p.userId FROM Profile p WHERE p.profileId = :profileId")
    UUID getUserIdByProfileId(@Param("profileId") UUID profileId);

    @Query("SELECT p.profileId FROM Profile p WHERE p.userId = :userId")
    UUID getProfileIdByUserId(@Param("userId") UUID userId);

    @Modifying
    @Transactional
    @Query("UPDATE Profile p SET p.profileImage = NULL WHERE p.profileId = :profileId")
    void clearProfileImageByProfileId(@Param("profileId") UUID profileId);

    // TODO: change from STRING (temporary) to UUID
    @Modifying
    @Transactional
    @Query(value = "UPDATE user_profiles SET listings = array_append(listings, :listingId) WHERE profile_id = :profileId", nativeQuery = true)
    void addListingToProfile(@Param("profileId") UUID profileId, @Param("listingId") UUID listingId);

    @Query("SELECT p.profileImage FROM Profile p WHERE p.userId = :userId")
    String getProfileImage(@Param("userId") UUID userId);
    // TODO: delete user's 
}
