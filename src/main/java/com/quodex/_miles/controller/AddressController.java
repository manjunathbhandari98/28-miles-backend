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

    @PostMapping
    public ResponseEntity<AddressResponse> addAddress(@RequestBody AddressRequest request){
        AddressResponse response = addressService.addAddress(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping()
    public ResponseEntity<List<AddressResponse>> getAddressesByUser(@RequestParam String userId){
        List<AddressResponse> addressResponses = addressService.getAddressByUser(userId);
        return ResponseEntity.ok(addressResponses);
    }
}
