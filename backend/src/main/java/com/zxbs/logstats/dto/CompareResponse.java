package com.zxbs.logstats.dto;

import java.util.Map;

public class CompareResponse {
    private long totalA;
    private long totalB;
    private Map<String, Long> levelDiff;
    private Map<Integer, Long> statusDiff;
    private PerformanceResponse performanceA;
    private PerformanceResponse performanceB;

    public long getTotalA() {
        return totalA;
    }

    public void setTotalA(long totalA) {
        this.totalA = totalA;
    }

    public long getTotalB() {
        return totalB;
    }

    public void setTotalB(long totalB) {
        this.totalB = totalB;
    }

    public Map<String, Long> getLevelDiff() {
        return levelDiff;
    }

    public void setLevelDiff(Map<String, Long> levelDiff) {
        this.levelDiff = levelDiff;
    }

    public Map<Integer, Long> getStatusDiff() {
        return statusDiff;
    }

    public void setStatusDiff(Map<Integer, Long> statusDiff) {
        this.statusDiff = statusDiff;
    }

    public PerformanceResponse getPerformanceA() {
        return performanceA;
    }

    public void setPerformanceA(PerformanceResponse performanceA) {
        this.performanceA = performanceA;
    }

    public PerformanceResponse getPerformanceB() {
        return performanceB;
    }

    public void setPerformanceB(PerformanceResponse performanceB) {
        this.performanceB = performanceB;
    }
}
