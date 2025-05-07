package com.mobile.infinitybank.controller;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

import com.mobile.infinitybank.databinding.ActivitySavingBinding;
import com.mobile.infinitybank.model.BankAccount;
import com.mobile.infinitybank.model.User;
import com.mobile.infinitybank.service.BankAccountService;
import com.mobile.infinitybank.service.UserService;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SavingActivity extends AppCompatActivity {
    private ActivitySavingBinding binding;
    @Inject
    UserService userService;
    @Inject
    BankAccountService bankAccountService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySavingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        showLoading(true);
        userService.getCurrentUser().addOnSuccessListener(documentSnapshot -> {
            User user = documentSnapshot.toObject(User.class);
            String phone = user.getPhoneNumber();
            bankAccountService.getBankAccount(phone+"_saving").addOnSuccessListener(documentSnapshot1 -> {
                BankAccount bankAccount = documentSnapshot1.toObject(BankAccount.class);
                updateUI(bankAccount);
                showLoading(false);
            });
        });

        // Set up click listeners
        setupClickListeners();
    }

    private void setupClickListeners() {
        binding.ivBack.setOnClickListener(v -> finish());
    }

    private void updateUI(BankAccount savingAccount) {
        if (savingAccount != null) {
            // Format and display balance
            if (savingAccount.getBalance() == 0) {
                binding.tvBalance.setText("0 VND");
            }
            else {
                String formattedBalance = String.format("%,d VND", savingAccount.getBalance());
                binding.tvBalance.setText(formattedBalance);
            }

            // Format and display interest rate
            String formattedInterestRate = String.format("%.2f%%", savingAccount.getInterestRate());
            binding.tvInterestRate.setText(formattedInterestRate);

            // Calculate and display monthly profit
            double monthlyProfit = savingAccount.getBalance() * 
                    (savingAccount.getInterestRate() / 100) / 12;
            String formattedMonthlyProfit = String.format("%,d VND", (long) monthlyProfit);
            binding.tvMonthlyProfit.setText(formattedMonthlyProfit);
        }
        else {
            new AlertDialog.Builder(this)
            .setTitle("Bạn chưa có tài khoản tiết kiệm")
            .setMessage("Bạn có muốn đăng ký tài khoản tiết kiệm?")
            .setPositiveButton("Có", (dialog, which) -> {
                Intent intent = new Intent(SavingActivity.this, RegisterSavingActivity.class);
                startActivity(intent);
            })
            .setNegativeButton("Không", (dialog, which) -> {
                finish();
            })
            .setCancelable(true)
            .show();
        }
    }

    private void showLoading(boolean show) {
        if (binding != null) {
            binding.loadingOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
