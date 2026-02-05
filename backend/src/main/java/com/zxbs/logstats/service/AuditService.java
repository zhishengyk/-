package com.zxbs.logstats.service;

import com.zxbs.logstats.entity.AuditLog;
import com.zxbs.logstats.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuditService {
    private final AuditLogRepository repository;
    private final HttpServletRequest request;

    public AuditService(AuditLogRepository repository, HttpServletRequest request) {
        this.repository = repository;
        this.request = request;
    }

    public void record(Long userId, String username, String action, String detail) {
        AuditLog log = new AuditLog();
        log.setUserId(userId);
        log.setUsername(username);
        log.setAction(action);
        log.setDetail(detail);
        String ip = "system";
        try {
            ip = request.getRemoteAddr();
        } catch (Exception ignored) {
        }
        log.setIp(ip);
        repository.save(log);
    }

    public Page<AuditLog> query(LocalDateTime start, LocalDateTime end, Pageable pageable) {
        return repository.findByCreatedAtBetween(start, end, pageable);
    }
}
