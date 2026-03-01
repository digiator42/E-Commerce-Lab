package com.ecommerce.lab.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.lab.model.GiftCard;

public interface GiftCardRepository extends JpaRepository<GiftCard, Long> {

}
