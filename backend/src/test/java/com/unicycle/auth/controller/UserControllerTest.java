package com.unicycle.auth.controller;

import com.unicycle.auth.dto.CurrentUserDto;
import com.unicycle.auth.entity.User;
import com.unicycle.auth.service.UserService;
import com.unicycle.exception.FailedToFetchUsernameException;
import com.unicycle.exception.InvalidCredentialsException;
import com.unicycle.exception.ProfileNotFoundException;
import com.unicycle.exception.UserNotFoundException;
import com.unicycle.profile.dto.HeaderProfileDto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserController Test")
public class UserControllerTest {
    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private User testUser;
    private UUID testUserId;
    private String testUsername;
    private String testEmail;
    private CurrentUserDto testCurrentUserDto;
    private HeaderProfileDto testHeaderProfileDto;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testUsername = "jdoe";
        testEmail = "john.doe@tufts.edu";

        testUser = new User();
        testUser.setUserId(testUserId);
        testUser.setEmail(testEmail);
        testUser.setUsername(testUsername);
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setEnabled(true);

        testCurrentUserDto = CurrentUserDto.builder()
            .userId(testUserId)
            .email(testEmail)
            .username(testUsername)
            .firstName("John")
            .lastName("Doe")
            .build();

        testHeaderProfileDto = HeaderProfileDto.builder()
            .userId(testUserId)
            .username(testUsername)
            .firstName("John")
            .lastName("Doe")
            .profileImageUrl("https://example.com/profile.jpg")
            .build();
    }

    @Nested
    @DisplayName("/users/me Tests")
    class AuthenticatedUserTests {

        @Test
        void authenticatedUser_completeProfileInformation_returnsCurrentUserDto() {
            when(userService.getCurrentUserDto()).thenReturn(testCurrentUserDto);

            ResponseEntity<CurrentUserDto> response = userController.authenticatedUser();

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(testUserId, response.getBody().getUserId());
            assertEquals(testEmail, response.getBody().getEmail());
            assertEquals(testUsername, response.getBody().getUsername());
            assertEquals("John", response.getBody().getFirstName());
            assertEquals("Doe", response.getBody().getLastName());
            verify(userService).getCurrentUserDto();
        }

        @Test
        void authenticatedUser_minimalProfileInformation_returnsDto() {
            CurrentUserDto minimalDto = CurrentUserDto.builder()
                .userId(testUserId)
                .email(testEmail)
                .username(testUsername)
                .firstName(null)
                .lastName(null)
                .build();
            when(userService.getCurrentUserDto()).thenReturn(minimalDto);

            ResponseEntity<CurrentUserDto> response = userController.authenticatedUser();

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertNull(response.getBody().getFirstName());
            assertNull(response.getBody().getLastName());
        }

        @Test
        void authenticatedUser_newlyRegisteredUser_returnsDto() {
            when(userService.getCurrentUserDto()).thenReturn(testCurrentUserDto);

            ResponseEntity<CurrentUserDto> response = userController.authenticatedUser();

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            verify(userService).getCurrentUserDto();
        }

        @Test
        void authenticatedUser_userWithUpdatedProfile_returnsCurrentData() {
            CurrentUserDto updatedDto = CurrentUserDto.builder()
                .userId(testUserId)
                .email("newemail@tufts.edu")
                .username("newusername")
                .firstName("UpdatedFirst")
                .lastName("UpdatedLast")
                .build();
            when(userService.getCurrentUserDto()).thenReturn(updatedDto);

            ResponseEntity<CurrentUserDto> response = userController.authenticatedUser();

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals("newemail@tufts.edu", response.getBody().getEmail());
            assertEquals("newusername", response.getBody().getUsername());
        }

        @Test
        void authenticatedUser_noAuthenticationInContext_throwsException() {
            when(userService.getCurrentUserDto()).thenThrow(new NullPointerException("No authentication"));

            assertThrows(NullPointerException.class, () -> userController.authenticatedUser());
        }

        @Test
        void authenticatedUser_userDeletedFromDatabase_throwsException() {
            when(userService.getCurrentUserDto()).thenThrow(new UserNotFoundException("User not found"));

            assertThrows(UserNotFoundException.class, () -> userController.authenticatedUser());
        }

        @Test
        void authenticatedUser_principalNotUser_throwsException() {
            when(userService.getCurrentUserDto()).thenThrow(new ClassCastException("Principal is not User"));

            assertThrows(ClassCastException.class, () -> userController.authenticatedUser());
        }

        @Test
        void authenticatedUser_multipleConsecutiveRequests_returnsConsistentData() {
            when(userService.getCurrentUserDto()).thenReturn(testCurrentUserDto);

            ResponseEntity<CurrentUserDto> response1 = userController.authenticatedUser();
            ResponseEntity<CurrentUserDto> response2 = userController.authenticatedUser();

            assertEquals(response1.getBody().getUserId(), response2.getBody().getUserId());
            verify(userService, times(2)).getCurrentUserDto();
        }
    }

    @Nested
    @DisplayName("/users/ Tests (Admin Only)")
    class AllUsersTests {

        @Test
        void allUsers_adminRequestsAllUsers_returnsUserList() {
            User user1 = new User();
            user1.setUserId(UUID.randomUUID());
            User user2 = new User();
            user2.setUserId(UUID.randomUUID());
            User user3 = new User();
            user3.setUserId(UUID.randomUUID());

            List<User> users = Arrays.asList(user1, user2, user3);
            when(userService.allUsers()).thenReturn(users);

            ResponseEntity<List<User>> response = userController.allUsers();

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(3, response.getBody().size());
            verify(userService).allUsers();
        }

        @Test
        void allUsers_zeroUsersInDatabase_returnsEmptyList() {
            when(userService.allUsers()).thenReturn(Collections.emptyList());

            ResponseEntity<List<User>> response = userController.allUsers();

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().isEmpty());
        }

        @Test
        void allUsers_exactlyOneUser_returnsSingleUserList() {
            when(userService.allUsers()).thenReturn(Collections.singletonList(testUser));

            ResponseEntity<List<User>> response = userController.allUsers();

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(1, response.getBody().size());
            assertEquals(testUserId, response.getBody().get(0).getUserId());
        }

        @Test
        void allUsers_thousandsOfUsers_returnsAllUsers() {
            List<User> largeUserList = new ArrayList<>();
            for (int i = 0; i < 5000; i++) {
                User user = new User();
                user.setUserId(UUID.randomUUID());
                largeUserList.add(user);
            }
            when(userService.allUsers()).thenReturn(largeUserList);

            ResponseEntity<List<User>> response = userController.allUsers();

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(5000, response.getBody().size());
        }

        @Test
        void allUsers_databaseConnectionSlow_throwsException() {
            when(userService.allUsers())
                .thenThrow(new DataAccessResourceFailureException("Connection timeout"));

            assertThrows(DataAccessResourceFailureException.class, () -> userController.allUsers());
        }

        @Test
        void allUsers_usersWithNullFields_returnsUsersWithNulls() {
            User userWithNulls = new User();
            userWithNulls.setUserId(testUserId);
            userWithNulls.setFirstName(null);
            userWithNulls.setLastName(null);

            when(userService.allUsers()).thenReturn(Collections.singletonList(userWithNulls));

            ResponseEntity<List<User>> response = userController.allUsers();

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNull(response.getBody().get(0).getFirstName());
            assertNull(response.getBody().get(0).getLastName());
        }
    }

    @Nested
    @DisplayName("/users/{userId}/get-header Tests")
    class GetUserHeaderTests {

        @Test
        void getUserHeader_existingUserCompleteInformation_returnsHeaderProfileDto() {
            when(userService.getHeaderProfileDto(testUserId)).thenReturn(testHeaderProfileDto);

            ResponseEntity<HeaderProfileDto> response = userController.getUserHeader(testUserId);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(testUserId, response.getBody().getUserId());
            assertEquals(testUsername, response.getBody().getUsername());
            assertEquals("John", response.getBody().getFirstName());
            assertEquals("Doe", response.getBody().getLastName());
            assertEquals("https://example.com/profile.jpg", response.getBody().getProfileImageUrl());
            verify(userService).getHeaderProfileDto(testUserId);
        }

        @Test
        void getUserHeader_existingUserMinimalProfile_returnsHeaderProfileDto() {
            HeaderProfileDto minimalDto = HeaderProfileDto.builder()
                .userId(testUserId)
                .username(testUsername)
                .firstName(null)
                .lastName(null)
                .profileImageUrl(null)
                .build();
            when(userService.getHeaderProfileDto(testUserId)).thenReturn(minimalDto);

            ResponseEntity<HeaderProfileDto> response = userController.getUserHeader(testUserId);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNull(response.getBody().getFirstName());
            assertNull(response.getBody().getProfileImageUrl());
        }

        @Test
        void getUserHeader_newlyRegisteredUser_returnsHeaderProfileDto() {
            when(userService.getHeaderProfileDto(testUserId)).thenReturn(testHeaderProfileDto);

            ResponseEntity<HeaderProfileDto> response = userController.getUserHeader(testUserId);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
        }

        @Test
        void getUserHeader_userRequestsOwnHeader_returnsHeaderProfileDto() {
            when(userService.getHeaderProfileDto(testUserId)).thenReturn(testHeaderProfileDto);

            ResponseEntity<HeaderProfileDto> response = userController.getUserHeader(testUserId);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(testUserId, response.getBody().getUserId());
        }

        @Test
        void getUserHeader_userRequestsAnotherUserHeader_returnsHeaderProfileDto() {
            UUID otherUserId = UUID.randomUUID();
            HeaderProfileDto otherUserDto = HeaderProfileDto.builder()
                .userId(otherUserId)
                .username("otheruser")
                .firstName("Other")
                .lastName("User")
                .profileImageUrl("https://example.com/other.jpg")
                .build();
            when(userService.getHeaderProfileDto(otherUserId)).thenReturn(otherUserDto);

            ResponseEntity<HeaderProfileDto> response = userController.getUserHeader(otherUserId);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(otherUserId, response.getBody().getUserId());
            assertEquals("otheruser", response.getBody().getUsername());
        }

        @Test
        void getUserHeader_nonExistentUserId_throwsException() {
            UUID nonExistentId = UUID.randomUUID();
            when(userService.getHeaderProfileDto(nonExistentId))
                .thenThrow(new UserNotFoundException("User not found with id: " + nonExistentId));

            assertThrows(UserNotFoundException.class, 
                () -> userController.getUserHeader(nonExistentId));
        }

        @Test
        void getUserHeader_userDeletedOrDeactivated_throwsException() {
            when(userService.getHeaderProfileDto(testUserId))
                .thenThrow(new UserNotFoundException("User not found"));

            assertThrows(UserNotFoundException.class, 
                () -> userController.getUserHeader(testUserId));
        }

        @Test
        void getUserHeader_profileNotFound_throwsException() {
            when(userService.getHeaderProfileDto(testUserId))
                .thenThrow(new ProfileNotFoundException("Profile not found with userId: " + testUserId));

            assertThrows(ProfileNotFoundException.class, 
                () -> userController.getUserHeader(testUserId));
        }

        @Test
        void getUserHeader_nullUserId_throwsException() {
            when(userService.getHeaderProfileDto(null))
                .thenThrow(new UserNotFoundException("User not found with id: null"));

            assertThrows(UserNotFoundException.class, 
                () -> userController.getUserHeader(null));
        }

        @Test
        void getUserHeader_userWithNullProfileImage_returnsDtoWithNullImage() {
            HeaderProfileDto dtoWithNullImage = HeaderProfileDto.builder()
                .userId(testUserId)
                .username(testUsername)
                .firstName("John")
                .lastName("Doe")
                .profileImageUrl(null)
                .build();
            when(userService.getHeaderProfileDto(testUserId)).thenReturn(dtoWithNullImage);

            ResponseEntity<HeaderProfileDto> response = userController.getUserHeader(testUserId);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNull(response.getBody().getProfileImageUrl());
        }

        @Test
        void getUserHeader_databaseConnectionFailure_throwsException() {
            when(userService.getHeaderProfileDto(testUserId))
                .thenThrow(new DataAccessResourceFailureException("Connection failed"));

            assertThrows(DataAccessResourceFailureException.class,
                () -> userController.getUserHeader(testUserId));
        }

        @Test
        void getUserHeader_multipleRapidRequests_allSucceed() {
            when(userService.getHeaderProfileDto(testUserId)).thenReturn(testHeaderProfileDto);

            ResponseEntity<HeaderProfileDto> response1 = userController.getUserHeader(testUserId);
            ResponseEntity<HeaderProfileDto> response2 = userController.getUserHeader(testUserId);
            ResponseEntity<HeaderProfileDto> response3 = userController.getUserHeader(testUserId);

            assertEquals(HttpStatus.OK, response1.getStatusCode());
            assertEquals(HttpStatus.OK, response2.getStatusCode());
            assertEquals(HttpStatus.OK, response3.getStatusCode());
            verify(userService, times(3)).getHeaderProfileDto(testUserId);
        }
    }

    @Nested
    @DisplayName("/users/{userId}/get-username Tests")
    class GetUsernameTests {

        @Test
        void getUsername_existingUserTypicalUsername_returnsUsername() {
            when(userService.findUsername(testUserId)).thenReturn(testUsername);

            ResponseEntity<String> response = userController.getUsername(testUserId);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(testUsername, response.getBody());
            verify(userService).findUsername(testUserId);
        }

        @Test
        void getUsername_usernameWithSpecialCharacters_returnsUsername() {
            String specialUsername = "user_name-123";
            when(userService.findUsername(testUserId)).thenReturn(specialUsername);

            ResponseEntity<String> response = userController.getUsername(testUserId);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(specialUsername, response.getBody());
        }

        @Test
        void getUsername_userRequestsOwnUsername_returnsUsername() {
            when(userService.findUsername(testUserId)).thenReturn(testUsername);

            ResponseEntity<String> response = userController.getUsername(testUserId);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(testUsername, response.getBody());
        }

        @Test
        void getUsername_userRequestsAnotherUsername_returnsUsername() {
            UUID otherUserId = UUID.randomUUID();
            String otherUsername = "otheruser";
            when(userService.findUsername(otherUserId)).thenReturn(otherUsername);

            ResponseEntity<String> response = userController.getUsername(otherUserId);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(otherUsername, response.getBody());
        }

        @Test
        void getUsername_nonExistentUserId_throwsException() {
            UUID nonExistentId = UUID.randomUUID();
            when(userService.findUsername(nonExistentId))
                .thenThrow(new FailedToFetchUsernameException("Failed to fetch username from user id."));

            assertThrows(FailedToFetchUsernameException.class,
                () -> userController.getUsername(nonExistentId));
        }

        @Test
        void getUsername_deletedUser_throwsException() {
            when(userService.findUsername(testUserId))
                .thenThrow(new FailedToFetchUsernameException("Failed to fetch username from user id."));

            assertThrows(FailedToFetchUsernameException.class,
                () -> userController.getUsername(testUserId));
        }

        @Test
        void getUsername_deactivatedUser_throwsException() {
            when(userService.findUsername(testUserId))
                .thenThrow(new FailedToFetchUsernameException("Failed to fetch username from user id."));

            assertThrows(FailedToFetchUsernameException.class,
                () -> userController.getUsername(testUserId));
        }

        @Test
        void getUsername_nullUserId_throwsException() {
            when(userService.findUsername(null))
                .thenThrow(new InvalidCredentialsException("User id is null."));

            assertThrows(InvalidCredentialsException.class,
                () -> userController.getUsername(null));
        }

        @Test
        void getUsername_userWithNullUsername_throwsException() {
            when(userService.findUsername(testUserId))
                .thenThrow(new FailedToFetchUsernameException("Failed to fetch username from user id."));

            assertThrows(FailedToFetchUsernameException.class,
                () -> userController.getUsername(testUserId));
        }

        @Test
        void getUsername_userWithEmptyUsername_throwsException() {
            when(userService.findUsername(testUserId))
                .thenThrow(new FailedToFetchUsernameException("Failed to fetch username from user id."));

            assertThrows(FailedToFetchUsernameException.class,
                () -> userController.getUsername(testUserId));
        }

        @Test
        void getUsername_databaseConnectionFailure_throwsException() {
            when(userService.findUsername(testUserId))
                .thenThrow(new DataAccessResourceFailureException("Connection failed"));

            assertThrows(DataAccessResourceFailureException.class,
                () -> userController.getUsername(testUserId));
        }

        @Test
        void getUsername_multipleRapidRequests_allSucceed() {
            when(userService.findUsername(testUserId)).thenReturn(testUsername);

            ResponseEntity<String> response1 = userController.getUsername(testUserId);
            ResponseEntity<String> response2 = userController.getUsername(testUserId);
            ResponseEntity<String> response3 = userController.getUsername(testUserId);

            assertEquals(HttpStatus.OK, response1.getStatusCode());
            assertEquals(HttpStatus.OK, response2.getStatusCode());
            assertEquals(HttpStatus.OK, response3.getStatusCode());
            verify(userService, times(3)).findUsername(testUserId);
        }

        @Test
        void getUsername_veryLongUsername_returnsUsername() {
            String longUsername = "a".repeat(255);
            when(userService.findUsername(testUserId)).thenReturn(longUsername);

            ResponseEntity<String> response = userController.getUsername(testUserId);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(longUsername, response.getBody());
        }
    }

    @Nested
    @DisplayName("Cross-Cutting Edge Case Tests")
    class CrossCuttingTests {

        @Test
        void getUserHeader_thenGetUsername_sameUser_consistentData() {
            when(userService.getHeaderProfileDto(testUserId)).thenReturn(testHeaderProfileDto);
            when(userService.findUsername(testUserId)).thenReturn(testUsername);

            ResponseEntity<HeaderProfileDto> headerResponse = userController.getUserHeader(testUserId);
            ResponseEntity<String> usernameResponse = userController.getUsername(testUserId);

            assertEquals(headerResponse.getBody().getUsername(), usernameResponse.getBody());
        }

        @Test
        void allEndpoints_concurrentAccess_noDataCorruption() {
            when(userService.getCurrentUserDto()).thenReturn(testCurrentUserDto);
            when(userService.getHeaderProfileDto(testUserId)).thenReturn(testHeaderProfileDto);
            when(userService.findUsername(testUserId)).thenReturn(testUsername);
            when(userService.allUsers()).thenReturn(Collections.singletonList(testUser));

            ResponseEntity<CurrentUserDto> meResponse = userController.authenticatedUser();
            ResponseEntity<HeaderProfileDto> headerResponse = userController.getUserHeader(testUserId);
            ResponseEntity<String> usernameResponse = userController.getUsername(testUserId);
            ResponseEntity<List<User>> allUsersResponse = userController.allUsers();

            assertEquals(HttpStatus.OK, meResponse.getStatusCode());
            assertEquals(HttpStatus.OK, headerResponse.getStatusCode());
            assertEquals(HttpStatus.OK, usernameResponse.getStatusCode());
            assertEquals(HttpStatus.OK, allUsersResponse.getStatusCode());
        }

        @Test
        void allEndpoints_serviceThrowsRuntimeException_exceptionPropagates() {
            when(userService.getCurrentUserDto()).thenThrow(new RuntimeException("Unexpected error"));
            when(userService.getHeaderProfileDto(any())).thenThrow(new RuntimeException("Unexpected error"));
            when(userService.findUsername(any())).thenThrow(new RuntimeException("Unexpected error"));
            when(userService.allUsers()).thenThrow(new RuntimeException("Unexpected error"));

            assertThrows(RuntimeException.class, () -> userController.authenticatedUser());
            assertThrows(RuntimeException.class, () -> userController.getUserHeader(testUserId));
            assertThrows(RuntimeException.class, () -> userController.getUsername(testUserId));
            assertThrows(RuntimeException.class, () -> userController.allUsers());
        }

        @Test
        void getUserHeader_andGetUsername_bothFailForDeletedUser_consistent() {
            UUID deletedUserId = UUID.randomUUID();
            when(userService.getHeaderProfileDto(deletedUserId))
                .thenThrow(new UserNotFoundException("User not found"));
            when(userService.findUsername(deletedUserId))
                .thenThrow(new FailedToFetchUsernameException("Failed to fetch username"));

            assertThrows(UserNotFoundException.class,
                () -> userController.getUserHeader(deletedUserId));
            assertThrows(FailedToFetchUsernameException.class,
                () -> userController.getUsername(deletedUserId));
        }

        @Test
        void multipleEndpoints_verifyServiceInteractions_correctCalls() {
            when(userService.getCurrentUserDto()).thenReturn(testCurrentUserDto);
            when(userService.getHeaderProfileDto(testUserId)).thenReturn(testHeaderProfileDto);
            when(userService.findUsername(testUserId)).thenReturn(testUsername);

            userController.authenticatedUser();
            userController.getUserHeader(testUserId);
            userController.getUsername(testUserId);

            verify(userService).getCurrentUserDto();
            verify(userService).getHeaderProfileDto(testUserId);
            verify(userService).findUsername(testUserId);
        }
    }
}
