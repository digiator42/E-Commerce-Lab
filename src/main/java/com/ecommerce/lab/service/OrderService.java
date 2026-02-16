package com.ecommerce.lab.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.ecommerce.lab.model.CartItem;
import com.ecommerce.lab.model.Order;
import com.ecommerce.lab.model.OrderItem;
import com.ecommerce.lab.repository.CartRepository;
import com.ecommerce.lab.repository.OrderRepository;

import jakarta.transaction.Transactional;

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

        Order order = new Order();
        order.setUser(cartItems.get(0).getUser());
        order.setOrderDate(LocalDateTime.now());

        double total = 0;
        for (CartItem ci : cartItems) {
            OrderItem oi = new OrderItem();
            oi.setProductName(ci.getProduct().getName());
            oi.setPriceAtPurchase(ci.getProduct().getPrice());
            oi.setQuantity(ci.getQuantity());
            order.getItems().add(oi);
            total += (ci.getProduct().getPrice() * ci.getQuantity());
        }
        order.setTotalAmount(total);

        orderRepository.save(order);
        cartRepository.deleteAll(cartItems);
    }
}