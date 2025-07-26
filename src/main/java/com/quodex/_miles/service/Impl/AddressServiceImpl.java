package com.quodex._miles.service.Impl;

import com.quodex._miles.entity.Address;
import com.quodex._miles.entity.User;
import com.quodex._miles.exception.ResourceNotFoundException;
import com.quodex._miles.io.AddressRequest;
import com.quodex._miles.io.AddressResponse;
import com.quodex._miles.repository.AddressRepository;
import com.quodex._miles.repository.UserRepository;
import com.quodex._miles.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;

    @Override
    public AddressResponse addAddress(AddressRequest request) {
        User user = userRepository.getByUserId(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + request.getUserId()));

        Address address = convertToEntity(request);
        Address saved = addressRepository.save(address);
        AddressResponse response = convertToDTO(saved);
        return response;
    }

    @Override
    public List<AddressResponse> getAddressByUser(String userId){
        User user = userRepository.getByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " +userId));
        return user.getAddresses().stream()
                .map(this::convertToDTO).toList();

    }

    @Override
    public AddressResponse updateAddress(String addressId, AddressRequest request) {
        Address address = addressRepository.findByAddressId(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address Not Found"));

        address.setFullName(request.getFullName());
        address.setPhone(request.getPhone());
        address.setStreet(request.getStreet());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setPostalCode(request.getPostalCode());
        address.setCountry(request.getCountry());
        address.setDefault(request.isDefault());

        // If you're allowing address transfer to another user (optional)
        if (request.getUserId() != null && !request.getUserId().equals(address.getUser().getUserId())) {
            User user = userRepository.getByUserId(request.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + request.getUserId()));
            address.setUser(user);
        }

        Address updated = addressRepository.save(address);
        return convertToDTO(updated);
    }

    @Override
    public void deleteAddress(String addressId){
        Address address = addressRepository.findByAddressId(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address Not Found"));
        addressRepository.delete(address);
    }


    private AddressResponse convertToDTO(Address saved) {
        return AddressResponse.builder()
                .addressId(saved.getAddressId())
                .fullName(saved.getFullName())
                .phone(saved.getPhone())
                .street(saved.getStreet())
                .city(saved.getCity())
                .state(saved.getState())
                .postalCode(saved.getPostalCode())
                .country(saved.getCountry())
                .isDefault(saved.isDefault())
                .userId(saved.getUser().getUserId())
                .build();
    }

    private Address convertToEntity(AddressRequest request) {
        User user = userRepository.getByUserId(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + request.getUserId()));

        return Address.builder()
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .street(request.getStreet())
                .city(request.getCity())
                .state(request.getState())
                .postalCode(request.getPostalCode())
                .country(request.getCountry())
                .isDefault(request.isDefault())
                .user(user)
                .build();
    }
}
