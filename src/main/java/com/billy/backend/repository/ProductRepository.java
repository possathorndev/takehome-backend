package com.billy.backend.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.billy.backend.model.Product;

public interface ProductRepository extends JpaRepository<Product, Integer> {
    Optional<Product> findBySlug(String slug);
    Boolean existsBySlug(String slug);
    Page<Product> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String name, String description, Pageable pageable);
}
