package com.ecommerce.lab.repository.base;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import com.ecommerce.lab.dto.CartItemResponseDTO;
import com.ecommerce.lab.model.CartItem;
import com.ecommerce.lab.model.Product;
import com.ecommerce.lab.model.User;

@NoRepositoryBean
public interface CartRepository extends JpaRepository<CartItem, Long> {
    @EntityGraph(attributePaths = {"product", "user"})
    List<CartItemResponseDTO> findByUserEmail(String email);

    @EntityGraph(attributePaths = {"product", "user"})
    List<CartItem> findAllByUserEmail(String email);
    
    @EntityGraph(attributePaths = {"product", "user"})
    Optional<CartItem> findByUserAndProduct(User user, Product product);
}