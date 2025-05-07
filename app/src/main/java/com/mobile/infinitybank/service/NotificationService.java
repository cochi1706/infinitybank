package com.mobile.infinitybank.service;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.mobile.infinitybank.R;
import com.mobile.infinitybank.model.NotificationItem;
import com.mobile.infinitybank.model.User;
import com.mobile.infinitybank.repository.NotificationRepository;

import javax.inject.Inject;

public class NotificationService {
    NotificationRepository notificationRepository;
    Context context;
    @Inject
    UserService userService;
    String currentUserId = "";

    @Inject
    public NotificationService(NotificationRepository notificationRepository, Context context) {
        this.notificationRepository = notificationRepository;
        this.context = context;
    }

    public Task<Void> createNotification(NotificationItem notification) {
        String userId = notification.getUserId();
        if (userId.contains("_")) {
            userId = userId.split("_")[0];
        }
        notification.setUserId(userId);
        userService.getCurrentUser().addOnSuccessListener(DocumentSnapshot -> {
            User currentUser = DocumentSnapshot.toObject(User.class);
            currentUserId = currentUser.getId();
            String id = notification.getUserId();
            if (id.contains("_")) {
                id = id.split("_")[0];
            }
            if (currentUserId.equals(id)) {
                NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "channel_id")
                        .setSmallIcon(R.drawable.ic_bank)
                        .setContentTitle(notification.getTitle())
                        .setContentText(notification.getContent())
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);
            }
        });
        return notificationRepository.createNotification(notification);
    }

    public Task<Void> updateNotification(NotificationItem notification) {
        return notificationRepository.updateNotification(notification);
    }

    public Task<QuerySnapshot> getNotificationsByUserId(String userId) {
        return notificationRepository.findByField("userId", userId);
    }

    public Task<QuerySnapshot> get10NotificationsByUserId(String userId) {
        return notificationRepository.find10ByField("userId", userId);
    }

    public Task<DocumentSnapshot> getNotificationById(String notificationId) {
        return notificationRepository.find(notificationId);
    }

    public Task<Void> deleteNotification(String notificationId) {
        return notificationRepository.remove(notificationId);
    }
} 