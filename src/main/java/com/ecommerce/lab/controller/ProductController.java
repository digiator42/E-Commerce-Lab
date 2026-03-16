package com.ecommerce.lab.controller;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ecommerce.lab.dto.ProductRequestDTO;
import com.ecommerce.lab.dto.ProductResponseDTO;
import com.ecommerce.lab.model.Product;
import com.ecommerce.lab.repository.base.ProductRepository;
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
    public ResponseEntity<ProductResponseDTO> getProductById(
        @PathVariable Long id,
        Principal principal
    ) {
        return ResponseEntity.ok(service.getProduct(id, principal));
    }

    @GetMapping
    public ResponseEntity<Page<ProductResponseDTO>> getAllProducts(
        @RequestParam(required = false) String search,
        @RequestParam(required = false) String category,
        Pageable pageable
    ) {

        Page<Product> productPage;

        if (StringUtils.hasText(search) && StringUtils.hasText(category)) {
            productPage = productRepository
                .findByCategoryNameAndNameContainingIgnoreCase(category, search, pageable);
        } else if (StringUtils.hasText(category)) {
            productPage = productRepository.findByCategoryName(category, pageable);
        } else if (StringUtils.hasText(search)) {
            productPage = productRepository.findByNameContainingIgnoreCase(search, pageable);
        } else {
            productPage = productRepository.findAll(pageable);
        }

        return ResponseEntity.ok(productPage.map(ProductResponseDTO::fromEntity));
    }

    @GetMapping("/custom")
    public ResponseEntity<Page<ProductResponseDTO>> getAllProducts(
        @RequestParam(required = false) String search,
        @RequestParam(required = false) List<String> category, // Handles multiple checkboxes
        @RequestParam(required = false) Double minPrice,
        @RequestParam(required = false) Double maxPrice,
        @RequestParam(required = false) Double minRating,
        @RequestParam(defaultValue = "newest") String sort, // newest, price_asc, price_desc, rating
        Pageable pageable,
        Principal principal
    ) {

        return ResponseEntity.ok(
            service.getProductsPage(
                search,
                category,
                minPrice,
                maxPrice,
                minRating,
                sort,
                pageable,
                principal
            )
        );
    }
}
