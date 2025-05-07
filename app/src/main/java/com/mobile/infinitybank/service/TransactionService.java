package com.mobile.infinitybank.service;

import android.content.Context;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.mobile.infinitybank.model.Transaction;
import com.mobile.infinitybank.model.NotificationItem;
import com.mobile.infinitybank.model.NotificationType;
import com.mobile.infinitybank.repository.TransactionRepository;

import javax.inject.Inject;

public class TransactionService {
    TransactionRepository transactionRepository;
    Context context;
    @Inject
    NotificationService notificationService;
    @Inject
    UserService userService;
    String currentUserId;
    
    @Inject
    public TransactionService(TransactionRepository transactionRepository, Context context, NotificationService notificationService, UserService userService) {
        this.transactionRepository = transactionRepository;
        this.context = context;
        this.notificationService = notificationService;
        this.userService = userService;
        this.currentUserId = userService.currentId;
    }

    public Task<Void> createTransaction(Transaction transaction) {
        NotificationItem notification = NotificationItem.createNotification("Biến động số dư", transaction.getDetails(), NotificationType.INFO);
        notification.setUserId(transaction.getSourceAccount());
        if (transaction.getAmount() < 0) {
            notification.setTitle("Giao dịch thành công");
            notification.setType(NotificationType.SUCCESS);
        }
        notificationService.createNotification(notification);
        return transactionRepository.createTransaction(transaction);
    }

    public Task<QuerySnapshot> getTransactionsByUserId(String userId) {
        return transactionRepository.getTransactionsByUserId(userId);
    }

    public Task<QuerySnapshot> getFilteredTransactions(String criteria) {
        return transactionRepository.getFilteredTransactions(criteria);
    }

    public Task<DocumentSnapshot> getTransactionById(String transactionId) {
        return transactionRepository.find(transactionId);
    }
}