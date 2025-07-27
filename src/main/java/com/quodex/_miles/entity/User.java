package com.quodex._miles.entity;

import com.quodex._miles.constant.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String userId;
    private String name;
    private String email;
    private String phone;
    private Role role;

    private boolean verified;
    // Optional: to store OTP temporarily
    private String otp;

    private LocalDateTime otpGeneratedAt;
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Address> addresses;


    @PrePersist
    public void generateUserId() {
        if (this.userId == null) {
            if (this.role == null || this.role == Role.USER) {
                this.role = Role.USER; // default if null
                this.userId = "USR" + UUID.randomUUID().toString().toUpperCase().substring(0,7);
            } else if (this.role == Role.ADMIN) {
                this.userId = "ADM" + UUID.randomUUID().toString().toUpperCase().substring(0,7);
            }
        }
    }


}
