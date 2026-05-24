package com.example.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RegisterActivity extends AppCompatActivity {

    private final OkHttpClient client = ApiConfig.getClient();
    private final Gson gson = new Gson();

    private EditText etPhone, etPwd, etConfirmPwd;
    private RadioGroup rgRole;
    private LinearLayout layoutVolunteer, layoutSchool;
    // volunteer fields
    private EditText etUserName, etUserEdu, etIdNumber;
    private RadioGroup rgGender;
    // school fields
    private EditText etSchoolName, etSchoolLicense, etSchoolAddress, etSchoolType;
    private EditText etLeaderName;

    private boolean isSchool = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // common
        etPhone = findViewById(R.id.reg_phone);
        etPwd = findViewById(R.id.reg_pwd);
        etConfirmPwd = findViewById(R.id.reg_pwd_confirm);
        rgRole = findViewById(R.id.rg_register_role);

        // sections
        layoutVolunteer = findViewById(R.id.layout_volunteer_fields);
        layoutSchool = findViewById(R.id.layout_school_fields);

        // volunteer
        etUserName = findViewById(R.id.reg_user_name);
        etUserEdu = findViewById(R.id.reg_user_edu);
        rgGender = findViewById(R.id.rg_gender);
        etIdNumber = findViewById(R.id.reg_id_number);

        // school
        etSchoolName = findViewById(R.id.reg_school_name);
        etSchoolLicense = findViewById(R.id.reg_school_license);
        etSchoolAddress = findViewById(R.id.reg_school_address);
        etSchoolType = findViewById(R.id.reg_school_type);
        etLeaderName = findViewById(R.id.reg_leader_name);

        // 切换角色时显示/隐藏对应字段
        rgRole.setOnCheckedChangeListener((group, checkedId) -> {
            isSchool = (checkedId == R.id.rb_reg_school);
            layoutVolunteer.setVisibility(isSchool ? View.GONE : View.VISIBLE);
            layoutSchool.setVisibility(isSchool ? View.VISIBLE : View.GONE);
        });

        Button btnSubmit = findViewById(R.id.btn_do_register);
        btnSubmit.setOnClickListener(v -> submitRegister());
    }

    private void submitRegister() {
        String phone = etPhone.getText().toString().trim();
        String pwd = etPwd.getText().toString().trim();
        String confirmPwd = etConfirmPwd.getText().toString().trim();

        // 公共校验
        if (phone.isEmpty() || pwd.isEmpty()) {
            Toast.makeText(this, "手机号和密码不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        if (phone.length() != 11) {
            Toast.makeText(this, "请输入正确的11位手机号", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!pwd.equals(confirmPwd)) {
            Toast.makeText(this, "两次输入的密码不一致，请重新输入", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject json = new JSONObject();
        try {
            json.put("userPhone", phone);
            json.put("password", pwd);
            json.put("userPermission", isSchool ? 2 : 1);

            if (isSchool) {
                String schoolName = etSchoolName.getText().toString().trim();
                String schoolLicense = etSchoolLicense.getText().toString().trim();
                String schoolAddress = etSchoolAddress.getText().toString().trim();
                String schoolType = etSchoolType.getText().toString().trim();
                String leaderName = etLeaderName.getText().toString().trim();

                if (schoolName.isEmpty() || schoolLicense.isEmpty() || schoolAddress.isEmpty()
                        || schoolType.isEmpty() || leaderName.isEmpty()) {
                    Toast.makeText(this, "请填写完整的学校和负责人信息", Toast.LENGTH_SHORT).show();
                    return;
                }
                json.put("schoolName", schoolName);
                json.put("principle", leaderName);
                json.put("license", schoolLicense);
                json.put("type", schoolType);
                json.put("address", schoolAddress);
            } else {
                String realName = etUserName.getText().toString().trim();
                String edu = etUserEdu.getText().toString().trim();
                String idNumber = etIdNumber.getText().toString().trim();
                String sex = rgGender.getCheckedRadioButtonId() == R.id.rb_male ? "男" : "女";

                if (realName.isEmpty() || edu.isEmpty() || idNumber.isEmpty()) {
                    Toast.makeText(this, "请填写姓名、学历和身份证号", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (idNumber.length() != 18) {
                    Toast.makeText(this, "请输入正确的18位身份证号", Toast.LENGTH_SHORT).show();
                    return;
                }
                json.put("userName", realName);
                json.put("userEdu", edu);
                json.put("idNumber", idNumber);
                json.put("userSex", sex);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 使用专用端点检查身份证号/办学许可证是否已被注册
        checkDuplicateThenRegister(json, isSchool);
    }

    private void checkDuplicateThenRegister(JSONObject registerJson, boolean isSchoolCheck) {
        String checkingTarget = isSchoolCheck
                ? etSchoolLicense.getText().toString().trim()
                : etIdNumber.getText().toString().trim();

        String checkPath = isSchoolCheck
                ? "/user/checkLicense?license=" + checkingTarget
                : "/user/checkIdNumber?idNumber=" + checkingTarget;
        String url = ApiConfig.getBaseUrl() + checkPath;

        Request request = new Request.Builder().url(url).get().build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                registerOnServer(registerJson);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                boolean duplicate = false;
                try {
                    String body = response.body() != null ? response.body().string() : null;
                    if (body != null) {
                        JSONObject obj = new JSONObject(body);
                        if (obj.getInt("code") == 200) {
                            duplicate = obj.optBoolean("data", false);
                        }
                    }
                } catch (Exception e) {
                    Log.e("RegisterActivity", "Duplicate check error", e);
                }

                if (duplicate) {
                    String msg = isSchoolCheck ? "该办学许可证已被注册使用" : "该身份证号已被注册使用";
                    runOnUiThread(() ->
                        Toast.makeText(RegisterActivity.this, msg, Toast.LENGTH_LONG).show());
                } else {
                    registerOnServer(registerJson);
                }
            }
        });
    }

    private void registerOnServer(JSONObject json) {
        String url = ApiConfig.getBaseUrl() + "/user/register";
        Log.d("RegisterActivity", "请求体: " + json.toString());

        RequestBody body = RequestBody.create(ApiConfig.JSON, json.toString());
        Request request = new Request.Builder().url(url).post(body).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                    Toast.makeText(RegisterActivity.this, "网络请求失败，请检查服务器", Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                Log.d("RegisterActivity", "HTTP " + response.code() + " | 服务器返回: " + result);
                try {
                    JSONObject resObj = new JSONObject(result);
                    if (resObj.has("code")) {
                        int code = resObj.getInt("code");
                        String msg = resObj.optString("message", "注册成功");
                        runOnUiThread(() -> {
                            Toast.makeText(RegisterActivity.this, ApiConfig.friendlyMsg(msg), Toast.LENGTH_SHORT).show();
                            if (code == 200) finish();
                        });
                    } else {
                        String error = resObj.optString("error", "服务器内部错误");
                        int status = resObj.optInt("status", response.code());
                        runOnUiThread(() ->
                            Toast.makeText(RegisterActivity.this, "服务器错误(" + status + "): " + error, Toast.LENGTH_LONG).show()
                        );
                    }
                } catch (Exception e) {
                    Log.e("RegisterActivity", "注册解析失败", e);
                    runOnUiThread(() ->
                        Toast.makeText(RegisterActivity.this, "注册失败: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
    }
}

