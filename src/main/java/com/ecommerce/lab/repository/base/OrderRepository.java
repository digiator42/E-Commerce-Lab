package com.ecommerce.lab.repository.base;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ecommerce.lab.model.Order;

import org.springframework.data.repository.NoRepositoryBean;
@NoRepositoryBean
public interface OrderRepository extends JpaRepository<Order, Long> {
    Order getOrderById(Long id);
    @Query("SELECT DISTINCT o FROM Order o " +
       "LEFT JOIN FETCH o.items i " +
       "LEFT JOIN FETCH i.product " + // This ensures the product is loaded
       "WHERE o.user.email = :email " +
       "ORDER BY o.orderDate DESC")
    List<Order> findByUserEmailOrderByOrderDateDesc(@Param("email") String email);

    boolean existsByUserEmailAndItemsProductId(String email, Long productId);
}
