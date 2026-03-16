package com.ecommerce.lab.repository.base;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ecommerce.lab.model.Category;

import org.springframework.data.repository.NoRepositoryBean;
@NoRepositoryBean
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByName(String categoryName);
}
