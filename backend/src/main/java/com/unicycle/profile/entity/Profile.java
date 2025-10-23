package com.unicycle.profile.entity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "user_profiles")
@Getter
@Setter
@Builder
@AllArgsConstructor
public class Profile {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID profileId;
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    @Column(name = "profile_image") // TODO: default image
    private String profileImage; 

    @Column(columnDefinition = "text[]")
    @JdbcTypeCode(SqlTypes.ARRAY)
    private List<UUID> listings; 

    @Column(name = "saved_listings", columnDefinition = "text[]")
    @JdbcTypeCode(SqlTypes.ARRAY)
    private List<UUID> savedListings; 

    @Column(columnDefinition = "text[]")
    @JdbcTypeCode(SqlTypes.ARRAY)
    private List<UUID> purchased; 

    @Column(columnDefinition = "text[]")
    @JdbcTypeCode(SqlTypes.ARRAY)
    private List<UUID> followers;

    private int followerCount;

    @Column(columnDefinition = "text[]")
    @JdbcTypeCode(SqlTypes.ARRAY)
    private List<UUID> following;
    private int followingCount;
    private double rating;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
