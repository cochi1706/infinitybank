package com.mobile.infinitybank.model;

import java.util.Date;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Bill {
    private String id;
    private String userId;
    private String serviceType;
    private Date completedAt;
    private String status;
    private double amount;
    private int month;
    private int year;

    public Bill(String userId, String serviceType, double amount, int month, int year) {
        this.id = String.valueOf(System.currentTimeMillis());
        this.userId = userId;
        this.serviceType = serviceType;
        this.month = month;
        this.year = year;
        this.status = "Chưa thanh toán";
        this.amount = amount;
    }
}
