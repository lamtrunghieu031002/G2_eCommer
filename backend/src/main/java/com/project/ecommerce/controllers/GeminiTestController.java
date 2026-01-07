package com.project.ecommerce.controllers;

import com.project.ecommerce.responses.ResponseObject;
import lombok.RequiredArgsConstructor;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/test")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class GeminiTestController {

    @Value("${gemini.api.key}")
    private String apiKey;

    private final OkHttpClient okHttpClient;

    /**
     * Test Gemini API với model gemini-pro
     */
    @GetMapping("/gemini-pro")
    public ResponseEntity<ResponseObject> testGeminiPro() {
        try {
            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=" + apiKey;
            String json = "{\"contents\":[{\"parts\":[{\"text\":\"Xin chao\"}]}]}";
            
            okhttp3.RequestBody body = okhttp3.RequestBody.create(json, MediaType.parse("application/json; charset=utf-8"));
            Request request = new Request.Builder().url(url).post(body).build();
            
            Response response = okHttpClient.newCall(request).execute();
            String responseBody = response.body().string();
            
            return ResponseEntity.ok(ResponseObject.builder()
                    .status(HttpStatus.OK)
                    .message("Test gemini-pro - Status: " + response.code())
                    .data(responseBody)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ResponseObject.builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .message("Error: " + e.getMessage())
                    .data(null)
                    .build());
        }
    }

    /**
     * Test Gemini API với model gemini-1.5-pro
     */
    @GetMapping("/gemini-1.5-pro")
    public ResponseEntity<ResponseObject> testGemini15Pro() {
        try {
            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-pro:generateContent?key=" + apiKey;
            String json = "{\"contents\":[{\"parts\":[{\"text\":\"Xin chao\"}]}]}";
            
            okhttp3.RequestBody body = okhttp3.RequestBody.create(json, MediaType.parse("application/json; charset=utf-8"));
            Request request = new Request.Builder().url(url).post(body).build();
            
            Response response = okHttpClient.newCall(request).execute();
            String responseBody = response.body().string();
            
            return ResponseEntity.ok(ResponseObject.builder()
                    .status(HttpStatus.OK)
                    .message("Test gemini-1.5-pro - Status: " + response.code())
                    .data(responseBody)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ResponseObject.builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .message("Error: " + e.getMessage())
                    .data(null)
                    .build());
        }
    }

    /**
     * Liệt kê tất cả models có sẵn
     */
    @GetMapping("/list-models")
    public ResponseEntity<ResponseObject> listModels() {
        try {
            String url = "https://generativelanguage.googleapis.com/v1beta/models?key=" + apiKey;
            
            Request request = new Request.Builder().url(url).get().build();
            
            Response response = okHttpClient.newCall(request).execute();
            String responseBody = response.body().string();
            
            return ResponseEntity.ok(ResponseObject.builder()
                    .status(HttpStatus.OK)
                    .message("List all available models - Status: " + response.code())
                    .data(responseBody)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ResponseObject.builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .message("Error: " + e.getMessage())
                    .data(null)
                    .build());
        }
    }
}
