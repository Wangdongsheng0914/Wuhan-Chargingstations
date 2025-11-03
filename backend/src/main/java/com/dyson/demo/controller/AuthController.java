package com.dyson.demo.controller;

import com.dyson.demo.dto.AuthResponse;
import com.dyson.demo.dto.LoginRequest;
import com.dyson.demo.dto.RegisterRequest;
import com.dyson.demo.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173") // Vue默认端口
public class AuthController {

    @Autowired
    private UserService userService;

    /**
     * 用户注册接口
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request, 
                                                  BindingResult bindingResult) {
        try {
            // 验证请求参数
            if (bindingResult.hasErrors()) {
                String errorMessage = bindingResult.getAllErrors().get(0).getDefaultMessage();
                return ResponseEntity.badRequest().body(new AuthResponse(false, errorMessage));
            }

            AuthResponse response = userService.register(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace(); // 打印详细错误信息到控制台
            return ResponseEntity.internalServerError()
                    .body(new AuthResponse(false, "服务器错误: " + e.getMessage()));
        }
    }

    /**
     * 用户登录接口
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request,
                                               BindingResult bindingResult) {
        // 验证请求参数
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getAllErrors().get(0).getDefaultMessage();
            return ResponseEntity.badRequest().body(new AuthResponse(false, errorMessage));
        }

        AuthResponse response = userService.login(request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/user/car")
    public ResponseEntity<?> updateUserCarModel(@RequestBody Map<String, String> payload) {
        String username = payload.get("username");
        String carModel = payload.get("carModel");

        if (username == null || carModel == null) {
            return ResponseEntity.badRequest().body("Username and carModel are required.");
        }

        try {
            userService.updateUserCarModel(username, carModel);
            return ResponseEntity.ok().body("User car model updated successfully.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @PutMapping("/user/username")
    public ResponseEntity<?> updateUsername(@RequestBody Map<String, String> payload) {
        String oldUsername = payload.get("oldUsername");
        String newUsername = payload.get("newUsername");
        String password = payload.get("password");

        if (oldUsername == null || newUsername == null || password == null) {
            return ResponseEntity.badRequest().body("oldUsername, newUsername and password are required.");
        }

        try {
            userService.updateUsername(oldUsername, newUsername, password);
            return ResponseEntity.ok().body("Username updated successfully.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @PutMapping("/user/password")
    public ResponseEntity<?> updatePassword(@RequestBody Map<String, String> payload) {
        String username = payload.get("username");
        String oldPassword = payload.get("oldPassword");
        String newPassword = payload.get("newPassword");

        if (username == null || oldPassword == null || newPassword == null) {
            return ResponseEntity.badRequest().body("username, oldPassword and newPassword are required.");
        }

        try {
            userService.updatePassword(username, oldPassword, newPassword);
            return ResponseEntity.ok().body("Password updated successfully.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    /**
     * 测试接口
     */
    @GetMapping("/test")
    public ResponseEntity<Map<String, String>> test() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "后端服务正常运行");
        return ResponseEntity.ok(response);
    }
}

