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

import com.ecommerce.lab.dto.ProductRequestDTO;
import com.ecommerce.lab.dto.ProductResponseDTO;
import com.ecommerce.lab.model.Product;
import com.ecommerce.lab.service.ProductService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
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
    public ResponseEntity<Page<ProductResponseDTO>> getAllProducts(Pageable pageable) {
        return ResponseEntity.ok(service.getAll(pageable));
    }

}
