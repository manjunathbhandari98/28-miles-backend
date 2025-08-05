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
    public AddressResponse addAddress(String userId, AddressRequest request) {
        User user = userRepository.getByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: "+ userId));
        Address address = convertToEntity(request,user);
        Address saved = addressRepository.save(address);
        return convertToDTO(saved);
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
        address.setEmail(request.getEmail());
        address.setStreet(request.getStreet());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setPostalCode(request.getPostalCode());
        address.setCountry(request.getCountry());

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
                .email(saved.getEmail())
                .street(saved.getStreet())
                .city(saved.getCity())
                .state(saved.getState())
                .postalCode(saved.getPostalCode())
                .country(saved.getCountry())
                .userId(saved.getUser() != null ? saved.getUser().getUserId() : null)
                .build();
    }


    private Address convertToEntity(AddressRequest request, User user) {
        return Address.builder()
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .street(request.getStreet())
                .city(request.getCity())
                .state(request.getState())
                .postalCode(request.getPostalCode())
                .country(request.getCountry())
                .user(user)
                .build();
    }

}
