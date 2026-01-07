package com.project.ecommerce.dtos.chatbot;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductSuggestion {
    
    @JsonProperty("id")
    private Long id;
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("price")
    private Float price;
    
    @JsonProperty("thumbnail")
    private String thumbnail;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("reason")
    private String reason; // Lý do AI gợi ý sản phẩm này
}