package com.project.ecommerce.services.chatbot.intent;

import lombok.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class đại diện cho một Intent đã được phân tích từ câu hỏi của user
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Intent {
    
    /**
     * Loại intent
     */
    private IntentType type;
    
    /**
     * Độ tin cậy (0.0 - 1.0)
     */
    private Double confidence;
    
    /**
     * Các từ khóa được trích xuất
     */
    @Builder.Default
    private List<String> keywords = new ArrayList<>();
    
    /**
     * Tên sản phẩm được trích xuất (nếu có)
     */
    private String productName;
    
    /**
     * Tên thương hiệu (brand)
     */
    private String brand;
    
    /**
     * Category (điện thoại, laptop...)
     */
    private String category;
    
    /**
     * Giá tối thiểu
     */
    private Float minPrice;
    
    /**
     * Giá tối đa
     */
    private Float maxPrice;
    
    /**
     * Dung lượng (256GB, 512GB...)
     */
    private String storage;
    
    /**
     * RAM
     */
    private String ram;
    
    /**
     * Màu sắc
     */
    private String color;
    
    /**
     * Các tham số bổ sung (key-value pairs)
     */
    @Builder.Default
    private Map<String, Object> parameters = new HashMap<>();
    
    /**
     * Câu hỏi gốc của user
     */
    private String originalMessage;
    
    /**
     * Có cần kiểm tra tồn kho không
     */
    private Boolean needsStockCheck;
    
    /**
     * Có cần hiển thị variants không
     */
    private Boolean needsVariants;
    
    /**
     * Số lượng sản phẩm tối đa cần trả về
     */
    @Builder.Default
    private Integer maxResults = 5;
    
    /**
     * Thêm keyword
     */
    public void addKeyword(String keyword) {
        if (this.keywords == null) {
            this.keywords = new ArrayList<>();
        }
        if (!this.keywords.contains(keyword.toLowerCase())) {
            this.keywords.add(keyword.toLowerCase());
        }
    }
    
    /**
     * Thêm parameter
     */
    public void addParameter(String key, Object value) {
        if (this.parameters == null) {
            this.parameters = new HashMap<>();
        }
        this.parameters.put(key, value);
    }
    
    /**
     * Kiểm tra có filter giá không
     */
    public boolean hasPriceFilter() {
        return minPrice != null || maxPrice != null;
    }
    
    /**
     * Kiểm tra có đủ thông tin để query không
     */
    public boolean isValid() {
        if (type == null || type == IntentType.UNKNOWN) {
            return false;
        }
        
        // Intent liên quan sản phẩm phải có ít nhất 1 keyword hoặc brand hoặc category
        if (type.isProductRelated()) {
            return !keywords.isEmpty() || brand != null || category != null || productName != null;
        }
        
        return true;
    }
    
    /**
     * Lấy description dễ đọc của intent
     */
    public String getDescription() {
        StringBuilder desc = new StringBuilder();
        desc.append("Intent: ").append(type.name());
        
        if (productName != null) {
            desc.append(", Product: ").append(productName);
        }
        
        if (brand != null) {
            desc.append(", Brand: ").append(brand);
        }
        
        if (category != null) {
            desc.append(", Category: ").append(category);
        }
        
        if (hasPriceFilter()) {
            desc.append(", Price Range: ");
            if (minPrice != null) desc.append(minPrice).append(" - ");
            if (maxPrice != null) desc.append(maxPrice);
        }
        
        if (!keywords.isEmpty()) {
            desc.append(", Keywords: ").append(String.join(", ", keywords));
        }
        
        return desc.toString();
    }
}
