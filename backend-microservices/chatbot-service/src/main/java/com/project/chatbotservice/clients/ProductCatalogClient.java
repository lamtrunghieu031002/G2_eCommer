package com.project.chatbotservice.clients;

import com.project.chatbotservice.models.Product;
import com.project.chatbotservice.models.ProductVariant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Thay the ProductRepository/VariantRepository cua monolith:
 * cung cac method cung chu ky, nhung goi REST sang product-service.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ProductCatalogClient {
    private final RestTemplate restTemplate;

    @Value("${services.product.url}")
    private String productServiceUrl;

    public Optional<Product> findByName(String name) {
        List<Product> list = getList("/internal/products/by-name?name=" + encode(name),
                new ParameterizedTypeReference<>() {
                });
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public List<Product> findByNameContainingIgnoreCase(String keyword) {
        return getList("/internal/products/search?keyword=" + encode(keyword),
                new ParameterizedTypeReference<>() {
                });
    }

    public Page<Product> searchProducts(Long categoryId, String keyword, PageRequest pageRequest) {
        String url = "/internal/products/search-page?categoryId=" + (categoryId == null ? 0 : categoryId)
                + "&keyword=" + encode(keyword == null ? "" : keyword)
                + "&page=" + pageRequest.getPageNumber()
                + "&size=" + pageRequest.getPageSize();
        List<Product> content = getList(url, new ParameterizedTypeReference<>() {
        });
        return new PageImpl<>(content, pageRequest, content.size());
    }

    public List<Product> findByPriceBetween(Float minPrice, Float maxPrice) {
        return getList("/internal/products/price-range?min=" + minPrice + "&max=" + maxPrice,
                new ParameterizedTypeReference<>() {
                });
    }

    public List<Product> findAll() {
        return getList("/internal/products/all", new ParameterizedTypeReference<>() {
        });
    }

    public List<ProductVariant> findByProductId(Long productId) {
        return getList("/internal/products/" + productId + "/variants",
                new ParameterizedTypeReference<>() {
                });
    }

    private <T> List<T> getList(String path, ParameterizedTypeReference<List<T>> type) {
        try {
            List<T> body = restTemplate.exchange(
                    productServiceUrl + path, HttpMethod.GET, null, type).getBody();
            return body != null ? body : Collections.emptyList();
        } catch (Exception e) {
            log.warn("product-service call failed [{}]: {}", path, e.getMessage());
            return Collections.emptyList();
        }
    }

    private String encode(String s) {
        return java.net.URLEncoder.encode(s, java.nio.charset.StandardCharsets.UTF_8);
    }
}
