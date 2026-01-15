package com.unicycle.auth.service;

import com.unicycle.auth.entity.RefreshToken;
import com.unicycle.auth.entity.User;
import com.unicycle.auth.repository.RefreshTokenRepository;
import com.unicycle.exception.InvalidRefreshTokenException;
import com.unicycle.exception.InvalidCredentialsException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RefreshTokenService Tests")
class RefreshTokenServiceTest {
    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    private User mockUser;
    private UUID mockUserId;
    private UUID mockRefreshTokenId;
    private String mockTokenValue;
    private RefreshToken mockRefreshToken;

    @BeforeEach
    void setUp() {
        mockUserId = UUID.randomUUID();
        mockRefreshTokenId = UUID.randomUUID();
        mockTokenValue = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMn0.KMUFsIDTnFmyG3nMiGM6H9FNFUROf3wh7SmqJp-QV30";
        mockUser = mock(User.class);

        mockRefreshToken = RefreshToken.builder()
            .id(mockRefreshTokenId)
            .userId(mockUserId)
            .token(mockTokenValue)
            .isActive(true)
            .expiresAt(LocalDateTime.now().plusDays(30))
            .createdAt(LocalDateTime.now())
            .build();
    }

    @Nested
    class CreateRefreshTokenTests {
        @Test
        void createRefreshToken_success() {
            when(jwtService.generateRefreshToken(mockUser)).thenReturn(mockTokenValue);
            when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(mockRefreshToken);
            when(mockUser.getUserId()).thenReturn(mockUserId);

            RefreshToken result = refreshTokenService.createRefreshToken(mockUser);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(mockRefreshTokenId);
            assertThat(result.getToken()).isEqualTo(mockTokenValue);
            assertThat(result.getUserId()).isEqualTo(mockUserId);
            verify(refreshTokenRepository).deactivateAllByUserId(mockUserId);
            verify(refreshTokenRepository).save(any(RefreshToken.class));
        }

        @Test
        void createRefreshToken_nullUserDetails_throwsException() {
            assertThrows(NullPointerException.class, () -> {
                refreshTokenService.createRefreshToken(null);
            });
        }

        @Test
        void createRefreshToken_nonUserUserDetails_throwsException() {
            UserDetails nonUserDetails = mock(UserDetails.class);
            assertThrows(ClassCastException.class, () -> {
                refreshTokenService.createRefreshToken(nonUserDetails);
            });
        }

        @Test
        void createRefreshToken_userWithNullUserId_throwsException() {
            User userWithNullId = mock(User.class);
            when(userWithNullId.getUserId()).thenReturn(null);
            assertThrows(InvalidCredentialsException.class, () -> {
                refreshTokenService.createRefreshToken(userWithNullId);
            });
        }

        @Test
        void createRefreshToken_userWithNoExistingTokens_createsNewTokenDeactivatesNone() {
            when(jwtService.generateRefreshToken(mockUser)).thenReturn(mockTokenValue);
            when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(mockRefreshToken);
            when(mockUser.getUserId()).thenReturn(mockUserId);

            RefreshToken result = refreshTokenService.createRefreshToken(mockUser);

            assertThat(result).isNotNull();
            verify(refreshTokenRepository).deactivateAllByUserId(mockUserId);
        }

        @Test
        void createRefreshToken_userWithManyExistingTokens_deactivatesAllFirst() {
            when(mockUser.getUserId()).thenReturn(mockUserId);
            when(jwtService.generateRefreshToken(mockUser)).thenReturn(mockTokenValue);
            when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(mockRefreshToken);

            refreshTokenService.createRefreshToken(mockUser);

            verify(refreshTokenRepository, times(1)).deactivateAllByUserId(mockUserId);
            verify(refreshTokenRepository).save(any(RefreshToken.class));
        }

        @Test
        void createRefreshToken_jwtServiceFailure_throwsException() {
            when(mockUser.getUserId()).thenReturn(mockUserId);
            when(jwtService.generateRefreshToken(mockUser))
                .thenThrow(new RuntimeException("JWT generation failed."));
            assertThrows(RuntimeException.class, () -> 
                refreshTokenService.createRefreshToken(mockUser));
        }

        @Test
        void createRefreshToken_databaseSaveFailure_throwsException() {
            when(mockUser.getUserId()).thenReturn(mockUserId);
            when(jwtService.generateRefreshToken(mockUser)).thenReturn(mockTokenValue);
            when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenThrow(new RuntimeException("Database connection failed."));
            assertThrows(RuntimeException.class, () -> 
                refreshTokenService.createRefreshToken(mockUser));
        }

        @Test
        void createRefreshToken_validUser_setsExpirationTo30Days() {
            ArgumentCaptor<RefreshToken> tokenCaptor = ArgumentCaptor.forClass(RefreshToken.class);
            when(mockUser.getUserId()).thenReturn(mockUserId);
            when(jwtService.generateRefreshToken(mockUser)).thenReturn(mockTokenValue);
            when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(mockRefreshToken);

            refreshTokenService.createRefreshToken(mockUser);

            verify(refreshTokenRepository).save(tokenCaptor.capture());
            RefreshToken capturedToken = tokenCaptor.getValue();
            
            LocalDateTime expectedExpiration = LocalDateTime.now().plusDays(30);
            assertThat(capturedToken.getExpiresAt())
                .isAfter(expectedExpiration.minusMinutes(1))
                .isBefore(expectedExpiration.plusMinutes(1));
        }
    }

    @Nested
    @DisplayName("findByToken Tests")
    class FindByTokenTests {
        @Test
        void findByToken_success() {
            when(refreshTokenRepository.findByTokenAndIsActive(mockTokenValue, true))
                .thenReturn(Optional.of(mockRefreshToken));
            Optional<RefreshToken> result = refreshTokenService.findByToken(mockTokenValue);
            assertThat(result).isPresent();
            assertThat(result.get().getToken()).isEqualTo(mockTokenValue);
        }

        @Test
        void findByToken_validInactiveToken_returnsEmpty() {
            when(refreshTokenRepository.findByTokenAndIsActive(mockTokenValue, true))
                .thenReturn(Optional.empty());
            assertThrows(NoSuchElementException.class, () -> 
                refreshTokenService.findByToken(mockTokenValue));
        }

        @Test
        void findByToken_expiredButActiveToken_throwsException() {
            RefreshToken expiredToken = RefreshToken.builder()
                .id(mockRefreshTokenId)
                .userId(mockUserId)
                .token(mockTokenValue)
                .isActive(true)
                .expiresAt(LocalDateTime.now().minusDays(1))
                .build();
            when(refreshTokenRepository.findByTokenAndIsActive(mockTokenValue, true))
                .thenReturn(Optional.of(expiredToken));
            assertThrows(InvalidRefreshTokenException.class, () ->
                refreshTokenService.findByToken(mockTokenValue));
        }

        @Test
        void findByToken_nonExistentToken_throwsException() {
            String fakeToken = "fake.token.value";
            assertThrows(InvalidCredentialsException.class, () -> 
                refreshTokenService.findByToken(fakeToken));
        }

        @Test
        void findByToken_nullToken_throwsException() {
            assertThrows(InvalidCredentialsException.class, () -> 
                refreshTokenService.findByToken(null));
        }

        @Test
        void findByToken_emptyStringToken_throwsException() {
            assertThrows(InvalidCredentialsException.class, () -> 
                refreshTokenService.findByToken(""));
        }

        @Test
        void findByToken_malformedToken_throwsException() {
            String malformedToken = "not-a-valid-jwt-at-all";
            assertThrows(InvalidCredentialsException.class, () ->
                refreshTokenService.findByToken(malformedToken));
        }

        @Test
        void findByToken_sqlInjectionAttempt_throwsException() {
            String maliciousToken = "'; DROP TABLE refresh_tokens; --";
            assertThrows(InvalidCredentialsException.class, () ->
                refreshTokenService.findByToken(maliciousToken));
        }
    }

    @Nested
    class ValidateRefreshTokenTests {
        @Test
        void validateRefreshToken_validActiveUnexpiredToken_returnsTrue() {
            when(refreshTokenRepository.findByTokenAndIsActive(mockTokenValue, true))
                .thenReturn(Optional.of(mockRefreshToken));
            when(jwtService.isRefreshToken(mockTokenValue)).thenReturn(true);
            when(jwtService.isTokenValid(mockTokenValue, mockUserId)).thenReturn(true);
            boolean result = refreshTokenService.validateRefreshToken(mockTokenValue);
            assertThat(result).isTrue();
        }

        @Test
        void validateRefreshToken_tokenNotInDatabase_throwsException() {
            when(refreshTokenRepository.findByTokenAndIsActive(mockTokenValue, true))
                .thenReturn(Optional.empty());
            assertThrows(NoSuchElementException.class, () -> 
                refreshTokenService.validateRefreshToken(mockTokenValue));
        }

        @Test
        void validateRefreshToken_inactiveToken_throwsException() {
            RefreshToken inactiveToken = RefreshToken.builder()
                .id(mockRefreshTokenId)
                .userId(mockUserId)
                .token(mockTokenValue)
                .isActive(false)
                .expiresAt(LocalDateTime.now().plusDays(30))
                .build();
            when(refreshTokenRepository.findByTokenAndIsActive(mockTokenValue, true))
                .thenReturn(Optional.of(inactiveToken));
            assertThrows(InvalidRefreshTokenException.class, () -> 
                refreshTokenService.validateRefreshToken(mockTokenValue));
        }

        @Test
        void validateRefreshToken_expiredToken_throwsException() {
            RefreshToken expiredToken = RefreshToken.builder()
                .id(mockRefreshTokenId)
                .userId(mockUserId)
                .token(mockTokenValue)
                .isActive(true)
                .expiresAt(LocalDateTime.now().minusDays(1))
                .build();
            when(refreshTokenRepository.findByTokenAndIsActive(mockTokenValue, true))
                .thenReturn(Optional.of(expiredToken));
            assertThrows(InvalidRefreshTokenException.class, () -> 
                refreshTokenService.validateRefreshToken(mockTokenValue));
        }

        @Test
        void validateRefreshToken_invalidJwtSignature_returnsFalse() {
            when(refreshTokenRepository.findByTokenAndIsActive(mockTokenValue, true))
                .thenReturn(Optional.of(mockRefreshToken));
            when(jwtService.isRefreshToken(mockTokenValue)).thenReturn(true);
            when(jwtService.isTokenValid(mockTokenValue, mockUserId)).thenReturn(false);
            boolean result = refreshTokenService.validateRefreshToken(mockTokenValue);
            assertThat(result).isFalse();
        }

        @Test
        void validateRefreshToken_accessTokenInstead_returnsFalse() {
            when(refreshTokenRepository.findByTokenAndIsActive(mockTokenValue, true))
                .thenReturn(Optional.of(mockRefreshToken));
            when(jwtService.isRefreshToken(mockTokenValue)).thenReturn(false); 
            boolean result = refreshTokenService.validateRefreshToken(mockTokenValue);
            assertThat(result).isFalse();
        }

        @Test
        void validateRefreshToken_userIdMismatch_returnsFalse() {
            when(refreshTokenRepository.findByTokenAndIsActive(mockTokenValue, true))
                .thenReturn(Optional.of(mockRefreshToken));
            when(jwtService.isRefreshToken(mockTokenValue)).thenReturn(true);
            when(jwtService.isTokenValid(mockTokenValue, mockUserId)).thenReturn(false);
            boolean result = refreshTokenService.validateRefreshToken(mockTokenValue);
            assertThat(result).isFalse();
        }

        @Test
        void validateRefreshToken_nullToken_throwsException() {
            assertThrows(InvalidCredentialsException.class, () -> 
                refreshTokenService.validateRefreshToken(null));
        }

        @Test
        void validateRefreshToken_emptyStringToken_throwsException() {
            assertThrows(InvalidCredentialsException.class, () -> 
                refreshTokenService.validateRefreshToken(null));
        }

        @Test
        void validateRefreshToken_deletedUser_returnsFalse() {
            when(refreshTokenRepository.findByTokenAndIsActive(mockTokenValue, true))
                .thenReturn(Optional.of(mockRefreshToken));
            when(jwtService.isRefreshToken(mockTokenValue)).thenReturn(true);
            when(jwtService.isTokenValid(mockTokenValue, mockUserId)).thenReturn(false);
            boolean result = refreshTokenService.validateRefreshToken(mockTokenValue);
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("revokeToken Tests")
    class RevokeTokenTests {
        @Test
        void revokeToken_validToken_callsRepositoryWithCorrectToken() {
            refreshTokenService.revokeToken(mockTokenValue);
            verify(refreshTokenRepository).deactivateByToken(mockTokenValue);
        }

        @Test
        void revokeToken_multipleTokens_callsRepositoryForEach() {
            String token1 = "token.one.value";
            String token2 = "token.two.value";
            String token3 = "token.three.value";

            refreshTokenService.revokeToken(token1);
            refreshTokenService.revokeToken(token2);
            refreshTokenService.revokeToken(token3);

            verify(refreshTokenRepository).deactivateByToken(token1);
            verify(refreshTokenRepository).deactivateByToken(token2);
            verify(refreshTokenRepository).deactivateByToken(token3);
        }

        @Test
        void revokeToken_sameTokenMultipleTimes_callsRepositoryEachTime() {
            refreshTokenService.revokeToken(mockTokenValue);
            refreshTokenService.revokeToken(mockTokenValue);
            verify(refreshTokenRepository, times(2)).deactivateByToken(mockTokenValue);
        }

        @Test
        void revokeToken_nullToken_passesNullToRepository() {
            refreshTokenService.revokeToken(null);
            verify(refreshTokenRepository).deactivateByToken(null);
        }

        @Test
        void revokeToken_emptyStringToken_passesEmptyStringToRepository() {
            refreshTokenService.revokeToken("");
            verify(refreshTokenRepository).deactivateByToken("");
        }

        @Test
        void revokeToken_repositoryThrowsException_propagatesException() {
            doThrow(new RuntimeException("Database error."))
                .when(refreshTokenRepository).deactivateByToken(mockTokenValue);
            assertThatThrownBy(() -> refreshTokenService.revokeToken(mockTokenValue))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Database error.");
        }
    }

    @Nested
    @DisplayName("revokeAllUserTokens Tests")
    class RevokeAllUserTokensTests {
        @Test
        void revokeAllUserTokens_validUserId_callsRepositoryWithCorrectUserId() {
            refreshTokenService.revokeAllUserTokens(mockUserId);
            verify(refreshTokenRepository).deactivateAllByUserId(mockUserId);
        }

        @Test
        void revokeAllUserTokens_differentUserIds_callsRepositoryWithEachUserId() {
            UUID userId1 = UUID.randomUUID();
            UUID userId2 = UUID.randomUUID();
            UUID userId3 = UUID.randomUUID();

            refreshTokenService.revokeAllUserTokens(userId1);
            refreshTokenService.revokeAllUserTokens(userId2);
            refreshTokenService.revokeAllUserTokens(userId3);

            verify(refreshTokenRepository).deactivateAllByUserId(userId1);
            verify(refreshTokenRepository).deactivateAllByUserId(userId2);
            verify(refreshTokenRepository).deactivateAllByUserId(userId3);
        }

       @Test
        void revokeAllUserTokens_sameUserIdMultipleTimes_callsRepositoryEachTime() {
            refreshTokenService.revokeAllUserTokens(mockUserId);
            refreshTokenService.revokeAllUserTokens(mockUserId);

            verify(refreshTokenRepository, times(2)).deactivateAllByUserId(mockUserId);
        }

        @Test
        void revokeAllUserTokens_nullUserId_passesNullToRepository() {
            refreshTokenService.revokeAllUserTokens(null);
            verify(refreshTokenRepository).deactivateAllByUserId(null);
        }

       @Test
        void revokeAllUserTokens_repositoryThrowsException_propagatesException() {
            doThrow(new RuntimeException("Database connection failed"))
                .when(refreshTokenRepository).deactivateAllByUserId(mockUserId);
            assertThatThrownBy(() -> refreshTokenService.revokeAllUserTokens(mockUserId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Database connection failed");
        }

        @Test
        void revokeAllUserTokens_afterCreatingToken_callsDeactivateTwice() {
            when(jwtService.generateRefreshToken(mockUser)).thenReturn(mockTokenValue);
            when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(mockRefreshToken);
            when(mockUser.getUserId()).thenReturn(mockUserId);
            refreshTokenService.createRefreshToken(mockUser);
            refreshTokenService.revokeAllUserTokens(mockUserId);
            verify(refreshTokenRepository, times(2)).deactivateAllByUserId(mockUserId);
        }
    }

    @Nested
    @DisplayName("cleanupExpiredTokens Tests")
    class CleanupExpiredTokensTests {
        @Test
        void cleanupExpiredTokens_success() {
            ArgumentCaptor<LocalDateTime> timeCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
            LocalDateTime before = LocalDateTime.now();

            refreshTokenService.cleanupExpiredTokens();

            verify(refreshTokenRepository).deleteExpiredTokens(timeCaptor.capture());
            LocalDateTime captured = timeCaptor.getValue();
            LocalDateTime after = LocalDateTime.now();

            assertThat(captured).isAfterOrEqualTo(before);
            assertThat(captured).isBeforeOrEqualTo(after);
        }

        @Test
        void cleanupExpiredTokens_calledMultipleTimes_passesUpdatedTimestamps() {
            ArgumentCaptor<LocalDateTime> timeCaptor = ArgumentCaptor.forClass(LocalDateTime.class);

            refreshTokenService.cleanupExpiredTokens();
            refreshTokenService.cleanupExpiredTokens();

            verify(refreshTokenRepository, times(2)).deleteExpiredTokens(timeCaptor.capture());
            assertThat(timeCaptor.getAllValues()).hasSize(2);
        }

        @Test
        void cleanupExpiredTokens_repositoryThrowsException_propagatesException() {
            doThrow(new RuntimeException("Database unavailable."))
                .when(refreshTokenRepository).deleteExpiredTokens(any(LocalDateTime.class));
            assertThatThrownBy(() -> refreshTokenService.cleanupExpiredTokens())
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Database unavailable.");
        }

        @Test
        void cleanupExpiredTokens_repositoryThrowsDataAccessException_propagatesException() {
            doThrow(new RuntimeException("Connection timeout."))
                .when(refreshTokenRepository).deleteExpiredTokens(any(LocalDateTime.class));
            assertThatThrownBy(() -> refreshTokenService.cleanupExpiredTokens())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Connection timeout.");
        }
    }

    @Nested
    @DisplayName("Integration Tests / Edge Cases")
    class CrossCuttingTests {
        @Test
        void createAndValidate_fullFlow_worksCorrectly() {
            when(jwtService.generateRefreshToken(mockUser)).thenReturn(mockTokenValue);
            when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(mockRefreshToken);
            when(refreshTokenRepository.findByTokenAndIsActive(mockTokenValue, true))
                .thenReturn(Optional.of(mockRefreshToken));
            when(jwtService.isRefreshToken(mockTokenValue)).thenReturn(true);
            when(jwtService.isTokenValid(mockTokenValue, mockUserId)).thenReturn(true);
            when(mockUser.getUserId()).thenReturn(mockUserId);

            RefreshToken created = refreshTokenService.createRefreshToken(mockUser);
            boolean isValid = refreshTokenService.validateRefreshToken(created.getToken());
            assertThat(isValid).isTrue();
        }

        @Test
        void createRevokeValidate_flow_throwsException() {
            when(jwtService.generateRefreshToken(mockUser)).thenReturn(mockTokenValue);
            when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(mockRefreshToken);
            when(refreshTokenRepository.findByTokenAndIsActive(mockTokenValue, true))
                .thenReturn(Optional.empty());
            when(mockUser.getUserId()).thenReturn(mockUserId);

            RefreshToken created = refreshTokenService.createRefreshToken(mockUser);
            refreshTokenService.revokeToken(created.getToken());
            assertThrows(NoSuchElementException.class, () -> 
                refreshTokenService.validateRefreshToken(created.getToken()));
        }

        @Test
        void multipleCreates_sequentially_deactivatesPrevious() {
            when(jwtService.generateRefreshToken(mockUser)).thenReturn(mockTokenValue);
            when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(mockRefreshToken);
            when(mockUser.getUserId()).thenReturn(mockUserId);

            refreshTokenService.createRefreshToken(mockUser);
            refreshTokenService.createRefreshToken(mockUser);
            refreshTokenService.createRefreshToken(mockUser);
            verify(refreshTokenRepository, times(3)).deactivateAllByUserId(mockUserId);
        }

        @Test
        void createRefreshToken_veryLongToken_handled() {
            String longToken = "a".repeat(10000); 
            when(jwtService.generateRefreshToken(mockUser)).thenReturn(longToken);
            
            RefreshToken longRefreshToken = RefreshToken.builder()
                .id(mockRefreshTokenId)
                .userId(mockUserId)
                .token(longToken)
                .isActive(true)
                .expiresAt(LocalDateTime.now().plusDays(30))
                .build();
            when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(longRefreshToken);
            when(mockUser.getUserId()).thenReturn(mockUserId);

            RefreshToken result = refreshTokenService.createRefreshToken(mockUser);
            assertThat(result.getToken()).hasSize(10000);
        }
    }
}