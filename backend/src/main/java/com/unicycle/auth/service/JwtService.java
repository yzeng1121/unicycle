package com.unicycle.auth.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.unicycle.auth.entity.User;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

// TODO: check if the HMAC signing method is efficeint for this project compared to others
@Service
public class JwtService {
    @Value("${security.jwt.secret-key}")
    private String secretKey;
    @Value("${security.jwt.access.expiration-time}") // 15 minutes
    private Long accessTokenExpiration;
    @Value("${security.jwt.refresh.expiration-time}") // 30 days
    private Long refreshTokenExpiration;
    
    // access token (short-lived)
    public String generateAccessToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "access");
        return createToken(claims, userDetails, accessTokenExpiration);
    }
    
    // refresh token (long-lived)
    public String generateRefreshToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");
        return createToken(claims, userDetails, refreshTokenExpiration);
    }
    
    // creates a JWT authentication token
    // assembles + delivers all the parts of a JWT string
    private String createToken(Map<String, Object> claims, UserDetails userDetails, Long expiration) {
        User user = (User) userDetails;
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getUserId().toString())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.ES256)
                .compact();
    }
    
    public UUID extractUserId(String token) {
        return UUID.fromString(extractClaim(token, Claims::getSubject));
    }
    
    // gets the expiration date of the JWT
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    
    public String extractTokenType(String token) {
        return extractClaim(token, claims -> claims.get("type", String.class));
    }
    
    // taking a JWT token & resolving a claim
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    
    // checks if JWT is expired
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
    
    // confirms if the token returned is valid
    public boolean isTokenValid(String token, UUID userId) {
        final UUID tokenUserId = extractUserId(token);
        return tokenUserId.equals(userId) && !isTokenExpired(token);
    }
    
    public boolean isAccessToken(String token) {
        return "access".equals(extractTokenType(token));
    }
    
    public boolean isRefreshToken(String token) {
        return "refresh".equals(extractTokenType(token));
    }

    // TODO: is this change ok for security reasons?
    private Key getSignInKey() {
        byte[] keyBytes = java.util.Base64.getDecoder().decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
