package com.unicycle.auth.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unicycle.auth.entity.RefreshToken;
import com.unicycle.auth.entity.User;
import com.unicycle.auth.repository.RefreshTokenRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

// TODO: create access token
// TODO: take in user device details to allow for multiple devices per account? (not that excessive could be like web + mobile)
@Service
@Transactional
public class RefreshTokenService {
    
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    
    @Autowired
    private JwtService jwtService;
    
    // TODO: how to make sure all generated user UUIDs are unique?
    public RefreshToken createRefreshToken(UserDetails userDetails) {
        // Cast UserDetails back to your User entity
        User user = (User) userDetails;
        UUID userId = user.getUserId();
    
        // Deactivate existing tokens for this user
        // TODO: deactivate then delete existing tokens to free up space ... 
        refreshTokenRepository.deactivateAllByUserId(userId);
        
        // Deactivate existing tokens for this user (optional - for single device login)
        // refreshTokenRepository.deactivateAllByUserId(userId);
        System.out.println("=== Creating Refresh Token ===");
        System.out.println("User email: " + userDetails.getUsername());
        
        String tokenValue = jwtService.generateRefreshToken(userDetails);
        System.out.println("Generated token value: " + tokenValue.substring(0, 20) + "...");
        
        RefreshToken refreshToken = RefreshToken.builder()
            .userId(userId)
            .token(tokenValue)
            .expiresAt(LocalDateTime.now().plusDays(30))
            .build();
        
        System.out.println("Before save - Token object: " + refreshToken);
        System.out.println("Before save - ID: " + refreshToken.getId());
        // return refreshTokenRepository.save(refreshToken);
        RefreshToken saved = refreshTokenRepository.save(refreshToken);
        
        System.out.println("After save - Token object: " + saved);
        System.out.println("After save - ID: " + saved.getId());
        System.out.println("=== Refresh Token Created Successfully ===");
        
        return saved;
    }
    
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByTokenAndIsActive(token, true);
    }
    
    public boolean validateRefreshToken(String token) {
        Optional<RefreshToken> refreshTokenOpt = findByToken(token);
        
        if (refreshTokenOpt.isEmpty()) {
            return false;
        }
        
        RefreshToken refreshToken = refreshTokenOpt.get();
        
        if (!refreshToken.isValid()) {
            System.out.println("refreshToken is NOT valid");
            // Token expired or inactive, remove it
            refreshTokenRepository.deactivateByToken(token);
            return false;
        }
        System.out.println("refreshToken is valid");
        
        System.out.println("token = " + token);
        System.out.println("refreshToken userId = " + refreshToken.getUserId().toString());
        return jwtService.isRefreshToken(token) && jwtService.isTokenValid(token, refreshToken.getUserId());
    }
    
    public void revokeToken(String token) {
        refreshTokenRepository.deactivateByToken(token);
    }
    
    public void revokeAllUserTokens(UUID userId) {
        refreshTokenRepository.deactivateAllByUserId(userId);
    }
    
    // Clean up expired tokens daily
    @Scheduled(cron = "0 0 2 * * ?") // 2 AM daily
    public void cleanupExpiredTokens() {
        refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());
    }
}