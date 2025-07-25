package com.quodex._miles.controller;

import com.quodex._miles.io.UserRequest;
import com.quodex._miles.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody UserRequest request) {
        authService.register(request);
        return ResponseEntity.ok("OTP sent for verification");
    }

    @PostMapping("/verify")
    public ResponseEntity<String> verifyOtp(@RequestParam String email, @RequestParam String otp) {
        String token = authService.verifyOtpAndLogin(email, otp);
        return ResponseEntity.ok("JWT Token: " + token);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestParam String email) {
        authService.sendOtpForLogin(email);
        return ResponseEntity.ok("OTP sent for login");
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<String> resendOtp(@RequestParam String email) {
        authService.resendOtp(email);
        return ResponseEntity.ok("OTP resent successfully");
    }
}


