package com.zxbs.logstats.controller;

import com.zxbs.logstats.dto.*;
import com.zxbs.logstats.model.NodeStats;
import com.zxbs.logstats.service.StatsService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/stats")
public class StatsController {
    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/summary")
    public ApiResponse<StatsSummaryResponse> summary(@RequestParam String start, @RequestParam String end) {
        LocalDateTime startTime = parse(start);
        LocalDateTime endTime = parse(end);
        NodeStats stats = statsService.summary(startTime, endTime);
        return ApiResponse.ok(new StatsSummaryResponse(stats.getTotalCount(), stats.getLevelCounts(), stats.getStatusCounts()));
    }

    @GetMapping("/performance")
    public ApiResponse<PerformanceResponse> performance(@RequestParam String start, @RequestParam String end) {
        return ApiResponse.ok(statsService.performance(parse(start), parse(end)));
    }

    @GetMapping("/error-trend")
    public ApiResponse<List<ErrorTrendPoint>> errorTrend(@RequestParam String start,
                                                         @RequestParam String end,
                                                         @RequestParam int statusCode,
                                                         @RequestParam(defaultValue = "60") int bucketMinutes) {
        return ApiResponse.ok(statsService.errorTrend(parse(start), parse(end), statusCode, bucketMinutes));
    }

    @GetMapping("/compare")
    public ApiResponse<CompareResponse> compare(@RequestParam String start1, @RequestParam String end1,
                                                @RequestParam String start2, @RequestParam String end2) {
        return ApiResponse.ok(statsService.compare(parse(start1), parse(end1), parse(start2), parse(end2)));
    }

    private LocalDateTime parse(String value) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.parse(value.replace("T", " "), formatter);
    }
}
