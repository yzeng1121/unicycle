package com.unicycle.listings.s3;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class S3Service {

    private final S3Client s3;
    
    @Value("${aws.s3.bucket}")
    private String bucketName;
    
    @Value("${aws.region}")
    private String region;

    public S3Service(S3Client s3) {
        this.s3 = s3;
    }

    // puts file directly into AWS S3
    public void putObject(String bucketName, String key, MultipartFile file) throws IOException {
        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(file.getContentType())
                .build();
        s3.putObject(objectRequest, RequestBody.fromInputStream(
            file.getInputStream(),
            file.getSize()
        ));
    }

    // High-level method for listing images (what your controller will use)
    public String uploadListingImage(MultipartFile file) throws IOException {
        validateImageFile(file);
        
        String fileName = generateUniqueFileName(file.getOriginalFilename());
        String key = "listings/" + fileName;
        
        putObject(bucketName, key, file);
        return getPublicUrl(key);
    }

    // High-level method for profile images
    public String uploadProfileImage(MultipartFile file) throws IOException {
        validateImageFile(file);
        
        String fileName = generateUniqueFileName(file.getOriginalFilename());
        String key = "profile-images/" + fileName;
        
        putObject(bucketName, key, file);
        return getPublicUrl(key);
    }

    // Generic upload method
    // TODO: error check added because just incase fails
    public String uploadFile(MultipartFile file, String folderType) throws IOException {
        System.out.println("folderType: " + folderType);
        
        if (folderType.equals("listings")) {
            return uploadListingImage(file); // Default to listing images
        } else if (folderType.equals("profile-images")) {
            return uploadProfileImage(file);
        } else {
            return "ERROR: unspecified folder to insert image in S3 bucket";
        }
    }

    // Delete file method
    public void deleteFile(String key) {
        DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();
        s3.deleteObject(deleteRequest);
    }

    // Delete file by full URL
    public void deleteFileByUrl(String fileUrl) {
        String key = extractKeyFromUrl(fileUrl);
        if (key != null) {
            deleteFile(key);
        }
    }

    // Your existing getObject method
    public byte[] getObject(String bucketName, String key) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        ResponseInputStream<GetObjectResponse> res = s3.getObject(getObjectRequest);

        try {
            return res.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Helper method to validate image files
    private void validateImageFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("File is empty");
        }
        
        if (file.getSize() > 10_000_000) { // 10MB limit
            throw new IOException("File size exceeds 10MB limit");
        }
        
        String contentType = file.getContentType();
        if (contentType == null || !isImageContentType(contentType)) {
            throw new IOException("File must be an image (JPEG, PNG, GIF, WebP)");
        }
    }

    // Helper method to check if content type is an image
    private boolean isImageContentType(String contentType) {
        List<String> allowedTypes = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", 
            "image/gif", "image/webp", "image/heic", "image/heif"
        );
        return allowedTypes.contains(contentType.toLowerCase());
    }

    // Helper method to generate unique file names
    private String generateUniqueFileName(String originalFilename) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return System.currentTimeMillis() + "-" + UUID.randomUUID().toString() + extension;
    }

    // Helper method to generate public URL
    private String getPublicUrl(String key) {
        return String.format("https://%s.s3.%s.amazonaws.com/%s", 
            bucketName, region, key);
    }

    // Helper method to extract key from full S3 URL
    private String extractKeyFromUrl(String fileUrl) {
        try {
            // Extract key from URL like: https://bucket.s3.region.amazonaws.com/key
            String baseUrl = String.format("https://%s.s3.%s.amazonaws.com/", bucketName, region);
            if (fileUrl.startsWith(baseUrl)) {
                return fileUrl.substring(baseUrl.length());
            }
        } catch (Exception e) {
            // Log error but don't throw
        }
        return null;
    }
}

