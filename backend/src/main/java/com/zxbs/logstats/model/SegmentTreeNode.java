package com.zxbs.logstats.model;

public class SegmentTreeNode {
    private int leftIndex = -1;
    private int rightIndex = -1;
    private int start;
    private int end;
    private NodeStats stats = new NodeStats();

    public SegmentTreeNode() {
    }

    public SegmentTreeNode(int start, int end) {
        this.start = start;
        this.end = end;
    }

    public int getLeftIndex() {
        return leftIndex;
    }

    public void setLeftIndex(int leftIndex) {
        this.leftIndex = leftIndex;
    }

    public int getRightIndex() {
        return rightIndex;
    }

    public void setRightIndex(int rightIndex) {
        this.rightIndex = rightIndex;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public NodeStats getStats() {
        return stats;
    }

    public void setStats(NodeStats stats) {
        this.stats = stats;
    }
}
