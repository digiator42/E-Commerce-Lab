package com.ecommerce.lab.controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.lab.dto.GiftCardRequest;
import com.ecommerce.lab.model.GiftCard;
import com.ecommerce.lab.model.User;
import com.ecommerce.lab.repository.GiftCardRepository;
import com.ecommerce.lab.repository.UserRepository;
import com.ecommerce.lab.service.EmailService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/gift-cards")
@RequiredArgsConstructor
public class UserGiftCardController {

    private final UserRepository userRepository;
    private final GiftCardRepository giftCardRepository;
    private final EmailService emailService;

    @PostMapping("/purchase")
    public ResponseEntity<?> purchaseGiftCard(@RequestBody GiftCardRequest request, Principal principal) {
        User buyer = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Generate the Gift Card
        GiftCard giftCard = new GiftCard();
        giftCard.setCode(generateSecureCode());
        giftCard.setInitialAmount(request.amount());
        giftCard.setBalance(request.amount());
        giftCard.setRecipientEmail(request.recipientEmail()); // Can be self or a friend
        giftCard.setMessage(request.message());
        giftCard.setExpiryDate(LocalDateTime.now().plusYears(1));
        giftCard.setActive(true);

        giftCardRepository.save(giftCard);

        // Send the code via Email
        emailService.sendGiftCardCode(giftCard.getRecipientEmail(), giftCard.getCode(), buyer.getName());

        return ResponseEntity.ok(Map.of(
                "message", "Gift card purchased successfully!",
                "codeSentTo", giftCard.getRecipientEmail()));
    }

    private String generateSecureCode() {
        // Generates a format like: XXXX-XXXX-XXXX
        return UUID.randomUUID().toString().substring(0, 12).toUpperCase()
                .replaceAll("(.{4})(?!$)", "$1-");
    }

    @PostMapping("/redeem")
    @Transactional
    public ResponseEntity<?> redeem(@RequestParam String code, Principal principal) {
        User user = userRepository.findByEmail(principal.getName()).get();

        GiftCard card = giftCardRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Gift card not found"));

        if (!card.isActive() || card.getBalance() <= 0) {
            throw new RuntimeException("This card has already been used or is inactive.");
        }

        if (card.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("This gift card has expired.");
        }

        // Transfer money to user profile
        user.setStoreBalance(user.getStoreBalance() + card.getBalance());

        // Deactivate the card so it can't be redeemed again
        card.setBalance(0);
        card.setActive(false);

        userRepository.save(user);
        giftCardRepository.save(card);

        return ResponseEntity.ok(Map.of("newBalance", user.getStoreBalance()));
    }
}
