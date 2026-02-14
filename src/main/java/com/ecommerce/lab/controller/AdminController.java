package com.ecommerce.lab.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

import com.ecommerce.lab.dto.ProductRequestDTO;
import com.ecommerce.lab.model.Category;
import com.ecommerce.lab.model.Order;
import com.ecommerce.lab.model.Product;
import com.ecommerce.lab.repository.CategoryRepository;
import com.ecommerce.lab.repository.OrderRepository;
import com.ecommerce.lab.repository.ProductRepository;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final CategoryRepository categoryRepository;

    public AdminController(ProductRepository productRepository, OrderRepository orderRepository,
            CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.categoryRepository = categoryRepository;
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getDashboardStats() {
        long totalProducts = productRepository.count();
        long totalOrders = orderRepository.count();
        // Summing all order totals for revenue
        double totalRevenue = orderRepository.findAll().stream()
                .mapToDouble(Order::getTotalAmount).sum();

        return ResponseEntity.ok(Map.of(
                "products", totalProducts,
                "orders", totalOrders,
                "revenue", totalRevenue));
    }

    @PostMapping("/products")
    public ResponseEntity<?> createProduct(@RequestBody ProductRequestDTO dto) {
        Category category = categoryRepository.findByName(dto.categoryName())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        Product product = new Product();
        product.setName(dto.name());
        product.setDescription(dto.description());
        product.setPrice(dto.price());
        product.setStock(dto.stock());
        product.setCategory(category);

        Product saved = productRepository.save(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        if (!productRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        productRepository.deleteById(id);
        return ResponseEntity.ok("Product deleted successfully");
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable Long id, @RequestBody ProductRequestDTO dto) {
        return productRepository.findById(id).map(product -> {

            Category category = categoryRepository.findByName(dto.categoryName())
                    .orElseThrow(() -> new RuntimeException("Category not found"));

            product.setName(dto.name());
            product.setPrice(dto.price());
            product.setDescription(dto.description());
            product.setCategory(category);

            productRepository.save(product);
            return ResponseEntity.ok("Product updated successfully");
        }).orElse(ResponseEntity.notFound().build());
    }

}