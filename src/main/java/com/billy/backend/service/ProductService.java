package com.billy.backend.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.billy.backend.model.Product;
import com.billy.backend.repository.ProductRepository;

@Service
public class ProductService {

    private final ProductRepository repository;
    private static final List<String> VALID_SORT_BY_FIELDS = List.of("name", "price", "quantity", "createdAt", "updatedAt");
    private static final String DEFAULT_SORT_BY = "updatedAt";

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    public String slugify(String name) {
        return name.trim().toLowerCase().replaceAll("[^a-z0-9]+", "-");
    }

    public String generateUniqueSlug(String baseSlug) {
        String slug = baseSlug;
        int count = 1;

        
        while (repository.existsBySlug(slug)) {
            slug = baseSlug + "-" + count;
            count++;
        }

        return slug;
    }

    public Product createProduct(Product product) {
        String baseSlug = slugify(product.getName());
        String uniqueSlug = generateUniqueSlug(baseSlug);

        return new Product(
            null,
            uniqueSlug,
            product.getName(),
            product.getDescription(),
            product.getPrice(),
            product.getQuantity(),
            LocalDateTime.now(),
            LocalDateTime.now()
        );
    }

    public Product updateProduct(Product existingProduct, Product product) {
        return new Product(
            existingProduct.getId(),
            existingProduct.getSlug(),
            product.getName() == null ? existingProduct.getName() : product.getName(),
            product.getDescription() == null ? existingProduct.getDescription() : product.getDescription(),
            product.getPrice() == null ? existingProduct.getPrice() : product.getPrice(),
            product.getQuantity() == null ? existingProduct.getQuantity() : product.getQuantity(),
            existingProduct.getCreatedAt(),
            LocalDateTime.now()
        );
    }

    public Pageable createPageable(int page, int pageSize, String sortBy, String sortOrder) {
        if (!VALID_SORT_BY_FIELDS.contains(sortBy)) {
            sortBy = DEFAULT_SORT_BY;
        }

        Sort.Direction direction;
        if ("asc".equalsIgnoreCase(sortOrder)) {
            direction = Sort.Direction.ASC;
        } else {
            direction = Sort.Direction.DESC;
        }

        return PageRequest.of(page, pageSize, Sort.by(direction, sortBy));
    }

}
