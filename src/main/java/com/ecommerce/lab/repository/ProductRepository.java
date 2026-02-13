package com.ecommerce.lab.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ecommerce.lab.model.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsByName(String name);

    Optional<Product> findByName(String name);

    List<Product> findAllByName(String name);

    List<Product> findAllByNameOrBrand(String name, String brand);

    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);
}