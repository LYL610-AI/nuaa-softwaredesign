package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
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

public class HomeActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ActivityAdapter adapter;
    private List<Activity> dataList = new ArrayList<>();
    private EditText etSearch;
    private final OkHttpClient client = ApiConfig.getClient();
    private final Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        recyclerView = findViewById(R.id.rv_activities);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ActivityAdapter(dataList);
        recyclerView.setAdapter(adapter);

        etSearch = findViewById(R.id.et_search);
        Button btnSearch = findViewById(R.id.btn_search);

        btnSearch.setOnClickListener(v -> {
            String keyword = etSearch.getText().toString().trim();
            if (keyword.isEmpty()) {
                fetchActivitiesFromServer();
            } else {
                searchActivities(keyword);
            }
        });

        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String keyword = etSearch.getText().toString().trim();
                if (keyword.isEmpty()) {
                    fetchActivitiesFromServer();
                } else {
                    searchActivities(keyword);
                }
                return true;
            }
            return false;
        });

        initBottomNavigation();
        fetchActivitiesFromServer();
    }

    private void fetchActivitiesFromServer() {
        String url = ApiConfig.getBaseUrl() + "/activity/reviewed";

        Request request = new Request.Builder().url(url).get().build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                    Toast.makeText(HomeActivity.this, "网络请求失败，请检查服务器", Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String jsonResult = response.body().string();
                    try {
                        List<Activity> serverData = parseActivityList(jsonResult);
                        if (serverData != null) {
                            runOnUiThread(() -> {
                                dataList.clear();
                                dataList.addAll(serverData);
                                adapter.notifyDataSetChanged();
                            });
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(() ->
                            Toast.makeText(HomeActivity.this, "数据解析异常", Toast.LENGTH_SHORT).show()
                        );
                    }
                }
            }
        });
    }

    private void searchActivities(String keyword) {
        String url = ApiConfig.getBaseUrl() + "/activity/list?keyword=" + keyword;

        Request request = new Request.Builder().url(url).get().build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                    Toast.makeText(HomeActivity.this, "搜索请求失败", Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String jsonResult = response.body().string();
                    try {
                        List<Activity> serverData = parseActivityList(jsonResult);
                        if (serverData != null) {
                            runOnUiThread(() -> {
                                dataList.clear();
                                dataList.addAll(serverData);
                                adapter.notifyDataSetChanged();
                                if (serverData.isEmpty()) {
                                    Toast.makeText(HomeActivity.this, "未找到匹配结果", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(() ->
                            Toast.makeText(HomeActivity.this, "搜索数据解析异常", Toast.LENGTH_SHORT).show()
                        );
                    }
                }
            }
        });
    }

    private List<Activity> parseActivityList(String jsonResult) {
        try {
            String listJson;
            if (jsonResult.trim().startsWith("[")) {
                listJson = jsonResult;
            } else {
                JSONObject jsonObject = new JSONObject(jsonResult);
                if (jsonObject.getInt("code") == 200) {
                    Object data = jsonObject.get("data");
                    if (data instanceof JSONObject) {
                        listJson = ((JSONObject) data).getJSONArray("list").toString();
                    } else if (data instanceof org.json.JSONArray) {
                        listJson = data.toString();
                    } else {
                        return null;
                    }
                } else {
                    return null;
                }
            }
            return gson.fromJson(listJson, new TypeToken<List<Activity>>(){}.getType());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void initBottomNavigation() {
        Button navHome = findViewById(R.id.nav_home);
        Button navPosts = findViewById(R.id.nav_posts);
        Button navProfile = findViewById(R.id.nav_profile);

        if (navHome != null) navHome.setOnClickListener(v ->
            Toast.makeText(this, "已在首页", Toast.LENGTH_SHORT).show());
        if (navPosts != null) {
            navPosts.setOnClickListener(v -> {
                startActivity(new Intent(HomeActivity.this, PostActivity.class));
                finish();
            });
        }
        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
                finish();
            });
        }
    }
}
