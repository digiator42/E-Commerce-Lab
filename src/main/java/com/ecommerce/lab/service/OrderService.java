package com.ecommerce.lab.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.ecommerce.lab.model.CartItem;
import com.ecommerce.lab.model.Order;
import com.ecommerce.lab.model.OrderItem;
import com.ecommerce.lab.model.OrderStatus;
import com.ecommerce.lab.repository.CartRepository;
import com.ecommerce.lab.repository.OrderRepository;

import org.springframework.transaction.annotation.Transactional;;

@Service
public class OrderService {

    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;

    public OrderService(CartRepository cartRepository, OrderRepository orderRepository) {
        this.cartRepository = cartRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional
    public void placeOrder(String email) {
        List<CartItem> cartItems = cartRepository.findAllByUserEmail(email);
        if (cartItems.isEmpty())
            throw new RuntimeException("Cart is empty");

        String fakeTransactionId = "FAKE-TX-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Order order = new Order();
        order.setUser(cartItems.get(0).getUser());
        order.setOrderDate(LocalDateTime.now());

        double total = 0;
        for (CartItem ci : cartItems) {

            OrderItem oi = new OrderItem();

            oi.setProductName(ci.getProduct().getName());
            oi.setPriceAtPurchase(ci.getProduct().getPrice());
            oi.setQuantity(ci.getQuantity());

            if (ci.getProduct().getStock() < ci.getQuantity()) {
                throw new RuntimeException("Insufficient stock for " + ci.getProduct().getName());
            }
            ci.getProduct().setStock(ci.getProduct().getStock() - ci.getQuantity());

            order.getItems().add(oi);
            total += (ci.getProduct().getPrice() * ci.getQuantity());
        }
        order.setTotalAmount(total);

        order.setPaymentTransactionId(fakeTransactionId);
        order.setPaymentStatus("PAID");
        order.setStatus(OrderStatus.COMPLETED);

        orderRepository.save(order);
        cartRepository.deleteAll(cartItems);
    }
}