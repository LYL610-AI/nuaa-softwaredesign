package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;



public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Activity data = (Activity) getIntent().getSerializableExtra("activity_data");

        TextView tvTitle = findViewById(R.id.tv_detail_title);
        TextView tvSchool = findViewById(R.id.tv_detail_school);
        TextView tvTime = findViewById(R.id.tv_detail_time);
        TextView tvAddress = findViewById(R.id.tv_detail_address);
        TextView tvRecruitNum = findViewById(R.id.tv_detail_recruit_num);
        TextView tvContent = findViewById(R.id.tv_detail_content);
        ImageView ivCover = findViewById(R.id.iv_detail_cover);
        Button btnApply = findViewById(R.id.btn_apply);

        User currentUser = SessionManager.getCurrentUser();
        boolean isSchool = currentUser != null && currentUser.isSchool();

        if (isSchool) {
            btnApply.setVisibility(View.GONE);
        }

        if (data != null) {
            tvTitle.setText(data.getTitle());
            tvSchool.setText("发起学校：" + (data.getUserId() != null ? data.getUserId() : "未知"));
            tvTime.setText("支教时间：" + (data.getStartDate() != null ? data.getStartDate() : "待定")
                    + " 至 " + (data.getEndDate() != null ? data.getEndDate() : "待定"));
            tvAddress.setText("支教地址：" + (data.getSchoolAddress() != null ? data.getSchoolAddress() : "待定"));
            tvRecruitNum.setText("招募人数：" + data.getRecruitsNumber() + "人");
            tvContent.setText(data.getContent() != null ? data.getContent() : "暂无详情");

            String imageUrl = ApiConfig.getFullImageUrl(data.getPictureUrl());
            if (imageUrl != null && !imageUrl.isEmpty()) {
                ivCover.setVisibility(View.VISIBLE);
                ImageLoader.load(imageUrl, ivCover);
            }
        }

        if (data != null && !isSchool) {
            String state = data.getActivityState();
            if ("结束".equals(state)) {
                btnApply.setEnabled(false);
                btnApply.setText("活动已结束");
            } else if (!"招募中".equals(state)) {
                btnApply.setEnabled(false);
                btnApply.setText("暂未开放报名");
            }
        }

        btnApply.setOnClickListener(v -> {
            if (data == null) return;
            Intent intent = new Intent(DetailActivity.this, RegistrationActivity.class);
            intent.putExtra("activity_id", data.getActivityId());
            intent.putExtra("activity_title", data.getTitle());
            startActivity(intent);
        });
    }
}