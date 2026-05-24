package com.example.myapplication;

import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MyPublishedAdapter extends RecyclerView.Adapter<MyPublishedAdapter.ViewHolder> {

    private List<TeachingActivity> activityList;
    private OnActionListener actionListener;

    public interface OnActionListener {
        void onEdit(TeachingActivity activity, int position);
        void onDelete(TeachingActivity activity, int position);
        void onSummary(TeachingActivity activity, int position);
        void onStart(TeachingActivity activity, int position);
    }

    public MyPublishedAdapter(List<TeachingActivity> list) {
        this.activityList = list;
    }

    public void setOnActionListener(OnActionListener listener) {
        this.actionListener = listener;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, location, status;
        Button btnStart, btnSummary, btnEdit, btnDelete;

        public ViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.tv_title);
            location = view.findViewById(R.id.tv_location);
            status = view.findViewById(R.id.tv_status);
            btnStart = view.findViewById(R.id.btn_start);
            btnSummary = view.findViewById(R.id.btn_summary);
            btnEdit = view.findViewById(R.id.btn_edit);
            btnDelete = view.findViewById(R.id.btn_delete);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_my_published, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TeachingActivity activity = activityList.get(position);
        holder.title.setText(activity.getTitle());
        holder.location.setText("地点：" + activity.getSchoolAddress());

        String state = activity.getActivityState();
        String auditState = activity.getAuditState();
        String summaryAuditState = activity.getSummaryAuditState();

        boolean isPendingAudit = "待审核".equals(auditState);
        boolean isRejected = "未通过".equals(auditState);
        boolean isEnd = "结束".equals(state);
        boolean summaryApproved = "通过".equals(summaryAuditState);

        if (isPendingAudit) holder.status.setText("待审核");
        else if (isRejected) holder.status.setText("未通过");
        else holder.status.setText(state);

        // 总结按钮：审核已通过、非结束、总结未被审核通过时显示
        holder.btnSummary.setVisibility((!isPendingAudit && !isRejected && !isEnd && !summaryApproved) ? View.VISIBLE : View.GONE);
        // 编辑按钮：仅待审核时显示
        holder.btnEdit.setVisibility(isPendingAudit ? View.VISIBLE : View.GONE);
        // 删除按钮：待审核、未通过或招募中时显示
        holder.btnDelete.setVisibility((isPendingAudit || isRejected || "招募中".equals(state)) ? View.VISIBLE : View.GONE);
        // 开始活动按钮：仅招募中且非待审核时显示
        holder.btnStart.setVisibility(("招募中".equals(state) && !isPendingAudit) ? View.VISIBLE : View.GONE);

        holder.btnStart.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onStart(activity, holder.getAdapterPosition());
            }
        });

        holder.btnSummary.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onSummary(activity, holder.getAdapterPosition());
            }
        });

        holder.btnEdit.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onEdit(activity, holder.getAdapterPosition());
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onDelete(activity, holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return activityList.size();
    }

    public void removeItem(int position) {
        if (position >= 0 && position < activityList.size()) {
            activityList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, activityList.size());
        }
    }
}