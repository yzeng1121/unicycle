package com.unicycle.auth.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import jakarta.mail.MessagingException;

import com.unicycle.auth.dto.RegisterUserDto;
import com.unicycle.auth.dto.LoginUserDto;
import com.unicycle.auth.dto.RegisterUserResponse;
import com.unicycle.auth.dto.VerifyUserDto;
import com.unicycle.auth.entity.User;
import com.unicycle.auth.repository.UserRepository;
import com.unicycle.exception.EmailAlreadyExistsException;
import com.unicycle.exception.ExpiredVerificationException;
import com.unicycle.exception.InvalidCredentialsException;
import com.unicycle.exception.InvalidVerificationException;
import com.unicycle.exception.UserAlreadyVerifiedException;
import com.unicycle.exception.UserNotVerifiedException;
import com.unicycle.exception.UsernameAlreadyExistsException;
import com.unicycle.profile.dto.UserBasicDto;
import com.unicycle.profile.entity.Profile;
import com.unicycle.profile.service.ProfileService;


@ExtendWith(MockitoExtension.class)
@DisplayName("AuthenticationService Tests")
public class AuthenticationServiceTest {
    @Mock
    private UserRepository mockUserRepository;

    @Mock
    private PasswordEncoder mockPasswordEncoder;

    @Mock
    private AuthenticationManager mockAuthenticationManager;

    @Mock
    private ProfileService mockProfileService;

    @Mock
    private EmailService mockEmailService;

    @InjectMocks
    private AuthenticationService authenticationService;

    private static final String TEST_EMAIL = "john.doe@tufts.edu";
    private static final String TEST_USERNAME = "jdoe";
    private static final String TEST_FIRST_NAME = "John";
    private static final String TEST_LAST_NAME = "Doe";
    private static final String TEST_DORM = "Harleston Hall";
    private static final String TEST_PASSWORD = "password123!@#";
    private static final String TEST_VERIFICATION_CODE = "123ABC";

    private static final RegisterUserDto testDto = RegisterUserDto.builder()
            .firstName(TEST_FIRST_NAME)
            .lastName(TEST_LAST_NAME)
            .dorm(TEST_DORM)
            .email(TEST_EMAIL)
            .password(TEST_PASSWORD)
            .username(TEST_USERNAME)
            .build();

    private User createMockUser(boolean enabled, String verificationCode, LocalDateTime expiresAt) {
        return User.builder()
                .userId(UUID.randomUUID())
                .firstName(TEST_FIRST_NAME)
                .lastName(TEST_LAST_NAME)
                .dorm(TEST_DORM)
                .username(TEST_USERNAME)
                .email(TEST_EMAIL)
                .password(TEST_PASSWORD)
                .verificationCode(verificationCode)
                .verificationCodeExpiresAt(expiresAt)
                .enabled(enabled)
                .build();
    }

    @Test
    void signup_success() throws MessagingException {
        when(mockUserRepository.existsByUsername(anyString())).thenReturn(false);
        when(mockUserRepository.existsByEmail(anyString())).thenReturn(false);

        UUID mockUserId = UUID.randomUUID();

        when(mockUserRepository.save(any())).thenAnswer(invocation -> {
            com.unicycle.auth.entity.User user = invocation.getArgument(0);
            return com.unicycle.auth.entity.User.builder()
                .userId(mockUserId)
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .dorm(user.getDorm())
                .username(user.getUsername())
                .email(user.getEmail())
                .password(user.getPassword())
                .verificationCode(user.getVerificationCode())
                .verificationCodeExpiresAt(user.getVerificationCodeExpiresAt())
                .enabled(user.isEnabled())
                .build();
        });

        Profile mockProfile = Profile.builder()
            .userId(mockUserId)
            .profileImage(null)
            .listings(new ArrayList<>())
            .savedListings(new ArrayList<>())
            .purchased(new ArrayList<>())
            .followers(new ArrayList<>())
            .followerCount(0)
            .following(new ArrayList<>())
            .followingCount(0)
            .rating(0)
            .build();

        when(mockProfileService.createProfile(mockUserId)).thenReturn(mockProfile);

        RegisterUserResponse response = authenticationService.signup(testDto);
        UserBasicDto dto = response.getUser();

        assertTrue(dto != null);
        assertEquals(mockUserId, dto.getUserId());
        assertEquals(TEST_USERNAME, dto.getUsername());
        assertEquals(TEST_FIRST_NAME, dto.getFirstName());
        assertEquals(TEST_LAST_NAME, dto.getLastName());
        assertEquals(TEST_DORM, dto.getDorm());
    }

    @Test
    void signup_duplicateUsername_throwsException() {
        when (mockUserRepository.existsByUsername(anyString())).thenReturn(true);
        assertThrows(UsernameAlreadyExistsException.class, () -> {
            authenticationService.signup(testDto);
        });
    }
    
    @Test
    void signup_duplicateEmail_throwsException() {
        when (mockUserRepository.existsByUsername(anyString())).thenReturn(false);
        when (mockUserRepository.existsByEmail(anyString())).thenReturn(true);
        assertThrows(EmailAlreadyExistsException.class, () -> {
            authenticationService.signup(testDto);
        });
    }

    @Test
    void authenticate_success() {
        LoginUserDto dto = new LoginUserDto(TEST_EMAIL, TEST_PASSWORD);
        User mockUser = createMockUser(true, TEST_VERIFICATION_CODE, LocalDateTime.now().plusMinutes(10));

        when(mockUserRepository.findByEmail(anyString())).thenReturn(Optional.of(mockUser));
        assertNotNull(authenticationService.authenticate(dto));
    }

    @Test
    void authenticate_invalidEmail_throwsException() {
        LoginUserDto dto = new LoginUserDto(TEST_EMAIL, TEST_PASSWORD);
        assertThrows(InvalidCredentialsException.class, () ->
            authenticationService.authenticate(dto));
    }

    @Test
    void authenticate_unverifiedAccount_throwsException() {
        LoginUserDto dto = new LoginUserDto(TEST_EMAIL, TEST_PASSWORD);
        User mockUser = createMockUser(false, TEST_VERIFICATION_CODE, LocalDateTime.now().plusMinutes(10));

        when(mockUserRepository.findByEmail(anyString())).thenReturn(Optional.of(mockUser));
        assertThrows(UserNotVerifiedException.class, () ->
            authenticationService.authenticate(dto));
    }

    @Test
    void verifyUser_success() {
        User mockUser = createMockUser(false, TEST_VERIFICATION_CODE, LocalDateTime.now().plusMinutes(10));

        when(mockUserRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(mockUser));
        when(mockUserRepository.save(any(User.class))).thenReturn(mockUser);

        VerifyUserDto dto = new VerifyUserDto(TEST_EMAIL, TEST_VERIFICATION_CODE);
        assertDoesNotThrow(() -> {
            authenticationService.verifyUser(dto);
        });
    }

    @Test
    void verifyUser_expiredVerificationCode_throwsException() {
        User mockUser = createMockUser(false, TEST_VERIFICATION_CODE, LocalDateTime.now().minusMinutes(10));

        when(mockUserRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(mockUser));

        VerifyUserDto dto = new VerifyUserDto(TEST_EMAIL, TEST_VERIFICATION_CODE);
        assertThrows(ExpiredVerificationException.class, () -> {
            authenticationService.verifyUser(dto);
        });
    }

    @Test
    void verifyUser_wrongVerificationCode_throwsException() {
        User mockUser = createMockUser(false, TEST_VERIFICATION_CODE, LocalDateTime.now().plusMinutes(10));

        when(mockUserRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(mockUser));

        VerifyUserDto dto = new VerifyUserDto(TEST_EMAIL, "123ABD");
        assertThrows(InvalidVerificationException.class, () -> {
            authenticationService.verifyUser(dto);
        });
    }

    @Test
    void resendVerificationCode_success() {
        User mockUser = createMockUser(false, TEST_VERIFICATION_CODE, LocalDateTime.now().plusMinutes(10));
        when(mockUserRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(mockUser));
        when(mockUserRepository.save(any(User.class))).thenReturn(mockUser);
        assertDoesNotThrow(() -> {
            authenticationService.resendVerificationCode(TEST_EMAIL);
        });
    }

    @Test
    void resendVerificationCode_alreadyVerified_throwsException() {
        User mockUser = createMockUser(true, TEST_VERIFICATION_CODE, LocalDateTime.now().plusMinutes(10));
        when(mockUserRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(mockUser));
        assertThrows(UserAlreadyVerifiedException.class, () -> {
            authenticationService.resendVerificationCode(TEST_EMAIL);
        });
    }

    @Test
    void sendVerificationEmail_success() {
        User mockUser = createMockUser(false, TEST_VERIFICATION_CODE, LocalDateTime.now().plusMinutes(10));
        assertDoesNotThrow(() -> {
            authenticationService.sendVerificationEmail(mockUser);
        });
    }

    @Test
    void sendVerificationEmail_throwsException() throws MessagingException {
        User mockUser = createMockUser(false, TEST_VERIFICATION_CODE, LocalDateTime.now().plusMinutes(10));
        doThrow(new MessagingException("Failed to send verification code to user email."))
            .when(mockEmailService).sendVerificationEmail(anyString(), anyString(), anyString());
        assertThrows(MessagingException.class, () -> {
            authenticationService.sendVerificationEmail(mockUser);
        });
    }

    // TODO: test the three private methods in the service class
}
