package com.project.productservice.repositories;

import com.project.productservice.models.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    boolean existsByName(String name);
    Page<Product> findAll(Pageable pageable);//phân trang
    @Query("SELECT p.id FROM Product p")
    List<Long> findAllProductIds();

    @Query("SELECT p FROM Product p WHERE " +
            "(:categoryId IS NULL OR :categoryId = 0 OR p.category.id = :categoryId) " +
            "AND (:keyword IS NULL OR :keyword = '' OR p.name LIKE %:keyword% OR p.description LIKE %:keyword%)")
    Page<Product> searchProducts
            (@Param("categoryId") Long categoryId,
             @Param("keyword") String keyword, Pageable pageable);
    @Query("SELECT DISTINCT p FROM Product p " +
            "LEFT JOIN FETCH p.productImages " +
            "LEFT JOIN FETCH p.productVariants " +
            "WHERE p.id = :productId")
    Optional<Product> getDetailProduct(@Param("productId") Long productId);

    @Query("SELECT p FROM Product p WHERE p.id IN :productIds")
    List<Product> findProductsByIds(@Param("productIds") List<Long> productIds);

    @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId AND NOT p.id = :productId")
    Page<Product> findByCategoryId(@Param("categoryId") Long categoryId,
                                   @Param("productId") Long productId, Pageable pageable);

    // Spring Data JPA query methods
    Optional<Product> findByName(String name);
    List<Product> findByNameContainingIgnoreCase(String name);
    List<Product> findByPriceBetween(Float minPrice, Float maxPrice);
}
