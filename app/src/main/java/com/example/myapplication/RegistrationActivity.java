package com.example.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
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
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RegistrationActivity extends AppCompatActivity {

    private final OkHttpClient client = ApiConfig.getClient();
    private final Gson gson = new Gson();
    private String activityId;
    private Button btnSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        activityId = getIntent().getStringExtra("activity_id");

        EditText etRealName = findViewById(R.id.et_reg_realname);
        EditText etIdCard = findViewById(R.id.et_reg_idcard);
        EditText etPhone = findViewById(R.id.et_reg_phone);
        RadioGroup rgGender = findViewById(R.id.rg_reg_gender);
        RadioButton rbMale = findViewById(R.id.rb_male);
        RadioButton rbFemale = findViewById(R.id.rb_female);
        EditText etEdu = findViewById(R.id.et_reg_edu);
        EditText etHealth = findViewById(R.id.et_reg_health);
        EditText etIntro = findViewById(R.id.et_reg_intro);
        btnSubmit = findViewById(R.id.btn_reg_submit);

        // 先从缓存预填，再从服务端获取完整个人信息
        prefillFromCache(etRealName, etPhone, etIdCard, etEdu, rbMale, rbFemale);
        fetchUserInfoAndFill(etRealName, etPhone, etIdCard, etEdu, rbMale, rbFemale);

        btnSubmit.setOnClickListener(v -> {
            String realName = etRealName.getText().toString().trim();
            String idCard = etIdCard.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String edu = etEdu.getText().toString().trim();
            String health = etHealth.getText().toString().trim();
            String intro = etIntro.getText().toString().trim();

            // 校验必填字段
            if (realName.isEmpty() || idCard.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "请填写姓名、身份证号和手机号码", Toast.LENGTH_SHORT).show();
                return;
            }
            if (idCard.length() != 18) {
                Toast.makeText(this, "请输入正确的18位身份证号", Toast.LENGTH_SHORT).show();
                return;
            }
            if (phone.length() != 11) {
                Toast.makeText(this, "请输入正确的11位手机号码", Toast.LENGTH_SHORT).show();
                return;
            }

            User user = SessionManager.getCurrentUser();
            if (user == null || user.getUserId() == null) {
                Toast.makeText(this, "请重新登录", Toast.LENGTH_SHORT).show();
                return;
            }

            int selectedGenderId = rgGender.getCheckedRadioButtonId();
            String gender = selectedGenderId == R.id.rb_female ? "女" : "男";

            // health → schoolWork (学校/工作单位), intro → 拼接在schoolWork后
            StringBuilder sb = new StringBuilder();
            if (!health.isEmpty()) {
                sb.append(health);
            }
            if (!intro.isEmpty()) {
                if (sb.length() > 0) sb.append("\n");
                sb.append("【自我介绍】").append(intro);
            }
            String schoolWork = sb.toString().trim();

            submitToServer(activityId, realName, phone, idCard, gender, edu, schoolWork);
        });

        // 检查是否已报名
        checkDuplicateRegistration();
    }

    private void prefillFromCache(EditText etRealName, EditText etPhone, EditText etIdCard,
            EditText etEdu, RadioButton rbMale, RadioButton rbFemale) {
        User u = SessionManager.getCurrentUser();
        if (u == null) return;
        if (u.getRealName() != null && !u.getRealName().isEmpty())
            etRealName.setText(u.getRealName());
        if (u.getUserPhone() != null && !u.getUserPhone().isEmpty())
            etPhone.setText(u.getUserPhone());
        if (u.getUserIdentity() != null && !u.getUserIdentity().isEmpty())
            etIdCard.setText(u.getUserIdentity());
        if (u.getUserEdu() != null && !u.getUserEdu().isEmpty())
            etEdu.setText(u.getUserEdu());
        if ("女".equals(u.getUserSex())) rbFemale.setChecked(true);
        else rbMale.setChecked(true);
    }

    private void fetchUserInfoAndFill(EditText etRealName, EditText etPhone, EditText etIdCard,
            EditText etEdu, RadioButton rbMale, RadioButton rbFemale) {
        String url = ApiConfig.getBaseUrl() + "/user/info";
        Request request = new Request.Builder().url(url).get().build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {}

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String body = response.body() != null ? response.body().string() : null;
                    if (body == null) return;
                    JSONObject obj = new JSONObject(body);
                    if (obj.getInt("code") != 200) return;
                    JSONObject data = obj.getJSONObject("data");
                    User u = gson.fromJson(data.toString(), User.class);
                    if (u == null) return;
                    SessionManager.setCurrentUser(u);
                    runOnUiThread(() -> prefillFromCache(etRealName, etPhone, etIdCard, etEdu, rbMale, rbFemale));
                } catch (Exception e) {
                    Log.e("Registration", "Fetch user info error", e);
                }
            }
        });
    }

    private void checkDuplicateRegistration() {
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser == null || currentUser.getUserId() == null) return;

        String url = ApiConfig.getBaseUrl() + "/registration/my";
        Request request = new Request.Builder().url(url).get().build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // 网络失败不阻塞报名流程
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) return;
                try {
                    String body = response.body() != null ? response.body().string() : null;
                    if (body == null) return;
                    JSONObject jsonObject = new JSONObject(body);
                    if (jsonObject.getInt("code") == 200) {
                        Object dataField = jsonObject.get("data");
                        String dataJson;
                        if (dataField instanceof JSONObject) {
                            dataJson = ((JSONObject) dataField).getJSONArray("list").toString();
                        } else {
                            dataJson = dataField.toString();
                        }
                        List<RegistrationRecord> records = gson.fromJson(dataJson,
                                new TypeToken<List<RegistrationRecord>>(){}.getType());
                        for (RegistrationRecord r : records) {
                            if (activityId.equals(r.getActivityId())) {
                                runOnUiThread(() -> {
                                    btnSubmit.setEnabled(false);
                                    btnSubmit.setText("您已报名此活动");
                                    Toast.makeText(RegistrationActivity.this,
                                            "您已报名过此活动，无需重复报名", Toast.LENGTH_LONG).show();
                                });
                                return;
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.e("Registration", "Duplicate check error", e);
                }
            }
        });
    }

    private void submitToServer(String activityId, String realName,
            String phone, String idCard, String gender, String edu, String schoolWork) {

        String url = ApiConfig.getBaseUrl() + "/registration/submit";

        JSONObject json = new JSONObject();
        try {
            json.put("activityId", activityId);
            json.put("realName", realName);
            json.put("phoneNumber", phone);
            json.put("idNumber", idCard);
            json.put("gender", gender);
            json.put("degree", edu);
            json.put("schoolWork", schoolWork);
        } catch (Exception e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(ApiConfig.JSON, json.toString());
        Request request = new Request.Builder().url(url).post(body).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                    Toast.makeText(RegistrationActivity.this, "网络请求失败", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = null;
                try {
                    if (response.body() != null) {
                        result = response.body().string();
                    }
                } catch (IOException e) {
                    Log.e("Registration", "Failed to read body", e);
                }
                final String finalResult = result;
                try {
                    if (finalResult != null) {
                        JSONObject resObj = new JSONObject(finalResult);
                        int code = resObj.has("code") ? resObj.getInt("code") : 500;
                        String msg = resObj.optString("message", "操作完成");
                        runOnUiThread(() -> {
                            Toast.makeText(RegistrationActivity.this, ApiConfig.friendlyMsg(msg), Toast.LENGTH_LONG).show();
                            if (code == 200) {
                                // 同步更新 SessionManager，下次报名时自动预填
                                User u = SessionManager.getCurrentUser();
                                if (u != null) {
                                    u.setUserPhone(phone);
                                    u.setUserIdentity(idCard);
                                    u.setUserSex(gender);
                                    u.setUserEdu(edu);
                                    SessionManager.setCurrentUser(u);
                                }
                                finish();
                            }
                        });
                    }
                } catch (Exception e) {
                    Log.e("Registration", "Parse error", e);
                    runOnUiThread(() ->
                        Toast.makeText(RegistrationActivity.this, "数据解析异常", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

}

