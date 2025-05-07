package com.mobile.infinitybank.model;

import com.mobile.infinitybank.R;

public enum NotificationType {
    INFO(R.drawable.ic_info),
    SUCCESS(R.drawable.ic_success),
    WARNING(R.drawable.ic_warning),
    PROMOTION(R.drawable.ic_notifications),
    ERROR(R.drawable.ic_error);

    private final int iconRes;

    NotificationType(int iconRes) {
        this.iconRes = iconRes;
    }

    public int getIconRes() {
        return iconRes;
    }
} 