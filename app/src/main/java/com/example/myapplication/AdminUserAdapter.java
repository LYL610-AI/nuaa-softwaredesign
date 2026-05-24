package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.ViewHolder> {

    private List<User> userList;
    private OnUserActionListener listener;

    public interface OnUserActionListener {
        void onDelete(String userId, int position);
        void onEdit(User user);
    }

    public AdminUserAdapter(List<User> list, OnUserActionListener listener) {
        this.userList = list;
        this.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvInfo, tvPermission;
        Button btnEdit, btnDelete;

        public ViewHolder(View view) {
            super(view);
            tvName = view.findViewById(R.id.tv_user_name);
            tvInfo = view.findViewById(R.id.tv_user_info);
            tvPermission = view.findViewById(R.id.tv_user_permission);
            btnEdit = view.findViewById(R.id.btn_edit_user);
            btnDelete = view.findViewById(R.id.btn_delete_user);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = userList.get(position);
        String displayName = user.getRealName() != null && !user.getRealName().isEmpty()
                ? user.getRealName() : user.getUserId();
        holder.tvName.setText(displayName);

        String permissionText;
        if (user.isAdmin()) {
            permissionText = "管理员";
        } else if (user.isSchool()) {
            permissionText = "学校负责人";
        } else {
            permissionText = "志愿者";
        }
        holder.tvPermission.setText(permissionText);
        holder.tvInfo.setText("手机号: " + (user.getUserPhone() != null ? user.getUserPhone() : "未填写"));

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEdit(user);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDelete(user.getUserId(), holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() { return userList != null ? userList.size() : 0; }

    public void removeItem(int position) {
        if (position >= 0 && position < userList.size()) {
            userList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, userList.size());
        }
    }

    public void updateData(List<User> newList) {
        this.userList = newList;
        notifyDataSetChanged();
    }
}
