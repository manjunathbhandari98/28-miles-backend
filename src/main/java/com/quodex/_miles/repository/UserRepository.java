package com.quodex._miles.repository;

import com.quodex._miles.entity.User;
import com.quodex._miles.io.AddressResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<User> getByUserId(String userId);

    List<AddressResponse> findByUserId(String userId);
}
