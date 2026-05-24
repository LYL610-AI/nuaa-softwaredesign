package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
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

public class BrowseApprovedActivity extends AppCompatActivity {

    private RecyclerView rv;
    private EditText etSearch;
    private ActivityBrowseAdapter adapter;

    private List<Activity> allList = new ArrayList<>();
    private List<Activity> displayList = new ArrayList<>();

    private final OkHttpClient client = ApiConfig.getClient();
    private final Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_approved);

        rv = findViewById(R.id.rv_approved_activities);
        rv.setLayoutManager(new LinearLayoutManager(this));
        etSearch = findViewById(R.id.et_search);

        adapter = new ActivityBrowseAdapter(displayList, (activity, position) -> {
            Intent intent = new Intent(BrowseApprovedActivity.this, DetailActivity.class);
            intent.putExtra("activity_data", activity);
            startActivity(intent);
        });
        rv.setAdapter(adapter);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        findViewById(R.id.btn_search).setOnClickListener(v -> {
            String keyword = etSearch.getText().toString().trim();
            filterActivities(keyword);
        });

        loadActivities();
    }

    private void loadActivities() {
        String url = ApiConfig.getBaseUrl() + "/activity/list";
        Request request = new Request.Builder().url(url).get().build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                    Toast.makeText(BrowseApprovedActivity.this, "网络错误", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    runOnUiThread(() ->
                        Toast.makeText(BrowseApprovedActivity.this, "服务器错误: " + response.code(), Toast.LENGTH_SHORT).show());
                    return;
                }
                try {
                    String jsonResult = response.body().string();
                    JSONObject resObj = new JSONObject(jsonResult);
                    if (resObj.getInt("code") == 200) {
                        String dataArrayString = resObj.getJSONObject("data").getJSONArray("list").toString();
                        List<Activity> serverData = gson.fromJson(dataArrayString,
                                new TypeToken<List<Activity>>(){}.getType());

                        // 筛选已审核通过且可报名的活动
                        List<Activity> approved = new ArrayList<>();
                        for (Activity a : serverData) {
                            if ("通过".equals(a.getAuditState())) {
                                String state = a.getActivityState();
                                if ("招募中".equals(state) || "进行中".equals(state)) {
                                    approved.add(a);
                                }
                            }
                        }

                        runOnUiThread(() -> {
                            allList.clear();
                            allList.addAll(approved);
                            displayList.clear();
                            displayList.addAll(approved);
                            adapter.notifyDataSetChanged();
                        });
                    }
                } catch (Exception e) {
                    Log.e("BrowseApproved", "Parse error", e);
                    runOnUiThread(() ->
                        Toast.makeText(BrowseApprovedActivity.this, "数据解析失败", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void filterActivities(String keyword) {
        displayList.clear();
        if (TextUtils.isEmpty(keyword)) {
            displayList.addAll(allList);
        } else {
            for (Activity a : allList) {
                if (a.getTitle() != null && a.getTitle().contains(keyword)) {
                    displayList.add(a);
                }
            }
        }
        adapter.updateData(displayList);
    }
}
