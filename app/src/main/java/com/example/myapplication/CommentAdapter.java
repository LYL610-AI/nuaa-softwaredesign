package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {

    private List<Comment> commentList;
    private String currentUserId;
    private boolean isAdmin;
    private OnDeleteListener deleteListener;

    public interface OnDeleteListener {
        void onDelete(Comment comment, int position);
    }

    public CommentAdapter(List<Comment> list, String currentUserId, boolean isAdmin) {
        this.commentList = list;
        this.currentUserId = currentUserId;
        this.isAdmin = isAdmin;
    }

    public void setOnDeleteListener(OnDeleteListener listener) {
        this.deleteListener = listener;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAvatar, tvAuthor, tvTime, tvContent;
        Button btnDelete;

        public ViewHolder(View view) {
            super(view);
            tvAvatar = view.findViewById(R.id.tv_comment_avatar);
            tvAuthor = view.findViewById(R.id.tv_comment_author);
            tvTime = view.findViewById(R.id.tv_comment_time);
            tvContent = view.findViewById(R.id.tv_comment_content);
            btnDelete = view.findViewById(R.id.btn_comment_delete);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_comment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Comment comment = commentList.get(position);

        String authorId = comment.getUserId();
        String authorName = comment.getUserName();
        String displayName;
        if (authorName != null && !authorName.isEmpty()) {
            displayName = authorName;
        } else if (authorId != null && !authorId.isEmpty()) {
            displayName = authorId;
        } else {
            displayName = "匿名用户";
        }

        holder.tvAuthor.setText("用户：" + displayName);
        holder.tvAvatar.setText(displayName.substring(0, 1).toUpperCase());
        holder.tvTime.setText(comment.getPublishTime());
        holder.tvContent.setText(comment.getContent());

        // 管理员或作者本人可删除
        boolean canDelete = isAdmin || (authorId != null && authorId.equals(currentUserId));
        holder.btnDelete.setVisibility(canDelete ? View.VISIBLE : View.GONE);
        holder.btnDelete.setEnabled(true);
        holder.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) {
                v.setEnabled(false);
                deleteListener.onDelete(comment, holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() { return commentList.size(); }

    public void removeItem(Comment comment) {
        int pos = commentList.indexOf(comment);
        if (pos >= 0) {
            commentList.remove(pos);
            notifyItemRemoved(pos);
            notifyItemRangeChanged(pos, commentList.size());
        }
    }
}
