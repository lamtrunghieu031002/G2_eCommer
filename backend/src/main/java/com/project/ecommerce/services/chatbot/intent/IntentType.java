package com.project.ecommerce.services.chatbot.intent;

/**
 * Enum định nghĩa các loại Intent (Ý định) của người dùng
 */
public enum IntentType {
    
    /**
     * Hỏi về sản phẩm cụ thể
     * VD: "iPhone 15 Pro Max có không?", "Cho tôi xem Macbook Air"
     */
    PRODUCT_INQUIRY,
    
    /**
     * Kiểm tra tình trạng còn hàng
     * VD: "iPhone 15 còn hàng không?", "Sản phẩm này còn không?"
     */
    CHECK_STOCK,
    
    /**
     * So sánh sản phẩm
     * VD: "So sánh iPhone 15 và iPhone 14", "Khác biệt giữa X và Y là gì?"
     */
    COMPARE_PRODUCTS,
    
    /**
     * Hỏi về giá sản phẩm
     * VD: "Giá iPhone 15 bao nhiêu?", "Sản phẩm này giá bao nhiêu?"
     */
    PRICE_INQUIRY,
    
    /**
     * Tìm sản phẩm theo ngân sách
     * VD: "Điện thoại dưới 10 triệu", "Laptop từ 15 đến 20 triệu"
     */
    FIND_BY_BUDGET,
    
    /**
     * Tìm sản phẩm theo category
     * VD: "Cho tôi xem điện thoại", "Có laptop nào không?"
     */
    FIND_BY_CATEGORY,
    
    /**
     * Tìm sản phẩm theo thương hiệu
     * VD: "Sản phẩm Apple", "Điện thoại Samsung"
     */
    FIND_BY_BRAND,
    
    /**
     * Tìm sản phẩm theo đặc điểm kỹ thuật
     * VD: "Điện thoại 256GB", "Laptop RAM 16GB"
     */
    FIND_BY_SPECS,
    
    /**
     * Chào hỏi, làm quen
     * VD: "Xin chào", "Hi", "Hello"
     */
    GREETING,
    
    /**
     * Hỏi về chính sách (FAQ)
     * VD: "Chính sách bảo hành?", "Giao hàng như thế nào?"
     */
    FAQ,
    
    /**
     * Cảm ơn, kết thúc
     * VD: "Cảm ơn", "Thanks", "Tạm biệt"
     */
    GOODBYE,
    
    /**
     * Không xác định được intent
     */
    UNKNOWN;
    
    /**
     * Kiểm tra có phải intent cần query DB không
     */
    public boolean requiresDatabaseQuery() {
        return this == PRODUCT_INQUIRY 
            || this == CHECK_STOCK
            || this == COMPARE_PRODUCTS
            || this == PRICE_INQUIRY
            || this == FIND_BY_BUDGET
            || this == FIND_BY_CATEGORY
            || this == FIND_BY_BRAND
            || this == FIND_BY_SPECS;
    }
    
    /**
     * Kiểm tra có phải câu hỏi về sản phẩm không
     */
    public boolean isProductRelated() {
        return requiresDatabaseQuery();
    }
}
