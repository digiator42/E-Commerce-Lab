package com.ecommerce.lab.controller;

import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

import com.ecommerce.lab.model.GiftCard;
import com.ecommerce.lab.repository.GiftCardRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/gift-cards")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminGiftCardController {

    private final GiftCardRepository giftCardRepository;

    @PostMapping
    public ResponseEntity<GiftCard> createGiftCard(@RequestBody GiftCard giftCard) {
        // Generate a random 12-char code if none provided
        if (giftCard.getCode() == null || giftCard.getCode().isBlank()) {
            giftCard.setCode(UUID.randomUUID().toString().substring(0, 13).toUpperCase());
        }

        giftCard.setBalance(giftCard.getInitialAmount()); // Balance starts at full
        giftCard.setActive(true);

        return ResponseEntity.status(HttpStatus.CREATED).body(giftCardRepository.save(giftCard));
    }
}