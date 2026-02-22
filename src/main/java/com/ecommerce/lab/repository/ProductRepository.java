package com.ecommerce.lab.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ecommerce.lab.model.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    boolean existsByName(String name);

    Optional<Product> findByName(String name);

    List<Product> findAllByName(String name);

    List<Product> findAllByNameOrBrand(String name, String brand);

    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Product> findByCategoryNameAndNameContainingIgnoreCase(String category, String name, Pageable pageable);

    Page<Product> findByCategoryName(String category, Pageable pageable);

    @Query("SELECT p FROM Product p LEFT JOIN p.reviews r GROUP BY p.id ORDER BY AVG(r.rating) DESC")
    Page<Product> findAllOrderByAverageRating(Pageable pageable);

}