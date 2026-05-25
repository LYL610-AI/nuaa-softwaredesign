# Android 端 API 对接完整指南

---

## 一、后端服务器信息

| 项目 | 值 |
|------|-----|
| 云服务器地址 | `http://<你的服务器公网IP>:8080` |
| API 前缀 | `/api` |
| 认证方式 | JWT，Header 携带 `Authorization: Bearer <token>` |
| 响应格式 | `{ "code": 200, "data": {...}, "message": "ok" }` |
| Token 有效期 | 7 天 |

> 替换 `<你的服务器公网IP>` 为实际云服务器 IP。

---

## 二、Android 项目依赖

`app/build.gradle.kts` 添加：

```kotlin
dependencies {
    // Retrofit + Gson
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // ViewModel + LiveData（可选，按需选用）
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")
}
```

`AndroidManifest.xml` 添加网络权限：

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

并在 `<application>` 标签中加 `android:usesCleartextTraffic="true"`（因为后端是 HTTP 而非 HTTPS）：

```xml
<application
    android:usesCleartextTraffic="true"
    ...>
```

---

## 三、网络层搭建

### 3.1 统一响应基类

```kotlin
// ApiResponse.kt
data class ApiResponse<T>(
    val code: Int,
    val message: String? = null,
    val data: T? = null
)
```

> 后端配置了 `jackson.default-property-inclusion: non_null`，`data` 为 null 时 JSON 中不返回该字段，Gson 解析后为 null。

### 3.2 Token 管理器

```kotlin
// TokenManager.kt
object TokenManager {
    private val prefs = /* 你的 SharedPreferences / DataStore */
    private const val KEY_TOKEN = "jwt_token"

    fun saveToken(token: String) {
        prefs.edit().putString(KEY_TOKEN, token).apply()
    }

    fun getToken(): String? {
        return prefs.getString(KEY_TOKEN, null)
    }

    fun clearToken() {
        prefs.edit().remove(KEY_TOKEN).apply()
    }

    fun isLoggedIn(): Boolean = getToken() != null
}
```

### 3.3 Auth 拦截器（自动附加 Token）

```kotlin
// AuthInterceptor.kt
class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = TokenManager.getToken()
        val request = if (token != null) {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            chain.request()
        }
        return chain.proceed(request)
    }
}
```

### 3.4 Retrofit 单例

```kotlin
// RetrofitClient.kt
object RetrofitClient {
    // 改成你的服务器地址
    private const val BASE_URL = "http://<服务器公网IP>:8080/"

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(AuthInterceptor())              // 自动带 Token
        .addInterceptor(HttpLoggingInterceptor().apply { // 调试用，上线可关
            level = HttpLoggingInterceptor.Level.BODY
        })
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // 各模块 Service（懒加载）
    val userService: UserService by lazy { retrofit.create(UserService::class.java) }
    val activityService: ActivityService by lazy { retrofit.create(ActivityService::class.java) }
    val registrationService: RegistrationService by lazy { retrofit.create(RegistrationService::class.java) }
    val postService: PostService by lazy { retrofit.create(PostService::class.java) }
    val commentService: CommentService by lazy { retrofit.create(CommentService::class.java) }
    val fileService: FileService by lazy { retrofit.create(FileService::class.java) }
}
```

### 3.5 处理 401 让用户重新登录

在实际项目中，可以在 `AuthInterceptor` 或 Repository 层统一处理 401：

```kotlin
// TokenManager 配合：收到 401 时
if (response.code == 401) {
    TokenManager.clearToken()
    // 发送 Event / 跳转到 LoginActivity
}
```

---

## 四、数据模型（Data Class）

### 4.1 用户相关

```kotlin
// 登录请求
data class LoginRequest(
    val phone: String,
    val password: String,
    val role: Int         // 1=志愿者 2=学校用户 3=管理员
)

// 登录响应
data class LoginResponse(
    val token: String,
    val userId: String,
    val userName: String?,
    val permission: Int,
    val phone: String?
)

// 志愿者注册请求
data class VolunteerRegisterRequest(
    val phone: String,
    val password: String,
    val role: String = "1",
    val realName: String,
    val idNumber: String,
    val sex: String,            // "男" / "女"
    val edu: String             // "高中" / "专科" / "本科" / "硕士" / "博士"
)

// 学校用户注册请求
data class SchoolRegisterRequest(
    val phone: String,
    val password: String,
    val role: String = "2",
    val schoolName: String,
    val principle: String,
    val license: String,
    val type: String,           // "小学" / "初中" / "高中"
    val address: String
)

// 修改密码请求
data class ChangePasswordRequest(
    val oldPwd: String,
    val newPwd: String
)

// 找回密码请求
data class RecoverPasswordRequest(
    val type: String,           // "volunteer" / "school"
    val idNumber: String? = null,
    val license: String? = null,
    val newPassword: String
)

// 用户信息（响应，字段随角色不同）
data class UserInfo(
    val userId: String? = null,
    val userName: String? = null,
    val schoolName: String? = null,
    val userPermission: Int? = null,
    val idNumber: String? = null,
    val userSex: String? = null,
    val userEdu: String? = null,
    val principle: String? = null,
    val type: String? = null,
    val address: String? = null,
    val license: String? = null,
    val userPhone: String? = null,
    val registerTime: String? = null
)
```

### 4.2 活动

```kotlin
data class Activity(
    val activityId: String? = null,
    val title: String? = null,
    val content: String? = null,
    val recruitsNumber: Int? = null,
    val volunteerDuration: Int? = null,
    val activityState: String? = null,    // "0"/"1"/"2"
    val auditState: String? = null,       // "0"/"1"/"2"
    val publishTime: String? = null,
    val auditTime: String? = null,
    val userId: String? = null,
    val schoolName: String? = null,
    val schoolAddress: String? = null,
    val address: String? = null,          // 发布时用，同 schoolAddress
    val summaryTitle: String? = null,
    val summaryContent: String? = null,
    val summaryAuditState: String? = null,
    val summarySubmitTime: String? = null
)

// 列表响应
data class ActivityListResponse(
    val list: List<Activity>,
    val total: Int,
    val page: Int,
    val pageSize: Int
)
```

### 4.3 报名

```kotlin
data class Registration(
    val registrationId: String? = null,
    val activityId: String? = null,
    val userId: String? = null,
    val realName: String? = null,
    val phoneNumber: String? = null,
    val idNumber: String? = null,
    val gender: String? = null,
    val degree: String? = null,
    val schoolWork: String? = null,       // 学校/工作单位
    val auditState: String? = null,       // "0"/"1"/"2"
    val entryTime: String? = null,
    val activityTitle: String? = null
)
```

### 4.4 帖子

```kotlin
data class Post(
    val postId: String? = null,
    val title: String? = null,
    val content: String? = null,
    val auditState: String? = null,       // "0"/"1"/"2"
    val publishTime: String? = null,
    val auditTime: String? = null,
    val activityId: String? = null,
    val userId: String? = null,
    val authorName: String? = null,
    val activityTitle: String? = null,
    val commentCount: Int? = null
)
```

### 4.5 评论

```kotlin
data class Comment(
    val commentId: String? = null,
    val content: String? = null,
    val publishTime: String? = null,
    val postId: String? = null,
    val userId: String? = null,
    val authorName: String? = null
)
```

---

## 五、Retrofit API 接口定义

### 5.1 UserService

```kotlin
// UserService.kt
interface UserService {

    // 登录（公开）
    @POST("api/user/login")
    suspend fun login(@Body request: LoginRequest): ApiResponse<LoginResponse>

    // 注册（公开）
    @POST("api/user/register")
    suspend fun register(@Body body: Map<String, String>): ApiResponse<Any>

    // 获取当前用户信息（需 Token）
    @GET("api/user/info")
    suspend fun getInfo(): ApiResponse<UserInfo>

    // 修改密码（需 Token）
    @PUT("api/user/password")
    suspend fun changePassword(@Body body: ChangePasswordRequest): ApiResponse<Any>

    // 修改个人信息（需 Token）
    @PUT("api/user/update")
    suspend fun updateProfile(@Body body: Map<String, String>): ApiResponse<Any>

    // 找回密码（公开）
    @POST("api/user/recover-password")
    suspend fun recoverPassword(@Body body: RecoverPasswordRequest): ApiResponse<Any>

    // 检查手机号（公开）
    @GET("api/user/checkPhone")
    suspend fun checkPhone(@Query("phone") phone: String): ApiResponse<Boolean>

    // 检查身份证号（公开）
    @GET("api/user/checkIdNumber")
    suspend fun checkIdNumber(@Query("idNumber") idNumber: String): ApiResponse<Boolean>

    // 检查许可证号（公开）
    @GET("api/user/checkLicense")
    suspend fun checkLicense(@Query("license") license: String): ApiResponse<Boolean>

    // --- 管理员接口 ---

    @GET("api/user/list")
    suspend fun listUsers(
        @Query("permission") permission: Int,
        @Query("keyword") keyword: String? = null,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 10
    ): ApiResponse<Any>

    @PUT("api/user/reset-password/{userId}")
    suspend fun resetPassword(
        @Path("userId") userId: String,
        @Body body: Map<String, String>  // { "permission": "1", "newPassword": "xxx" }
    ): ApiResponse<Any>

    @PUT("api/user/admin-update/{userId}")
    suspend fun adminUpdateUser(
        @Path("userId") userId: String,
        @Body body: Map<String, Any>
    ): ApiResponse<Any>

    @DELETE("api/user/delete/{userId}")
    suspend fun deleteUser(
        @Path("userId") userId: String,
        @Query("permission") permission: Int
    ): ApiResponse<Any>
}
```

### 5.2 ActivityService

```kotlin
// ActivityService.kt
interface ActivityService {

    // 活动列表（公开）
    @GET("api/activity/list")
    suspend fun list(
        @Query("keyword") keyword: String? = null,
        @Query("region") region: String? = null,
        @Query("state") state: String? = null,
        @Query("auditState") auditState: String? = null,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 6
    ): ApiResponse<ActivityListResponse>

    // 活动详情（公开）
    @GET("api/activity/detail/{id}")
    suspend fun detail(@Path("id") id: String): ApiResponse<Activity>

    // 发布活动（需 Token，学校用户）
    @POST("api/activity/create")
    suspend fun create(@Body activity: Activity): ApiResponse<Any>

    // 修改活动（需 Token，仅发布者本人）
    @PUT("api/activity/update/{id}")
    suspend fun update(@Path("id") id: String, @Body activity: Activity): ApiResponse<Any>

    // 修改活动状态（需 Token，仅发布者本人）
    @PUT("api/activity/state/{id}")
    suspend fun changeState(
        @Path("id") id: String,
        @Body body: Map<String, String>  // { "activityState": "1" }
    ): ApiResponse<Any>

    // 删除活动（需 Token，发布者或管理员）
    @DELETE("api/activity/delete/{id}")
    suspend fun delete(@Path("id") id: String): ApiResponse<Any>

    // 审核活动（需 Token，仅管理员）
    @PUT("api/activity/review/{id}")
    suspend fun review(
        @Path("id") id: String,
        @Body body: Map<String, String>  // { "auditState": "1" }
    ): ApiResponse<Any>

    // 我的活动（需 Token，学校用户）
    @GET("api/activity/my")
    suspend fun myActivities(): ApiResponse<List<Activity>>

    // 提交活动总结（需 Token，仅发布者本人）
    @POST("api/activity/summary/{id}")
    suspend fun submitSummary(
        @Path("id") id: String,
        @Body body: Map<String, String>  // { "title": "...", "content": "..." }
    ): ApiResponse<Any>

    // 审核活动总结（需 Token，仅管理员）
    @PUT("api/activity/summary/review/{id}")
    suspend fun reviewSummary(
        @Path("id") id: String,
        @Body body: Map<String, String>  // { "auditState": "1" }
    ): ApiResponse<Any>

    // 已审核通过的活动（公开）
    @GET("api/activity/reviewed")
    suspend fun listReviewed(
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20
    ): ApiResponse<ActivityListResponse>

    // 已审核通过的活动总结（公开）
    @GET("api/activity/summary/reviewed")
    suspend fun listSummariesReviewed(
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20
    ): ApiResponse<Any>

    // 活动总结列表（管理员审核用）
    @GET("api/activity/summary/list")
    suspend fun listSummaries(
        @Query("auditState") auditState: String? = null,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 50
    ): ApiResponse<Any>
}
```

### 5.3 RegistrationService

```kotlin
// RegistrationService.kt
interface RegistrationService {

    // 提交报名（需 Token，志愿者）
    @POST("api/registration/submit")
    suspend fun submit(@Body registration: Registration): ApiResponse<Any>

    // 检查是否已报名（需 Token）
    @GET("api/registration/check/{activityId}")
    suspend fun check(@Path("activityId") activityId: String): ApiResponse<Boolean>

    // 我的报名列表（需 Token，志愿者）
    @GET("api/registration/my")
    suspend fun myRegistrations(): ApiResponse<List<Registration>>

    // 取消报名（需 Token，仅本人）
    @DELETE("api/registration/cancel/{id}")
    suspend fun cancel(@Path("id") registrationId: String): ApiResponse<Any>

    // 查看活动报名列表（需 Token，学校用户）
    @GET("api/registration/list/{activityId}")
    suspend fun listByActivity(@Path("activityId") activityId: String): ApiResponse<List<Registration>>

    // 所有报名列表（需 Token，管理员）
    @GET("api/registration/list/all")
    suspend fun listAll(
        @Query("auditState") auditState: String? = null,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 50
    ): ApiResponse<Any>

    // 审核报名（需 Token，学校用户）
    @PUT("api/registration/review/{id}")
    suspend fun review(
        @Path("id") registrationId: String,
        @Body body: Map<String, String>  // { "auditState": "1" }
    ): ApiResponse<Any>
}
```

### 5.4 PostService

```kotlin
// PostService.kt
interface PostService {

    // 帖子列表（公开）
    @GET("api/post/list")
    suspend fun list(
        @Query("activityId") activityId: String? = null,
        @Query("auditState") auditState: String? = null,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20
    ): ApiResponse<Any>

    // 帖子详情（公开）
    @GET("api/post/detail/{id}")
    suspend fun detail(@Path("id") postId: String): ApiResponse<Post>

    // 发布帖子（需 Token）
    @POST("api/post/create")
    suspend fun create(@Body post: Post): ApiResponse<Any>

    // 修改帖子（需 Token，仅发布者本人）
    @PUT("api/post/update/{id}")
    suspend fun update(@Path("id") postId: String, @Body post: Post): ApiResponse<Any>

    // 删除帖子（需 Token，发布者或管理员）
    @DELETE("api/post/delete/{id}")
    suspend fun delete(@Path("id") postId: String): ApiResponse<Any>

    // 审核帖子（需 Token，管理员）
    @PUT("api/post/review/{id}")
    suspend fun review(
        @Path("id") postId: String,
        @Body body: Map<String, String>  // { "auditState": "1" }
    ): ApiResponse<Any>

    // 我的帖子（需 Token）
    @GET("api/post/my")
    suspend fun myPosts(): ApiResponse<List<Post>>

    // 已审核通过的帖子（公开）
    @GET("api/post/reviewed")
    suspend fun listReviewed(
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20
    ): ApiResponse<Any>
}
```

### 5.5 CommentService

```kotlin
// CommentService.kt
interface CommentService {

    // 评论列表（公开）
    @GET("api/comment/list/{postId}")
    suspend fun listByPost(@Path("postId") postId: String): ApiResponse<List<Comment>>

    // 发表评论（需 Token）
    @POST("api/comment/create")
    suspend fun create(@Body comment: Comment): ApiResponse<Any>

    // 修改评论（需 Token，仅发布者本人）
    @PUT("api/comment/update/{id}")
    suspend fun update(
        @Path("id") commentId: String,
        @Body comment: Comment
    ): ApiResponse<Any>

    // 删除评论（需 Token，发布者或管理员）
    @DELETE("api/comment/delete/{id}")
    suspend fun delete(@Path("id") commentId: String): ApiResponse<Any>
}
```

---

## 六、调用示例（ViewModel 中）

### 6.1 登录

```kotlin
// LoginViewModel.kt
class LoginViewModel : ViewModel() {

    private val _loginResult = MutableLiveData<Result<LoginResponse>>()
    val loginResult: LiveData<Result<LoginResponse>> = _loginResult

    fun login(phone: String, password: String, role: Int) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.userService.login(
                    LoginRequest(phone, password, role)
                )
                if (response.code == 200 && response.data != null) {
                    TokenManager.saveToken(response.data.token)
                    _loginResult.value = Result.success(response.data)
                } else {
                    _loginResult.value = Result.failure(Exception(response.message ?: "登录失败"))
                }
            } catch (e: Exception) {
                _loginResult.value = Result.failure(e)
            }
        }
    }
}
```

### 6.2 获取活动列表

```kotlin
// ActivityListViewModel.kt
class ActivityListViewModel : ViewModel() {

    private val _activities = MutableLiveData<List<Activity>>()
    val activities: LiveData<List<Activity>> = _activities

    fun loadActivities(keyword: String? = null, state: String? = null) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.activityService.list(
                    keyword = keyword,
                    state = state,
                    page = 1,
                    pageSize = 20
                )
                if (response.code == 200 && response.data != null) {
                    _activities.value = response.data.list
                }
            } catch (e: Exception) {
                // 处理错误
            }
        }
    }
}
```

### 6.3 发布活动

```kotlin
fun publishActivity(title: String, content: String, recruits: Int, duration: Int, address: String) {
    viewModelScope.launch {
        try {
            val activity = Activity(
                title = title,
                content = content,
                recruitsNumber = recruits,
                volunteerDuration = duration,
                address = address
            )
            val response = RetrofitClient.activityService.create(activity)
            if (response.code == 200) {
                // 发布成功
            } else {
                // response.message
            }
        } catch (e: Exception) {
            // 网络错误
        }
    }
}
```

### 6.4 提交报名

```kotlin
fun submitRegistration(activityId: String, realName: String, phone: String,
                       idNumber: String, gender: String, degree: String, schoolWork: String) {
    viewModelScope.launch {
        try {
            val reg = Registration(
                activityId = activityId,
                realName = realName,
                phoneNumber = phone,
                idNumber = idNumber,
                gender = gender,
                degree = degree,
                schoolWork = schoolWork
            )
            val response = RetrofitClient.registrationService.submit(reg)
            if (response.code == 200) {
                // 报名成功
            } else {
                // response.message（如 "您已报名过该活动，不能重复报名"）
            }
        } catch (e: Exception) {
            // 网络错误
        }
    }
}
```

### 6.5 发帖

```kotlin
fun createPost(activityId: String, title: String, content: String) {
    viewModelScope.launch {
        try {
            val post = Post(activityId = activityId, title = title, content = content)
            val response = RetrofitClient.postService.create(post)
            if (response.code == 200) {
                // 发布成功，待管理员审核
            }
        } catch (e: Exception) {
            // 网络错误
        }
    }
}
```

### 6.6 发表评论

```kotlin
fun addComment(postId: String, content: String) {
    viewModelScope.launch {
        try {
            val comment = Comment(postId = postId, content = content)
            val response = RetrofitClient.commentService.create(comment)
            if (response.code == 200) {
                // 评论成功
            }
        } catch (e: Exception) {
            // 网络错误
        }
    }
}
```

### 6.7 审核操作通用示例

```kotlin
// 审核活动（管理员）
fun reviewActivity(activityId: String, approve: Boolean) {
    viewModelScope.launch {
        val state = if (approve) "1" else "2"
        RetrofitClient.activityService.review(
            activityId, mapOf("auditState" to state)
        )
    }
}

// 审核报名（学校用户）
fun reviewRegistration(regId: String, approve: Boolean) {
    viewModelScope.launch {
        val state = if (approve) "1" else "2"
        RetrofitClient.registrationService.review(
            regId, mapOf("auditState" to state)
        )
    }
}
```

---

## 七、接口权限速查表

| 角色 | 可调用接口 |
|------|-----------|
| **未登录** | 登录、注册、找回密码、活动列表/详情、帖子列表/详情、评论列表、字段唯一性校验 |
| **志愿者 (permission=1)** | 未登录接口 + 个人信息/改密、报名/取消报名/我的报名、发帖/我的帖子/删自己的帖子、发评论/删自己的评论 |
| **学校用户 (permission=2)** | 未登录接口 + 个人信息/改密、发布活动/我的活动/修改活动/提交总结、查看报名列表/审核报名、发帖/评论 |
| **管理员 (permission=3)** | 全部接口：审核活动、审核帖子、审核总结、管理用户、查看所有报名 |

---

## 八、常见坑点

### 8.1 状态值是字符串不是整数

```kotlin
// ❌ 错误
val state = 0

// ✅ 正确——所有状态字段都是 String
val auditState = "0"  // 待审核
val auditState = "1"  // 审核通过
val auditState = "2"  // 未通过

val activityState = "0"  // 招募中
val activityState = "1"  // 进行中
val activityState = "2"  // 已结束
```

### 8.2 HTTP 明文流量

后端是 HTTP 而非 HTTPS，Android 9+ 默认禁止明文流量，必须加 `android:usesCleartextTraffic="true"`，否则所有请求都会失败。

### 8.3 分页从 1 开始

所有 `page` 参数从 `1` 开始，不是 `0`。

### 8.4 Token 过期处理

Token 有效期 7 天，过期后接口返回 `{ code: 401, message: "登录已过期，请重新登录" }`。建议在拦截器或 BaseRepository 中统一捕获，清掉本地 Token 并跳转登录页。

### 8.5 register 接口参数不固定

志愿者和学校用户的 `register` 请求体字段完全不同，所以 Retrofit 里用了 `Map<String, String>` 而非固定 data class。调用时自己拼 Map：

```kotlin
// 志愿者
val body = mapOf(
    "phone" to phone,
    "password" to password,
    "role" to "1",
    "realName" to realName,
    "idNumber" to idNumber,
    "sex" to sex,
    "edu" to edu
)

// 学校用户
val body = mapOf(
    "phone" to phone,
    "password" to password,
    "role" to "2",
    "schoolName" to schoolName,
    "principle" to principle,
    "license" to license,
    "type" to type,
    "address" to address
)
```

### 8.6 模拟器访问云服务器

如果用 Android 模拟器，访问宿主机外的云服务器直接用公网 IP 即可。如果用实体机连同一局域网，也可用局域网地址。注意云服务器安全组需要放行 **8080 端口**。
