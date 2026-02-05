package com.zxbs.logstats.dto;

public class PerformanceResponse {
    private double avgMs;
    private long maxMs;
    private long minMs;

    public PerformanceResponse() {
    }

    public PerformanceResponse(double avgMs, long maxMs, long minMs) {
        this.avgMs = avgMs;
        this.maxMs = maxMs;
        this.minMs = minMs;
    }

    public double getAvgMs() {
        return avgMs;
    }

    public void setAvgMs(double avgMs) {
        this.avgMs = avgMs;
    }

    public long getMaxMs() {
        return maxMs;
    }

    public void setMaxMs(long maxMs) {
        this.maxMs = maxMs;
    }

    public long getMinMs() {
        return minMs;
    }

    public void setMinMs(long minMs) {
        this.minMs = minMs;
    }
}
