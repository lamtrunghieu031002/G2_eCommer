package com.project.productservice.repositories;

import com.project.productservice.models.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VariantRepository extends JpaRepository<ProductVariant, Long> {

    List<ProductVariant> getProductVariantByProductId(Long productId);
    @Query("SELECT p FROM ProductVariant p WHERE p.id IN :productIds")
    List<ProductVariant> findVariantsByIds(@Param("productIds") List<Long> productIds);

    // Spring Data JPA query method
    List<ProductVariant> findByProductId(Long productId);
    @Modifying
    @Query("UPDATE ProductVariant v SET v.stock = v.stock - :qty WHERE v.id = :id AND v.stock >= :qty")
    int decreaseStock(@Param("id") Long variantId, @Param("qty") int quantity);
    @Modifying
    @Query("UPDATE ProductVariant v SET v.stock = v.stock + :qty WHERE v.id = :id")
    int increaseStock(@Param("id") Long variantId, @Param("qty") int quantity);
}
