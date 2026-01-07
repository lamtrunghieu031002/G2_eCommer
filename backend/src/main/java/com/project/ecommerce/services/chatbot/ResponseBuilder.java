package com.project.ecommerce.services.chatbot;

import com.project.ecommerce.models.Product;
import com.project.ecommerce.models.ProductVariant;
import com.project.ecommerce.repositories.VariantRepository;
import com.project.ecommerce.services.chatbot.intent.Intent;
import com.project.ecommerce.services.chatbot.intent.IntentType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Component xây dựng câu trả lời có cấu trúc từ dữ liệu database
 * KHÔNG SỬ DỤNG AI - Chỉ format dữ liệu thành text có cấu trúc
 */
@Component
@RequiredArgsConstructor
public class ResponseBuilder {
    
    private final VariantRepository variantRepository;
    
    /**
     * Xây dựng response text dựa trên Intent và Products
     * Text này sẽ được gửi cho Gemini để format lại thành câu tự nhiên
     */
    public String buildStructuredResponse(Intent intent, List<Product> products) {
        if (products.isEmpty()) {
            return buildNoProductResponse(intent);
        }
        
        System.out.println("===== RESPONSE BUILDER =====");
        System.out.println("Building response for " + products.size() + " products");
        
        StringBuilder response = new StringBuilder();
        
        // Thêm context về intent
        response.append("INTENT: ").append(intent.getType().name()).append("\n");
        response.append("QUERY: ").append(intent.getOriginalMessage()).append("\n\n");
        
        switch (intent.getType()) {
            case CHECK_STOCK:
                response.append(buildStockCheckResponse(products));
                break;
                
            case PRICE_INQUIRY:
                response.append(buildPriceInquiryResponse(products));
                break;
                
            case COMPARE_PRODUCTS:
                response.append(buildComparisonResponse(products));
                break;
                
            case FIND_BY_BUDGET:
                response.append(buildBudgetResponse(products, intent));
                break;
                
            default:
                response.append(buildGeneralProductResponse(products, intent));
                break;
        }
        
        System.out.println("Structured response built (" + response.length() + " chars)");
        System.out.println("===== RESPONSE BUILDER COMPLETE =====\n");
        
        return response.toString();
    }
    
    /**
     * Response khi không tìm thấy sản phẩm
     * ✅ ĐƠN GIẢN HÓA - CHỈ CUNG CẤP CONTEXT, ĐỂ AI TỰ TẠO CÂU TRẢ LỜI
     */
    private String buildNoProductResponse(Intent intent) {
        StringBuilder context = new StringBuilder();
        
        context.append("CONTEXT: Khách hàng tìm kiếm nhưng không có sản phẩm khớp chính xác.\n\n");
        
        context.append("THÔNG TIN TÌM KIẾM:\n");
        
        if (intent.getProductName() != null) {
            context.append("- Tên sản phẩm: ").append(intent.getProductName()).append("\n");
        }
        
        if (intent.getCategory() != null) {
            context.append("- Danh mục: ").append(intent.getCategory()).append("\n");
        }
        
        if (intent.getBrand() != null) {
            context.append("- Thương hiệu: ").append(intent.getBrand()).append("\n");
        }
        
        if (!intent.getKeywords().isEmpty()) {
            context.append("- Từ khóa: ").append(String.join(", ", intent.getKeywords())).append("\n");
        }
        
        if (intent.getMaxPrice() != null) {
            context.append("- Ngân sách tối đa: ").append(formatPrice(intent.getMaxPrice())).append("\n");
        }
        
        if (intent.getStorage() != null) {
            context.append("- Dung lượng: ").append(intent.getStorage()).append("\n");
        }
        
        // ✅ YÊU CẦU AI TỰ ĐỘNG GỢI Ý - KHÔNG QUÁ CỨNG NHẮC
        context.append("\nHÃY TRẢ LỜI TỰ NHIÊN:\n");
        context.append("- Xin lỗi khách vì không tìm thấy sản phẩm chính xác\n");
        context.append("- Gợi ý 2-3 hướng thay thế (hỏi thêm về ngân sách, thương hiệu, hoặc mục đích sử dụng)\n");
        context.append("- Giữ giọng điệu thân thiện, như nhân viên tư vấn thực\n");
        context.append("- KHÔNG liệt kê dạng bullet points, hãy nói như trò chuyện bình thường\n");
        
        return context.toString();
    }
    
    /**
     * Response cho stock check
     */
    private String buildStockCheckResponse(List<Product> products) {
        StringBuilder response = new StringBuilder();
        
        for (Product product : products) {
            response.append("SẢN PHẨM: ").append(product.getName()).append("\n");
            response.append("GIÁ: ").append(formatPrice(product.getPrice())).append("\n");
            
            // Lấy variants để check stock
            List<ProductVariant> variants = variantRepository.findByProductId(product.getId());
            
            if (variants.isEmpty()) {
                response.append("TÌNH TRẠNG: Hết hàng\n");
            } else {
                response.append("CÁC PHIÊN BẢN:\n");
                for (ProductVariant variant : variants) {
                    response.append("  - ").append(variant.getName()).append(": ");
                    if (variant.getQuantity() > 0) {
                        response.append("Còn ").append(variant.getQuantity()).append(" sản phẩm\n");
                    } else {
                        response.append("Hết hàng\n");
                    }
                }
            }
            
            response.append("\n");
        }
        
        return response.toString();
    }
    
    /**
     * Response cho price inquiry
     */
    private String buildPriceInquiryResponse(List<Product> products) {
        StringBuilder response = new StringBuilder();
        
        for (Product product : products) {
            response.append("SẢN PHẨM: ").append(product.getName()).append("\n");
            response.append("GIÁ: ").append(formatPrice(product.getPrice())).append("\n");
            
            // Thêm variants nếu có giá khác
            List<ProductVariant> variants = variantRepository.findByProductId(product.getId());
            if (!variants.isEmpty()) {
                boolean hasDifferentPrices = variants.stream()
                    .anyMatch(v -> v.getPrice() != null && !v.getPrice().equals(product.getPrice()));
                
                if (hasDifferentPrices) {
                    response.append("CÁC PHIÊN BẢN:\n");
                    for (ProductVariant variant : variants) {
                        if (variant.getPrice() != null) {
                            response.append("  - ").append(variant.getName())
                                   .append(": ").append(formatPrice(variant.getPrice())).append("\n");
                        }
                    }
                }
            }
            
            response.append("\n");
        }
        
        return response.toString();
    }
    
    /**
     * Response cho comparison
     */
    private String buildComparisonResponse(List<Product> products) {
        if (products.size() < 2) {
            return "Cần ít nhất 2 sản phẩm để so sánh.\n";
        }
        
        StringBuilder response = new StringBuilder();
        response.append("SO SÁNH SẢN PHẨM:\n\n");
        
        Product p1 = products.get(0);
        Product p2 = products.get(1);
        
        response.append("1. ").append(p1.getName()).append("\n");
        response.append("   - Giá: ").append(formatPrice(p1.getPrice())).append("\n");
        if (p1.getDescription() != null) {
            response.append("   - Mô tả: ").append(p1.getDescription().substring(0, Math.min(100, p1.getDescription().length()))).append("...\n");
        }
        
        response.append("\n");
        
        response.append("2. ").append(p2.getName()).append("\n");
        response.append("   - Giá: ").append(formatPrice(p2.getPrice())).append("\n");
        if (p2.getDescription() != null) {
            response.append("   - Mô tả: ").append(p2.getDescription().substring(0, Math.min(100, p2.getDescription().length()))).append("...\n");
        }
        
        response.append("\n");
        
        // Chênh lệch giá
        float priceDiff = Math.abs(p1.getPrice() - p2.getPrice());
        response.append("CHÊNH LỆCH GIÁ: ").append(formatPrice(priceDiff)).append("\n");
        
        if (p1.getPrice() < p2.getPrice()) {
            response.append("→ ").append(p1.getName()).append(" rẻ hơn\n");
        } else {
            response.append("→ ").append(p2.getName()).append(" rẻ hơn\n");
        }
        
        return response.toString();
    }
    
    /**
     * Response cho budget search
     */
    private String buildBudgetResponse(List<Product> products, Intent intent) {
        StringBuilder response = new StringBuilder();
        
        response.append("CÁC SẢN PHẨM TRONG NGÂN SÁCH ");
        if (intent.getMaxPrice() != null) {
            response.append("DƯỚI ").append(formatPrice(intent.getMaxPrice()));
        }
        response.append(":\n\n");
        
        // Limit to top 3
        int count = Math.min(3, products.size());
        for (int i = 0; i < count; i++) {
            Product p = products.get(i);
            response.append((i + 1)).append(". ").append(p.getName()).append("\n");
            response.append("   - Giá: ").append(formatPrice(p.getPrice())).append("\n");
            
            // Check stock
            List<ProductVariant> variants = variantRepository.findByProductId(p.getId());
            boolean hasStock = variants.stream().anyMatch(v -> v.getQuantity() > 0);
            response.append("   - Tình trạng: ").append(hasStock ? "Còn hàng" : "Hết hàng").append("\n");
            
            response.append("\n");
        }
        
        return response.toString();
    }
    
    /**
     * Response chung cho các intent khác
     */
    private String buildGeneralProductResponse(List<Product> products, Intent intent) {
        StringBuilder response = new StringBuilder();
        
        response.append("TÌM THẤY ").append(products.size()).append(" SẢN PHẨM PHÙ HỢP:\n\n");
        
        // Limit to top 3
        int count = Math.min(3, products.size());
        for (int i = 0; i < count; i++) {
            Product p = products.get(i);
            response.append((i + 1)).append(". ").append(p.getName()).append("\n");
            response.append("   - Giá: ").append(formatPrice(p.getPrice())).append("\n");
            
            // Thêm variants nếu intent cần
            if (intent.getNeedsVariants() != null && intent.getNeedsVariants()) {
                List<ProductVariant> variants = variantRepository.findByProductId(p.getId());
                if (!variants.isEmpty()) {
                    response.append("   - Phiên bản: ");
                    for (int j = 0; j < Math.min(2, variants.size()); j++) {
                        ProductVariant v = variants.get(j);
                        response.append(v.getName());
                        if (j < variants.size() - 1) response.append(", ");
                    }
                    if (variants.size() > 2) {
                        response.append("...");
                    }
                    response.append("\n");
                }
            }
            
            // Check stock nếu cần
            if (intent.getNeedsStockCheck() != null && intent.getNeedsStockCheck()) {
                List<ProductVariant> variants = variantRepository.findByProductId(p.getId());
                boolean hasStock = variants.stream().anyMatch(v -> v.getQuantity() > 0);
                response.append("   - Tình trạng: ").append(hasStock ? "Còn hàng" : "Hết hàng").append("\n");
            }
            
            response.append("\n");
        }
        
        return response.toString();
    }
    
    /**
     * Response cho greeting
     */
    public String buildGreetingResponse() {
        return "Xin chào! Tôi là trợ lý mua sắm của shop.\n" +
               "Tôi có thể giúp bạn tìm sản phẩm, check giá, hoặc tư vấn sản phẩm phù hợp.\n" +
               "Bạn muốn tìm sản phẩm gì?";
    }
    
    /**
     * Response cho goodbye
     */
    public String buildGoodbyeResponse() {
        return "Cảm ơn bạn đã quan tâm!\n" +
               "Nếu cần hỗ trợ thêm, hãy chat với tôi bất cứ lúc nào nhé. 😊";
    }
    
    /**
     * Response cho FAQ
     */
    public String buildFAQResponse(String faqAnswer) {
        return faqAnswer;
    }
    
    /**
     * Format giá tiền
     */
    private String formatPrice(Float price) {
        if (price >= 1_000_000) {
            float millions = price / 1_000_000;
            if (millions == (int) millions) {
                return String.format("%d triệu", (int) millions);
            } else {
                return String.format("%.1f triệu", millions);
            }
        } else if (price >= 1_000) {
            return String.format("%.0fk", price / 1_000);
        } else {
            return String.format("%.0f đ", price);
        }
    }
}
