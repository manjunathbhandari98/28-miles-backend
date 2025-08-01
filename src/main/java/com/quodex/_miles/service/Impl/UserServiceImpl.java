package com.quodex._miles.service.Impl;

import com.quodex._miles.entity.Address;
import com.quodex._miles.entity.User;
import com.quodex._miles.io.AddressResponse;
import com.quodex._miles.io.LoginResponse;
import com.quodex._miles.io.UserRequest;
import com.quodex._miles.io.UserResponse;
import com.quodex._miles.repository.UserRepository;
import com.quodex._miles.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public List<UserResponse> getUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Override
    public UserResponse getUserByUserId(String userId) {
        User user = userRepository.getByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        return convertToResponse(user);

    }

    @Override
    public UserResponse updateUser(String userId, UserRequest request) {
       return null;
    }

    private User convertToEntity(UserRequest request) {
        return User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .build();
    }

    @Override
    public UserResponse getUsername(String userId){
        User user = userRepository.getByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        return convertToResponse(user);
    }

    private UserResponse convertToResponse(User user) {
        return UserResponse.builder()
                .userId(user.getUserId())
                .role(user.getRole())
                .name(user.getName())
                .phone(user.getPhone())
                .email(user.getEmail())
                .addresses(convertAddresses(user.getAddresses()))
                .build();
    }

    public List<AddressResponse> convertAddresses(List<Address> addresses) {
        return addresses.stream()
                .map(address -> AddressResponse.builder()
                        .addressId(address.getAddressId())
                        .fullName(address.getFullName())
                        .street(address.getStreet())
                        .city(address.getCity())
                        .state(address.getState())
                        .country(address.getCountry())
                        .postalCode(address.getPostalCode())
                        .phone(address.getPhone())
                        .email(address.getEmail())
                        .isDefault(address.isDefault())
                        .userId(address.getUser().getUserId())
                        .build())
                .collect(Collectors.toList());
    }
}
