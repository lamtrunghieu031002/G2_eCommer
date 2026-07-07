package com.project.productservice.controllers;

import com.project.productservice.exceptions.DataNotFoundException;
import com.project.productservice.models.Product;
import com.project.productservice.models.ProductVariant;
import com.project.productservice.repositories.ProductRepository;
import com.project.productservice.repositories.VariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * API noi bo cho order-service va chatbot-service.
 * Khong duoc expose qua API Gateway.
 */
@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class InternalCatalogController {
    private final ProductRepository productRepository;
    private final VariantRepository variantRepository;

    /** order-service goi khi tao don: lay gia + ten san pham cua variant */
    @GetMapping("/variants/{id}")
    public Map<String, Object> getVariant(@PathVariable Long id) {
        ProductVariant variant = variantRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Cannot find variant with id: " + id));
        Product product = variant.getProduct();
        Map<String, Object> result = new HashMap<>();
        result.put("id", variant.getId());
        result.put("variantName", variant.getVariant());
        result.put("stock", variant.getStock());
        result.put("productId", product != null ? product.getId() : null);
        result.put("productName", product != null ? product.getName() : null);
        result.put("productPrice", product != null ? product.getPrice() : null);
        result.put("thumbnail", product != null ? product.getThumbnail() : null);
        return result;
    }

    /** chatbot-service: tim san pham theo ten (chinh xac) */
    @GetMapping("/products/by-name")
    public List<Product> findByName(@RequestParam String name) {
        return productRepository.findByName(name).map(List::of).orElse(List.of());
    }

    /** chatbot-service: tim san pham theo ten (chua keyword) */
    @GetMapping("/products/search")
    public List<Product> searchByName(@RequestParam String keyword) {
        return productRepository.findByNameContainingIgnoreCase(keyword);
    }

    /** chatbot-service: tim theo khoang gia */
    @GetMapping("/products/price-range")
    public List<Product> findByPriceRange(@RequestParam Float min, @RequestParam Float max) {
        return productRepository.findByPriceBetween(min, max);
    }

    /** chatbot-service: tim theo category/keyword co phan trang */
    @GetMapping("/products/search-page")
    public List<Product> searchProducts(
            @RequestParam(required = false, defaultValue = "0") Long categoryId,
            @RequestParam(required = false, defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return productRepository.searchProducts(categoryId, keyword, PageRequest.of(page, size)).getContent();
    }

    /** chatbot-service: toan bo san pham (dung cho goi y) */
    @GetMapping("/products/all")
    public List<Product> findAll() {
        return productRepository.findAll();
    }

    /** chatbot-service: variants cua mot san pham */
    @GetMapping("/products/{productId}/variants")
    public List<ProductVariant> findVariantsByProductId(@PathVariable Long productId) {
        return variantRepository.findByProductId(productId);
    }
}
