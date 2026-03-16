package com.ecommerce.lab.repository.base;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.lab.model.Coupon;

import org.springframework.data.repository.NoRepositoryBean;
@NoRepositoryBean
public interface CouponRepository extends JpaRepository<Coupon, Long> {

    Optional<Coupon> findByCode(String couponCode);

    Boolean existsByCode(String couponCode);
}
