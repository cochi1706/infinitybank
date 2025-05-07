package com.mobile.infinitybank.model;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class SavingPolicy {
    private String id;
    private double profitRate;
    private Date createdAt;

    public SavingPolicy(double profitRate) {
        this.profitRate = profitRate;
        this.createdAt = new Date();
    }
}
