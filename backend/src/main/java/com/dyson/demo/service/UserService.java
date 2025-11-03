package com.dyson.demo.service;

import com.dyson.demo.dto.AuthResponse;
import com.dyson.demo.dto.LoginRequest;
import com.dyson.demo.dto.RegisterRequest;
import com.dyson.demo.entity.User;
import com.dyson.demo.repository.UserRepository;
import com.dyson.demo.util.IdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 用户注册
     */
    public AuthResponse register(RegisterRequest request) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(request.getUsername())) {
            return new AuthResponse(false, "用户名已存在");
        }

        // 检查邮箱是否已存在
        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            if (userRepository.existsByEmail(request.getEmail())) {
                return new AuthResponse(false, "邮箱已被注册");
            }
        }

        // 生成唯一的用户 ID（格式: CS + 5位随机数字）
        String userId;
        do {
            userId = IdGenerator.generateUserId();
        } while (userRepository.existsById(userId)); // 确保 ID 唯一

        // 创建新用户
        User user = new User();
        user.setId(userId); // ID将由数据库自动生成，不再需要手动设置
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));  // BCrypt 加密密码
        user.setEmail(request.getEmail());
        // carModel 和 batteryLevel 在注册时为 null，后续用户自己填写

        User savedUser = userRepository.save(user);

        // 返回成功响应
        AuthResponse.UserInfo userInfo = new AuthResponse.UserInfo(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail(),
                savedUser.getCarModel()
        );

        return new AuthResponse(true, "注册成功", userInfo);
    }

    /**
     * 用户登录
     */
    public AuthResponse login(LoginRequest request) {
        // 查找用户
        Optional<User> userOptional = userRepository.findByUsername(request.getUsername());

        if (userOptional.isEmpty()) {
            return new AuthResponse(false, "用户名不存在");
        }

        User user = userOptional.get();

        // 验证密码（使用 BCrypt 比对）
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return new AuthResponse(false, "密码错误");
        }

        // 登录成功
        AuthResponse.UserInfo userInfo = new AuthResponse.UserInfo(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getCarModel()
        );

        return new AuthResponse(true, "登录成功", userInfo);
    }

    public void updateUserCarModel(String username, String carModel) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
        user.setCarModel(carModel);
        userRepository.save(user);
    }

    /**
     * 更新用户名
     */
    public void updateUsername(String oldUsername, String newUsername, String password) {
        // 查找用户
        User user = userRepository.findByUsername(oldUsername)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 验证密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        // 检查新用户名是否已存在
        if (userRepository.existsByUsername(newUsername)) {
            throw new RuntimeException("用户名已存在");
        }

        // 更新用户名
        user.setUsername(newUsername);
        userRepository.save(user);
    }

    /**
     * 更新密码
     */
    public void updatePassword(String username, String oldPassword, String newPassword) {
        // 查找用户
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("原密码错误");
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}

