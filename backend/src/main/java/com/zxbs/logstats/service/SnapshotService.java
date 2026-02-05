package com.zxbs.logstats.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zxbs.logstats.config.AppProperties;
import com.zxbs.logstats.dto.SnapshotInfo;
import com.zxbs.logstats.model.SnapshotData;
import com.zxbs.logstats.model.SnapshotEntry;
import com.zxbs.logstats.model.SnapshotIndex;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class SnapshotService {
    private final SegmentTreeService segmentTreeService;
    private final AppProperties properties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SnapshotService(SegmentTreeService segmentTreeService, AppProperties properties) {
        this.segmentTreeService = segmentTreeService;
        this.properties = properties;
    }

    public SnapshotEntry createSnapshot(String note) {
        String date = LocalDate.now().toString();
        String fileName = "snapshot-" + date + ".json";
        String createdAt = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        SnapshotEntry entry = new SnapshotEntry(date, fileName, createdAt, note);

        SnapshotData data = segmentTreeService.exportSnapshot();
        File dir = new File(properties.getSnapshot().getBaseDir());
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(dir, fileName);
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, data);
        } catch (IOException e) {
            throw new IllegalStateException("保存快照失败");
        }
        SnapshotIndex index = loadIndex();
        index.getEntries().removeIf(it -> it.getDate().equals(date));
        index.getEntries().add(entry);
        saveIndex(index);
        return entry;
    }

    public List<SnapshotInfo> listSnapshots() {
        SnapshotIndex index = loadIndex();
        List<SnapshotInfo> result = new ArrayList<>();
        index.getEntries().stream()
                .sorted(Comparator.comparing(SnapshotEntry::getDate).reversed())
                .forEach(it -> result.add(new SnapshotInfo(it.getDate(), it.getFileName(), it.getCreatedAt(), it.getNote())));
        return result;
    }

    public SnapshotData loadSnapshot(String date) {
        SnapshotIndex index = loadIndex();
        SnapshotEntry entry = index.getEntries().stream()
                .filter(it -> it.getDate().equals(date))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("快照不存在"));
        File file = new File(properties.getSnapshot().getBaseDir(), entry.getFileName());
        try {
            return objectMapper.readValue(file, SnapshotData.class);
        } catch (IOException e) {
            throw new IllegalStateException("读取快照失败");
        }
    }

    public SnapshotIndex loadIndex() {
        File dir = new File(properties.getSnapshot().getBaseDir());
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File indexFile = new File(dir, "snapshot-index.json");
        if (!indexFile.exists()) {
            return new SnapshotIndex();
        }
        try {
            return objectMapper.readValue(indexFile, SnapshotIndex.class);
        } catch (IOException e) {
            return new SnapshotIndex();
        }
    }

    public void saveIndex(SnapshotIndex index) {
        File dir = new File(properties.getSnapshot().getBaseDir());
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File indexFile = new File(dir, "snapshot-index.json");
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(indexFile, index);
        } catch (IOException e) {
            throw new IllegalStateException("保存快照索引失败");
        }
    }

    @Scheduled(cron = "0 0 2 * * ?")
    public void autoSnapshot() {
        createSnapshot("auto");
    }
}
