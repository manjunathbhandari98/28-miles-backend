package com.quodex._miles.io;

import com.quodex._miles.constant.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRequest {
    private String name;
    private String email;
    private String phone;
    private Role role;

}
