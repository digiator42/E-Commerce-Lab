package com.ecommerce.lab.service;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.ecommerce.lab.dto.ProductRequestDTO;
import com.ecommerce.lab.dto.ProductResponseDTO;
import com.ecommerce.lab.exception.ProductNotFoundException;
import com.ecommerce.lab.model.Product;
import com.ecommerce.lab.model.Review;
import com.ecommerce.lab.repository.base.OrderRepository;
import com.ecommerce.lab.repository.base.ProductRepository;
import com.ecommerce.lab.repository.base.ReviewRepository;

import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import jakarta.validation.Valid;

@Service
@Transactional
public class ProductService {
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final ReviewRepository reviewRepository;

    public ProductService(
        ProductRepository productRepository, OrderRepository orderRepository,
        ReviewRepository reviewRepository
    ) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.reviewRepository = reviewRepository;
    }

    public Product findProductById(Long id) {
        return productRepository.findById(id)
            .orElseThrow(() -> new ProductNotFoundException("Product Not Found"));
    }

    public List<Product> findAllProductsList() { return productRepository.findAll(); }

    public Page<ProductResponseDTO> getAll(Pageable pageable) {
        return productRepository.findAll(pageable)
            .map(ProductResponseDTO::fromEntity);
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

        var product = this.findProductById(id);

        product.setName(dto.name());
        product.setDescription(dto.description());
        product.setStock(dto.stock());

        return productRepository.save(product);
    }

    @Transactional(readOnly = true)
    public String canReview(String email, Long id) {
        boolean purchased = orderRepository.existsByUserEmailAndItemsProductId(email, id);
        boolean reviewed = reviewRepository.existsByUserEmailAndProductId(email, id);

        String status = "GUEST";

        if (reviewed) {
            status = "ALREADY_REVIEWED";
        } else if (purchased) {
            status = "CAN_REVIEW";
        } else {
            status = "MUST_PURCHASE";
        }

        return status;
    }

    @Transactional(readOnly = true)
    public ProductResponseDTO getProduct(Long id, Principal principal) {

        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ProductNotFoundException("Product Not Found"));

        String status = (principal != null) ? this.canReview(principal.getName(), id) : "GUEST";

        return ProductResponseDTO.fromEntity(product, status);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponseDTO> getProductsPage(
        String search,
        List<String> category,
        Double minPrice,
        Double maxPrice,
        Double minRating,
        String sort,
        Pageable pageable,
        Principal principal
    ) {
        // Sorting
        Sort sortOrder = switch (sort) {
        case "price_asc" -> Sort.by("price").ascending();
        case "price_desc" -> Sort.by("price").descending();
        case "name_asc" -> Sort.by("name").ascending();
        case "name_desc" -> Sort.by("name").descending();
        case "stock" -> Sort.by("stock").ascending();
        case "newest" -> Sort.by("id").descending();
        default -> Sort.unsorted();
        };

        Pageable updatedPageable = PageRequest
            .of(pageable.getPageNumber(), pageable.getPageSize(), sortOrder);

        Page<Product> productPage;
        if ("rating".equals(sort)) {
            // Special case for average rating sorting
            productPage = productRepository.findAllOrderByAverageRating(updatedPageable);
        } else {
            Specification<Product> spec = this
                .filterBy(search, category, minPrice, maxPrice, minRating);
            productPage = productRepository.findAll(spec, updatedPageable);
        }

        List<Long> productIds = productPage.getContent().stream().map(Product::getId).toList();

        // Batch fetch purchased and reviewed IDs for this user
        Set<Long> purchasedIds = (principal == null) ? Set.of()
            : orderRepository.findPurchasedProductIds(principal.getName(), productIds);

        Set<Long> reviewedIds = (principal == null) ? Set.of()
            : reviewRepository.findReviewedProductIds(principal.getName(), productIds);

        return productPage.map(product -> {
            String status = "GUEST";
            if (principal != null) {
                if (reviewedIds.contains(product.getId())) {
                    status = "ALREADY_REVIEWED";
                } else if (purchasedIds.contains(product.getId())) {
                    status = "CAN_REVIEW";
                } else {
                    status = "MUST_PURCHASE";
                }
            }
            return ProductResponseDTO.fromEntity(product, status);
        });
    }

    public Specification<Product> filterBy(
        String search,
        List<String> categories,
        Double minPrice,
        Double maxPrice,
        Double minRating
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (search != null && !search.isEmpty()) {
                predicates
                    .add(cb.like(cb.lower(root.get("name")), "%" + search.toLowerCase() + "%"));
            }

            if (categories != null && !categories.isEmpty()) {
                predicates.add(root.get("category").get("name").in(categories));
            }

            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }

            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            if (minRating != null && minRating > 0) {

                // Create the subquery AVG(rating) WHERE review.product_id = product.id
                Subquery<Double> subquery = query.subquery(Double.class);
                Root<Review> subRoot = subquery.from(Review.class);

                // Define the AVG calculation
                subquery.select(cb.avg(subRoot.get("rating")))
                    .where(cb.equal(subRoot.get("product"), root));

                // Add the result as a filter condition
                predicates.add(cb.greaterThanOrEqualTo(subquery, minRating));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException("Product not found");
        }
        productRepository.deleteById(id);
    }
}
