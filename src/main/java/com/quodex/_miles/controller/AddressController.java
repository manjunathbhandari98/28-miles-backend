package com.quodex._miles.controller;

import com.quodex._miles.io.AddressRequest;
import com.quodex._miles.io.AddressResponse;
import com.quodex._miles.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/addresses")
@RestController
@RequiredArgsConstructor
public class AddressController {
    private final AddressService addressService;

    @PostMapping("/{userId}")
    public ResponseEntity<AddressResponse> addAddress(@PathVariable String userId, @RequestBody AddressRequest request){
        AddressResponse response = addressService.addAddress(userId,request);
        return ResponseEntity.ok(response);
    }

    @GetMapping()
    public ResponseEntity<List<AddressResponse>> getAddressesByUser(@RequestParam String userId){
        List<AddressResponse> addressResponses = addressService.getAddressByUser(userId);
        return ResponseEntity.ok(addressResponses);
    }

    @PutMapping("/update/{addressId}")
    public ResponseEntity<AddressResponse> updateAddress(@PathVariable String addressId,
                                                         @RequestBody AddressRequest addressRequest){
        AddressResponse addressResponse = addressService.updateAddress(addressId, addressRequest);
        return ResponseEntity.ok(addressResponse);
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<String> deleteAddress(@PathVariable String addressId){
        addressService.deleteAddress(addressId);
        return ResponseEntity.ok("Address Deleted Successfully");
    }
}
