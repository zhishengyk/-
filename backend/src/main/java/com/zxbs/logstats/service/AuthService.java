package com.zxbs.logstats.service;

import com.zxbs.logstats.dto.AuthRequest;
import com.zxbs.logstats.dto.AuthResponse;
import com.zxbs.logstats.entity.User;
import com.zxbs.logstats.repository.UserRepository;
import com.zxbs.logstats.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuditService auditService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService, AuditService auditService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.auditService = auditService;
    }

    public AuthResponse login(AuthRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("用户名或密码错误"));
        if (!user.isEnabled()) {
            throw new IllegalArgumentException("账号已被禁用");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("用户名或密码错误");
        }
        String token = jwtService.generateToken(user.getUsername(), user.getRole().name());
        try {
            auditService.record(user.getId(), user.getUsername(), "LOGIN", "用户登录");
        } catch (Exception ignored) {
        }
        return new AuthResponse(token, user.getUsername(), user.getRole().name());
    }
}
