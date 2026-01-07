package com.project.ecommerce.configurations;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.reactive.function.client.WebClient;

// VÔ HIỆU HÓA CONFIG NÀY - ĐÃ CHUYỂN SANG DÙNG GEMINI
//@Configuration
@Getter
@Setter
public class OpenAIConfig {

    @Value("${openai.api.key:}")
    private String apiKey;

    @Value("${openai.api.url:}")
    private String apiUrl;

    @Value("${openai.api.model:}")
    private String model;

    @Value("${openai.api.max-tokens:1000}")
    private Integer maxTokens;

    @Value("${openai.api.temperature:0.7}")
    private Double temperature;

    //@Bean
    public WebClient openAIWebClient() {
        return WebClient.builder()
                .baseUrl(apiUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}