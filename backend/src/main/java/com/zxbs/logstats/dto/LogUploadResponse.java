package com.zxbs.logstats.dto;

public class LogUploadResponse {
    private int totalLines;
    private int successCount;
    private int failedCount;

    public LogUploadResponse() {
    }

    public LogUploadResponse(int totalLines, int successCount, int failedCount) {
        this.totalLines = totalLines;
        this.successCount = successCount;
        this.failedCount = failedCount;
    }

    public int getTotalLines() {
        return totalLines;
    }

    public void setTotalLines(int totalLines) {
        this.totalLines = totalLines;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(int successCount) {
        this.successCount = successCount;
    }

    public int getFailedCount() {
        return failedCount;
    }

    public void setFailedCount(int failedCount) {
        this.failedCount = failedCount;
    }
}
