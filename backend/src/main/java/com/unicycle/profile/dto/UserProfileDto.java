package com.unicycle.profile.dto;

import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileDto {
    private UUID profileId;
    private String profileImage; 
    private List<UUID> listings;
    private List<UUID> savedListings;
    private List<UUID> purchased;
    private List<UUID> followers;
    private List<UUID> following;
    private double rating;
    private int followerCount;
    private int followingCount;
}
