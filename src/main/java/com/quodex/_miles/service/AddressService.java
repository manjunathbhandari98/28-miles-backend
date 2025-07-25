package com.quodex._miles.service;

import com.quodex._miles.io.AddressRequest;
import com.quodex._miles.io.AddressResponse;

import java.util.List;

public interface AddressService {
    AddressResponse addAddress(AddressRequest request);

    List<AddressResponse> getAddressByUser(String userId);

    AddressResponse updateAddress(String addressId, AddressRequest request)

}
