package com.ecommerce.lab.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.ecommerce.lab.dto.GiftCardRequest;
import com.ecommerce.lab.exception.BusinessLogicException;
import com.ecommerce.lab.exception.ResourceNotFoundException;
import com.ecommerce.lab.model.Address;
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
import tools.jackson.databind.ObjectMapper;

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

    private double remainingPhysical;
    private double discountedPhysical;
    private double amountToDeduct;
    private double finalTotal;

    private record OrderBreakdown(
        double physicalTotal,
        double giftCardTotal
    ) {
    }

    @Transactional(rollbackFor = Exception.class)
    public void placeOrder(
        String email,
        String couponCode,
        boolean useStoreBalance,
        String shippingAddress,
        List<GiftCardRequest> giftCards
    )
        throws Exception {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<CartItem> cartItems = cartRepository.findAllByUserEmail(email);
        // To be linked to orderitem
        List<GiftCard> internalGiftCards = new ArrayList<>();

        if (cartItems.isEmpty()) {
            throw new BusinessLogicException("Cart is empty");
        }

        // Validate and set new address
        Address address = handleAddress(shippingAddress, user);

        // Process Items & Stock
        OrderBreakdown breakdown = this.processItemsAndStock(cartItems, internalGiftCards, giftCards, email);

        // Apply Discount only to physical items
        this.discountedPhysical = this.applyCoupon(breakdown.physicalTotal(), couponCode);

        // Apply Store Balance ONLY to the physical discounted total
        this.handleUserStoreBalance(useStoreBalance, user);

        this.finalTotal = this.remainingPhysical + breakdown.giftCardTotal();

        // Persistence
        Order savedOrder = this.createAndSaveOrder(
            user, shippingAddress, address, cartItems, internalGiftCards
        );

        if (useStoreBalance && user.getStoreBalance() > 0) {
            if (amountToDeduct != 0)
                this.createUsageTX(user, amountToDeduct, savedOrder);
        }

        cartRepository.deleteAll(cartItems);

        // Async (Emails)
        this.sendNotifications(savedOrder, user);
    }

    private void handleUserStoreBalance(boolean useStoreBalance, User user) {
        this.remainingPhysical = this.discountedPhysical;

        if (useStoreBalance && user.getStoreBalance() > 0) {
            this.amountToDeduct = Math.min(this.discountedPhysical, user.getStoreBalance());
            this.remainingPhysical = this.discountedPhysical - amountToDeduct;

            // Deduct from user profile
            user.setStoreBalance(user.getStoreBalance() - amountToDeduct);

            userRepository.save(user);
        }
    }

    private Address handleAddress(String shippingAddress, User user) {
        Address address = null;
        // Core Validation
        if (shippingAddress == null) {
            this.validateShippingAddress(user);
        } else {
            ObjectMapper mapper = new ObjectMapper();
            address = mapper.readValue(shippingAddress, Address.class);

            // Save default address if not
            if (user.getAddress() == null || user.getAddress().isBlank()) {
                user.setAddress(shippingAddress);
            }
            // Add address to list of addresses
            address.setUser(user);
            user.getAddressObjects().add(address);
            userRepository.save(user);
        }
        return address;
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
            throw new BusinessLogicException(
                "Please set a shipping address in your profile before checkout."
            );
        }
    }

    private OrderBreakdown processItemsAndStock(
        List<CartItem> cartItems,
        List<GiftCard> internalGiftCards,
        List<GiftCardRequest> gcRequest,
        String email
    ) {
        double physicalTotal = 0;
        double giftCardTotal = 0;

        int gcIndex = 0;

        for (CartItem ci : cartItems) {
            if (ci.isGiftCard()) {
                giftCardTotal += ci.getGiftCardAmount();
                String giftedEmail = gcRequest.get(gcIndex++).recipientEmail();
                internalGiftCards.add(this.generateAndEmailGiftCard(ci, giftedEmail));
            } else {
                if (ci.getProduct().getStock() < ci.getQuantity()) {
                    throw new BusinessLogicException(
                        "Insufficient stock for " + ci.getProduct().getName()
                    );
                }
                ci.getProduct().setStock(ci.getProduct().getStock() - ci.getQuantity());
                physicalTotal += (ci.getProduct().getPrice() * ci.getQuantity());
            }
        }
        return new OrderBreakdown(physicalTotal, giftCardTotal);
    }

    private GiftCard generateAndEmailGiftCard(CartItem ci, String recipientEmail) {

        GiftCard gc = new GiftCard();
        gc.setName("Digital Gift Card (To: " + recipientEmail + ")");
        gc.setCode(UUID.randomUUID().toString().substring(0, 12).toUpperCase());
        gc.setBalance(ci.getGiftCardAmount());
        gc.setInitialAmount(ci.getGiftCardAmount());
        gc.setRecipientEmail(recipientEmail);
        gc.setExpiryDate(LocalDateTime.now().plusYears(1));
        gc.setActive(true);

        gc = giftCardRepository.save(gc);

        emailService.sendGiftCardCode(
            recipientEmail,
            gc.getCode(),
            gc.getInitialAmount(),
            ci.getUser().getName()
        );

        return gc;
    }

    private double applyCoupon(double total, String couponCode) {
        if (couponCode == null || couponCode.isBlank()) {
            return total;
        }

        Coupon coupon = couponRepository.findByCode(couponCode)
            .orElseThrow(() -> new BusinessLogicException("Coupon not found"));

        validateCoupon(coupon);

        double discount = total * (coupon.getDiscountPercentage() / 100);

        // Update coupon usage
        coupon.setTimesUsed(coupon.getTimesUsed() + 1);
        couponRepository.save(coupon);

        return total - discount;
    }

    private Order createAndSaveOrder(
        User user,
        String shippingAddress,
        Address address,
        List<CartItem> cartItems,
        List<GiftCard> internalGiftCards
    ) {
        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        // A snapshot of shipping address, current order address
        order.setShippingAddress(shippingAddress != null ? shippingAddress : address.toString());
        order.setTotalAmount(Math.round(this.finalTotal * 100.0) / 100.0);
        order.setPaymentStatus("PAID");
        order.setStatus(OrderStatus.COMPLETED);
        order.setPaymentTransactionId(
            "FAKE-TX-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase()
        );

        // Map CartItems to OrderItems
        int gcIndex = 0;
        for (CartItem ci : cartItems) {
            OrderItem oi = new OrderItem();

            if (ci.isGiftCard()) {
                oi.setProduct(null); // No physical product link
                oi.setGiftCard(internalGiftCards.get(gcIndex));
                oi.setProductName("Digital Gift Card (To: " + internalGiftCards.get(gcIndex++).getRecipientEmail() + ")");
                oi.setPriceAtPurchase(ci.getGiftCardAmount());
                oi.setQuantity(ci.getQuantity());
            } else {
                // Handle Physical Product
                oi.setProduct(ci.getProduct());
                oi.setProductName(ci.getProduct().getName());
                oi.setPriceAtPurchase(ci.getProduct().getPrice());
                oi.setQuantity(ci.getQuantity());
            }
            oi.setOrder(order);
            order.getItems().add(oi);
        }

        return orderRepository.save(order);
    }

    private void sendNotifications(Order order, User user) {
        // Send to customer
        emailService.sendOrderConfirmationWithInvoice(order);

        // Send to admin
        // emailService.sendSimpleEmail(
        // "admin@admin.com",
        // "New Order Received!",
        // "Order #" + order.getId() + " was placed by " + user.getEmail()
        // );
    }

    public void validateCoupon(Coupon coupon) {
        if (!coupon.isActive()) {
            throw new BusinessLogicException("Coupon is disabled.");
        }
        if (coupon.getExpiryDate().isBefore(LocalDate.now())) {
            throw new BusinessLogicException("Coupon has expired.");
        }
        if (coupon.getTimesUsed() >= coupon.getUsageLimit()) {
            throw new BusinessLogicException("Coupon usage limit reached.");
        }
    }

    public boolean hasUserPurchasedProduct(String email, Long productId) {
        return orderRepository.existsByUserEmailAndItemsProductId(email, productId);
    }
}