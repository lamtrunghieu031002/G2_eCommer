package com.project.ecommerce.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.project.ecommerce.models.*;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByPhoneNumber(String phoneNumber);
    Optional<User> findByPhoneNumber(String phoneNumber);

    @Query("SELECT o FROM User o WHERE o.active = true AND " +
            "(:keyword IS NULL OR :keyword = '' OR LOWER(o.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(o.address) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(o.phoneNumber) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND LOWER(o.role.name) = 'user' " +
            "ORDER BY o.id")
    Page<User> findAll(@Param("keyword") String keyword, Pageable pageable);


}

