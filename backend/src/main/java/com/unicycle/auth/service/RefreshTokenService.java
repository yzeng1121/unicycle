package com.unicycle.auth.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unicycle.auth.entity.RefreshToken;
import com.unicycle.auth.entity.User;
import com.unicycle.auth.repository.RefreshTokenRepository;
import com.unicycle.exception.InvalidRefreshTokenException;
import com.unicycle.exception.InvalidCredentialsException;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

// TODO: take in user device details to allow for multiple devices per account? (not that excessive could be like web + mobile)
@Service
@Transactional
public class RefreshTokenService {
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    
    @Autowired
    private JwtService jwtService;
    
    public RefreshToken createRefreshToken(UserDetails userDetails) {
        if (userDetails == null) throw new NullPointerException("UserDetails object is null.");

        // Cast UserDetails back to your User entity
        User user = (User) userDetails;
        UUID userId = user.getUserId();

        if (userId == null) throw new InvalidCredentialsException("User id is null."); 

        refreshTokenRepository.deactivateAllByUserId(userId);
        refreshTokenRepository.deletePreviousTokens(userId);
        
        String tokenValue = jwtService.generateRefreshToken(userDetails);
        
        RefreshToken refreshToken = RefreshToken.builder()
            .userId(userId)
            .token(tokenValue)
            .expiresAt(LocalDateTime.now().plusDays(30))
            .build();
        
        RefreshToken saved = refreshTokenRepository.save(refreshToken);
        return saved;
    }
    
    public Optional<RefreshToken> findByToken(String token) {
        if (token == null || token.length() == 0 || !isFormatted(token)) {
            throw new InvalidCredentialsException("Refresh token string is invalid.");
        }
        Optional<RefreshToken> optionalToken = refreshTokenRepository.findByTokenAndIsActive(token, true);
        if (optionalToken.isEmpty()) {
            throw new NoSuchElementException("Token not found in database.");
        }
        RefreshToken refreshToken = optionalToken.get();
        if (!refreshToken.isValid()) throw new InvalidRefreshTokenException("Refresh token is invalid.");
        return optionalToken;
    }

    private boolean isFormatted(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 3) return false;
        
        try {
            Base64.getUrlDecoder().decode(parts[0]);
            Base64.getUrlDecoder().decode(parts[1]);
            Base64.getUrlDecoder().decode(parts[2]);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    // TODO: consider deleting after deactivating
    public boolean validateRefreshToken(String token) {
        Optional<RefreshToken> refreshTokenOpt = findByToken(token);
        if (refreshTokenOpt.isEmpty()) return false;

        RefreshToken refreshToken = refreshTokenOpt.get();
        return jwtService.isRefreshToken(token) && jwtService.isTokenValid(token, refreshToken.getUserId());
    }
    
    public void revokeToken(String token) {
        refreshTokenRepository.deactivateByToken(token);
    }
    
    public void revokeAllUserTokens(UUID userId) {
        refreshTokenRepository.deactivateAllByUserId(userId);
    }
    
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupExpiredTokens() {
        refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());
    }
}