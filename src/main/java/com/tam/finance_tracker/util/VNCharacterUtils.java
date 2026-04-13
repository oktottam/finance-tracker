package com.tam.finance_tracker.util;

import java.text.Normalizer;
import java.util.regex.Pattern;

public class VNCharacterUtils { // Đổi tên để tránh trùng với Spring Utils
    
    private static final Pattern DIACRITICAL_MARKS = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

    public static String normalizeForSearch(String input) {
        if (input == null) return "";
        
        // 1. Chuyển về chữ thường và dọn dẹp khoảng trắng
        String temp = input.toLowerCase().trim();
        
        // 2. Khử dấu tiếng Việt chuẩn Unicode
        temp = Normalizer.normalize(temp, Normalizer.Form.NFD);
        temp = DIACRITICAL_MARKS.matcher(temp).replaceAll("");
        
        // 3. Xử lý các ký tự đặc biệt không nằm trong dải diacritical marks
        return temp.replace("đ", "d")
                   .replace("ð", "d") // Một số bảng mã cũ
                   .replaceAll("[^a-z0-9\\s]", ""); // Xóa luôn các ký tự đặc biệt còn sót lại nếu cần
    }
}