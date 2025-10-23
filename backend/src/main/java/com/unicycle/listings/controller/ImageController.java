/*
 * ImageController.java
 * Purpose: testing successful upload of image to S3 bucket
 */

package com.unicycle.listings.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.unicycle.listings.service.ImageUploadService;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@RestController
@RequestMapping("/api/images")
@CrossOrigin(origins = "*") // TODO: for development - restrict in production
public class ImageController {
    private final ImageUploadService imageUploadService;  // Use new service
    // NOTE: only first file is processed & saved when multiple images are uploaded
    // uploading images endpoint
    // TODO: have frontend throw an error if no files are provided so that the backend doesnt have to deal w/ it
    @PostMapping("/upload")
    public ResponseEntity<?> uploadImage(
        @RequestParam("image") MultipartFile file, 
        @RequestParam("type") String folderType
    ) {
        try {
            // Upload to S3 and get URL
            String imageUrl = imageUploadService.uploadSingleImage(file, folderType);
            System.out.println("Image URL uploaded...");

            return ResponseEntity.ok(Map.of(
                "success", true,
                "url", imageUrl
            ));
            
        } catch (Exception e) {
            // return ResponseEntity.status(500).body("Upload failed: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    // TODO: make more modular this can just call on a single until files is empty/at the end of files
    @PostMapping("/upload-multiple")
    public ResponseEntity<?> uploadMultipleImages(
        @RequestParam("images") MultipartFile[] files,
        @RequestParam("type") String folderType
    ) {
        System.out.println("Trying to upload" + files.length + "images...");
        try {
            List<String> imageUrls = imageUploadService.uploadMultipleImages(files, folderType);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "count", imageUrls.size(),
                "urls", imageUrls
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Upload failed: " + e.getMessage());
        }
    }
}
