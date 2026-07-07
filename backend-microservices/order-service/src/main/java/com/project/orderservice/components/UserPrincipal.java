package com.project.orderservice.components;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Thong tin nguoi dung lay tu JWT claims (khong load tu database).
 */
@Getter
@AllArgsConstructor
public class UserPrincipal {
    private Long id;
    private String phoneNumber;
    private String role;
}
