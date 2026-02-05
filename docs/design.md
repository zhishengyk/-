# 软件设计说明书

## 1. 总体架构
系统采用 B/S 架构，后端提供 REST API，前端页面进行展示与交互。

1. 表现层：静态页面 + 图表展示。
2. 业务层：日志解析、线段树统计、快照管理、报警规则。
3. 数据层：关系数据库 + 本地快照文件。

## 2. 模块划分
1. 日志采集与解析模块
2. 线段树统计模块
3. 查询引擎模块
4. 快照持久化模块
5. 权限与审计模块
6. 报警规则模块

## 3. 核心数据结构设计
### 3.1 线段树节点
1. 区间范围：`start`、`end`
2. 子节点索引：`leftIndex`、`rightIndex`
3. 统计信息：总量、级别分布、状态码分布、响应时间统计

### 3.2 统计信息结构
1. 总量 `totalCount`
2. 级别分布 `levelCounts`
3. 状态码分布 `statusCounts`
4. 性能指标 `responseSum/Min/Max`

## 4. 数据库设计
### 4.1 用户表 `users`
| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | bigint | 主键 |
| username | varchar | 用户名 |
| password_hash | varchar | 密码哈希 |
| role | varchar | 角色 |
| enabled | tinyint | 启用状态 |
| created_at | datetime | 创建时间 |
| updated_at | datetime | 更新时间 |

### 4.2 日志表 `log_entries`
| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | bigint | 主键 |
| timestamp | datetime | 日志时间 |
| level | varchar | 日志级别 |
| status_code | int | HTTP 状态码 |
| response_time_ms | bigint | 响应时间 |
| message | text | 消息 |
| source | varchar | 来源 |
| raw | text | 原始日志 |

### 4.3 审计表 `audit_logs`
| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | bigint | 主键 |
| user_id | bigint | 用户 ID |
| username | varchar | 用户名 |
| action | varchar | 操作类型 |
| detail | varchar | 详细描述 |
| ip | varchar | IP |
| created_at | datetime | 时间 |

### 4.4 报警规则表 `alert_rules`
| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | bigint | 主键 |
| name | varchar | 规则名 |
| status_code | int | 状态码 |
| threshold_count | bigint | 阈值 |
| window_minutes | int | 时间窗口 |
| enabled | tinyint | 启用状态 |
| last_triggered_at | datetime | 上次触发时间 |

## 5. 接口设计
接口遵循 REST 风格，详见 `docs/api.md`。

## 6. 核心流程说明
### 6.1 日志导入流程
1. 管理员上传日志文件。
2. 日志解析器逐行解析并写入数据库。
3. 线段树实时更新统计节点。

### 6.2 区间统计流程
1. 用户提交时间区间。
2. 线段树进行区间查询。
3. 统计结果返回并可视化展示。

### 6.3 快照流程
1. 定时任务生成快照文件。
2. 快照索引更新并可查询。
3. 回滚时加载快照并替换内存状态。

## 7. 安全设计
1. JWT 鉴权与角色访问控制。
2. 管理员操作写入审计日志。

## 8. 错误处理
1. 参数校验失败返回统一错误响应。
2. 系统异常返回 500。
