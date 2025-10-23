package com.unicycle.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.unicycle.auth.entity.RefreshToken;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    // TODO: why are there multiple refresh tokens for a single user
    Optional<RefreshToken> findByTokenAndIsActive(String token, Boolean isActive);
    
    List<RefreshToken> findByUserIdAndIsActive(UUID userId, Boolean isActive);
    
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.isActive = false WHERE rt.userId = :userId")
    void deactivateAllByUserId(@Param("userId") UUID userId);
    
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.isActive = false WHERE rt.token = :token")
    void deactivateByToken(@Param("token") String token);
    
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :now")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);
}
