package com.ecommerce.lab.controller;

import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.lab.dto.OrderRequest;
import com.ecommerce.lab.dto.OrderResponseDTO;
import com.ecommerce.lab.model.Order;
import com.ecommerce.lab.repository.base.OrderRepository;
import com.ecommerce.lab.service.InvoiceService;
import com.ecommerce.lab.service.OrderService;

import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final InvoiceService invoiceService;

    public OrderController(
        OrderService orderService, OrderRepository orderRepository,
        InvoiceService invoiceService
    ) {
        this.orderService = orderService;
        this.orderRepository = orderRepository;
        this.invoiceService = invoiceService;
    }

    @PostMapping("/place")
    public ResponseEntity<?> placeOrder(
        Authentication authentication,
        @RequestBody OrderRequest orderRequest
    ) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Please login to checkout.");
        }

        try {
            orderService.placeOrder(
                authentication.getName(),
                orderRequest.couponCode(),
                orderRequest.useStoreBalance(),
                orderRequest.shippingAddress(),
                orderRequest.giftCards()
            );

            return ResponseEntity.ok(Map.of("message", "Order successfully created"));
        } catch (RuntimeException e) {
            System.out.println("Order failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "An unexpected error occurred"));
        }
    }

    @GetMapping("/my-orders")
    @Transactional(readOnly = true)
    public ResponseEntity<List<OrderResponseDTO>> getMyOrders(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }

        List<Order> orders = orderRepository
            .findByUserEmailOrderByOrderDateDesc(principal.getName());
        return ResponseEntity.ok(orders.stream().map(OrderResponseDTO::fromEntity).toList());
    }

    @GetMapping("/{orderId}/download-invoice")
    public void downloadInvoice(@PathVariable Long orderId, HttpServletResponse response)
        throws IOException {

        response.setContentType("application/pdf");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=invoice_order_" + orderId + ".pdf";
        response.setHeader(headerKey, headerValue);

        invoiceService.generateInvoice(orderId, response.getOutputStream());
    }
}
