package com.example.myapplication;

import android.content.Intent;
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
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ResetPasswordActivity extends AppCompatActivity {

    private final OkHttpClient client = ApiConfig.getClient();

    private EditText etIdNumber, etNewPwd, etConfirmPwd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        etIdNumber = findViewById(R.id.et_reset_phone);
        etNewPwd = findViewById(R.id.et_reset_new_pwd);
        etConfirmPwd = findViewById(R.id.et_reset_confirm_pwd);
        Button btnSubmit = findViewById(R.id.btn_do_reset);
        TextView tvBack = findViewById(R.id.tv_back_login);

        tvBack.setOnClickListener(v -> finish());

        btnSubmit.setOnClickListener(v -> {
            String idNumber = etIdNumber.getText().toString().trim();
            String newPwd = etNewPwd.getText().toString().trim();
            String confirmPwd = etConfirmPwd.getText().toString().trim();

            if (idNumber.isEmpty() || newPwd.isEmpty()) {
                Toast.makeText(this, "身份证号/办学许可证和新密码不能为空", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!newPwd.equals(confirmPwd)) {
                Toast.makeText(this, "两次输入的密码不一致", Toast.LENGTH_SHORT).show();
                return;
            }

            // 根据输入长度判断：18位=身份证号(志愿者)，否则=办学许可证(学校)
            boolean isVolunteer = idNumber.length() == 18;
            resetPasswordOnServer(idNumber, newPwd, isVolunteer);
        });
    }

    private void resetPasswordOnServer(String idNumberOrLicense, String newPassword, boolean isVolunteer) {
        String url = ApiConfig.getBaseUrl() + "/user/recover-password";

        JSONObject json = new JSONObject();
        try {
            if (isVolunteer) {
                json.put("type", "volunteer");
                json.put("idNumber", idNumberOrLicense);
            } else {
                json.put("type", "school");
                json.put("license", idNumberOrLicense);
            }
            json.put("newPassword", newPassword);
        } catch (Exception e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(ApiConfig.JSON, json.toString());
        Request request = new Request.Builder().url(url).post(body).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                    Toast.makeText(ResetPasswordActivity.this, "网络请求失败", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                Log.d("ResetPassword", "HTTP " + response.code() + " | " + result);
                try {
                    JSONObject resObj = new JSONObject(result);
                    int code = resObj.getInt("code");
                    String msg = resObj.optString("message", "操作完成");
                    runOnUiThread(() -> {
                        Toast.makeText(ResetPasswordActivity.this, ApiConfig.friendlyMsg(msg), Toast.LENGTH_SHORT).show();
                        if (code == 200) {
                            Intent intent = new Intent(ResetPasswordActivity.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();
                        }
                    });
                } catch (Exception e) {
                    Log.e("ResetPassword", "解析失败", e);
                    runOnUiThread(() ->
                        Toast.makeText(ResetPasswordActivity.this, "重置失败", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }
}

