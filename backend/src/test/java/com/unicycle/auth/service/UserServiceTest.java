package com.unicycle.auth.service;

import com.unicycle.auth.dto.CurrentUserDto;
import com.unicycle.auth.entity.User;
import com.unicycle.auth.repository.UserRepository;
import com.unicycle.exception.FailedToFetchUsernameException;
import com.unicycle.exception.InvalidCredentialsException;
import com.unicycle.exception.ProfileNotFoundException;
import com.unicycle.exception.UserNotFoundException;
import com.unicycle.profile.dto.HeaderProfileDto;
import com.unicycle.profile.dto.UserBasicDto;
import com.unicycle.profile.dto.UserProfileDto;
import com.unicycle.profile.service.ProfileService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Test")
class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private ProfileService profileService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private UserService userService;

    private UUID testUserId;
    private User testUser;
    private String testEmail;
    private String testUsername;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testEmail = "john.doe@tufts.edu";
        testUsername = "jdoe";

        testUser = mock(User.class);
        lenient().when(testUser.getUserId()).thenReturn(testUserId);
        lenient().when(testUser.getEmail()).thenReturn(testEmail);
        lenient().when(testUser.getUsername()).thenReturn(testUsername);
        lenient().when(testUser.getFirstName()).thenReturn("John");
        lenient().when(testUser.getLastName()).thenReturn("Doe");
    }

    @Nested
    @DisplayName("allUsers Tests")
    class AllUsersTests {

        @Test
        void allUsers_manyUsersExist_returnsAllUsers() {
            User user1 = mock(User.class);
            User user2 = mock(User.class);
            User user3 = mock(User.class);
            when(userRepository.findAll()).thenReturn(Arrays.asList(user1, user2, user3));
            List<User> result = userService.allUsers();
            assertThat(result).hasSize(3);
            assertThat(result).containsExactly(user1, user2, user3);
        }

        @Test
        void allUsers_oneUserExists_returnsSingleUserList() {
            when(userRepository.findAll()).thenReturn(Collections.singletonList(testUser));
            List<User> result = userService.allUsers();
            assertThat(result).hasSize(1);
            assertThat(result.get(0)).isEqualTo(testUser);
        }

        @Test
        void allUsers_noUsersExist_returnsEmptyList() {
            when(userRepository.findAll()).thenReturn(Collections.emptyList());
            List<User> result = userService.allUsers();
            assertThat(result).isEmpty();
        }

        @Test
        void allUsers_databaseConnectionFailure_throwsDataAccessException() {
            when(userRepository.findAll())
                .thenThrow(new DataAccessResourceFailureException("Connection failed."));
            assertThrows(DataAccessResourceFailureException.class, () ->
                userService.allUsers());
        }

        @Test
        void allUsers_usersWithNullFields_returnsUsersWithNulls() {
            User userWithNulls = mock(User.class);
            when(userWithNulls.getFirstName()).thenReturn(null);
            when(userWithNulls.getLastName()).thenReturn(null);
            when(userRepository.findAll()).thenReturn(Collections.singletonList(userWithNulls));
            List<User> result = userService.allUsers();
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getFirstName()).isNull();
            assertThat(result.get(0).getLastName()).isNull();
        }
    }

    @Nested
    @DisplayName("findUsername Tests")
    class FindUsernameTests {
        @Test
        void findUsername_validUserIdWithUsername_returnsUsername() {
            when(userRepository.getUsernameByUserId(testUserId)).thenReturn(testUsername);
            String result = userService.findUsername(testUserId);
            assertThat(result).isEqualTo(testUsername);
        }

        @Test
        void findUsername_validUserIdNullUsername_throwsException() {
            when(userRepository.getUsernameByUserId(testUserId)).thenReturn(null);
            assertThrows(FailedToFetchUsernameException.class, () ->
                userService.findUsername(testUserId));
        }

        @Test
        void findUsername_validUserIdEmptyUsername_throwsException() {
            when(userRepository.getUsernameByUserId(testUserId)).thenReturn("");
            assertThrows(UsernameNotFoundException.class, () ->
                userService.findUsername(testUserId));
        }

        @Test
        void findUsername_nonExistentUserId_throwsException() {
            UUID nonExistentId = UUID.randomUUID();
            when(userRepository.getUsernameByUserId(nonExistentId)).thenReturn(null);
            assertThrows(FailedToFetchUsernameException.class, () ->
                userService.findUsername(nonExistentId));
        }

        @Test
        void findUsername_nullUserId_throwsException() {
            assertThrows(InvalidCredentialsException.class, () ->
                userService.findUsername(null));
        }

        @Test
        void findUsername_databaseConnectionFailure_throwsException() {
            when(userRepository.getUsernameByUserId(testUserId))
                .thenThrow(new DataAccessResourceFailureException("Connection failed."));
            assertThrows(DataAccessResourceFailureException.class, () ->
                userService.findUsername(testUserId));
        }

        @Test
        void findUsername_whitespaceOnlyUsername_throwsException() {
            when(userRepository.getUsernameByUserId(testUserId)).thenReturn("   ");
            assertThrows(UsernameNotFoundException.class, () ->
                userService.findUsername(testUserId));
        }
    }

    @Nested
    @DisplayName("findByUserId Tests")
    class FindByUserIdTests {
        @Test
        void findByUserId_validExistingUserId_returnsUser() {
            when(userRepository.findByUserId(testUserId)).thenReturn(Optional.of(testUser));
            User result = userService.findByUserId(testUserId);
            assertThat(result).isEqualTo(testUser);
            assertThat(result.getUserId()).isEqualTo(testUserId);
        }

        @Test
        void findByUserId_nonExistentUserId_throwsException() {
            UUID nonExistentId = UUID.randomUUID();
            when(userRepository.findByUserId(nonExistentId)).thenReturn(Optional.empty());
            assertThrows(UserNotFoundException.class, () ->
                userService.findByUserId(nonExistentId));
        }

        @Test
        void findByUserId_nullUserId_throwsException() {
            when(userRepository.findByUserId(null)).thenReturn(Optional.empty());
            assertThrows(UserNotFoundException.class, () ->
                userService.findByUserId(null));
        }

        @Test
        void findByUserId_databaseConnectionFailure_throwsException() {
            when(userRepository.findByUserId(testUserId))
                .thenThrow(new DataAccessResourceFailureException("Connection failed."));
            assertThrows(DataAccessResourceFailureException.class, () ->
                userService.findByUserId(testUserId));
        }

        @Test
        void findByUserId_userWithNullFields_returnsUserWithNulls() {
            User userWithNulls = mock(User.class);
            when(userWithNulls.getUserId()).thenReturn(testUserId);
            when(userWithNulls.getFirstName()).thenReturn(null);
            when(userRepository.findByUserId(testUserId)).thenReturn(Optional.of(userWithNulls));
            User result = userService.findByUserId(testUserId);
            assertThat(result.getUserId()).isEqualTo(testUserId);
            assertThat(result.getFirstName()).isNull();
        }
    }

    @Nested
    @DisplayName("findByEmail Tests")
    class FindByEmailTests {
        @Test
        void findByEmail_validExistingEmail_returnsUser() {
            when(emailService.isValidEmail(testEmail)).thenReturn(true);
            when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
            User result = userService.findByEmail(testEmail);
            assertThat(result).isEqualTo(testUser);
            assertThat(result.getEmail()).isEqualTo(testEmail);
        }

        @Test
        void findByEmail_nonExistentEmail_throwsException() {
            String nonExistentEmail = "john.doe@tufts.edu";
            when(emailService.isValidEmail(nonExistentEmail)).thenReturn(true);
            when(userRepository.findByEmail(nonExistentEmail.toLowerCase())).thenReturn(Optional.empty());
            assertThrows(UserNotFoundException.class, () -> 
                userService.findByEmail(nonExistentEmail));
        }

        @Test
        void findByEmail_nullEmail_throwsInvalidCredentialsException() {
            when(emailService.isValidEmail(null)).thenReturn(false);
            assertThrows(InvalidCredentialsException.class, () ->
                userService.findByEmail(null));
        }

        @Test
        void findByEmail_emptyStringEmail_throwsInvalidCredentialsException() {
            when(emailService.isValidEmail("")).thenReturn(false);
            assertThrows(InvalidCredentialsException.class, () ->
                userService.findByEmail(""));
        }

        @Test
        void findByEmail_differentCaseEmail_returnsUser() {
            String upperCaseEmail = "JOHN.DOE@TUFTS.EDU";
            when(emailService.isValidEmail(upperCaseEmail)).thenReturn(true);
            when(userRepository.findByEmail(upperCaseEmail.toLowerCase())).thenReturn(Optional.of(testUser));
            User result = userService.findByEmail(upperCaseEmail);
            assertThat(result).isEqualTo(testUser);
            assertThat(result.getEmail()).isEqualTo(testEmail.toLowerCase());
        }

        @Test
        void findByEmail_emailWithSpaces_returnsUser() {
            String emailWithSpaces = "    john.doe@tufts.edu ";
            when(emailService.isValidEmail(emailWithSpaces)).thenReturn(true);
            when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
            User result = userService.findByEmail(emailWithSpaces);
            assertThat(result).isEqualTo(testUser);
            assertThat(result.getEmail()).isEqualTo(emailWithSpaces.replaceAll("\\s+", ""));
        }

        @Test
        void findByEmail_invalidEmailFormat_throwsException() {
            String invalidEmail = "not-an-email";
            when(emailService.isValidEmail(invalidEmail)).thenReturn(false);
            assertThrows(InvalidCredentialsException.class, () ->
                userService.findByEmail(invalidEmail));
        }

        @Test
        void findByEmail_sqlInjectionAttempt_throwsException() {
            String maliciousEmail = "'; DROP TABLE users;--";
            when(emailService.isValidEmail(maliciousEmail)).thenReturn(false);
            assertThrows(InvalidCredentialsException.class, () ->
                userService.findByEmail(maliciousEmail));
        }

        @Test
        void findByEmail_veryLongEmail_throwsException() {
            String longEmail = "a".repeat(1000) + "@tufts.edu";
            when(emailService.isValidEmail(longEmail)).thenReturn(false);
            assertThrows(InvalidCredentialsException.class, () ->
                userService.findByEmail(longEmail));
        }

        @Test
        void findByEmail_databaseConnectionFailure_throwsDataAccessException() {
            when(emailService.isValidEmail(testEmail)).thenReturn(true);
            when(userRepository.findByEmail(testEmail.toLowerCase()))
                .thenThrow(new DataAccessResourceFailureException("Connection failed"));
            assertThrows(DataAccessResourceFailureException.class, () ->
                userService.findByEmail(testEmail));
        }
    }

    @Nested
    @DisplayName("getUserBasicDto Tests")
    class GetUserBasicDtoTests {
        private UserBasicDto testUserBasicDto;

        @BeforeEach
        void setUpDto() {
            testUserBasicDto = mock(UserBasicDto.class);
            lenient().when(testUserBasicDto.getUsername()).thenReturn(testUsername);
            lenient().when(testUserBasicDto.getFirstName()).thenReturn("John");
            lenient().when(testUserBasicDto.getLastName()).thenReturn("Doe");
        }

        @Test
        void getUserBasicDto_validUserIdCompleteData_returnsDto() {
            when(userRepository.getUserBasicDtoByUserId(testUserId)).thenReturn(testUserBasicDto);
            UserBasicDto result = userService.getUserBasicDto(testUserId);
            assertThat(result).isEqualTo(testUserBasicDto);
            assertThat(result.getUsername()).isEqualTo(testUsername);
        }

        @Test
        void getUserBasicDto_validUserIdNullFields_returnsDtoWithNulls() {
            UserBasicDto dtoWithNulls = mock(UserBasicDto.class);
            when(dtoWithNulls.getFirstName()).thenReturn(null);
            when(dtoWithNulls.getLastName()).thenReturn(null);
            when(userRepository.getUserBasicDtoByUserId(testUserId)).thenReturn(dtoWithNulls);
            UserBasicDto result = userService.getUserBasicDto(testUserId);
            assertThat(result.getFirstName()).isNull();
            assertThat(result.getLastName()).isNull();
        }

        @Test
        void getUserBasicDto_nonExistentUserId_throwsUserNotFoundException() {
            UUID nonExistentId = UUID.randomUUID();
            when(userRepository.getUserBasicDtoByUserId(nonExistentId)).thenReturn(null);
            assertThrows(UserNotFoundException.class, () ->
                userService.getUserBasicDto(nonExistentId));
        }

        @Test
        void getUserBasicDto_nullUserId_throwsUserNotFoundException() {
            when(userRepository.getUserBasicDtoByUserId(null)).thenReturn(null);
            assertThrows(UserNotFoundException.class, () ->
                userService.getUserBasicDto(null));
        }

        @Test
        void getUserBasicDto_databaseConnectionFailure_throwsDataAccessException() {
            when(userRepository.getUserBasicDtoByUserId(testUserId))
                .thenThrow(new DataAccessResourceFailureException("Connection failed"));
            assertThrows(DataAccessResourceFailureException.class, () ->
                userService.getUserBasicDto(testUserId));
        }
    }

    @Nested
    @DisplayName("getCurrentUserDto Tests")
    class GetCurrentUserDtoTests {
        @Test
        void getCurrentUserDto_authenticatedUser_returnsDto() {
            Authentication authentication = mock(Authentication.class);
            SecurityContext securityContext = mock(SecurityContext.class);
            
            when(authentication.getPrincipal()).thenReturn(testUser);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            
            try (MockedStatic<SecurityContextHolder> mockedHolder = mockStatic(
                SecurityContextHolder.class
            )) {
                mockedHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
                CurrentUserDto result = userService.getCurrentUserDto();

                assertThat(result).isNotNull();
                assertThat(result.getUserId()).isEqualTo(testUserId);
                assertThat(result.getEmail()).isEqualTo(testEmail);
                assertThat(result.getUsername()).isEqualTo(testUsername);
                assertThat(result.getFirstName()).isEqualTo("John");
                assertThat(result.getLastName()).isEqualTo("Doe");
            }
        }

        @Test
        void getCurrentUserDto_noAuthenticationInContext_throwsException() {
            SecurityContext securityContext = mock(SecurityContext.class);
            when(securityContext.getAuthentication()).thenReturn(null);
            try (MockedStatic<SecurityContextHolder> mockedHolder = mockStatic(
                SecurityContextHolder.class)
            ) {
                mockedHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
                assertThrows(NullPointerException.class, () ->
                    userService.getCurrentUserDto());
            }
        }

        @Test
        void getCurrentUserDto_nullPrincipal_throwsException() {
            Authentication authentication = mock(Authentication.class);
            SecurityContext securityContext = mock(SecurityContext.class);
            
            when(authentication.getPrincipal()).thenReturn(null);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            
            try (MockedStatic<SecurityContextHolder> mockedHolder = mockStatic(
                SecurityContextHolder.class
            )) {
                mockedHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
                assertThrows(NullPointerException.class, () ->
                    userService.getCurrentUserDto());
            }
        }

        @Test
        void getCurrentUserDto_principalNotUser_throwsException() {
            Authentication authentication = mock(Authentication.class);
            SecurityContext securityContext = mock(SecurityContext.class);
            UserDetails nonUserPrincipal = mock(UserDetails.class);
            
            when(authentication.getPrincipal()).thenReturn(nonUserPrincipal);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            
            try (MockedStatic<SecurityContextHolder> mockedHolder = mockStatic(
                SecurityContextHolder.class
            )) {
                mockedHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
                assertThrows(ClassCastException.class, () ->
                    userService.getCurrentUserDto());
            }
        }

        @Test
        void getCurrentUserDto_userWithNullFields_returnsDtoWithNulls() {
            User userWithNulls = mock(User.class);
            when(userWithNulls.getUserId()).thenReturn(testUserId);
            when(userWithNulls.getEmail()).thenReturn(testEmail);
            when(userWithNulls.getUsername()).thenReturn(testUsername);
            when(userWithNulls.getFirstName()).thenReturn(null);
            when(userWithNulls.getLastName()).thenReturn(null);
            
            Authentication authentication = mock(Authentication.class);
            SecurityContext securityContext = mock(SecurityContext.class);
            
            when(authentication.getPrincipal()).thenReturn(userWithNulls);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            
            try (MockedStatic<SecurityContextHolder> mockedHolder = mockStatic(
                SecurityContextHolder.class
            )) {
                mockedHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
                CurrentUserDto result = userService.getCurrentUserDto();
                assertThat(result.getFirstName()).isNull();
                assertThat(result.getLastName()).isNull();
            }
        }

        @Test
        void getCurrentUserDto_anonymousAuthentication_throwsException() {
            Authentication authentication = mock(Authentication.class);
            SecurityContext securityContext = mock(SecurityContext.class);
            
            when(authentication.getPrincipal()).thenReturn("anonymousUser");
            when(securityContext.getAuthentication()).thenReturn(authentication);
            
            try (MockedStatic<SecurityContextHolder> mockedHolder = mockStatic(
                SecurityContextHolder.class
            )) {
                mockedHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
                assertThrows(ClassCastException.class, () ->
                    userService.getCurrentUserDto());
            }
        }

        @Test
        void getCurrentUserDto_nullSecurityContext_throwsNullPointerException() {
            try (MockedStatic<SecurityContextHolder> mockedHolder = mockStatic(
                SecurityContextHolder.class
            )) {
                mockedHolder.when(SecurityContextHolder::getContext).thenReturn(null);
                assertThrows(NullPointerException.class, () ->
                    userService.getCurrentUserDto());
            }
        }
    }

    @Nested
    @DisplayName("getHeaderProfileDto Tests")
    class GetHeaderProfileDtoTests {
        private UserBasicDto testUserBasicDto;
        private UserProfileDto testUserProfileDto;

        @BeforeEach
        void setUpDtos() {
            testUserBasicDto = mock(UserBasicDto.class);
            lenient().when(testUserBasicDto.getUsername()).thenReturn(testUsername);
            lenient().when(testUserBasicDto.getFirstName()).thenReturn("John");
            lenient().when(testUserBasicDto.getLastName()).thenReturn("Doe");

            testUserProfileDto = mock(UserProfileDto.class);
            lenient().when(testUserProfileDto.getProfileImage()).thenReturn("https://example.com/profile.jpg");
        }

        @Test
        void getHeaderProfileDto_validUserIdWithProfile_returnsDto() {
            when(userRepository.getUserBasicDtoByUserId(testUserId)).thenReturn(testUserBasicDto);
            when(profileService.getUserProfileDto(testUserId)).thenReturn(testUserProfileDto);
            HeaderProfileDto result = userService.getHeaderProfileDto(testUserId);

            assertThat(result).isNotNull();
            assertThat(result.getUserId()).isEqualTo(testUserId);
            assertThat(result.getUsername()).isEqualTo(testUsername);
            assertThat(result.getFirstName()).isEqualTo("John");
            assertThat(result.getLastName()).isEqualTo("Doe");
            assertThat(result.getProfileImageUrl()).isEqualTo("https://example.com/profile.jpg");
        }

        @Test
        void getHeaderProfileDto_validUserIdNoProfile_throwsException() {
            when(userRepository.getUserBasicDtoByUserId(testUserId)).thenReturn(testUserBasicDto);
            when(profileService.getUserProfileDto(testUserId))
                .thenThrow(new ProfileNotFoundException("Profile not found with userId: " + testUserId));
            assertThrows(ProfileNotFoundException.class, () ->
                userService.getHeaderProfileDto(testUserId));
        }

        @Test
        void getHeaderProfileDto_nonExistentUserId_throwsException() {
            UUID nonExistentId = UUID.randomUUID();
            when(userRepository.getUserBasicDtoByUserId(nonExistentId)).thenReturn(null);
            assertThrows(UserNotFoundException.class, () ->
                userService.getHeaderProfileDto(nonExistentId));
        }

        @Test
        void getHeaderProfileDto_nullUserId_throwsUserNotFoundException() {
            when(userRepository.getUserBasicDtoByUserId(null)).thenReturn(null);
            assertThrows(UserNotFoundException.class, () ->
                userService.getHeaderProfileDto(null));
        }

        @Test
        void getHeaderProfileDto_profileWithNullImage_returnsDtoWithNullImage() {
            UserProfileDto profileWithNullImage = mock(UserProfileDto.class);
            when(profileWithNullImage.getProfileImage()).thenReturn(null);
            when(userRepository.getUserBasicDtoByUserId(testUserId)).thenReturn(testUserBasicDto);
            when(profileService.getUserProfileDto(testUserId)).thenReturn(profileWithNullImage);

            HeaderProfileDto result = userService.getHeaderProfileDto(testUserId);
            assertThat(result.getProfileImageUrl()).isNull();
        }

        @Test
        void getHeaderProfileDto_profileServiceThrowsException_propagates() {
            when(userRepository.getUserBasicDtoByUserId(testUserId)).thenReturn(testUserBasicDto);
            when(profileService.getUserProfileDto(testUserId))
                .thenThrow(new ProfileNotFoundException("Profile not found with userId: " + testUserId));
            assertThrows(ProfileNotFoundException.class, () ->
                userService.getHeaderProfileDto(testUserId));
        }

        @Test
        void getHeaderProfileDto_profileServiceReturnsNull_throwsException() {
            when(userRepository.getUserBasicDtoByUserId(testUserId)).thenReturn(testUserBasicDto);
            when(profileService.getUserProfileDto(testUserId)).thenReturn(null);
            assertThrows(NullPointerException.class, () ->
                userService.getHeaderProfileDto(testUserId));
        }

        @Test
        void getHeaderProfileDto_databaseConnectionFailure_throwsException() {
            when(userRepository.getUserBasicDtoByUserId(testUserId))
                .thenThrow(new DataAccessResourceFailureException("Connection failed"));
            assertThrows(DataAccessResourceFailureException.class, () ->
                userService.getHeaderProfileDto(testUserId));
        }

        @Test
        void getHeaderProfileDto_inconsistentData_returnsDtoWithFetchedData() {
            UserBasicDto basicWithDifferentName = mock(UserBasicDto.class);
            when(basicWithDifferentName.getUsername()).thenReturn("differentUsername");
            when(basicWithDifferentName.getFirstName()).thenReturn("Different");
            when(basicWithDifferentName.getLastName()).thenReturn("Name");
            when(userRepository.getUserBasicDtoByUserId(testUserId)).thenReturn(basicWithDifferentName);
            when(profileService.getUserProfileDto(testUserId)).thenReturn(testUserProfileDto);

            HeaderProfileDto result = userService.getHeaderProfileDto(testUserId);
            assertThat(result.getUsername()).isEqualTo("differentUsername");
            assertThat(result.getFirstName()).isEqualTo("Different");
        }
    }

    @Nested
    @DisplayName("Cross-Cutting Edge Cases")
    class CrossCuttingTests {
        @Test
        void findByUserIdThenGetHeader_fullFlow_works() {
            UserBasicDto basicDto = mock(UserBasicDto.class);
            when(basicDto.getUsername()).thenReturn(testUsername);
            when(basicDto.getFirstName()).thenReturn("Test");
            when(basicDto.getLastName()).thenReturn("User");
            
            UserProfileDto profileDto = mock(UserProfileDto.class);
            when(profileDto.getProfileImage()).thenReturn("https://example.com/img.jpg");
            when(userRepository.findByUserId(testUserId)).thenReturn(Optional.of(testUser));
            when(userRepository.getUserBasicDtoByUserId(testUserId)).thenReturn(basicDto);
            when(profileService.getUserProfileDto(testUserId)).thenReturn(profileDto);

            User user = userService.findByUserId(testUserId);
            HeaderProfileDto header = userService.getHeaderProfileDto(user.getUserId());

            assertThat(header.getUserId()).isEqualTo(testUserId);
            assertThat(header.getUsername()).isEqualTo(testUsername);
        }

        @Test
        void findByEmailThenById_sameUser_consistent() {
            when(emailService.isValidEmail(testEmail)).thenReturn(true);
            when(userRepository.findByEmail(testEmail.toLowerCase())).thenReturn(Optional.of(testUser));
            when(userRepository.findByUserId(testUserId)).thenReturn(Optional.of(testUser));

            User byEmail = userService.findByEmail(testEmail);
            User byId = userService.findByUserId(byEmail.getUserId());

            assertThat(byEmail).isEqualTo(byId);
        }

        @Test
        void allMethods_verifyRepositoryInteractions_correct() {
            when(emailService.isValidEmail(testEmail)).thenReturn(true);
            when(userRepository.findByUserId(testUserId)).thenReturn(Optional.of(testUser));
            when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
            when(userRepository.getUsernameByUserId(testUserId)).thenReturn(testUsername);

            userService.findByUserId(testUserId);
            userService.findByEmail(testEmail);
            userService.findUsername(testUserId);

            verify(userRepository).findByUserId(testUserId);
            verify(userRepository).findByEmail(testEmail);
            verify(userRepository).getUsernameByUserId(testUserId);
        }
    }
}