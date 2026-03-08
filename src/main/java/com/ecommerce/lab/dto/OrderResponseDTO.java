package com.ecommerce.lab.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.ecommerce.lab.model.Order;

public record OrderResponseDTO(
        Long id,
        LocalDateTime orderDate,
        String paymentStatus,
        String paymentTransactionId,
        String shippingAddress,
        Double totalAmount,
        List<OrderItemDTO> items) {

    public static OrderResponseDTO fromEntity(Order order) {
        List<OrderItemDTO> oiDTOs = order.getItems().stream().map(OrderItemDTO::fromEntity).toList();

        return new OrderResponseDTO(
                order.getId(),
                order.getOrderDate(),
                order.getPaymentStatus(),
                order.getPaymentTransactionId(),
                order.getShippingAddress(),
                order.getTotalAmount(),
                oiDTOs);
    }
}