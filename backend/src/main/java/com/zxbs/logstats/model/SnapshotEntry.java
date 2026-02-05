package com.zxbs.logstats.model;

public class SnapshotEntry {
    private String date;
    private String fileName;
    private String createdAt;
    private String note;

    public SnapshotEntry() {
    }

    public SnapshotEntry(String date, String fileName, String createdAt, String note) {
        this.date = date;
        this.fileName = fileName;
        this.createdAt = createdAt;
        this.note = note;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
