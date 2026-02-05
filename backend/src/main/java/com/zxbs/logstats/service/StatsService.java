package com.zxbs.logstats.service;

import com.zxbs.logstats.dto.CompareResponse;
import com.zxbs.logstats.dto.ErrorTrendPoint;
import com.zxbs.logstats.dto.PerformanceResponse;
import com.zxbs.logstats.model.NodeStats;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StatsService {
    private final SegmentTreeService segmentTreeService;

    public StatsService(SegmentTreeService segmentTreeService) {
        this.segmentTreeService = segmentTreeService;
    }

    public NodeStats summary(LocalDateTime start, LocalDateTime end) {
        return segmentTreeService.query(start, end);
    }

    public PerformanceResponse performance(LocalDateTime start, LocalDateTime end) {
        NodeStats stats = segmentTreeService.query(start, end);
        return new PerformanceResponse(stats.getAvgResponse(), stats.getResponseMax(), stats.getResponseMin());
    }

    public List<ErrorTrendPoint> errorTrend(LocalDateTime start, LocalDateTime end, int statusCode, int bucketMinutes) {
        List<ErrorTrendPoint> points = new ArrayList<>();
        LocalDateTime cursor = start;
        while (!cursor.isAfter(end)) {
            LocalDateTime bucketEnd = cursor.plusMinutes(bucketMinutes).minusSeconds(1);
            NodeStats stats = segmentTreeService.query(cursor, bucketEnd);
            long count = stats.getStatusCounts().getOrDefault(statusCode, 0L);
            points.add(new ErrorTrendPoint(segmentTreeService.formatBucket(cursor), count));
            cursor = cursor.plusMinutes(bucketMinutes);
        }
        return points;
    }

    public CompareResponse compare(LocalDateTime startA, LocalDateTime endA, LocalDateTime startB, LocalDateTime endB) {
        NodeStats a = segmentTreeService.query(startA, endA);
        NodeStats b = segmentTreeService.query(startB, endB);
        CompareResponse resp = new CompareResponse();
        resp.setTotalA(a.getTotalCount());
        resp.setTotalB(b.getTotalCount());
        resp.setPerformanceA(new PerformanceResponse(a.getAvgResponse(), a.getResponseMax(), a.getResponseMin()));
        resp.setPerformanceB(new PerformanceResponse(b.getAvgResponse(), b.getResponseMax(), b.getResponseMin()));
        resp.setLevelDiff(diffMap(a.getLevelCounts(), b.getLevelCounts()));
        resp.setStatusDiff(diffMapInt(a.getStatusCounts(), b.getStatusCounts()));
        return resp;
    }

    private Map<String, Long> diffMap(Map<String, Long> a, Map<String, Long> b) {
        Map<String, Long> diff = new HashMap<>();
        for (String key : a.keySet()) {
            diff.put(key, a.getOrDefault(key, 0L) - b.getOrDefault(key, 0L));
        }
        for (String key : b.keySet()) {
            diff.putIfAbsent(key, 0L - b.getOrDefault(key, 0L));
        }
        return diff;
    }

    private Map<Integer, Long> diffMapInt(Map<Integer, Long> a, Map<Integer, Long> b) {
        Map<Integer, Long> diff = new HashMap<>();
        for (Integer key : a.keySet()) {
            diff.put(key, a.getOrDefault(key, 0L) - b.getOrDefault(key, 0L));
        }
        for (Integer key : b.keySet()) {
            diff.putIfAbsent(key, 0L - b.getOrDefault(key, 0L));
        }
        return diff;
    }
}
