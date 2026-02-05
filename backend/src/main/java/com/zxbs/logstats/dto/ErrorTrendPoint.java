package com.zxbs.logstats.dto;

public class ErrorTrendPoint {
    private String bucket;
    private long count;

    public ErrorTrendPoint() {
    }

    public ErrorTrendPoint(String bucket, long count) {
        this.bucket = bucket;
        this.count = count;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }
}
