package com.mobile.infinitybank.service;

import android.content.Context;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.mobile.infinitybank.model.Bill;
import com.mobile.infinitybank.repository.BillRepository;

import javax.inject.Inject;

public class BillService {
    BillRepository billRepository;
    Context context;

    @Inject
    public BillService(BillRepository billRepository, Context context) {
        this.billRepository = billRepository;
        this.context = context;
    }

    public Task<Void> createBill(Bill bill) {
        return billRepository.createBill(bill);
    }

    public Task<Void> updateBill(Bill bill) {
        return billRepository.updateBill(bill);
    }

    public Task<QuerySnapshot> getLast3BillByUserId(String userId) {
        return billRepository.getLast3BillByUserId(userId);
    }
}