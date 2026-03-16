package com.ecommerce.lab.controller;

import java.security.Principal;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ecommerce.lab.model.Product;
import com.ecommerce.lab.model.Review;
import com.ecommerce.lab.model.User;
import com.ecommerce.lab.repository.base.ProductRepository;
import com.ecommerce.lab.repository.base.ReviewRepository;
import com.ecommerce.lab.repository.base.UserRepository;
import com.ecommerce.lab.service.OrderService;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final OrderService orderService;

    public ReviewController(
        ProductRepository productRepository, UserRepository userRepository,
        ReviewRepository reviewRepository, OrderService orderService
    ) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.reviewRepository = reviewRepository;
        this.orderService = orderService;
    }

    @PostMapping("/{productId}")
    public ResponseEntity<?> addReview(
        @PathVariable Long productId,
        @RequestBody Review review,
        Principal principal
    ) {
        if (principal == null)
            return ResponseEntity.status(401).body("Login required");

        String email = principal.getName();

        if (!orderService.hasUserPurchasedProduct(email, productId)) {
            return ResponseEntity.badRequest().body(
                Map.of("error", "You can only review products you have purchased.")
            );
        }

        if (reviewRepository.existsByUserEmailAndProductId(email, productId)) {
            return ResponseEntity.badRequest().body(
                Map.of("error", "You have already reviewed this product.")
            );
        }

        Product product = productRepository.findById(productId).orElseThrow();
        User user = userRepository.findByEmail(email).orElseThrow();

        review.setProduct(product);
        review.setUser(user);
        reviewRepository.save(review);

        return ResponseEntity.ok().build();
    }
}