package com.ecommerce.lab.repository.base;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.lab.model.WishlistItem;

import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface WishlistRepository extends JpaRepository<WishlistItem, Long> {
    @EntityGraph(attributePaths = {
            "product", "user", "product.category",
            "product.reviews"
    })
    List<WishlistItem> findByUserEmail(String email);

    @EntityGraph(attributePaths = {
            "product", "user", "product.category",
            "product.reviews"
    })
    boolean existsByUserEmailAndProductId(String email, Long productId);

    @Transactional
    @EntityGraph(attributePaths = {
            "product", "user", "product.category",
            "product.reviews"
    })
    void deleteByUserEmailAndProductId(String email, Long productId);
}
