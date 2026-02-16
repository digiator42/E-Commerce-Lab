package com.ecommerce.lab.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.ecommerce.lab.dto.CartItemResponseDTO;
import com.ecommerce.lab.model.CartItem;
import com.ecommerce.lab.model.Product;
import com.ecommerce.lab.model.User;
import com.ecommerce.lab.repository.CartRepository;
import com.ecommerce.lab.repository.ProductRepository;
import com.ecommerce.lab.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class CartService {
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public CartService(CartRepository cartRepository, ProductRepository productRepository,
            UserRepository userRepository) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void addItemToCart(Long productId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Optional<CartItem> existingItem = cartRepository.findByUserAndProduct(user, product);

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + 1);
            cartRepository.save(item);
        } else {
            CartItem newItem = new CartItem();
            newItem.setUser(user);
            newItem.setProduct(product);
            newItem.setQuantity(1);
            cartRepository.save(newItem);
        }
    }
    
    public List<CartItemResponseDTO> getCartItems(String email) {
        return cartRepository.findByUserEmail(email);
    }
}