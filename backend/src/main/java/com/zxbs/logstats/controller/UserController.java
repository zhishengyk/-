package com.zxbs.logstats.controller;

import com.zxbs.logstats.dto.ApiResponse;
import com.zxbs.logstats.dto.UserRequest;
import com.zxbs.logstats.dto.UserResponse;
import com.zxbs.logstats.entity.User;
import com.zxbs.logstats.service.AuditService;
import com.zxbs.logstats.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {
    private final UserService userService;
    private final AuditService auditService;

    public UserController(UserService userService, AuditService auditService) {
        this.userService = userService;
        this.auditService = auditService;
    }

    @GetMapping
    public ApiResponse<List<UserResponse>> list() {
        List<UserResponse> data = userService.list().stream()
                .map(u -> new UserResponse(u.getId(), u.getUsername(), u.getRole().name(), u.isEnabled()))
                .collect(Collectors.toList());
        return ApiResponse.ok(data);
    }

    @PostMapping
    public ApiResponse<UserResponse> create(@Valid @RequestBody UserRequest req, Authentication auth) {
        User user = userService.create(req);
        auditService.record(null, auth != null ? auth.getName() : "admin", "CREATE_USER", "创建用户 " + user.getUsername());
        return ApiResponse.ok(new UserResponse(user.getId(), user.getUsername(), user.getRole().name(), user.isEnabled()));
    }

    @PutMapping("/{id}")
    public ApiResponse<UserResponse> update(@PathVariable Long id, @Valid @RequestBody UserRequest req, Authentication auth) {
        User user = userService.update(id, req);
        auditService.record(null, auth != null ? auth.getName() : "admin", "UPDATE_USER", "更新用户 " + user.getUsername());
        return ApiResponse.ok(new UserResponse(user.getId(), user.getUsername(), user.getRole().name(), user.isEnabled()));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable Long id, Authentication auth) {
        userService.delete(id);
        auditService.record(null, auth != null ? auth.getName() : "admin", "DELETE_USER", "删除用户 " + id);
        return ApiResponse.ok("OK");
    }
}
