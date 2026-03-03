package com.ecommerce.lab.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.lab.model.BalanceTransaction;
import com.ecommerce.lab.model.User;

public interface BalanceTransactionRepository extends JpaRepository<BalanceTransaction, Long> {
    List<BalanceTransaction> findAllByUserOrderByDateDesc(User user);
}
