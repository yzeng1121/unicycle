package com.unicycle.auth.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.unicycle.auth.entity.User;
import com.unicycle.profile.dto.UserBasicDto;

@Repository
public interface UserRepository extends CrudRepository<User, UUID> {
    // TODO: if login via either email or username would have to join the two
    // methods below
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    Optional<User> findByVerificationCode(String verificationCode);

    @Query("SELECT u FROM User u WHERE u.userId = :userId")
    Optional<User> findByUserId(@Param("userId") UUID userId);
    
    // fetch basic user data from users table in DB
    @Query("SELECT new com.unicycle.profile.dto.UserBasicDto(u.username, u.firstName, u.lastName, u.dorm) FROM User u WHERE u.userId = :userId")
    UserBasicDto getUserBasicDtoByUserId(@Param("userId") UUID userId);

    @Query("SELECT u.username FROM User u WHERE u.userId = :userId")
    String getUsernameByUserId(@Param("userId") UUID userId);

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
