package com.ecommerce.lab.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.ecommerce.lab.dto.ProductResponseDTO;
import com.ecommerce.lab.model.Product;
import com.ecommerce.lab.model.User;
import com.ecommerce.lab.model.WishlistItem;
import com.ecommerce.lab.repository.base.ProductRepository;
import com.ecommerce.lab.repository.base.UserRepository;
import com.ecommerce.lab.repository.base.WishlistRepository;

@Service
public class WishlistService {
    private final WishlistRepository wishlistRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public WishlistService(
        WishlistRepository wishlistRepository,
        ProductRepository productRepository,
        UserRepository userRepository
    ) {

        this.wishlistRepository = wishlistRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    public void addToWishlist(Long productId, String email) {
        if (wishlistRepository.existsByUserEmailAndProductId(email, productId)) {
            return;
        }

        User user = userRepository.findByEmail(email).orElseThrow();
        Product product = productRepository.findById(productId).orElseThrow();

        WishlistItem item = new WishlistItem();
        item.setUser(user);
        item.setProduct(product);
        wishlistRepository.save(item);
    }

    public List<ProductResponseDTO> getWishlist(String email) {
        return wishlistRepository.findByUserEmail(email).stream()
            .map(item -> ProductResponseDTO.fromEntity(item.getProduct()))
            .toList();
    }
}
