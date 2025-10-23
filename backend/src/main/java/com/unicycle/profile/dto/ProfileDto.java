// used for creation of profiles

package com.unicycle.profile.dto;

import java.util.List;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

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
public class ProfileDto {
    private UUID userId;
    private MultipartFile profileImage;
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
