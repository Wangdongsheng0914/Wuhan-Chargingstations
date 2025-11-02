package com.dyson.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private boolean success;
    private String message;
    private UserInfo user;

    public AuthResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserInfo {
        private String id;  // 改为 String 类型（格式: CS + 5位数字）
        private String username;
        private String email;
        private String carModel;
    }
}

