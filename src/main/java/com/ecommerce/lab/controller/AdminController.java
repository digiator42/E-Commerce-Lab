package com.ecommerce.lab.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

import com.ecommerce.lab.model.Order;
import com.ecommerce.lab.model.Product;
import com.ecommerce.lab.repository.OrderRepository;
import com.ecommerce.lab.repository.ProductRepository;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;


    public AdminController(ProductRepository productRepository, OrderRepository orderRepository) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getDashboardStats() {
        long totalProducts = productRepository.count();
        long totalOrders = orderRepository.count();
        // Summing all order totals for revenue
        double totalRevenue = orderRepository.findAll().stream()
                .mapToDouble(Order::getTotalAmount).sum();

        return ResponseEntity.ok(Map.of(
                "products", totalProducts,
                "orders", totalOrders,
                "revenue", totalRevenue));
    }

    @PostMapping("/products")
    public ResponseEntity<Product> addProduct(@RequestBody Product product) {
        return ResponseEntity.ok(productRepository.save(product));
    }
}