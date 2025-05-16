package com.volleyball.finder.service;

import com.volleyball.finder.dto.UserPrivateResponse;
import com.volleyball.finder.dto.UserResponse;
import com.volleyball.finder.dto.UserUpdateRequest;
import com.volleyball.finder.entity.User;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing user-related operations
 */
public interface UserService {
    /**
     * Find a user by their ID
     *
     * @param id The user's ID
     * @return The found user, or null if not found
     */
    User findById(Long id);

    Optional<User> findByLineId(String lineId);

    /**
     * Create a new user
     *
     * @param user The user to create
     * @return The created user with generated ID
     */
    User createUser(User user);

    User updateUser(Long id, UserUpdateRequest userUpdateRequest);

    /**
     * Delete a user by their ID
     *
     * @param id The ID of the user to delete
     */
    void delete(Long id);

    /**
     * Get the currently authenticated user
     *
     * @return The current user
     * @throws IllegalStateException if no user is authenticated
     */
    User getCurrentUser();

    boolean isNicknameTaken(String nickname);

    String getFcmToken(Long userId);

    void updateFcmToken(Long userId, String fcmToken);

    Optional<UserResponse> getUserResponseById(Long id);

    List<UserPrivateResponse> getUserPrivateResponseByIds(List<Long> ids);
}

