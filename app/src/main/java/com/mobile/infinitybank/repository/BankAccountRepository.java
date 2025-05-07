package com.mobile.infinitybank.repository;

import com.google.android.gms.tasks.Task;
import com.mobile.infinitybank.model.BankAccount;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class BankAccountRepository extends BaseRepository<BankAccount> {
    @Inject
    public BankAccountRepository() {
        super("bankAccount");
    }

    public Task<Void> createBankAccount(BankAccount bankAccount) {
        return db.collection(collectionName)
                .document(bankAccount.getId())
                .set(bankAccount);
    }

    public Task<Void> updateBankAccount(BankAccount bankAccount) {
        return db.collection(collectionName)
                .document(bankAccount.getId())
                .set(bankAccount);
    }
}
