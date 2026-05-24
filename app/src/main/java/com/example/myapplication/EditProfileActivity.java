package com.example.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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

public class EditProfileActivity extends AppCompatActivity {

    private final OkHttpClient client = ApiConfig.getClient();
    private boolean isAdminMode;
    private String targetUserId;
    private String targetPermission;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        EditText etPhone = findViewById(R.id.et_edit_phone);
        EditText etOldPassword = findViewById(R.id.et_edit_old_password);
        EditText etPassword = findViewById(R.id.et_edit_password);
        EditText etPasswordConfirm = findViewById(R.id.et_edit_password_confirm);
        EditText etIdentity = findViewById(R.id.et_edit_identity);
        EditText etRealName = findViewById(R.id.et_edit_realname);
        RadioGroup rgGender = findViewById(R.id.rg_edit_gender);
        RadioButton rbMale = findViewById(R.id.rb_edit_male);
        RadioButton rbFemale = findViewById(R.id.rb_edit_female);
        EditText etEdu = findViewById(R.id.et_edit_edu);
        EditText etSchoolName = findViewById(R.id.et_edit_school_name);
        EditText etSchoolAddress = findViewById(R.id.et_edit_school_address);
        EditText etSchoolType = findViewById(R.id.et_edit_school_type);
        EditText etSchoolLicense = findViewById(R.id.et_edit_school_license);
        EditText etSchoolPrinciple = findViewById(R.id.et_edit_school_principle);
        View layoutIdentity = findViewById(R.id.layout_edit_identity);
        View layoutRealName = findViewById(R.id.layout_edit_realname);
        View layoutGender = findViewById(R.id.layout_edit_gender);
        View layoutEdu = findViewById(R.id.layout_edit_edu);
        View layoutSchoolName = findViewById(R.id.layout_edit_school_name);
        View layoutSchoolAddress = findViewById(R.id.layout_edit_school_address);
        View layoutSchoolType = findViewById(R.id.layout_edit_school_type);
        View layoutSchoolLicense = findViewById(R.id.layout_edit_school_license);
        View layoutSchoolPrinciple = findViewById(R.id.layout_edit_school_principle);
        TextView tvTarget = findViewById(R.id.tv_edit_target_user);
        Button btnSave = findViewById(R.id.btn_save_profile);

        // 判断模式：管理员编辑他人 / 自己编辑自己
        isAdminMode = getIntent().getBooleanExtra("admin_mode", false);
        User currentUser = SessionManager.getCurrentUser();

        if (isAdminMode) {
            // 管理员编辑指定用户
            targetUserId = getIntent().getStringExtra("target_user_id");
            targetPermission = getIntent().getStringExtra("target_permission");
            String targetPhone = getIntent().getStringExtra("target_phone");
            String targetRealName = getIntent().getStringExtra("target_real_name");
            String targetIdentity = getIntent().getStringExtra("target_identity");
            String targetSex = getIntent().getStringExtra("target_sex");
            String targetEdu = getIntent().getStringExtra("target_edu");
            String targetSchoolName = getIntent().getStringExtra("target_school_name");
            String targetSchoolAddress = getIntent().getStringExtra("target_school_address");
            String targetType = getIntent().getStringExtra("target_type");
            String targetLicense = getIntent().getStringExtra("target_license");
            String targetPrinciple = getIntent().getStringExtra("target_principle");

            tvTarget.setVisibility(View.VISIBLE);
            tvTarget.setText("正在编辑用户: " + targetUserId + " (" + getPermissionLabel(targetPermission) + ")");

            etPhone.setText(targetPhone != null ? targetPhone : "");
            etRealName.setText(targetRealName != null ? targetRealName : "");
            etIdentity.setText(targetIdentity != null ? targetIdentity : "");
            etEdu.setText(targetEdu != null ? targetEdu : "");
            etSchoolName.setText(targetSchoolName != null ? targetSchoolName : "");
            etSchoolAddress.setText(targetSchoolAddress != null ? targetSchoolAddress : "");
            etSchoolType.setText(targetType != null ? targetType : "");
            etSchoolLicense.setText(targetLicense != null ? targetLicense : "");
            etSchoolPrinciple.setText(targetPrinciple != null ? targetPrinciple : "");
            if ("女".equals(targetSex)) {
                rbFemale.setChecked(true);
            } else {
                rbMale.setChecked(true);
            }

            showFieldsForPermission(layoutIdentity, layoutRealName, layoutGender, layoutEdu,
                    layoutSchoolName, layoutSchoolAddress, layoutSchoolType,
                    layoutSchoolLicense, layoutSchoolPrinciple, targetPermission);

        } else {
            // 自己编辑自己的信息
            if (currentUser == null) {
                Toast.makeText(this, "请重新登录", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            targetUserId = currentUser.getUserId();
            targetPermission = currentUser.getUserPermission();

            etPhone.setText(currentUser.getUserPhone() != null ? currentUser.getUserPhone() : "");
            etRealName.setText(currentUser.getRealName() != null ? currentUser.getRealName() : "");
            etIdentity.setText(currentUser.getUserIdentity() != null ? currentUser.getUserIdentity() : "");
            etIdentity.setEnabled(false); // 身份证号不可修改
            etEdu.setText(currentUser.getUserEdu() != null ? currentUser.getUserEdu() : "");
            etSchoolName.setText(currentUser.getSchoolName() != null ? currentUser.getSchoolName() : "");
            etSchoolAddress.setText(currentUser.getSchoolAddress() != null ? currentUser.getSchoolAddress() : "");
            etSchoolType.setText(currentUser.getType() != null ? currentUser.getType() : "");
            etSchoolLicense.setText(currentUser.getLicense() != null ? currentUser.getLicense() : "");
            etSchoolPrinciple.setText(currentUser.getPrinciple() != null ? currentUser.getPrinciple() : "");
            if ("女".equals(currentUser.getUserSex())) {
                rbFemale.setChecked(true);
            } else {
                rbMale.setChecked(true);
            }

            showFieldsForPermission(layoutIdentity, layoutRealName, layoutGender, layoutEdu,
                    layoutSchoolName, layoutSchoolAddress, layoutSchoolType,
                    layoutSchoolLicense, layoutSchoolPrinciple, targetPermission);

            // 从服务器获取完整用户信息并预填所有字段
            fetchUserProfileFromServer(currentUser.getUserId(),
                    etPhone, etRealName, etIdentity, rgGender, rbMale, rbFemale, etEdu,
                    etSchoolName, etSchoolAddress, etSchoolType, etSchoolLicense, etSchoolPrinciple);
        }

        btnSave.setOnClickListener(v -> {
            String phone = etPhone.getText().toString().trim();
            String oldPassword = etOldPassword.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String passwordConfirm = etPasswordConfirm.getText().toString().trim();
            String realName = etRealName.getText().toString().trim();
            String identity = etIdentity.getText().toString().trim();
            int selectedGenderId = rgGender.getCheckedRadioButtonId();
            String gender = selectedGenderId == R.id.rb_edit_female ? "女" : "男";
            String edu = etEdu.getText().toString().trim();
            String schoolName = etSchoolName.getText().toString().trim();
            String schoolAddress = etSchoolAddress.getText().toString().trim();
            String schoolType = etSchoolType.getText().toString().trim();
            String schoolLicense = etSchoolLicense.getText().toString().trim();
            String schoolPrinciple = etSchoolPrinciple.getText().toString().trim();

            if (phone.isEmpty()) {
                Toast.makeText(this, "手机号码不能为空", Toast.LENGTH_SHORT).show();
                return;
            }
            if (phone.length() != 11) {
                Toast.makeText(this, "请输入正确的11位手机号码", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!password.isEmpty()) {
                if (oldPassword.isEmpty()) {
                    Toast.makeText(this, "修改密码需要输入旧密码", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (password.length() < 6) {
                    Toast.makeText(this, "密码至少6位", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!password.equals(passwordConfirm)) {
                    Toast.makeText(this, "两次密码输入不一致", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            saveProfile(targetUserId, targetPermission, phone, oldPassword, password,
                    realName, identity, gender, edu,
                    schoolName, schoolAddress, schoolType, schoolLicense, schoolPrinciple,
                    isAdminMode);
        });
    }

    private void showFieldsForPermission(View layoutIdentity, View layoutRealName, View layoutGender, View layoutEdu,
            View layoutSchoolName, View layoutSchoolAddress, View layoutSchoolType,
            View layoutSchoolLicense, View layoutSchoolPrinciple, String permission) {
        boolean isVolunteer = "1".equals(permission);
        boolean isSchool = "2".equals(permission);

        layoutIdentity.setVisibility(isVolunteer ? View.VISIBLE : View.GONE);
        layoutRealName.setVisibility(isVolunteer ? View.VISIBLE : View.GONE);
        layoutGender.setVisibility(isVolunteer ? View.VISIBLE : View.GONE);
        layoutEdu.setVisibility(isVolunteer ? View.VISIBLE : View.GONE);
        layoutSchoolName.setVisibility(isSchool ? View.VISIBLE : View.GONE);
        layoutSchoolAddress.setVisibility(isSchool ? View.VISIBLE : View.GONE);
        layoutSchoolType.setVisibility(isSchool ? View.VISIBLE : View.GONE);
        layoutSchoolLicense.setVisibility(isSchool ? View.VISIBLE : View.GONE);
        layoutSchoolPrinciple.setVisibility(isSchool ? View.VISIBLE : View.GONE);
    }

    private String getPermissionLabel(String permission) {
        if ("1".equals(permission)) return "志愿者";
        if ("2".equals(permission)) return "学校";
        if ("3".equals(permission)) return "管理员";
        return "未知";
    }

    private void saveProfile(String userId, String permission, String phone,
            String oldPassword, String newPassword,
            String realName, String identity, String gender, String edu,
            String schoolName, String schoolAddress, String schoolType, String schoolLicense,
            String schoolPrinciple, boolean adminMode) {

        if (!newPassword.isEmpty()) {
            if (adminMode) {
                // 管理员重置用户密码：调用 /user/reset-password/{userId}
                adminResetPasswordThenUpdate(userId, permission, phone, newPassword,
                        realName, identity, gender, edu,
                        schoolName, schoolAddress, schoolType, schoolLicense, schoolPrinciple);
            } else {
                changePasswordThenUpdate(userId, permission, phone, oldPassword, newPassword,
                        realName, identity, gender, edu,
                        schoolName, schoolAddress, schoolType, schoolLicense, schoolPrinciple);
            }
        } else {
            doUpdateProfile(userId, permission, phone, null,
                    realName, identity, gender, edu,
                    schoolName, schoolAddress, schoolType, schoolLicense, schoolPrinciple, adminMode);
        }
    }

    private void changePasswordThenUpdate(String userId, String permission, String phone,
            String oldPwd, String newPwd,
            String realName, String identity, String gender, String edu,
            String schoolName, String schoolAddress, String schoolType, String schoolLicense,
            String schoolPrinciple) {

        String url = ApiConfig.getBaseUrl() + "/user/password";

        JSONObject json = new JSONObject();
        try {
            json.put("oldPwd", oldPwd);
            json.put("newPwd", newPwd);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        Log.d("EditProfile", "Change password request: " + json.toString());

        RequestBody body = RequestBody.create(ApiConfig.JSON, json.toString());
        Request request = new Request.Builder().url(url).put(body).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("EditProfile", "Password change failed, url=" + url, e);
                runOnUiThread(() ->
                    Toast.makeText(EditProfileActivity.this, "网络请求失败: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }

            @Override
            public void onResponse(Call call, Response response) {
                String result = null;
                try {
                    if (response.body() != null) result = response.body().string();
                } catch (IOException e) {
                    Log.e("EditProfile", "Failed to read body", e);
                }
                Log.d("EditProfile", "Password change response code=" + response.code() + ", body=" + result);

                boolean pwdOk = false;
                String pwdMsg = "密码修改失败";
                if (result != null) {
                    try {
                        JSONObject resObj = new JSONObject(result);
                        if (response.code() == 200 && resObj.optInt("code") == 200) {
                            pwdOk = true;
                        } else {
                            pwdMsg = resObj.optString("message",
                                    resObj.optString("error", "密码修改失败"));
                        }
                    } catch (Exception e) {
                        Log.e("EditProfile", "Parse error", e);
                    }
                }

                if (!pwdOk) {
                    final String finalMsg = pwdMsg;
                    runOnUiThread(() ->
                        Toast.makeText(EditProfileActivity.this, ApiConfig.friendlyMsg(finalMsg), Toast.LENGTH_SHORT).show());
                    return;
                }

                // 密码修改成功后，继续更新个人信息
                doUpdateProfile(userId, permission, phone, null,
                        realName, identity, gender, edu,
                        schoolName, schoolAddress, schoolType, schoolLicense, schoolPrinciple, false);
            }
        });
    }

    private void adminResetPasswordThenUpdate(String userId, String permission, String phone,
            String newPassword,
            String realName, String identity, String gender, String edu,
            String schoolName, String schoolAddress, String schoolType, String schoolLicense,
            String schoolPrinciple) {

        String url = ApiConfig.getBaseUrl() + "/user/reset-password/" + userId;

        JSONObject json = new JSONObject();
        try {
            json.put("permission", permission);
            json.put("newPassword", newPassword);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        Log.d("EditProfile", "Admin reset password request: " + json.toString());

        RequestBody body = RequestBody.create(ApiConfig.JSON, json.toString());
        Request request = new Request.Builder().url(url).put(body).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("EditProfile", "Admin reset password failed, url=" + url, e);
                runOnUiThread(() ->
                    Toast.makeText(EditProfileActivity.this, "网络请求失败: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }

            @Override
            public void onResponse(Call call, Response response) {
                String result = null;
                try {
                    if (response.body() != null) result = response.body().string();
                } catch (IOException e) {
                    Log.e("EditProfile", "Failed to read body", e);
                }
                Log.d("EditProfile", "Admin reset password response code=" + response.code() + ", body=" + result);

                boolean pwdOk = false;
                String pwdMsg = "密码重置失败";
                if (result != null) {
                    try {
                        JSONObject resObj = new JSONObject(result);
                        if (response.code() == 200 && resObj.optInt("code") == 200) {
                            pwdOk = true;
                        } else {
                            pwdMsg = resObj.optString("message",
                                    resObj.optString("error", "密码重置失败"));
                        }
                    } catch (Exception e) {
                        Log.e("EditProfile", "Parse error", e);
                    }
                }

                if (!pwdOk) {
                    final String finalMsg = pwdMsg;
                    runOnUiThread(() ->
                        Toast.makeText(EditProfileActivity.this, ApiConfig.friendlyMsg(finalMsg), Toast.LENGTH_SHORT).show());
                    return;
                }

                // 密码重置成功后，继续更新个人信息
                doUpdateProfile(userId, permission, phone, null,
                        realName, identity, gender, edu,
                        schoolName, schoolAddress, schoolType, schoolLicense, schoolPrinciple, true);
            }
        });
    }

    private void doUpdateProfile(String userId, String permission, String phone, String password,
            String realName, String identity, String gender, String edu,
            String schoolName, String schoolAddress, String schoolType, String schoolLicense,
            String schoolPrinciple, boolean adminMode) {

        if (userId == null || userId.isEmpty()) {
            runOnUiThread(() ->
                Toast.makeText(EditProfileActivity.this, "用户ID为空，无法保存", Toast.LENGTH_SHORT).show());
            return;
        }

        // 管理员和自助编辑共用 /user/update，服务器通过 token 区分权限，通过 body 中的 userId 识别目标用户
        String url = ApiConfig.getBaseUrl() + "/user/update";

        JSONObject json = new JSONObject();
        try {
            json.put("userId", userId);
            json.put("userPermission", permission);
            json.put("userPhone", phone);
            if (!realName.isEmpty()) json.put("userName", realName);
            if (!identity.isEmpty()) json.put("userIdentity", identity);
            if (!gender.isEmpty()) json.put("userSex", gender);
            if (!edu.isEmpty()) json.put("userEdu", edu);
            if (!schoolName.isEmpty()) json.put("schoolName", schoolName);
            if (!schoolAddress.isEmpty()) json.put("address", schoolAddress);
            if (!schoolType.isEmpty()) json.put("type", schoolType);
            if (!schoolLicense.isEmpty()) json.put("license", schoolLicense);
            if (!schoolPrinciple.isEmpty()) json.put("principle", schoolPrinciple);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.d("EditProfile", "Save request: " + json.toString());

        RequestBody body = RequestBody.create(ApiConfig.JSON, json.toString());
        Request request = new Request.Builder().url(url).put(body).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("EditProfile", "Save failed, url=" + url + ", body=" + json.toString(), e);
                runOnUiThread(() ->
                    Toast.makeText(EditProfileActivity.this, "网络请求失败: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }

            @Override
            public void onResponse(Call call, Response response) {
                String result = null;
                try {
                    if (response.body() != null) {
                        result = response.body().string();
                    }
                } catch (IOException e) {
                    Log.e("EditProfile", "Failed to read body", e);
                }
                Log.d("EditProfile", "Save response code=" + response.code() + ", body=" + result);
                final String finalResult = result;
                final int httpCode = response.code();
                runOnUiThread(() -> {
                    if (finalResult == null) {
                        Toast.makeText(EditProfileActivity.this,
                                "服务器返回异常", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    try {
                        JSONObject resObj = new JSONObject(finalResult);
                        int code = resObj.has("code") ? resObj.getInt("code") : 500;
                        String msg = resObj.optString("message", "保存成功");
                        if (httpCode == 200 && code == 200) {
                            Toast.makeText(EditProfileActivity.this, ApiConfig.friendlyMsg(msg), Toast.LENGTH_SHORT).show();
                            if (!adminMode) {
                                updateSessionUser(phone, realName, identity, gender, edu, schoolName,
                                        schoolAddress, schoolType, schoolLicense, schoolPrinciple);
                            }
                            finish();
                        } else {
                            String errMsg = resObj.optString("message",
                                    resObj.optString("error", "操作失败"));
                            Toast.makeText(EditProfileActivity.this, ApiConfig.friendlyMsg(errMsg), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e("EditProfile", "Parse error", e);
                        Toast.makeText(EditProfileActivity.this, "操作失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void fetchUserProfileFromServer(String userId,
            EditText etPhone, EditText etRealName, EditText etIdentity,
            RadioGroup rgGender, RadioButton rbMale, RadioButton rbFemale, EditText etEdu,
            EditText etSchoolName, EditText etSchoolAddress, EditText etSchoolType,
            EditText etSchoolLicense, EditText etSchoolPrinciple) {
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
                    com.google.gson.Gson gson = new com.google.gson.Gson();
                    User u = gson.fromJson(data.toString(), User.class);
                    if (u == null) return;
                    SessionManager.setCurrentUser(u);
                    runOnUiThread(() -> {
                        if (u.getUserPhone() != null && !u.getUserPhone().isEmpty())
                            etPhone.setText(u.getUserPhone());
                        if (u.getRealName() != null && !u.getRealName().isEmpty())
                            etRealName.setText(u.getRealName());
                        if (u.getUserIdentity() != null && !u.getUserIdentity().isEmpty())
                            etIdentity.setText(u.getUserIdentity());
                        if (u.getUserEdu() != null && !u.getUserEdu().isEmpty())
                            etEdu.setText(u.getUserEdu());
                        if (u.getUserSex() != null) {
                            if ("女".equals(u.getUserSex())) rbFemale.setChecked(true);
                            else rbMale.setChecked(true);
                        }
                        if (u.getSchoolName() != null && !u.getSchoolName().isEmpty())
                            etSchoolName.setText(u.getSchoolName());
                        if (u.getSchoolAddress() != null && !u.getSchoolAddress().isEmpty())
                            etSchoolAddress.setText(u.getSchoolAddress());
                        if (u.getType() != null && !u.getType().isEmpty())
                            etSchoolType.setText(u.getType());
                        if (u.getLicense() != null && !u.getLicense().isEmpty())
                            etSchoolLicense.setText(u.getLicense());
                        if (u.getPrinciple() != null && !u.getPrinciple().isEmpty())
                            etSchoolPrinciple.setText(u.getPrinciple());
                    });
                } catch (Exception e) {
                    Log.e("EditProfile", "Fetch profile error", e);
                }
            }
        });
    }

    private void updateSessionUser(String phone, String realName, String identity, String gender, String edu,
            String schoolName, String schoolAddress, String schoolType, String schoolLicense,
            String schoolPrinciple) {
        User user = SessionManager.getCurrentUser();
        if (user != null) {
            user.setUserPhone(phone);
            user.setRealName(realName);
            user.setUserIdentity(identity);
            user.setUserSex(gender);
            user.setUserEdu(edu);
            if (schoolName != null) user.setSchoolName(schoolName);
            if (schoolAddress != null) user.setSchoolAddress(schoolAddress);
            if (schoolType != null) user.setType(schoolType);
            if (schoolLicense != null) user.setLicense(schoolLicense);
            if (schoolPrinciple != null) user.setPrinciple(schoolPrinciple);
            SessionManager.setCurrentUser(user);
        }
    }
}

