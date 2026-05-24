package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class AdminPostVerifyAdapter extends RecyclerView.Adapter<AdminPostVerifyAdapter.ViewHolder> {

    private List<Post> pendingList;
    private OnVerifyListener listener;
    private OnDeleteListener deleteListener;
    private OnDetailListener detailListener;

    public interface OnVerifyListener {
        void onAction(Post post, String action, int position);
    }

    public interface OnDeleteListener {
        void onDelete(Post post, int position);
    }

    public interface OnDetailListener {
        void onDetail(Post post);
    }

    public AdminPostVerifyAdapter(List<Post> list, OnVerifyListener listener, OnDeleteListener deleteListener, OnDetailListener detailListener) {
        this.pendingList = list;
        this.listener = listener;
        this.deleteListener = deleteListener;
        this.detailListener = detailListener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvSubtitle;
        Button btnApprove, btnReject, btnDelete, btnDetail;

        public ViewHolder(View view) {
            super(view);
            tvTitle = view.findViewById(R.id.tv_item_title);
            tvSubtitle = view.findViewById(R.id.tv_item_subtitle);
            btnApprove = view.findViewById(R.id.btn_approve);
            btnReject = view.findViewById(R.id.btn_reject);
            btnDelete = view.findViewById(R.id.btn_delete);
            btnDetail = view.findViewById(R.id.btn_view_detail);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_post_verify, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Post post = pendingList.get(position);

        String auditState = post.getAuditState();
        if (auditState == null) auditState = "";

        holder.tvTitle.setText(post.getTitle());
        String userName = post.getUserName();
        holder.tvSubtitle.setText("发布者: " + (userName != null && !userName.isEmpty() ? userName : post.getUserId())
                + " | " + (post.getPublishTime() != null ? post.getPublishTime() : "")
                + " | 状态: " + auditState);

        // 只有待审核的帖子才能操作
        boolean isPending = "未审核".equals(auditState) || "待审核".equals(auditState);
        holder.btnApprove.setEnabled(isPending);
        holder.btnReject.setEnabled(isPending);
        if (!isPending) {
            holder.btnApprove.setText("已审核");
            holder.btnReject.setText("已审核");
        } else {
            holder.btnApprove.setText("通过");
            holder.btnReject.setText("驳回");
        }

        holder.btnApprove.setOnClickListener(v -> {
            if (!isPending) return;
            int pos = holder.getAdapterPosition();
            if (pos < 0 || pos >= pendingList.size()) return;
            Post item = pendingList.get(pos);
            holder.btnApprove.setEnabled(false);
            holder.btnReject.setEnabled(false);
            if (listener != null) listener.onAction(item, "通过", pos);
        });

        holder.btnReject.setOnClickListener(v -> {
            if (!isPending) return;
            int pos = holder.getAdapterPosition();
            if (pos < 0 || pos >= pendingList.size()) return;
            Post item = pendingList.get(pos);
            holder.btnApprove.setEnabled(false);
            holder.btnReject.setEnabled(false);
            if (listener != null) listener.onAction(item, "未通过", pos);
        });

        holder.btnDelete.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos < 0 || pos >= pendingList.size()) return;
            Post item = pendingList.get(pos);
            if (deleteListener != null) deleteListener.onDelete(item, pos);
        });

        holder.btnDetail.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos < 0 || pos >= pendingList.size()) return;
            if (detailListener != null) detailListener.onDetail(pendingList.get(pos));
        });
    }

    @Override
    public int getItemCount() { return pendingList != null ? pendingList.size() : 0; }

    public void removeItem(Post post) {
        int pos = pendingList.indexOf(post);
        if (pos >= 0) {
            pendingList.remove(pos);
            notifyItemRemoved(pos);
            notifyItemRangeChanged(pos, pendingList.size());
        }
    }

    public void removeItem(int position) {
        if (position >= 0 && position < pendingList.size()) {
            pendingList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, pendingList.size());
        }
    }
}
