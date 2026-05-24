package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AdminUserListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AdminUserAdapter adapter;
    private List<User> userList = new ArrayList<>();

    private final OkHttpClient client = ApiConfig.getClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_user_list);

        recyclerView = findViewById(R.id.rv_admin_users);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new AdminUserAdapter(userList, new AdminUserAdapter.OnUserActionListener() {
            @Override
            public void onDelete(String userId, int position) {
                new AlertDialog.Builder(AdminUserListActivity.this)
                        .setTitle("删除用户")
                        .setMessage("确定要删除用户「" + userId + "」吗？")
                        .setPositiveButton("确认删除", (dialog, which) ->
                            deleteUser(userId, position))
                        .setNegativeButton("取消", null)
                        .show();
            }

            @Override
            public void onEdit(User user) {
                Intent intent = new Intent(AdminUserListActivity.this, EditProfileActivity.class);
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
        recyclerView.setAdapter(adapter);

        TextView btnBack = findViewById(R.id.btn_back_to_profile);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        loadUserList();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 从编辑页返回后刷新列表
        loadUserList();
    }

    private void loadUserList() {
        String url = ApiConfig.getBaseUrl() + "/user/list?permission=&keyword=&page=1&pageSize=50";

        Request request = new Request.Builder().url(url).get().build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("AdminUserList", "Load users failed", e);
                runOnUiThread(() ->
                    Toast.makeText(AdminUserListActivity.this, "网络连接失败", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    runOnUiThread(() ->
                        Toast.makeText(AdminUserListActivity.this, "获取用户列表失败", Toast.LENGTH_SHORT).show());
                    return;
                }
                try {
                    String body = response.body() != null ? response.body().string() : null;
                    if (body == null) return;
                    JSONObject jsonObject = new JSONObject(body);
                    int code = jsonObject.getInt("code");
                    if (code == 200) {
                        JSONArray dataArray = jsonObject.getJSONArray("data");
                        List<User> serverData = new ArrayList<>();
                        for (int i = 0; i < dataArray.length(); i++) {
                            JSONObject userObj = dataArray.getJSONObject(i);
                            User user = new User();
                            user.setUserId(userObj.optString("userId", ""));
                            user.setUserPhone(userObj.optString("userPhone", ""));
                            user.setUserPermission(userObj.optString("userPermission", "3"));
                            user.setUserIdentity(userObj.optString("userIdentity", ""));
                            user.setUserSex(userObj.optString("userSex", ""));
                            user.setUserEdu(userObj.optString("userEdu", ""));
                            user.setRealName(userObj.optString("realName", ""));
                            user.setSchoolName(userObj.optString("schoolName", ""));
                            user.setSchoolAddress(userObj.optString("schoolAddress", ""));
                            user.setType(userObj.optString("type", ""));
                            user.setLicense(userObj.optString("license", ""));
                            user.setPrinciple(userObj.optString("principle", ""));
                            serverData.add(user);
                        }
                        runOnUiThread(() -> {
                            userList.clear();
                            userList.addAll(serverData);
                            adapter.notifyDataSetChanged();
                        });
                    }
                } catch (Exception e) {
                    Log.e("AdminUserList", "Parse error", e);
                    runOnUiThread(() ->
                        Toast.makeText(AdminUserListActivity.this, "解析数据异常", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void deleteUser(String userId, int position) {
        String url = ApiConfig.getBaseUrl() + "/user/delete/" + userId + "?permission=";
        Request request = new Request.Builder().url(url).delete().build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                    Toast.makeText(AdminUserListActivity.this, "网络连接失败", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = null;
                try {
                    if (response.body() != null) {
                        result = response.body().string();
                    }
                } catch (IOException e) {
                    Log.e("AdminUserList", "Failed to read body", e);
                }
                final String finalResult = result;
                try {
                    if (finalResult != null) {
                        JSONObject res = new JSONObject(finalResult);
                        int code = res.has("code") ? res.getInt("code") : 500;
                        String msg = res.optString("message", "删除成功");
                        runOnUiThread(() -> {
                            Toast.makeText(AdminUserListActivity.this, ApiConfig.friendlyMsg(msg), Toast.LENGTH_SHORT).show();
                            if (code == 200) {
                                adapter.removeItem(position);
                            }
                        });
                    }
                } catch (Exception e) {
                    Log.e("AdminUserList", "Parse error", e);
                    runOnUiThread(() ->
                        Toast.makeText(AdminUserListActivity.this, "操作失败", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }
}

