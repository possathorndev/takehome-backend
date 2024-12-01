package com.billy.backend.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

import com.billy.backend.exception.ProductNameNotValidException;
import com.billy.backend.exception.ProductNotFoundException;
import com.billy.backend.model.LlmRequest;
import com.billy.backend.model.Product;
import com.billy.backend.service.LlmService;
import com.billy.backend.service.ProductService;
import com.billy.backend.repository.ProductRepository;
import com.billy.backend.dto.ProductResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/products")
@CrossOrigin
public class ProductController {
    
    private final ProductRepository repository;
    private final ProductService productService;
    private final LlmService llmService;

    public ProductController(ProductRepository repository, ProductService productService, LlmService llmService) {
        this.repository = repository;
        this.productService = productService;
        this.llmService = llmService;
    }

    @GetMapping("")
    public Map<String, Object> findAll(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "12") int pageSize,
        @RequestParam(defaultValue = "updatedAt") String sortBy,
        @RequestParam(defaultValue = "desc") String sortOrder,
        @RequestParam(defaultValue = "") String search
    ) {
        Pageable pageable = productService.createPageable(page, pageSize, sortBy, sortOrder);

        Page<Product> productPage;
        if (!search.isEmpty()) {
            productPage = repository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(search, search, pageable);
        } else {
            productPage = repository.findAll(pageable);
        }

        List<ProductResponse> productResponses = productPage.getContent().stream()
        .map(product -> new ProductResponse(product.getName(), product.getSlug(), product.getDescription(), product.getPrice(), product.getQuantity()))
        .toList();

        Map<String, Object> response = new HashMap<>();
        response.put("data", productResponses);

        Map<String, Object> pagination = new HashMap<>();
        pagination.put("page", productPage.getNumber());
        pagination.put("pageSize", productPage.getSize());
        pagination.put("pageCount", productPage.getTotalPages());
        pagination.put("total", productPage.getTotalElements());
        response.put("meta", Map.of("pagination", pagination));

        return response;
    }

    @GetMapping("/{slug}")
    public ProductResponse findBySlug(@PathVariable String slug) {
        Product product = repository.findBySlug(slug).orElseThrow(() -> new ProductNotFoundException("Product with slug " + slug + " not found"));
        return new ProductResponse(product.getName(), product.getSlug(), product.getDescription(), product.getPrice(), product.getQuantity());
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("")
    public ProductResponse create(@Valid @RequestBody Product product) {
        Product newProduct = productService.createProduct(product);
        Product saveProduct = repository.save(newProduct);
        return new ProductResponse(saveProduct.getName(), saveProduct.getSlug(), saveProduct.getDescription(), saveProduct.getPrice(), saveProduct.getQuantity());
    }

    @ResponseStatus(HttpStatus.OK)
    @PutMapping("/{slug}")
    public ProductResponse update(@RequestBody Product product, @PathVariable String slug) {
        Product existingProduct = repository.findBySlug(slug)
            .orElseThrow(() -> new ProductNotFoundException("Product with slug " + slug + " not found"));

        Product updatedProduct = productService.updateProduct(existingProduct, product);
        Product saveProduct = repository.save(updatedProduct);
        return new ProductResponse(saveProduct.getName(), saveProduct.getSlug(), saveProduct.getDescription(), saveProduct.getPrice(), saveProduct.getQuantity());
    }

    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("/{slug}")
    public String delete(@PathVariable String slug) {
        Product existingProduct = repository.findBySlug(slug)
        .orElseThrow(() -> new ProductNotFoundException("Product with slug " + slug + " not found"));

        repository.deleteById(existingProduct.getId());

        return String.format("Deleted %s successfully!", existingProduct.getName());
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/generate-description")
    public String generateDescription(@RequestBody LlmRequest body) {
        if (body == null || body.name() == null || body.name() == "") {
            throw new ProductNameNotValidException("Product name is missing, please enter product name before generating description");
        }

        String description = llmService.chatCompletionApi(body.name());
        return description;
    }
}
