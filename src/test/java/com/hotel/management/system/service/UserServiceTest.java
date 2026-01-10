package com.hotel.management.system.service;

import com.hotel.management.system.database.ConnectionProvider;
import com.hotel.management.system.database.TestDB;
import com.hotel.management.system.model.Role;
import com.hotel.management.system.model.User;
import com.hotel.management.system.repository.IUserRepository;
import com.hotel.management.system.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    private UserService userService;
    private User testUser;

    private final ConnectionProvider connectionProvider = TestDB.INSTANCE;

    @BeforeEach
    void setUp() throws Exception {
        // Clean the users table before each test
        try (Connection conn = connectionProvider.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM users");
        }

        // Inject TestDB into repository
        IUserRepository userRepository = new UserRepository(connectionProvider);
        userService = new UserService(userRepository);

        // Prepare a test user
        UUID testUserId = UUID.randomUUID();
        testUser = new User(testUserId, "luke_test_user", "luke_password123", Role.STAFF);
    }

    @Test
    void createUser_ShouldCreateAndSaveUser() {
        userService.createUser("luke_new_user", "luke_new_pass", Role.ADMIN);

        List<User> allUsers = userService.getAllUsers();
        assertEquals(1, allUsers.size());
        User savedUser = allUsers.getFirst();

        assertEquals("luke_new_user", savedUser.getUsername());
        assertEquals("luke_new_pass", savedUser.getPassword());
        assertEquals(Role.ADMIN, savedUser.getRole());
        assertNotNull(savedUser.getId());
    }

    @Test
    void authenticate_WhenCredentialsCorrect_ShouldReturnUser() {
        userService.createUser(testUser.getUsername(), testUser.getPassword(), testUser.getRole());

        Optional<User> result = userService.authenticate("luke_test_user", "luke_password123");
        assertTrue(result.isPresent());
        assertEquals("luke_test_user", result.get().getUsername());
    }

    @Test
    void authenticate_WhenPasswordIncorrect_ShouldReturnEmpty() {
        userService.createUser(testUser.getUsername(), testUser.getPassword(), testUser.getRole());

        Optional<User> result = userService.authenticate("luke_test_user", "wrong_password");
        assertFalse(result.isPresent());
    }

    @Test
    void authenticate_WhenUsernameNotFound_ShouldReturnEmpty() {
        Optional<User> result = userService.authenticate("luke_nonexistent", "luke_password123");
        assertFalse(result.isPresent());
    }

    @Test
    void updatePassword_WhenUserExists_ShouldUpdatePassword() {
        userService.createUser(testUser.getUsername(), testUser.getPassword(), testUser.getRole());
        User createdUser = userService.getAllUsers().getFirst();

        userService.updatePassword(createdUser.getId(), "luke_new_password456");

        User updatedUser = userService.getAllUsers().getFirst();
        assertEquals("luke_new_password456", updatedUser.getPassword());
    }

    @Test
    void updatePassword_WhenUserDoesNotExist_ShouldDoNothing() {
        // No users in repo yet
        assertDoesNotThrow(() -> userService.updatePassword(UUID.randomUUID(), "luke_new_password"));
    }

    @Test
    void getAllUsers_ShouldReturnAllUsers() {
        userService.createUser("luke_user1", "luke_pass1", Role.STAFF);
        userService.createUser("luke_admin", "luke_pass2", Role.ADMIN);

        List<User> allUsers = userService.getAllUsers();
        assertEquals(2, allUsers.size());
    }

    @Test
    void deleteUser_ShouldDeleteUser() {
        userService.createUser("luke_userToDelete", "luke_pass", Role.STAFF);
        User u = userService.getAllUsers().getFirst();

        userService.deleteUser(u.getId());
        assertEquals(0, userService.getAllUsers().size());
    }
}
