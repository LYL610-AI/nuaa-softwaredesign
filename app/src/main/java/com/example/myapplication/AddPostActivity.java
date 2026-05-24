package com.example.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;
import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AddPostActivity extends AppCompatActivity {

    private final OkHttpClient client = ApiConfig.getClient();
    private final Gson gson = new Gson();
    private boolean eligible;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        EditText etActivityName = findViewById(R.id.et_post_activity_name);
        EditText etTitle = findViewById(R.id.et_post_title);
        EditText etImageUrl = findViewById(R.id.et_post_image_url);
        EditText etContent = findViewById(R.id.et_post_input);
        Button btnSend = findViewById(R.id.btn_send_post);
        TextView tvEligibility = findViewById(R.id.tv_post_eligibility);

        // 检查是否志愿者
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser == null || currentUser.getUserId() == null) {
            Toast.makeText(this, "请重新登录", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        if (!currentUser.isVolunteer()) {
            Toast.makeText(this, "仅志愿者可以发布主题帖", Toast.LENGTH_LONG).show();
            btnSend.setEnabled(false);
            tvEligibility.setText("（仅参与过支教活动的志愿者可发布）");
            return;
        }

        // 检查是否有报名记录（参与过支教活动）
        checkEligibility(currentUser.getUserId(), btnSend, tvEligibility);

        btnSend.setOnClickListener(v -> {
            String activityName = etActivityName.getText().toString().trim();
            String title = etTitle.getText().toString().trim();
            String imageUrl = etImageUrl.getText().toString().trim();
            String content = etContent.getText().toString().trim();

            if (activityName.isEmpty() || title.isEmpty() || content.isEmpty()) {
                Toast.makeText(this, "支教活动名称、标题和内容不能为空", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!eligible) {
                Toast.makeText(this, "您需要参与过已结束的支教活动才能发布主题帖", Toast.LENGTH_LONG).show();
                return;
            }

            btnSend.setEnabled(false);
            btnSend.setText("验证活动中...");
            searchActivityAndSubmit(activityName, title, imageUrl, content, btnSend);
        });
    }

    private void checkEligibility(String userId, Button btnSend, TextView tvEligibility) {
        String url = ApiConfig.getBaseUrl() + "/registration/my";
        Request request = new Request.Builder().url(url).get().build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    btnSend.setEnabled(false);
                    tvEligibility.setText("（网络异常，无法验证发布资格）");
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                boolean hasEligible = false;
                try {
                    String body = response.body() != null ? response.body().string() : null;
                    if (body != null) {
                        JSONObject obj = new JSONObject(body);
                        if (obj.getInt("code") == 200) {
                            Object dataField = obj.get("data");
                            String dataJson;
                            if (dataField instanceof JSONObject) {
                                dataJson = ((JSONObject) dataField).getJSONArray("list").toString();
                            } else {
                                dataJson = dataField.toString();
                            }
                            com.google.gson.Gson gson = new com.google.gson.Gson();
                            RegistrationRecord[] records = gson.fromJson(dataJson,
                                    RegistrationRecord[].class);
                            for (RegistrationRecord r : records) {
                                // 已通过的报名记录视为有效参与
                                if ("通过".equals(r.getAuditState())) {
                                    hasEligible = true;
                                    break;
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.e("AddPost", "Eligibility check error", e);
                }
                final boolean finalEligible = hasEligible;
                runOnUiThread(() -> {
                    eligible = finalEligible;
                    if (finalEligible) {
                        tvEligibility.setText("（已确认为支教活动参与者，可发布主题帖）");
                        tvEligibility.setTextColor(0xFF4CAF50);
                    } else {
                        btnSend.setEnabled(false);
                        tvEligibility.setText("（您需要先报名参加支教活动并获得通过审核）");
                        tvEligibility.setTextColor(0xFFFF9800);
                    }
                });
            }
        });
    }

    private void searchActivityAndSubmit(String activityName, String title, String imageUrl, String content, Button btnSend) {
        String keyword = activityName;
        String url = ApiConfig.getBaseUrl() + "/activity/list?keyword=" + keyword;

        Request request = new Request.Builder().url(url).get().build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    btnSend.setEnabled(true);
                    btnSend.setText("提交主题帖（需审核）");
                    Toast.makeText(AddPostActivity.this, "网络连接失败", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String matchedActivityId = null;
                try {
                    String body = response.body() != null ? response.body().string() : null;
                    if (body != null) {
                        JSONObject obj = new JSONObject(body);
                        if (obj.getInt("code") == 200) {
                            Object dataField = obj.get("data");
                            String dataJson;
                            if (dataField instanceof JSONObject) {
                                dataJson = ((JSONObject) dataField).getJSONArray("list").toString();
                            } else if (dataField instanceof org.json.JSONArray) {
                                dataJson = dataField.toString();
                            } else {
                                dataJson = null;
                            }
                            if (dataJson != null) {
                                List<Activity> activities = gson.fromJson(dataJson, new TypeToken<List<Activity>>(){}.getType());
                                for (Activity a : activities) {
                                    if (activityName.equals(a.getTitle())) {
                                        matchedActivityId = a.getActivityId();
                                        break;
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.e("AddPost", "Activity search error", e);
                }

                final String activityId = matchedActivityId;
                runOnUiThread(() -> {
                    if (activityId == null) {
                        btnSend.setEnabled(true);
                        btnSend.setText("提交主题帖（需审核）");
                        Toast.makeText(AddPostActivity.this, "支教活动「" + activityName + "」不存在，请检查活动名称", Toast.LENGTH_LONG).show();
                        return;
                    }
                    btnSend.setText("发布中...");
                    submitPostToServer(activityId, title, imageUrl, content, btnSend);
                });
            }
        });
    }

    private void submitPostToServer(String activityId, String title, String imageUrl, String content, Button btnSend) {
        String url = ApiConfig.getBaseUrl() + "/post/create";

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("activityId", activityId);
            jsonObject.put("title", title);
            jsonObject.put("content", content);
            if (imageUrl != null && !imageUrl.isEmpty()) {
                jsonObject.put("pictureUrl", imageUrl);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(ApiConfig.JSON, jsonObject.toString());
        Request request = new Request.Builder().url(url).post(body).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    btnSend.setEnabled(true);
                    btnSend.setText("提交主题帖（需审核）");
                    Toast.makeText(AddPostActivity.this, "网络连接失败", Toast.LENGTH_SHORT).show();
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
                    Log.e("AddPost", "Failed to read body", e);
                }
                final String finalResult = result;
                runOnUiThread(() -> {
                    if (finalResult == null) {
                        btnSend.setEnabled(true);
                        btnSend.setText("提交主题帖（需审核）");
                        Toast.makeText(AddPostActivity.this, "服务器返回异常", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    try {
                        JSONObject res = new JSONObject(finalResult);
                        int code = res.has("code") ? res.getInt("code") : 500;
                        String msg = res.optString("message", "发布完成");
                        Toast.makeText(AddPostActivity.this, ApiConfig.friendlyMsg(msg), Toast.LENGTH_LONG).show();
                        if (code == 200) {
                            finish();
                        } else {
                            btnSend.setEnabled(true);
                            btnSend.setText("提交主题帖（需审核）");
                        }
                    } catch (Exception e) {
                        Log.e("AddPost", "Parse error", e);
                        btnSend.setEnabled(true);
                        btnSend.setText("提交主题帖（需审核）");
                        Toast.makeText(AddPostActivity.this, "服务器返回数据格式有误", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}

