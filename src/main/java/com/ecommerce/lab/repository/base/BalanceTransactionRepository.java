package com.ecommerce.lab.repository.base;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import com.ecommerce.lab.model.BalanceTransaction;
import com.ecommerce.lab.model.User;

@NoRepositoryBean
public interface BalanceTransactionRepository extends JpaRepository<BalanceTransaction, Long> {
    List<BalanceTransaction> findAllByUserOrderByDateDesc(User user);
}
