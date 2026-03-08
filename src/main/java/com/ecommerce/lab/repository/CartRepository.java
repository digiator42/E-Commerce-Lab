package com.ecommerce.lab.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ecommerce.lab.dto.CartItemResponseDTO;
import com.ecommerce.lab.model.CartItem;
import com.ecommerce.lab.model.Product;
import com.ecommerce.lab.model.User;

@Repository
public interface CartRepository extends JpaRepository<CartItem, Long> {
    List<CartItemResponseDTO> findByUserEmail(String email);

    List<CartItem> findAllByUserEmail(String email);
    
    Optional<CartItem> findByUserAndProduct(User user, Product product);
}