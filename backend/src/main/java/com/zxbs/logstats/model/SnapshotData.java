package com.zxbs.logstats.model;

import java.util.ArrayList;
import java.util.List;

public class SnapshotData {
    private String rangeStart;
    private String rangeEnd;
    private int bucketMinutes;
    private int rootIndex;
    private List<SegmentTreeNode> nodes = new ArrayList<>();

    public String getRangeStart() {
        return rangeStart;
    }

    public void setRangeStart(String rangeStart) {
        this.rangeStart = rangeStart;
    }

    public String getRangeEnd() {
        return rangeEnd;
    }

    public void setRangeEnd(String rangeEnd) {
        this.rangeEnd = rangeEnd;
    }

    public int getBucketMinutes() {
        return bucketMinutes;
    }

    public void setBucketMinutes(int bucketMinutes) {
        this.bucketMinutes = bucketMinutes;
    }

    public int getRootIndex() {
        return rootIndex;
    }

    public void setRootIndex(int rootIndex) {
        this.rootIndex = rootIndex;
    }

    public List<SegmentTreeNode> getNodes() {
        return nodes;
    }

    public void setNodes(List<SegmentTreeNode> nodes) {
        this.nodes = nodes;
    }
}
