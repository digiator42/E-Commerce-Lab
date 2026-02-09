package com.ecommerce.lab.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.lab.model.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {

}