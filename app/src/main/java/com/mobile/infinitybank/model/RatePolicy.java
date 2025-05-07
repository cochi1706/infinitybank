package com.mobile.infinitybank.model;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class RatePolicy {
    private String id;
    private double rate;
    private String name;
    private Date createdAt;
    private Date updatedAt;

    public RatePolicy(String id, double rate, String name) {
        this.rate = rate;
        this.name = name;
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }
}
