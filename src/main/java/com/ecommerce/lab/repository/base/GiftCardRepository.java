package com.ecommerce.lab.repository.base;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.lab.model.GiftCard;


import org.springframework.data.repository.NoRepositoryBean;
@NoRepositoryBean
public interface GiftCardRepository extends JpaRepository<GiftCard, Long> {
    
    Optional<GiftCard> findByCode(String code);
}
