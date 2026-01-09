package com.hotel.management.system.service;

import com.hotel.management.system.model.Role;
import com.hotel.management.system.model.User;
import com.hotel.management.system.repository.IUserRepository;
import com.hotel.management.system.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    private UserService userService;
    private User testUser;

    @BeforeEach
    void setUp() {
        IUserRepository userRepository = new UserRepository();
        userService = new UserService(userRepository);

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
        Optional<User> createdUser = userService.getAllUsers().stream().findFirst();
        assertTrue(createdUser.isPresent());

        userService.updatePassword(createdUser.get().getId(), "luke_new_password456");

        Optional<User> updatedUser = userService.getAllUsers().stream().findFirst();
        assertTrue(updatedUser.isPresent());
        assertEquals("luke_new_password456", updatedUser.get().getPassword());
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
