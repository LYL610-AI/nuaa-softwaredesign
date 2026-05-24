package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AdminSummaryVerifyAdapter extends RecyclerView.Adapter<AdminSummaryVerifyAdapter.ViewHolder> {

    private List<Activity> pendingList;
    private OnSummaryVerifyListener listener;

    public interface OnSummaryVerifyListener {
        void onAction(String activityId, String action, int position);
    }

    public AdminSummaryVerifyAdapter(List<Activity> list, OnSummaryVerifyListener listener) {
        this.pendingList = list;
        this.listener = listener;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvSubtitle, tvSummaryPreview;
        Button btnApprove, btnReject;

        public ViewHolder(View view) {
            super(view);
            tvTitle = view.findViewById(R.id.tv_item_title);
            tvSubtitle = view.findViewById(R.id.tv_item_subtitle);
            tvSummaryPreview = view.findViewById(R.id.tv_summary_preview);
            btnApprove = view.findViewById(R.id.btn_summary_approve);
            btnReject = view.findViewById(R.id.btn_summary_reject);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_summary_verify, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Activity activity = pendingList.get(position);

        String stateText = activity.getSummaryAuditState();
        if (stateText == null || stateText.isEmpty()) stateText = "待审核";

        holder.tvTitle.setText(activity.getTitle());
        holder.tvSubtitle.setText("学校ID: " + activity.getUserId()
                + " | " + (activity.getStartDate() != null ? activity.getStartDate() : "")
                + " 至 " + (activity.getEndDate() != null ? activity.getEndDate() : "")
                + " | 状态: " + stateText);
        holder.tvSummaryPreview.setText(activity.getSummaryContent() != null
                && !activity.getSummaryContent().isEmpty() ? activity.getSummaryContent() : "（无总结内容）");

        boolean isPending = "待审核".equals(stateText);
        holder.btnApprove.setEnabled(isPending);
        holder.btnReject.setEnabled(isPending);
        if (!isPending) {
            String label = "未通过".equals(stateText) ? "已驳回" : "已" + stateText;
            holder.btnApprove.setText(label);
            holder.btnReject.setText(label);
        } else {
            holder.btnApprove.setText("通过");
            holder.btnReject.setText("驳回");
        }

        holder.btnApprove.setOnClickListener(v -> {
            if (!isPending) return;
            int pos = holder.getAdapterPosition();
            if (pos < 0 || pos >= pendingList.size()) return;
            holder.btnApprove.setEnabled(false);
            holder.btnReject.setEnabled(false);
            if (listener != null) listener.onAction(activity.getActivityId(), "通过", pos);
        });

        holder.btnReject.setOnClickListener(v -> {
            if (!isPending) return;
            int pos = holder.getAdapterPosition();
            if (pos < 0 || pos >= pendingList.size()) return;
            holder.btnApprove.setEnabled(false);
            holder.btnReject.setEnabled(false);
            if (listener != null) listener.onAction(activity.getActivityId(), "未通过", pos);
        });
    }

    @Override
    public int getItemCount() {
        return pendingList != null ? pendingList.size() : 0;
    }

    public void removeItem(int position) {
        if (position >= 0 && position < pendingList.size()) {
            pendingList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, pendingList.size());
        }
    }
}