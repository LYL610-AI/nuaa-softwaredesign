package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ActivityBrowseAdapter extends RecyclerView.Adapter<ActivityBrowseAdapter.ViewHolder> {

    private List<Activity> activityList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onClick(Activity activity, int position);
    }

    public ActivityBrowseAdapter(List<Activity> list, OnItemClickListener listener) {
        this.activityList = list;
        this.listener = listener;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvSubtitle, tvContent;

        ViewHolder(View view) {
            super(view);
            tvTitle = view.findViewById(R.id.tv_item_title);
            tvSubtitle = view.findViewById(R.id.tv_item_subtitle);
            tvContent = view.findViewById(R.id.tv_item_content);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_activity_browse, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Activity activity = activityList.get(position);

        holder.tvTitle.setText(activity.getTitle());

        String state = activity.getActivityState() != null ? activity.getActivityState() : "";
        holder.tvSubtitle.setText("学校ID: " + activity.getUserId()
                + " | 招募: " + activity.getRecruitsNumber() + "人"
                + " | 状态: " + state);

        String content = activity.getContent();
        holder.tvContent.setText(content != null && !content.isEmpty() ? content : "（无详细介绍）");

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onClick(activity, holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return activityList != null ? activityList.size() : 0;
    }

    public void updateData(List<Activity> newList) {
        this.activityList = newList;
        notifyDataSetChanged();
    }
}
