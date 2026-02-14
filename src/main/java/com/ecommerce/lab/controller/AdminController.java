package com.ecommerce.lab.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
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
    private final RequestMappingHandlerMapping handlerMapping;

    public AdminController(ProductRepository productRepository, OrderRepository orderRepository,
            CategoryRepository categoryRepository, RequestMappingHandlerMapping handlerMapping) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.categoryRepository = categoryRepository;
        this.handlerMapping = handlerMapping;
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

    @GetMapping("/routes")
    public ResponseEntity<List<Map<String, Object>>> getAllRoutes() {
        List<Map<String, Object>> routes = handlerMapping.getHandlerMethods()
                .entrySet().stream()
                .map(entry -> {
                    RequestMappingInfo info = entry.getKey();
                    HandlerMethod method = entry.getValue();

                    java.util.Set<String> patterns = java.util.Collections.emptySet();

                    if (info.getPathPatternsCondition() != null) {
                        patterns = info.getPathPatternsCondition().getPatternValues();
                    } else if (info.getPatternsCondition() != null) {
                        patterns = info.getPatternsCondition().getPatterns();
                    }

                    ResponseStatus statusAnnotation = method.getMethodAnnotation(ResponseStatus.class);
                    String status = (statusAnnotation != null) ? statusAnnotation.value().toString()
                            : "200 OK (Default)";

                    return Map.of(
                            "path", patterns,
                            "methods", info.getMethodsCondition().getMethods().stream()
                                    .map(Enum::name)
                                    .collect(Collectors.toList()),
                            "handler", method.getShortLogMessage(),
                            "expectedStatus", status);
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(routes);
    }
}