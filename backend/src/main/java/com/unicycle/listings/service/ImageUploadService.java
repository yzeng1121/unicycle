/*
 * ImageUploadService.java
 * Purpose: uploads all images into S3 bucket
 */

package com.unicycle.listings.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.unicycle.listings.s3.S3Service;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class ImageUploadService {
    private final S3Service s3Service;
    
    public String uploadSingleImage(MultipartFile image, String folderType) {
        if (image == null || image.isEmpty()) throw new IllegalArgumentException("Image file is required.");
        return s3Service.uploadFile(image, folderType);
    }
    
    public List<String> uploadMultipleImages(MultipartFile[] images, String folderType) {
        if (images == null || images.length == 0) return new ArrayList<>();
        
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
