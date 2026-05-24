package com.example.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;



import org.json.JSONObject;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AdminActivityDetailActivity extends AppCompatActivity {

    private Activity currentActivity;
    private final OkHttpClient client = ApiConfig.getClient();
    private Button btnApprove, btnReject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_activity_detail);

        currentActivity = (Activity) getIntent().getSerializableExtra("activity_data");
        if (currentActivity == null) {
            Toast.makeText(this, "数据加载失败", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        TextView tvTitle = findViewById(R.id.tv_detail_title);
        TextView tvSchool = findViewById(R.id.tv_detail_school);
        TextView tvAddress = findViewById(R.id.tv_detail_address);
        TextView tvRecruits = findViewById(R.id.tv_detail_recruits);
        TextView tvStartDate = findViewById(R.id.tv_detail_start_date);
        TextView tvEndDate = findViewById(R.id.tv_detail_end_date);
        TextView tvState = findViewById(R.id.tv_detail_state);
        TextView tvPublishTime = findViewById(R.id.tv_detail_publish_time);
        TextView tvContent = findViewById(R.id.tv_detail_content);
        ImageView ivCover = findViewById(R.id.iv_detail_cover);
        btnApprove = findViewById(R.id.btn_audit_approve);
        btnReject = findViewById(R.id.btn_audit_reject);

        tvTitle.setText(currentActivity.getTitle());
        tvSchool.setText("发布学校ID：" + (currentActivity.getUserId() != null ? currentActivity.getUserId() : "未知"));
        tvAddress.setText("学校地址：" + (currentActivity.getSchoolAddress() != null ? currentActivity.getSchoolAddress() : "未填写"));
        tvRecruits.setText("招募人数：" + currentActivity.getRecruitsNumber() + "人");
        tvStartDate.setText("开始日期：" + (currentActivity.getStartDate() != null ? currentActivity.getStartDate() : "未设置"));
        tvEndDate.setText("结束日期：" + (currentActivity.getEndDate() != null ? currentActivity.getEndDate() : "未设置"));
        tvState.setText("活动状态：" + (currentActivity.getActivityState() != null ? currentActivity.getActivityState() : "未知"));
        tvPublishTime.setText("发布时间：" + (currentActivity.getPublishTime() != null ? currentActivity.getPublishTime() : "未知"));
        tvContent.setText(currentActivity.getContent() != null ? currentActivity.getContent() : "");

        String imageUrl = ApiConfig.getFullImageUrl(currentActivity.getPictureUrl());
        if (imageUrl != null && !imageUrl.isEmpty()) {
            ivCover.setVisibility(View.VISIBLE);
            ImageLoader.load(imageUrl, ivCover);
        }

        btnApprove.setOnClickListener(v -> auditActivity("通过"));
        btnReject.setOnClickListener(v -> auditActivity("未通过"));

        // 已审核通过或已驳回的活动不允许再次操作
        String state = currentActivity.getAuditState();
        if ("通过".equals(state) || "未通过".equals(state)) {
            btnApprove.setEnabled(false);
            btnReject.setEnabled(false);
            String label = "通过".equals(state) ? "已通过" : "已驳回";
            btnApprove.setText(label);
            btnReject.setText(label);
        }
    }

    private void auditActivity(String auditResult) {
        btnApprove.setEnabled(false);
        btnReject.setEnabled(false);

        String url = ApiConfig.getBaseUrl() + "/activity/review/" + currentActivity.getActivityId();
        JSONObject json = new JSONObject();
        try {
            json.put("auditState", "通过".equals(auditResult) ? "1" : "2");
        } catch (Exception e) {
            e.printStackTrace();
            enableButtons();
            return;
        }

        Log.d("AdminDetail", "Audit request: " + json.toString());

        RequestBody body = RequestBody.create(ApiConfig.JSON, json.toString());
        Request request = new Request.Builder().url(url).put(body).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(AdminActivityDetailActivity.this, "网络请求失败", Toast.LENGTH_SHORT).show();
                    enableButtons();
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
                    Log.e("AdminDetail", "Failed to read body", e);
                }
                Log.d("AdminDetail", "Audit response: " + result);
                final String finalResult = result;
                runOnUiThread(() -> {
                    if (finalResult == null) {
                        Toast.makeText(AdminActivityDetailActivity.this, "服务器返回异常", Toast.LENGTH_SHORT).show();
                        enableButtons();
                        return;
                    }
                    try {
                        JSONObject res = new JSONObject(finalResult);
                        int code = res.has("code") ? res.getInt("code") : 500;
                        String msg = res.optString("message", auditResult + " 成功");
                        Toast.makeText(AdminActivityDetailActivity.this, ApiConfig.friendlyMsg(msg), Toast.LENGTH_SHORT).show();
                        if (code == 200) {
                            finish();
                        } else {
                            enableButtons();
                        }
                    } catch (Exception e) {
                        Log.e("AdminDetail", "Parse error", e);
                        Toast.makeText(AdminActivityDetailActivity.this, "操作失败", Toast.LENGTH_SHORT).show();
                        enableButtons();
                    }
                });
            }
        });
    }

    private void enableButtons() {
        btnApprove.setEnabled(true);
        btnReject.setEnabled(true);
    }
}
