package com.ecommerce.lab.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import com.ecommerce.lab.dto.ProductResponseDTO;
import com.ecommerce.lab.repository.base.WishlistRepository;
import com.ecommerce.lab.service.WishlistService;

@RestController
@RequestMapping("/api/wishlist")
public class WishlistController {
    private final WishlistService wishlistService;
    private final WishlistRepository wishlistRepository;

    public WishlistController(
        WishlistService wishlistService, WishlistRepository wishlistRepository
    ) {
        this.wishlistService = wishlistService;
        this.wishlistRepository = wishlistRepository;
    }

    @PostMapping("/{productId}")
    public ResponseEntity<?> addToWishlist(@PathVariable Long productId, Principal principal) {
        if (principal == null)
            return ResponseEntity.status(401).build();

        wishlistService.addToWishlist(productId, principal.getName());
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<ProductResponseDTO>> getWishlist(Principal principal) {
        if (principal == null)
            return ResponseEntity.status(401).build();

        return ResponseEntity.ok(wishlistService.getWishlist(principal.getName()));
    }

    @DeleteMapping("/{productId}")
    @Transactional
    public ResponseEntity<?> removeFromWishlist(@PathVariable Long productId, Principal principal) {
        if (principal == null)
            return ResponseEntity.status(401).build();

        wishlistRepository.deleteByUserEmailAndProductId(principal.getName(), productId);
        return ResponseEntity.ok().build();
    }
}
