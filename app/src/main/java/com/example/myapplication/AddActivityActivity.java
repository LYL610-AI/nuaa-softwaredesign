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

public class AddActivityActivity extends AppCompatActivity {

    private final okhttp3.OkHttpClient client = ApiConfig.getClient();
    private Uri selectedImageUri;
    private ImageView ivPreview;
    private Button btnSelectImage, btnClearImage, btnSubmit;
    private ActivityResultLauncher<String> imagePickerLauncher;

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
        btnSubmit = findViewById(R.id.btn_add_submit);
        ivPreview = findViewById(R.id.iv_activity_preview);
        btnSelectImage = findViewById(R.id.btn_select_activity_image);
        btnClearImage = findViewById(R.id.btn_clear_activity_image);

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;
                        ivPreview.setImageURI(uri);
                        ivPreview.setVisibility(View.VISIBLE);
                        btnClearImage.setVisibility(View.VISIBLE);
                    }
                });

        btnSelectImage.setOnClickListener(v ->
                imagePickerLauncher.launch("image/*"));

        btnClearImage.setOnClickListener(v -> {
            selectedImageUri = null;
            ivPreview.setImageURI(null);
            ivPreview.setVisibility(View.GONE);
            btnClearImage.setVisibility(View.GONE);
        });

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
                uploadImageThenSubmit(title, schoolAddress, startDate, endDate,
                        Integer.parseInt(numStr), content);
            } else {
                btnSubmit.setText("提交中...");
                submitActivityToServer(title, schoolAddress, startDate, endDate,
                        Integer.parseInt(numStr), content, null);
            }
        });
    }

    private void uploadImageThenSubmit(String title, String schoolAddress,
            String startDate, String endDate, int recruitsNumber, String content) {
        new Thread(() -> {
            try {
                File imageFile = copyUriToFile(selectedImageUri);
                if (imageFile == null) {
                    runOnUiThread(() -> {
                        btnSubmit.setEnabled(true);
                        btnSubmit.setText("提交发布（需管理员审核）");
                        Toast.makeText(AddActivityActivity.this, "无法读取图片文件", Toast.LENGTH_SHORT).show();
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
                Log.d("AddActivity", "Upload response code=" + response.code() + ", body=" + result);
                if (result != null && response.isSuccessful()) {
                    JSONObject res = new JSONObject(result);
                    if (res.getInt("code") == 200) {
                        String uploadedUrl = res.getJSONObject("data").getString("url");
                        imageFile.delete();
                        final String finalUrl = uploadedUrl;
                        runOnUiThread(() -> {
                            btnSubmit.setText("提交中...");
                            submitActivityToServer(title, schoolAddress, startDate, endDate,
                                    recruitsNumber, content, finalUrl);
                        });
                        return;
                    }
                }
                imageFile.delete();
                final String errDetail = result != null ? result : "HTTP " + response.code();
                Log.e("AddActivity", "Upload failed: " + errDetail);
                runOnUiThread(() -> {
                    btnSubmit.setEnabled(true);
                    btnSubmit.setText("提交发布（需管理员审核）");
                    Toast.makeText(AddActivityActivity.this, "图片上传失败: " + errDetail, Toast.LENGTH_LONG).show();
                });
            } catch (Exception e) {
                Log.e("AddActivity", "Image upload error", e);
                runOnUiThread(() -> {
                    btnSubmit.setEnabled(true);
                    btnSubmit.setText("提交发布（需管理员审核）");
                    Toast.makeText(AddActivityActivity.this, "图片上传失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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

    private void submitActivityToServer(String title, String schoolAddress,
            String startDate, String endDate, int recruitsNumber,
            String content, String pictureUrl) {

        String url = ApiConfig.getBaseUrl() + "/activity/create";

        JSONObject json = new JSONObject();
        try {
            json.put("title", title);
            json.put("address", schoolAddress);
            json.put("startDate", startDate);
            json.put("endDate", endDate);
            json.put("recruitsNumber", recruitsNumber);
            json.put("content", content);
            if (pictureUrl != null && !pictureUrl.isEmpty()) {
                json.put("pictureUrl", pictureUrl);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.d("AddActivity", "请求体: " + json.toString());

        RequestBody body = RequestBody.create(ApiConfig.JSON, json.toString());
        Request request = new Request.Builder().url(url).post(body).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    btnSubmit.setEnabled(true);
                    btnSubmit.setText("提交发布（需管理员审核）");
                    Toast.makeText(AddActivityActivity.this, "网络连接失败", Toast.LENGTH_SHORT).show();
                });
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
                        else {
                            btnSubmit.setEnabled(true);
                            btnSubmit.setText("提交发布（需管理员审核）");
                        }
                    });
                } catch (Exception e) {
                    Log.e("AddActivity", "解析失败", e);
                    runOnUiThread(() -> {
                        btnSubmit.setEnabled(true);
                        btnSubmit.setText("提交发布（需管理员审核）");
                        Toast.makeText(AddActivityActivity.this, "提交失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }
}
