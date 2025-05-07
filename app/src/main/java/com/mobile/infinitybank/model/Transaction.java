package com.mobile.infinitybank.model;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Transaction {
    private String id;
    private String description;
    private String details;
    private double amount;
    private boolean isIncoming;
    private String sourceAccount;
    private String destinationAccount;
    private Date date;
    private String method;
    private String status;
    private double balance;
    private String time;

    public Transaction(String sourceAccount, String destinationAccount, double amount, String description, String details) {
        this.id = String.valueOf(System.currentTimeMillis());
        this.description = description;
        this.amount = amount;
        this.sourceAccount = sourceAccount;
        this.destinationAccount = destinationAccount;
        this.date = new Date();
        this.details = details;
    }
}
