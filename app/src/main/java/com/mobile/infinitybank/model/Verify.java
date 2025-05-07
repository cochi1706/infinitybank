package com.mobile.infinitybank.model;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Verify {
    private String id;
    private String otpString;
    private Date expiredAt;
    private String transactionId;

    public Verify(String id, String otpString, String transactionId) {
        this.id = id;
        this.otpString = otpString;
        this.expiredAt = new Date();
        this.expiredAt.setTime(this.expiredAt.getTime() + 600000);
        this.transactionId = transactionId;
    }
}
