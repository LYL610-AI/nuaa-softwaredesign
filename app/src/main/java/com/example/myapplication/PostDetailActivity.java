package com.example.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import okhttp3.RequestBody;
import okhttp3.Response;

public class PostDetailActivity extends AppCompatActivity {

    private List<Comment> commentList = new ArrayList<>();
    private CommentAdapter adapter;
    private final OkHttpClient client = ApiConfig.getClient();
    private Post currentPost;
    private boolean isAdmin;
    private String currentUserId;
    private static final String TAG = "PostDetail";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        currentPost = (Post) getIntent().getSerializableExtra("post_data");

        User currentUser = SessionManager.getCurrentUser();
        currentUserId = currentUser != null ? currentUser.getUserId() : "";
        isAdmin = currentUser != null && currentUser.isAdmin();

        TextView tvTitle = findViewById(R.id.tv_post_detail_title);
        TextView tvContent = findViewById(R.id.tv_post_detail_content);
        TextView tvAuthor = findViewById(R.id.tv_post_detail_author);
        TextView tvTime = findViewById(R.id.tv_post_detail_time);
        ImageView ivPostImage = findViewById(R.id.iv_post_image);
        if (currentPost != null) {
            tvTitle.setText(currentPost.getTitle());
            tvContent.setText(currentPost.getContent());
            String authorName = currentPost.getUserName();
            tvAuthor.setText("用户：" + (authorName != null && !authorName.isEmpty() ? authorName : currentPost.getUserId() != null ? currentPost.getUserId() : "未知"));
            tvTime.setText("时间：" + (currentPost.getPublishTime() != null ? currentPost.getPublishTime() : "未知"));

            String imageUrl = ApiConfig.getFullImageUrl(currentPost.getPictureUrl());
            if (imageUrl != null && !imageUrl.isEmpty()) {
                ivPostImage.setVisibility(View.VISIBLE);
                ImageLoader.load(imageUrl, ivPostImage);
            }
        }

        RecyclerView rv = findViewById(R.id.rv_comments);
        rv.setLayoutManager(new LinearLayoutManager(this));

        adapter = new CommentAdapter(commentList, currentUserId, isAdmin);
        adapter.setOnDeleteListener((comment, position) -> {
            new AlertDialog.Builder(PostDetailActivity.this)
                    .setTitle("删除评论")
                    .setMessage("确定要删除这条评论吗？")
                    .setPositiveButton("确认删除", (dialog, which) ->
                        deleteComment(comment, position))
                    .setNegativeButton("返回", (dialog, which) ->
                        adapter.notifyItemChanged(position))
                    .show();
        });
        rv.setAdapter(adapter);

        fetchComments();

        EditText etInput = findViewById(R.id.et_comment_input);
        findViewById(R.id.btn_send_comment).setOnClickListener(v -> {
            String content = etInput.getText().toString().trim();
            if (!content.isEmpty()) {
                submitComment(content, etInput);
            }
        });
    }

    private void fetchComments() {
        if (currentPost == null) return;
        String url = ApiConfig.getBaseUrl() + "/comment/list/" + currentPost.getPostId();
        Request request = new Request.Builder().url(url).get().build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String body = response.body() != null ? response.body().string() : null;
                        if (body == null) return;
                        JSONObject obj = new JSONObject(body);
                        Object dataField = obj.get("data");
                        String dataJson;
                        if (dataField instanceof JSONObject) {
                            dataJson = ((JSONObject) dataField).getJSONArray("list").toString();
                        } else {
                            dataJson = dataField.toString();
                        }
                        List<Comment> data = new Gson().fromJson(
                                dataJson,
                                new TypeToken<List<Comment>>(){}.getType());
                        runOnUiThread(() -> {
                            commentList.clear();
                            commentList.addAll(data);
                            adapter.notifyDataSetChanged();
                        });
                    } catch (Exception e) {
                        Log.e(TAG, "Parse error", e);
                    }
                }
            }
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Fetch comments failed", e);
            }
        });
    }

    private void submitComment(String content, EditText et) {
        String url = ApiConfig.getBaseUrl() + "/comment/create";
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser == null || currentUser.getUserId() == null) {
            Toast.makeText(this, "请重新登录", Toast.LENGTH_SHORT).show();
            return;
        }
        JSONObject json = new JSONObject();
        try {
            json.put("postId", currentPost.getPostId());
            json.put("userId", currentUser.getUserId());
            json.put("content", content);
        } catch (Exception e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(ApiConfig.JSON, json.toString());
        Request request = new Request.Builder().url(url).post(body).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) {
                String result = null;
                try {
                    if (response.body() != null) {
                        result = response.body().string();
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Failed to read body", e);
                }
                final String finalResult = result;
                runOnUiThread(() -> {
                    if (finalResult == null) {
                        Toast.makeText(PostDetailActivity.this, "评论失败，请重试", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    try {
                        JSONObject res = new JSONObject(finalResult);
                        int code = res.has("code") ? res.getInt("code") : 500;
                        String msg = res.optString("message", "评论成功");
                        Toast.makeText(PostDetailActivity.this, ApiConfig.friendlyMsg(msg), Toast.LENGTH_SHORT).show();
                        if (code == 200) {
                            et.setText("");
                            fetchComments();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Parse error", e);
                        Toast.makeText(PostDetailActivity.this, "评论失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                    Toast.makeText(PostDetailActivity.this, "网络连接失败", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void deleteComment(Comment comment, int position) {
        String url = ApiConfig.getBaseUrl() + "/comment/delete/" + comment.getCommentId();
        Request request = new Request.Builder().url(url).delete().build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(PostDetailActivity.this, "网络连接失败", Toast.LENGTH_SHORT).show();
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
                    Log.e(TAG, "Failed to read body", e);
                }
                final String finalResult = result;
                runOnUiThread(() -> {
                    if (finalResult == null) {
                        Toast.makeText(PostDetailActivity.this, "服务器返回异常", Toast.LENGTH_SHORT).show();
                        adapter.notifyItemChanged(position);
                        return;
                    }
                    try {
                        JSONObject res = new JSONObject(finalResult);
                        int code = res.has("code") ? res.getInt("code") : 500;
                        String msg = res.optString("message", "已删除");
                        Toast.makeText(PostDetailActivity.this, ApiConfig.friendlyMsg(msg), Toast.LENGTH_SHORT).show();
                        if (code == 200) {
                            adapter.removeItem(comment);
                        } else {
                            adapter.notifyItemChanged(position);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Parse error", e);
                        Toast.makeText(PostDetailActivity.this, "操作失败", Toast.LENGTH_SHORT).show();
                        adapter.notifyItemChanged(position);
                    }
                });
            }
        });
    }
}

