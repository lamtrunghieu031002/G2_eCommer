package com.project.paymentservice.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.InetAddress;

@RestController
@RequestMapping("${api.prefix}/healthcheck")
public class HealthCheckController {
    @Value("${spring.application.name}")
    private String serviceName;

    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        try {
            String computerName = InetAddress.getLocalHost().getHostName();
            return ResponseEntity.ok("ok, service: " + serviceName + ", host: " + computerName);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("failed");
        }
    }
}
