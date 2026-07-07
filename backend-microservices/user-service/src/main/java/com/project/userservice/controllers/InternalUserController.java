package com.project.userservice.controllers;

import com.project.userservice.exceptions.DataNotFoundException;
import com.project.userservice.models.User;
import com.project.userservice.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * API noi bo cho cac service khac (order-service, product-service).
 * Khong duoc expose qua API Gateway.
 */
@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
public class InternalUserController {
    private final UserRepository userRepository;

    @GetMapping("/{id}")
    public Map<String, Object> getUser(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Cannot find user with id: " + id));
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("id", user.getId());
        result.put("fullName", user.getFullName());
        result.put("phoneNumber", user.getPhoneNumber());
        result.put("address", user.getAddress());
        result.put("active", user.isActive());
        return result;
    }
}
