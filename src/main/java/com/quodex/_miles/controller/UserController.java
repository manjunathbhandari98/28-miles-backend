package com.quodex._miles.controller;

import com.quodex._miles.io.UserRequest;
import com.quodex._miles.io.UserResponse;
import com.quodex._miles.service.UserService;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/users")
@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<UserResponse>> getUsers(){
        List<UserResponse> userResponses = userService.getUsers();
        return ResponseEntity.ok(userResponses);
    }

    @GetMapping("/name/{userId}")
    public ResponseEntity<UserResponse> getUserName(@PathVariable String userId){
        UserResponse userResponse = userService.getUsername(userId);
        return ResponseEntity.ok(userResponse);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUserByUserId(@PathVariable String userId){
        UserResponse userResponse = userService.getUserByUserId(userId);
        return ResponseEntity.ok(userResponse);
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable String userId, @RequestBody UserRequest request){
        UserResponse userResponse = userService.updateUser(userId, request);
        return ResponseEntity.ok(userResponse);
    }
}
