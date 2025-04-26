package com.volleyball.finder.service;

import com.volleyball.finder.dto.UserUpdateDto;
import com.volleyball.finder.entity.User;

import java.util.Optional;

/**
 * Service interface for managing user-related operations
 */
public interface UserService {
    /**
     * Find a user by their ID
     * @param id The user's ID
     * @return The found user, or null if not found
     */
    User findById(Long id);

    Optional<User> findByLineId(String lineId);

    /**
     * Create a new user
     * @param user The user to create
     * @return The created user with generated ID
     */
    User createUser(User user);

    User updateUser(Long id, UserUpdateDto userUpdateDto);

    /**
     * Delete a user by their ID
     * @param id The ID of the user to delete
     */
    void delete(Long id);

    /**
     * Get the currently authenticated user
     * @return The current user
     * @throws IllegalStateException if no user is authenticated
     */
    User getCurrentUser();
}

