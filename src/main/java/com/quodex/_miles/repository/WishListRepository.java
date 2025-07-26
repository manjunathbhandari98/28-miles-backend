package com.quodex._miles.repository;

import com.quodex._miles.entity.User;
import com.quodex._miles.entity.WishList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WishListRepository extends JpaRepository<WishList, Long> {
    Optional<WishList> findByUser(User user);

    WishList findByWishListId(String wishListId);
}
