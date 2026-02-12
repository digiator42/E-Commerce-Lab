package com.ecommerce.lab.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.lab.dto.CartItemDTO;
import com.ecommerce.lab.model.CartItem;
import com.ecommerce.lab.repository.CartRepository;
import com.ecommerce.lab.service.CartService;

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

    @GetMapping
    public ResponseEntity<List<CartItem>> getCart(Principal principal) {

        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<CartItem> items = cartService.getCartItems(principal.getName());
        return ResponseEntity.ok(items);
    }

    @DeleteMapping("/remove/{id}")
    public ResponseEntity<?> removeFromCart(@PathVariable Long id) {
        cartRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/clear")
    public ResponseEntity<?> clearCart(Principal principal) {
        List<CartItem> items = cartRepository.findByUserEmail(principal.getName());
        cartRepository.deleteAll(items);
        return ResponseEntity.ok().build();
    }
}