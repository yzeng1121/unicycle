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

    // TODO: after a user successfully signs up, they should have an associated default
    // user profile on the database
    @PostMapping("/signup")
    public ResponseEntity<?> register(@RequestBody RegisterUserDto registerUserDto) {
        try {
            User registeredUser = authenticationService.signup(registerUserDto);
            Profile initializedProfile = profileService.initializeProfile(registeredUser.getUserId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("user", registeredUser);
            response.put("profile", initializedProfile);
            
            return ResponseEntity.ok(response);
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

    // TODO: logging in an unverified user leads to 403 code
    // TODO: currently each time user logs in, a new refreshToken is created --> long term implications/storage factor
    @PostMapping("/login")
    public ResponseEntity<?> authenticate(@RequestBody LoginUserDto loginUserDto) {
        try {
            User authenticatedUser = authenticationService.authenticate(loginUserDto);
            
            String accessToken = jwtService.generateAccessToken(authenticatedUser);
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(authenticatedUser);
            
            UserDto userDto = mapToUserDto(authenticatedUser);
       
            AuthenticationResponse response = new AuthenticationResponse(
                accessToken,
                refreshToken.getToken(),
                userDto,
                LocalDateTime.now().plusMinutes(15) // Access token expiration
            );
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            // TODO: Handle your existing 403 logic for unverified users
            if (e.getMessage().contains("not verified") || e.getMessage().contains("disabled")) {
                return ResponseEntity.status(403).body(
                    Map.of("message", "Account not verified")
                );
            }
            return ResponseEntity.status(401).body(
                Map.of("message", e.getMessage())
            );
        }
    }

    // generates new access token
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest request) {
        System.out.println("Refreshing token right now: " + System.currentTimeMillis());
        String refreshTokenValue = request.getRefreshToken();
        
        // checks if token is valid
        if (!refreshTokenService.validateRefreshToken(refreshTokenValue)) {
            return ResponseEntity.status(401).body(
                Map.of("message", "Invalid or expired refresh token")
            );
        }
        
        try {
            // Extract user from refresh token
            UUID userId = jwtService.extractUserId(refreshTokenValue);
            // TODO: error here because tries to find user by email but actually gives username
            User user = userService.findByUserId(userId);
            
            // Generate new access token
            String newAccessToken = jwtService.generateAccessToken(user);
            
            AccessTokenResponse response = new AccessTokenResponse(
                newAccessToken,
                LocalDateTime.now().plusMinutes(15)
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(
                Map.of("message", "Token refresh failed")
            );
        }
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
        try {
            authenticationService.verifyUser(verifyUserDto);
            return ResponseEntity.ok("Account verified successfully.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // TODO: implement get access token
    // @GetMapping("/auth/validate")
    // public ResponseEntity<?> validateToken(HttpServletRequest request) {
    //     // Your token validation logic
    // }

    @PostMapping("/resend")
    public ResponseEntity<?> resendVerificationCode(@RequestBody Map<String, String> body) {
        try {
            authenticationService.resendVerificationCode(body.get("email"));
            return ResponseEntity.ok("Verification code sent.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
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
