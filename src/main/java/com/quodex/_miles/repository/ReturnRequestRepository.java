package com.quodex._miles.repository;

import com.quodex._miles.entity.Order;
import com.quodex._miles.entity.Return;
import com.quodex._miles.entity.User;
import com.quodex._miles.io.ReturnResponse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public interface ReturnRequestRepository extends JpaRepository<Return, Long> {
    Optional<Return> findByRequestId(String requestId);

    List<Return> findByUser_UserId(String userId);

    Return findByOrder(Order order);
}
