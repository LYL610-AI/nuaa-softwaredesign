package com.example.myapplication;

import android.os.Bundle;
import android.util.Log;
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

public class MyPostsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PostAdapter adapter;
    private List<Post> dataList = new ArrayList<>();

    private final OkHttpClient client = ApiConfig.getClient();
    private final Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_posts);

        recyclerView = findViewById(R.id.rv_my_posts);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new PostAdapter(dataList);

        User currentUser = SessionManager.getCurrentUser();
        String currentUserId = currentUser != null ? currentUser.getUserId() : "";
        adapter.setOnDeleteListener((post, position) -> {
            new AlertDialog.Builder(MyPostsActivity.this)
                    .setTitle("删除主题帖")
                    .setMessage("确定要删除「" + post.getTitle() + "」吗？")
                    .setPositiveButton("确认删除", (dialog, which) ->
                        deletePost(post, position))
                    .setNegativeButton("返回", (dialog, which) ->
                        adapter.notifyItemChanged(position))
                    .show();
        }, currentUserId);

        recyclerView.setAdapter(adapter);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        fetchMyPosts();
    }

    private void fetchMyPosts() {
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser == null || currentUser.getUserId() == null) {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = ApiConfig.getBaseUrl() + "/post/my";

        Request request = new Request.Builder().url(url).get().build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(MyPostsActivity.this, "网络连接失败", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String body = response.body() != null ? response.body().string() : null;
                        if (body == null) return;
                        JSONObject jsonObject = new JSONObject(body);
                        if (jsonObject.getInt("code") == 200) {
                            Object dataField = jsonObject.get("data");
                            String dataJson;
                            if (dataField instanceof JSONObject) {
                                dataJson = ((JSONObject) dataField).getJSONArray("list").toString();
                            } else {
                                dataJson = dataField.toString();
                            }
                            List<Post> serverData = gson.fromJson(dataJson,
                                    new TypeToken<List<Post>>(){}.getType());
                            runOnUiThread(() -> {
                                dataList.clear();
                                dataList.addAll(serverData);
                                adapter.notifyDataSetChanged();
                            });
                        }
                    } catch (Exception e) {
                        Log.e("MyPosts", "Parse error", e);
                    }
                }
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
                    Toast.makeText(MyPostsActivity.this, "网络连接失败", Toast.LENGTH_SHORT).show();
                    adapter.notifyItemChanged(position);
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
                    Log.e("MyPosts", "Failed to read body", e);
                }
                final String finalResult = result;
                runOnUiThread(() -> {
                    if (finalResult == null) {
                        Toast.makeText(MyPostsActivity.this, "服务器返回异常", Toast.LENGTH_SHORT).show();
                        adapter.notifyItemChanged(position);
                        return;
                    }
                    try {
                        JSONObject res = new JSONObject(finalResult);
                        int code = res.has("code") ? res.getInt("code") : 500;
                        String msg = res.optString("message", "已删除");
                        Toast.makeText(MyPostsActivity.this, ApiConfig.friendlyMsg(msg), Toast.LENGTH_SHORT).show();
                        if (code == 200) {
                            adapter.removeItem(post);
                        } else {
                            adapter.notifyItemChanged(position);
                        }
                    } catch (Exception e) {
                        Log.e("MyPosts", "Parse error", e);
                        Toast.makeText(MyPostsActivity.this, "操作失败", Toast.LENGTH_SHORT).show();
                        adapter.notifyItemChanged(position);
                    }
                });
            }
        });
    }
}

