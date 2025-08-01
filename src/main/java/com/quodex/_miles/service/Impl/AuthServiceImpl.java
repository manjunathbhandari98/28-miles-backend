package com.quodex._miles.service.Impl;

import com.quodex._miles.constant.Role;
import com.quodex._miles.entity.User;
import com.quodex._miles.exception.AlreadyExistsException;
import com.quodex._miles.exception.ResourceNotFoundException;
import com.quodex._miles.io.JWTResponse;
import com.quodex._miles.io.LoginResponse;
import com.quodex._miles.io.UserRequest;
import com.quodex._miles.io.UserResponse;
import com.quodex._miles.jwt.JwtUtil;
import com.quodex._miles.repository.UserRepository;
import com.quodex._miles.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Random;
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @Override
    public void register(UserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AlreadyExistsException("Email already registered");
        }

        String otp = generateOtp();

        User user = User.builder()
                .email(request.getEmail())
                .name(request.getName())
                .phone(request.getPhone())
                .role(Role.USER)
                .verified(false)
                .otp(otp)
                .otpGeneratedAt(LocalDateTime.now())
                .build();

        userRepository.save(user);

        System.out.println("OTP sent for registration: " + otp);
    }

    @Override
    public void sendOtpForLogin(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!user.isVerified()) {
            throw new RuntimeException("User not verified. Please verify account first.");
        }

        String otp = generateOtp();
        user.setOtp(otp);
        user.setOtpGeneratedAt(LocalDateTime.now());
        userRepository.save(user);

        System.out.println("OTP sent for login: " + otp);
    }

    @Override
    public JWTResponse verifyOtpAndLogin(String email, String otp) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getOtp() == null || !user.getOtp().equals(otp)) {
            throw new RuntimeException("Invalid OTP");
        }

        if (user.getOtpGeneratedAt() == null || user.getOtpGeneratedAt().plusMinutes(5).isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP expired");
        }

        if (!user.isVerified()) {
            user.setVerified(true);
        }

        user.setOtp(null);
        user.setOtpGeneratedAt(null);
        userRepository.save(user);
        LoginResponse response = convertToResponse(user);
        String token = jwtUtil.generateToken(user.getEmail());
        return new JWTResponse(token,response);
    }

    private LoginResponse convertToResponse(User user) {
        return LoginResponse.builder()
                .name(user.getName())
                .userId(user.getUserId())
                .email(user.getEmail())
                .phone(user.getPhone())
                .build();
    }


    @Override
    public void resendOtp(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String newOtp = generateOtp();
        user.setOtp(newOtp);
        user.setOtpGeneratedAt(LocalDateTime.now());
        userRepository.save(user);

        System.out.println("Resent OTP: " + newOtp);
    }

    private String generateOtp() {
        StringBuilder otp = new StringBuilder(); // StringBuilder for efficient string concatenation
        SecureRandom random = new SecureRandom(); // SecureRandom for cryptographic security

        // Generate a 4-digit OTP by appending random digits (0-9)
        for (int i = 0; i < 4; i++) {
            otp.append(random.nextInt(10)); // nextInt(10) generates a number between 0 and 9
        }

        return otp.toString(); // Convert StringBuilder to a String and return
    }
}
