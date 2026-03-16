package com.ecommerce.lab.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.ecommerce.lab.dto.CartItemResponseDTO;
import com.ecommerce.lab.dto.GiftCardRequest;
import com.ecommerce.lab.exception.BusinessLogicException;
import com.ecommerce.lab.exception.ProductNotFoundException;
import com.ecommerce.lab.exception.ResourceNotFoundException;
import com.ecommerce.lab.model.CartItem;
import com.ecommerce.lab.model.Product;
import com.ecommerce.lab.model.User;
import com.ecommerce.lab.repository.base.CartRepository;
import com.ecommerce.lab.repository.base.ProductRepository;
import com.ecommerce.lab.repository.base.UserRepository;

import org.springframework.transaction.annotation.Transactional;;

@Service
public class CartService {
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public CartService(
        CartRepository cartRepository, ProductRepository productRepository,
        UserRepository userRepository
    ) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void addItemToCart(Long productId, String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ProductNotFoundException("Product not found"));

        if (product.getStock() == 0) {
            throw new BusinessLogicException("Product is out of stock");
        }

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

    public void addGiftCardToCart(GiftCardRequest request, String email) {
        User user = userRepository.findByEmail(email).get();

        CartItem item = new CartItem();
        item.setUser(user);
        item.setGiftCard(true);
        item.setGiftCardAmount(request.amount());
        item.setRecipientEmail(request.recipientEmail());
        item.setGiftCardMessage(request.message());
        item.setQuantity(1); // Usually 1 per unique recipient

        cartRepository.save(item);
    }

    public List<CartItemResponseDTO> getCartItems(String email) {
        List<CartItem> entities = cartRepository.findAllByUserEmail(email);

        return entities.stream()
            .map(CartItemResponseDTO::fromEntity)
            .toList();
    }
}