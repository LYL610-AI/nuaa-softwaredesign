package com.example.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class WriteSummaryActivity extends AppCompatActivity {

    private final okhttp3.OkHttpClient client = ApiConfig.getClient();
    private String activityId;
    private String activityTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_summary);

        TeachingActivity data = (TeachingActivity) getIntent().getSerializableExtra("activity_data");
        if (data == null) {
            Toast.makeText(this, "数据加载失败", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        activityId = data.getActivityId();
        activityTitle = data.getTitle();

        TextView tvTitle = findViewById(R.id.tv_summary_activity_title);
        TextView tvTime = findViewById(R.id.tv_summary_activity_time);
        TextView tvAddress = findViewById(R.id.tv_summary_activity_address);
        EditText etSummary = findViewById(R.id.et_summary_content);
        Button btnSubmit = findViewById(R.id.btn_submit_summary);

        tvTitle.setText("项目名称：" + (data.getTitle() != null ? data.getTitle() : ""));
        tvTime.setText("支教时间：" + (data.getStartDate() != null ? data.getStartDate() : "?")
                + " 至 " + (data.getEndDate() != null ? data.getEndDate() : "?"));
        tvAddress.setText("支教地址：" + (data.getSchoolAddress() != null ? data.getSchoolAddress() : ""));

        String existingContent = data.getSummaryContent();
        if (existingContent != null && !existingContent.isEmpty()) {
            etSummary.setText(existingContent);
        } else if (data.getSummary() != null && !data.getSummary().isEmpty()) {
            etSummary.setText(data.getSummary());
        }

        if ("结束".equals(data.getActivityState())) {
            etSummary.setEnabled(false);
            btnSubmit.setEnabled(false);
            btnSubmit.setText("活动已结束");
            Toast.makeText(this, "该活动已结束，无法提交总结", Toast.LENGTH_LONG).show();
        }

        String summaryAuditState = data.getSummaryAuditState();
        if ("通过".equals(summaryAuditState)) {
            etSummary.setEnabled(false);
            btnSubmit.setEnabled(false);
            btnSubmit.setText("总结已通过审核");
            Toast.makeText(this, "总结已通过审核，无需重复提交", Toast.LENGTH_LONG).show();
        }

        btnSubmit.setOnClickListener(v -> {
            String summary = etSummary.getText().toString().trim();
            if (summary.isEmpty()) {
                Toast.makeText(this, "请填写总结报告内容", Toast.LENGTH_SHORT).show();
                return;
            }

            User currentUser = SessionManager.getCurrentUser();
            if (currentUser == null || currentUser.getUserId() == null) {
                Toast.makeText(this, "请重新登录", Toast.LENGTH_SHORT).show();
                return;
            }

            submitSummary(summary, currentUser.getUserId());
        });
    }

    private void submitSummary(String summary, String userId) {
        String url = ApiConfig.getBaseUrl() + "/activity/summary/" + activityId;

        JSONObject json = new JSONObject();
        try {
            json.put("title", activityTitle != null ? activityTitle : "");
            json.put("content", summary);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.d("WriteSummary", "请求体: " + json.toString());

        RequestBody body = RequestBody.create(ApiConfig.JSON, json.toString());
        Request request = new Request.Builder().url(url).post(body).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                    Toast.makeText(WriteSummaryActivity.this, "网络连接失败", Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                Log.d("WriteSummary", "HTTP " + response.code() + " | " + result);
                try {
                    JSONObject res = new JSONObject(result);
                    int code = res.getInt("code");
                    String msg = res.optString("message", "提交成功");
                    runOnUiThread(() -> {
                        Toast.makeText(WriteSummaryActivity.this, ApiConfig.friendlyMsg(msg), Toast.LENGTH_LONG).show();
                        if (code == 200) {
                            setResult(RESULT_OK);
                            finish();
                        }
                    });
                } catch (Exception e) {
                    Log.e("WriteSummary", "解析失败", e);
                    runOnUiThread(() ->
                        Toast.makeText(WriteSummaryActivity.this, "提交失败: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
    }
}
