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

public class MyRegistrationsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecordAdapter adapter;
    private List<RegistrationRecord> dataList = new ArrayList<>();

    private final OkHttpClient client = ApiConfig.getClient();
    private final Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_registrations);

        recyclerView = findViewById(R.id.rv_my_registrations);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new RecordAdapter(dataList);
        adapter.setOnCancelListener((record, position) -> {
            new AlertDialog.Builder(MyRegistrationsActivity.this)
                    .setTitle("取消报名")
                    .setMessage("确定要取消「" + record.getActivityTitle() + "」的报名吗？")
                    .setPositiveButton("确认取消", (dialog, which) ->
                        cancelRegistration(record))
                    .setNegativeButton("返回", (dialog, which) -> {
                        // 用户取消后恢复按钮
                        adapter.notifyItemChanged(position);
                    })
                    .show();
        });
        recyclerView.setAdapter(adapter);

        TextView btnBackHome = findViewById(R.id.btn_back_to_home);
        if (btnBackHome != null) {
            btnBackHome.setOnClickListener(v -> finish());
        }

        fetchMyRecords();
    }

    private void fetchMyRecords() {
        String url = ApiConfig.getBaseUrl() + "/registration/my";

        Request request = new Request.Builder().url(url).get().build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                    Toast.makeText(MyRegistrationsActivity.this, "网络连接失败", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String body = response.body() != null ? response.body().string() : null;
                        if (body == null) return;
                        JSONObject jsonObject = new JSONObject(body);
                        int code = jsonObject.getInt("code");
                        if (code == 200) {
                            Object dataField = jsonObject.get("data");
                            String dataJson;
                            if (dataField instanceof JSONObject) {
                                dataJson = ((JSONObject) dataField).getJSONArray("list").toString();
                            } else {
                                dataJson = dataField.toString();
                            }
                            List<RegistrationRecord> serverData = gson.fromJson(dataJson,
                                    new TypeToken<List<RegistrationRecord>>(){}.getType());
                            runOnUiThread(() -> {
                                dataList.clear();
                                dataList.addAll(serverData);
                                adapter.notifyDataSetChanged();
                            });
                        }
                    } catch (Exception e) {
                        Log.e("MyRegistrations", "Parse error", e);
                        runOnUiThread(() ->
                            Toast.makeText(MyRegistrationsActivity.this, "解析数据异常", Toast.LENGTH_SHORT).show());
                    }
                }
            }
        });
    }

    private void cancelRegistration(RegistrationRecord record) {
        String url = ApiConfig.getBaseUrl() + "/registration/cancel/" + record.getRegistrationId();

        Request request = new Request.Builder().url(url).delete().build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(MyRegistrationsActivity.this, "网络连接失败", Toast.LENGTH_SHORT).show();
                    adapter.notifyDataSetChanged();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = null;
                try {
                    if (response.body() != null) {
                        result = response.body().string();
                    }
                } catch (IOException e) {
                    Log.e("MyRegistrations", "Failed to read body", e);
                }
                Log.d("MyRegistrations", "Cancel HTTP " + response.code() + " | " + result);
                final String finalResult = result;
                try {
                    if (finalResult != null) {
                        JSONObject res = new JSONObject(finalResult);
                        int code = res.has("code") ? res.getInt("code") : 500;
                        String msg = res.optString("message", "已取消");
                        runOnUiThread(() -> {
                            Toast.makeText(MyRegistrationsActivity.this, ApiConfig.friendlyMsg(msg), Toast.LENGTH_SHORT).show();
                            if (code == 200) {
                                adapter.removeItem(record);
                            } else {
                                adapter.notifyDataSetChanged();
                            }
                        });
                    }
                } catch (Exception e) {
                    Log.e("MyRegistrations", "取消解析失败", e);
                    runOnUiThread(() -> {
                        Toast.makeText(MyRegistrationsActivity.this, "操作失败", Toast.LENGTH_SHORT).show();
                        adapter.notifyDataSetChanged();
                    });
                }
            }
        });
    }
}

