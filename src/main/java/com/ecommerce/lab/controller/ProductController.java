package com.ecommerce.lab.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ecommerce.lab.dto.ProductRequestDTO;
import com.ecommerce.lab.dto.ProductResponseDTO;
import com.ecommerce.lab.model.Product;
import com.ecommerce.lab.repository.ProductRepository;
import com.ecommerce.lab.service.ProductService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService service;
    private final ProductRepository productRepository;

    public ProductController(ProductService service, ProductRepository productRepository) {
        this.service = service;
        this.productRepository = productRepository;
    }

    @PostMapping()
    public ResponseEntity<Product> createProduct(@Valid @RequestBody ProductRequestDTO dto) {
        return ResponseEntity.ok(service.createProduct(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProduct(@PathVariable Long id) {
        return ResponseEntity.ok(service.findProductById(id));
    }

    @GetMapping
    public ResponseEntity<Page<ProductResponseDTO>> getAllProducts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            Pageable pageable) {

        Page<Product> productPage;

        // Logic Tree for Filtering
        if (search != null && !search.isEmpty() && category != null && !category.isEmpty()) {
            // Filter by BOTH
            productPage = productRepository.findByCategoryNameAndNameContainingIgnoreCase(category, search, pageable);
        } else if (search != null && !search.isEmpty()) {
            // Filter by SEARCH only
            productPage = productRepository.findByNameContainingIgnoreCase(search, pageable);
        } else if (category != null && !category.isEmpty()) {
            // Filter by CATEGORY only
            productPage = productRepository.findByCategoryName(category, pageable);
        } else {
            // No filters
            productPage = productRepository.findAll(pageable);
        }

        return ResponseEntity.ok(productPage.map(ProductResponseDTO::fromEntity));
    }
}
