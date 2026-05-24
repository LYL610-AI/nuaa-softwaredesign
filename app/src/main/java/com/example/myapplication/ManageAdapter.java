package com.example.myapplication;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ManageAdapter extends RecyclerView.Adapter<ManageAdapter.ViewHolder> {

    private List<Registration> applyList;
    private final OkHttpClient client = ApiConfig.getClient();
    private android.content.Context adapterContext;

    public ManageAdapter(List<Registration> applyList) {
        this.applyList = applyList;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUser, tvProject, tvAuditStatus;
        Button btnApprove, btnReject;

        public ViewHolder(View view) {
            super(view);
            tvUser = view.findViewById(R.id.tv_apply_user);
            tvProject = view.findViewById(R.id.tv_apply_project);
            tvAuditStatus = view.findViewById(R.id.tv_audit_status);
            btnApprove = view.findViewById(R.id.btn_approve);
            btnReject = view.findViewById(R.id.btn_reject);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_manage_apply, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        adapterContext = holder.itemView.getContext();
        Registration reg = applyList.get(position);
        holder.tvUser.setText("申请人：" + reg.getRealName());
        holder.tvProject.setText("手机号：" + reg.getPhoneNumber() + " | 学历：" + reg.getDegree());

        String auditState = reg.getAuditState();
        boolean isPending = auditState == null || auditState.isEmpty()
                || (!"通过".equals(auditState) && !"未通过".equals(auditState));

        if (isPending) {
            holder.tvAuditStatus.setText("● 待审核");
            holder.tvAuditStatus.setTextColor(Color.parseColor("#FF9800"));
            holder.btnApprove.setEnabled(true);
            holder.btnReject.setEnabled(true);
            holder.btnApprove.setText("通过");
            holder.btnReject.setText("驳回");
        } else if ("通过".equals(auditState)) {
            holder.tvAuditStatus.setText("● 通过");
            holder.tvAuditStatus.setTextColor(Color.parseColor("#4CAF50"));
            holder.btnApprove.setEnabled(false);
            holder.btnReject.setEnabled(false);
            holder.btnApprove.setText("已审核");
            holder.btnReject.setText("已审核");
        } else {
            holder.tvAuditStatus.setText("● 已驳回");
            holder.tvAuditStatus.setTextColor(Color.parseColor("#F44336"));
            holder.btnApprove.setEnabled(false);
            holder.btnReject.setEnabled(false);
            holder.btnApprove.setText("已审核");
            holder.btnReject.setText("已审核");
        }

        holder.btnApprove.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos < 0 || pos >= applyList.size()) return;
            Registration item = applyList.get(pos);
            holder.btnApprove.setEnabled(false);
            holder.btnReject.setEnabled(false);
            auditRegistration(item, "通过", pos);
        });

        holder.btnReject.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos < 0 || pos >= applyList.size()) return;
            Registration item = applyList.get(pos);
            holder.btnApprove.setEnabled(false);
            holder.btnReject.setEnabled(false);
            auditRegistration(item, "未通过", pos);
        });
    }

    @Override
    public int getItemCount() {
        return applyList.size();
    }

    private void auditRegistration(Registration reg, String auditResult, int position) {
        String url = ApiConfig.getBaseUrl() + "/registration/review/" + reg.getRegistrationId();
        JSONObject json = new JSONObject();
        try {
            String auditStateCode = "通过".equals(auditResult) ? "1" : "2";
            json.put("auditState", auditStateCode);
        } catch (Exception e) {
            e.printStackTrace();
            notifyItemChanged(position);
            return;
        }

        Log.d("ManageAdapter", "Audit request: " + json.toString());

        RequestBody body = RequestBody.create(ApiConfig.JSON, json.toString());
        Request request = new Request.Builder().url(url).put(body).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("ManageAdapter", "Audit onFailure", e);
                notifyItemChanged(position);
            }

            @Override
            public void onResponse(Call call, Response response) {
                String result = null;
                try {
                    if (response.body() != null) {
                        result = response.body().string();
                    }
                } catch (IOException e) {
                    Log.e("ManageAdapter", "Failed to read body", e);
                }

                Log.d("ManageAdapter", "Audit response code=" + response.code() + ", body=" + result);

                final String finalResult = result;
                final android.content.Context ctx = adapterContext;
                if (ctx == null) {
                    notifyItemChanged(position);
                    return;
                }

                if (ctx instanceof android.app.Activity) {
                    ((android.app.Activity) ctx).runOnUiThread(() -> {
                        if (finalResult == null) {
                            Toast.makeText(ctx, "服务器返回异常", Toast.LENGTH_SHORT).show();
                            notifyItemChanged(position);
                            return;
                        }
                        try {
                            JSONObject resObj = new JSONObject(finalResult);
                            if (resObj.has("code")) {
                                int code = resObj.getInt("code");
                                String msg = resObj.optString("message", auditResult + " 成功");
                                Toast.makeText(ctx, ApiConfig.friendlyMsg(msg), Toast.LENGTH_SHORT).show();
                                if (code == 200) {
                                    reg.setAuditState(auditResult);
                                    notifyItemChanged(position);
                                } else {
                                    notifyItemChanged(position);
                                }
                            } else {
                                notifyItemChanged(position);
                                String errMsg = resObj.optString("error",
                                        resObj.optString("message", "审核失败"));
                                Toast.makeText(ctx, errMsg, Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            Log.e("ManageAdapter", "Parse audit response error", e);
                            Toast.makeText(ctx, "操作失败", Toast.LENGTH_SHORT).show();
                            notifyItemChanged(position);
                        }
                    });
                } else {
                    notifyItemChanged(position);
                }
            }
        });
    }
}
