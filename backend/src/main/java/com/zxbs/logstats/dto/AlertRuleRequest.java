package com.zxbs.logstats.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class AlertRuleRequest {
    @NotBlank
    private String name;
    private Integer statusCode;
    @NotNull
    private Long thresholdCount;
    @NotNull
    private Integer windowMinutes;
    private boolean enabled = true;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

    public Long getThresholdCount() {
        return thresholdCount;
    }

    public void setThresholdCount(Long thresholdCount) {
        this.thresholdCount = thresholdCount;
    }

    public Integer getWindowMinutes() {
        return windowMinutes;
    }

    public void setWindowMinutes(Integer windowMinutes) {
        this.windowMinutes = windowMinutes;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
