package com.zxbs.logstats.dto;

import java.util.Map;

public class StatsSummaryResponse {
    private long totalCount;
    private Map<String, Long> levelCounts;
    private Map<Integer, Long> statusCounts;

    public StatsSummaryResponse() {
    }

    public StatsSummaryResponse(long totalCount, Map<String, Long> levelCounts, Map<Integer, Long> statusCounts) {
        this.totalCount = totalCount;
        this.levelCounts = levelCounts;
        this.statusCounts = statusCounts;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    public Map<String, Long> getLevelCounts() {
        return levelCounts;
    }

    public void setLevelCounts(Map<String, Long> levelCounts) {
        this.levelCounts = levelCounts;
    }

    public Map<Integer, Long> getStatusCounts() {
        return statusCounts;
    }

    public void setStatusCounts(Map<Integer, Long> statusCounts) {
        this.statusCounts = statusCounts;
    }
}
