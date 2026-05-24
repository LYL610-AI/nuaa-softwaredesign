package com.example.myapplication;

import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RecordAdapter extends RecyclerView.Adapter<RecordAdapter.ViewHolder> {

    private List<RegistrationRecord> recordList;
    private OnCancelListener cancelListener;

    public interface OnCancelListener {
        void onCancel(RegistrationRecord record, int position);
    }

    public RecordAdapter(List<RegistrationRecord> list) {
        this.recordList = list;
    }

    public void setOnCancelListener(OnCancelListener listener) {
        this.cancelListener = listener;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, date, status;
        Button btnCancel;

        public ViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.tv_record_title);
            date = view.findViewById(R.id.tv_record_date);
            status = view.findViewById(R.id.tv_record_status);
            btnCancel = view.findViewById(R.id.btn_cancel_reg);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_registration, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RegistrationRecord record = recordList.get(position);
        holder.title.setText(record.getActivityTitle());
        holder.date.setText("申请日期：" + record.getApplyDate());
        holder.status.setText(record.getAuditState());

        String status = record.getAuditState();
        if ("通过".equals(status)) {
            holder.status.setTextColor(0xFF4CAF50);
        } else if ("待审核".equals(status)) {
            holder.status.setTextColor(0xFFFF9800);
        } else if ("已拒绝".equals(status) || "未通过".equals(status)) {
            holder.status.setTextColor(0xFFF44336);
        } else {
            holder.status.setTextColor(0xFF666666);
        }

        boolean isPending = "待审核".equals(status);
        holder.btnCancel.setVisibility(isPending ? View.VISIBLE : View.GONE);
        holder.btnCancel.setEnabled(true);
        holder.btnCancel.setOnClickListener(v -> {
            v.setEnabled(false);
            if (cancelListener != null) {
                cancelListener.onCancel(record, holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return recordList.size();
    }

    public void removeItem(int position) {
        if (position >= 0 && position < recordList.size()) {
            recordList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, recordList.size());
        }
    }

    public void removeItem(RegistrationRecord record) {
        int pos = recordList.indexOf(record);
        if (pos >= 0) {
            recordList.remove(pos);
            notifyItemRemoved(pos);
            notifyItemRangeChanged(pos, recordList.size());
        }
    }
}
