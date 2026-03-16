package com.ecommerce.lab.repository.base;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import com.ecommerce.lab.model.Product;

import org.springframework.data.repository.NoRepositoryBean;
@NoRepositoryBean
public interface ProductRepository
    extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    boolean existsByName(String name);

    Optional<Product> findByName(String name);

    @EntityGraph(attributePaths = {"reviews", "category"})
    Optional<Product> findById(Long id);

    @Override
    @EntityGraph(attributePaths = {"reviews", "category"})
    Page<Product> findAll(Specification<Product> spec, Pageable pageable);
    
    @EntityGraph(attributePaths = {"reviews", "category"})
    List<Product> findAllByName(String name);
    
    @EntityGraph(attributePaths = {"reviews", "category"})
    List<Product> findAllByNameOrBrand(String name, String brand);
    
    @EntityGraph(attributePaths = {"reviews", "category"})
    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);
    
    @EntityGraph(attributePaths = {"reviews", "category"})
    Page<Product> findByCategoryNameAndNameContainingIgnoreCase(
        String category,
        String name,
        Pageable pageable
    );

    Page<Product> findByCategoryName(String category, Pageable pageable);

    @EntityGraph(attributePaths = {"reviews", "category"})
    @Query("SELECT p FROM Product p LEFT JOIN p.reviews r GROUP BY p.id ORDER BY COALESCE(AVG(r.rating), 0) DESC")
    Page<Product> findAllOrderByAverageRating(Pageable pageable);

}