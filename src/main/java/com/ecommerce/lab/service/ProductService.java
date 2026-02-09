package com.ecommerce.lab.service;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.ecommerce.lab.dto.ProductRequestDTO;
import com.ecommerce.lab.exception.ProductNotFoundException;
import com.ecommerce.lab.model.Product;
import com.ecommerce.lab.repository.ProductRepository;

import jakarta.validation.Valid;

@Service
public class ProductService {
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Product createProduct(@Valid ProductRequestDTO dto) {
        var product = new Product();

        product.setName(dto.name());
        product.setDescription(dto.description());
        product.setStock(dto.stock());
        product.setPrice(dto.price());

        return productRepository.save(product);
    }

    public Product updateProduct(Long id, ProductRequestDTO dto) {

        var product = productRepository
                .findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product Already Exist"));

        product.setName(dto.name());
        product.setDescription(dto.description());
        product.setStock(dto.stock());

        return product;
    }
}
