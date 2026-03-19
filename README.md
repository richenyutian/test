# 工单处理系统

基于 **Spring Boot 3 + Spring Security + JWT + MyBatis Plus + PostgreSQL 16 + Redis** 与 **Vue 3 + TypeScript + Vite + Element Plus + Pinia + Vue Router + Axios + ECharts** 的前后端分离工单处理系统。

当前仓库目标是：

- 后端可直接本地启动
- 前端可直接本地启动
- PostgreSQL 初始化 SQL 可直接执行
- Redis 可直接接入
- 登录后可以进入后台
- 可以完成基础工单流程联调

> 当前登录模式为“开发/测试模式模拟 SSO 直登”，只需要输入用户名即可登录，不需要密码。

---

## 1. 项目结构

```text
.
├── backend/                         # Spring Boot 后端工程（Maven）
├── database/
│   └── postgresql/
│       ├── 01_schema.sql           # 建表脚本
│       ├── 02_init_data.sql        # 初始化系统数据
│       └── 03_mock_data.sql        # 工单/SLA/通知/审计样例数据
├── docs/
│   └── ticket-system-design.md     # 设计说明文档
├── public/                         # 前端静态资源
├── src/                            # Vue 3 前端源码
├── package.json                    # 前端 package.json（前端项目在仓库根目录）
└── README.md
```

---

## 2. 技术栈

### 后端

- JDK 17
- Spring Boot 3.5.x
- Spring Security
- JWT
- MyBatis Plus
- PostgreSQL 16
- Redis
- Maven Wrapper
- OpenAPI / Swagger

### 前端

- Vue 3
- TypeScript
- Vite
- Element Plus
- Pinia
- Vue Router
- Axios
- ECharts

---

## 3. 当前已实现模块

### 3.1 后端模块

- 认证中心
  - 用户名直登
  - JWT 签发
  - 当前用户信息
  - 退出登录（Redis 黑名单）
- 系统管理
  - 用户管理
  - 角色管理
  - 菜单管理
  - 处理组管理
- 工单管理
  - 创建工单
  - 列表查询
  - 工单详情
  - 受理 / 分派 / 接单 / 转派 / 挂起 / 恢复 / 完成 / 确认 / 关闭 / 重开 / 升级
  - 附件上传下载
  - 流转记录
- SLA 管理
  - SLA 规则维护
  - 超时扫描
  - 定时任务扫描
- 通知中心
  - 站内消息列表
  - 标记已读
- 审计日志
  - 注解 + 切面自动记录
  - 审计分页查询
- 报表模块
  - 工单趋势
  - 状态统计
  - 优先级统计
  - 处理组统计
  - 处理人统计
  - SLA 统计
  - 超时统计

### 3.2 前端页面

- 登录页
- 工作台 Dashboard
- 用户管理页
- 角色管理页
- 菜单管理页
- 处理组管理页
- 工单列表页
- 创建工单页
- 工单详情页
- 工单处理抽屉
- SLA 规则页
- 站内消息页
- 审计日志页
- 报表页

---

## 4. 环境要求

请确保本地已安装以下环境：

### 后端

- JDK 17
- Maven 3.9+（或直接使用仓库内 `./mvnw`）
- PostgreSQL 16
- Redis 7+

### 前端

- Node.js 20+
- npm 10+

---

## 5. 默认运行端口

### 后端

- 服务地址：`http://127.0.0.1:8080`
- 接口上下文：`/api`
- Swagger 地址：

```text
http://127.0.0.1:8080/api/swagger-ui.html
```

### 前端

- 开发地址：

```text
http://127.0.0.1:5173
```

Vite 已配置代理：

- `/api -> http://127.0.0.1:8080`

---

## 6. 数据库初始化说明

### 6.1 创建数据库

请先在 PostgreSQL 16 中创建数据库：

```sql
CREATE DATABASE work_order_service;
```

### 6.2 执行 SQL 顺序

请按以下顺序执行：

1. `database/postgresql/01_schema.sql`
2. `database/postgresql/02_init_data.sql`
3. `database/postgresql/03_mock_data.sql`

### 6.3 使用 psql 执行示例

```bash
psql -U postgres -d work_order_service -f database/postgresql/01_schema.sql
psql -U postgres -d work_order_service -f database/postgresql/02_init_data.sql
psql -U postgres -d work_order_service -f database/postgresql/03_mock_data.sql
```

---

## 7. Redis 说明

默认 Redis 配置如下：

- host: `127.0.0.1`
- port: `6379`
- db: `0`

主要用途：

- JWT 退出登录黑名单
- 后续可扩展为会话治理、通知缓存、热点数据缓存

如果 Redis 未启动，涉及退出登录或需要 Redis 访问的功能会报错，因此建议本地启动 Redis。

---

## 8. 默认账号说明

当前版本采用**用户名直登模式**，不需要密码。

### 推荐登录账号

| 账号 | 说明 |
| --- | --- |
| `admin` | 默认管理员 |
| `supervisor_demo` | 主管演示账号 |
| `ops_demo` | 运维演示账号 |
| `rd_demo` | 研发演示账号 |
| `requester_demo` | 提单人演示账号 |

### 登录方式

前端登录页只输入用户名即可。

例如：

```text
admin
```

---

## 9. 后端启动步骤

### 9.1 配置确认

后端默认配置文件：

- `backend/src/main/resources/application.yml`
- `backend/src/main/resources/application-dev.yml`

当前默认开发环境配置：

- PostgreSQL 数据库：`work_order_service`
- 用户名：`postgres`
- 密码：`postgres`
- Redis：`127.0.0.1:6379`

如本地环境不同，请修改：

```text
backend/src/main/resources/application-dev.yml
```

### 9.2 启动命令

进入后端目录：

```bash
cd backend
```

使用 Maven Wrapper 启动：

```bash
./mvnw spring-boot:run
```

或打包验证：

```bash
./mvnw -q -DskipTests package
```

---

## 10. 前端启动步骤

前端项目位于仓库根目录。

### 10.1 安装依赖

```bash
npm install
```

### 10.2 启动开发环境

```bash
npm run dev
```

### 10.3 构建验证

```bash
npm run build
```

---

## 11. 本地联调顺序建议

建议按以下顺序启动：

1. 启动 PostgreSQL
2. 启动 Redis
3. 执行数据库初始化 SQL
4. 启动后端：

```bash
cd backend
./mvnw spring-boot:run
```

5. 启动前端：

```bash
npm install
npm run dev
```

6. 浏览器打开：

```text
http://127.0.0.1:5173
```

---

## 12. 当前可验证的业务闭环

初始化完成后，当前版本可验证以下流程：

1. 使用 `admin` 或 `requester_demo` 登录
2. 创建工单
3. 在工单列表中查看
4. 进入工单详情
5. 使用运维或主管账号进行：
   - 受理
   - 分派
   - 接单
   - 挂起 / 恢复
   - 完成
6. 使用提单人账号进行确认或重开
7. 使用主管或管理员账号关闭工单
8. 在：
   - 通知中心
   - 审计日志
   - 报表中心
   查看对应结果

---

## 13. 文件上传说明

附件采用本地文件存储。

默认目录：

```text
backend/storage/upload
```

运行时如果目录不存在，系统会自动创建。

当前已支持：

- 工单详情页上传附件
- 工单详情页下载附件
- 数据库存储附件元信息

---

## 14. 权限模型说明

当前版本已实现：

### 后端

- Spring Security + JWT
- 方法级 `@PreAuthorize`
- 菜单权限
- 按钮权限
- 基础数据权限（工单侧）

### 前端

- 动态菜单渲染
- `v-permission` 按钮权限指令

数据权限当前规则：

- 提单人：仅本人创建工单
- 运维：本组工单 + 待受理 / 已受理
- 研发：本组工单
- 主管：本组工单 + 管理动作
- 管理员：全部工单

---

## 15. 关键文档

详细设计文档见：

- `docs/ticket-system-design.md`

内容包括：

- 角色职责
- 状态机设计
- RBAC 设计
- SLA 设计
- 报表指标
- mock 数据策略

---

## 16. 后续接入 SSO 的扩展建议

当前版本是“用户名直登模拟 SSO”，后续接公司 SSO 时建议按以下方式演进：

1. 保留现有 `/api/v1/auth/login` 作为开发模式登录接口
2. 新增统一认证抽象，例如：
   - `LoginProvider`
   - `DirectLoginProvider`
   - `SsoLoginProvider`
3. 增加 SSO 回调接口，例如：
   - `/api/v1/auth/sso/callback`
4. 使用 `sso_subject` 字段和企业统一身份做映射
5. JWT 保持为系统内部访问令牌
6. 前端登录页根据环境切换：
   - 开发环境：用户名输入框
   - 生产环境：跳转 SSO

这样可以在不推翻现有业务模块的情况下平滑切到公司单点登录。

---

## 17. 当前待优化项

当前项目已经可以本地运行和联调，但仍有一些可继续优化的点：

### 后端

1. 报表聚合目前主要在 Service 层完成，可进一步下沉为更高性能 SQL
2. 工单数据权限目前是基础实现，可继续抽象为统一数据权限组件
3. 审计日志当前是通用切面实现，可继续增强对象前后值对比
4. 附件上传当前为本地存储，可扩展为 OSS / MinIO
5. 当前未实现评论/沟通记录模块
6. 当前未实现更复杂的自动升级规则引擎

### 前端

1. 当前未做标签页管理
2. 当前表单校验可进一步封装统一规则
3. 当前表格筛选项可继续丰富
4. 当前 ECharts 与 Element Plus 打包体积较大，可进一步做按需优化
5. 当前没有单独的“菜单缓存 / 路由缓存策略”管理

---

## 18. 当前分支

当前开发分支：

```text
work_order_service
```

---

## 19. 快速检查清单

如果你准备本地启动，可按下面检查：

- [ ] PostgreSQL 16 已启动
- [ ] Redis 已启动
- [ ] `work_order_service` 数据库已创建
- [ ] 三个 SQL 已按顺序执行
- [ ] 后端已执行 `./mvnw spring-boot:run`
- [ ] 前端已执行 `npm run dev`
- [ ] 能访问 `http://127.0.0.1:5173`
- [ ] 能访问 `http://127.0.0.1:8080/api/swagger-ui.html`
- [ ] 可以用 `admin` 登录

---

## 20. 构建命令汇总

### 前端

```bash
npm install
npm run dev
npm run build
```

### 后端

```bash
cd backend
./mvnw spring-boot:run
./mvnw -q -DskipTests package
```
