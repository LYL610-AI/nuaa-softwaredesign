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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ApiConfig.init(this);

        EditText etUsername = findViewById(R.id.et_username);
        EditText etPassword = findViewById(R.id.et_password);
        RadioGroup rgRole = findViewById(R.id.rg_login_role);
        Button btnLogin = findViewById(R.id.btn_login);

        btnLogin.setOnClickListener(v -> {
            String userId = etUsername.getText().toString().trim();
            String pwd = etPassword.getText().toString().trim();

            if (userId.isEmpty() || pwd.isEmpty()) {
                Toast.makeText(this, "手机号或密码不能为空", Toast.LENGTH_SHORT).show();
                return;
            }

            int checkedId = rgRole.getCheckedRadioButtonId();
            int role;
            if (checkedId == R.id.rb_school) {
                role = 2;
            } else if (checkedId == R.id.rb_admin) {
                role = 3;
            } else {
                role = 1;
            }

            loginFromServer(userId, pwd, role);
        });

        findViewById(R.id.tv_go_register).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, RegisterActivity.class));
        });

        findViewById(R.id.tv_forgot_pwd).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, ResetPasswordActivity.class));
        });
    }

    private void loginFromServer(String phone, String password, int role) {
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
