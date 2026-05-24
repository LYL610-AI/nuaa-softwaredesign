package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

public class PostActivity extends AppCompatActivity {

    private List<Post> postList = new ArrayList<>();
    private PostAdapter adapter;
    private final OkHttpClient client = ApiConfig.getClient();
    private boolean isAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        User currentUser = SessionManager.getCurrentUser();
        isAdmin = currentUser != null && currentUser.isAdmin();

        RecyclerView rv = findViewById(R.id.rv_posts);
        rv.setLayoutManager(new LinearLayoutManager(this));

        adapter = new PostAdapter(postList);
        rv.setAdapter(adapter);

        fetchPosts();

        findViewById(R.id.fab_add_post).setOnClickListener(v -> {
            startActivity(new Intent(this, AddPostActivity.class));
        });

        TextView btnBackHome = findViewById(R.id.btn_back_to_home);
        if (btnBackHome != null) {
            btnBackHome.setOnClickListener(v -> {
                startActivity(new Intent(this, HomeActivity.class));
                finish();
            });
        }
    }

    private void fetchPosts() {
        String url = ApiConfig.getBaseUrl() + "/post/list";
        Request request = new Request.Builder().url(url).get().build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                    Toast.makeText(PostActivity.this, "加载社区失败", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String body = response.body() != null ? response.body().string() : null;
                        if (body == null) return;
                        JSONObject obj = new JSONObject(body);
                        if (obj.getInt("code") == 200) {
                            Object dataField = obj.get("data");
                            String dataJson;
                            if (dataField instanceof JSONObject) {
                                dataJson = ((JSONObject) dataField).getJSONArray("list").toString();
                            } else {
                                dataJson = dataField.toString();
                            }
                            List<Post> serverData = new Gson().fromJson(
                                    dataJson,
                                    new TypeToken<List<Post>>(){}.getType()
                            );
                            runOnUiThread(() -> {
                                postList.clear();
                                // 非管理员只显示审核通过的帖子
                                for (Post p : serverData) {
                                    if (isAdmin || "通过".equals(p.getAuditState())) {
                                        postList.add(p);
                                    }
                                }
                                adapter.notifyDataSetChanged();
                            });
                        }
                    } catch (Exception e) {
                        Log.e("PostActivity", "Parse error", e);
                    }
                }
            }
        });
    }
}

