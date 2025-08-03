package com.quodex._miles.io;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressRequest {

    private String fullName;
    private String phone;
    private String email;
    private String street;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private Boolean defaultAddress;

}

