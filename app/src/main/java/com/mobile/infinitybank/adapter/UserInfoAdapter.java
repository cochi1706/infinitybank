package com.mobile.infinitybank.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mobile.infinitybank.model.UserInfo;

import java.util.ArrayList;
import java.util.List;

public class UserInfoAdapter extends RecyclerView.Adapter<UserInfoAdapter.UserInfoViewHolder> {
    private final List<UserInfo> userInfoList;

    public UserInfoAdapter() {
        this.userInfoList = new ArrayList<>();
    }

    @NonNull
    @Override
    public UserInfoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_2, parent, false);
        return new UserInfoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserInfoViewHolder holder, int position) {
        UserInfo userInfo = userInfoList.get(position);
        holder.tvLabel.setText(userInfo.getLabel());
        holder.tvValue.setText(userInfo.getValue());
    }

    @Override
    public int getItemCount() {
        return userInfoList.size();
    }

    public void updateData(List<UserInfo> newUserInfoList) {
        userInfoList.clear();
        userInfoList.addAll(newUserInfoList);
        notifyDataSetChanged();
    }

    public static class UserInfoViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvLabel;
        private final TextView tvValue;

        public UserInfoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLabel = itemView.findViewById(android.R.id.text1);
            tvValue = itemView.findViewById(android.R.id.text2);
        }
    }
} 