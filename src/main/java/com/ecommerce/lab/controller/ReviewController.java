package com.ecommerce.lab.controller;

import java.security.Principal;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ecommerce.lab.model.Product;
import com.ecommerce.lab.model.Review;
import com.ecommerce.lab.model.User;
import com.ecommerce.lab.repository.ProductRepository;
import com.ecommerce.lab.repository.ReviewRepository;
import com.ecommerce.lab.repository.UserRepository;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;

    public ReviewController(ProductRepository productRepository, UserRepository userRepository,
            ReviewRepository reviewRepository) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.reviewRepository = reviewRepository;
    }

    @PostMapping("/{productId}")
    public ResponseEntity<?> addReview(@PathVariable Long productId,
            @RequestBody Review review,
            Principal principal) {
        if (principal == null)
            return ResponseEntity.status(401).body("Login required");

        Product product = productRepository.findById(productId).orElseThrow();
        User user = userRepository.findByEmail(principal.getName()).orElseThrow();

        review.setProduct(product);
        review.setUser(user);
        reviewRepository.save(review);

        return ResponseEntity.ok().build();
    }
}