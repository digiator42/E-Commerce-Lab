package com.ecommerce.lab.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.ecommerce.lab.model.CartItem;
import com.ecommerce.lab.model.Order;
import com.ecommerce.lab.model.OrderItem;
import com.ecommerce.lab.model.OrderStatus;
import com.ecommerce.lab.model.User;
import com.ecommerce.lab.repository.CartRepository;
import com.ecommerce.lab.repository.OrderRepository;
import com.ecommerce.lab.repository.UserRepository;

import org.springframework.transaction.annotation.Transactional;;

@Service
public class OrderService {

    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public OrderService(CartRepository cartRepository, OrderRepository orderRepository, UserRepository userRepository) {
        this.cartRepository = cartRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void placeOrder(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<CartItem> cartItems = cartRepository.findAllByUserEmail(email);
        if (cartItems.isEmpty())
            throw new RuntimeException("Cart is empty");

        String fakeTransactionId = "FAKE-TX-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Order order = new Order();
        order.setUser(cartItems.get(0).getUser());
        order.setOrderDate(LocalDateTime.now());

        if (user.getAddress() == null || user.getAddress().isBlank()) {
            throw new RuntimeException("Please set a shipping address in your profile before checkout.");
        }
        order.setShippingAddress(user.getAddress());

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