package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AdminVerifyActivity extends AppCompatActivity {

    private RecyclerView rv;
    private EditText etSearch;
    private Button tabUsers, tabActivities, tabPosts, tabSummaries;

    private AdminUserAdapter userAdapter;
    private AdminActivityVerifyAdapter activityAdapter;
    private AdminPostVerifyAdapter postAdapter;
    private AdminSummaryVerifyAdapter summaryAdapter;

    private List<User> userList = new ArrayList<>();
    private List<User> allUserList = new ArrayList<>();
    private List<Activity> activityList = new ArrayList<>();
    private List<Activity> allActivityList = new ArrayList<>();
    private List<Post> postList = new ArrayList<>();
    private List<Post> allPostList = new ArrayList<>();
    private List<Activity> summaryList = new ArrayList<>();
    private List<Activity> allSummaryList = new ArrayList<>();

    private final OkHttpClient client = ApiConfig.getClient();
    private final Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_verify);

        rv = findViewById(R.id.rv_pending_schools);
        rv.setLayoutManager(new LinearLayoutManager(this));
        etSearch = findViewById(R.id.et_admin_search);

        tabUsers = findViewById(R.id.btn_tab_users);
        tabActivities = findViewById(R.id.btn_tab_activities);
        tabPosts = findViewById(R.id.btn_tab_posts);
        tabSummaries = findViewById(R.id.btn_tab_summaries);

        userAdapter = new AdminUserAdapter(userList, new AdminUserAdapter.OnUserActionListener() {
            @Override
            public void onDelete(String userId, int position) { deleteUser(userId, position); }
            @Override
            public void onEdit(User user) {
                Intent intent = new Intent(AdminVerifyActivity.this, EditProfileActivity.class);
                intent.putExtra("admin_mode", true);
                intent.putExtra("target_user_id", user.getUserId());
                intent.putExtra("target_permission", user.getUserPermission());
                intent.putExtra("target_phone", user.getUserPhone());
                intent.putExtra("target_real_name", user.getRealName());
                intent.putExtra("target_identity", user.getUserIdentity());
                intent.putExtra("target_sex", user.getUserSex());
                intent.putExtra("target_edu", user.getUserEdu());
                intent.putExtra("target_school_name", user.getSchoolName());
                intent.putExtra("target_school_address", user.getSchoolAddress());
                intent.putExtra("target_type", user.getType());
                intent.putExtra("target_license", user.getLicense());
                intent.putExtra("target_principle", user.getPrinciple());
                startActivity(intent);
            }
        });

        activityAdapter = new AdminActivityVerifyAdapter(activityList, (activity, position) -> {
            Intent intent = new Intent(AdminVerifyActivity.this, AdminActivityDetailActivity.class);
            intent.putExtra("activity_data", activity);
            startActivity(intent);
        });

        postAdapter = new AdminPostVerifyAdapter(postList, (post, action, position) -> {
            verifyPostOnServer(post, action, position);
        }, (post, position) -> {
            deletePost(post, position);
        }, (post) -> {
            Intent intent = new Intent(AdminVerifyActivity.this, PostDetailActivity.class);
            intent.putExtra("post_data", post);
            startActivity(intent);
        });

        summaryAdapter = new AdminSummaryVerifyAdapter(summaryList, (activityId, action, position) -> {
            verifySummaryOnServer(activityId, action, position);
        });

        // 默认选中审核活动
        setTabActive(tabActivities, tabUsers, tabPosts, tabSummaries);
        rv.setAdapter(activityAdapter);
        loadPendingActivities();

        tabUsers.setOnClickListener(v -> {
            setTabActive(tabUsers, tabActivities, tabPosts, tabSummaries);
            rv.setAdapter(userAdapter);
            etSearch.setText("");
            etSearch.setHint("输入 UID 或手机号搜索用户");
            loadUserList();
        });

        tabActivities.setOnClickListener(v -> {
            setTabActive(tabActivities, tabUsers, tabPosts, tabSummaries);
            rv.setAdapter(activityAdapter);
            etSearch.setText("");
            etSearch.setHint("输入标题或学校ID搜索活动");
            loadPendingActivities();
        });

        tabPosts.setOnClickListener(v -> {
            setTabActive(tabPosts, tabUsers, tabActivities, tabSummaries);
            rv.setAdapter(postAdapter);
            etSearch.setText("");
            etSearch.setHint("输入标题搜索帖子");
            loadPendingPosts();
        });

        tabSummaries.setOnClickListener(v -> {
            setTabActive(tabSummaries, tabUsers, tabActivities, tabPosts);
            rv.setAdapter(summaryAdapter);
            etSearch.setText("");
            etSearch.setHint("输入活动标题搜索总结");
            loadPendingSummaries();
        });

        // 搜索功能：支持全部四个 Tab 实时筛选
        etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(android.text.Editable s) {
                String keyword = s.toString().trim();
                if (rv.getAdapter() == userAdapter) {
                    filterUsers(keyword);
                } else if (rv.getAdapter() == activityAdapter) {
                    filterActivities(keyword);
                } else if (rv.getAdapter() == postAdapter) {
                    filterPosts(keyword);
                } else if (rv.getAdapter() == summaryAdapter) {
                    filterSummaries(keyword);
                }
            }
        });

        findViewById(R.id.btn_admin_logout).setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
    }

    private String extractListJson(String jsonResult) {
        try {
            String trimmed = jsonResult.trim();
            if (trimmed.startsWith("[")) return trimmed;
            JSONObject obj = new JSONObject(trimmed);
            if (obj.optInt("code") == 200) {
                Object data = obj.get("data");
                if (data instanceof JSONObject) {
                    JSONObject dataObj = (JSONObject) data;
                    if (dataObj.has("list")) return dataObj.getJSONArray("list").toString();
                } else if (data instanceof org.json.JSONArray) {
                    return data.toString();
                }
            }
        } catch (Exception e) {
            Log.e("AdminVerify", "Extract list error", e);
        }
        return null;
    }

    private void setTabActive(Button active, Button inactive1, Button inactive2, Button inactive3) {
        active.setTextColor(ContextCompat.getColor(this, R.color.primary_blue));
        inactive1.setTextColor(ContextCompat.getColor(this, R.color.text_main));
        inactive2.setTextColor(ContextCompat.getColor(this, R.color.text_main));
        inactive3.setTextColor(ContextCompat.getColor(this, R.color.text_main));
    }

    // ===================== 用户管理 =====================

    private void loadUserList() {
        // 三次请求获取全部三种角色的用户
        allUserList.clear();
        userList.clear();
        fetchUsersByPermission(1);
        fetchUsersByPermission(2);
        fetchUsersByPermission(3);
    }

    private void fetchUsersByPermission(int permission) {
        String url = ApiConfig.getBaseUrl() + "/user/list?permission=" + permission + "&pageSize=100";
        Request request = new Request.Builder().url(url).get().build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {}

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String jsonResult = response.body().string();
                        String listJson = extractListJson(jsonResult);
                        if (listJson != null) {
                            List<User> serverData = gson.fromJson(listJson, new TypeToken<List<User>>(){}.getType());
                            runOnUiThread(() -> {
                                allUserList.addAll(serverData);
                                userList.addAll(serverData);
                                userAdapter.notifyDataSetChanged();
                            });
                        }
                    } catch (Exception e) {
                        Log.e("AdminVerify", "Fetch users error", e);
                    }
                }
            }
        });
    }

    private void filterUsers(String keyword) {
        userList.clear();
        if (TextUtils.isEmpty(keyword)) {
            userList.addAll(allUserList);
        } else {
            for (User u : allUserList) {
                if ((u.getUserId() != null && u.getUserId().contains(keyword))
                        || (u.getUserPhone() != null && u.getUserPhone().contains(keyword))) {
                    userList.add(u);
                }
            }
        }
        userAdapter.updateData(userList);
    }

    private void filterActivities(String keyword) {
        activityList.clear();
        if (TextUtils.isEmpty(keyword)) {
            activityList.addAll(allActivityList);
        } else {
            for (Activity a : allActivityList) {
                if ((a.getTitle() != null && a.getTitle().contains(keyword))
                        || (a.getUserId() != null && a.getUserId().contains(keyword))) {
                    activityList.add(a);
                }
            }
        }
        activityAdapter.notifyDataSetChanged();
    }

    private void filterPosts(String keyword) {
        postList.clear();
        if (TextUtils.isEmpty(keyword)) {
            postList.addAll(allPostList);
        } else {
            for (Post p : allPostList) {
                if (p.getTitle() != null && p.getTitle().contains(keyword)) {
                    postList.add(p);
                }
            }
        }
        postAdapter.notifyDataSetChanged();
    }

    private void filterSummaries(String keyword) {
        summaryList.clear();
        if (TextUtils.isEmpty(keyword)) {
            summaryList.addAll(allSummaryList);
        } else {
            for (Activity a : allSummaryList) {
                if (a.getTitle() != null && a.getTitle().contains(keyword)) {
                    summaryList.add(a);
                }
            }
        }
        summaryAdapter.notifyDataSetChanged();
    }

    private void deleteUser(String userId, int position) {
        String permission = "";
        for (User u : allUserList) {
            if (userId.equals(u.getUserId())) {
                permission = u.getUserPermission() != null ? u.getUserPermission() : "";
                break;
            }
        }
        String url = ApiConfig.getBaseUrl() + "/user/delete/" + userId + "?permission=" + permission;

        Request request = new Request.Builder().url(url).delete().build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(AdminVerifyActivity.this, "网络请求失败", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) {
                String result = null;
                try {
                    if (response.body() != null) {
                        result = response.body().string();
                    }
                } catch (IOException e) {
                    Log.e("AdminVerify", "Failed to read body", e);
                }
                final String finalResult = result;
                runOnUiThread(() -> {
                    if (finalResult != null) {
                        try {
                            JSONObject res = new JSONObject(finalResult);
                            if (res.has("code") && res.getInt("code") == 200) {
                                Toast.makeText(AdminVerifyActivity.this, "用户已删除", Toast.LENGTH_SHORT).show();
                                userAdapter.removeItem(position);
                                if (position < allUserList.size()) {
                                    allUserList.remove(position);
                                }
                                return;
                            }
                        } catch (Exception e) {
                            Log.e("AdminVerify", "Parse error", e);
                        }
                    }
                    Toast.makeText(AdminVerifyActivity.this, "删除失败", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 从详情页返回后刷新审核活动列表
        if (rv.getAdapter() == activityAdapter) {
            loadPendingActivities();
        }
    }

    // ===================== 审核活动 =====================

    private void loadPendingActivities() {
        allActivityList.clear();
        final java.util.concurrent.atomic.AtomicInteger pending = new java.util.concurrent.atomic.AtomicInteger(3);
        fetchActivitiesByState("0", pending);
        fetchActivitiesByState("1", pending);
        fetchActivitiesByState("2", pending);
    }

    private void fetchActivitiesByState(String state, java.util.concurrent.atomic.AtomicInteger pending) {
        String url = ApiConfig.getBaseUrl() + "/activity/list?auditState=" + state;
        Request request = new Request.Builder().url(url).get().build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (pending.decrementAndGet() == 0) {
                    runOnUiThread(() -> {
                        activityList.clear();
                        activityList.addAll(allActivityList);
                        activityAdapter.notifyDataSetChanged();
                    });
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String jsonResult = response.body().string();
                        String listJson = extractListJson(jsonResult);
                        if (listJson != null) {
                            List<Activity> serverData = gson.fromJson(listJson, new TypeToken<List<Activity>>(){}.getType());
                            synchronized (allActivityList) {
                                allActivityList.addAll(serverData);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (pending.decrementAndGet() == 0) {
                    runOnUiThread(() -> {
                        activityList.clear();
                        activityList.addAll(allActivityList);
                        activityAdapter.notifyDataSetChanged();
                    });
                }
            }
        });
    }

    private void verifyActivityOnServer(String activityId, String action, int position) {
        String url = ApiConfig.getBaseUrl() + "/activity/review/" + activityId;
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("auditState", "通过".equals(action) ? "1" : "2");
        } catch (Exception e) { e.printStackTrace(); }

        RequestBody body = RequestBody.create(ApiConfig.JSON, jsonObject.toString());
        Request request = new Request.Builder().url(url).put(body).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(AdminVerifyActivity.this, "网络请求失败", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) {
                String result = null;
                try {
                    if (response.body() != null) {
                        result = response.body().string();
                    }
                } catch (IOException e) {
                    Log.e("AdminVerify", "Failed to read body", e);
                }
                final String finalResult = result;
                runOnUiThread(() -> {
                    if (finalResult == null) {
                        Toast.makeText(AdminVerifyActivity.this, "服务器返回异常", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    try {
                        JSONObject res = new JSONObject(finalResult);
                        int code = res.has("code") ? res.getInt("code") : 500;
                        String msg = res.optString("message", action + " 成功");
                        Toast.makeText(AdminVerifyActivity.this, ApiConfig.friendlyMsg(msg), Toast.LENGTH_SHORT).show();
                        if (code == 200) {
                            activityAdapter.removeItem(position);
                        }
                    } catch (Exception e) {
                        Log.e("AdminVerify", "Parse error", e);
                        Toast.makeText(AdminVerifyActivity.this, "操作失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    // ===================== 审核帖子 =====================

    private void loadPendingPosts() {
        allPostList.clear();
        final java.util.concurrent.atomic.AtomicInteger pending = new java.util.concurrent.atomic.AtomicInteger(3);
        fetchPostsByState("0", pending);
        fetchPostsByState("1", pending);
        fetchPostsByState("2", pending);
    }

    private void fetchPostsByState(String state, java.util.concurrent.atomic.AtomicInteger pending) {
        String url = ApiConfig.getBaseUrl() + "/post/list?auditState=" + state;
        Request request = new Request.Builder().url(url).get().build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (pending.decrementAndGet() == 0) {
                    runOnUiThread(() -> {
                        postList.clear();
                        postList.addAll(allPostList);
                        postAdapter.notifyDataSetChanged();
                    });
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String jsonResult = response.body().string();
                        String listJson = extractListJson(jsonResult);
                        if (listJson != null) {
                            List<Post> serverData = gson.fromJson(listJson, new TypeToken<List<Post>>(){}.getType());
                            synchronized (allPostList) {
                                allPostList.addAll(serverData);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (pending.decrementAndGet() == 0) {
                    runOnUiThread(() -> {
                        postList.clear();
                        postList.addAll(allPostList);
                        postAdapter.notifyDataSetChanged();
                    });
                }
            }
        });
    }

    private void verifyPostOnServer(Post post, String action, int position) {
        String url = ApiConfig.getBaseUrl() + "/post/review/" + post.getPostId();
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("auditState", "通过".equals(action) ? "1" : "2");
        } catch (Exception e) { e.printStackTrace(); }

        RequestBody body = RequestBody.create(ApiConfig.JSON, jsonObject.toString());
        Request request = new Request.Builder().url(url).put(body).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(AdminVerifyActivity.this, "网络请求失败", Toast.LENGTH_SHORT).show();
                    postAdapter.notifyItemChanged(position);
                });
            }

            @Override
            public void onResponse(Call call, Response response) {
                String result = null;
                try {
                    if (response.body() != null) {
                        result = response.body().string();
                    }
                } catch (IOException e) {
                    Log.e("AdminVerify", "Failed to read body", e);
                }
                final String finalResult = result;
                runOnUiThread(() -> {
                    if (finalResult == null) {
                        Toast.makeText(AdminVerifyActivity.this, "服务器返回异常", Toast.LENGTH_SHORT).show();
                        postAdapter.notifyItemChanged(position);
                        return;
                    }
                    try {
                        JSONObject res = new JSONObject(finalResult);
                        int code = res.has("code") ? res.getInt("code") : 500;
                        String msg = res.optString("message", action + " 成功");
                        Toast.makeText(AdminVerifyActivity.this, ApiConfig.friendlyMsg(msg), Toast.LENGTH_SHORT).show();
                        if (code == 200) {
                            postAdapter.removeItem(post);
                        } else {
                            postAdapter.notifyItemChanged(position);
                        }
                    } catch (Exception e) {
                        Log.e("AdminVerify", "Parse error", e);
                        Toast.makeText(AdminVerifyActivity.this, "操作失败", Toast.LENGTH_SHORT).show();
                        postAdapter.notifyItemChanged(position);
                    }
                });
            }
        });
    }

    private void deletePost(Post post, int position) {
        String url = ApiConfig.getBaseUrl() + "/post/delete/" + post.getPostId();

        Request request = new Request.Builder().url(url).delete().build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(AdminVerifyActivity.this, "网络请求失败", Toast.LENGTH_SHORT).show();
                    postAdapter.notifyItemChanged(position);
                });
            }

            @Override
            public void onResponse(Call call, Response response) {
                String result = null;
                try {
                    if (response.body() != null) {
                        result = response.body().string();
                    }
                } catch (IOException e) {
                    Log.e("AdminVerify", "Failed to read body", e);
                }
                final String finalResult = result;
                runOnUiThread(() -> {
                    if (finalResult == null) {
                        Toast.makeText(AdminVerifyActivity.this, "服务器返回异常", Toast.LENGTH_SHORT).show();
                        postAdapter.notifyItemChanged(position);
                        return;
                    }
                    try {
                        JSONObject res = new JSONObject(finalResult);
                        int code = res.has("code") ? res.getInt("code") : 500;
                        String msg = res.optString("message", "删除成功");
                        Toast.makeText(AdminVerifyActivity.this, ApiConfig.friendlyMsg(msg), Toast.LENGTH_SHORT).show();
                        if (code == 200) {
                            postAdapter.removeItem(position);
                            // 同步更新全量列表
                            allPostList.remove(post);
                        } else {
                            postAdapter.notifyItemChanged(position);
                        }
                    } catch (Exception e) {
                        Log.e("AdminVerify", "Parse error", e);
                        Toast.makeText(AdminVerifyActivity.this, "删除失败", Toast.LENGTH_SHORT).show();
                        postAdapter.notifyItemChanged(position);
                    }
                });
            }
        });
    }

    // ===================== 审核总结报告 =====================

    private void loadPendingSummaries() {
        String url = ApiConfig.getBaseUrl() + "/activity/list";
        Request request = new Request.Builder().url(url).get().build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                    Toast.makeText(AdminVerifyActivity.this, "加载总结列表失败", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    runOnUiThread(() ->
                        Toast.makeText(AdminVerifyActivity.this, "服务器错误: " + response.code(), Toast.LENGTH_SHORT).show());
                    return;
                }
                try {
                    String jsonResult = response.body().string();
                    String listJson = extractListJson(jsonResult);
                    if (listJson != null) {
                        List<Activity> serverData = gson.fromJson(listJson, new TypeToken<List<Activity>>(){}.getType());
                        List<Activity> withSummary = new ArrayList<>();
                        for (Activity a : serverData) {
                            if (a.getSummaryContent() != null && !a.getSummaryContent().isEmpty()) {
                                withSummary.add(a);
                            }
                        }
                        runOnUiThread(() -> {
                            allSummaryList.clear();
                            allSummaryList.addAll(withSummary);
                            summaryList.clear();
                            summaryList.addAll(withSummary);
                            summaryAdapter.notifyDataSetChanged();
                        });
                    }
                } catch (Exception e) {
                    Log.e("AdminVerify", "Parse summary list error", e);
                    runOnUiThread(() ->
                        Toast.makeText(AdminVerifyActivity.this, "数据解析失败", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void verifySummaryOnServer(String activityId, String action, int position) {
        String url = ApiConfig.getBaseUrl() + "/activity/summary/review/" + activityId;
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("auditState", "通过".equals(action) ? "1" : "2");
        } catch (Exception e) { e.printStackTrace(); }

        RequestBody body = RequestBody.create(ApiConfig.JSON, jsonObject.toString());
        Request request = new Request.Builder().url(url).put(body).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(AdminVerifyActivity.this, "网络请求失败", Toast.LENGTH_SHORT).show();
                    summaryAdapter.notifyItemChanged(position);
                });
            }

            @Override
            public void onResponse(Call call, Response response) {
                String result = null;
                try {
                    if (response.body() != null) {
                        result = response.body().string();
                    }
                } catch (IOException e) {
                    Log.e("AdminVerify", "Failed to read body", e);
                }
                final String finalResult = result;
                runOnUiThread(() -> {
                    if (finalResult == null) {
                        Toast.makeText(AdminVerifyActivity.this, "服务器返回异常", Toast.LENGTH_SHORT).show();
                        summaryAdapter.notifyItemChanged(position);
                        return;
                    }
                    try {
                        JSONObject res = new JSONObject(finalResult);
                        int code = res.has("code") ? res.getInt("code") : 500;
                        String msg = res.optString("message", action + " 成功");
                        Toast.makeText(AdminVerifyActivity.this, ApiConfig.friendlyMsg(msg), Toast.LENGTH_SHORT).show();
                        if (code == 200) {
                            // 乐观更新本地数据：action 是 "通过"/"未通过"，映射为数字码 "1"/"2"
                            String numericState = "通过".equals(action) ? "1" : "2";
                            if (position >= 0 && position < summaryList.size()) {
                                Activity item = summaryList.get(position);
                                if (item != null) item.setSummaryAuditState(numericState);
                            }
                            for (Activity a : allSummaryList) {
                                if (a.getActivityId().equals(activityId)) {
                                    a.setSummaryAuditState(numericState);
                                    break;
                                }
                            }
                            summaryAdapter.notifyItemChanged(position);
                        } else {
                            summaryAdapter.notifyItemChanged(position);
                        }
                    } catch (Exception e) {
                        Log.e("AdminVerify", "Parse error", e);
                        Toast.makeText(AdminVerifyActivity.this, "操作失败", Toast.LENGTH_SHORT).show();
                        summaryAdapter.notifyItemChanged(position);
                    }
                });
            }
        });
    }
}
