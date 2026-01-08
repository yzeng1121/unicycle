package com.unicycle.auth.service;

import com.unicycle.auth.entity.User;
import com.unicycle.auth.repository.UserRepository;
import com.unicycle.exception.FailedToFetchUserIdException;
import com.unicycle.exception.FailedToFetchUsernameException;
import com.unicycle.exception.InvalidCredentialsException;
import com.unicycle.exception.UserNotFoundException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtUserDetailsService Tests")
class JwtUserDetailsServiceTest {

    @Mock
    private UserRepository mockUserRepository;

    @InjectMocks
    private JwtUserDetailsService jwtUserDetailsService;

    private static User mockUser = User.builder()
        .userId(UUID.fromString("550e8400-e29b-41d4-a716-446655440001"))
        .username("jdoe")
        .email("john.doe@tufts.edu")
        .password("password123")
        .firstName("John")
        .lastName("Doe")
        .dorm("Harleston Hall")
        .enabled(true)
        .build();

    @Nested
    @DisplayName("loadUserByUsername Tests")
    class LoadUserByUsernameTests {

        @Test
        void loadUserByUsername_success() {
            when(mockUserRepository.findByUsername(mockUser.getUsername())).thenReturn(Optional.of(mockUser));
            UserDetails result = jwtUserDetailsService.loadUserByUsername(mockUser.getUsername());
            assertNotNull(result);
            assertEquals(mockUser.getUsername(), result.getUsername());
            verify(mockUserRepository, times(1)).findByUsername(mockUser.getUsername());
        }

        @Test
        void loadUserByUsername_validUserNotExists_throwsException() {
            when(mockUserRepository.findByUsername("no.body")).thenReturn(Optional.empty());
            assertThrows(
                UsernameNotFoundException.class,
                () -> jwtUserDetailsService.loadUserByUsername("no.body")
            );
        }

        @Test
        void loadUserByUsername_nullUsername_throwsException() {
            assertThrows(
                InvalidCredentialsException.class,
                () -> jwtUserDetailsService.loadUserByUsername(null)
            );
        }

        @Test
        void loadUserByUsername_emptyString_throwsException() {
            assertThrows(
                InvalidCredentialsException.class,
                () -> jwtUserDetailsService.loadUserByUsername("")
            );
        }

        @Test
        void loadUserByUsername_whitespaceOnly_throwsException() {
            assertThrows(
                InvalidCredentialsException.class,
                () -> jwtUserDetailsService.loadUserByUsername("   ")
            );
        }

        @Test
        void loadUserByUsername_usernameWithSpaces_success() {
            String usernameWithSpaces = " jdoe         ";
            when(mockUserRepository.findByUsername(anyString())).thenReturn(Optional.of(mockUser));
            UserDetails result = jwtUserDetailsService.loadUserByUsername(usernameWithSpaces);
            assertNotNull(result);
            assertEquals(mockUser.getUsername(), result.getUsername());
            verify(mockUserRepository, times(1)).findByUsername(mockUser.getUsername());
        }

        @Test
        void loadUserByUsername_uppercaseUsername_success() {
            String uppercaseUsername = "JDOE";
            when(mockUserRepository.findByUsername(anyString())).thenReturn(Optional.of(mockUser));
            UserDetails result = jwtUserDetailsService.loadUserByUsername(uppercaseUsername);
            assertNotNull(result);
            assertEquals(mockUser.getUsername(), result.getUsername());
            verify(mockUserRepository, times(1)).findByUsername(mockUser.getUsername());
        }

        @Test
        void loadUserByUsername_mixedCaseEmail_success() {
            String mixedCaseUsername = "jDOe";
            when(mockUserRepository.findByUsername(anyString())).thenReturn(Optional.of(mockUser));
            UserDetails result = jwtUserDetailsService.loadUserByUsername(mixedCaseUsername);
            assertNotNull(result);
            assertEquals(mockUser.getUsername(), result.getUsername());
            verify(mockUserRepository, times(1)).findByUsername(mockUser.getUsername());
        }

        @Test
        void loadUserByUsername_veryLongString_throwsException() {
            assertThrows(
                InvalidCredentialsException.class,
                () -> jwtUserDetailsService.loadUserByUsername("a".repeat(10000))
            );
        }

        @Test
        @DisplayName("SQL injection attempt is safely handled")
        void loadUserByUsername_sqlInjectionAttempt_throwsException() {
            assertThrows(
                InvalidCredentialsException.class,
                () -> jwtUserDetailsService.loadUserByUsername("'; DROP TABLE users;--")
            );
        }

        @Test
        void loadUserByUsername_emailWithPlus_throwsException() {
            assertThrows(
                InvalidCredentialsException.class,
                () -> jwtUserDetailsService.loadUserByUsername("john+doe")
            );
        }

        @Test
        void loadUserByUsername_unicodeEmail_throwsException() {
            String unicodeEmail = "用户jdoe";
            assertThrows(
                InvalidCredentialsException.class,
                () -> jwtUserDetailsService.loadUserByUsername(unicodeEmail)
            );
        }

        @Test
        void loadUserByUsername_UUIDLikeString_throwsException() {
            assertThrows(
                InvalidCredentialsException.class,
                () -> jwtUserDetailsService.loadUserByUsername("550e8400-e29b-41d4-a716-446655440000")
            );
        }

        @Test
        void loadUserByUsername_databaseException_throwsException() {
            when(mockUserRepository.findByUsername(anyString()))
                .thenThrow(new FailedToFetchUsernameException("Database connection failed"));

            assertThrows(
                FailedToFetchUsernameException.class,
                () -> jwtUserDetailsService.loadUserByUsername(mockUser.getUsername())
            );
        }
    }

    @Nested
    @DisplayName("loadUserByUserId Tests")
    class LoadUserByUserIdTests {

        @Test
        void loadUserByUserId_validUUIDUserExists_success() {
            when(mockUserRepository.findByUserId(mockUser.getUserId())).thenReturn(Optional.of(mockUser));
            UserDetails result = jwtUserDetailsService.loadUserByUserId(mockUser.getUserId());
            assertNotNull(result);
            verify(mockUserRepository, times(1)).findByUserId(mockUser.getUserId());
        }

        @Test
        void loadUserByUserId_validUUIDUserNotExists_throwsException() {
            when(mockUserRepository.findByUserId(mockUser.getUserId())).thenReturn(Optional.empty());
            assertThrows(
                UserNotFoundException.class,
                () -> jwtUserDetailsService.loadUserByUserId(mockUser.getUserId())
            );
        }

        @Test
        void loadUserByUserId_nullUuid_throwsException() {
            assertThrows(
                InvalidCredentialsException.class,
                () -> jwtUserDetailsService.loadUserByUserId(null)
            );
        }

        @Test
        void loadUserByUserId_allZerosUuid_throwsException() {
            UUID zeroUUID = UUID.fromString("00000000-0000-0000-0000-000000000000");
            when(mockUserRepository.findByUserId(zeroUUID)).thenReturn(Optional.empty());

            assertThrows(
                UserNotFoundException.class,
                () -> jwtUserDetailsService.loadUserByUserId(zeroUUID)
            );

            verify(mockUserRepository, times(1)).findByUserId(zeroUUID);
        }

        @Test
        void loadUserByUserId_maxValueUUID_throwsException() {
            UUID maxUuid = UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff");
            when(mockUserRepository.findByUserId(maxUuid)).thenReturn(Optional.empty());

            assertThrows(
                UserNotFoundException.class,
                () -> jwtUserDetailsService.loadUserByUserId(maxUuid)
            );

            verify(mockUserRepository, times(1)).findByUserId(maxUuid);
        }

        @Test
        void loadUserByUserId_deletedUserUUID_throwsException() {
            UUID deletedUserId = UUID.randomUUID();
            when(mockUserRepository.findByUserId(deletedUserId)).thenReturn(Optional.empty());

            assertThrows(
                UserNotFoundException.class,
                () -> jwtUserDetailsService.loadUserByUserId(deletedUserId)
            );

            verify(mockUserRepository, times(1)).findByUserId(deletedUserId);
        }

        @Test
        void loadUserByUserId_databaseException_throwsException() {
            when(mockUserRepository.findByUserId(any(UUID.class)))
                .thenThrow(new FailedToFetchUserIdException("Database connection failed."));

            assertThrows(
                FailedToFetchUserIdException.class,
                () -> jwtUserDetailsService.loadUserByUserId(mockUser.getUserId())
            );
        }

        @Test
        void loadUserByUserId_multipleCalls_queriesRepositoryEachTime() {
            UUID userId1 = UUID.randomUUID();
            UUID userId2 = UUID.randomUUID();
            
            when(mockUserRepository.findByUserId(userId1)).thenReturn(Optional.of(mockUser));
            when(mockUserRepository.findByUserId(userId2)).thenReturn(Optional.empty());

            assertNotNull(jwtUserDetailsService.loadUserByUserId(userId1));
            assertThrows(
                UserNotFoundException.class,
                () -> jwtUserDetailsService.loadUserByUserId(userId2)
            );

            verify(mockUserRepository, times(1)).findByUserId(userId1);
            verify(mockUserRepository, times(1)).findByUserId(userId2);
        }
    }

    @Nested
    @DisplayName("General Behavior Tests")
    class GeneralBehaviorTests {

        @Test
        void service_usernameLookup_delegatesToRepository() {
            when(mockUserRepository.findByUsername(mockUser.getUsername())).thenReturn(Optional.of(mockUser));
            jwtUserDetailsService.loadUserByUsername(mockUser.getUsername());
            verify(mockUserRepository).findByUsername(mockUser.getUsername());
            verifyNoMoreInteractions(mockUserRepository);
        }

        @Test
        void service_userIdLookup_delegatesToRepository() {
            when(mockUserRepository.findByUserId(mockUser.getUserId())).thenReturn(Optional.of(mockUser));
            jwtUserDetailsService.loadUserByUserId(mockUser.getUserId());
            verify(mockUserRepository).findByUserId(mockUser.getUserId());
            verifyNoMoreInteractions(mockUserRepository);
        }

        @Test
        void service_exceptionMessage_containsIdentifier() {
            when(mockUserRepository.findByUsername(mockUser.getUsername())).thenReturn(Optional.empty());
            when(mockUserRepository.findByUserId(mockUser.getUserId())).thenReturn(Optional.empty());

            UsernameNotFoundException emailException = assertThrows(
                UsernameNotFoundException.class,
                () -> jwtUserDetailsService.loadUserByUsername(mockUser.getUsername())
            );
            
            UserNotFoundException userIdException = assertThrows(
                UserNotFoundException.class,
                () -> jwtUserDetailsService.loadUserByUserId(mockUser.getUserId())
            );

            assertTrue(emailException.getMessage().contains(mockUser.getUsername()));
            assertTrue(userIdException.getMessage().contains(mockUser.getUserId().toString()));
        }
    }
}