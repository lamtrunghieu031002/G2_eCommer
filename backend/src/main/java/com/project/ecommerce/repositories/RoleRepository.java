package com.project.ecommerce.repositories;

import com.project.ecommerce.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
}
