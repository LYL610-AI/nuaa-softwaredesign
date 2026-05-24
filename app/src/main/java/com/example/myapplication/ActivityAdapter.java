package com.example.myapplication;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;



import java.util.List;

public class ActivityAdapter extends RecyclerView.Adapter<ActivityAdapter.ViewHolder> {
    private List<Activity> activityList;
    private OnItemClickListener itemClickListener;

    public interface OnItemClickListener {
        void onItemClick(Activity activity, int position);
    }

    public ActivityAdapter(List<Activity> list) {
        this.activityList = list;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, location, status;
        ImageView icon;
        public ViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.tv_title);
            location = view.findViewById(R.id.tv_location);
            status = view.findViewById(R.id.tv_status);
            icon = view.findViewById(R.id.iv_activity_icon);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_activity, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Activity activity = activityList.get(position);
        holder.title.setText(activity.getTitle());
        holder.location.setText("地点：" + activity.getSchoolAddress());
        holder.status.setText(activity.getActivityState());

        String imageUrl = ApiConfig.getFullImageUrl(activity.getPictureUrl());
        if (imageUrl != null && !imageUrl.isEmpty()) {
            ImageLoader.load(imageUrl, holder.icon);
        } else {
            holder.icon.setImageResource(R.mipmap.ic_launcher);
        }
        holder.itemView.setOnClickListener(v -> {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(activity, holder.getAdapterPosition());
            } else {
                Intent intent = new Intent(v.getContext(), DetailActivity.class);
                intent.putExtra("activity_data", activity);
                v.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return activityList.size();
    }
}