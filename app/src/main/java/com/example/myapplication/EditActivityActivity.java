package com.example.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class EditActivityActivity extends AppCompatActivity {

    private final okhttp3.OkHttpClient client = ApiConfig.getClient();
    private String activityId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_activity);

        TeachingActivity data = (TeachingActivity) getIntent().getSerializableExtra("activity_data");
        if (data == null) {
            Toast.makeText(this, "数据加载失败", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        activityId = data.getActivityId();

        EditText etTitle = findViewById(R.id.et_edit_title);
        EditText etLocation = findViewById(R.id.et_edit_location);
        EditText etStartDate = findViewById(R.id.et_edit_start_date);
        EditText etEndDate = findViewById(R.id.et_edit_end_date);
        EditText etNum = findViewById(R.id.et_edit_num);
        EditText etContent = findViewById(R.id.et_edit_content);
        Button btnSubmit = findViewById(R.id.btn_edit_submit);

        etTitle.setText(data.getTitle() != null ? data.getTitle() : "");
        etLocation.setText(data.getSchoolAddress() != null ? data.getSchoolAddress() : "");
        etStartDate.setText(data.getStartDate() != null ? data.getStartDate() : "");
        etEndDate.setText(data.getEndDate() != null ? data.getEndDate() : "");
        etNum.setText(String.valueOf(data.getRecruitsNumber()));
        etContent.setText(data.getContent() != null ? data.getContent() : "");

        if ("结束".equals(data.getActivityState())) {
            etTitle.setEnabled(false);
            etLocation.setEnabled(false);
            etStartDate.setEnabled(false);
            etEndDate.setEnabled(false);
            etNum.setEnabled(false);
            etContent.setEnabled(false);
            btnSubmit.setEnabled(false);
            btnSubmit.setText("活动已结束");
            Toast.makeText(this, "该活动已结束，无法修改信息", Toast.LENGTH_LONG).show();
        }

        btnSubmit.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String schoolAddress = etLocation.getText().toString().trim();
            String startDate = etStartDate.getText().toString().trim();
            String endDate = etEndDate.getText().toString().trim();
            String numStr = etNum.getText().toString().trim();
            String content = etContent.getText().toString().trim();

            if (title.isEmpty() || schoolAddress.isEmpty() || startDate.isEmpty()
                    || endDate.isEmpty() || numStr.isEmpty() || content.isEmpty()) {
                Toast.makeText(this, "请填写所有必填项", Toast.LENGTH_SHORT).show();
                return;
            }

            User currentUser = SessionManager.getCurrentUser();
            if (currentUser == null || currentUser.getUserId() == null) {
                Toast.makeText(this, "请重新登录", Toast.LENGTH_SHORT).show();
                return;
            }

            submitUpdate(title, schoolAddress, startDate, endDate,
                    Integer.parseInt(numStr), content, currentUser.getUserId());
        });
    }

    private void submitUpdate(String title, String schoolAddress,
            String startDate, String endDate, int recruitsNumber,
            String content, String userId) {

        String url = ApiConfig.getBaseUrl() + "/activity/update/" + activityId;

        JSONObject json = new JSONObject();
        try {
            json.put("activityId", activityId);
            json.put("title", title);
            json.put("schoolAddress", schoolAddress);
            json.put("startDate", startDate);
            json.put("endDate", endDate);
            json.put("recruitsNumber", recruitsNumber);
            json.put("content", content);
            json.put("userId", userId);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.d("EditActivity", "请求体: " + json.toString());

        RequestBody body = RequestBody.create(ApiConfig.JSON, json.toString());
        Request request = new Request.Builder().url(url).put(body).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                    Toast.makeText(EditActivityActivity.this, "网络连接失败", Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                Log.d("EditActivity", "HTTP " + response.code() + " | " + result);
                try {
                    JSONObject res = new JSONObject(result);
                    int code = res.getInt("code");
                    String msg = res.optString("message", "更新成功");
                    runOnUiThread(() -> {
                        Toast.makeText(EditActivityActivity.this, ApiConfig.friendlyMsg(msg), Toast.LENGTH_LONG).show();
                        if (code == 200) {
                            setResult(RESULT_OK);
                            finish();
                        }
                    });
                } catch (Exception e) {
                    Log.e("EditActivity", "解析失败", e);
                    runOnUiThread(() ->
                        Toast.makeText(EditActivityActivity.this, "更新失败: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
    }
}
