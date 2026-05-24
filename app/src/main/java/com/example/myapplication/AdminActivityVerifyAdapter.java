package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class AdminActivityVerifyAdapter extends RecyclerView.Adapter<AdminActivityVerifyAdapter.ViewHolder> {

    private List<Activity> pendingList;
    private OnDetailListener listener;

    public interface OnDetailListener {
        void onViewDetail(Activity activity, int position);
    }

    public AdminActivityVerifyAdapter(List<Activity> list, OnDetailListener listener) {
        this.pendingList = list;
        this.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvSubtitle;
        Button btnViewDetail;

        public ViewHolder(View view) {
            super(view);
            tvTitle = view.findViewById(R.id.tv_item_title);
            tvSubtitle = view.findViewById(R.id.tv_item_subtitle);
            btnViewDetail = view.findViewById(R.id.btn_view_detail);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_verify, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Activity activity = pendingList.get(position);

        String auditState = activity.getAuditState();
        if (auditState == null) auditState = "";

        holder.tvTitle.setText(activity.getTitle());
        holder.tvSubtitle.setText("学校ID: " + activity.getUserId()
                + " | 招募: " + activity.getRecruitsNumber() + "人"
                + " | 状态: " + auditState);

        holder.btnViewDetail.setText("查看详情");
        holder.btnViewDetail.setEnabled(true);

        holder.btnViewDetail.setOnClickListener(v -> {
            if (listener != null) listener.onViewDetail(activity, holder.getAdapterPosition());
        });
    }

    @Override
    public int getItemCount() { return pendingList != null ? pendingList.size() : 0; }

    public void removeItem(int position) {
        pendingList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, pendingList.size());
    }
}
