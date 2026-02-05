package com.zxbs.logstats.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zxbs.logstats.entity.LogEntry;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class LogParser {
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Pattern textPattern = Pattern.compile(
            "^(?<time>\\d{4}-\\d{2}-\\d{2}[ T]\\d{2}:\\d{2}:\\d{2})\\s+(?<level>\\w+)\\s+(?<status>\\d{3})\\s+(?<rt>\\d+)(ms)?\\s*(?<msg>.*)$"
    );

    public Optional<LogEntry> parseLine(String line) {
        if (line == null || line.isBlank()) {
            return Optional.empty();
        }
        line = line.trim();
        if (line.startsWith("{") && line.endsWith("}")) {
            try {
                JsonNode node = objectMapper.readTree(line);
                LogEntry entry = new LogEntry();
                entry.setTimestamp(parseTime(node.path("timestamp").asText(null), node.path("time").asText(null), node.path("ts").asText(null)));
                entry.setLevel(node.path("level").asText("INFO"));
                entry.setStatusCode(node.path("statusCode").asInt(node.path("status").asInt(200)));
                entry.setResponseTimeMs(node.path("responseTime").asLong(node.path("rt").asLong(0)));
                entry.setMessage(node.path("message").asText(node.path("msg").asText("")));
                entry.setSource(node.path("source").asText(""));
                entry.setRaw(line);
                if (entry.getTimestamp() == null) {
                    return Optional.empty();
                }
                return Optional.of(entry);
            } catch (Exception ignored) {
            }
        }
        Matcher matcher = textPattern.matcher(line);
        if (matcher.find()) {
            LogEntry entry = new LogEntry();
            entry.setTimestamp(parseTime(matcher.group("time"), null, null));
            entry.setLevel(matcher.group("level"));
            entry.setStatusCode(Integer.parseInt(matcher.group("status")));
            entry.setResponseTimeMs(Long.parseLong(matcher.group("rt")));
            entry.setMessage(matcher.group("msg"));
            entry.setRaw(line);
            return Optional.of(entry);
        }
        return Optional.empty();
    }

    private LocalDateTime parseTime(String primary, String fallback, String fallback2) {
        String value = primary != null ? primary : (fallback != null ? fallback : fallback2);
        if (value == null || value.isBlank()) {
            return null;
        }
        value = value.replace("T", " ");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        try {
            return LocalDateTime.parse(value, formatter);
        } catch (Exception ex) {
            return null;
        }
    }
}
