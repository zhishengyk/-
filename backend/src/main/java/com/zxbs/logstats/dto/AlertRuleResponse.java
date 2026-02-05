package com.zxbs.logstats.dto;

public class AlertRuleResponse {
    private Long id;
    private String name;
    private Integer statusCode;
    private long thresholdCount;
    private int windowMinutes;
    private boolean enabled;
    private String lastTriggeredAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public long getThresholdCount() {
        return thresholdCount;
    }

    public void setThresholdCount(long thresholdCount) {
        this.thresholdCount = thresholdCount;
    }

    public int getWindowMinutes() {
        return windowMinutes;
    }

    public void setWindowMinutes(int windowMinutes) {
        this.windowMinutes = windowMinutes;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getLastTriggeredAt() {
        return lastTriggeredAt;
    }

    public void setLastTriggeredAt(String lastTriggeredAt) {
        this.lastTriggeredAt = lastTriggeredAt;
    }
}
