package com.mobile.infinitybank.service;

import android.content.Context;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;
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
            userId = userId.split("_")[0].strip();
        }
        if (userId.contains("-")) {
            userId = userId.split("-")[0].strip();
        }
        notification.setUserId(userId);
        userService.getUserById(userId).addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot == null) {
                return;
            }
            User currentUser = documentSnapshot.toObject(User.class);
            String token = currentUser.getToken();
            if (token != null) {
                // Create FCM message payload
                String title = notification.getTitle();
                String body = notification.getContent();
                
                // Build FCM notification message
                RemoteMessage message = new RemoteMessage.Builder(token + "@gcm.googleapis.com")
                        .addData("title", title)
                        .addData("body", body)
                        .build();
                FirebaseMessaging.getInstance().send(message);
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