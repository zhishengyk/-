package com.zxbs.logstats.model;

import com.zxbs.logstats.entity.LogEntry;

import java.util.HashMap;
import java.util.Map;

public class NodeStats {
    private long totalCount;
    private Map<String, Long> levelCounts = new HashMap<>();
    private Map<Integer, Long> statusCounts = new HashMap<>();
    private long responseSum;
    private long responseMin = Long.MAX_VALUE;
    private long responseMax = Long.MIN_VALUE;

    public void addLog(LogEntry log) {
        totalCount++;
        levelCounts.merge(log.getLevel(), 1L, Long::sum);
        statusCounts.merge(log.getStatusCode(), 1L, Long::sum);
        long rt = log.getResponseTimeMs();
        responseSum += rt;
        responseMin = Math.min(responseMin, rt);
        responseMax = Math.max(responseMax, rt);
    }

    public void merge(NodeStats other) {
        if (other == null || other.totalCount == 0) {
            return;
        }
        totalCount += other.totalCount;
        other.levelCounts.forEach((k, v) -> levelCounts.merge(k, v, Long::sum));
        other.statusCounts.forEach((k, v) -> statusCounts.merge(k, v, Long::sum));
        responseSum += other.responseSum;
        responseMin = Math.min(responseMin, other.responseMin);
        responseMax = Math.max(responseMax, other.responseMax);
    }

    public NodeStats copy() {
        NodeStats copy = new NodeStats();
        copy.totalCount = totalCount;
        copy.levelCounts = new HashMap<>(levelCounts);
        copy.statusCounts = new HashMap<>(statusCounts);
        copy.responseSum = responseSum;
        copy.responseMin = responseMin;
        copy.responseMax = responseMax;
        return copy;
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

    public long getResponseSum() {
        return responseSum;
    }

    public void setResponseSum(long responseSum) {
        this.responseSum = responseSum;
    }

    public long getResponseMin() {
        return responseMin == Long.MAX_VALUE ? 0 : responseMin;
    }

    public void setResponseMin(long responseMin) {
        this.responseMin = responseMin;
    }

    public long getResponseMax() {
        return responseMax == Long.MIN_VALUE ? 0 : responseMax;
    }

    public void setResponseMax(long responseMax) {
        this.responseMax = responseMax;
    }

    public double getAvgResponse() {
        return totalCount == 0 ? 0 : (double) responseSum / totalCount;
    }
}
