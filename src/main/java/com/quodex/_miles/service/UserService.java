package com.quodex._miles.service;

import com.quodex._miles.io.UserRequest;
import com.quodex._miles.io.UserResponse;

import java.util.List;

public interface UserService {
    List<UserResponse> getUsers();

    UserResponse getUsername(String userId);

    UserResponse getUserByUserId(String userId);

    UserResponse updateUser(String userId, UserRequest request);
}
