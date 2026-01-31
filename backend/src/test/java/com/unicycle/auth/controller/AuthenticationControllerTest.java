package com.unicycle.auth.controller;

import com.unicycle.auth.dto.*;
import com.unicycle.auth.entity.RefreshToken;
import com.unicycle.auth.entity.User;
import com.unicycle.auth.service.AuthenticationService;
import com.unicycle.auth.service.JwtService;
import com.unicycle.auth.service.RefreshTokenService;
import com.unicycle.auth.service.UserService;
import com.unicycle.exception.EmailAlreadyExistsException;
import com.unicycle.exception.ExpiredVerificationException;
import com.unicycle.exception.FailedToExtractUserIdException;
import com.unicycle.exception.InvalidCredentialsException;
import com.unicycle.exception.InvalidVerificationException;
import com.unicycle.exception.UserNotFoundException;
import com.unicycle.exception.UserNotVerifiedException;

import jakarta.mail.MessagingException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthenticationControllerTest {
    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthenticationController authenticationController;

    private User testUser;
    private UUID testUserId;
    private String mockTokenVal;
    private RefreshToken testRefreshToken;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testUser = new User();
        testUser.setUserId(testUserId);
        testUser.setEmail("john.doe@tufts.edu");
        testUser.setUsername("jdoe");
        testUser.setEnabled(true);

        mockTokenVal = """
            eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.\
            eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMn0.\
            KMUFsIDTnFmyG3nMiGM6H9FNFUROf3wh7SmqJp-QV30\
            """;
        testRefreshToken = new RefreshToken();
        testRefreshToken.setToken(mockTokenVal);
        testRefreshToken.setUserId(testUserId);
        testRefreshToken.setExpiresAt(LocalDateTime.now().plusSeconds(86400));
    }

    @Nested
    @DisplayName("Signup Endpoint Tests")
    class SignupTests {
        @Test
        void signup_validRequest_returnsOkResponse() throws MessagingException {
            RegisterUserDto dto = RegisterUserDto.builder()
                .firstName("John")
                .lastName("Doe")
                .dorm("Harleston")
                .email("john.doe@tufts.edu")
                .password("password123!@#")
                .username("jdoe")
                .build();
            RegisterUserResponse expectedResponse = new RegisterUserResponse();
            when(authenticationService.signup(any(RegisterUserDto.class))).thenReturn(expectedResponse);

            ResponseEntity<RegisterUserResponse> response = authenticationController.register(dto);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            verify(authenticationService).signup(dto);
        }

        @Test
        void signup_serviceThrowsException_propagatesException() throws MessagingException {
            RegisterUserDto dto = RegisterUserDto.builder()
                .firstName("John")
                .lastName("Doe")
                .dorm("Carmichael")
                .email("existing@tufts.edu")
                .password("Password123!")
                .username("newuser")
                .build();
            when(authenticationService.signup(any(RegisterUserDto.class)))
                .thenThrow(new EmailAlreadyExistsException("Email already exists."));
            assertThrows(EmailAlreadyExistsException.class, () -> 
                authenticationController.register(dto));
        }
    }

    @Nested
    @DisplayName("Login Endpoint Tests")
    class LoginTests {
        @Test
        void login_validCredentials_returnsAuthResponse() {
            LoginUserDto dto = new LoginUserDto("john.doe@tufts.edu", "password123!@#");
            when(authenticationService.authenticate(any(LoginUserDto.class))).thenReturn(testUser);
            when(jwtService.generateAccessToken(testUser)).thenReturn("access-token");
            when(refreshTokenService.createRefreshToken(testUser)).thenReturn(testRefreshToken);

            ResponseEntity<AuthenticationResponse> response = authenticationController.authenticate(dto);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertNotNull(response.getBody().getAccessToken());
            assertNotNull(response.getBody().getRefreshToken());
            verify(authenticationService).authenticate(dto);
            verify(jwtService).generateAccessToken(testUser);
            verify(refreshTokenService).createRefreshToken(testUser);
        }

        @Test
        void login_invalidCredentials_throwsException() {
            LoginUserDto dto = new LoginUserDto("john.doe@tufts.edu", "wongPassword!");
            when(authenticationService.authenticate(any(LoginUserDto.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials."));
            assertThrows(BadCredentialsException.class, () -> 
            authenticationController.authenticate(dto));
        }

        @Test
        void login_unverifiedAccount_throwsException() {
            LoginUserDto dto = new LoginUserDto("jane.doe@tufts.edu", "password123!@#");
            when(authenticationService.authenticate(any(LoginUserDto.class)))
                .thenThrow(new UserNotVerifiedException("Account not verified. Please verify your account."));
            assertThrows(UserNotVerifiedException.class, () -> 
                authenticationController.authenticate(dto));
        }
    }

    @Nested
    @DisplayName("Refresh Token Endpoint Tests")
    class RefreshTokenTests {
        @Test
        void refreshToken_validToken_returnsNewAccessToken() {
            RefreshTokenRequest request = new RefreshTokenRequest(mockTokenVal);
            when(refreshTokenService.validateRefreshToken(mockTokenVal)).thenReturn(true);
            when(jwtService.extractUserId(mockTokenVal)).thenReturn(testUserId);
            when(userService.findByUserId(testUserId)).thenReturn(testUser);
            when(jwtService.generateAccessToken(testUser)).thenReturn("new-access-token");

            ResponseEntity<?> response = authenticationController.refreshToken(request);

            assertEquals(HttpStatus.OK, response.getStatusCode());
        }

        @Test
        void refreshToken_invalidToken_throwsExpiredVerificationException() {
            RefreshTokenRequest request = new RefreshTokenRequest("expired-token");
            when(refreshTokenService.validateRefreshToken("expired-token")).thenReturn(false);
            assertThrows(ExpiredVerificationException.class, () -> 
                authenticationController.refreshToken(request));
        }

        @Test
        void refreshToken_userNotFound_throwsException() {
            RefreshTokenRequest request = new RefreshTokenRequest("deleted-user-token");
            when(refreshTokenService.validateRefreshToken("deleted-user-token")).thenReturn(true);
            when(jwtService.extractUserId("deleted-user-token")).thenReturn(testUserId);
            when(userService.findByUserId(testUserId))
                .thenThrow(new UserNotFoundException("User not found."));
            assertThrows(FailedToExtractUserIdException.class, () -> 
                authenticationController.refreshToken(request));
        }

        @Test
        void refreshToken_extractUserIdFails_throwsException() {
            RefreshTokenRequest request = new RefreshTokenRequest("valid-looking-token");
            when(refreshTokenService.validateRefreshToken("valid-looking-token")).thenReturn(true);
            when(jwtService.extractUserId("valid-looking-token"))
                .thenThrow(new FailedToExtractUserIdException("Failed to extract user id from token."));
            assertThrows(FailedToExtractUserIdException.class, () -> 
                authenticationController.refreshToken(request));
        }
    }

    @Nested
    @DisplayName("Logout Endpoint Tests")
    class LogoutTests {
        @Test
        void logout_validRefreshToken_returnsSuccessMessage() {
            RefreshTokenRequest request = new RefreshTokenRequest("valid-refresh-token");
            ResponseEntity<?> response = authenticationController.logout(request);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(refreshTokenService).revokeToken("valid-refresh-token");
        }

        @Test
        void logout_nullToken_returnsSuccessWithoutRevocation() {
            RefreshTokenRequest request = new RefreshTokenRequest(null);
            ResponseEntity<?> response = authenticationController.logout(request);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(refreshTokenService, never()).revokeToken(anyString());
        }
    }

    @Nested
    @DisplayName("Verify Endpoint Tests")
    class VerifyTests {
        @Test
        void verify_correctCode_returnsSuccessMessage() {
            VerifyUserDto dto = new VerifyUserDto("john.doe@tufts.edu", "123456");
            doNothing().when(authenticationService).verifyUser(any(VerifyUserDto.class));

            ResponseEntity<?> response = authenticationController.verifyUser(dto);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals("Account verified successfully.", response.getBody());
            verify(authenticationService).verifyUser(dto);
        }

        @Test
        void verify_wrongCode_throwsException() {
            VerifyUserDto dto = new VerifyUserDto("john.doe@tufts.edu", "999999");
            doThrow(new InvalidVerificationException("Invalid verification code."))
                .when(authenticationService).verifyUser(any(VerifyUserDto.class));
            assertThrows(InvalidVerificationException.class, () -> 
                authenticationController.verifyUser(dto));
        }

        @Test
        void verify_expiredCode_throwsExpiredVerificationException() {
            VerifyUserDto dto = new VerifyUserDto("john.doe@tufts.edu", "123456");
            doThrow(new ExpiredVerificationException("Verification code expired."))
                .when(authenticationService).verifyUser(any(VerifyUserDto.class));
            assertThrows(ExpiredVerificationException.class, () -> 
                authenticationController.verifyUser(dto));
        }
    }

    @Nested
    @DisplayName("Resend Verification Endpoint Tests")
    class ResendVerificationTests {
        @Test
        void resend_validEmail_returnsSuccessMessage() throws MessagingException {
            Map<String, String> body = Map.of("email", "jane.doe@tufts.edu");
            doNothing().when(authenticationService).resendVerificationCode("jane.doe@tufts.edu");

            ResponseEntity<?> response = authenticationController.resendVerificationCode(body);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals("Verification code sent.", response.getBody());
            verify(authenticationService).resendVerificationCode("jane.doe@tufts.edu");
        }

        @Test
        void resend_emailNotFound_throwsException() throws MessagingException {
            Map<String, String> body = Map.of("email", "jdidoe@tufts.edu");
            doThrow(new UserNotFoundException("User not found."))
                .when(authenticationService).resendVerificationCode("jdidoe@tufts.edu");
            assertThrows(UserNotFoundException.class, () -> 
                authenticationController.resendVerificationCode(body));
        }

        @Test
        void resend_missingEmailKey_throwsException() {
            Map<String, String> body = Map.of();
            assertThrows(InvalidCredentialsException.class, () -> 
                authenticationController.resendVerificationCode(body));
        }

        @Test
        void resend_emailServiceDown_throwsException() throws MessagingException {
            Map<String, String> body = Map.of("email", "john.doe@tufts.edu");
            doThrow(new MessagingException("Email service unavailable."))
                .when(authenticationService).resendVerificationCode("john.doe@tufts.edu");
            assertThrows(MessagingException.class, () -> 
                authenticationController.resendVerificationCode(body));
        }
    }
}
