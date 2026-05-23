# 数据库设计文档

> 数据库名：`software_design` · MySQL 8.0 · 字符集 utf8mb4

---

## 1. volunteer_user — 志愿者用户

| 字段 | 类型 | 空 | 键 | 说明 |
|------|------|:---:|:---:|------|
| user_id | varchar(10) | NO | PK | 系统生成的用户唯一ID |
| user_password | varchar(255) | NO | | 明文密码 |
| user_permission | int | NO | | 固定值 1（标识角色） |
| user_name | varchar(50) | YES | | 真实姓名 |
| id_number | varchar(18) | YES | UNI | 身份证号，唯一，注册必填 |
| user_sex | varchar(255) | YES | | 性别（男/女） |
| user_edu | varchar(255) | YES | | 学历（高中/专科/本科/硕士/博士） |
| user_phone | varchar(20) | NO | | 手机号，用于登录 |
| register_time | datetime | NO | | 注册时间 |

## 2. school_user — 学校用户

| 字段 | 类型 | 空 | 键 | 说明 |
|------|------|:---:|:---:|------|
| user_id | varchar(10) | NO | PK | 系统生成的用户唯一ID |
| user_password | varchar(255) | NO | | 明文密码 |
| user_permission | int | NO | | 固定值 2（标识角色） |
| school_name | varchar(255) | NO | | 学校全称 |
| type | varchar(255) | YES | | 学校类型（小学/初中/高中） |
| address | varchar(255) | YES | | 学校地址（省/市/县/镇） |
| license | varchar(255) | YES | UNI | 办学许可证号，唯一 |
| principle | varchar(255) | YES | | 学校负责人姓名 |
| user_phone | varchar(20) | NO | | 手机号，用于登录 |
| register_time | datetime | NO | | 注册时间 |

## 3. administrator — 管理员

| 字段 | 类型 | 空 | 键 | 说明 |
|------|------|:---:|:---:|------|
| user_id | varchar(10) | NO | PK | 系统生成的用户唯一ID |
| user_password | varchar(255) | NO | | 明文密码 |
| user_permission | int | NO | | 固定值 3（标识角色） |
| user_phone | varchar(20) | NO | | 手机号，用于登录 |
| register_time | datetime | NO | | 注册时间 |
 
## 4. activity — 活动

| 字段 | 类型 | 空 | 键 | 说明 |
|------|------|:---:|:---:|------|
| activity_id | varchar(10) | NO | PK | 系统生成的活动唯一ID |
| user_id | varchar(10) | YES | FK | 发布者（学校用户）ID |
| title | text | NO | | 活动标题 |
| content | text | NO | | 活动详细内容 |
| recruits_number | int | NO | | 招募志愿者人数 |
| school_address | text | YES | | 活动地址（冗余自学校地址） |
| start_date | date | YES | | 活动开始日期 |
| end_date | date | YES | | 活动结束日期 |
| volunteer_duration | int | YES | | 志愿时长（小时） |
| activity_state | text | NO | | 活动状态：`'0'` 招募中 / `'1'` 进行中 / `'2'` 已结束 |
| audit_state | text | NO | | 审核状态：`'0'` 待审核 / `'1'` 通过 / `'2'` 未通过 |
| audit_time | datetime | YES | | 审核时间 |
| publish_time | datetime | NO | | 发布时间 |
| summary | text | YES | | **已废弃** |
| summary_state | text | YES | | **已废弃** |
| summary_title | varchar(200) | YES | | 活动总结标题 |
| summary_content | text | YES | | 活动总结内容 |
| summary_audit_state | varchar(2) | YES | | 总结审核状态：`'0'` 待审核 / `'1'` 通过 / `'2'` 未通过 |
| summary_submit_time | datetime | YES | | 总结提交时间 |

## 5. post — 讨论帖

| 字段 | 类型 | 空 | 键 | 说明 |
|------|------|:---:|:---:|------|
| post_id | varchar(10) | NO | PK | 系统生成的帖子唯一ID |
| user_id | varchar(10) | YES | FK | 发布者用户ID |
| title | varchar(100) | NO | | 帖子标题 |
| content | text | NO | | 帖子内容 |
| activity_id | varchar(10) | YES | FK | 关联活动ID |
| audit_state | text | NO | | 审核状态：`'0'` 待审核 / `'1'` 通过 / `'2'` 未通过 |
| audit_time | datetime | YES | | 审核时间 |
| publish_time | datetime | NO | | 发布时间 |

## 6. comment — 评论

| 字段 | 类型 | 空 | 键 | 说明 |
|------|------|:---:|:---:|------|
| comment_id | varchar(10) | NO | PK | 系统生成的评论唯一ID |
| post_id | varchar(10) | YES | FK | 所属帖子ID |
| user_id | varchar(10) | YES | FK | 评论者用户ID |
| content | text | NO | | 评论内容 |
| publish_time | datetime | NO | | 发布时间 |

## 7. registration — 活动报名

| 字段 | 类型 | 空 | 键 | 说明 |
|------|------|:---:|:---:|------|
| registration_id | varchar(10) | NO | PK | 系统生成的报名唯一ID |
| user_id | varchar(10) | YES | FK | 报名者（志愿者）用户ID |
| activity_id | varchar(10) | YES | FK | 报名的活动ID |
| real_name | varchar(100) | NO | | 报名时填写的真实姓名 |
| phone_number | varchar(20) | YES | | 报名时填写的手机号 |
| id_number | varchar(18) | NO | | 报名时填写的身份证号（当前版本报名表单已移除此字段） |
| gender | varchar(100) | YES | | 性别 |
| degree | varchar(200) | YES | | 学历 |
| introduce | text | YES | | 自我介绍（前端字段名 schoolWork） |
| audit_state | text | NO | | 审核状态：`'0'` 待审核 / `'1'` 通过 / `'2'` 未通过 |
| entry_time | datetime | NO | | 报名时间 |

## 8. user — 旧用户表（未使用）

| 字段 | 类型 | 空 | 键 | 说明 |
|------|------|:---:|:---:|------|
| user_id | varchar(10) | NO | PK | 用户ID |
| user_password | varchar(255) | NO | | 密码 |
| user_permission | int | NO | | 权限 |
| user_phone | varchar(20) | YES | | 手机号 |
| register_time | datetime | NO | | 注册时间 |

> 此表为项目早期遗留，当前版本已不再使用，所有用户数据已拆分到 volunteer_user / school_user / administrator 三张表中。

---

## 枚举值速查

| 字段 | 可选值 | 说明 |
|------|--------|------|
| user_permission | 1 | 志愿者 |
| | 2 | 学校用户 |
| | 3 | 管理员 |
| activity_state | '0' | 招募中 |
| | '1' | 进行中 |
| | '2' | 已结束 |
| audit_state / summary_audit_state | '0' | 待审核 |
| | '1' | 审核通过 |
| | '2' | 未通过 |
