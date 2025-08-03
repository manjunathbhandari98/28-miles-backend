package com.quodex._miles.repository;

import com.quodex._miles.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Long> {
    Optional<Address> findByAddressId(String addressId);

    Optional<Address> getByUserIdAndDefaultAddressTrue(Long id);
}
