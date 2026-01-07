package com.project.ecommerce.services.chatbot.intent;

import org.springframework.stereotype.Component;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service phân tích Intent từ câu hỏi của user
 * KHÔNG SỬ DỤNG AI - Chỉ dùng pattern matching và keyword extraction
 */
@Component
public class IntentDetector {
    
    // Patterns cho greeting
    private static final List<String> GREETING_PATTERNS = Arrays.asList(
        "xin chào", "chào", "hi", "hello", "hey", "chào bạn", "chào shop"
    );
    
    // Patterns cho goodbye
    private static final List<String> GOODBYE_PATTERNS = Arrays.asList(
        "tạm biệt", "bye", "goodbye", "cảm ơn", "thanks", "thank you", "hẹn gặp lại"
    );
    
    // Patterns cho stock check
    private static final List<String> STOCK_PATTERNS = Arrays.asList(
        "còn hàng", "còn không", "còn ko", "có hàng", "available", "in stock", "tình trạng"
    );
    
    // Patterns cho price inquiry
    private static final List<String> PRICE_PATTERNS = Arrays.asList(
        "giá", "bao nhiêu", "giá bao nhiêu", "price", "cost", "giá tiền", "giá cả"
    );
    
    // Patterns cho comparison
    private static final List<String> COMPARE_PATTERNS = Arrays.asList(
        "so sánh", "khác biệt", "khác gì", "compare", "difference", "phân biệt"
    );
    
    // Patterns cho FAQ
    private static final Map<String, List<String>> FAQ_PATTERNS = Map.of(
        "shipping", Arrays.asList("giao hàng", "vận chuyển", "ship", "delivery"),
        "payment", Arrays.asList("thanh toán", "payment", "trả tiền", "pay"),
        "warranty", Arrays.asList("bảo hành", "warranty", "đổi trả", "return"),
        "policy", Arrays.asList("chính sách", "policy", "điều khoản")
    );
    
    // Brand keywords
    private static final List<String> BRANDS = Arrays.asList(
        "apple", "samsung", "xiaomi", "oppo", "vivo", "realme", "nokia",
        "dell", "hp", "asus", "lenovo", "acer", "msi", "lg", "sony"
    );
    
    // Category keywords
    private static final Map<String, List<String>> CATEGORY_KEYWORDS = Map.of(
        "phone", Arrays.asList("điện thoại", "phone", "smartphone", "iphone", "mobile", "dòng điện thoại", "dòng máy", "điện thoại di động"),
        "laptop", Arrays.asList("laptop", "máy tính", "notebook", "macbook", "dòng laptop"),
        "tablet", Arrays.asList("tablet", "ipad", "máy tính bảng"),
        "headphone", Arrays.asList("tai nghe", "headphone", "earphone", "airpods"),
        "watch", Arrays.asList("đồng hồ", "watch", "smartwatch")
    );
    
    // Storage patterns
    private static final Pattern STORAGE_PATTERN = Pattern.compile("(\\d+)\\s*(gb|tb)", Pattern.CASE_INSENSITIVE);
    
    // RAM patterns
    private static final Pattern RAM_PATTERN = Pattern.compile("(\\d+)\\s*gb\\s*ram", Pattern.CASE_INSENSITIVE);
    
    // Price patterns
    private static final Pattern PRICE_PATTERN = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*(triệu|trieu|tr|million|k)", Pattern.CASE_INSENSITIVE);
    
    /**
     * Phân tích Intent từ message của user
     */
    public Intent detectIntent(String message) {
        if (message == null || message.trim().isEmpty()) {
            return createUnknownIntent(message);
        }
        
        String lowerMessage = message.toLowerCase().trim();
        
        // Log để debug
        System.out.println("===== INTENT DETECTION =====");
        System.out.println("Original Message: " + message);
        
        Intent.IntentBuilder intentBuilder = Intent.builder()
            .originalMessage(message)
            .confidence(0.0)
            .needsStockCheck(false)
            .needsVariants(false)
            .maxResults(5);
        
        // 1. Kiểm tra Greeting
        if (containsAny(lowerMessage, GREETING_PATTERNS)) {
            System.out.println("Detected: GREETING");
            return intentBuilder
                .type(IntentType.GREETING)
                .confidence(1.0)
                .build();
        }
        
        // 2. Kiểm tra Goodbye
        if (containsAny(lowerMessage, GOODBYE_PATTERNS)) {
            System.out.println("Detected: GOODBYE");
            return intentBuilder
                .type(IntentType.GOODBYE)
                .confidence(1.0)
                .build();
        }
        
        // 3. Kiểm tra FAQ
        IntentType faqType = detectFAQ(lowerMessage);
        if (faqType != null) {
            System.out.println("Detected: FAQ");
            
            // ✅ SỬA: Tạo parameters map trước, rồi set vào builder
            Map<String, Object> faqParams = new HashMap<>();
            faqParams.put("faq_category", faqType);
            
            return intentBuilder
                .type(IntentType.FAQ)
                .confidence(0.9)
                .parameters(faqParams)  // ✅ Dùng .parameters() thay vì .addParameter()
                .build();
        }
        
        // 4. Extract thông tin sản phẩm
        String brand = extractBrand(lowerMessage);
        String category = extractCategory(lowerMessage);
        String storage = extractStorage(lowerMessage);
        String ram = extractRAM(lowerMessage);
        Float minPrice = null;
        Float maxPrice = extractMaxPrice(lowerMessage);
        
        // Extract keywords (loại bỏ stopwords)
        List<String> keywords = extractKeywords(lowerMessage);
        
        // 5. Xác định Intent Type
        IntentType intentType = IntentType.UNKNOWN;
        double confidence = 0.5;
        
        // Check comparison
        if (containsAny(lowerMessage, COMPARE_PATTERNS)) {
            intentType = IntentType.COMPARE_PRODUCTS;
            confidence = 0.9;
        }
        // Check stock
        else if (containsAny(lowerMessage, STOCK_PATTERNS)) {
            intentType = IntentType.CHECK_STOCK;
            confidence = 0.95;
            intentBuilder.needsStockCheck(true).needsVariants(true);
        }
        // Check price inquiry
        else if (containsAny(lowerMessage, PRICE_PATTERNS) && !lowerMessage.contains("dưới") && !lowerMessage.contains("trên")) {
            intentType = IntentType.PRICE_INQUIRY;
            confidence = 0.9;
        }
        // Check budget search
        else if (maxPrice != null || lowerMessage.contains("dưới") || lowerMessage.contains("từ") || lowerMessage.contains("khoảng")) {
            intentType = IntentType.FIND_BY_BUDGET;
            confidence = 0.85;
        }
        // Check brand search
        else if (brand != null && category == null) {
            intentType = IntentType.FIND_BY_BRAND;
            confidence = 0.8;
        }
        // Check category search
        else if (category != null && keywords.isEmpty()) {
            intentType = IntentType.FIND_BY_CATEGORY;
            confidence = 0.8;
        }
        // Check specs search
        else if (storage != null || ram != null) {
            intentType = IntentType.FIND_BY_SPECS;
            confidence = 0.75;
        }
        // Default: Product inquiry
        else if (!keywords.isEmpty() || brand != null || category != null) {
            intentType = IntentType.PRODUCT_INQUIRY;
            confidence = 0.7;
        }
        
        System.out.println("Detected Intent Type: " + intentType);
        System.out.println("Confidence: " + confidence);
        System.out.println("Brand: " + brand);
        System.out.println("Category: " + category);
        System.out.println("Keywords: " + keywords);
        System.out.println("Max Price: " + maxPrice);
        
        // Build Intent object
        Intent intent = intentBuilder
            .type(intentType)
            .confidence(confidence)
            .brand(brand)
            .category(category)
            .storage(storage)
            .ram(ram)
            .maxPrice(maxPrice)
            .minPrice(minPrice)
            .keywords(keywords)
            .build();
        
        // Extract product name nếu có từ khóa cụ thể
        String productName = extractProductName(message, keywords);
        if (productName != null) {
            intent.setProductName(productName);
            System.out.println("Product Name: " + productName);
        }
        
        System.out.println("===== INTENT DETECTION COMPLETE =====\n");
        
        return intent;
    }
    
    /**
     * Kiểm tra message có chứa bất kỳ pattern nào không
     */
    private boolean containsAny(String message, List<String> patterns) {
        for (String pattern : patterns) {
            if (message.contains(pattern)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Detect FAQ category
     */
    private IntentType detectFAQ(String message) {
        for (Map.Entry<String, List<String>> entry : FAQ_PATTERNS.entrySet()) {
            if (containsAny(message, entry.getValue())) {
                return IntentType.FAQ;
            }
        }
        return null;
    }
    
    /**
     * Extract brand từ message
     */
    private String extractBrand(String message) {
        for (String brand : BRANDS) {
            if (message.contains(brand)) {
                return brand.substring(0, 1).toUpperCase() + brand.substring(1);
            }
        }
        return null;
    }
    
    /**
     * Extract category từ message
     */
    private String extractCategory(String message) {
        for (Map.Entry<String, List<String>> entry : CATEGORY_KEYWORDS.entrySet()) {
            if (containsAny(message, entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }
    
    /**
     * Extract storage từ message (256GB, 512GB...)
     */
    private String extractStorage(String message) {
        Matcher matcher = STORAGE_PATTERN.matcher(message);
        if (matcher.find()) {
            return matcher.group(1) + matcher.group(2).toUpperCase();
        }
        return null;
    }
    
    /**
     * Extract RAM từ message
     */
    private String extractRAM(String message) {
        Matcher matcher = RAM_PATTERN.matcher(message);
        if (matcher.find()) {
            return matcher.group(1) + "GB";
        }
        return null;
    }
    
    /**
     * Extract giá tối đa từ message
     */
    private Float extractMaxPrice(String message) {
        Matcher matcher = PRICE_PATTERN.matcher(message);
        while (matcher.find()) {
            try {
                float value = Float.parseFloat(matcher.group(1));
                String unit = matcher.group(2).toLowerCase();
                
                if (unit.contains("triệu") || unit.contains("trieu") || unit.equals("tr")) {
                    return value * 1_000_000;
                } else if (unit.equals("k")) {
                    return value * 1_000;
                }
            } catch (NumberFormatException e) {
                // Ignore
            }
        }
        return null;
    }
    
    /**
     * Extract keywords từ message (loại bỏ stopwords)
     */
    private List<String> extractKeywords(String message) {
        List<String> stopwords = Arrays.asList(
            "tôi", "tớ", "mình", "cho", "của", "và", "có", "không", "được", 
            "là", "thì", "như", "để", "bạn", "anh", "chị", "em", "shop",
            "xem", "tìm", "muốn", "cần", "giúp", "với", "về", "nào", "gì",
            "bao nhiêu", "thế nào", "ra sao", "à", "ạ", "nhé", "nha"
        );
        
        String[] words = message.toLowerCase().split("\\s+");
        List<String> keywords = new ArrayList<>();
        
        for (String word : words) {
            // Loại bỏ ký tự đặc biệt
            word = word.replaceAll("[^a-zA-Z0-9àáạảãâầấậẩẫăằắặẳẵèéẹẻẽêềếệểễìíịỉĩòóọỏõôồốộổỗơờớợởỡùúụủũưừứựửữỳýỵỷỹđ]", "");
            
            // Bỏ qua nếu là stopword hoặc quá ngắn
            if (word.length() > 2 && !stopwords.contains(word)) {
                keywords.add(word);
            }
        }
        
        return keywords;
    }
    
    /**
     * Extract product name từ keywords
     */
    private String extractProductName(String message, List<String> keywords) {
        // Nếu có từ 2 keywords trở lên, ghép lại thành product name
        if (keywords.size() >= 2) {
            // Tìm cụm từ dài nhất trong message chứa các keywords
            List<String> productParts = new ArrayList<>();
            String[] words = message.split("\\s+");
            
            for (int i = 0; i < words.length; i++) {
                String word = words[i].toLowerCase().replaceAll("[^a-zA-Z0-9àáạảãâầấậẩẫăằắặẳẵèéẹẻẽêềếệểễìíịỉĩòóọỏõôồốộổỗơờớợởỡùúụủũưừứựửữỳýỵỷỹđ]", "");
                if (keywords.contains(word)) {
                    productParts.add(words[i]);
                }
            }
            
            if (productParts.size() >= 2) {
                return String.join(" ", productParts);
            }
        }
        
        return null;
    }
    
    /**
     * Tạo Unknown Intent
     */
    private Intent createUnknownIntent(String message) {
        return Intent.builder()
            .type(IntentType.UNKNOWN)
            .originalMessage(message)
            .confidence(0.0)
            .build();
    }
}
