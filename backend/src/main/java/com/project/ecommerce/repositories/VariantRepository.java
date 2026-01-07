package com.project.ecommerce.repositories;

import com.project.ecommerce.models.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VariantRepository extends JpaRepository<ProductVariant, Long> {

    // ✅ Method cũ - giữ nguyên
    List<ProductVariant> getProductVariantByProductId(Long productId);
    
    // ✅ Alias method cho chatbot (cùng chức năng với method trên)
    default List<ProductVariant> findByProductId(Long productId) {
        return getProductVariantByProductId(productId);
    }
    
    @Query("SELECT p FROM ProductVariant p WHERE p.id IN :productIds")
    List<ProductVariant> findVariantsByIds(@Param("productIds") List<Long> productIds);
    
    // ✅ THÊM METHOD MỚI CHO CHATBOT
    /**
     * Tìm variants theo tên (chứa keyword)
     */
    List<ProductVariant> findByVariantContainingIgnoreCase(String keyword);
    
    /**
     * Đếm số lượng variants của một sản phẩm
     */
    Long countByProductId(Long productId);
}
