package com.mobile.infinitybank.model;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class MortgagePolicy {
    private String id;
    private long monthlyPayment;
    private long weeklyPayment;
    private Date createdAt;

    public MortgagePolicy(String id, long monthlyPayment, long weeklyPayment) {
        this.id = id;
        this.monthlyPayment = monthlyPayment;
        this.weeklyPayment = weeklyPayment;
        this.createdAt = new Date();
    }
}
