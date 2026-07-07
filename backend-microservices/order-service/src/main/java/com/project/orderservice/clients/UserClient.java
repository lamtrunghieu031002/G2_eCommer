package com.project.orderservice.clients;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Goi user-service de lay thong tin user (thay cho UserRepository truoc day).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UserClient {
    private final RestTemplate restTemplate;

    @Value("${services.user.url}")
    private String userServiceUrl;

    /** Tra ve thong tin user hoac null neu khong ton tai / loi. */
    public Map<String, Object> getUser(Long userId) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> user = restTemplate.getForObject(
                    userServiceUrl + "/internal/users/" + userId, Map.class);
            return user;
        } catch (Exception e) {
            log.warn("Cannot fetch user {} from user-service: {}", userId, e.getMessage());
            return null;
        }
    }
}
