package com.ecommerce.lab.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.lab.model.WishlistItem;

public interface WishlistRepository extends JpaRepository<WishlistItem, Long> {
    List<WishlistItem> findByUserEmail(String email);

    boolean existsByUserEmailAndProductId(String email, Long productId);

    @Transactional
    void deleteByUserEmailAndProductId(String email, Long productId);
}
