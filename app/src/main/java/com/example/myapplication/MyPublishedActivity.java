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

public class MyPublishedActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MyPublishedAdapter adapter;
    private List<TeachingActivity> dataList = new ArrayList<>();

    private final OkHttpClient client = ApiConfig.getClient();
    private final Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_published);

        TextView btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.rv_my_published);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new MyPublishedAdapter(dataList);
        adapter.setOnActionListener(new MyPublishedAdapter.OnActionListener() {
            @Override
            public void onEdit(TeachingActivity activity, int position) {
                if (!"待审核".equals(activity.getAuditState())) {
                    Toast.makeText(MyPublishedActivity.this, "仅待审核的活动可以编辑", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(MyPublishedActivity.this, EditActivityActivity.class);
                intent.putExtra("activity_data", activity);
                startActivity(intent);
            }

            @Override
            public void onDelete(TeachingActivity activity, int position) {
                new AlertDialog.Builder(MyPublishedActivity.this)
                        .setTitle("确认删除")
                        .setMessage("确定要删除「" + activity.getTitle() + "」吗？\n该活动的所有报名信息也将被删除。")
                        .setPositiveButton("确认删除", (dialog, which) -> deleteActivity(activity, position))
                        .setNegativeButton("取消", null)
                        .show();
            }

            @Override
            public void onSummary(TeachingActivity activity, int position) {
                Intent intent = new Intent(MyPublishedActivity.this, WriteSummaryActivity.class);
                intent.putExtra("activity_data", activity);
                startActivity(intent);
            }

            @Override
            public void onView(TeachingActivity activity, int position) {
                Intent intent = new Intent(MyPublishedActivity.this, DetailActivity.class);
                intent.putExtra("activity_data", activity);
                startActivity(intent);
            }

            @Override
            public void onStart(TeachingActivity activity, int position) {
                new AlertDialog.Builder(MyPublishedActivity.this)
                        .setTitle("确认开始活动")
                        .setMessage("确定要将「" + activity.getTitle() + "」标记为进行中吗？")
                        .setPositiveButton("确认", (dialog, which) -> setActivityInProgress(activity, position))
                        .setNegativeButton("取消", null)
                        .show();
            }
        });
        recyclerView.setAdapter(adapter);

        fetchMyPublished();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchMyPublished();
    }

    private void fetchMyPublished() {
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser == null || currentUser.getUserId() == null) {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            return;
        }

        final String userId = currentUser.getUserId();
        final List<TeachingActivity> allActivities = new ArrayList<>();
        final java.util.concurrent.atomic.AtomicInteger pending = new java.util.concurrent.atomic.AtomicInteger(3);

        for (String state : new String[]{"0", "1", "2"}) {
            String url = ApiConfig.getBaseUrl() + "/activity/list?auditState=" + state;
            Request request = new Request.Builder().url(url).get().build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    if (pending.decrementAndGet() == 0) {
                        updateMyPublishedList(allActivities, userId);
                    }
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        try {
                            String jsonResult = response.body().string();
                            JSONObject jsonObject = new JSONObject(jsonResult);
                            if (jsonObject.getInt("code") == 200) {
                                String dataArrayString = jsonObject.getJSONObject("data").getJSONArray("list").toString();
                                List<TeachingActivity> serverData = gson.fromJson(dataArrayString,
                                        new TypeToken<List<TeachingActivity>>(){}.getType());
                                synchronized (allActivities) {
                                    allActivities.addAll(serverData);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if (pending.decrementAndGet() == 0) {
                        updateMyPublishedList(allActivities, userId);
                    }
                }
            });
        }
    }

    private void updateMyPublishedList(List<TeachingActivity> allActivities, String userId) {
        List<TeachingActivity> myActivities = new ArrayList<>();
        synchronized (allActivities) {
            for (TeachingActivity a : allActivities) {
                if (userId.equals(a.getUserId())) {
                    myActivities.add(a);
                }
            }
        }
        runOnUiThread(() -> {
            dataList.clear();
            dataList.addAll(myActivities);
            adapter.notifyDataSetChanged();
        });
    }

    private void setActivityInProgress(TeachingActivity activity, int position) {
        String url = ApiConfig.getBaseUrl() + "/activity/state/" + activity.getActivityId();

        JSONObject json = new JSONObject();
        try {
            json.put("activityState", "1");
        } catch (Exception e) {
            e.printStackTrace();
        }

        okhttp3.RequestBody body = okhttp3.RequestBody.create(ApiConfig.JSON, json.toString());
        Request request = new Request.Builder().url(url).put(body).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                    Toast.makeText(MyPublishedActivity.this, "网络连接失败", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                Log.d("MyPublished", "START HTTP " + response.code() + " | " + result);
                try {
                    JSONObject res = new JSONObject(result);
                    int code = res.getInt("code");
                    String msg = res.optString("message", "活动已开始");
                    runOnUiThread(() -> {
                        Toast.makeText(MyPublishedActivity.this, ApiConfig.friendlyMsg(msg), Toast.LENGTH_SHORT).show();
                        if (code == 200) {
                            fetchMyPublished();
                        }
                    });
                } catch (Exception e) {
                    Log.e("MyPublished", "Start parse error", e);
                    runOnUiThread(() ->
                        Toast.makeText(MyPublishedActivity.this, "操作失败", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void deleteActivity(TeachingActivity activity, int position) {
        String url = ApiConfig.getBaseUrl() + "/activity/delete/" + activity.getActivityId();

        Request request = new Request.Builder().url(url).delete().build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                    Toast.makeText(MyPublishedActivity.this, "网络连接失败", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                Log.d("MyPublished", "DELETE HTTP " + response.code() + " | " + result);
                try {
                    JSONObject res = new JSONObject(result);
                    int code = res.getInt("code");
                    String msg = res.optString("message", "删除成功");
                    runOnUiThread(() -> {
                        Toast.makeText(MyPublishedActivity.this, ApiConfig.friendlyMsg(msg), Toast.LENGTH_SHORT).show();
                        if (code == 200) {
                            adapter.removeItem(position);
                        }
                    });
                } catch (Exception e) {
                    Log.e("MyPublished", "删除解析失败", e);
                    runOnUiThread(() ->
                        Toast.makeText(MyPublishedActivity.this, "删除失败", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }
}
