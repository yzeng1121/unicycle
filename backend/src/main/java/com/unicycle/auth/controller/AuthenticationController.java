package com.unicycle.auth.controller;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.unicycle.auth.dto.AccessTokenResponse;
import com.unicycle.auth.dto.AuthenticationResponse;
import com.unicycle.auth.dto.LoginUserDto;
import com.unicycle.auth.dto.RefreshTokenRequest;
import com.unicycle.auth.dto.RegisterUserDto;
import com.unicycle.auth.dto.UserDto;
import com.unicycle.auth.dto.VerifyUserDto;
import com.unicycle.auth.entity.RefreshToken;
import com.unicycle.auth.entity.User;
import com.unicycle.auth.service.AuthenticationService;
import com.unicycle.auth.service.JwtService;
import com.unicycle.auth.service.RefreshTokenService;
import com.unicycle.auth.service.UserService;
import com.unicycle.exception.ExpiredVerificationException;
import com.unicycle.profile.service.ProfileService;
import com.unicycle.profile.entity.Profile;

import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RequestMapping("/auth")
@RestController
@AllArgsConstructor
public class AuthenticationController {
    private final JwtService jwtService;
    private final AuthenticationService authenticationService;
    private final RefreshTokenService refreshTokenService;
    private final UserService userService;
    private final ProfileService profileService;

    private static final int ACCESS_TOKEN_EXPIRY_MINUTES = 15;

    @PostMapping("/signup")
    public ResponseEntity<?> register(@RequestBody RegisterUserDto registerUserDto) {
        try {
            User registeredUser = authenticationService.signup(registerUserDto);
            return ResponseEntity.ok(buildRegisterResponse(registeredUser));
        } catch (DataIntegrityViolationException e) {
            if (e.getMessage().contains("unique_username")) {
                return ResponseEntity.status(403).body(
                    Map.of("message", "Username already exists")
                );
            }
            return ResponseEntity.status(401).body(
                Map.of("message", e.getMessage())
            );
        }
    }

    private Map<String, Object> buildRegisterResponse(User registeredUser) {
        Profile initializedProfile = profileService.initializeProfile(registeredUser.getUserId());
        Map<String, Object> response = new HashMap<>();
        response.put("user", registeredUser);
        response.put("profile", initializedProfile);
        
        return response;
    }

    // TODO: logging in an unverified user leads to 403 code
    // TODO: currently each time user logs in, a new refreshToken is created --> long term implications/storage factor
    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody LoginUserDto loginUserDto) {
        User authenticatedUser = authenticationService.authenticate(loginUserDto);
        return ResponseEntity.ok(buildAuthResponse(authenticatedUser));
    }

    private AuthenticationResponse buildAuthResponse(User authenticatedUser) {
        String accessToken = jwtService.generateAccessToken(authenticatedUser);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(authenticatedUser);
        UserDto userDto = mapToUserDto(authenticatedUser);
    
        AuthenticationResponse response = new AuthenticationResponse(
            accessToken,
            refreshToken.getToken(),
            userDto,
            LocalDateTime.now().plusMinutes(ACCESS_TOKEN_EXPIRY_MINUTES) // Access token expiration
        );
        return response;
    }

    // generates new access token
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest request) {
        String refreshTokenValue = request.getRefreshToken();
        
        // checks if token is valid
        if (!refreshTokenService.validateRefreshToken(refreshTokenValue)) {
            throw new ExpiredVerificationException("Invalid or expired refresh token.");
        }
        
        try {
            UUID userId = jwtService.extractUserId(refreshTokenValue);
            // TODO: error here because tries to find user by email but actually gives username
            return ResponseEntity.ok(buildAccessTokenResponse(userId));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(
                Map.of("message", "Token refresh failed")
            );
        }
    }

    private AccessTokenResponse buildAccessTokenResponse(UUID userId) {
        User user = userService.findByUserId(userId);
        String newAccessToken = jwtService.generateAccessToken(user);
        
        AccessTokenResponse response = new AccessTokenResponse(
            newAccessToken,
            LocalDateTime.now().plusMinutes(ACCESS_TOKEN_EXPIRY_MINUTES)
        );
        return response;
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();
        
        if (refreshToken != null) {
            refreshTokenService.revokeToken(refreshToken);
        }
        
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    // Keep your existing verification endpoints
    @PostMapping("/verify")
    public ResponseEntity<?> verifyUser(@RequestBody VerifyUserDto verifyUserDto) {
        authenticationService.verifyUser(verifyUserDto);
        return ResponseEntity.ok("Account verified successfully.");
    }

    // TODO: implement get access token
    // @GetMapping("/auth/validate")
    // public ResponseEntity<?> validateToken(HttpServletRequest request) {
    //     // Your token validation logic
    // }

    @PostMapping("/resend")
    public ResponseEntity<?> resendVerificationCode(@RequestBody Map<String, String> body) {
        authenticationService.resendVerificationCode(body.get("email"));
        return ResponseEntity.ok("Verification code sent.");
    }

    // TODO: reset password 
    // TODO: frontend implementation/email HTML file
    // @PostMapping("/reset-password")
    // public ResponseEntity<?> resetPassword() {

    // }

    // TODO: forgot password
    
    // Helper methods
    private UserDto mapToUserDto(User user) {
        return new UserDto(
            user.getUserId(),
            user.getEmail(),
            user.getUsername(),
            user.isEnabled()
        );
    }
}
