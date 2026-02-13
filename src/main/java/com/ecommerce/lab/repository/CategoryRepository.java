package com.ecommerce.lab.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ecommerce.lab.model.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

}
