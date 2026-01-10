package com.hotel.management.system.service;

import com.hotel.management.system.model.Role;
import com.hotel.management.system.model.User;
import com.hotel.management.system.repository.IUserRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class UserService {
    private final IUserRepository userRepository;

    public UserService(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void createUser(String username, String password, Role role) {
        User user = new User(java.util.UUID.randomUUID(), username, password, role);
        userRepository.save(user);
    }

    public Optional<User> authenticate(String username, String password) {
        return userRepository.findByUsername(username)
                .filter(user -> user.getPassword().equals(password));
    }

    public void updatePassword(UUID userId, String newPassword) {
        userRepository.updatePassword(userId, newPassword);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public void deleteUser(UUID userId) {
        userRepository.deleteById(userId);
    }
}
