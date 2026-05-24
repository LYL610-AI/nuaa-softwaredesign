# API 接口文档

> 基础路径：`/api` · 统一响应格式：`{ code, message, data }` · 鉴权方式：`Authorization: Bearer {token}`

---

## 1. 用户模块 `/api/user`

### POST /user/login
登录

| 参数 | 类型 | 必填 | 说明 |
|------|------|:---:|------|
| phone | String | 是 | 手机号 |
| password | String | 是 | 密码 |
| role | int | 是 | 1=志愿者 2=学校 3=管理员 |

响应 `data` 包含 `userId`、`userPermission`、`userPhone`、`registerTime`、`token` 等。

### POST /user/register
注册（志愿者或学校用户）。志愿者需传 `userName`、`idNumber`、`userSex`、`userEdu`、`userPhone`、`userPassword`；学校需传 `schoolName`、`type`、`address`、`license`、`principle`、`userPhone`、`userPassword`。

### GET /user/info
获取当前登录用户信息。

### PUT /user/update
更新个人资料。

### PUT /user/password
修改密码，参数 `oldPwd`、`newPwd`。

### POST /user/recover-password
找回密码，参数 `idNumber`（志愿者）或 `license`（学校）+ `newPassword`。

| 接口 | 方法 | 说明 |
|------|------|------|
| /user/checkPhone | GET | 检查手机号，参数 `phone` |
| /user/checkIdNumber | GET | 检查身份证号，参数 `idNumber` |
| /user/checkLicense | GET | 检查办学许可证号，参数 `license` |
| /user/list | GET | 管理员-用户列表，参数 `permission`、`keyword`、`page`、`pageSize` |
| /user/reset-password/{userId} | PUT | 管理员-重置密码 |
| /user/admin-update/{userId} | PUT | 管理员-编辑用户 |
| /user/delete/{userId} | DELETE | 管理员-删除用户，参数 `permission` |

---

## 2. 活动模块 `/api/activity`

### GET /activity/list
活动列表。参数 `keyword`、`region`、`state`、`auditState`、`page`、`pageSize`。

### GET /activity/detail/{id}
活动详情。响应含 `pictureUrl`（封面图片 URL，可能为空）。

### POST /activity/create
发布活动（学校用户）。

| 参数 | 类型 | 必填 | 说明 |
|------|------|:---:|------|
| title | String | 是 | 活动标题 |
| content | String | 是 | 活动内容 |
| recruitsNumber | int | 是 | 招募人数 |
| startDate | String | 否 | 开始日期 |
| endDate | String | 否 | 结束日期 |
| address | String | 否 | 活动地址 |
| pictureUrl | String | 否 | 封面图片 URL，通过 `/file/upload` 获取 |

### PUT /activity/update/{id}
更新活动，参数同上。

| 接口 | 方法 | 说明 |
|------|------|------|
| /activity/my | GET | 我的活动 |
| /activity/review/{id} | PUT | 管理员审核，参数 `auditState`、`reason` |
| /activity/state/{id} | PUT | 更改活动状态，参数 `activityState` |
| /activity/delete/{id} | DELETE | 删除活动 |
| /activity/summary/{id} | POST | 提交活动总结 |
| /activity/summary/review/{id} | PUT | 管理员审核总结，参数 `auditState` |
| /activity/summary/list | GET | 总结列表（管理员） |
| /activity/reviewed | GET | 已审核活动列表 |
| /activity/summary/reviewed | GET | 已审核总结列表 |

---

## 3. 帖子模块 `/api/post`

### POST /post/create
发布帖子。

| 参数 | 类型 | 必填 | 说明 |
|------|------|:---:|------|
| activityId | String | 是 | 关联活动 ID |
| title | String | 是 | 帖子标题 |
| content | String | 是 | 帖子内容 |
| pictureUrl | String | 否 | 图片 URL，通过 `/file/upload` 获取 |

### GET /post/detail/{id}
帖子详情。响应含 `pictureUrl`、`authorName`、`activityTitle`、`commentCount`。

| 接口 | 方法 | 说明 |
|------|------|------|
| /post/list | GET | 帖子列表，参数 `activityId`、`auditState`、`page`、`pageSize` |
| /post/my | GET | 我的帖子 |
| /post/reviewed | GET | 已审核帖子列表 |
| /post/update/{id} | PUT | 更新帖子 |
| /post/review/{id} | PUT | 管理员审核，参数 `auditState` |
| /post/delete/{id} | DELETE | 删除帖子 |

---

## 4. 评论模块 `/api/comment`

| 接口 | 方法 | 说明 |
|------|------|------|
| /comment/list/{postId} | GET | 获取帖子评论列表 |
| /comment/create | POST | 发表评论，参数 `postId`、`content` |
| /comment/update/{id} | PUT | 更新评论，参数 `content` |
| /comment/delete/{id} | DELETE | 删除评论 |

---

## 5. 报名模块 `/api/registration`

| 接口 | 方法 | 说明 |
|------|------|------|
| /registration/submit | POST | 提交报名，参数 `activityId`、`schoolWork` |
| /registration/list/{activityId} | GET | 活动报名列表（学校用户） |
| /registration/my | GET | 我的报名记录 |
| /registration/list/all | GET | 全部报名（管理员） |
| /registration/count/{activityId} | GET | 活动已通过报名人数 |
| /registration/check/{activityId} | GET | 检查当前用户是否已报名 |
| /registration/review/{id} | PUT | 审核报名，参数 `auditState` |
| /registration/cancel/{id} | DELETE | 取消报名 |

---

## 6. 文件上传 `/api/file`（新增）

### POST /file/upload
上传图片，返回可访问 URL。

**请求头** `Authorization: Bearer {token}`

**请求体** `multipart/form-data`

| 参数 | 类型 | 必填 | 说明 |
|------|------|:---:|------|
| file | File | 是 | 图片文件 |

**约束**：JPG / PNG / GIF / WebP，≤ 10MB

**成功** `200`
```json
{ "code": 200, "message": "ok", "data": { "url": "/uploads/abc123.jpg" } }
```

**错误**：400（文件为空/超大小/格式不支持）、401（未登录）、500（服务器错误）

---

## 公开接口（无需登录）

| 方法 | 接口 |
|------|------|
| GET | /activity/list、/activity/detail/{id} |
| GET | /post/list、/post/detail/{id} |
| GET | /comment/list/{postId} |
| GET | /registration/count/{activityId} |
| GET | /user/checkPhone、/user/checkIdNumber、/user/checkLicense |
| POST | /user/login、/user/register、/user/recover-password |
