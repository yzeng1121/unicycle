package com.unicycle.auth.repository;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import static org.junit.jupiter.api.Assertions.*;

import com.unicycle.auth.entity.User;
import com.unicycle.profile.dto.UserBasicDto;

@DataJpaTest
public class UserRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository underTest;

    private UUID realUUID;
    private static final UUID fakeUUID = UUID.fromString("fbb712e7-02a2-4aa0-a94a-7f2ad874c7aa");

    @BeforeEach
    void setUp() {
        User testUser = User.builder()
                .username("jdoe")
                .email("john.doe@tufts.edu")
                .password("password123")
                .firstName("John")
                .lastName("Doe")
                .dorm("Harleston Hall")
                .enabled(true)
                .build();

        entityManager.persist(testUser);
        entityManager.flush();

        // Get the generated UUID
        realUUID = testUser.getUserId();
    }

    @Test
    void findByUserIdTestSuccess() {
        Optional<User> user = underTest.findByUserId(realUUID);
        assertTrue(user.isPresent());
        assertEquals(realUUID, user.get().getUserId());
    }

    @Test
    void findByUserIdTestFail() {
        Optional<User> user = underTest.findByUserId(fakeUUID);
        assertFalse(user.isPresent());
    }

    @Test
    void getUserBasicDtoByUserIdTestSuccess() {
        UserBasicDto dto = underTest.getUserBasicDtoByUserId(realUUID);
        assertNotNull(dto);
        assertEquals("jdoe", dto.getUsername());
        assertEquals("John", dto.getFirstName());
        assertEquals("Doe", dto.getLastName());
        assertEquals("Harleston Hall", dto.getDorm());
    }

    @Test
    void getUserBasicDtoByUserIdTestFail() {
        UserBasicDto dto = underTest.getUserBasicDtoByUserId(fakeUUID);
        assertNull(dto);
    }

    @Test
    void getUsernameByUserIdTestSuccess() {
        String username = underTest.getUsernameByUserId(realUUID);
        assertNotNull(username);
        assertEquals("jdoe", username);
    }

    @Test
    void getUsernameByUserIdTestFail() {
        String username = underTest.getUsernameByUserId(fakeUUID);
        assertNull(username);
    }
}