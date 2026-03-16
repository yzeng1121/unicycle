package com.unicycle.auth.repository;

import com.unicycle.auth.entity.RefreshToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("RefreshToken Repository Tests")
class RefreshTokenRepositoryTest {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    private UUID testUserId;
    private String testToken;
    private RefreshToken activeToken;
    private RefreshToken inactiveToken;

    @BeforeEach
    void setUp() {
        refreshTokenRepository.deleteAll();
        
        testUserId = UUID.randomUUID();
        testToken = "test-refresh-token-" + UUID.randomUUID();
        
        // Create active token
        activeToken = RefreshToken.builder()
                .userId(testUserId)
                .token(testToken)
                .isActive(true)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();
        
        // Create inactive token
        inactiveToken = RefreshToken.builder()
                .userId(testUserId)
                .token("inactive-token-" + UUID.randomUUID())
                .isActive(false)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();
    }

    @Test
    @DisplayName("Should find active token by token string and active status")
    void findByTokenAndIsActive_WhenTokenExistsAndActive_ReturnsToken() {
        // Given
        refreshTokenRepository.save(activeToken);
        
        // When
        Optional<RefreshToken> result = refreshTokenRepository.findByTokenAndIsActive(testToken, true);
        
        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getToken()).isEqualTo(testToken);
        assertThat(result.get().isActive()).isTrue();
    }

    @Test
    @DisplayName("Should not find inactive token when searching for active")
    void findByTokenAndIsActive_WhenTokenInactive_ReturnsEmpty() {
        // Given
        refreshTokenRepository.save(inactiveToken);
        
        // When
        Optional<RefreshToken> result = refreshTokenRepository.findByTokenAndIsActive(inactiveToken.getToken(), true);
        
        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should not find token when token does not exist")
    void findByTokenAndIsActive_WhenTokenDoesNotExist_ReturnsEmpty() {
        // When
        Optional<RefreshToken> result = refreshTokenRepository.findByTokenAndIsActive("non-existent-token", true);
        
        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should find all active tokens for a user")
    void findByUserIdAndIsActive_WhenMultipleActiveTokens_ReturnsAllActive() {
        // Given
        RefreshToken token1 = RefreshToken.builder()
                .userId(testUserId)
                .token("token1")
                .isActive(true)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();
        
        RefreshToken token2 = RefreshToken.builder()
                .userId(testUserId)
                .token("token2")
                .isActive(true)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();
        
        RefreshToken token3 = RefreshToken.builder()
                .userId(testUserId)
                .token("token3")
                .isActive(false)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();
        
        refreshTokenRepository.saveAll(List.of(token1, token2, token3));
        
        // When
        List<RefreshToken> activeTokens = refreshTokenRepository.findByUserIdAndIsActive(testUserId, true);
        
        // Then
        assertThat(activeTokens).hasSize(2);
        assertThat(activeTokens).extracting(RefreshToken::getToken)
                .containsExactlyInAnyOrder("token1", "token2");
    }

    @Test
    @DisplayName("Should find all inactive tokens for a user")
    void findByUserIdAndIsActive_WhenSearchingInactive_ReturnsOnlyInactive() {
        // Given
        refreshTokenRepository.saveAll(List.of(activeToken, inactiveToken));
        
        // When
        List<RefreshToken> inactiveTokens = refreshTokenRepository.findByUserIdAndIsActive(testUserId, false);
        
        // Then
        assertThat(inactiveTokens).hasSize(1);
        assertThat(inactiveTokens.get(0).isActive()).isFalse();
    }

    @Test
    @DisplayName("Should return empty list when user has no tokens")
    void findByUserIdAndIsActive_WhenUserHasNoTokens_ReturnsEmptyList() {
        // Given
        UUID nonExistentUserId = UUID.randomUUID();
        
        // When
        List<RefreshToken> tokens = refreshTokenRepository.findByUserIdAndIsActive(nonExistentUserId, true);
        
        // Then
        assertThat(tokens).isEmpty();
    }

    @Test
    @DisplayName("Should deactivate all tokens for a user")
    void deactivateAllByUserId_WhenUserHasMultipleTokens_DeactivatesAll() {
        // Given
        RefreshToken token1 = RefreshToken.builder()
                .userId(testUserId)
                .token("token1")
                .isActive(true)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();
        
        RefreshToken token2 = RefreshToken.builder()
                .userId(testUserId)
                .token("token2")
                .isActive(true)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();
        
        refreshTokenRepository.saveAll(List.of(token1, token2));
        
        // When
        refreshTokenRepository.deactivateAllByUserId(testUserId);
        refreshTokenRepository.flush();
        
        // Then
        List<RefreshToken> tokens = refreshTokenRepository.findByUserIdAndIsActive(testUserId, true);
        assertThat(tokens).isEmpty();
        
        List<RefreshToken> deactivatedTokens = refreshTokenRepository.findByUserIdAndIsActive(testUserId, false);
        assertThat(deactivatedTokens).hasSize(2);
    }

    @Test
    @DisplayName("Should not affect tokens of other users when deactivating")
    void deactivateAllByUserId_WhenDeactivatingOneUser_DoesNotAffectOtherUsers() {
        // Given
        UUID otherUserId = UUID.randomUUID();
        
        RefreshToken userToken = RefreshToken.builder()
                .userId(testUserId)
                .token("user-token")
                .isActive(true)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();
        
        RefreshToken otherUserToken = RefreshToken.builder()
                .userId(otherUserId)
                .token("other-user-token")
                .isActive(true)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();
        
        refreshTokenRepository.saveAll(List.of(userToken, otherUserToken));
        
        // When
        refreshTokenRepository.deactivateAllByUserId(testUserId);
        refreshTokenRepository.flush();
        
        // Then
        List<RefreshToken> otherUserActiveTokens = refreshTokenRepository.findByUserIdAndIsActive(otherUserId, true);
        assertThat(otherUserActiveTokens).hasSize(1);
        assertThat(otherUserActiveTokens.get(0).getToken()).isEqualTo("other-user-token");
    }

    @Test
    @DisplayName("Should deactivate specific token by token string")
    void deactivateByToken_WhenTokenExists_DeactivatesToken() {
        // Given
        refreshTokenRepository.save(activeToken);
        
        // When
        refreshTokenRepository.deactivateByToken(testToken);
        refreshTokenRepository.flush();
        
        // Then
        Optional<RefreshToken> result = refreshTokenRepository.findByTokenAndIsActive(testToken, true);
        assertThat(result).isEmpty();
        
        Optional<RefreshToken> deactivated = refreshTokenRepository.findByTokenAndIsActive(testToken, false);
        assertThat(deactivated).isPresent();
    }

    @Test
    @DisplayName("Should not throw exception when deactivating non-existent token")
    void deactivateByToken_WhenTokenDoesNotExist_DoesNotThrowException() {
        // When/Then
        refreshTokenRepository.deactivateByToken("non-existent-token");
        refreshTokenRepository.flush();
        // No exception should be thrown
    }

    @Test
    @DisplayName("Should delete expired tokens")
    void deleteExpiredTokens_WhenTokensExpired_DeletesExpiredOnly() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        
        RefreshToken expiredToken1 = RefreshToken.builder()
                .userId(testUserId)
                .token("expired1")
                .isActive(true)
                .expiresAt(now.minusDays(1))
                .build();
        
        RefreshToken expiredToken2 = RefreshToken.builder()
                .userId(testUserId)
                .token("expired2")
                .isActive(true)
                .expiresAt(now.minusHours(1))
                .build();
        
        RefreshToken validToken = RefreshToken.builder()
                .userId(testUserId)
                .token("valid")
                .isActive(true)
                .expiresAt(now.plusDays(7))
                .build();
        
        refreshTokenRepository.saveAll(List.of(expiredToken1, expiredToken2, validToken));
        
        // When
        refreshTokenRepository.deleteExpiredTokens(now);
        refreshTokenRepository.flush();
        
        // Then
        List<RefreshToken> remainingTokens = refreshTokenRepository.findAll();
        assertThat(remainingTokens).hasSize(1);
        assertThat(remainingTokens.get(0).getToken()).isEqualTo("valid");
    }

    @Test
    @DisplayName("Should not delete tokens that expire exactly at cutoff time")
    void deleteExpiredTokens_WhenTokenExpiresAtCutoff_DoesNotDelete() {
        // Given
        LocalDateTime cutoffTime = LocalDateTime.now();
        
        RefreshToken tokenExpiresAtCutoff = RefreshToken.builder()
                .userId(testUserId)
                .token("expires-at-cutoff")
                .isActive(true)
                .expiresAt(cutoffTime)
                .build();
        
        refreshTokenRepository.save(tokenExpiresAtCutoff);
        
        // When
        refreshTokenRepository.deleteExpiredTokens(cutoffTime);
        refreshTokenRepository.flush();
        
        // Then
        Optional<RefreshToken> result = refreshTokenRepository.findById(tokenExpiresAtCutoff.getId());
        assertThat(result).isPresent();
    }

    @Test
    @DisplayName("Should delete all previous tokens for a user")
    void deletePreviousTokens_WhenUserHasMultipleTokens_DeletesAll() {
        // Given
        RefreshToken token1 = RefreshToken.builder()
                .userId(testUserId)
                .token("token1")
                .isActive(true)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();
        
        RefreshToken token2 = RefreshToken.builder()
                .userId(testUserId)
                .token("token2")
                .isActive(false)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();
        
        refreshTokenRepository.saveAll(List.of(token1, token2));
        
        // When
        refreshTokenRepository.deletePreviousTokens(testUserId);
        refreshTokenRepository.flush();
        
        // Then
        List<RefreshToken> remainingTokens = refreshTokenRepository.findByUserIdAndIsActive(testUserId, true);
        assertThat(remainingTokens).isEmpty();
        
        List<RefreshToken> allUserTokens = refreshTokenRepository.findAll()
                .stream()
                .filter(t -> t.getUserId().equals(testUserId))
                .toList();
        assertThat(allUserTokens).isEmpty();
    }

    @Test
    @DisplayName("Should not delete tokens of other users when deleting previous tokens")
    void deletePreviousTokens_WhenDeletingOneUser_DoesNotAffectOtherUsers() {
        // Given
        UUID otherUserId = UUID.randomUUID();
        
        RefreshToken userToken = RefreshToken.builder()
                .userId(testUserId)
                .token("user-token")
                .isActive(true)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();
        
        RefreshToken otherUserToken = RefreshToken.builder()
                .userId(otherUserId)
                .token("other-user-token")
                .isActive(true)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();
        
        refreshTokenRepository.saveAll(List.of(userToken, otherUserToken));
        
        // When
        refreshTokenRepository.deletePreviousTokens(testUserId);
        refreshTokenRepository.flush();
        
        // Then
        List<RefreshToken> otherUserTokens = refreshTokenRepository.findByUserIdAndIsActive(otherUserId, true);
        assertThat(otherUserTokens).hasSize(1);
        assertThat(otherUserTokens.get(0).getToken()).isEqualTo("other-user-token");
        
        List<RefreshToken> testUserTokens = refreshTokenRepository.findAll()
                .stream()
                .filter(t -> t.getUserId().equals(testUserId))
                .toList();
        assertThat(testUserTokens).isEmpty();
    }

    @Test
    @DisplayName("Should not throw exception when deleting tokens for user with no tokens")
    void deletePreviousTokens_WhenUserHasNoTokens_DoesNotThrowException() {
        // Given
        UUID userWithNoTokens = UUID.randomUUID();
        
        // When/Then
        refreshTokenRepository.deletePreviousTokens(userWithNoTokens);
        refreshTokenRepository.flush();
        // No exception should be thrown
    }
}