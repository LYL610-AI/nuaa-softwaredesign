# 支教信息发布平台

基于 Web 的支教活动全流程管理平台，连接支教学校与志愿者，支持活动发布、报名审核、社区互动等功能。

**技术栈**：HTML5 + CSS3 + JavaScript（前端）/ Spring Boot 3.2.5 + JdbcTemplate（后端）/ MySQL 8.0（数据库）/ 内嵌 Tomcat（部署）/ 图片上传（multipart）

---

## 一、用户角色

| 角色 | 标识 | 核心权限 |
|------|------|----------|
| 志愿者用户 | `user_permission = 1` | 浏览活动、报名支教、取消报名、发帖评论 |
| 学校用户 | `user_permission = 2` | 发布支教活动、审核报名、提交活动总结 |
| 管理员 | `user_permission = 3` | 审核活动、审核帖子、审核总结、管理用户 |

---

## 二、功能模块

### 1. 用户模块

- 三种角色独立注册/登录，角色选择切换后展示对应表单
- 学校用户注册字段：学校名称、负责人、办学许可证号、学校地址、学校类型、联系电话
- 志愿者用户注册字段：真实姓名、身份证号、性别、学历、手机号
- 管理员由系统预设，不开放自行注册
- 公共功能：个人信息修改、密码修改、找回密码

### 2. 支教活动模块

- 学校用户发布招募信息：标题、内容、招募人数、活动地址、封面图片（可选上传）
- 管理员审核活动（通过/退回），审核通过后前端可见
- 所有用户可查看已通过的活动列表
- 支持按标题关键字、地区、活动状态搜索筛选
- 活动状态流转：**招募中(0)** → **进行中(1)** → **已结束(2)**
- 审核状态：待审核(0) / 审核通过(1) / 未通过(2)

### 3. 报名模块

- 志愿者填写报名表：真实姓名、身份证号、手机号、性别、学历、学校/工作单位
- 志愿者可在个人中心取消已提交的报名
- 学校用户在活动详情页查看报名列表并审核（通过/退回）
- 报名审核状态：待审核(0) / 已通过(1) / 未通过(2)

### 4. 社区互动模块

- 用户发布主题帖（支持可选上传图片），提交后需管理员审核
- 所有用户可在帖子详情中发表评论
- 帖子发布者或管理员可删除帖子
- 评论发布者或管理员可删除评论
- 讨论区右侧栏支持按关联活动筛选帖子

### 5. 四重审核流程

| 审核项 | 提交者 | 审核者 |
|--------|--------|--------|
| 支教活动发布 | 学校用户 | 管理员 |
| 志愿者报名 | 志愿者 | 学校用户 |
| 主题帖发布 | 所有用户 | 管理员 |
| 活动总结报告 | 学校用户 | 管理员 |

---

## 三、项目结构

```
web设计/
├── pom.xml                                    # Maven 依赖管理
├── README.md                                  # 项目说明
├── src/main/java/com/teachingplatform/
│   ├── TeachingPlatformApplication.java       # Spring Boot 启动入口
│   ├── config/
│   │   ├── CorsConfig.java                   # CORS 跨域配置
│   │   └── WebConfig.java                    # 拦截器注册
│   ├── interceptor/
│   │   └── AuthInterceptor.java              # JWT 认证拦截器
│   ├── controller/                           # REST 控制器（6个）
│   │   ├── UserController.java               # /api/user/*
│   │   ├── ActivityController.java           # /api/activity/*
│   │   ├── RegistrationController.java       # /api/registration/*
│   │   ├── PostController.java               # /api/post/*
│   │   ├── CommentController.java            # /api/comment/*
│   │   └── FileController.java               # /api/file/*
│   ├── service/                              # 业务逻辑层（6个）
│   ├── dao/                                  # 数据访问层（JdbcTemplate）
│   ├── entity/                               # 实体类（8个）
│   └── util/                                 # JwtUtil + Result
├── src/main/resources/
│   ├── application.yml                        # 全局配置（数据库、JWT）
│   └── static/                                # 前端静态资源
│       ├── index.html                         # 首页
│       ├── css/style.css                      # 全局样式
│       ├── js/
│       │   ├── api.js                         # API 请求封装
│       │   └── main.js                        # 公共工具函数
│       └── pages/                             # 页面（8个）
└── target/
    └── teaching-platform-1.0.0.jar            # 可执行 JAR 包
```

---

## 四、界面设计

### 设计规范

- **主题色**：浅蓝 `#5B9BD5`，辅色 `#3A7BBF`，浅色背景 `#B8D4F0`
- **背景色**：`#F2F6FA`，卡片白色 `#FFFFFF`
- **圆角**：8px（卡片）/ 4px（按钮、输入框）
- **阴影**：`0 2px 12px rgba(0,0,0,0.08)`，悬停加深
- **字体**：PingFang SC / Microsoft YaHei / Segoe UI 系统字体栈
- **布局**：Flexbox + Grid，最大宽度 1200px 居中

### 页面详情

#### 首页（index.html）
- 顶部渐变横幅：「让知识跨越山海」标语
- 搜索栏：标题关键字 + 地区 + 活动状态下拉
- 四张统计卡片：累计活动数 / 招募中 / 进行中 / 已结束
- 活动卡片网格（自适应列数）：标题、学校、地址、招募人数、志愿时长、状态标签
- 底部分页组件

#### 登录页（login.html）
- 全屏蓝色渐变背景，白色居中卡片
- 三角色标签切换按钮：志愿者 / 学校用户 / 管理员
- 账号 + 密码 + 记住账号 + 忘记密码链接
- 登录成功后根据 user_permission 跳转首页，导航栏自动切换

#### 注册页（register.html）
- 顶部角色切换：志愿者注册 / 学校用户注册
- 公共字段：账号、密码、确认密码、手机号
- 志愿者特有：真实姓名、性别、学历（动态显示/隐藏 + required 切换）
- 学校用户特有：学校名称、负责人、办学许可证号、学校类型、学校地址

#### 活动详情页（activity-detail.html）
- 返回按钮 + 活动信息卡片（完整字段展示）
- 招募中活动：志愿者可见「我要报名」按钮，弹出报名表单（真实姓名、身份证、手机号、性别、学历、学校/工作单位）
- 学校用户可见报名列表表格，审核按钮（通过/退回）

#### 发布活动页（publish-activity.html）
- 表单：标题、内容(文本域)、招募人数、志愿时长、活动地址
- 确认信息勾选框 + 提交审核按钮 + 保存草稿按钮
- 下方表格：我发布的活动列表，可编辑/提交总结

#### 个人中心页（personal-center.html）
- 用户信息卡片：头像占位、姓名、角色、手机号、注册时间
- 四个标签页切换：
  - 我的报名：表格 + 取消报名按钮
  - 我的活动：表格 + 编辑/提交总结
  - 我的帖子：表格 + 删除按钮
  - 账号安全：修改密码表单

#### 审核管理页（admin-review.html）
- 四张待审统计卡片（黄色数字）
- 四个审核标签页：活动审核 / 报名审核 / 帖子审核 / 总结审核
- 每页表格 + 通过/退回按钮，退回可填写原因

#### 讨论区页（discussion.html）
- 双栏布局：左侧帖子列表（标题、摘要、元信息），右侧活动筛选 + 帖子详情
- 点击帖子标题展开详情，显示评论列表 + 发表评论输入框
- 发布主题帖弹窗：选择关联活动 + 标题 + 内容

### 导航栏自适应

| 登录状态 | 显示内容 |
|----------|----------|
| 未登录 | 导航链接隐藏，右上角登录/注册按钮 |
| 志愿者 | 首页、讨论区 ｜ 用户名、个人中心、退出 |
| 学校用户 | 首页、发布活动、讨论区 ｜ 用户名、个人中心、退出 |
| 管理员 | 首页、审核管理、讨论区 ｜ 用户名、个人中心、退出 |

---

## 五、数据库设计

系统使用 8 张 MySQL 表：

| 表名 | 说明 | 核心字段 |
|------|------|----------|
| `user` | 用户基础表 | user_id(PK), user_password, user_permission, user_phone, register_time |
| `school_user` | 学校用户表 | user_id(PK), user_password, type, address, license, principle, user_phone, register_time |
| `volunteer_user` | 志愿者用户表 | user_id(PK), user_password, user_identity, user_sex, user_edu, user_phone, register_time |
| `administrator` | 管理员表 | user_id(PK), user_password, user_permission, user_phone, register_time |
| `activity` | 支教活动表 | activity_id(PK), user_id(FK), title, content, recruits_number, school_address, start_date, end_date, activity_state, audit_state, picture_url, publish_time |
| `registration` | 报名表 | registration_id(PK), user_id(FK), activity_id(FK), phone_number, real_name, id_number, gender, degree, introduce, audit_state, entry_time |
| `post` | 主题帖表 | post_id(PK), user_id(FK), activity_id(FK), title, content, picture_url, audit_state, publish_time, audit_time |
| `comment` | 评论表 | comment_id(PK), post_id(FK), user_id(FK), content, publish_time |

**状态枚举**：

| 字段 | 值 | 含义 |
|------|-----|------|
| activity_state | 0 / 1 / 2 | 招募中 / 进行中 / 已结束 |
| audit_state | 0 / 1 / 2 | 待审核 / 审核通过 / 未通过 |

---

## 六、后端 API 设计

统一响应格式：`{ code: number, data: any, message: string }`
统一错误格式：`{ code: 4xx/5xx, message: "错误描述" }`
认证方式：`Authorization: Bearer <token>`（JWT）

| 模块 | Method | URL | 说明 | 权限 |
|------|--------|-----|------|------|
| 用户 | POST | `/api/user/login` | 登录 | 公开 |
| 用户 | POST | `/api/user/register` | 注册 | 公开 |
| 用户 | GET | `/api/user/info` | 获取当前用户信息 | 登录 |
| 用户 | PUT | `/api/user/password` | 修改密码 | 登录 |
| 活动 | GET | `/api/activity/list` | 活动列表（支持 ?keyword=&region=&state=&page=&pageSize=） | 公开 |
| 活动 | GET | `/api/activity/detail/:id` | 活动详情 | 公开 |
| 活动 | GET | `/api/activity/my` | 我的活动列表 | 学校 |
| 活动 | POST | `/api/activity/create` | 发布活动 | 学校 |
| 活动 | PUT | `/api/activity/review/:id` | 审核活动 | 管理员 |
| 活动 | PUT | `/api/activity/summary/review/:id` | 审核活动总结 | 管理员 |
| 报名 | GET | `/api/registration/my` | 我的报名列表 | 志愿者 |
| 报名 | GET | `/api/registration/list/all` | 全部报名列表（支持 ?auditState=&page=&pageSize=） | 管理员 |
| 报名 | GET | `/api/registration/list/:activityId` | 查看活动报名列表 | 学校(发布者) |
| 报名 | POST | `/api/registration/submit` | 提交报名 | 志愿者 |
| 报名 | DELETE | `/api/registration/cancel/:id` | 取消报名 | 志愿者(本人) |
| 报名 | PUT | `/api/registration/review/:id` | 审核报名 | 学校 |
| 帖子 | GET | `/api/post/list` | 帖子列表（支持 ?activityId=&auditState=&page=&pageSize=） | 公开 |
| 帖子 | GET | `/api/post/detail/:id` | 帖子详情 | 公开 |
| 帖子 | GET | `/api/post/my` | 我的帖子列表 | 登录 |
| 帖子 | POST | `/api/post/create` | 发布帖子 | 登录 |
| 帖子 | DELETE | `/api/post/delete/:id` | 删除帖子 | 发布者/管理员 |
| 帖子 | PUT | `/api/post/review/:id` | 审核帖子 | 管理员 |
| 评论 | GET | `/api/comment/list/:postId` | 评论列表 | 公开 |
| 评论 | POST | `/api/comment/create` | 发表评论 | 登录 |
| 评论 | DELETE | `/api/comment/delete/:id` | 删除评论 | 发布者/管理员 |
| 文件 | POST | `/api/file/upload` | 上传图片（multipart，返回 URL） | 登录 |

---

## 七、部署方案

```
用户浏览器
    │
    ▼
Nginx (:80) ── 反向代理 ──► Spring Boot (:8080, 内嵌 Tomcat)
                                   │
                                   ▼
                              MySQL (:3306)
```

前端静态文件和后端 API 同在一个 JAR 包中，无需单独部署 Tomcat。

**环境要求**：
- JDK 17+
- MySQL 8.0+

**部署步骤**：

```bash
# 1. 修改 application.yml 中的数据库连接信息
#    spring.datasource.url / username / password

# 2. 打包
mvn clean package -DskipTests

# 3. 确保上传目录存在
mkdir -p /www/wwwroot/software_design/pictures

# 4. 上传 JAR 到服务器
scp target/2.0.jar root@服务器IP:/www/wwwroot/software_design/

# 5. 创建数据库（默认库名 software_design）并导入 software_design.sql
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS software_design DEFAULT CHARSET utf8mb4"
mysql -u root -p software_design < software_design.sql

# 6. 启动
java -jar /www/wwwroot/software_design/2.0.jar &

# 7. （可选）Nginx 反向代理
# location / { proxy_pass http://127.0.0.1:8080; }
# location /uploads/ { alias /www/wwwroot/software_design/pictures/; }
```

---

## 八、状态流转图

```
活动生命周期：
  学校发布 ──► 待审核 ──► 审核通过 ──► 招募中 ──► 进行中 ──► 提交总结 ──► 审核总结 ──► 已结束
                │                        │                      │
                ▼                        ▼                      ▼
              未通过                    未通过                  未通过
             (退回)                   (退回)                 (退回)

报名流程：
  志愿者报名 ──► 待审核 ──► 学校通过 ──► 已通过
                  │            │
                  ▼            ▼
                未通过       志愿者取消报名
               (退回)

社区互动：
  发布帖子 ──► 待审核 ──► 审核通过 ──► 展示 ──► 用户评论
                │                              │
                ▼                              ▼
              未通过                         删除评论/帖子
             (退回)
```
