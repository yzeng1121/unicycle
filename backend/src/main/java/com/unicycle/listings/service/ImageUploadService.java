/*
 * ImageUploadService.java
 * Purpose: uploads all images into S3 bucket
 */

package com.unicycle.listings.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.unicycle.listings.s3.S3Service;

@Service
public class ImageUploadService {
    private final S3Service s3Service;
    
    public ImageUploadService(S3Service s3Service) {
        this.s3Service = s3Service;
    }
    
    public String uploadSingleImage(MultipartFile image, String folderType) {
        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("Image file is required");
        }
        try {
            return s3Service.uploadFile(image, folderType);
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload image: " + e.getMessage(), e);
        }
    }
    
    public List<String> uploadMultipleImages(MultipartFile[] images, String folderType) {
        if (images == null || images.length == 0) {
            return new ArrayList<>();
        }
        
        List<String> imageUrls = new ArrayList<>();
        for (MultipartFile image : images) {
            if (!image.isEmpty()) {
                String url = uploadSingleImage(image, folderType);
                imageUrls.add(url);
            }
        }
        return imageUrls;
    }
}
