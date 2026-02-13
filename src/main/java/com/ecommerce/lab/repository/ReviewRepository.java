package com.ecommerce.lab.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.lab.model.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {

}
