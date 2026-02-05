package com.zxbs.logstats.service;

import com.zxbs.logstats.entity.AlertRule;
import com.zxbs.logstats.model.NodeStats;
import com.zxbs.logstats.repository.AlertRuleRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AlertService {
    private final AlertRuleRepository repository;
    private final SegmentTreeService segmentTreeService;
    private final AuditService auditService;

    public AlertService(AlertRuleRepository repository, SegmentTreeService segmentTreeService, AuditService auditService) {
        this.repository = repository;
        this.segmentTreeService = segmentTreeService;
        this.auditService = auditService;
    }

    public List<AlertRule> list() {
        return repository.findAll();
    }

    public AlertRule create(AlertRule rule) {
        return repository.save(rule);
    }

    public AlertRule update(Long id, AlertRule rule) {
        AlertRule exist = repository.findById(id).orElseThrow();
        exist.setName(rule.getName());
        exist.setStatusCode(rule.getStatusCode());
        exist.setThresholdCount(rule.getThresholdCount());
        exist.setWindowMinutes(rule.getWindowMinutes());
        exist.setEnabled(rule.isEnabled());
        return repository.save(exist);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    @Scheduled(fixedDelay = 60000)
    public void checkRules() {
        List<AlertRule> rules = repository.findAll();
        if (rules.isEmpty()) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        for (AlertRule rule : rules) {
            if (!rule.isEnabled()) {
                continue;
            }
            LocalDateTime start = now.minusMinutes(rule.getWindowMinutes());
            NodeStats stats = segmentTreeService.query(start, now);
            long count = rule.getStatusCode() == null ? stats.getTotalCount()
                    : stats.getStatusCounts().getOrDefault(rule.getStatusCode(), 0L);
            boolean shouldTrigger = count >= rule.getThresholdCount();
            if (shouldTrigger && (rule.getLastTriggeredAt() == null ||
                    rule.getLastTriggeredAt().isBefore(now.minusMinutes(rule.getWindowMinutes())))) {
                rule.setLastTriggeredAt(now);
                repository.save(rule);
                auditService.record(null, "system", "ALERT_TRIGGER",
                        "规则[" + rule.getName() + "]触发，count=" + count);
            }
        }
    }
}
