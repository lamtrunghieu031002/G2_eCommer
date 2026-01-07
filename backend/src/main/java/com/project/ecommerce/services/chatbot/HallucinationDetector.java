package com.project.ecommerce.services.chatbot;

import org.springframework.stereotype.Component;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Component kiểm tra hallucination (AI tự bịa thông tin)
 * 5 LỚP KIỂM TRA:
 * 1. Số liệu (giá, số lượng)
 * 2. Tên sản phẩm
 * 3. Từ khóa marketing cấm
 * 4. Câu chào/kết dư thừa
 * 5. Thông tin không có trong data gốc
 */
@Component
public class HallucinationDetector {
    
    // Từ khóa marketing CẤM
    private static final List<String> BANNED_MARKETING_WORDS = Arrays.asList(
        "tuyệt vời", "hoàn hảo", "đỉnh cao", "xuất sắc", "tốt nhất",
        "ưu đãi", "khuyến mãi", "giảm giá", "sale", "hot deal",
        "siêu phẩm", "bom tấn", "cực đỉnh", "xịn sò", "đỉnh của chóp"
    );
    
    // Câu kết dư thừa CẤM
    private static final List<String> BANNED_CLOSING_PHRASES = Arrays.asList(
        "hy vọng", "chúc bạn", "mong rằng", "cảm ơn bạn đã",
        "nếu cần hỗ trợ", "liên hệ chúng tôi", "đừng ngại"
    );
    
    // Pattern để extract số
    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d+(?:\\.\\d+)?");
    
    /**
     * Phát hiện hallucination - Return true nếu phát hiện
     */
    public boolean detect(String originalData, String aiResponse) {
        System.out.println("===== HALLUCINATION DETECTION =====");
        
        // Layer 1: Kiểm tra số liệu
        if (hasNumberHallucination(originalData, aiResponse)) {
            System.err.println("❌ FAILED: Number hallucination detected");
            return true;
        }
        
        // Layer 2: Kiểm tra tên sản phẩm
        if (hasProductNameHallucination(originalData, aiResponse)) {
            System.err.println("❌ FAILED: Product name hallucination detected");
            return true;
        }
        
        // Layer 3: Kiểm tra từ marketing
        if (hasMarketingWords(aiResponse)) {
            System.err.println("❌ FAILED: Marketing words detected");
            return true;
        }
        
        // Layer 4: Kiểm tra câu kết dư thừa
        if (hasClosingPhrases(aiResponse)) {
            System.err.println("❌ FAILED: Closing phrases detected");
            return true;
        }
        
        // Layer 5: Kiểm tra độ dài (quá dài = thêm thông tin)
        if (isTooLong(aiResponse)) {
            System.err.println("❌ FAILED: Response too long");
            return true;
        }
        
        System.out.println("✅ PASSED: No hallucination detected");
        System.out.println("===== DETECTION COMPLETE =====\n");
        return false;
    }
    
    /**
     * ✅ Method wrapper cho GeminiFormatter
     * Return TRUE nếu response HỢP LỆ (không có hallucination)
     * Return FALSE nếu phát hiện hallucination
     */
    public boolean isResponseValid(String originalData, String aiResponse) {
        // detect() return TRUE nếu CÓ hallucination
        // isResponseValid() return TRUE nếu KHÔNG CÓ hallucination
        return !detect(originalData, aiResponse);
    }
    
    /**
     * Layer 1: Kiểm tra số liệu (giá, số lượng)
     */
    private boolean hasNumberHallucination(String original, String response) {
        List<String> originalNumbers = extractNumbers(original);
        List<String> responseNumbers = extractNumbers(response);
        
        System.out.println("Original numbers: " + originalNumbers);
        System.out.println("Response numbers: " + responseNumbers);
        
        // Nếu AI thêm số mới không có trong data gốc
        for (String num : responseNumbers) {
            if (!originalNumbers.contains(num)) {
                System.err.println("⚠️ New number found: " + num);
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Layer 2: Kiểm tra tên sản phẩm
     */
    private boolean hasProductNameHallucination(String original, String response) {
        List<String> originalProducts = extractProductNames(original);
        List<String> responseProducts = extractProductNames(response);
        
        System.out.println("Original products: " + originalProducts);
        System.out.println("Response products: " + responseProducts);
        
        // Kiểm tra từng sản phẩm trong response
        for (String respProduct : responseProducts) {
            boolean found = false;
            
            for (String origProduct : originalProducts) {
                // Fuzzy match: chứa hoặc tương tự
                if (respProduct.contains(origProduct) || 
                    origProduct.contains(respProduct) ||
                    isSimilar(respProduct, origProduct)) {
                    found = true;
                    break;
                }
            }
            
            if (!found) {
                System.err.println("⚠️ New product name found: " + respProduct);
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Layer 3: Kiểm tra từ marketing
     */
    private boolean hasMarketingWords(String response) {
        String lowerResponse = response.toLowerCase();
        
        for (String banned : BANNED_MARKETING_WORDS) {
            if (lowerResponse.contains(banned)) {
                System.err.println("⚠️ Marketing word found: " + banned);
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Layer 4: Kiểm tra câu kết dư thừa
     */
    private boolean hasClosingPhrases(String response) {
        String lowerResponse = response.toLowerCase();
        
        for (String banned : BANNED_CLOSING_PHRASES) {
            if (lowerResponse.contains(banned)) {
                System.err.println("⚠️ Closing phrase found: " + banned);
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Layer 5: Kiểm tra độ dài (>500 chars = có thể thêm thông tin)
     */
    private boolean isTooLong(String response) {
        int length = response.length();
        System.out.println("Response length: " + length + " chars");
        
        if (length > 500) {
            System.err.println("⚠️ Response too long (>500 chars)");
            return true;
        }
        
        return false;
    }
    
    /**
     * Extract tất cả số từ text
     */
    private List<String> extractNumbers(String text) {
        List<String> numbers = new ArrayList<>();
        Matcher matcher = NUMBER_PATTERN.matcher(text);
        
        while (matcher.find()) {
            String num = matcher.group();
            // Normalize: loại bỏ số 0 đầu, format decimal
            try {
                double d = Double.parseDouble(num);
                // Chỉ lưu phần nguyên nếu không có phần thập phân
                if (d == (long) d) {
                    numbers.add(String.valueOf((long) d));
                } else {
                    numbers.add(String.valueOf(d));
                }
            } catch (NumberFormatException e) {
                numbers.add(num);
            }
        }
        
        return numbers;
    }
    
    /**
     * Extract tên sản phẩm từ text
     * Tìm các cụm từ viết hoa hoặc có số model
     */
    private List<String> extractProductNames(String text) {
        List<String> products = new ArrayList<>();
        
        // Pattern: Từ viết hoa + số + từ (VD: iPhone 15 Pro Max)
        Pattern productPattern = Pattern.compile("([A-Z][a-zA-Z]*(?:\\s+\\d+)?(?:\\s+[A-Z][a-z]+)*(?:\\s+[A-Z][a-z]+)*)");
        Matcher matcher = productPattern.matcher(text);
        
        while (matcher.find()) {
            String product = matcher.group(1).trim();
            if (product.length() > 3) { // Loại bỏ từ quá ngắn
                products.add(product.toLowerCase());
            }
        }
        
        return products;
    }
    
    /**
     * Kiểm tra 2 string có tương tự không (Levenshtein distance)
     */
    private boolean isSimilar(String s1, String s2) {
        s1 = s1.toLowerCase();
        s2 = s2.toLowerCase();
        
        int distance = levenshteinDistance(s1, s2);
        int maxLength = Math.max(s1.length(), s2.length());
        
        // Nếu khoảng cách < 30% độ dài → Coi là tương tự
        return (double) distance / maxLength < 0.3;
    }
    
    /**
     * Tính Levenshtein distance
     */
    private int levenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];
        
        for (int i = 0; i <= s1.length(); i++) {
            dp[i][0] = i;
        }
        
        for (int j = 0; j <= s2.length(); j++) {
            dp[0][j] = j;
        }
        
        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = 1 + Math.min(
                        Math.min(dp[i - 1][j], dp[i][j - 1]),
                        dp[i - 1][j - 1]
                    );
                }
            }
        }
        
        return dp[s1.length()][s2.length()];
    }
    
    /**
     * Lấy validation report chi tiết
     */
    public ValidationReport getValidationReport(String originalData, String aiResponse) {
        ValidationReport report = new ValidationReport();
        
        report.setNumberValid(!hasNumberHallucination(originalData, aiResponse));
        report.setProductNameValid(!hasProductNameHallucination(originalData, aiResponse));
        report.setMarketingWordsFree(!hasMarketingWords(aiResponse));
        report.setClosingPhrasesFree(!hasClosingPhrases(aiResponse));
        report.setLengthValid(!isTooLong(aiResponse));
        
        report.setPassed(report.isNumberValid() && 
                         report.isProductNameValid() && 
                         report.isMarketingWordsFree() && 
                         report.isClosingPhrasesFree() && 
                         report.isLengthValid());
        
        return report;
    }
    
    /**
     * Inner class: Validation Report
     */
    public static class ValidationReport {
        private boolean passed;
        private boolean numberValid;
        private boolean productNameValid;
        private boolean marketingWordsFree;
        private boolean closingPhrasesFree;
        private boolean lengthValid;
        
        // Getters & Setters
        public boolean isPassed() { return passed; }
        public void setPassed(boolean passed) { this.passed = passed; }
        
        public boolean isNumberValid() { return numberValid; }
        public void setNumberValid(boolean numberValid) { this.numberValid = numberValid; }
        
        public boolean isProductNameValid() { return productNameValid; }
        public void setProductNameValid(boolean productNameValid) { this.productNameValid = productNameValid; }
        
        public boolean isMarketingWordsFree() { return marketingWordsFree; }
        public void setMarketingWordsFree(boolean marketingWordsFree) { this.marketingWordsFree = marketingWordsFree; }
        
        public boolean isClosingPhrasesFree() { return closingPhrasesFree; }
        public void setClosingPhrasesFree(boolean closingPhrasesFree) { this.closingPhrasesFree = closingPhrasesFree; }
        
        public boolean isLengthValid() { return lengthValid; }
        public void setLengthValid(boolean lengthValid) { this.lengthValid = lengthValid; }
        
        @Override
        public String toString() {
            return String.format(
                "ValidationReport{passed=%s, number=%s, productName=%s, marketing=%s, closing=%s, length=%s}",
                passed, numberValid, productNameValid, marketingWordsFree, closingPhrasesFree, lengthValid
            );
        }
    }
}
