package com.ecommerce.lab.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.lab.model.Order;

public interface OrderRepository extends JpaRepository<Order, Long>  {

}
