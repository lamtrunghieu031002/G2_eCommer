package com.project.productservice.clients;

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

    /**
     * Tra ve ten hien thi cua user, hoac null neu user khong ton tai.
     */
    public String getUserName(Long userId) {
        try {
            Map<?, ?> user = restTemplate.getForObject(
                    userServiceUrl + "/internal/users/" + userId, Map.class);
            if (user == null) {
                return null;
            }
            Object fullName = user.get("fullName");
            return fullName != null ? fullName.toString() : null;
        } catch (Exception e) {
            log.warn("Cannot fetch user {} from user-service: {}", userId, e.getMessage());
            return null;
        }
    }

    public boolean userExists(Long userId) {
        try {
            Map<?, ?> user = restTemplate.getForObject(
                    userServiceUrl + "/internal/users/" + userId, Map.class);
            return user != null;
        } catch (Exception e) {
            return false;
        }
    }
}
