package com.mobile.infinitybank.repository;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.mobile.infinitybank.model.Bill;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class BillRepository extends BaseRepository<Bill> {
    @Inject
    public BillRepository() {
        super("bill");
    }

    public Task<Void> createBill(Bill bill) {
        return db.collection(collectionName)
                .document(bill.getId())
                .set(bill);
    }

    public Task<Void> updateBill(Bill bill) {
        return db.collection(collectionName)
                .document(bill.getId())
                .set(bill);
    }

    public Task<QuerySnapshot> getBillsByUserId(String userId) {
        return db.collection(collectionName)
                .whereEqualTo("userId", userId)
                .get();
    }

    public Task<QuerySnapshot> getLast3BillByUserId(String userId) {
        return db.collection(collectionName)
                .whereEqualTo("userId", userId)
                .orderBy("month", Query.Direction.DESCENDING)
                .orderBy("year", Query.Direction.DESCENDING)
                .limit(3)
                .get();
    }
}