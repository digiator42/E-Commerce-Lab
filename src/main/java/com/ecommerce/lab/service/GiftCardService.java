package com.ecommerce.lab.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.ecommerce.lab.dto.GiftCardRequest;
import com.ecommerce.lab.model.GiftCard;
import com.ecommerce.lab.model.User;
import com.ecommerce.lab.repository.base.GiftCardRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GiftCardService {

    private final GiftCardRepository giftCardRepository;
    private final EmailService emailService;

    public Double purchaseMultiGiftCard(List<GiftCardRequest> giftCards, User buyer) {
        // Process Gift Cards
        double giftCardTotal = 0;
        if (giftCards != null && !giftCards.isEmpty()) {
            for (GiftCardRequest purchase : giftCards) {
                giftCardTotal += purchase.amount();
                this.purchaseGiftCard(purchase, buyer);
            }
        }

        return giftCardTotal;
    }

    public void purchaseGiftCard(GiftCardRequest purchase, User buyer) {

        // Generate the Gift Card
        GiftCard giftCard = new GiftCard();
        giftCard.setCode(generateSecureCode());
        giftCard.setInitialAmount(purchase.amount());
        giftCard.setBalance(purchase.amount());
        giftCard.setRecipientEmail(purchase.recipientEmail()); // Can be self or a friend
        giftCard.setMessage(purchase.message());
        giftCard.setExpiryDate(LocalDateTime.now().plusYears(1));
        giftCard.setActive(true);

        giftCardRepository.save(giftCard);

        // Send the code via Email
        emailService
            .sendGiftCardCode(giftCard.getRecipientEmail(), giftCard.getCode(), giftCard.getInitialAmount(), buyer.getName());

    }

    private String generateSecureCode() {
        // Generates a format like: XXXX-XXXX-XXXX
        return "GIFT-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase()
            .replaceAll("(.{4})(?!$)", "$1-");
    }
}
