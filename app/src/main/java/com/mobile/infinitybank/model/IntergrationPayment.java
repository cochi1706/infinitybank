package com.mobile.infinitybank.model;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class IntergrationPayment {
    private String id;
    private String gatewayName;
    private String TransactionRef;
    private String billId;
    private Date createdAt;

    public IntergrationPayment(String gatewayName, String TransactionRef, String billId) {
        this.gatewayName = gatewayName;
        this.TransactionRef = TransactionRef;
        this.billId = billId;
        this.createdAt = new Date();
    }
}
