# 短链接系统（阶段1）

一个短链接生成与跳转系统：长链接转短码，访问短码 302 跳转到原链接，并提供详情查询。

当前完成 **阶段1**：创建短链接、短码 302 跳转、查询详情（含访问次数字段）。Redis 缓存、访问统计、限流分别留给阶段2-4。

## 技术栈

| 组件 | 版本 |
| --- | --- |
| Spring Boot | 3.5.11 |
| Java | 21 |
| MyBatis-Plus | 3.5.15 |
| MySQL | 8.0+ |
| Redis | 依赖已引入，阶段1 关闭自动配置（阶段2 启用） |

## 已实现

- `POST /api/shortlink/create`：长链接创建短链接，雪花 ID + 62 进制短码，同一长链接幂等返回同一短码，支持可选过期天数。
- `GET /api/shortlink/{shortCode}`：查询详情（短码、短链接、长链接、访问次数、创建/过期时间）。
- `GET /{shortCode}`：302 临时重定向；短码不存在或已过期返回真实 404。
- `GET /`：极简蓝白测试页，可在浏览器里创建、复制、打开、查询短链接。
- 统一返回 `Result{code,msg,data}`、业务异常 `BizException`、全局异常处理、雪花时钟回拨检测。
- 访问统计（阶段3）、Redis 缓存（阶段2）、限流（阶段4）尚未实现，`visit_count` 阶段1 固定为 0。

## 目录结构

```text
短链接/
├── pom.xml
├── sql/init.sql                    建库建表脚本
├── scripts/verify-phase1.ps1       阶段1 自动验收脚本（PowerShell）
├── docs/
│   ├── 设计思路.md                  设计取舍、流程、面试点
│   ├── 接口文档.md                  接口契约、错误码、验收清单
│   └── 阶段1验收报告.md              实际验收记录（本次）
└── src/main/
    ├── java/com/bq/shortlink/
    │   ├── ShortLinkApplication.java
    │   ├── common/    Result、PageResult
    │   ├── config/    GlobalExceptionHandler、MybatisPlusConfig
    │   ├── exception/ BizException
    │   ├── controller/ ShortLinkController、RedirectController
    │   ├── service/    ShortLinkService、impl/ShortLinkServiceImpl
    │   ├── mapper/     ShortLinkMapper
    │   ├── entity/     ShortLink
    │   ├── dto/        ShortLinkCreateDTO
    │   ├── vo/         ShortLinkVO
    │   └── util/       SnowflakeUtil、ShortCodeUtil
    └── resources/
        ├── application.yml
        ├── application-local.yml.example
        └── static/index.html
```

## 快速开始

### 1. 初始化数据库

MySQL 8 已启动的前提下，执行：

```powershell
cd C:\Users\ZhuanZ\Desktop\短链接
Get-Content -Raw -Encoding UTF8 sql\init.sql | & "D:\MySQL\MySQL Server 8.0\bin\mysql.exe" -uroot -p --default-character-set=utf8mb4
```

脚本会创建 `shortlink` 库、删除早期实验表 `short_link`、重建 `t_short_link`。**注意：会重建 `t_short_link`，已有数据会被清空。**

也可以用 Navicat / DataGrip 直接执行 `sql/init.sql`。

### 2. 配置本地数据库账号

```powershell
Copy-Item src\main\resources\application-local.yml.example src\main\resources\application-local.yml
# 编辑 application-local.yml，把 password 改成自己的 MySQL 密码
```

`application-local.yml` 已加入 `.gitignore`，不会提交到仓库。

### 3. 启动

```powershell
mvn spring-boot:run
```

启动后访问：

- 测试页：http://localhost:8080/
- 创建：`POST http://localhost:8080/api/shortlink/create`
- 跳转：http://localhost:8080/{shortCode}

### 4. 自动验收

应用启动后，另开一个终端：

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts\verify-phase1.ps1
```

脚本会自动验证：首页、创建、幂等、详情、302 跳转、随机短码 404、空/非法长链接错误码、详情不存在 50003。

## 接口速览

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | /api/shortlink/create | 创建短链接 |
| GET | /api/shortlink/{shortCode} | 查询详情 |
| GET | /{shortCode} | 302 跳转到长链接，不存在/过期返回 404 |
| GET | / | 蓝白测试页 |

详细请求/响应示例、错误码与验收清单见 [接口文档](docs/接口文档.md)。

## 配置说明

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `server.port` | 8080 | 可用环境变量 `SERVER_PORT` 覆盖 |
| `spring.profiles.active` | local | 可用 `SPRING_PROFILES_ACTIVE` 覆盖 |
| `shortlink.domain` | http://localhost:8080/ | 返回给前端的短链接前缀 |
| `shortlink.worker-id` | 1 | 雪花算法机器 ID，多实例部署必须区分 |

`application.yml` 中已排除 Redis 自动配置，因此阶段1 即使本机没启动 Redis 也能正常启动。

## 阶段规划

- 阶段2：Redis 缓存（穿透/击穿/雪崩：空值缓存、布隆过滤器、互斥重建、过期打散）。
- 阶段3：访问统计（PV/UV，访问日志 + 聚合，详情展示访问次数）。
- 阶段4：限流防刷（IP/用户维度令牌桶或滑动窗口）。
- 阶段5：多级缓存 + 压测。

## 文档

- [设计思路](docs/设计思路.md)
- [接口文档](docs/接口文档.md)
- [阶段1 验收报告](docs/阶段1验收报告.md)
