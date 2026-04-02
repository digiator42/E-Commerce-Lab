package com.ecommerce.lab.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.lab.dto.CartItemResponseDTO;
import com.ecommerce.lab.dto.GiftCardRequest;
import com.ecommerce.lab.model.CartItem;
import com.ecommerce.lab.repository.base.CartRepository;
import com.ecommerce.lab.service.CartService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;
    private final CartRepository cartRepository;

    public CartController(CartService cartService, CartRepository cartRepository) {
        this.cartService = cartService;
        this.cartRepository = cartRepository;
    }

    @PostMapping("/add/{productId}")
    public ResponseEntity<?> addToCart(@PathVariable Long productId, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Login required");
        }
        cartService.addItemToCart(productId, principal.getName());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/add-gift-card")
    public ResponseEntity<?> addGiftCardToCart(
        @RequestBody @Valid GiftCardRequest gcRequest,
        Principal principal
    ) {
        if (principal == null)
            return ResponseEntity.status(401).body("Login required");

        cartService.addGiftCardToCart(gcRequest, principal.getName());
        return ResponseEntity.ok("Gift card added to cart");
    }

    @GetMapping
    public ResponseEntity<?> getCart(Principal principal) {

        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<CartItemResponseDTO> items = cartService.getCartItems(principal.getName());
        return ResponseEntity.ok(items);
    }

    @DeleteMapping("/remove/{id}")
    @Transactional
    public ResponseEntity<?> removeFromCart(@PathVariable Long id) {
        cartRepository.deleteById(id);
        cartRepository.flush(); // Force the DB to sync NOW
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/clear")
    @Transactional
    public ResponseEntity<?> clearCart(Principal principal) {
        List<CartItem> items = cartRepository.findAllByUserEmail(principal.getName());
        cartRepository.deleteAll(items);
        return ResponseEntity.ok().build();
    }
}