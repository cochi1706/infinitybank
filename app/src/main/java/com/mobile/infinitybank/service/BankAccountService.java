package com.mobile.infinitybank.service;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.mobile.infinitybank.model.BankAccount;
import com.mobile.infinitybank.repository.BankAccountRepository;

import javax.inject.Inject;

public class BankAccountService {
    BankAccountRepository bankAccountRepository;
    AppStatusService appStatusService;

    @Inject
    public BankAccountService(BankAccountRepository bankAccountRepository,
            AppStatusService appStatusService) {
        this.bankAccountRepository = bankAccountRepository;
        this.appStatusService = appStatusService;
    }

    public Task<Void> createBankAccount(BankAccount bankAccount) {
        return bankAccountRepository.createBankAccount(bankAccount);
    }

    public Task<Void> register(String number) {
        BankAccount bankAccount = new BankAccount(number, number, "Main");
        return createBankAccount(bankAccount);
    }

    public Task<Void> registerSavingAccount(String number) {
        String savingId = number + "_saving";
        BankAccount bankAccount = new BankAccount(savingId, number, "Saving");
        return createBankAccount(bankAccount);
    }

    public Task<Void> registerLoanAccount(String number) {
        String loanId = number + "_loan";
        BankAccount bankAccount = new BankAccount(loanId, number, "Loan");
        return createBankAccount(bankAccount);
    }

    public Task<Void> updateBankAccount(BankAccount bankAccount) {
        return bankAccountRepository.updateBankAccount(bankAccount);
    }

    public Task<Void> deposit(String currentId, double amount) {
        return bankAccountRepository.find(currentId).continueWithTask(task -> {
            DocumentSnapshot documentSnapshot = task.getResult();
            BankAccount bankAccount = documentSnapshot.toObject(BankAccount.class);
            double balance = bankAccount.getBalance() + amount;
            bankAccount.setBalance(balance);
            return updateBankAccount(bankAccount);
        });
    }

    public Task<Void> deposit(String currentId, String id, double amount) {
        return bankAccountRepository.find(id).continueWithTask(task -> {
            DocumentSnapshot documentSnapshot = task.getResult();
            BankAccount bankAccount = documentSnapshot.toObject(BankAccount.class);
            double balance = bankAccount.getBalance() + amount;
            bankAccount.setBalance(balance);
            deposit(currentId, amount);
            return updateBankAccount(bankAccount);
        });
    }

    public Task<Void> withdraw(String currentId, double amount) {
        return bankAccountRepository.find(currentId).continueWithTask(task -> {
            DocumentSnapshot documentSnapshot = task.getResult();
            BankAccount bankAccount = documentSnapshot.toObject(BankAccount.class);
            double balance = bankAccount.getBalance() - amount;
            bankAccount.setBalance(balance);
            return updateBankAccount(bankAccount);
        });
    }

    public Task<Void> withdraw(String currentId, String id, double amount) {
        return bankAccountRepository.find(id).continueWithTask(task -> {
            DocumentSnapshot documentSnapshot = task.getResult();
            BankAccount bankAccount = documentSnapshot.toObject(BankAccount.class);
            double balance = bankAccount.getBalance() - amount;
            bankAccount.setBalance(balance);
            withdraw(currentId, amount);
            return updateBankAccount(bankAccount);
        });
    }

    public Task<Void> changeAmountDebt(String currentId, double amount) {
        return bankAccountRepository.find(currentId).continueWithTask(task -> {
            DocumentSnapshot documentSnapshot = task.getResult();
            BankAccount bankAccount = documentSnapshot.toObject(BankAccount.class);
            double amountDebt = bankAccount.getAmountDebt() + amount;
            bankAccount.setAmountDebt(amountDebt);
            return updateBankAccount(bankAccount);
        });
    }

    public Task<Double> getBalance(String currentId) {
        return bankAccountRepository.find(currentId).continueWith(task -> {
            DocumentSnapshot documentSnapshot = task.getResult();
            BankAccount bankAccount = documentSnapshot.toObject(BankAccount.class); 
            return bankAccount.getBalance();
        });
    }

    public Task<Void> removeBankAccount(String currentId) {
        return bankAccountRepository.remove(currentId);
    }

    public Task<QuerySnapshot> getBankAccounts(String currentId) {
        return bankAccountRepository.findByField("userId", currentId);
    }

    public Task<DocumentSnapshot> getBankAccount(String id) {
        return bankAccountRepository.find(id);
    }

    public Task<Void> updateBankAccount(String accountId, double amount) {
        return bankAccountRepository.find(accountId).continueWithTask(task -> {
            DocumentSnapshot documentSnapshot = task.getResult();
            BankAccount bankAccount = documentSnapshot.toObject(BankAccount.class);
            bankAccount.setBalance(bankAccount.getBalance() + amount);
            return updateBankAccount(bankAccount);
        });
    }
}
