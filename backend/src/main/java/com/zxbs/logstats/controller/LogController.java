package com.zxbs.logstats.controller;

import com.zxbs.logstats.dto.ApiResponse;
import com.zxbs.logstats.dto.LogCreateRequest;
import com.zxbs.logstats.dto.LogUploadResponse;
import com.zxbs.logstats.entity.LogEntry;
import com.zxbs.logstats.repository.LogEntryRepository;
import com.zxbs.logstats.service.AlertService;
import com.zxbs.logstats.service.AuditService;
import com.zxbs.logstats.service.LogIngestService;
import com.zxbs.logstats.service.SegmentTreeService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/logs")
public class LogController {
    private final LogIngestService ingestService;
    private final LogEntryRepository logRepository;
    private final SegmentTreeService segmentTreeService;
    private final AlertService alertService;
    private final AuditService auditService;

    public LogController(LogIngestService ingestService, LogEntryRepository logRepository,
                         SegmentTreeService segmentTreeService, AlertService alertService,
                         AuditService auditService) {
        this.ingestService = ingestService;
        this.logRepository = logRepository;
        this.segmentTreeService = segmentTreeService;
        this.alertService = alertService;
        this.auditService = auditService;
    }

    @PostMapping("/upload")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<LogUploadResponse> upload(@RequestParam("file") MultipartFile file, Authentication auth) {
        LogUploadResponse resp = ingestService.ingestFile(file);
        auditService.record(null, auth != null ? auth.getName() : "admin",
                "UPLOAD_LOG", "上传日志文件: " + file.getOriginalFilename());
        return ApiResponse.ok(resp);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<LogEntry> create(@Valid @RequestBody LogCreateRequest req, Authentication auth) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LogEntry entry = new LogEntry();
        entry.setTimestamp(LocalDateTime.parse(req.getTimestamp().replace("T", " "), formatter));
        entry.setLevel(req.getLevel());
        entry.setStatusCode(req.getStatusCode());
        entry.setResponseTimeMs(req.getResponseTimeMs());
        entry.setMessage(req.getMessage());
        entry.setSource(req.getSource());
        LogEntry saved = logRepository.save(entry);
        segmentTreeService.updateWithLog(saved);
        alertService.checkRules();
        auditService.record(null, auth != null ? auth.getName() : "admin", "CREATE_LOG", "新增日志");
        return ApiResponse.ok(saved);
    }

    @GetMapping("/query")
    public ApiResponse<Page<LogEntry>> query(@RequestParam String start,
                                             @RequestParam String end,
                                             @RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "20") int size) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime startTime = LocalDateTime.parse(start.replace("T", " "), formatter);
        LocalDateTime endTime = LocalDateTime.parse(end.replace("T", " "), formatter);
        Page<LogEntry> data = logRepository.findByTimestampBetween(
                startTime, endTime, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp")));
        return ApiResponse.ok(data);
    }
}
