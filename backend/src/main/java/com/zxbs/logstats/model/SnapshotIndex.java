package com.zxbs.logstats.model;

import java.util.ArrayList;
import java.util.List;

public class SnapshotIndex {
    private List<SnapshotEntry> entries = new ArrayList<>();

    public List<SnapshotEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<SnapshotEntry> entries) {
        this.entries = entries;
    }
}
