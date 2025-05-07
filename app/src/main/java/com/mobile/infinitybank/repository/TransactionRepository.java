package com.mobile.infinitybank.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.mobile.infinitybank.model.Transaction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class TransactionRepository extends BaseRepository<Transaction> {
    private static final String COLLECTION_NAME = "transactions";
    @Inject
    public TransactionRepository() {
        super(COLLECTION_NAME);
    }

    public void getAllTransactions(OnTransactionsFetchedListener listener) {
        db.collection(COLLECTION_NAME)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Transaction> transactions = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Transaction transaction = document.toObject(Transaction.class);
                        transactions.add(transaction);
                    }
                    listener.onTransactionsFetched(transactions);
                })
                .addOnFailureListener(e -> {
                    listener.onError(e.getMessage());
                });
    }

    public void getTransactionById(String transactionId, OnTransactionFetchedListener listener) {
        db.collection(COLLECTION_NAME)
                .document(transactionId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Transaction transaction = documentSnapshot.toObject(Transaction.class);
                        listener.onTransactionFetched(transaction);
                    } else {
                        listener.onError("Transaction not found");
                    }
                })
                .addOnFailureListener(e -> {
                    listener.onError(e.getMessage());
                });
    }

    public void getFilteredTransactions(String criteria, OnTransactionsFetchedListener listener) {
        db.collection(COLLECTION_NAME)
                .whereEqualTo("status", criteria) // Adjust this based on your filtering needs
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Transaction> transactions = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Transaction transaction = document.toObject(Transaction.class);
                        transactions.add(transaction);
                    }
                    listener.onTransactionsFetched(transactions);
                })
                .addOnFailureListener(e -> {
                    listener.onError(e.getMessage());
                });
    }

    // Callback interfaces
    public interface OnTransactionsFetchedListener {
        void onTransactionsFetched(List<Transaction> transactions);
        void onError(String errorMessage);
    }

    public interface OnTransactionFetchedListener {
        void onTransactionFetched(Transaction transaction);
        void onError(String errorMessage);
    }

    public Task<Void> createTransaction(Transaction transaction) {
        transaction.setId(String.valueOf(System.currentTimeMillis()));
        transaction.setStatus("Thành công");
        return db.collection(COLLECTION_NAME)
                .document(transaction.getId())
                .set(transaction);
    }

    public Task<QuerySnapshot> getTransactionsByUserId(String userId) {
        List<String> sourceAccounts = Arrays.asList(userId, userId + "_saving", userId + "_loan");
        return db.collection(COLLECTION_NAME)
                .whereIn("sourceAccount", sourceAccounts)
                .orderBy("date", Query.Direction.DESCENDING)
                .get();
    }

    public Task<QuerySnapshot> getFilteredTransactions(String criteria) {
        return db.collection(COLLECTION_NAME)
                .whereEqualTo("status", criteria)
                .get();
    }
}