package com.zxbs.logstats.controller;

import com.zxbs.logstats.dto.ApiResponse;
import com.zxbs.logstats.dto.AuthRequest;
import com.zxbs.logstats.dto.AuthResponse;
import com.zxbs.logstats.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        return ApiResponse.ok(authService.login(request));
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Map<String, Object>> me(Authentication authentication) {
        List<String> authorities = authentication.getAuthorities().stream()
                .map(it -> it.getAuthority())
                .collect(Collectors.toList());
        return ApiResponse.ok(Map.of(
                "username", authentication.getName(),
                "authorities", authorities
        ));
    }
}
