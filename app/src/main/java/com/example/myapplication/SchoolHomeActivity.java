package com.example.myapplication;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class SchoolHomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_school_home);

        // 1. 获取当前登录用户（SessionManager 优先，Intent 传参兜底）
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            currentUser = (User) getIntent().getSerializableExtra("user_data");
        }
        final User user = currentUser;

        // 2. 绑定控件
        TextView tvName = findViewById(R.id.tv_school_display_name);
        TextView tvStatus = findViewById(R.id.tv_auth_status);
        Button btnAddActivity = findViewById(R.id.btn_to_add_activity);

        if (currentUser != null) {
            // 从统一的 User 对象中获取学校名称
            tvName.setText(currentUser.getSchoolName() != null ? currentUser.getSchoolName() : "待完善学校资料");

            // 3. 认证状态控制
            // 临时设为 true，方便你测试点击发布按钮。如果后期数据库有审核字段，再替换成 currentUser.isVerified()
            boolean isVerified = true;

            if (isVerified) {
                tvStatus.setText("● 已通过官方认证");
                tvStatus.setTextColor(Color.GREEN);
                btnAddActivity.setEnabled(true);
            } else {
                tvStatus.setText("● 资质审核中（暂无法发布）");
                tvStatus.setTextColor(Color.RED);
                btnAddActivity.setEnabled(false);
            }
        }

        // --- 按钮点击事件 ---

        // 1. 发布支教活动按钮
        btnAddActivity.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddActivityActivity.class);
            intent.putExtra("user_data", user);
            startActivity(intent);
        });

        // 2. 管理已发布活动按钮
        findViewById(R.id.btn_to_my_published).setOnClickListener(v -> {
            Intent intent = new Intent(this, MyPublishedActivity.class);
            intent.putExtra("user_data", user);
            startActivity(intent);
        });

        // 3. 审核报名按钮
        findViewById(R.id.btn_to_manage_volunteers).setOnClickListener(v -> {
            Intent intent = new Intent(this, ManageActivity.class);
            intent.putExtra("user_data", user);
            startActivity(intent);
        });

        // 4. 浏览已审核招募信息
        findViewById(R.id.btn_browse_approved).setOnClickListener(v -> {
            startActivity(new Intent(this, BrowseApprovedActivity.class));
        });

        // 5. 编辑学校信息
        findViewById(R.id.btn_edit_school_info).setOnClickListener(v -> {
            startActivity(new Intent(this, EditProfileActivity.class));
        });

        // 6. 退出登录按钮
        findViewById(R.id.btn_school_logout).setOnClickListener(v -> {
            SessionManager.logout(this);
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
    }
}