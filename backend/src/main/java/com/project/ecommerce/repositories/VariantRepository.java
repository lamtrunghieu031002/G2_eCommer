package com.project.ecommerce.repositories;

import com.project.ecommerce.models.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VariantRepository extends JpaRepository<ProductVariant, Long> {

    List<ProductVariant> getProductVariantByProductId(Long productId);
    @Query("SELECT p FROM ProductVariant p WHERE p.id IN :productIds")
    List<ProductVariant> findVariantsByIds(@Param("productIds") List<Long> productIds);
}
