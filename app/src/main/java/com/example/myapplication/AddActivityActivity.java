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

public class AddActivityActivity extends AppCompatActivity {

    private final okhttp3.OkHttpClient client = ApiConfig.getClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_activity);

        EditText etTitle = findViewById(R.id.et_add_title);
        EditText etLocation = findViewById(R.id.et_add_location);
        EditText etStartDate = findViewById(R.id.et_add_start_date);
        EditText etEndDate = findViewById(R.id.et_add_end_date);
        EditText etNum = findViewById(R.id.et_add_num);
        EditText etContent = findViewById(R.id.et_add_content);
        Button btnSubmit = findViewById(R.id.btn_add_submit);

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

            submitActivityToServer(title, schoolAddress, startDate, endDate,
                    Integer.parseInt(numStr), content);
        });
    }

    private void submitActivityToServer(String title, String schoolAddress,
            String startDate, String endDate, int recruitsNumber,
            String content) {

        String url = ApiConfig.getBaseUrl() + "/activity/create";

        JSONObject json = new JSONObject();
        try {
            json.put("title", title);
            json.put("address", schoolAddress);
            json.put("startDate", startDate);
            json.put("endDate", endDate);
            json.put("recruitsNumber", recruitsNumber);
            json.put("content", content);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.d("AddActivity", "请求体: " + json.toString());

        RequestBody body = RequestBody.create(ApiConfig.JSON, json.toString());
        Request request = new Request.Builder().url(url).post(body).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                    Toast.makeText(AddActivityActivity.this, "网络连接失败", Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                Log.d("AddActivity", "HTTP " + response.code() + " | " + result);
                try {
                    JSONObject res = new JSONObject(result);
                    int code = res.getInt("code");
                    String msg = res.optString("message", "提交成功");
                    runOnUiThread(() -> {
                        Toast.makeText(AddActivityActivity.this, ApiConfig.friendlyMsg(msg), Toast.LENGTH_LONG).show();
                        if (code == 200) finish();
                    });
                } catch (Exception e) {
                    Log.e("AddActivity", "解析失败", e);
                    runOnUiThread(() ->
                        Toast.makeText(AddActivityActivity.this, "提交失败: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
    }
}
