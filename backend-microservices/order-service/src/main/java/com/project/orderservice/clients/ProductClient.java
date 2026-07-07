package com.project.orderservice.clients;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Goi product-service de lay thong tin variant/san pham
 * (thay cho VariantRepository truoc day).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ProductClient {
    private final RestTemplate restTemplate;

    @Value("${services.product.url}")
    private String productServiceUrl;

    public record VariantInfo(Long id, String variantName, Integer stock,
                              Long productId, String productName,
                              Float productPrice, String thumbnail) {
    }

    /** Tra ve thong tin variant hoac null neu khong ton tai / loi. */
    public VariantInfo getVariant(Long variantId) {
        try {
            Map<?, ?> m = restTemplate.getForObject(
                    productServiceUrl + "/internal/variants/" + variantId, Map.class);
            if (m == null) {
                return null;
            }
            return new VariantInfo(
                    toLong(m.get("id")),
                    (String) m.get("variantName"),
                    m.get("stock") == null ? null : ((Number) m.get("stock")).intValue(),
                    toLong(m.get("productId")),
                    (String) m.get("productName"),
                    m.get("productPrice") == null ? null : ((Number) m.get("productPrice")).floatValue(),
                    (String) m.get("thumbnail"));
        } catch (Exception e) {
            log.warn("Cannot fetch variant {} from product-service: {}", variantId, e.getMessage());
            return null;
        }
    }

    private Long toLong(Object o) {
        return o == null ? null : ((Number) o).longValue();
    }
}
