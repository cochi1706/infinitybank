package com.mobile.infinitybank.controller.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.mobile.infinitybank.databinding.FragmentAccountInfoBinding;
import com.mobile.infinitybank.model.BankAccount;
import com.mobile.infinitybank.model.User;
import com.mobile.infinitybank.service.BankAccountService;
import com.mobile.infinitybank.service.UserService;

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AccountInfoFragment extends Fragment {

    private FragmentAccountInfoBinding binding;
    @Inject
    UserService userService;
    @Inject
    BankAccountService bankAccountService;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        try {
            binding = FragmentAccountInfoBinding.inflate(inflater, container, false);
            return binding.getRoot();
        } catch (Exception e) {
            Log.e("AccountInfoFragment", "Error inflating layout", e);
            throw e;
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        showLoading(true);
        userService.getCurrentUser().addOnSuccessListener(documentSnapshot -> {
            User user = documentSnapshot.toObject(User.class);
            String phone = user.getPhoneNumber();
            bankAccountService.getBankAccounts(phone).addOnSuccessListener(querySnapshot -> {
                List<BankAccount> accounts = querySnapshot.getDocuments().stream()
                    .map(documentSnapshot2 -> documentSnapshot2.toObject(BankAccount.class))
                    .collect(Collectors.toList());
                displayAccounts(accounts);
                showLoading(false);
            });
        });
    }

    private void displayAccounts(List<BankAccount> accounts) {
        // Định dạng số tiền với dấu phân cách hàng nghìn
        NumberFormat numberFormat = NumberFormat.getNumberInstance(new Locale("vi", "VN"));

        for (BankAccount account : accounts) {
            switch (account.getType()) {
                case "Main":
                    binding.tvMainBalance.setText(numberFormat.format(account.getBalance()) + " VND");
                    break;
                case "Saving":
                    binding.savingAccount.setVisibility(View.VISIBLE);
                    binding.tvSavingBalance.setText(numberFormat.format(account.getBalance()) + " VND");
                    if (account.getInterestRate() != 0) {
                        binding.tvSavingInterestRate.setText((int)(account.getInterestRate() * 100) + "%");
                    }
                    double monthlyProfit = account.getBalance() *
                            (account.getInterestRate() / 100) / 12;
                    String formattedMonthlyProfit = String.format("%,d VND", (long) monthlyProfit);
                    binding.tvSavingMonthlyProfit.setText(formattedMonthlyProfit);
                    break;
                case "Loan":
                    binding.loanAccount.setVisibility(View.VISIBLE);
                    binding.tvLoanBalance.setText(numberFormat.format(account.getMonthlyPayment()) + " VND");
                    break;
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void showLoading(boolean show) {
        binding.loadingOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
    }
} 