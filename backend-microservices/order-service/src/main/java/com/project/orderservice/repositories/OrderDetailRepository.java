package com.project.orderservice.repositories;

import com.project.orderservice.models.OrderDetail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {
    List<OrderDetail> findByOrderId(Long orderId);

    // Thong ke doanh thu theo san pham (dua tren snapshot productName)
    @Query("SELECT od.productName, SUM(od.numberOfProducts), SUM(od.price * od.numberOfProducts) " +
            "FROM OrderDetail od JOIN od.order o " +
            "WHERE o.status = 'delivered' " +
            "AND (:start IS NULL OR o.orderDate >= :start) " +
            "AND (:end IS NULL OR o.orderDate <= :end) " +
            "GROUP BY od.productName")
    Page<Object[]> getProductStat(@Param("start") LocalDate start,
                                  @Param("end") LocalDate end, Pageable pageable);
}
