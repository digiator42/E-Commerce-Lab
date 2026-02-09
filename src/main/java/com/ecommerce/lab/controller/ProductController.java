package com.ecommerce.lab.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ecommerce.lab.dto.ProductRequestDTO;
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
}
