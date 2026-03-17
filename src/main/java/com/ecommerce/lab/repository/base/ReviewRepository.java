package com.ecommerce.lab.repository.base;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.ecommerce.lab.model.Review;

import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface ReviewRepository extends JpaRepository<Review, Long> {
    @EntityGraph(attributePaths = {
            "product", "user"
    })
    boolean existsByUserEmailAndProductId(String email, Long productId);

    @Query("SELECT r.product.id FROM Review r WHERE r.user.email = :email AND r.product.id IN :ids")
    Set<Long> findReviewedProductIds(String email, List<Long> ids);
}
