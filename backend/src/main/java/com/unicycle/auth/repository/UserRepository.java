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
    Optional<User> findByUserId(UUID userId);
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    Optional<User> findByVerificationCode(String verificationCode);
    
    // fetch basic user data from users table in DB 
    @Query("SELECT u.username, u.firstName, u.lastName, u.dorm FROM User u WHERE u.id = :id")
    UserBasicDto getUserInUsersTableByUserId(@Param("id") UUID id);

    @Query("SELECT u.username FROM User u WHERE u.id = :id")
    String getUsernameByUserId(@Param("id") UUID id);
}
