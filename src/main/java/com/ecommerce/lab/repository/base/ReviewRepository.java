package com.ecommerce.lab.repository.base;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.lab.model.Review;


import org.springframework.data.repository.NoRepositoryBean;
@NoRepositoryBean
public interface ReviewRepository extends JpaRepository<Review, Long> {
    boolean existsByUserEmailAndProductId(String email, Long productId);
}
