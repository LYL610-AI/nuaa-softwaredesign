package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import org.json.JSONObject;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import android.util.Log;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private final OkHttpClient client = ApiConfig.getClient();
    private final Gson gson = new Gson();
    private boolean smsCountdown;
    private EditText etSmsCode;
    private Button btnSendSms;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ApiConfig.init(this);
        SessionManager.init(this);

        // 已有登录状态，直接跳转
        if (SessionManager.hasLoggedIn()) {
            navigateAfterLogin(SessionManager.getCurrentUser());
            return;
        }

        setContentView(R.layout.activity_main);

        EditText etUsername = findViewById(R.id.et_username);
        EditText etPassword = findViewById(R.id.et_password);
        etSmsCode = findViewById(R.id.et_sms_code);
        btnSendSms = findViewById(R.id.btn_send_sms);
        RadioGroup rgRole = findViewById(R.id.rg_login_role);
        Button btnLogin = findViewById(R.id.btn_login);

        btnSendSms.setOnClickListener(v -> {
            String phone = etUsername.getText().toString().trim();
            if (phone.isEmpty() || phone.length() != 11) {
                Toast.makeText(this, "请先输入正确的11位手机号", Toast.LENGTH_SHORT).show();
                return;
            }
            if (smsCountdown) return;
            sendSmsCode(phone);
        });

        btnLogin.setOnClickListener(v -> {
            String phone = etUsername.getText().toString().trim();
            String pwd = etPassword.getText().toString().trim();
            String smsCode = etSmsCode.getText().toString().trim();

            int checkedId = rgRole.getCheckedRadioButtonId();
            int role;
            boolean isAdmin = checkedId == R.id.rb_admin;
            if (checkedId == R.id.rb_school) {
                role = 2;
            } else if (isAdmin) {
                role = 3;
            } else {
                role = 1;
            }

            if (phone.isEmpty()) {
                Toast.makeText(this, "请输入手机号", Toast.LENGTH_SHORT).show();
                return;
            }

            if (isAdmin) {
                // 管理员：密码登录
                if (pwd.isEmpty()) {
                    Toast.makeText(this, "请输入密码", Toast.LENGTH_SHORT).show();
                    return;
                }
                loginByPassword(phone, pwd, role);
            } else {
                // 志愿者/学校：验证码登录
                if (smsCode.isEmpty()) {
                    Toast.makeText(this, "请输入短信验证码", Toast.LENGTH_SHORT).show();
                    return;
                }
                loginByCode(phone, smsCode, role);
            }
        });

        findViewById(R.id.tv_go_register).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, RegisterActivity.class));
        });

        findViewById(R.id.tv_forgot_pwd).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, ResetPasswordActivity.class));
        });
    }

    private void sendSmsCode(String phone) {
        smsCountdown = true;
        btnSendSms.setEnabled(false);
        btnSendSms.setText("发送中...");

        String url = ApiConfig.getBaseUrl() + "/sms/send";
        JSONObject json = new JSONObject();
        try {
            json.put("phone", phone);
        } catch (Exception e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(ApiConfig.JSON, json.toString());
        Request request = new Request.Builder().url(url).post(body).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("MainActivity", "SMS send failed: " + e.getMessage());
                runOnUiThread(() -> {
                    smsCountdown = false;
                    btnSendSms.setEnabled(true);
                    btnSendSms.setText("获取验证码");
                    Toast.makeText(MainActivity.this, "发送失败，请检查网络", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body() != null ? response.body().string() : null;
                Log.d("MainActivity", "SMS send response: " + result);
                try {
                    JSONObject res = result != null ? new JSONObject(result) : null;
                    int code = res != null ? res.optInt("code", 500) : 500;
                    String msg = res != null ? res.optString("message", "验证码已发送") : "验证码已发送";
                    runOnUiThread(() -> {
                        if (code == 200) {
                            Toast.makeText(MainActivity.this, "验证码已发送", Toast.LENGTH_SHORT).show();
                            startCountdown();
                        } else {
                            smsCountdown = false;
                            btnSendSms.setEnabled(true);
                            btnSendSms.setText("获取验证码");
                            Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (Exception e) {
                    Log.e("MainActivity", "SMS parse error", e);
                    runOnUiThread(() -> {
                        smsCountdown = false;
                        btnSendSms.setEnabled(true);
                        btnSendSms.setText("获取验证码");
                        Toast.makeText(MainActivity.this, "发送失败", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void loginByCode(String phone, String code, int role) {
        String url = ApiConfig.getBaseUrl() + "/user/login";
        Log.d("MainActivity", "Login URL: " + url);

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("phone", phone);
            jsonObject.put("code", code);
            jsonObject.put("role", role);
        } catch (Exception e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(ApiConfig.JSON, jsonObject.toString());
        Request request = new Request.Builder().url(url).post(body).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("MainActivity", "Login failed: " + e.getMessage());
                runOnUiThread(() ->
                        Toast.makeText(MainActivity.this, "网络请求失败", Toast.LENGTH_LONG).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String rawResult = response.body().string();
                Log.d("MainActivity", "Login response: " + rawResult);

                runOnUiThread(() -> {
                    try {
                        JSONObject res = new JSONObject(rawResult);
                        int code = res.getInt("code");

                        if (code == 200) {
                            JSONObject data = res.getJSONObject("data");
                            String token = data.getString("token");
                            User loginUser = gson.fromJson(data.toString(), User.class);

                            if (loginUser != null && loginUser.getUserId() != null) {
                                SessionManager.login(MainActivity.this, loginUser, token);
                                Toast.makeText(MainActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                                navigateAfterLogin(loginUser);
                            } else {
                                Toast.makeText(MainActivity.this, "登录失败：用户数据异常", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            String msg = res.optString("message", "登录失败");
                            Toast.makeText(MainActivity.this, ApiConfig.friendlyMsg(msg), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e("MainActivity", "Parse error", e);
                        Toast.makeText(MainActivity.this, "登录失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void loginByPassword(String phone, String password, int role) {
        String url = ApiConfig.getBaseUrl() + "/user/login";
        Log.d("MainActivity", "Login URL: " + url);

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("phone", phone);
            jsonObject.put("password", password);
            jsonObject.put("role", role);
        } catch (Exception e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(ApiConfig.JSON, jsonObject.toString());
        Request request = new Request.Builder().url(url).post(body).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("MainActivity", "Login failed: " + e.getMessage());
                runOnUiThread(() ->
                        Toast.makeText(MainActivity.this, "网络请求失败", Toast.LENGTH_LONG).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String rawResult = response.body().string();
                Log.d("MainActivity", "Login response: " + rawResult);

                runOnUiThread(() -> {
                    try {
                        JSONObject res = new JSONObject(rawResult);
                        int code = res.getInt("code");

                        if (code == 200) {
                            JSONObject data = res.getJSONObject("data");
                            String token = data.getString("token");
                            User loginUser = gson.fromJson(data.toString(), User.class);

                            if (loginUser != null && loginUser.getUserId() != null) {
                                SessionManager.login(MainActivity.this, loginUser, token);
                                Toast.makeText(MainActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                                navigateAfterLogin(loginUser);
                            } else {
                                Toast.makeText(MainActivity.this, "登录失败：用户数据异常", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            String msg = res.optString("message", "登录失败");
                            Toast.makeText(MainActivity.this, ApiConfig.friendlyMsg(msg), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e("MainActivity", "Parse error", e);
                        Toast.makeText(MainActivity.this, "登录失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void startCountdown() {
        new Thread(() -> {
            for (int i = 60; i >= 0; i--) {
                if (!smsCountdown) return;
                int sec = i;
                runOnUiThread(() -> btnSendSms.setText(sec + "s后重发"));
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    return;
                }
            }
            runOnUiThread(() -> {
                smsCountdown = false;
                btnSendSms.setEnabled(true);
                btnSendSms.setText("获取验证码");
            });
        }).start();
    }

    private void navigateAfterLogin(User user) {
        Intent intent;
        if (user.isAdmin()) {
            intent = new Intent(MainActivity.this, AdminVerifyActivity.class);
        } else if (user.isSchool()) {
            intent = new Intent(MainActivity.this, SchoolHomeActivity.class);
        } else {
            intent = new Intent(MainActivity.this, HomeActivity.class);
        }
        startActivity(intent);
        finish();
    }
}
