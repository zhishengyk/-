package com.zxbs.logstats.controller;

import com.zxbs.logstats.dto.ApiResponse;
import com.zxbs.logstats.dto.CompareResponse;
import com.zxbs.logstats.dto.PerformanceResponse;
import com.zxbs.logstats.dto.SnapshotInfo;
import com.zxbs.logstats.model.NodeStats;
import com.zxbs.logstats.model.SnapshotData;
import com.zxbs.logstats.model.SnapshotEntry;
import com.zxbs.logstats.service.AuditService;
import com.zxbs.logstats.service.SegmentTreeService;
import com.zxbs.logstats.service.SnapshotService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/snapshots")
public class SnapshotController {
    private final SnapshotService snapshotService;
    private final SegmentTreeService segmentTreeService;
    private final AuditService auditService;

    public SnapshotController(SnapshotService snapshotService, SegmentTreeService segmentTreeService, AuditService auditService) {
        this.snapshotService = snapshotService;
        this.segmentTreeService = segmentTreeService;
        this.auditService = auditService;
    }

    @GetMapping
    public ApiResponse<List<SnapshotInfo>> list() {
        return ApiResponse.ok(snapshotService.listSnapshots());
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<SnapshotEntry> create(@RequestParam(defaultValue = "manual") String note, Authentication auth) {
        SnapshotEntry entry = snapshotService.createSnapshot(note);
        auditService.record(null, auth != null ? auth.getName() : "admin", "SNAPSHOT_CREATE", "创建快照");
        return ApiResponse.ok(entry);
    }

    @PostMapping("/rollback")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<String> rollback(@RequestParam String date, Authentication auth) {
        SnapshotData data = snapshotService.loadSnapshot(date);
        segmentTreeService.loadSnapshot(data);
        auditService.record(null, auth != null ? auth.getName() : "admin", "SNAPSHOT_ROLLBACK", "回滚到 " + date);
        return ApiResponse.ok("OK");
    }

    @GetMapping("/compare")
    public ApiResponse<CompareResponse> compare(@RequestParam String date1, @RequestParam String date2) {
        SnapshotData s1 = snapshotService.loadSnapshot(date1);
        SnapshotData s2 = snapshotService.loadSnapshot(date2);
        NodeStats a = s1.getNodes().get(s1.getRootIndex()).getStats();
        NodeStats b = s2.getNodes().get(s2.getRootIndex()).getStats();
        CompareResponse resp = new CompareResponse();
        resp.setTotalA(a.getTotalCount());
        resp.setTotalB(b.getTotalCount());
        resp.setPerformanceA(new PerformanceResponse(a.getAvgResponse(), a.getResponseMax(), a.getResponseMin()));
        resp.setPerformanceB(new PerformanceResponse(b.getAvgResponse(), b.getResponseMax(), b.getResponseMin()));
        resp.setLevelDiff(diffMap(a.getLevelCounts(), b.getLevelCounts()));
        resp.setStatusDiff(diffMapInt(a.getStatusCounts(), b.getStatusCounts()));
        return ApiResponse.ok(resp);
    }

    private java.util.Map<String, Long> diffMap(java.util.Map<String, Long> a, java.util.Map<String, Long> b) {
        java.util.Map<String, Long> diff = new java.util.HashMap<>();
        for (String key : a.keySet()) {
            diff.put(key, a.getOrDefault(key, 0L) - b.getOrDefault(key, 0L));
        }
        for (String key : b.keySet()) {
            diff.putIfAbsent(key, 0L - b.getOrDefault(key, 0L));
        }
        return diff;
    }

    private java.util.Map<Integer, Long> diffMapInt(java.util.Map<Integer, Long> a, java.util.Map<Integer, Long> b) {
        java.util.Map<Integer, Long> diff = new java.util.HashMap<>();
        for (Integer key : a.keySet()) {
            diff.put(key, a.getOrDefault(key, 0L) - b.getOrDefault(key, 0L));
        }
        for (Integer key : b.keySet()) {
            diff.putIfAbsent(key, 0L - b.getOrDefault(key, 0L));
        }
        return diff;
    }
}
