package com.billy.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.billy.backend.model.Product;
import com.billy.backend.repository.ProductRepository;
import com.billy.backend.service.ProductService;

public class ProductTest {
    @InjectMocks
    private ProductService productService;

    @Mock
    private ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSlugify() {
        String result = productService.slugify("Test Product Name");
        assertEquals("test-product-name", result);
    }

    @Test
    void testGenerateUniqueSlug() {
        when(productRepository.existsBySlug("test-product")).thenReturn(true, false);

        String uniqueSlug = productService.generateUniqueSlug("test-product");

        assertEquals("test-product-1", uniqueSlug);
        verify(productRepository, times(2)).existsBySlug(anyString());
    }

    @Test
    void testCreateProduct() {
        Product product = new Product(null, null, "Test Product", "Description", 9.99f, 10, null, null);

        when(productRepository.existsBySlug("test-product")).thenReturn(false);

        Product result = productService.createProduct(product);

        assertEquals("test-product", result.getSlug());
        assertEquals("Test Product", result.getName());
        assertEquals("Description", result.getDescription());
        assertEquals(9.99f, result.getPrice());
        assertEquals(10, result.getQuantity());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());
    }

    @Test
    void testUpdateProduct() {
        Product existingProduct = new Product(1, "test-product", "old name", "old description", 1.23f, 5, LocalDateTime.now().minusDays(1), LocalDateTime.now().minusDays(1));
        Product updates = new Product(null, null, "new name", null, null, 20, null, null);

        Product result = productService.updateProduct(existingProduct, updates);

        assertEquals(1, result.getId());
        assertEquals("test-product", result.getSlug());
        assertEquals("new name", result.getName());
        assertEquals("old description", result.getDescription());
        assertEquals(1.23f, result.getPrice());
        assertEquals(20, result.getQuantity());
        assertEquals(existingProduct.getCreatedAt(), result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());
        assertTrue(result.getUpdatedAt().isAfter(existingProduct.getUpdatedAt()));
    }

    @Test
    void testCreatePageable() {
        Pageable pageable = productService.createPageable(0, 9, "name", "desc");

        assertEquals(0, pageable.getPageNumber());
        assertEquals(9, pageable.getPageSize());
        assertEquals(Sort.by(Sort.Direction.DESC, "name"), pageable.getSort());

        Pageable defaultSortPageable = productService.createPageable(1, 5, "", "");

        assertEquals(1, defaultSortPageable.getPageNumber());
        assertEquals(5, defaultSortPageable.getPageSize());
        assertEquals(Sort.by(Sort.Direction.DESC, "updatedAt"), defaultSortPageable.getSort());
    }
}
