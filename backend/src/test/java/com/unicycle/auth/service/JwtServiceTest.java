package com.unicycle.auth.service;

import com.unicycle.auth.entity.User;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JwtService Tests")
class JwtServiceTest {

    private JwtService jwtService;

    private User mockUserA;
    private User mockUserB;
    private User mockUserC;

    private static final UUID MOCK_USER_A_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
    private static final UUID MOCK_USER_B_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440002");
    private static final UUID MOCK_USER_C_ID = UUID.fromString("00000000-0000-0000-0000-000000000003");

    private static final String MOCK_SECRET_KEY = "dGhpc2lzYXZlcnlsb25nc2VjcmV0a2V5Zm9ydW5pY3ljbGVhcHBsaWNhdGlvbjEyMzQ1Njc4OQ==";
    
    private static final Long ACCESS_TOKEN_EXPIRATION = 900000L; // 15 minutes
    private static final Long REFRESH_TOKEN_EXPIRATION = 2592000000L; // 30 days

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        
        ReflectionTestUtils.setField(jwtService, "secretKey", MOCK_SECRET_KEY);
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiration", ACCESS_TOKEN_EXPIRATION);
        ReflectionTestUtils.setField(jwtService, "refreshTokenExpiration", REFRESH_TOKEN_EXPIRATION);

        mockUserA = new User();
        mockUserA.setUserId(MOCK_USER_A_ID);
        mockUserA.setEmail("john.doe@tufts.edu");
        mockUserA.setUsername("jdoe");

        mockUserB = new User();
        mockUserB.setUserId(MOCK_USER_B_ID);
        mockUserB.setEmail("jane.doe@tufts.edu");
        mockUserB.setUsername("jadoe");

        mockUserC = new User();
        mockUserC.setUserId(MOCK_USER_C_ID);
        mockUserC.setEmail("test.user@tufts.edu");
        mockUserC.setUsername("testuser");
    }

    @Nested
    @DisplayName("generateAccessToken Tests")
    class AccessTokenGenerationTests {

        @Test
        void generateAccessToken_validUser_returnsNonNullToken() {
            String token = jwtService.generateAccessToken(mockUserA);
            assertNotNull(token);
            assertFalse(token.isEmpty());
        }

        @Test
        void generateAccessToken_validUser_returnsValidJwtFormat() {
            String token = jwtService.generateAccessToken(mockUserA);
            String[] parts = token.split("\\.");
            assertEquals(3, parts.length, "JWT should have header, payload, and signature");
        }

        @Test
        void generateAccessToken_sameUserCalledTwice_returnsUniqueTokens() throws InterruptedException {
            String token1 = jwtService.generateAccessToken(mockUserA);
            Thread.sleep(1000);
            String token2 = jwtService.generateAccessToken(mockUserA);
            assertNotEquals(token1, token2);
        }

        @Test
        void generateAccessToken_uniqueUsers_returnsUniqueTokens() {
            String token1 = jwtService.generateAccessToken(mockUserA);
            String token2 = jwtService.generateAccessToken(mockUserB);
            assertNotEquals(token1, token2);
        }
    }

    @Nested
    @DisplayName("generateRefreshToken Tests")
    class RefreshTokenGenerationTests {

        @Test
        void generateRefreshToken_validUser_returnsNonNullToken() {
            String token = jwtService.generateRefreshToken(mockUserA);
            assertNotNull(token);
            assertFalse(token.isEmpty());
        }

        @Test
        void generateRefreshToken_validUser_returnsLongerExpirationThanAccessToken() {
            String accessToken = jwtService.generateAccessToken(mockUserA);
            String refreshToken = jwtService.generateRefreshToken(mockUserA);
            
            Date accessExpiration = jwtService.extractExpiration(accessToken);
            Date refreshExpiration = jwtService.extractExpiration(refreshToken);
            
            assertTrue(refreshExpiration.after(accessExpiration));
        }
    }

    @Nested
    @DisplayName("extractUserI Tests")
    class UserIdExtractionTests {

        @Test
        void extractUserId_validAccessToken_returnsCorrectUserId() {
            String token = jwtService.generateAccessToken(mockUserA);
            UUID extractedId = jwtService.extractUserId(token);
            assertEquals(MOCK_USER_A_ID, extractedId);
        }

        @Test
        @DisplayName("Should extract correct user ID from refresh token")
        void extractUserId_validRefreshToken_returnsCorrectUserId() {
            String token = jwtService.generateRefreshToken(mockUserB);
            
            UUID extractedId = jwtService.extractUserId(token);
            
            assertEquals(MOCK_USER_B_ID, extractedId);
        }

        @Test
        @DisplayName("Should extract user ID with all zeros UUID")
        void extractUserId_tokenWithZerosUuid_returnsCorrectUserId() {
            String token = jwtService.generateAccessToken(mockUserC);
            
            UUID extractedId = jwtService.extractUserId(token);
            
            assertEquals(MOCK_USER_C_ID, extractedId);
        }
    }

    @Nested
    @DisplayName("isAccessToken, isRefreshToken Tests")
    class TokenTypeExtractionTests {

        @Test
        void isAccessToken_accessToken_returnsTrue() {
            String token = jwtService.generateAccessToken(mockUserA);
            assertTrue(jwtService.isAccessToken(token));
        }

        @Test
        void isAccessToken_refreshToken_returnsFalse() {
            String token = jwtService.generateRefreshToken(mockUserA);
            assertFalse(jwtService.isAccessToken(token));
        }

        @Test
        void isRefreshToken_refreshToken_returnsTrue() {
            String token = jwtService.generateRefreshToken(mockUserA);
            assertTrue(jwtService.isRefreshToken(token));
        }

        @Test
        void isRefreshToken_accessToken_returnsFalse() {
            String token = jwtService.generateAccessToken(mockUserA);
            assertFalse(jwtService.isRefreshToken(token));
        }

        @Test
        void extractTokenType_accessToken_returnsAccessString() {
            String accessToken = jwtService.generateAccessToken(mockUserA);
            assertEquals("access", jwtService.extractTokenType(accessToken));
        }

        @Test
        void extractTokenType_refreshToken_returnsRefreshString() {
            String refreshToken = jwtService.generateRefreshToken(mockUserA);
            assertEquals("refresh", jwtService.extractTokenType(refreshToken));
        }
    }

    @Nested
    @DisplayName("extractExpiration Tests")
    class ExpirationExtractionTests {

        @Test
        void extractExpiration_validAccessToken_returnsFutureDate() {
            String token = jwtService.generateAccessToken(mockUserA);
            Date expiration = jwtService.extractExpiration(token);
            assertNotNull(expiration);
            assertTrue(expiration.after(new Date()));
        }

        @Test
        void extractExpiration_accessToken_returnsApproximately15Minutes() {
            long now = System.currentTimeMillis();
            String token = jwtService.generateAccessToken(mockUserA);
            Date expiration = jwtService.extractExpiration(token);
            
            long expectedExpiration = now + ACCESS_TOKEN_EXPIRATION;
            long tolerance = 1000;
            
            assertTrue(Math.abs(expiration.getTime() - expectedExpiration) <= tolerance);
        }

        @Test
        void extractExpiration_refreshToken_returnsApproximately30Days() {
            long now = System.currentTimeMillis();
            String token = jwtService.generateRefreshToken(mockUserA);
            Date expiration = jwtService.extractExpiration(token);
            
            long expectedExpiration = now + REFRESH_TOKEN_EXPIRATION;
            long tolerance = 1000;
            
            assertTrue(Math.abs(expiration.getTime() - expectedExpiration) <= tolerance);
        }
    }

    @Nested
    @DisplayName("isTokenValid Tests")
    class TokenValidationTests {

        @Test
        void isTokenValid_correctUserId_returnsTrue() {
            String token = jwtService.generateAccessToken(mockUserA);
            assertTrue(jwtService.isTokenValid(token, MOCK_USER_A_ID));
        }

        @Test
        void isTokenValid_wrongUserId_returnsFalse() {
            String token = jwtService.generateAccessToken(mockUserA);
            assertFalse(jwtService.isTokenValid(token, MOCK_USER_B_ID));
        }

        @Test
        void isTokenValid_refreshTokenCorrectUserId_returnsTrue() {
            String token = jwtService.generateRefreshToken(mockUserB);
            assertTrue(jwtService.isTokenValid(token, MOCK_USER_B_ID));
        }

        @Test
        void isTokenValid_expiredToken_throwsExpiredJwtException() {
            JwtService shortExpirationService = new JwtService();
            ReflectionTestUtils.setField(shortExpirationService, "secretKey", MOCK_SECRET_KEY);
            ReflectionTestUtils.setField(shortExpirationService, "accessTokenExpiration", 1L); // 1ms
            ReflectionTestUtils.setField(shortExpirationService, "refreshTokenExpiration", REFRESH_TOKEN_EXPIRATION);
            
            String token = shortExpirationService.generateAccessToken(mockUserA);
            
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            assertThrows(ExpiredJwtException.class, () -> {
                shortExpirationService.isTokenValid(token, MOCK_USER_A_ID);
            });
        }
    }

    @Nested
    @DisplayName("Invalid Token Tests")
    class InvalidTokenHandlingTests {

        @Test
        void extractUserId_malformedToken_throwsMalformedJwtException() {
            String malformedToken = "not.a.valid.jwt.token";
            assertThrows(MalformedJwtException.class, () -> {
                jwtService.extractUserId(malformedToken);
            });
        }

        @Test
        void extractUserId_emptyToken_throwsIllegalArgumentException() {
            assertThrows(IllegalArgumentException.class, () -> {
                jwtService.extractUserId("");
            });
        }

        @Test
        void extractUserId_nullToken_throwsIllegalArgumentException() {
            assertThrows(IllegalArgumentException.class, () -> {
                jwtService.extractUserId(null);
            });
        }

        @Test
        void extractUserId_tamperedSignature_throwsSignatureException() {
            String token = jwtService.generateAccessToken(mockUserA);
            String tamperedToken = token.substring(0, token.lastIndexOf('.') + 1) + "invalidsignature";
            assertThrows(SignatureException.class, () -> {
                jwtService.extractUserId(tamperedToken);
            });
        }

        @Test
        void extractUserId_differentSecretKey_throwsSignatureException() {
            JwtService differentKeyService = new JwtService();
            String differentKey = "YW5vdGhlcnZlcnlsb25nc2VjcmV0a2V5Zm9ydGVzdGluZ3B1cnBvc2VzMTIzNDU2Nzg5MA==";

            ReflectionTestUtils.setField(differentKeyService, "secretKey", differentKey);
            ReflectionTestUtils.setField(differentKeyService, "accessTokenExpiration", ACCESS_TOKEN_EXPIRATION);
            ReflectionTestUtils.setField(differentKeyService, "refreshTokenExpiration", REFRESH_TOKEN_EXPIRATION);
            
            String tokenFromDifferentKey = differentKeyService.generateAccessToken(mockUserA);
            assertThrows(SignatureException.class, () -> {
                jwtService.extractUserId(tokenFromDifferentKey);
            });
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        void extractUserId_minimalUUID_returnsCorrectUserId() {
            User minimalUser = new User();
            minimalUser.setUserId(UUID.fromString("00000000-0000-0000-0000-000000000000"));
            minimalUser.setEmail("minimal@test.com");
            minimalUser.setUsername("minimal");
            
            String token = jwtService.generateAccessToken(minimalUser);
            UUID extractedId = jwtService.extractUserId(token);
            
            assertEquals(minimalUser.getUserId(), extractedId);
        }

        @Test
        void extractUserId_maximumUUID_returnsCorrectUserId() {
            User maxUser = new User();
            maxUser.setUserId(UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff"));
            maxUser.setEmail("max@test.com");
            maxUser.setUsername("maxuser");
            
            String token = jwtService.generateAccessToken(maxUser);
            UUID extractedId = jwtService.extractUserId(token);
            
            assertEquals(maxUser.getUserId(), extractedId);
        }

        @Test
        void extractClaim_multipleExtractions_returnsConsistentResults() {
            String token = jwtService.generateAccessToken(mockUserA);
            
            UUID id1 = jwtService.extractUserId(token);
            UUID id2 = jwtService.extractUserId(token);
            Date exp1 = jwtService.extractExpiration(token);
            Date exp2 = jwtService.extractExpiration(token);
            String type1 = jwtService.extractTokenType(token);
            String type2 = jwtService.extractTokenType(token);
            
            assertEquals(id1, id2);
            assertEquals(exp1, exp2);
            assertEquals(type1, type2);
        }
    }

    @Nested
    @DisplayName("Cross-Token Validation Tests")
    class CrossTokenValidationTests {

        @Test
        void extractUserId_accessAndRefreshToken_returnsSameUserId() {
            String accessToken = jwtService.generateAccessToken(mockUserA);
            String refreshToken = jwtService.generateRefreshToken(mockUserA);
            
            assertEquals(
                jwtService.extractUserId(accessToken),
                jwtService.extractUserId(refreshToken)
            );
        }
    }
}