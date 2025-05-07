package com.mobile.infinitybank.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mobile.infinitybank.databinding.ItemNotificationBinding;
import com.mobile.infinitybank.databinding.ItemNotificationHeaderBinding;
import com.mobile.infinitybank.model.NotificationItem;

import java.util.Date;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_NOTIFICATION = 1;

    private final List<NotificationItem> items;

    public NotificationAdapter(List<NotificationItem> items) {
        // Sort notifications by time in descending order (newest first)
        items.sort((item1, item2) -> {
            if (item1.getTime() == null || item2.getTime() == null) {
                return 0;
            }
            return item2.getTime().compareTo(item1.getTime());
        });
        this.items = items;
    }

    public class HeaderViewHolder extends RecyclerView.ViewHolder {
        private final ItemNotificationHeaderBinding binding;

        public HeaderViewHolder(ItemNotificationHeaderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(NotificationItem header) {
            binding.tvHeaderTitle.setText(header.getTitle());
            binding.ivHeaderIcon.setImageResource(header.getIconRes());
        }
    }

    public class NotificationViewHolder extends RecyclerView.ViewHolder {
        private final ItemNotificationBinding binding;

        public NotificationViewHolder(ItemNotificationBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(NotificationItem notification) {
            binding.tvNotificationTitle.setText(notification.getTitle());
            binding.tvNotificationContent.setText(notification.getContent());
            Date time = notification.getTime();
            binding.tvNotificationTime.setText(String.format("%tR %td/%tm/%tY", time, time, time, time));
            binding.ivNotificationIcon.setImageResource(notification.getType().getIconRes());
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case TYPE_HEADER:
                ItemNotificationHeaderBinding headerBinding = ItemNotificationHeaderBinding.inflate(inflater, parent, false);
                return new HeaderViewHolder(headerBinding);
            case TYPE_NOTIFICATION:
                ItemNotificationBinding notificationBinding = ItemNotificationBinding.inflate(inflater, parent, false);
                return new NotificationViewHolder(notificationBinding);
            default:
                throw new IllegalArgumentException("Invalid view type");
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).bind((NotificationItem) items.get(position));
        } else if (holder instanceof NotificationViewHolder) {
            ((NotificationViewHolder) holder).bind((NotificationItem) items.get(position));
        }
    }

    @Override
    public int getItemViewType(int position) {
        NotificationItem item = items.get(position);
        if (item.getItemType().equals("HEADER")) {
            return TYPE_HEADER;
        } else if (item.getItemType().equals("NOTIFICATION")) {
            return TYPE_NOTIFICATION;
        }
        throw new IllegalArgumentException("Invalid item type at position " + position);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
} 