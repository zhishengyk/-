package com.zxbs.logstats.service;

import com.zxbs.logstats.dto.LogUploadResponse;
import com.zxbs.logstats.entity.LogEntry;
import com.zxbs.logstats.repository.LogEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class LogIngestService {
    private final LogParser logParser;
    private final LogEntryRepository logRepository;
    private final SegmentTreeService segmentTreeService;
    private final AlertService alertService;

    public LogIngestService(LogParser logParser, LogEntryRepository logRepository,
                            SegmentTreeService segmentTreeService, AlertService alertService) {
        this.logParser = logParser;
        this.logRepository = logRepository;
        this.segmentTreeService = segmentTreeService;
        this.alertService = alertService;
    }

    public LogUploadResponse ingestFile(MultipartFile file) {
        int total = 0;
        int success = 0;
        int failed = 0;
        List<LogEntry> batch = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                total++;
                logParser.parseLine(line).ifPresentOrElse(entry -> {
                    entry.setSource(file.getOriginalFilename());
                    batch.add(entry);
                }, () -> {
                    // parse failed
                });
            }
        } catch (IOException e) {
            throw new IllegalStateException("读取日志文件失败");
        }
        for (LogEntry entry : batch) {
            logRepository.save(entry);
            segmentTreeService.updateWithLog(entry);
            success++;
        }
        alertService.checkRules();
        failed = total - success;
        return new LogUploadResponse(total, success, failed);
    }
}
