# API 说明

所有时间参数格式：`yyyy-MM-dd HH:mm:ss`

## 认证
- `POST /api/auth/login`
  - Body: `{"username":"admin","password":"admin123"}`
  - 返回：JWT

## 日志
- `POST /api/logs/upload`（管理员）
  - form-data: `file`
- `POST /api/logs`（管理员）
  - Body: `{"timestamp":"2025-01-01 10:00:00","level":"INFO","statusCode":200,"responseTimeMs":120}`
- `GET /api/logs/query?start=...&end=...&page=0&size=20`

## 统计
- `GET /api/stats/summary?start=...&end=...`
- `GET /api/stats/performance?start=...&end=...`
- `GET /api/stats/error-trend?start=...&end=...&statusCode=404&bucketMinutes=60`
- `GET /api/stats/compare?start1=...&end1=...&start2=...&end2=...`

## 快照
- `GET /api/snapshots`
- `POST /api/snapshots/create?note=manual`
- `POST /api/snapshots/rollback?date=YYYY-MM-DD`
- `GET /api/snapshots/compare?date1=YYYY-MM-DD&date2=YYYY-MM-DD`

## 用户与审计
- `GET /api/users`
- `POST /api/users`
- `PUT /api/users/{id}`
- `DELETE /api/users/{id}`
- `GET /api/audits?start=...&end=...`

## 报警规则
- `GET /api/alerts`
- `POST /api/alerts`
- `PUT /api/alerts/{id}`
- `DELETE /api/alerts/{id}`
