package com.unicycle.listings.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ImageFolder {
    LISTINGS("listings"),
    PROFILE_IMAGES("profile-images");

    final String imagePath;
}
