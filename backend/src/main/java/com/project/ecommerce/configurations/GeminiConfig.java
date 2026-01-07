package com.project.ecommerce.configurations;

import lombok.Getter;
import lombok.Setter;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@Getter
@Setter
public class GeminiConfig {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.model:gemini-2.5-flash}")
    private String model;

    @Value("${gemini.api.max-tokens}")
    private Integer maxTokens;

    @Value("${gemini.api.temperature}")
    private Float temperature;

    @Bean
    public OkHttpClient okHttpClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();
    }
    
    public String getApiUrl() {
        // DÙNG MODEL MỚI NHẤT: gemini-2.5-flash
        return "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";
    }
}
