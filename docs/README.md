# 项目说明

本项目为“基于线段树的大数据日志统计系统”的毕业设计实现，包含后端系统、数据生成脚本及文档模板。

**当前已实现**
- 日志采集与解析（JSON 行、纯文本）
- 线段树统计与实时更新
- 统计查询与对比
- 快照持久化与回滚
- 用户与权限管理
- 审计日志

**技术栈**
- 后端：Spring Boot 3 / Spring Security / JPA
- 数据库：开发环境 H2，生产环境 MySQL
- 脚本：Python 3

**快速启动（开发环境）**
1. 进入后端目录：`backend`
2. 运行：`mvn spring-boot:run`
3. 访问：`http://localhost:18081`

**默认账号**
- 管理员：`admin` / `admin123`
- 普通用户：`user` / `user123`

**日志生成**
使用脚本生成模拟日志：
```
python tools/generate_logs.py --count 1000 --output sample-logs.jsonl
```

**接口示例**
详见 `docs/api.md`

**前端可视化**
演示页面使用 ECharts CDN 加载图表组件，需保持可访问外网环境。
