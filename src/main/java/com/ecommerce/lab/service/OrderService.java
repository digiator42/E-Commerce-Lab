package com.ecommerce.lab.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.ecommerce.lab.dto.GiftCardRequest;
import com.ecommerce.lab.model.BalanceTransaction;
import com.ecommerce.lab.model.CartItem;
import com.ecommerce.lab.model.Coupon;
import com.ecommerce.lab.model.GiftCard;
import com.ecommerce.lab.model.Order;
import com.ecommerce.lab.model.OrderItem;
import com.ecommerce.lab.model.OrderStatus;
import com.ecommerce.lab.model.User;
import com.ecommerce.lab.repository.BalanceTransactionRepository;
import com.ecommerce.lab.repository.CartRepository;
import com.ecommerce.lab.repository.CouponRepository;
import com.ecommerce.lab.repository.GiftCardRepository;
import com.ecommerce.lab.repository.OrderRepository;
import com.ecommerce.lab.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.transaction.annotation.Transactional;;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final CouponRepository couponRepository;
    private final GiftCardService giftCardService;
    private final GiftCardRepository giftCardRepository;
    private final BalanceTransactionRepository balanceTransactionRepository;

    @Transactional(rollbackFor = Exception.class)
    public void placeOrder(String email, String couponCode, boolean useStoreBalance) throws Exception {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<CartItem> cartItems = cartRepository.findAllByUserEmail(email);

        if (cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        // Core Validation
        this.validateShippingAddress(user);

        // Process Items & Stock
        OrderBreakdown breakdown = this.processItemsAndStock(cartItems, email);

        // Apply Discount only to physical items
        double discountedPhysical = this.applyCoupon(breakdown.physicalTotal(), couponCode);

        // Apply Store Balance ONLY to the physical discounted total
        double remainingPhysical = discountedPhysical;
        double amountToDeduct = 0;
        if (useStoreBalance && user.getStoreBalance() > 0) {
            amountToDeduct = Math.min(discountedPhysical, user.getStoreBalance());
            remainingPhysical = discountedPhysical - amountToDeduct;

            // Deduct from user profile
            user.setStoreBalance(user.getStoreBalance() - amountToDeduct);

            userRepository.save(user);
        }

        double finalTotal = remainingPhysical + breakdown.giftCardTotal();

        // Persistence
        Order savedOrder = this.createAndSaveOrder(user, cartItems, finalTotal);

        if (useStoreBalance && user.getStoreBalance() > 0) {
            this.createUsageTX(user, amountToDeduct, savedOrder);
        }

        cartRepository.deleteAll(cartItems);

        // Async/External Tasks (Emails)
        this.sendNotifications(savedOrder, user);
    }

    private void createUsageTX(User user, double amountToDeduct, Order savedOrder) {
        BalanceTransaction tx = new BalanceTransaction();
        tx.setUser(user);

        // This is a negative amount because they are SPENDING
        tx.setAmount(-amountToDeduct);

        // Instead of a card code, we use the Order ID or Transaction ID
        tx.setCode("ORDER-" + savedOrder.getId());

        tx.setDate(LocalDateTime.now());
        tx.setType("PURCHASE");

        balanceTransactionRepository.save(tx);
    }

    private void validateShippingAddress(User user) {
        if (user.getAddress() == null || user.getAddress().isBlank()) {
            throw new RuntimeException("Please set a shipping address in your profile before checkout.");
        }
    }

    public record OrderBreakdown(double physicalTotal, double giftCardTotal) {
        public double getGrandTotal() {
            return physicalTotal + giftCardTotal;
        }
    }

    private OrderBreakdown processItemsAndStock(List<CartItem> cartItems, String email) {
        double physicalTotal = 0;
        double giftCardTotal = 0;

        for (CartItem ci : cartItems) {
            if (ci.isGiftCard()) {
                giftCardTotal += ci.getGiftCardAmount();
                this.generateAndEmailGiftCard(ci, email);
            } else {
                if (ci.getProduct().getStock() < ci.getQuantity()) {
                    throw new RuntimeException("Insufficient stock for " + ci.getProduct().getName());
                }
                ci.getProduct().setStock(ci.getProduct().getStock() - ci.getQuantity());
                physicalTotal += (ci.getProduct().getPrice() * ci.getQuantity());
            }
        }
        return new OrderBreakdown(physicalTotal, giftCardTotal);
    }

    private void generateAndEmailGiftCard(CartItem ci, String buyerEmail) {

        GiftCard gc = new GiftCard();
        gc.setCode(UUID.randomUUID().toString().substring(0, 12).toUpperCase());
        gc.setBalance(ci.getGiftCardAmount());
        gc.setInitialAmount(ci.getGiftCardAmount());
        gc.setRecipientEmail(ci.getRecipientEmail());
        gc.setExpiryDate(LocalDateTime.now().plusYears(1));
        gc.setActive(true);

        giftCardRepository.save(gc);

        emailService.sendGiftCardCode(
                ci.getRecipientEmail(),
                gc.getCode(),
                ci.getGiftCardMessage());
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

            if (ci.isGiftCard()) {
                // Handle Virtual Item (Gift Card)
                oi.setProduct(null); // No physical product link
                oi.setProductName("Digital Gift Card (To: " + ci.getRecipientEmail() + ")");
                oi.setPriceAtPurchase(ci.getGiftCardAmount());
                oi.setQuantity(ci.getQuantity());
            } else {
                // Handle Physical Product
                oi.setProduct(ci.getProduct());
                oi.setProductName(ci.getProduct().getName());
                oi.setPriceAtPurchase(ci.getProduct().getPrice());
                oi.setQuantity(ci.getQuantity());
            }

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