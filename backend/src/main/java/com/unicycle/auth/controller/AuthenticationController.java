package com.unicycle.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.unicycle.auth.dto.AccessTokenResponse;
import com.unicycle.auth.dto.AuthenticationResponse;
import com.unicycle.auth.dto.LoginUserDto;
import com.unicycle.auth.dto.RefreshTokenRequest;
import com.unicycle.auth.dto.RegisterUserDto;
import com.unicycle.auth.dto.RegisterUserResponse;
import com.unicycle.auth.dto.UserDto;
import com.unicycle.auth.dto.VerifyUserDto;
import com.unicycle.auth.entity.RefreshToken;
import com.unicycle.auth.entity.User;
import com.unicycle.auth.service.AuthenticationService;
import com.unicycle.auth.service.JwtService;
import com.unicycle.auth.service.RefreshTokenService;
import com.unicycle.auth.service.UserService;
import com.unicycle.exception.ExpiredVerificationException;
import com.unicycle.exception.InvalidCredentialsException;
import com.unicycle.exception.FailedToExtractUserIdException;

import jakarta.mail.MessagingException;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
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

    private static final int ACCESS_TOKEN_EXPIRY_MINUTES = 15;

    @PostMapping("/signup")
    public ResponseEntity<RegisterUserResponse> register(
        @RequestBody RegisterUserDto registerUserDto
    ) throws MessagingException {
        return ResponseEntity.ok(authenticationService.signup(registerUserDto));
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
            throw new FailedToExtractUserIdException("Failed to extract user id from token: " + e.getMessage());
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

    // TODO: tighten the response entity types from ? -> an actual object
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();
        if (refreshToken != null) refreshTokenService.revokeToken(refreshToken);
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
    public ResponseEntity<?> resendVerificationCode(
        @RequestBody Map<String, String> body
    ) throws MessagingException {
        if (body.isEmpty()) throw new InvalidCredentialsException("Please provide proper email to resend.");
        authenticationService.resendVerificationCode(body.get("email"));
        return ResponseEntity.ok("Verification code sent.");
    }

    // TODO: reset password 
    // TODO: frontend implementation/email HTML file
    // TODO: implement testing for this in AuthenticationControllerTests
    // TODO: for now... a user must KNOW their old password to reset their old
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody LoginUserDto loginUserDto) {
        User user = userService.findByEmail(loginUserDto.getEmail());
        UUID userId = user.getUserId();
        String password = loginUserDto.getPassword();

        userService.changePassword(userId, password);
        return ResponseEntity.ok("Password has been reset.");
    }

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
