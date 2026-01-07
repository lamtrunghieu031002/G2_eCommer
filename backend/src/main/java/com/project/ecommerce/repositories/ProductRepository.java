package com.project.ecommerce.repositories;

import com.project.ecommerce.models.Product;
import com.project.ecommerce.responses.ProductStatResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

    @Query("SELECT " +
            "p.name , " +
            "SUM(oi.numberOfProducts) ," +
            "SUM(oi.price * oi.numberOfProducts) " +
            "FROM OrderDetail oi " +
            "JOIN Order o ON oi.order.id = o.id " +
            "JOIN ProductVariant pv ON oi.productVariant.id = pv.id " +
            "JOIN Product p ON pv.product.id = p.id " +
            "WHERE " +
            "o.status = 'delivered' " +
            "AND (:start IS NULL OR o.orderDate >= :start) " +
            "AND (:end IS NULL OR o.orderDate <= :end) " +
            "GROUP BY p.name ")
    Page<Object[]> getProductStat(@Param("start") LocalDate start,
                                             @Param("end") LocalDate end, Pageable pageable);
}
