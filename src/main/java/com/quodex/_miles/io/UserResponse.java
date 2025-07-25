package com.quodex._miles.io;

import com.quodex._miles.constant.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class UserResponse {
    private String userId;
    private String name;
    private String email;
    private String phone;
    private Role role;
    private List<AddressResponse> addresses;
}
