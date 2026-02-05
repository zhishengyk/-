package com.zxbs.logstats;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LogStatsApplication {
    public static void main(String[] args) {
        SpringApplication.run(LogStatsApplication.class, args);
    }
}
