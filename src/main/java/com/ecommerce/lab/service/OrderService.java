package com.ecommerce.lab.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.ecommerce.lab.model.CartItem;
import com.ecommerce.lab.model.Coupon;
import com.ecommerce.lab.model.Order;
import com.ecommerce.lab.model.OrderItem;
import com.ecommerce.lab.model.OrderStatus;
import com.ecommerce.lab.model.User;
import com.ecommerce.lab.repository.CartRepository;
import com.ecommerce.lab.repository.CouponRepository;
import com.ecommerce.lab.repository.OrderRepository;
import com.ecommerce.lab.repository.UserRepository;

import org.springframework.transaction.annotation.Transactional;;

@Service
public class OrderService {

    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final CouponRepository couponRepository;

    public OrderService(CartRepository cartRepository, OrderRepository orderRepository, UserRepository userRepository,
            EmailService emailService, CouponRepository couponRepository) {
        this.cartRepository = cartRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.couponRepository = couponRepository;
    }

    @Transactional(rollbackFor = Exception.class)
    public void placeOrder(String email, String couponCode) throws Exception {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<CartItem> cartItems = cartRepository.findAllByUserEmail(email);

        if (cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        // Core Validation
        this.validateShippingAddress(user);

        // Process Items & Stock
        double subtotal = this.processItemsAndStock(cartItems);

        // Apply Discount
        double finalTotal = this.applyCoupon(subtotal, couponCode);

        // Persistence
        Order savedOrder = this.createAndSaveOrder(user, cartItems, finalTotal);
        cartRepository.deleteAll(cartItems);

        // Async/External Tasks (Emails)
        this.sendNotifications(savedOrder, user);
    }

    private void validateShippingAddress(User user) {
        if (user.getAddress() == null || user.getAddress().isBlank()) {
            throw new RuntimeException("Please set a shipping address in your profile before checkout.");
        }
    }

    private double processItemsAndStock(List<CartItem> cartItems) {
        double subtotal = 0;
        for (CartItem ci : cartItems) {
            if (ci.getProduct().getStock() < ci.getQuantity()) {
                throw new RuntimeException("Insufficient stock for " + ci.getProduct().getName());
            }

            // Reduce stock
            ci.getProduct().setStock(ci.getProduct().getStock() - ci.getQuantity());
            subtotal += (ci.getProduct().getPrice() * ci.getQuantity());
        }
        return subtotal;
    }

    private double applyCoupon(double total, String couponCode) {
        if (couponCode == null || couponCode.isBlank()) {
            return total;
        }

        Coupon coupon = couponRepository.findByCode(couponCode)
                .orElseThrow(() -> new RuntimeException("Coupon not found"));

        validateCoupon(coupon);

        double discount = total * (coupon.getDiscountPercentage() / 100);

        // Update coupon usage
        coupon.setTimesUsed(coupon.getTimesUsed() + 1);
        couponRepository.save(coupon);

        return total - discount;
    }

    private Order createAndSaveOrder(User user, List<CartItem> cartItems, double finalTotal) {
        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setShippingAddress(user.getAddress());
        order.setTotalAmount(Math.round(finalTotal * 100.0) / 100.0);
        order.setPaymentStatus("PAID");
        order.setStatus(OrderStatus.COMPLETED);
        order.setPaymentTransactionId("FAKE-TX-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());

        // Map CartItems to OrderItems
        for (CartItem ci : cartItems) {
            OrderItem oi = new OrderItem();
            oi.setProduct(ci.getProduct());
            oi.setProductName(ci.getProduct().getName());
            oi.setPriceAtPurchase(ci.getProduct().getPrice());
            oi.setQuantity(ci.getQuantity());
            order.getItems().add(oi);
        }

        return orderRepository.save(order);
    }

    private void sendNotifications(Order order, User user) {
        // Send to customer
        emailService.sendOrderConfirmationWithInvoice(order);

        // Send to admin
        emailService.sendSimpleEmail(
                "admin@admin.com",
                "New Order Received!",
                "Order #" + order.getId() + " was placed by " + user.getEmail());
    }

    public void validateCoupon(Coupon coupon) {
        if (!coupon.isActive()) {
            throw new RuntimeException("Coupon is disabled.");
        }
        if (coupon.getExpiryDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("Coupon has expired.");
        }
        if (coupon.getTimesUsed() >= coupon.getUsageLimit()) {
            throw new RuntimeException("Coupon usage limit reached.");
        }
    }

    public boolean hasUserPurchasedProduct(String email, Long productId) {
        return orderRepository.existsByUserEmailAndItemsProductId(email, productId);
    }
}