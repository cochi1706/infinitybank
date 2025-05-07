package com.mobile.infinitybank.model;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class BankAccount {
    private String id;
    private double balance;
    private String userId;
    private double amountDebt;
    private Date createdAt;
    private String accountPolicy;
    private String type;
    private double interestRate;
    private double monthlyProfit;
    private double monthlyPayment;

    public BankAccount(String id, String userId, String type) {
        this.id = id;
        this.userId = userId;
        this.createdAt = new Date();
        this.type = type;

        if (type.equals("Main")) {
            this.balance = 0;
        } else if (type.equals("Saving")) {
            this.balance = 50000;
            this.interestRate = 5;
            this.monthlyProfit = 0;
        } else {
            this.amountDebt = 1000000;
            this.monthlyPayment = 20000;
        }
    }
}
