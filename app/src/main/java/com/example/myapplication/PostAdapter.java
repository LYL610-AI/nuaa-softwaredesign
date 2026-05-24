package com.example.myapplication;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;



import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {
    private List<Post> postList;
    private OnDeleteListener deleteListener;
    private String currentUserId;

    public interface OnDeleteListener {
        void onDelete(Post post, int position);
    }

    public PostAdapter(List<Post> list) {
        this.postList = list;
    }

    public void setOnDeleteListener(OnDeleteListener listener, String currentUserId) {
        this.deleteListener = listener;
        this.currentUserId = currentUserId;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView user, content, time, like;
        ImageView thumbnail;
        Button btnDelete;

        public ViewHolder(View view) {
            super(view);
            user = view.findViewById(R.id.tv_post_user);
            content = view.findViewById(R.id.tv_post_content);
            time = view.findViewById(R.id.tv_post_time);
            like = view.findViewById(R.id.tv_post_like);
            thumbnail = view.findViewById(R.id.iv_post_thumbnail);
            btnDelete = view.findViewById(R.id.btn_delete_post);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Post post = postList.get(position);
        String userName = post.getUserName();
        holder.user.setText(userName != null && !userName.isEmpty() ? userName : post.getUserId());
        holder.content.setText(post.getTitle() + "\n" + post.getContent());
        holder.time.setText(post.getPublishTime());
        holder.like.setVisibility(View.GONE);

        String imageUrl = ApiConfig.getFullImageUrl(post.getPictureUrl());
        if (imageUrl != null && !imageUrl.isEmpty()) {
            holder.thumbnail.setVisibility(View.VISIBLE);
            ImageLoader.load(imageUrl, holder.thumbnail);
        } else {
            holder.thumbnail.setVisibility(View.GONE);
        }

        // 删除按钮：仅在自己的帖子列表或管理员场景显示
        boolean canDelete = deleteListener != null &&
                (currentUserId != null && currentUserId.equals(post.getUserId()));
        holder.btnDelete.setVisibility(canDelete ? View.VISIBLE : View.GONE);
        holder.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) {
                v.setEnabled(false);
                deleteListener.onDelete(post, holder.getAdapterPosition());
            }
        });

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), PostDetailActivity.class);
            intent.putExtra("post_data", post);
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public void removeItem(int position) {
        if (position >= 0 && position < postList.size()) {
            postList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, postList.size());
        }
    }

    public void removeItem(Post post) {
        int pos = postList.indexOf(post);
        if (pos >= 0) {
            postList.remove(pos);
            notifyItemRemoved(pos);
            notifyItemRangeChanged(pos, postList.size());
        }
    }
}
