# bili-live 项目分析报告

> 分析对象：`D:\javaproject\web\bili-live`  
> 分析日期：2026-11-18  
> 分析方式：只读静态代码分析，未修改任何项目文件  
> 用途：项目梳理、代码质量评估、后续学习与简历素材

---

## 目录

- [一、项目概述](#一项目概述)
- [二、技术栈](#二技术栈)
- [三、模块结构](#三模块结构)
- [四、核心功能](#四核心功能)
- [五、技术亮点与可展示点](#五技术亮点与可展示点)
- [六、代码质量与优化建议](#六代码质量与优化建议)
- [七、推荐系统专项分析](#七推荐系统专项分析)
- [八、工程规范与仓库状态](#八工程规范与仓库状态)
- [九、改进路线图](#九改进路线图)
- [十、总结](#十总结)

---

## 一、项目概述

`bili-live` 是一个**仿哔哩哔哩（bilibili）的视频分享平台**，采用前后端分离架构。严格来说，它是一个**视频点播（VOD）网站**而非直播平台——尽管名称含 "live"，全项目没有直播流、房间、礼物、连麦相关代码，也没有 WebSocket/STOMP 实现。弹幕和"在线人数"通过 HTTP 轮询 + Redis 过期事件模拟，属于伪实时。

- **目标用户**：平台普通观众、内容创作者（UP 主）、后台管理员
- **核心价值**：提供视频投稿、播放、点赞/投币/收藏/评论/弹幕等互动，以及基于协同过滤 + 内容推荐的个性化内容分发
- **近期重点**：新增了一套推荐系统（UserCF + 内容推荐混合算法），并有配套的详细设计文档 `RECOMMENDATION_SYSTEM.md`（约 1543 行）

> ⚠️ 需要留意：名为"直播"但实际是点播、无真正实时通信，这一点在写简历或在面试中介绍项目时必须如实说明，避免被追问后失分。

---

## 二、技术栈

| 层次    | 技术                                                           | 版本/说明                                                  |
| ----- | ------------------------------------------------------------ | ------------------------------------------------------ |
| 后端框架  | Spring Boot                                                  | 2.7.18（父 pom）                                          |
| 语言    | Java                                                         | 父 pom 设 17，子模块覆盖为 1.8（不统一）                             |
| ORM   | MyBatis                                                      | mybatis-spring-boot-starter 1.3.2                      |
| 数据库   | MySQL                                                        | 8.0.23，JDBC 开启 `allowMultiQueries=true`                |
| 缓存/队列 | Redis                                                        | RedisTemplate + JSON 序列化，用于 token、验证码、转码队列、在线人数、推荐缓存   |
| 搜索    | Elasticsearch                                                | RestHighLevelClient，multiMatch 查询 + 高亮 + painless 脚本计数 |
| 视频处理  | FFmpeg / ffprobe                                             | 分片合并、HEVC→H.264 转码、HLS（m3u8+ts）切片                      |
| 安全    | Redis token + Cookie                                         | 非 JWT；UUID 随机 token；AOP/拦截器鉴权                          |
| 前端    | Vue3 + Vite + Element Plus                                   | Vue 3.4 / Vite 5.2 / Element Plus 2.7                  |
| 前端状态  | Pinia                                                        | 用户端使用，后台未实际使用                                          |
| 播放器   | Artplayer + hls.js                                           | 配合 `artplayer-plugin-danmuku` 渲染弹幕                     |
| 图表    | ECharts                                                      | 后台数据看板、用户端个人中心                                         |
| 工具库   | axios / moment / js-md5 / mitt / vue-cookies / fingerprintjs |                                                        |
| 构建    | Maven（多模块）/ npm                                              | `skipTests=true`，默认跳过测试                                |

---

## 三、模块结构

```
bili-live/                          # 父工程 pom（packaging=pom）
├── bili-common/                    # 公共核心模块（134 Java / 15 Mapper XML）
│   ├── Enum/                       # 业务枚举（行为类型、排序、消息类型、推荐类型等）
│   ├── annotation/                 # @GlobalInterceptor 注解
│   ├── advice/ + global/           # 两套全局异常处理（重复）
│   ├── component/                  # EsSearchComponent、RedisComponent
│   ├── entity/config|constants/    # AppConfig、EsConfiguration、Constants
│   ├── mapper/                     # 15 个 Mapper 接口 + XML
│   ├── pojo/                       # 52 个 DO/VO/DTO/Query（平铺）
│   ├── redis/                      # RedisConfig、RedisUtils
│   ├── service/(impl)/             # 17 个 Service 接口 + 实现
│   └── utils/                      # FFmpeg、推荐算法、JSON、日期等工具
├── bili-web/                       # C 端启动模块 :7071（17 Controller + 2 AOP + 监听器 + 任务）
├── bili-admin/                     # 后台启动模块 :7070 /admin（8 Controller + 2 拦截器）
└── bili-live/bili-live-front/      # 前端（不在 Maven modules 中）
    ├── bili-live-front-web/        # 用户端 :3000
    └── bili-live-front-admin/      # 管理后台 :3001
```

**架构特点**：`bili-common` 是被两个启动模块依赖的共享 jar，没有启动类/Controller。两个启动模块都 `@MapperScan("com.bililive.mapper")` 且扫描全包 `com.bililive`，导致各自进程都会加载 common 的全部组件（含 ES 客户端、Redis 监听器、定时任务），职责未隔离。

---

## 四、核心功能

### 4.1 用户与社交

- 注册/登录/自动登录/登出，验证码（easy-captcha），基于 Redis 的 UUID token
- 用户主页、资料修改（硬币消费改昵称）、关注/取关、粉丝/关注列表
- 消息通知（评论/点赞/收藏/系统消息），通过 AOP 在目标方法后自动落库
- 视频合集（创建、增删视频、排序）

### 4.2 视频与互动

- 视频投稿：分片上传 → Redis 队列 → FFmpeg 异步转码 → HLS 切片 → 审核发布（写 ES、硬币奖励）
- 点赞/投币/收藏（状态切换 + 计数）、弹幕、评论（楼中楼、置顶、级联删除）
- 播放历史、分类树管理、站内搜索（ES）、热门视频

### 4.3 后台管理

- 登录、数据看板（ECharts 统计）、用户封禁、视频审核/推荐/删除、评论弹幕审核、分类管理、系统设置

### 4.4 推荐系统

- 4 个接口：个性化推荐、热门推荐、冷启动推荐、清缓存
- 混合推荐：UserCF + Content-Based，Redis 缓存 5 分钟
- 详见第七节专项分析

---

## 五、技术亮点与可展示点

> 这一节用于**简历和面试话术**——即使代码存在缺陷，以下技术点本身是真实实现、有代码支撑的，可以作为项目亮点陈述。面试时被问到"为什么这么做""有什么问题"时，可结合第六节的优化建议展开，体现你的思考深度。

| 亮点               | 技术实现                                                        | 面试可展开的角度                     |
| ---------------- | ----------------------------------------------------------- | ---------------------------- |
| 大规模文件分片上传        | 前端分片 + `preUploadVideo`/`uploadVideo` + Redis 记录进度          | 断点续传如何做、分片乱序/重复如何处理          |
| 异步视频转码流水线        | Redis List 队列 + 线程池消费者 + FFmpeg 转 HLS                       | 事务与队列一致性、失败重试、削峰填谷           |
| 混合推荐算法           | UserCF + Content-Based 加权融合，余弦/Jaccard 相似度                  | 冷启动、稀疏矩阵、A/B 测试、推荐效果评估       |
| Elasticsearch 搜索 | multiMatch + 高亮 + 排序分页 + painless 脚本更新计数                    | 深分页、ES 与 DB 一致性、中文分词         |
| Redis 多场景运用      | token、验证码、转码队列、在线人数（过期监听）、热词、推荐缓存                           | `KEYS` 与 `SCAN`、缓存穿透/击穿、分布式锁 |
| 声明式鉴权            | 自定义 `@GlobalInterceptor` + AOP（C 端）/ HandlerInterceptor（后台） | AOP 原理、Advisor 顺序、注解反射       |
| 事件驱动消息           | `@Around` 切面在业务方法后自动落库消息通知                                  | 切面参数解析、事务边界                  |
| Redis 模拟在线人数     | key 过期事件监听 + 心跳上报 + 设备指纹去重                                  | 为何不用 WebSocket、精确性取舍         |
| 前后端工程            | Vue3 组合式 API、Vite 手写拆包、Artplayer 弹幕插件                       | 前端性能优化、组件复用                  |



---

## 六、代码质量与优化建议

> 按严重程度分级，每条尽量附具体文件/行号证据。建议修复顺序：P0 → P1 → P2。

### P0 · 安全（高危，优先修复）

| #  | 问题                                                    | 证据                                                                                 | 建议                                               |
| -- | ----------------------------------------------------- | ---------------------------------------------------------------------------------- | ------------------------------------------------ |
| 1  | MyBatis 全局 SQL 注入：`ORDER BY ${orderBy}` 直接拼接排序字段，无白名单 | `VideoInfoMapper.xml:265`、`:289`、`UserInfoMapper.xml:79`、`VideoDanmuMapper.xml:69` | 排序字段改白名单枚举校验；`updateCountInfo` 的 `${field}` 同理   |
| 2  | 系统命令执行注入：`/bin/sh -c` 拼接用户上传文件名                       | `utils/ProcessUtils.java:30`、`FfmpegUtils.java:24-58`                              | 改 `ProcessBuilder(List<String>)` 参数数组，路径做白名单/规范化 |
| 3  | 明文数据库口令 + 多语句查询 + 关闭 SSL                              | `bili-web/application.yml:11-13`（`root`/`Ymz123456`/`allowMultiQueries=true`）      | 凭据外置到环境变量/配置中心，按需建最小权限账号                         |
| 4  | 路径遍历：文件读取/删除路径直接拼用户输入，且视频分片无鉴权                        | `web/FileController.java:126-203`、`:458-498`；`admin/FileController.java:394-443`   | 路径规范化 + 白名单根目录校验，资源接口加签名/鉴权防盗链                   |
| 5  | 存储型 XSS：多处 `v-html` 直接渲染评论/简介/标题/公告                   | `VideoCommentItem.vue:25`、`VideoSummary.vue:7`、`Home.vue:12`、`VideoItem.vue:18`    | 引入 DOMPurify 等做白名单过滤                             |
| 6  | MD5 无盐口令存储                                            | `UserInfoServiceImpl.java:46`、`admin/AccountController.java:69`                    | 改 BCrypt/PBKDF2                                  |
| 7  | 验证码答案、管理员 token 写入日志                                  | `web/AccountController.java:47`、`admin/AppInterceptor.java:36,41`                  | 删除敏感日志                                           |
| 8  | 无鉴权的统计重算接口                                            | `UcenterInteractionController.java:89-93` 的 `/test`                                | 删除或加鉴权                                           |
| 9  | Cookie 未设 HttpOnly/Secure/SameSite；token 存前端可读 Cookie | `AccountController.saveToken2Cookie()`、前端 `Request.js`                             | 设置安全属性，配合 XSS 修复                                 |
| 10 | admin 鉴权用 `URI.contains("/account")` 放行，过于宽松          | `admin/AppInterceptor.java`                                                        | 改精确路径匹配                                          |
| 11 | 全项目无 CORS 配置，依赖 nginx 同源代理                            | 全局 grep 无 `CorsFilter`/`@CrossOrigin`                                              | 显式配置跨域策略                                         |

### P1 · 数据一致性与性能

| #  | 问题                                 | 证据                                                                                                         | 建议                                |
| -- | ---------------------------------- | ---------------------------------------------------------------------------------------------------------- | --------------------------------- |
| 12 | 多表写操作缺 `@Transactional`（投币、弹幕、评论）  | `UserActionServiceImpl.java:83-89`、`VideoDanmuServiceImpl.java:43-54`、`VideoCommentServiceImpl.java:65-76` | 补事务，统一标准                          |
| 13 | 硬币可扣成负数，且多处忽略扣减返回值                 | `UserInfoMapper.xml:32`、`UserInfoServiceImpl.java:121`、`VideoInfoServiceImpl.java:159`                     | SQL 加 `AND coin_count >= ?`，校验返回值 |
| 14 | 事务内推 Redis 队列存在竞态；转码失败无重试；线程池无优雅停机 | `VideoInfoPostServiceImpl.saveVideoInfo()`、`ExecuteQueueTask.java`                                         | 用事务消息/延时双删思路，失败入重试队列              |
| 15 | 事务内混用异步线程池 + ES 删除，DB/ES 可能不一致     | `VideoInfoServiceImpl.deleteVideo():129-186`                                                               | 异步与事务解耦，引入最终一致性补偿                 |
| 16 | 推荐服务 N+1 查询 + 全表拉取 + 无界线程池         | `VideoRecommendationServiceImpl.java:281,331,356-362`                                                      | 批量查询、分页限制、有界线程池                   |
| 17 | Redis `KEYS` 阻塞 + `indexOf` O(n²)  | `RedisUtils.java:173,179,182`                                                                              | 改 `SCAN`                          |
| 18 | ES 深分页 + 回查 MySQL 补昵称（N+1）         | `EsSearchComponent.java:170-226`                                                                           | search_after + 用户信息本地缓存           |

### P2 · 功能性 Bug 与代码质量

| #  | 问题                                                                                      | 证据                                                                                   |
| -- | --------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------ |
| 19 | 确定性空指针：合集服务先 `.equals()` 后判 null                                                        | `UserVideoSeriesServiceImpl.java:120-126`                                            |
| 20 | 两套全局异常处理并存，行为不确定                                                                        | `advice/GlobalExceptionHandler.java` 与 `global/GlobalException.java`                 |
| 21 | AOP 把业务码 901 吞成 500，前端跳转登录失效                                                            | `MessageOperactionAspect.java:53-55`                                                 |
| 22 | admin 首次登录无 Cookie 时 `cookies` 未判空 → NPE                                                | `admin/AccountController.java:77-83`                                                 |
| 23 | 前端 Pinia store 字段拼写不一致致状态失效                                                             | `navActionStore.js` 中 `showCategroy` vs `showCategory`                               |
| 24 | 前端 mitt `off(type)` 未传 handler，监听泄漏                                                     | `Player.vue:243`、`VideoDanmu.vue:60`、`Index.vue:83` 等                                |
| 25 | 前端 admin 路由 404 组件缺少 `() =>` 包裹                                                         | `admin/router/index.js:45`                                                           |
| 26 | 重复投稿列表接口、Controller 直连 Mapper、`instanceof` 强转实现类                                        | `VideoInfoController.java`、多个 `FileController`、`admin/CategoryController.java:29-31` |
| 27 | 构建配置不统一：Java 17 vs 1.8，ES starter 3.3.2 与 Boot 2.7 生态不匹配，spring-boot-maven-plugin 2.2.6 | 各 pom.xml                                                                            |
| 28 | `/bin/sh -c` 假设 Linux 环境，与 Windows 开发路径不匹配                                              | `ProcessUtils.java:30` + `project.folder: D:/...`                                    |

---

## 七、推荐系统专项分析

`RECOMMENDATION_SYSTEM.md` 是一份设计完整、描述宏大的文档，但代码实现是"主干可用、细节打折"的版本。

**已实现且吻合的部分**：

- 混合推荐 = UserCF + Content-Based 加权融合（`VideoRecommendationServiceImpl.getHybridRecommendation():152`）
- 行为权重 1/2/3/4/5（`CollaborativeFilteringAlgorithm.ACTION_WEIGHTS`）
- 融合公式、位置倒数得分、缓存 key/TTL、150 维特征向量、4 个接口均已落地

**文档承诺但未落地/有偏差的部分**：

| # | 问题               | 说明                                                                     |
| - | ---------------- | ---------------------------------------------------------------------- |
| 1 | 冷启动阈值不一致（最关键）    | 文档承诺 50，实际生效 5；ServiceImpl 里的 `BEHAVIOR_THRESHOLD=50` 是死常量             |
| 2 | ItemCF 是死代码      | 文档宣传结合 ItemCF，`calculateItemSimilarities()`/`recommendByItemCF()` 无调用点 |
| 3 | 用户行为口径不符         | `getUserActions()` 不含"播放"，与文档/批量查询口径不一                                 |
| 4 | 冷启动 60%/40% 配比失效 | 默认 topN=10 时热门占满，分类均衡数量为 0                                             |
| 5 | "分类均衡"名不副实       | 实为按 `create_time desc` 排序，未做分类分组去重                                     |
| 6 | 中文分词/TF-IDF 缺失   | `extractKeywords()` 只按空格标点 split，对中文几乎无效                               |
| 7 | N+1 查询依旧存在       | 文档批评的反模式在两处仍存在                                                         |

> 这部分是很好的**简历素材**：能讲清楚"文档设计与实现偏差"并给出修复方案，体现工程判断力。详见配套学习计划。

---

## 八、工程规范与仓库状态

- **git 状态**：仅 1 条 `Initial commit`（`bb4602ff`），当前 `master` 分支；推荐系统整套代码和文档均为 **Untracked**，尚未纳入版本控制
- **`.gitignore` 不完整**：只忽略 3 个 `target/`，未忽略 `node_modules`、`dist`、`.idea`、`*.log`
- **工作区垃圾**：3 个 `hs_err_pid*.log`（JVM 崩溃日志）、`dist.rar`（web 6.2MB + admin 2.4MB）、前端 `Api.js_w` 死代码（5.8KB）
- **测试现状**：全项目**零测试代码**，仅 pom 中 `skipTests=true`；`RECOMMENDATION_SYSTEM.md:888` 起有一份测试指南（JUnit 示例 + JMeter 压测场景），可直接作为学习起点

---

## 九、改进路线图

| 阶段  | 目标  | 主要工作                                                       |
| --- | --- | ---------------------------------------------------------- |
| 第一步 | 止血  | 修复 P0 安全项：SQL 注入、命令注入、明文密码、路径遍历、XSS、敏感日志                   |
| 第二步 | 稳基  | 补事务、硬币并发扣减、队列一致性；修复 P2 中的确定性 bug（#19-25）                   |
| 第三步 | 提效  | 消除 N+1、`KEYS`→`SCAN`、ES 深分页、批量查询；统一构建配置                    |
| 第四步 | 正名  | 推荐系统：统一阈值、接入或删减 ItemCF、修正冷启动配比、引入中文分词（IK）                  |
| 第五步 | 工程化 | 纳入 git、完善 `.gitignore`、抽取前端公共模块、补 ESLint/Prettier/TS、建立 CI |

---

## 十、总结

`bili-live` 是一个功能覆盖完整、技术栈丰富的仿 B 站视频点播平台，采用 Spring Boot 多模块 + Vue3 前后端分离架构，包含分片上传、FFmpeg 转码、ES 搜索、Redis 多场景运用和混合推荐算法等较多可展示的技术点。

主要短板集中在：**系统性安全风险**（SQL/命令注入、明文口令、XSS、路径遍历）、**数据一致性隐患**（事务缺失、并发扣币）、**推荐系统文档与实现偏差**，以及**工程规范薄弱**（无测试、无 CI、代码重复、仓库未清理）。

建议以"先安全止血、再一致性、后性能与工程化"的顺序推进；同时，本项目"有亮点也有明显问题"的特点，非常适合作为**学习测试 + 简历包装**的载体——具体计划见配套文档 `TESTING_LEARNING_PLAN.md`。
