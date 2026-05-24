package com.example.myapplication;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
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
import okhttp3.Response;

public class ManageActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private TextView tvTitle;
    private TextView btnBack;

    private ActivityAdapter activityAdapter;
    private ManageAdapter manageAdapter;
    private List<Activity> activityList = new ArrayList<>();
    private List<Registration> registrationList = new ArrayList<>();

    private final OkHttpClient client = ApiConfig.getClient();
    private final Gson gson = new Gson();

    private boolean isShowingRegistrations = false;
    private Activity selectedActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage);

        tvTitle = findViewById(R.id.tv_manage_title);
        btnBack = findViewById(R.id.btn_back_to_home);
        recyclerView = findViewById(R.id.rv_manage_applies);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        activityAdapter = new ActivityAdapter(activityList);
        activityAdapter.setOnItemClickListener((activity, position) -> {
            selectedActivity = activity;
            loadRegistrationsForActivity(activity.getActivityId());
        });

        manageAdapter = new ManageAdapter(registrationList);

        btnBack.setOnClickListener(v -> {
            if (isShowingRegistrations) {
                switchToActivityList();
            } else {
                finish();
            }
        });

        loadMyActivities();
    }

    private void loadMyActivities() {
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser == null || currentUser.getUserId() == null) {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = ApiConfig.getBaseUrl() + "/activity/list";
        Request request = new Request.Builder().url(url).get().build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(ManageActivity.this, "网络请求失败", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String jsonResult = response.body().string();
                        JSONObject jsonObject = new JSONObject(jsonResult);
                        if (jsonObject.getInt("code") == 200) {
                            String dataArrayString = jsonObject.getJSONObject("data").getJSONArray("list").toString();
                            List<Activity> serverData = gson.fromJson(dataArrayString,
                                    new TypeToken<List<Activity>>(){}.getType());
                            // 过滤出当前用户发布的活动
                            List<Activity> myActivities = new ArrayList<>();
                            for (Activity a : serverData) {
                                if (currentUser.getUserId().equals(a.getUserId())) {
                                    myActivities.add(a);
                                }
                            }
                            runOnUiThread(() -> {
                                activityList.clear();
                                activityList.addAll(myActivities);
                                activityAdapter.notifyDataSetChanged();
                                switchToActivityList();
                            });
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void loadRegistrationsForActivity(String activityId) {
        String url = ApiConfig.getBaseUrl() + "/registration/list/" + activityId;
        Request request = new Request.Builder().url(url).get().build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(ManageActivity.this, "加载报名列表失败", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String jsonResult = response.body().string();
                        String dataArrayString;
                        try {
                            new org.json.JSONArray(jsonResult.trim());
                            dataArrayString = jsonResult;
                        } catch (org.json.JSONException e) {
                            JSONObject jsonObject = new JSONObject(jsonResult);
                            if (jsonObject.getInt("code") != 200) return;
                            try {
                                dataArrayString = jsonObject.getJSONObject("data").getJSONArray("list").toString();
                            } catch (org.json.JSONException e2) {
                                dataArrayString = jsonObject.getJSONArray("data").toString();
                            }
                        }
                        List<Registration> serverData = gson.fromJson(dataArrayString,
                                new TypeToken<List<Registration>>(){}.getType());
                        runOnUiThread(() -> {
                            registrationList.clear();
                            registrationList.addAll(serverData);
                            manageAdapter.notifyDataSetChanged();
                            switchToRegistrationList();
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(() -> Toast.makeText(ManageActivity.this, "数据解析失败", Toast.LENGTH_SHORT).show());
                    }
                }
            }
        });
    }

    private void switchToActivityList() {
        isShowingRegistrations = false;
        tvTitle.setText("我发布的活动");
        btnBack.setText("‹ 首页");
        recyclerView.setAdapter(activityAdapter);
    }

    private void switchToRegistrationList() {
        isShowingRegistrations = true;
        tvTitle.setText("审核报名申请");
        btnBack.setText("‹ 返回");
        recyclerView.setAdapter(manageAdapter);
    }
}
