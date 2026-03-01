package com.ecommerce.lab.dto;

import java.util.List;

public record OrderRequest(
        String couponCode,
        List<GiftCardRequest> giftCards) {
}
