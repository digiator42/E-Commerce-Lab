package com.ecommerce.lab.repository.base;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.lab.model.Category;

import org.springframework.data.repository.NoRepositoryBean;
@NoRepositoryBean
public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByName(String name);

    Optional<Category> findByName(String categoryName);

}
