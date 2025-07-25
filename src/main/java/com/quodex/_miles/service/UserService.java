package com.quodex._miles.service;

import com.quodex._miles.io.UserResponse;

import java.util.List;

public interface UserService {
    List<UserResponse> getUsers();

    UserResponse getUserByUserId(String userId);
}
