package com.ecommerce.lab.controller;

import java.security.Principal;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.lab.exception.ProductNotFoundException;
import com.ecommerce.lab.model.Coupon;
import com.ecommerce.lab.repository.CouponRepository;
import com.ecommerce.lab.service.OrderService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class CouponController {
    private final CouponRepository couponRepository;
    private final OrderService orderService;

    @GetMapping("/check")
    public ResponseEntity<?> checkCoupon(@RequestParam String code, Principal principal) {
        Coupon coupon = couponRepository.findByCode(code)
                .orElseThrow(() -> new ProductNotFoundException("Coupon not found"));

        // Call the same helper function used in placeOrder
        orderService.validateCoupon(coupon);

        return ResponseEntity.ok(Map.of(
                "code", coupon.getCode(),
                "discountPercentage", coupon.getDiscountPercentage(),
                "message", "Coupon valid!"));
    }
}
