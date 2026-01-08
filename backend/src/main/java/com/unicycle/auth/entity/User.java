package com.unicycle.auth.entity;

import java.time.LocalDateTime;
import java.util.*;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// TODO: add
// TODO: @JsonIgnore for password since its being returned to the frontend
@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id")
    private UUID userId;
    @Column(name = "first_name", nullable = false)
    private String firstName;
    @Column(name = "last_name", nullable = false)
    private String lastName;
    @Column(nullable = false)
    private String dorm;
    @Column(unique = true, nullable = false, name = "username")
    private String username;
    @Column(unique = true, nullable = false)
    private String email;
    @JsonIgnore
    @Column(nullable = false)
    private String password;
    @JsonIgnore
    private boolean enabled;
    @JsonIgnore
    @Column(name = "verification_code")
    private String verificationCode;
    @JsonIgnore
    @Column(name = "verification_expiration")
    private LocalDateTime verificationCodeExpiresAt;

    // public String getRealUsername() {
    //     return this.username;
    // }

    // TODO: check if can change to improve clarity
    @JsonIgnore
    @Override
    public String getUsername() {
        return this.username; // return email for authentication
    }
    
    // overriding method to meet the requirements of the user detail interface
    // will return the users' role lists 
    // returns empty list since no role-based authentication
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    // TODO: how to check if an account is expired (in the case of students, when
    // students graduate)
    // OR expired as in refresh token no longer exists
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    // TODO: how to check if an account is nonlocked
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    // TODO: how to check if an account is nonexpired
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}

