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

        java.io.Serializable rawData = getIntent().getSerializableExtra("activity_data");

        TextView tvTitle = findViewById(R.id.tv_detail_title);
        TextView tvSchool = findViewById(R.id.tv_detail_school);
        TextView tvTime = findViewById(R.id.tv_detail_time);
        TextView tvAddress = findViewById(R.id.tv_detail_address);
        TextView tvRecruitNum = findViewById(R.id.tv_detail_recruit_num);
        TextView tvContent = findViewById(R.id.tv_detail_content);
        ImageView ivCover = findViewById(R.id.iv_detail_cover);
        Button btnApply = findViewById(R.id.btn_apply);
        View llSummary = findViewById(R.id.ll_summary_section);
        TextView tvSummaryTitle = findViewById(R.id.tv_detail_summary_title);
        TextView tvSummaryContent = findViewById(R.id.tv_detail_summary_content);
        TextView tvSummaryAudit = findViewById(R.id.tv_detail_summary_audit);

        User currentUser = SessionManager.getCurrentUser();
        boolean isSchool = currentUser != null && currentUser.isSchool();

        if (isSchool) {
            btnApply.setVisibility(View.GONE);
        }

        String title = null, userId = null, startDate = null, endDate = null;
        String schoolAddress = null, content = null, pictureUrl = null;
        String activityState = null;
        String summaryTitle = null, summaryContent = null, summaryAuditState = null;

        if (rawData instanceof TeachingActivity) {
            TeachingActivity ta = (TeachingActivity) rawData;
            title = ta.getTitle();
            userId = ta.getUserId();
            startDate = ta.getStartDate();
            endDate = ta.getEndDate();
            schoolAddress = ta.getSchoolAddress();
            content = ta.getContent();
            pictureUrl = ta.getPictureUrl();
            activityState = ta.getActivityState();
            summaryTitle = ta.getSummaryTitle();
            summaryContent = ta.getSummaryContent();
            summaryAuditState = ta.getSummaryAuditState();
        } else if (rawData instanceof Activity) {
            Activity a = (Activity) rawData;
            title = a.getTitle();
            userId = a.getUserId();
            startDate = a.getStartDate();
            endDate = a.getEndDate();
            schoolAddress = a.getSchoolAddress();
            content = a.getContent();
            pictureUrl = a.getPictureUrl();
            activityState = a.getActivityState();
            summaryTitle = a.getSummaryTitle();
            summaryContent = a.getSummaryContent();
            summaryAuditState = a.getSummaryAuditState();
        }

        if (title != null) {
            tvTitle.setText(title);
            tvSchool.setText("发起学校：" + (userId != null ? userId : "未知"));
            tvTime.setText("支教时间：" + (startDate != null ? startDate : "待定")
                    + " 至 " + (endDate != null ? endDate : "待定"));
            tvAddress.setText("支教地址：" + (schoolAddress != null ? schoolAddress : "待定"));
            tvRecruitNum.setText("招募人数：" + (rawData instanceof TeachingActivity
                    ? ((TeachingActivity) rawData).getRecruitsNumber()
                    : ((Activity) rawData).getRecruitsNumber()) + "人");
            tvContent.setText(content != null ? content : "暂无详情");

            String imageUrl = ApiConfig.getFullImageUrl(pictureUrl);
            if (imageUrl != null && !imageUrl.isEmpty()) {
                ivCover.setVisibility(View.VISIBLE);
                ImageLoader.load(imageUrl, ivCover);
            }

            // 显示总结信息
            if (summaryContent != null && !summaryContent.isEmpty()) {
                llSummary.setVisibility(View.VISIBLE);
                tvSummaryTitle.setText(summaryTitle != null && !summaryTitle.isEmpty()
                        ? summaryTitle : "总结报告");
                tvSummaryContent.setText(summaryContent);
                String auditLabel;
                if (summaryAuditState == null) {
                    auditLabel = "未审核";
                } else {
                    switch (summaryAuditState) {
                        case "0": auditLabel = "待审核"; break;
                        case "1": auditLabel = "已通过"; break;
                        case "2": auditLabel = "未通过"; break;
                        default: auditLabel = summaryAuditState; break;
                    }
                }
                tvSummaryAudit.setText("总结审核状态：" + auditLabel);
            }
        }

        if (rawData != null && !isSchool) {
            if ("结束".equals(activityState)) {
                btnApply.setEnabled(false);
                btnApply.setText("活动已结束");
            } else if (!"招募中".equals(activityState)) {
                btnApply.setEnabled(false);
                btnApply.setText("暂未开放报名");
            }
        }

        final String finalActivityId = rawData instanceof TeachingActivity
                ? ((TeachingActivity) rawData).getActivityId()
                : (rawData instanceof Activity ? ((Activity) rawData).getActivityId() : null);
        final String finalTitle = title;

        btnApply.setOnClickListener(v -> {
            if (finalActivityId == null) return;
            Intent intent = new Intent(DetailActivity.this, RegistrationActivity.class);
            intent.putExtra("activity_id", finalActivityId);
            intent.putExtra("activity_title", finalTitle);
            startActivity(intent);
        });
    }
}