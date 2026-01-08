package com.unicycle.auth.repository;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

import com.unicycle.auth.entity.User;
import com.unicycle.profile.dto.UserBasicDto;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.mail.host=localhost",
    "security.jwt.secret-key=testkey1234567890123456789012345678901234567890"
})
public class UserRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository undertest;

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
    void findByUserId_success() {
        Optional<User> user = undertest.findByUserId(realUUID);
        assertTrue(user.isPresent());
        assertEquals(realUUID, user.get().getUserId());
    }

    @Test
    void findByUserId_invalidUserId_fail() {
        Optional<User> user = undertest.findByUserId(fakeUUID);
        assertFalse(user.isPresent());
    }

    @Test
    void getUserBasicDtoByUserIdTest_success() {
        UserBasicDto dto = undertest.getUserBasicDtoByUserId(realUUID);
        assertNotNull(dto);
        assertEquals(realUUID, dto.getUserId());
        assertEquals("jdoe", dto.getUsername());
        assertEquals("John", dto.getFirstName());
        assertEquals("Doe", dto.getLastName());
        assertEquals("Harleston Hall", dto.getDorm());
    }

    @Test
    void getUserBasicDtoByUserIdTest_invalidUserId_fail() {
        UserBasicDto dto = undertest.getUserBasicDtoByUserId(fakeUUID);
        assertNull(dto);
    }

    @Test
    void getUsernameByUserIdTest_success() {
        String username = undertest.getUsernameByUserId(realUUID);
        assertNotNull(username);
        assertEquals("jdoe", username);
    }

    @Test
    void getUsernameByUserIdTest_invalidUserId_fail() {
        String username = undertest.getUsernameByUserId(fakeUUID);
        assertNull(username);
    }
}