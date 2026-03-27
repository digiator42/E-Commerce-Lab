package com.ecommerce.lab.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

// Import all base interfaces
import com.ecommerce.lab.repository.base.*;

@Configuration
public class RepositoryPrimaryConfig {

    // --- BALANCETRANSACTIONS REPOSITORY ---
    @Bean
    @Primary
    @Profile("!mysql-primary")
    public BalanceTransactionRepository primaryBalanceTransactionsRepoPg(
        @Qualifier("balanceTransactionRepositoryPostgres") BalanceTransactionRepository repo
    ) {
        return repo;
    }

    @Bean
    @Primary
    @Profile("mysql-primary")
    public BalanceTransactionRepository primaryBalanceTransactionsRepoMy(
        @Qualifier("balanceTransactionRepositoryMysql") BalanceTransactionRepository repo
    ) {
        return repo;
    }

    // --- USER REPOSITORY ---
    @Bean
    @Primary
    @Profile("!mysql-primary")
    public UserRepository primaryUserRepoPg(
        @Qualifier("userRepositoryPostgres") UserRepository repo
    ) {
        return repo;
    }

    @Bean
    @Primary
    @Profile("mysql-primary")
    public UserRepository primaryUserRepoMy(@Qualifier("userRepositoryMySQL") UserRepository repo) {
        return repo;
    }

    // --- PRODUCT REPOSITORY ---
    @Bean
    @Primary
    @Profile("!mysql-primary")
    public ProductRepository primaryProductRepoPg(
        @Qualifier("productRepositoryPostgres") ProductRepository repo
    ) {
        return repo;
    }

    @Bean
    @Primary
    @Profile("mysql-primary")
    public ProductRepository primaryProductRepoMy(
        @Qualifier("productRepositoryMysql") ProductRepository repo
    ) {
        return repo;
    }

    // --- OrderItemRepository ---
    @Bean
    @Primary
    @Profile("!mysql-primary")
    public OrderItemRepository primaryOrderItemRepoPg(
        @Qualifier("orderItemRepositoryPostgres") OrderItemRepository repo
    ) {
        return repo;
    }

    @Bean
    @Primary
    @Profile("mysql-primary")
    public OrderItemRepository primaryOrderItemRepoMy(
        @Qualifier("orderItemRepositoryMysql") OrderItemRepository repo
    ) {
        return repo;
    }



    // --- ORDER REPOSITORY ---
    @Bean
    @Primary
    @Profile("!mysql-primary")
    public OrderRepository primaryOrderRepoPg(
        @Qualifier("orderRepositoryPostgres") OrderRepository repo
    ) {
        return repo;
    }

    @Bean
    @Primary
    @Profile("mysql-primary")
    public OrderRepository primaryOrderRepoMy(
        @Qualifier("orderRepositoryMysql") OrderRepository repo
    ) {
        return repo;
    }

    // --- COUPON REPOSITORY ---
    @Bean
    @Primary
    @Profile("!mysql-primary")
    public CouponRepository primaryCouponRepoPg(
        @Qualifier("couponRepositoryPostgres") CouponRepository repo
    ) {
        return repo;
    }

    @Bean
    @Primary
    @Profile("mysql-primary")
    public CouponRepository primaryCouponRepoMy(
        @Qualifier("couponRepositoryMysql") CouponRepository repo
    ) {
        return repo;
    }

    // --- CATEGORY REPOSITORY ---
    @Bean
    @Primary
    @Profile("!mysql-primary")
    public CategoryRepository primaryCategoryRepoPg(
        @Qualifier("categoryRepositoryPostgres") CategoryRepository repo
    ) {
        return repo;
    }

    @Bean
    @Primary
    @Profile("mysql-primary")
    public CategoryRepository primaryCategoryRepoMy(
        @Qualifier("categoryRepositoryMysql") CategoryRepository repo
    ) {
        return repo;
    }

    // --- CART REPOSITORY ---
    @Bean
    @Primary
    @Profile("!mysql-primary")
    public CartRepository primaryCartRepoPg(
        @Qualifier("cartRepositoryPostgres") CartRepository repo
    ) {
        return repo;
    }

    @Bean
    @Primary
    @Profile("mysql-primary")
    public CartRepository primaryCartRepoMy(
        @Qualifier("cartRepositoryMysql") CartRepository repo
    ) {
        return repo;
    }

    // --- WISHLIST REPOSITORY ---
    @Bean
    @Primary
    @Profile("!mysql-primary")
    public WishlistRepository primaryWishlistRepoPg(
        @Qualifier("wishlistRepositoryPostgres") WishlistRepository repo
    ) {
        return repo;
    }

    @Bean
    @Primary
    @Profile("mysql-primary")
    public WishlistRepository primaryWishlistRepoMy(
        @Qualifier("wishlistRepositoryMysql") WishlistRepository repo
    ) {
        return repo;
    }

    // --- GIFTCARD REPOSITORY ---
    @Bean
    @Primary
    @Profile("!mysql-primary")
    public GiftCardRepository primaryGiftCardRepoPg(
        @Qualifier("giftCardRepositoryPostgres") GiftCardRepository repo
    ) {
        return repo;
    }

    @Bean
    @Primary
    @Profile("mysql-primary")
    public GiftCardRepository primaryGiftCardRepoMy(
        @Qualifier("giftCardRepositoryMysql") GiftCardRepository repo
    ) {
        return repo;
    }

    // --- REVIEW REPOSITORY ---
    @Bean
    @Primary
    @Profile("!mysql-primary")
    public ReviewRepository primaryReviewRepoPg(
        @Qualifier("reviewRepositoryPostgres") ReviewRepository repo
    ) {
        return repo;
    }

    @Bean
    @Primary
    @Profile("mysql-primary")
    public ReviewRepository primaryReviewRepoMy(
        @Qualifier("reviewRepositoryMysql") ReviewRepository repo
    ) {
        return repo;
    }
}