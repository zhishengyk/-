package com.zxbs.logstats.service;

import com.zxbs.logstats.config.AppProperties;
import com.zxbs.logstats.entity.LogEntry;
import com.zxbs.logstats.model.NodeStats;
import com.zxbs.logstats.model.SegmentTreeNode;
import com.zxbs.logstats.model.SnapshotData;
import com.zxbs.logstats.repository.LogEntryRepository;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Service
public class SegmentTreeService {
    private final AppProperties properties;
    private final LogEntryRepository logRepository;

    private LocalDateTime rangeStart;
    private LocalDateTime rangeEnd;
    private int bucketMinutes;
    private int maxIndex;

    private final List<SegmentTreeNode> nodes = new ArrayList<>();
    private int rootIndex = -1;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public SegmentTreeService(AppProperties properties, LogEntryRepository logRepository) {
        this.properties = properties;
        this.logRepository = logRepository;
    }

    @PostConstruct
    public void init() {
        this.rangeStart = LocalDateTime.parse(properties.getSegmentTree().getRangeStart());
        this.rangeEnd = LocalDateTime.parse(properties.getSegmentTree().getRangeEnd());
        this.bucketMinutes = properties.getSegmentTree().getBucketMinutes();
        long totalMinutes = Duration.between(rangeStart, rangeEnd).toMinutes();
        this.maxIndex = (int) Math.max(0, totalMinutes / bucketMinutes);
        this.rootIndex = createNode(0, maxIndex);

        lock.writeLock().lock();
        try {
            logRepository.findAll().forEach(this::updateWithLog);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void updateWithLog(LogEntry log) {
        int idx = toIndex(log.getTimestamp());
        if (idx < 0 || idx > maxIndex) {
            return;
        }
        lock.writeLock().lock();
        try {
            rootIndex = updateNode(rootIndex, 0, maxIndex, idx, log);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public NodeStats query(LocalDateTime start, LocalDateTime end) {
        int left = Math.max(0, toIndex(start));
        int right = Math.min(maxIndex, toIndex(end));
        if (left > right) {
            return new NodeStats();
        }
        lock.readLock().lock();
        try {
            return queryNode(rootIndex, 0, maxIndex, left, right);
        } finally {
            lock.readLock().unlock();
        }
    }

    public NodeStats getBucketStats(int index) {
        lock.readLock().lock();
        try {
            return queryNode(rootIndex, 0, maxIndex, index, index);
        } finally {
            lock.readLock().unlock();
        }
    }

    public int toIndex(LocalDateTime time) {
        long minutes = Duration.between(rangeStart, time).toMinutes();
        return (int) (minutes / bucketMinutes);
    }

    public LocalDateTime indexToTime(int index) {
        return rangeStart.plusMinutes((long) index * bucketMinutes);
    }

    public SnapshotData exportSnapshot() {
        lock.readLock().lock();
        try {
            SnapshotData data = new SnapshotData();
            data.setRangeStart(rangeStart.toString());
            data.setRangeEnd(rangeEnd.toString());
            data.setBucketMinutes(bucketMinutes);
            data.setRootIndex(rootIndex);
            List<SegmentTreeNode> copyNodes = new ArrayList<>();
            for (SegmentTreeNode node : nodes) {
                SegmentTreeNode copy = new SegmentTreeNode();
                copy.setStart(node.getStart());
                copy.setEnd(node.getEnd());
                copy.setLeftIndex(node.getLeftIndex());
                copy.setRightIndex(node.getRightIndex());
                copy.setStats(node.getStats().copy());
                copyNodes.add(copy);
            }
            data.setNodes(copyNodes);
            return data;
        } finally {
            lock.readLock().unlock();
        }
    }

    public void loadSnapshot(SnapshotData data) {
        lock.writeLock().lock();
        try {
            this.rangeStart = LocalDateTime.parse(data.getRangeStart());
            this.rangeEnd = LocalDateTime.parse(data.getRangeEnd());
            this.bucketMinutes = data.getBucketMinutes();
            long totalMinutes = Duration.between(rangeStart, rangeEnd).toMinutes();
            this.maxIndex = (int) Math.max(0, totalMinutes / bucketMinutes);
            this.nodes.clear();
            this.nodes.addAll(data.getNodes());
            this.rootIndex = data.getRootIndex();
        } finally {
            lock.writeLock().unlock();
        }
    }

    public String formatBucket(LocalDateTime time) {
        return time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    private int createNode(int start, int end) {
        SegmentTreeNode node = new SegmentTreeNode(start, end);
        nodes.add(node);
        return nodes.size() - 1;
    }

    private int updateNode(int nodeIndex, int start, int end, int target, LogEntry log) {
        SegmentTreeNode node = nodes.get(nodeIndex);
        if (start == end) {
            node.getStats().addLog(log);
            return nodeIndex;
        }
        int mid = start + (end - start) / 2;
        if (target <= mid) {
            int left = node.getLeftIndex();
            if (left == -1) {
                left = createNode(start, mid);
                node.setLeftIndex(left);
            }
            updateNode(left, start, mid, target, log);
        } else {
            int right = node.getRightIndex();
            if (right == -1) {
                right = createNode(mid + 1, end);
                node.setRightIndex(right);
            }
            updateNode(right, mid + 1, end, target, log);
        }
        NodeStats merged = new NodeStats();
        if (node.getLeftIndex() != -1) {
            merged.merge(nodes.get(node.getLeftIndex()).getStats());
        }
        if (node.getRightIndex() != -1) {
            merged.merge(nodes.get(node.getRightIndex()).getStats());
        }
        node.setStats(merged);
        return nodeIndex;
    }

    private NodeStats queryNode(int nodeIndex, int start, int end, int left, int right) {
        if (nodeIndex == -1 || left > end || right < start) {
            return new NodeStats();
        }
        if (left <= start && end <= right) {
            return nodes.get(nodeIndex).getStats().copy();
        }
        int mid = start + (end - start) / 2;
        NodeStats res = new NodeStats();
        res.merge(queryNode(nodes.get(nodeIndex).getLeftIndex(), start, mid, left, right));
        res.merge(queryNode(nodes.get(nodeIndex).getRightIndex(), mid + 1, end, left, right));
        return res;
    }
}
