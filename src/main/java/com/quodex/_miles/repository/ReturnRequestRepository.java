package com.quodex._miles.repository;

import com.quodex._miles.entity.Return;
import com.quodex._miles.io.ReturnResponse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReturnRequestRepository extends JpaRepository<Return, Long> {
    Optional<Return> findByRequestId(String requestId);
}
