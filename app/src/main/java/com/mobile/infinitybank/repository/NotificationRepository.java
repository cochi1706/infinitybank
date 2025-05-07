package com.mobile.infinitybank.repository;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.mobile.infinitybank.model.NotificationItem;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class NotificationRepository extends BaseRepository<NotificationItem> {
    @Inject
    public NotificationRepository() {
        super("notifications");
    }

    public Task<Void> createNotification(NotificationItem notification) {
        notification.setId(UUID.randomUUID().toString());
        return db.collection(collectionName)
                .document(notification.getId())
                .set(notification);
    }

    public Task<Void> updateNotification(NotificationItem notification) {
        return db.collection(collectionName)
                .document(notification.getId())
                .set(notification);
    }

    public Task<QuerySnapshot> find10ByField(String field, String value) {
        return db.collection(collectionName)
                .whereEqualTo(field, value)
                .orderBy("time", Query.Direction.DESCENDING)
                .limit(10)
                .get();
    }
} 