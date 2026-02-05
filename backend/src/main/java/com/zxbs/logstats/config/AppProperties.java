package com.zxbs.logstats.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {
    private Jwt jwt = new Jwt();
    private SegmentTree segmentTree = new SegmentTree();
    private Snapshot snapshot = new Snapshot();
    private DefaultUsers defaultUsers = new DefaultUsers();

    public Jwt getJwt() {
        return jwt;
    }

    public void setJwt(Jwt jwt) {
        this.jwt = jwt;
    }

    public SegmentTree getSegmentTree() {
        return segmentTree;
    }

    public void setSegmentTree(SegmentTree segmentTree) {
        this.segmentTree = segmentTree;
    }

    public Snapshot getSnapshot() {
        return snapshot;
    }

    public void setSnapshot(Snapshot snapshot) {
        this.snapshot = snapshot;
    }

    public DefaultUsers getDefaultUsers() {
        return defaultUsers;
    }

    public void setDefaultUsers(DefaultUsers defaultUsers) {
        this.defaultUsers = defaultUsers;
    }

    public static class Jwt {
        private String secret;
        private int expirationMinutes;

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public int getExpirationMinutes() {
            return expirationMinutes;
        }

        public void setExpirationMinutes(int expirationMinutes) {
            this.expirationMinutes = expirationMinutes;
        }
    }

    public static class SegmentTree {
        private String rangeStart;
        private String rangeEnd;
        private int bucketMinutes = 1;

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
    }

    public static class Snapshot {
        private String baseDir;

        public String getBaseDir() {
            return baseDir;
        }

        public void setBaseDir(String baseDir) {
            this.baseDir = baseDir;
        }
    }

    public static class DefaultUsers {
        private String adminUsername;
        private String adminPassword;
        private String userUsername;
        private String userPassword;

        public String getAdminUsername() {
            return adminUsername;
        }

        public void setAdminUsername(String adminUsername) {
            this.adminUsername = adminUsername;
        }

        public String getAdminPassword() {
            return adminPassword;
        }

        public void setAdminPassword(String adminPassword) {
            this.adminPassword = adminPassword;
        }

        public String getUserUsername() {
            return userUsername;
        }

        public void setUserUsername(String userUsername) {
            this.userUsername = userUsername;
        }

        public String getUserPassword() {
            return userPassword;
        }

        public void setUserPassword(String userPassword) {
            this.userPassword = userPassword;
        }
    }
}
