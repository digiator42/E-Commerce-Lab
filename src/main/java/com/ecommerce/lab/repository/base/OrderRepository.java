package com.ecommerce.lab.repository.base;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;

import com.ecommerce.lab.model.Order;

import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface OrderRepository extends JpaRepository<Order, Long> {

    @EntityGraph(value = "Order.fullDetails")
    List<Order> findAll(Sort sort);

    @EntityGraph(attributePaths = {
            "items",
            "items.product",
            "items.product.category"
    })
    Order getOrderById(Long id);

    @EntityGraph(value = "Order.fullDetails")
    List<Order> findByUserEmailOrderByOrderDateDesc(@Param("email") String email);

    @Query("SELECT DISTINCT i.product.id FROM Order o JOIN o.items i " +
        "WHERE o.user.email = :email AND i.product.id IN :ids")
    Set<Long> findPurchasedProductIds(@Param("email") String email, @Param("ids") List<Long> ids);

    @EntityGraph(value = "Order.fullDetails")
    boolean existsByUserEmailAndItemsProductId(String email, Long productId);
}
