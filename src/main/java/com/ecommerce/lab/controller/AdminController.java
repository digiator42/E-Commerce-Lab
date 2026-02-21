package com.ecommerce.lab.controller;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.core.io.UrlResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.ecommerce.lab.dto.ProductRequestDTO;
import com.ecommerce.lab.dto.ProductResponseDTO;
import com.ecommerce.lab.dto.UserResponseDTO;
import com.ecommerce.lab.model.Category;
import com.ecommerce.lab.model.Order;
import com.ecommerce.lab.model.OrderStatus;
import com.ecommerce.lab.model.Product;
import com.ecommerce.lab.model.Role;
import com.ecommerce.lab.model.User;
import com.ecommerce.lab.repository.CategoryRepository;
import com.ecommerce.lab.repository.OrderRepository;
import com.ecommerce.lab.repository.ProductRepository;
import com.ecommerce.lab.repository.UserRepository;

import tools.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final CategoryRepository categoryRepository;
    private final RequestMappingHandlerMapping handlerMapping;
    private final UserRepository userRepository;

    public AdminController(ProductRepository productRepository,
            OrderRepository orderRepository,
            CategoryRepository categoryRepository,
            RequestMappingHandlerMapping handlerMapping,
            UserRepository userRepository) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.categoryRepository = categoryRepository;
        this.handlerMapping = handlerMapping;
        this.userRepository = userRepository;
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

    @PostMapping(value = "/products", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createProduct(
            @RequestPart("product") String productJson,
            @RequestPart(value = "file", required = false) MultipartFile file) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        ProductRequestDTO dto = mapper.readValue(productJson, ProductRequestDTO.class);

        Category category = categoryRepository.findByName(dto.categoryName())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        Product product = new Product();
        product.setName(dto.name());
        product.setDescription(dto.description());
        product.setPrice(dto.price());
        product.setStock(dto.stock());
        product.setCategory(category);

        // Handle File if present
        if (file != null && !file.isEmpty()) {
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path uploadPath = Paths.get("uploads");

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path filePath = uploadPath.resolve(fileName);
            Files.write(filePath, file.getBytes());

            product.setImageUrl("/api/images/" + fileName);
        }

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
            product.setStock(dto.stock());
            product.setCategory(category);

            productRepository.save(product);
            return ResponseEntity.ok("Product updated successfully");
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/orders")
    public ResponseEntity<List<Order>> getAllOrders() {
        return ResponseEntity.ok(orderRepository.findAll(Sort.by(Sort.Direction.DESC, "orderDate")));
    }

    @PatchMapping("/orders/{id}/status")
    public ResponseEntity<?> updateOrderStatus(@PathVariable Long id, @RequestParam OrderStatus status) {
        Order order = orderRepository.findById(id).orElseThrow();
        order.setStatus(status);
        orderRepository.save(order);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll().stream()
                .map(u -> {
                    String role = u.getRole() != null ? u.getRole().name() : "ROLE_USER";
                    return new UserResponseDTO(
                            u.getId(),
                            u.getName(),
                            u.getUserName(),
                            u.getEmail(),
                            u.getAddress(),
                            role);
                })
                .toList());
    }

    @PatchMapping("/users/{id}/role")
    public ResponseEntity<?> updateUserRole(
            @PathVariable Long id,
            @RequestParam Role role) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String loggedInIdentifier = auth.getName();

        User targetUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if ((Objects.equals(targetUser.getUserName(), loggedInIdentifier) ||
                Objects.equals(targetUser.getEmail(), loggedInIdentifier)) &&
                role != Role.ROLE_ADMIN) {

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Self-downgrade protected."));
        }

        targetUser.setRole(role);
        userRepository.save(targetUser);
        return ResponseEntity.ok().build();
    }

    private final String UPLOAD_DIR = "uploads/";

    @PostMapping("/products/{id}/upload-image")
    public ResponseEntity<?> uploadImage(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        try {
            // Validate File
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest().body("Only images are allowed");
            }

            // Generate a Unique, Clean Filename
            String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
            String cleanName = UUID.randomUUID().toString() + "." + extension;

            // Save to Local Filesystem
            Path path = Paths.get(UPLOAD_DIR + cleanName);
            Files.createDirectories(path.getParent());
            Files.write(path, file.getBytes());

            // Save only the Filename in the DB
            Product product = productRepository.findById(id).orElseThrow();
            product.setImageUrl("/api/images/" + cleanName); // Virtual path
            productRepository.save(product);

            return ResponseEntity.ok("Image uploaded successfully");
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Upload failed");
        }
    }

    @GetMapping("/api/images/{filename}")
    public ResponseEntity<Resource> getImage(@PathVariable String filename) throws MalformedURLException {
        Path path = Paths.get("uploads/" + filename);
        Resource resource = new UrlResource(path.toUri());

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG) // Or detect based on extension
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .body(resource);
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