package com.ecommerce.lab.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.ecommerce.lab.dto.ProductRequestDTO;
import com.ecommerce.lab.dto.ProductResponseDTO;
import com.ecommerce.lab.exception.ProductNotFoundException;
import com.ecommerce.lab.model.Product;
import com.ecommerce.lab.repository.ProductRepository;

import org.springframework.transaction.annotation.Transactional;;
import jakarta.validation.Valid;

@Service
@Transactional
public class ProductService {
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Product findProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product Not Found"));
    }

    public List<Product> findAllProductsList() {
        return productRepository.findAll();
    }

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

    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException("Product not found");
        }
        productRepository.deleteById(id);
    }
}
