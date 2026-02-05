package com.zxbs.logstats.controller;

import com.zxbs.logstats.dto.AlertRuleRequest;
import com.zxbs.logstats.dto.AlertRuleResponse;
import com.zxbs.logstats.dto.ApiResponse;
import com.zxbs.logstats.entity.AlertRule;
import com.zxbs.logstats.service.AlertService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/alerts")
@PreAuthorize("hasRole('ADMIN')")
public class AlertRuleController {
    private final AlertService alertService;

    public AlertRuleController(AlertService alertService) {
        this.alertService = alertService;
    }

    @GetMapping
    public ApiResponse<List<AlertRuleResponse>> list() {
        List<AlertRuleResponse> data = alertService.list().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ApiResponse.ok(data);
    }

    @PostMapping
    public ApiResponse<AlertRuleResponse> create(@Valid @RequestBody AlertRuleRequest req) {
        AlertRule rule = new AlertRule();
        rule.setName(req.getName());
        rule.setStatusCode(req.getStatusCode());
        rule.setThresholdCount(req.getThresholdCount());
        rule.setWindowMinutes(req.getWindowMinutes());
        rule.setEnabled(req.isEnabled());
        return ApiResponse.ok(toResponse(alertService.create(rule)));
    }

    @PutMapping("/{id}")
    public ApiResponse<AlertRuleResponse> update(@PathVariable Long id, @Valid @RequestBody AlertRuleRequest req) {
        AlertRule rule = new AlertRule();
        rule.setName(req.getName());
        rule.setStatusCode(req.getStatusCode());
        rule.setThresholdCount(req.getThresholdCount());
        rule.setWindowMinutes(req.getWindowMinutes());
        rule.setEnabled(req.isEnabled());
        return ApiResponse.ok(toResponse(alertService.update(id, rule)));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable Long id) {
        alertService.delete(id);
        return ApiResponse.ok("OK");
    }

    private AlertRuleResponse toResponse(AlertRule rule) {
        AlertRuleResponse resp = new AlertRuleResponse();
        resp.setId(rule.getId());
        resp.setName(rule.getName());
        resp.setStatusCode(rule.getStatusCode());
        resp.setThresholdCount(rule.getThresholdCount());
        resp.setWindowMinutes(rule.getWindowMinutes());
        resp.setEnabled(rule.isEnabled());
        resp.setLastTriggeredAt(rule.getLastTriggeredAt() == null ? null : rule.getLastTriggeredAt().toString());
        return resp;
    }
}
