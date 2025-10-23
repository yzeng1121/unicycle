package com.unicycle.auth.repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import static org.assertj.core.api.Assertions.assertThat;

import com.unicycle.auth.entity.User;

@DataJpaTest
public class UserRepositoryTest {
    @Autowired
    private UserRepository underTest;

    @Test
    void findUserByUserId() {
        // given
        User user = new User(
            null,
            "Bob", 
            "Dillon", 
            "Stratton Hall", 
            "bob_dillon", 
            "bob_dillon@tufts.edu", 
            "bobdillonisnotreal",
            true,
            "12345",
            LocalDateTime.now().plusDays(7)
        );

        User savedUser = underTest.save(user);
        Optional<User> exists = underTest.findByUserId(savedUser.getUserId());

        // then
        assertThat(exists).isPresent();
        assertThat(exists.get().getFirstName()).isEqualTo("Bob");
        assertThat(exists.get().getLastName()).isEqualTo("Dillon");
        assertThat(exists.get().getUsername()).isEqualTo("bob_dillon@tufts.edu");
        assertThat(exists.get().getDorm()).isEqualTo("Stratton Hall");
        assertThat(exists.get().getEmail()).isEqualTo("bob_dillon@tufts.edu");
    }
}