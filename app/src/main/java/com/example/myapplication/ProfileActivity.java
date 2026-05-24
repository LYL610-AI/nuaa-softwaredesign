package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    private User currentUser; // 保存当前用户实例

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        TextView tvName = findViewById(R.id.tv_profile_name);
        TextView tvType = findViewById(R.id.tv_profile_type);

        // 从 SessionManager 获取当前登录用户（来源可靠，不依赖 Intent 传参）
        currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            currentUser = (User) getIntent().getSerializableExtra("user_data");
        }

        // 获取所有的操作按钮
        Button btnApplied = findViewById(R.id.btn_my_applied_activities);
        Button btnPublished = findViewById(R.id.btn_my_published_activities);
        Button btnMyPosts = findViewById(R.id.btn_my_posts);
        Button btnAudit = findViewById(R.id.btn_audit_my_registrations);

        // 2. 核心逻辑：根据角色动态展示个人信息
        if (currentUser != null) {
            if (currentUser.isSchool()) {
                String schoolName = currentUser.getSchoolName();
                tvName.setText(schoolName != null && !schoolName.isEmpty() ? schoolName : "未命名学校");
                if (tvType != null) tvType.setText("角色：学校负责人");

            } else if (currentUser.isVolunteer()) {
                String realName = currentUser.getRealName();
                tvName.setText(realName != null && !realName.isEmpty() ? realName : currentUser.getUserPhone());
                if (tvType != null) tvType.setText("角色：志愿者");

            } else if (currentUser.isAdmin()) {
                tvName.setText("管理员");
                if (tvType != null) tvType.setText("角色：系统管理员");
            }
        } else {
            // 未登录，回登录页
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        // --- 按钮点击事件绑定 ---

        // 学校专属功能区域（已发布活动 + 审核报名）
        View layoutSchoolActions = findViewById(R.id.layout_school_actions);
        if (currentUser != null && currentUser.isSchool()) {
            layoutSchoolActions.setVisibility(View.VISIBLE);
            if (btnPublished != null) {
                btnPublished.setOnClickListener(v -> {
                    startActivity(new Intent(ProfileActivity.this, MyPublishedActivity.class));
                });
            }
            if (btnAudit != null) {
                btnAudit.setOnClickListener(v -> {
                    Intent intent = new Intent(ProfileActivity.this, ManageActivity.class);
                    intent.putExtra("user_data", currentUser);
                    startActivity(intent);
                });
            }
        }

        // 已报名活动
        if (btnApplied != null) {
            btnApplied.setOnClickListener(v -> {
                startActivity(new Intent(ProfileActivity.this, MyRegistrationsActivity.class));
            });
        }

        // 我的主题帖
        if (btnMyPosts != null) {
            btnMyPosts.setOnClickListener(v -> {
                startActivity(new Intent(ProfileActivity.this, MyPostsActivity.class));
            });
        }

        // 7. 编辑个人信息 (志愿者/学校可见，管理员隐藏)
        Button btnEditProfile = findViewById(R.id.btn_edit_profile);
        if (btnEditProfile != null && currentUser != null) {
            if (currentUser.isAdmin()) {
                btnEditProfile.setVisibility(View.GONE);
            } else {
                btnEditProfile.setVisibility(View.VISIBLE);
                btnEditProfile.setOnClickListener(v -> {
                    startActivity(new Intent(ProfileActivity.this, EditProfileActivity.class));
                });
            }
        }

        // 8. 用户管理 (仅管理员可见)
        Button btnAdminUsers = findViewById(R.id.btn_admin_users);
        if (btnAdminUsers != null && currentUser != null && currentUser.isAdmin()) {
            btnAdminUsers.setVisibility(View.VISIBLE);
            btnAdminUsers.setOnClickListener(v -> {
                startActivity(new Intent(ProfileActivity.this, AdminUserListActivity.class));
            });
        }

        // --- 退出登录逻辑 ---
        Button btnLogout = findViewById(R.id.btn_volunteer_logout);
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            });
        }

        // --- 底部导航栏逻辑 ---
        Button navHome = findViewById(R.id.nav_home);
        Button navPosts = findViewById(R.id.nav_posts);
        Button navProfile = findViewById(R.id.nav_profile);

        // 首页按钮 (需要根据身份返回不同的首页)
        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                Intent intent;
                if (currentUser != null && currentUser.isSchool()) {
                    intent = new Intent(ProfileActivity.this, SchoolHomeActivity.class);
                } else {
                    intent = new Intent(ProfileActivity.this, HomeActivity.class);
                }
                intent.putExtra("user_data", currentUser);
                startActivity(intent);
                finish();
            });
        }

        // 主题帖按钮
        if (navPosts != null) {
            navPosts.setOnClickListener(v -> {
                Intent intent = new Intent(ProfileActivity.this, PostActivity.class);
                intent.putExtra("user_data", currentUser);
                startActivity(intent);
                finish();
            });
        }

        // 我的按钮
        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                Toast.makeText(this, "已在个人中心", Toast.LENGTH_SHORT).show();
            });
        }
    }

}