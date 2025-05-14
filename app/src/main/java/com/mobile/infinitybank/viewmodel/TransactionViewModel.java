package com.mobile.infinitybank.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;

import com.google.firebase.firestore.DocumentSnapshot;
import com.mobile.infinitybank.model.Transaction;
import com.mobile.infinitybank.service.TransactionService;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class TransactionViewModel extends AndroidViewModel {
    private final SavedStateHandle savedStateHandle;
    private final TransactionService transactionService;
    private String currentUserId;
    
    // LiveData for storing transaction list
    private final MutableLiveData<List<Transaction>> transactions = new MutableLiveData<>();
    
    // LiveData for storing selected transaction
    private final MutableLiveData<Transaction> selectedTransaction = new MutableLiveData<>();

    private final MutableLiveData<String> error = new MutableLiveData<>();

    @Inject
    public TransactionViewModel(@NonNull Application application, @NonNull SavedStateHandle savedStateHandle, TransactionService transactionService) {
        super(application);
        this.savedStateHandle = savedStateHandle;
        this.transactionService = transactionService;
        // Restore selected transaction if exists
        String savedTransaction = savedStateHandle.get("selected_transaction_id");
        if (savedTransaction != null) {
            selectTransaction(savedTransaction);
        }
    }

    // Getter for transactions LiveData
    public LiveData<List<Transaction>> getTransactions() {
        return transactions;
    }

    // Getter for selected transaction LiveData
    public LiveData<Transaction> getSelectedTransaction() {
        return selectedTransaction;
    }

    public LiveData<String> getError() {
        return error;
    }

    // Method to load all transactions
    public void loadTransactions(String currentUserId) {
        this.currentUserId = currentUserId;
        transactionService.getTransactionsByUserId(currentUserId).addOnSuccessListener(queryDocumentSnapshots -> {
            List<Transaction> transactionList = queryDocumentSnapshots.toObjects(Transaction.class);
            // Collections.sort(transactionList, (t1, t2) -> t1.getDate().compareTo(t2.getDate())); // Sort ascending
            transactions.postValue(transactionList);
        });
    }

    // Method to select a transaction by ID
    public void selectTransaction(String transactionId) {
        transactionService.getTransactionById(transactionId).addOnSuccessListener(documentSnapshot -> {
            selectedTransaction.postValue(documentSnapshot.toObject(Transaction.class));
        });
    }

    // Method to clear selected transaction (used when navigating back)
    public void clearSelectedTransaction() {
        selectedTransaction.postValue(null);
        savedStateHandle.remove("selected_transaction_id");
    }

    // Optional: Method to refresh transactions
    public void refreshTransactions() {
        loadTransactions(currentUserId);
    }

    // Optional: Method to filter transactions
    public void filterTransactions(String criteria) {
    }

    // Optional: Method to get transaction count
    public int getTransactionCount() {
        List<Transaction> currentTransactions = transactions.getValue();
        return currentTransactions != null ? currentTransactions.size() : 0;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        // Clean up any resources if needed
    }
} 