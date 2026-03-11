package com.ecommerce.lab.dto;

import java.util.List;

public record OrderRequest(
        String couponCode,
        boolean useStoreBalance,
        String shippingAddress,
        List<GiftCardRequest> giftCards
) {

}
