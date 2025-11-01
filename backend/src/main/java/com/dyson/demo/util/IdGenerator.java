package com.dyson.demo.util;

import java.util.Random;

/**
 * ID 生成器工具类
 * 生成格式: CS + 5位随机数字 (例如: CS12345, CS98765)
 */
public class IdGenerator {
    
    private static final String PREFIX = "CS";
    private static final Random random = new Random();
    
    /**
     * 生成用户 ID
     * @return 格式为 CS + 5位随机数字的 ID
     */
    public static String generateUserId() {
        // 生成 10000 到 99999 之间的随机数（保证是5位数字）
        int randomNumber = 10000 + random.nextInt(90000);
        return PREFIX + randomNumber;
    }
    
    /**
     * 验证 ID 格式是否正确
     * @param id 要验证的 ID
     * @return true 如果格式正确
     */
    public static boolean isValidUserId(String id) {
        if (id == null || id.length() != 7) {
            return false;
        }
        if (!id.startsWith(PREFIX)) {
            return false;
        }
        String numberPart = id.substring(2);
        try {
            int number = Integer.parseInt(numberPart);
            return number >= 10000 && number <= 99999;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}

