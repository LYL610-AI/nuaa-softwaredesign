package com.example.myapplication;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class EditActivityActivity extends AppCompatActivity {

    private final okhttp3.OkHttpClient client = ApiConfig.getClient();
    private String activityId;
    private Uri selectedImageUri;
    private ImageView ivPreview;
    private Button btnSelectImage, btnClearImage, btnSubmit;
    private ActivityResultLauncher<String> imagePickerLauncher;
    private boolean imageCleared;

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
        btnSubmit = findViewById(R.id.btn_edit_submit);
        ivPreview = findViewById(R.id.iv_edit_preview);
        btnSelectImage = findViewById(R.id.btn_select_edit_image);
        btnClearImage = findViewById(R.id.btn_clear_edit_image);

        // 显示当前封面图片
        String existingUrl = ApiConfig.getFullImageUrl(data.getPictureUrl());
        if (existingUrl != null && !existingUrl.isEmpty()) {
            ivPreview.setVisibility(View.VISIBLE);
            btnClearImage.setVisibility(View.VISIBLE);
            ImageLoader.load(existingUrl, ivPreview);
        }

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;
                        imageCleared = false;
                        ivPreview.setImageURI(uri);
                        ivPreview.setVisibility(View.VISIBLE);
                        btnClearImage.setVisibility(View.VISIBLE);
                    }
                });

        btnSelectImage.setOnClickListener(v ->
                imagePickerLauncher.launch("image/*"));

        btnClearImage.setOnClickListener(v -> {
            selectedImageUri = null;
            imageCleared = true;
            ivPreview.setImageURI(null);
            ivPreview.setVisibility(View.GONE);
            btnClearImage.setVisibility(View.GONE);
        });

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
            btnSelectImage.setEnabled(false);
            btnClearImage.setEnabled(false);
            btnSubmit.setEnabled(false);
            btnSubmit.setText("活动已结束");
            Toast.makeText(this, "该活动已结束，无法修改信息", Toast.LENGTH_LONG).show();
        } else if (!"待审核".equals(data.getAuditState())) {
            etTitle.setEnabled(false);
            etLocation.setEnabled(false);
            etStartDate.setEnabled(false);
            etEndDate.setEnabled(false);
            etNum.setEnabled(false);
            etContent.setEnabled(false);
            btnSelectImage.setEnabled(false);
            btnClearImage.setEnabled(false);
            btnSubmit.setEnabled(false);
            btnSubmit.setText("仅待审核可编辑");
            Toast.makeText(this, "仅待审核的活动可以编辑", Toast.LENGTH_LONG).show();
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

            btnSubmit.setEnabled(false);
            if (selectedImageUri != null) {
                btnSubmit.setText("上传封面图片中...");
                uploadImageThenUpdate(title, schoolAddress, startDate, endDate,
                        Integer.parseInt(numStr), content, currentUser.getUserId());
            } else {
                btnSubmit.setText("保存中...");
                submitUpdate(title, schoolAddress, startDate, endDate,
                        Integer.parseInt(numStr), content, currentUser.getUserId(), null);
            }
        });
    }

    private void uploadImageThenUpdate(String title, String schoolAddress,
            String startDate, String endDate, int recruitsNumber,
            String content, String userId) {
        new Thread(() -> {
            try {
                File imageFile = copyUriToFile(selectedImageUri);
                if (imageFile == null) {
                    runOnUiThread(() -> {
                        btnSubmit.setEnabled(true);
                        btnSubmit.setText("保存修改（需重新审核）");
                        Toast.makeText(EditActivityActivity.this, "无法读取图片文件", Toast.LENGTH_SHORT).show();
                    });
                    return;
                }

                RequestBody fileBody = RequestBody.create(MediaType.parse("image/jpeg"), imageFile);
                MultipartBody body = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("file", imageFile.getName(), fileBody)
                        .build();

                String url = ApiConfig.getBaseUrl() + "/file/upload";
                Request request = new Request.Builder().url(url).post(body).build();
                Response response = client.newCall(request).execute();

                String result = response.body() != null ? response.body().string() : null;
                Log.d("EditActivity", "Upload response code=" + response.code() + ", body=" + result);
                if (result != null && response.isSuccessful()) {
                    JSONObject res = new JSONObject(result);
                    if (res.getInt("code") == 200) {
                        String uploadedUrl = res.getJSONObject("data").getString("url");
                        imageFile.delete();
                        final String finalUrl = uploadedUrl;
                        runOnUiThread(() -> {
                            btnSubmit.setText("保存中...");
                            submitUpdate(title, schoolAddress, startDate, endDate,
                                    recruitsNumber, content, userId, finalUrl);
                        });
                        return;
                    }
                }
                imageFile.delete();
                final String errDetail = result != null ? result : "HTTP " + response.code();
                Log.e("EditActivity", "Upload failed: " + errDetail);
                runOnUiThread(() -> {
                    btnSubmit.setEnabled(true);
                    btnSubmit.setText("保存修改（需重新审核）");
                    Toast.makeText(EditActivityActivity.this, "图片上传失败: " + errDetail, Toast.LENGTH_LONG).show();
                });
            } catch (Exception e) {
                Log.e("EditActivity", "Image upload error", e);
                runOnUiThread(() -> {
                    btnSubmit.setEnabled(true);
                    btnSubmit.setText("保存修改（需重新审核）");
                    Toast.makeText(EditActivityActivity.this, "图片上传失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private File copyUriToFile(Uri uri) throws IOException {
        InputStream in = getContentResolver().openInputStream(uri);
        if (in == null) return null;
        File file = new File(getCacheDir(), "upload_" + System.currentTimeMillis() + ".jpg");
        FileOutputStream fos = new FileOutputStream(file);
        byte[] buf = new byte[8192];
        int len;
        while ((len = in.read(buf)) > 0) {
            fos.write(buf, 0, len);
        }
        fos.close();
        in.close();
        return file;
    }

    private void submitUpdate(String title, String schoolAddress,
            String startDate, String endDate, int recruitsNumber,
            String content, String userId, String pictureUrl) {

        String url = ApiConfig.getBaseUrl() + "/activity/update/" + activityId;

        JSONObject json = new JSONObject();
        try {
            json.put("title", title);
            json.put("schoolAddress", schoolAddress);
            json.put("startDate", startDate);
            json.put("endDate", endDate);
            json.put("recruitsNumber", recruitsNumber);
            json.put("content", content);
            json.put("activityState", "0");
            json.put("auditState", "0");
            if (imageCleared) {
                json.put("pictureUrl", "");
            } else if (pictureUrl != null && !pictureUrl.isEmpty()) {
                json.put("pictureUrl", pictureUrl);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.e("EditActivity", "请求体: " + json.toString());

        RequestBody body = RequestBody.create(ApiConfig.JSON, json.toString());
        Request request = new Request.Builder().url(url).put(body).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    btnSubmit.setEnabled(true);
                    btnSubmit.setText("保存修改（需重新审核）");
                    Toast.makeText(EditActivityActivity.this, "网络连接失败", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                Log.e("EditActivity", "HTTP " + response.code() + " | " + result);
                try {
                    JSONObject res = new JSONObject(result);
                    int code = res.optInt("code", response.isSuccessful() ? 200 : response.code());
                    String msg = res.optString("message", response.isSuccessful() ? "更新成功" : "服务器错误");
                    Log.e("EditActivity", "UPDATE RESPONSE code=" + code + " body=" + result);
                    String finalMsg = msg;
                    int finalCode = code;
                    runOnUiThread(() -> {
                        Toast.makeText(EditActivityActivity.this, ApiConfig.friendlyMsg(finalMsg), Toast.LENGTH_LONG).show();
                        if (finalCode == 200) {
                            setResult(RESULT_OK);
                            finish();
                        } else {
                            btnSubmit.setEnabled(true);
                            btnSubmit.setText("保存修改（需重新审核）");
                        }
                    });
                } catch (Exception e) {
                    Log.e("EditActivity", "解析失败", e);
                    final boolean success = response.isSuccessful();
                    runOnUiThread(() -> {
                        if (success) {
                            setResult(RESULT_OK);
                            finish();
                        } else {
                            btnSubmit.setEnabled(true);
                            btnSubmit.setText("保存修改（需重新审核）");
                            Toast.makeText(EditActivityActivity.this, "服务器错误(500)，更新失败", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }
}
