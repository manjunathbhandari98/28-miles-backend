package com.quodex._miles.service;

import com.quodex._miles.io.UserRequest;
public interface AuthService {
    void register(UserRequest request);
    void sendOtpForLogin(String email);
    String verifyOtpAndLogin(String email, String otp);
    void resendOtp(String email);
}
