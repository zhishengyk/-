package com.zxbs.logstats.repository;

import com.zxbs.logstats.entity.AlertRule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlertRuleRepository extends JpaRepository<AlertRule, Long> {
}
