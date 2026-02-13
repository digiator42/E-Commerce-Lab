package com.ecommerce.lab.controller;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.lab.model.Order;
import com.ecommerce.lab.repository.OrderRepository;
import com.ecommerce.lab.service.OrderService;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final OrderRepository orderRepository;

    public OrderController(OrderService orderService, OrderRepository orderRepository) {
        this.orderService = orderService;
        this.orderRepository = orderRepository;
    }

    @PostMapping("/place")
    public ResponseEntity<?> placeOrder(Principal principal) {

        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Please login to checkout.");
        }

        try {
            orderService.placeOrder(principal.getName());
            return ResponseEntity.ok(Map.of("message", "Order successfully created"));
        } catch (RuntimeException e) {
            // Cart is empty
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/my-orders")
    public ResponseEntity<List<Order>> getMyOrders(Principal principal) {
        if (principal == null)
            return ResponseEntity.status(401).build();

        List<Order> orders = orderRepository.findByUserEmailOrderByOrderDateDesc(principal.getName());
        return ResponseEntity.ok(orders);
    }
}