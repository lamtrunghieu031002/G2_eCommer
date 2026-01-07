package com.project.ecommerce.services.chatbot;

import com.project.ecommerce.models.Product;
import com.project.ecommerce.models.ProductVariant;
import com.project.ecommerce.repositories.ProductRepository;
import com.project.ecommerce.repositories.VariantRepository;
import com.project.ecommerce.services.chatbot.intent.Intent;
import com.project.ecommerce.services.chatbot.intent.IntentType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Component xây dựng query thông minh để tìm sản phẩm từ database
 * Dựa trên Intent đã được phân tích
 */
@Component
@RequiredArgsConstructor
public class ProductQueryBuilder {
    
    private final ProductRepository productRepository;
    private final VariantRepository variantRepository;
    
    /**
     * Tìm sản phẩm dựa trên Intent
     */
    public List<Product> findProducts(Intent intent) {
        if (intent == null || !intent.getType().requiresDatabaseQuery()) {
            return Collections.emptyList();
        }
        
        System.out.println("===== PRODUCT QUERY BUILDER =====");
        System.out.println("Intent Type: " + intent.getType());
        System.out.println("Keywords: " + intent.getKeywords());
        
        List<Product> products = new ArrayList<>();
        
        switch (intent.getType()) {
            case PRODUCT_INQUIRY:
            case CHECK_STOCK:
            case PRICE_INQUIRY:
                products = findByProductInquiry(intent);
                break;
                
            case FIND_BY_BUDGET:
                products = findByBudget(intent);
                break;
                
            case FIND_BY_CATEGORY:
                products = findByCategory(intent);
                break;
                
            case FIND_BY_BRAND:
                products = findByBrand(intent);
                break;
                
            case FIND_BY_SPECS:
                products = findBySpecs(intent);
                break;
                
            case COMPARE_PRODUCTS:
                products = findForComparison(intent);
                break;
                
            default:
                products = findByKeywords(intent);
                break;
        }
        
        // Apply price filter if exists
        if (intent.hasPriceFilter()) {
            products = filterByPrice(products, intent.getMinPrice(), intent.getMaxPrice());
        }
        
        // Apply storage filter if exists
        if (intent.getStorage() != null) {
            products = filterByStorage(products, intent.getStorage());
        }
        
        // Sort by relevance
        products = sortByRelevance(products, intent);
        
        // Limit results
        int maxResults = intent.getMaxResults() != null ? intent.getMaxResults() : 5;
        if (products.size() > maxResults) {
            products = products.subList(0, maxResults);
        }
        
        System.out.println("Found " + products.size() + " products");
        System.out.println("===== QUERY COMPLETE =====\n");
        
        return products;
    }
    
    /**
     * Tìm sản phẩm theo tên cụ thể (EXACT MATCH ưu tiên)
     */
    private List<Product> findByProductInquiry(Intent intent) {
        List<Product> products = new ArrayList<>();
        
        // 1. Thử EXACT MATCH với product name
        if (intent.getProductName() != null) {
            Optional<Product> exactMatch = productRepository.findByName(intent.getProductName());
            if (exactMatch.isPresent()) {
                System.out.println("✓ Exact match found: " + exactMatch.get().getName());
                products.add(exactMatch.get());
                return products;
            }
        }
        
        // 2. Thử tìm theo từng keyword riêng lẻ
        if (!intent.getKeywords().isEmpty()) {
            for (String keyword : intent.getKeywords()) {
                List<Product> matches = productRepository.findByNameContainingIgnoreCase(keyword);
                for (Product p : matches) {
                    if (!products.contains(p)) {
                        products.add(p);
                    }
                }
            }
        }
        
        // 3. Nếu vẫn chưa có kết quả, dùng searchProducts
        if (products.isEmpty()) {
            String searchTerm = intent.getProductName() != null 
                ? intent.getProductName() 
                : String.join(" ", intent.getKeywords());
            
            products = productRepository.searchProducts(
                null, // categoryId
                searchTerm,
                PageRequest.of(0, 10)
            ).getContent();
        }
        
        return products;
    }
    
    /**
     * Tìm sản phẩm theo ngân sách
     */
    private List<Product> findByBudget(Intent intent) {
        Float minPrice = intent.getMinPrice() != null ? intent.getMinPrice() : 0f;
        Float maxPrice = intent.getMaxPrice() != null ? intent.getMaxPrice() : Float.MAX_VALUE;
        
        System.out.println("Searching by budget: " + minPrice + " - " + maxPrice);
        
        List<Product> products = productRepository.findByPriceBetween(minPrice, maxPrice);
        
        // Nếu có category, lọc thêm
        if (intent.getCategory() != null) {
            products = products.stream()
                .filter(p -> matchesCategory(p, intent.getCategory()))
                .collect(Collectors.toList());
        }
        
        return products;
    }
    
    /**
     * Tìm sản phẩm theo category
     */
    private List<Product> findByCategory(Intent intent) {
        String category = intent.getCategory();
        System.out.println("Searching by category: " + category);
        
        // Tìm tất cả sản phẩm và filter theo category name
        List<Product> allProducts = productRepository.findAll();
        return allProducts.stream()
            .filter(p -> matchesCategory(p, category))
            .collect(Collectors.toList());
    }
    
    /**
     * Tìm sản phẩm theo brand
     */
    private List<Product> findByBrand(Intent intent) {
        String brand = intent.getBrand();
        System.out.println("Searching by brand: " + brand);
        
        return productRepository.findByNameContainingIgnoreCase(brand);
    }
    
    /**
     * Tìm sản phẩm theo specs (storage, RAM...)
     */
    private List<Product> findBySpecs(Intent intent) {
        List<Product> products = productRepository.findAll();
        
        // Filter by storage
        if (intent.getStorage() != null) {
            products = filterByStorage(products, intent.getStorage());
        }
        
        // Filter by RAM
        if (intent.getRam() != null) {
            final String ram = intent.getRam();
            products = products.stream()
                .filter(p -> p.getName().toLowerCase().contains(ram.toLowerCase()))
                .collect(Collectors.toList());
        }
        
        // Filter by category if exists
        if (intent.getCategory() != null) {
            products = products.stream()
                .filter(p -> matchesCategory(p, intent.getCategory()))
                .collect(Collectors.toList());
        }
        
        return products;
    }
    
    /**
     * Tìm sản phẩm để so sánh
     */
    private List<Product> findForComparison(Intent intent) {
        // Extract 2 sản phẩm từ keywords
        if (intent.getKeywords().size() >= 2) {
            List<Product> products = new ArrayList<>();
            
            for (String keyword : intent.getKeywords()) {
                List<Product> matches = productRepository.findByNameContainingIgnoreCase(keyword);
                if (!matches.isEmpty() && !products.contains(matches.get(0))) {
                    products.add(matches.get(0));
                }
                
                if (products.size() >= 2) break;
            }
            
            return products;
        }
        
        return Collections.emptyList();
    }
    
    /**
     * Tìm sản phẩm theo keywords
     */
    private List<Product> findByKeywords(Intent intent) {
        if (intent.getKeywords().isEmpty()) {
            return Collections.emptyList();
        }
        
        String searchTerm = String.join(" ", intent.getKeywords());
        return productRepository.searchProducts(
            null,
            searchTerm,
            PageRequest.of(0, 10)
        ).getContent();
    }
    
    /**
     * Filter sản phẩm theo giá
     */
    private List<Product> filterByPrice(List<Product> products, Float minPrice, Float maxPrice) {
        return products.stream()
            .filter(p -> {
                float price = p.getPrice();
                if (minPrice != null && price < minPrice) return false;
                if (maxPrice != null && price > maxPrice) return false;
                return true;
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Filter sản phẩm theo storage
     */
    private List<Product> filterByStorage(List<Product> products, String storage) {
        return products.stream()
            .filter(p -> {
                // Kiểm tra trong tên sản phẩm
                if (p.getName().toLowerCase().contains(storage.toLowerCase())) {
                    return true;
                }
                
                // Kiểm tra trong variants
                List<ProductVariant> variants = variantRepository.findByProductId(p.getId());
                return variants.stream()
                    .anyMatch(v -> v.getName().toLowerCase().contains(storage.toLowerCase()));
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Kiểm tra sản phẩm có thuộc category không
     */
    private boolean matchesCategory(Product product, String categoryKeyword) {
        if (product.getCategory() == null) {
            return false;
        }
        
        String categoryName = product.getCategory().getName().toLowerCase();
        categoryKeyword = categoryKeyword.toLowerCase();
        
        // Mapping category keywords
        Map<String, List<String>> categoryMap = Map.of(
            "phone", Arrays.asList("phone", "điện thoại", "smartphone"),
            "laptop", Arrays.asList("laptop", "máy tính"),
            "tablet", Arrays.asList("tablet", "ipad"),
            "headphone", Arrays.asList("tai nghe", "headphone"),
            "watch", Arrays.asList("đồng hồ", "watch")
        );
        
        for (Map.Entry<String, List<String>> entry : categoryMap.entrySet()) {
            if (entry.getValue().contains(categoryKeyword)) {
                return entry.getValue().stream()
                    .anyMatch(categoryName::contains);
            }
        }
        
        return categoryName.contains(categoryKeyword);
    }
    
    /**
     * Sort sản phẩm theo độ liên quan
     */
    private List<Product> sortByRelevance(List<Product> products, Intent intent) {
        return products.stream()
            .sorted((p1, p2) -> {
                int score1 = calculateRelevanceScore(p1, intent);
                int score2 = calculateRelevanceScore(p2, intent);
                
                // Sort descending (score cao hơn lên trước)
                return Integer.compare(score2, score1);
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Tính điểm độ liên quan của sản phẩm
     */
    private int calculateRelevanceScore(Product product, Intent intent) {
        int score = 0;
        String productName = product.getName().toLowerCase();
        
        // Exact match product name = +100
        if (intent.getProductName() != null 
            && productName.equals(intent.getProductName().toLowerCase())) {
            score += 100;
        }
        
        // Contains all keywords = +50
        if (!intent.getKeywords().isEmpty()) {
            long matchingKeywords = intent.getKeywords().stream()
                .filter(kw -> productName.contains(kw.toLowerCase()))
                .count();
            score += (int) (matchingKeywords * 10);
        }
        
        // Matching brand = +30
        if (intent.getBrand() != null 
            && productName.contains(intent.getBrand().toLowerCase())) {
            score += 30;
        }
        
        // Matching category = +20
        if (intent.getCategory() != null && matchesCategory(product, intent.getCategory())) {
            score += 20;
        }
        
        // In stock = +10
        if (intent.getNeedsStockCheck() != null && intent.getNeedsStockCheck()) {
            List<ProductVariant> variants = variantRepository.findByProductId(product.getId());
            boolean hasStock = variants.stream().anyMatch(v -> v.getQuantity() > 0);
            if (hasStock) {
                score += 10;
            }
        }
        
        return score;
    }
    
    /**
     * Lấy variants của sản phẩm
     */
    public List<ProductVariant> getProductVariants(Long productId) {
        return variantRepository.findByProductId(productId);
    }
}
