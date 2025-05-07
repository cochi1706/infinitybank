package com.mobile.infinitybank.model;

import com.mobile.infinitybank.R;
import java.util.Date;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class NotificationItem {
    private String id;
    private String userId;
    protected Date time;
    private String title;
    private String content;
    private NotificationType type;
    private int iconRes;
    private String itemType = "NOTIFICATION"; // Default value

    public static NotificationItem createHeader(String title) {
        return createHeader(title, R.drawable.ic_notifications);
    }

    public static NotificationItem createHeader(String title, int iconRes) {
        NotificationItem item = new NotificationItem();
        item.setTitle(title);
        item.setIconRes(iconRes);
        item.setItemType("HEADER");
        return item;
    }

    public static NotificationItem createNotification(String title, String content) {
        return createNotification(title, content, NotificationType.INFO);
    }

    public static NotificationItem createNotification(String title, String content, NotificationType type) {
        NotificationItem item = new NotificationItem();
        item.setTitle(title);
        item.setContent(content);
        item.setTime(new Date());
        item.setType(type);
        item.setItemType("NOTIFICATION");
        return item;
    }
} 