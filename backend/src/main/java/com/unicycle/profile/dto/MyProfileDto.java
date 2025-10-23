package com.unicycle.profile.dto;

import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MyProfileDto {
    private UUID profileId;
    private UUID userId;
    private String profileImage;
    private String username;
    private String firstName;
    private String lastName;
    private String dorm;
    private List<UUID> listings; 
    private List<UUID> savedListings; 
    private List<UUID> purchased; 
    private List<UUID> followers;
    private int followerCount;
    private List<UUID> following;
    private int followingCount;
    private double rating;
}
