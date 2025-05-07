package com.mobile.infinitybank.controller.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.mobile.infinitybank.R;
import com.mobile.infinitybank.controller.CreateAccountActivity;
import com.mobile.infinitybank.controller.EditCustomerInfoActivity;
import com.mobile.infinitybank.controller.SavingActivity;
import com.mobile.infinitybank.controller.ScanVNPayActivity;
import com.mobile.infinitybank.databinding.FragmentMainBinding;
import com.mobile.infinitybank.controller.TransferActivity;
import com.mobile.infinitybank.controller.PaymentActivity;
import com.mobile.infinitybank.controller.PhoneTopupActivity;
import com.mobile.infinitybank.model.User;
import com.mobile.infinitybank.service.UserService;
import com.mobile.infinitybank.service.BankAccountService;

import java.text.NumberFormat;
import java.util.Date;
import java.util.Locale;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainFragment extends Fragment {
    @Inject
    UserService userService;
    @Inject
    BankAccountService bankAccountService;
    private FragmentMainBinding binding;
    private boolean isBalanceVisible = true; // Trạng thái hiển thị số dư
    private String balance = "";
    private final String hiddenBalanceText = "***********"; // Văn bản khi ẩn số dư
    private User currentUser = new User();
    private boolean isEmployee = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMainBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Request notification permission
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            requireActivity().requestPermissions(
                new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                1
            );
        }

        // Request screenshot permission 
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            requireActivity().requestPermissions(
                new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE, 
                           android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                2
            );
        }

        // Thiết lập ban đầu
        showLoading(true);
        userService.getCurrentUser().addOnSuccessListener(documentSnapshot -> {
            if (!isAdded() || binding == null) {
                return;  // Fragment is no longer attached or view is destroyed
            }
            currentUser = documentSnapshot.toObject(User.class);
            if (currentUser != null) {
                binding.tvName.setText(currentUser.getFullName());
                if (!currentUser.getUserType().equals("customer")) {
                    isEmployee = true;
                    binding.onlyEmployee.setVisibility(View.VISIBLE);
                } else {
                    isEmployee = false;
                    binding.onlyEmployee.setVisibility(View.GONE);
                }
                bankAccountService.getBalance(currentUser.getId()).addOnSuccessListener(result -> {
                    if (!isAdded() || binding == null) {
                        return;
                    }
                    balance = result.toString();
                    binding.tvBalance.setText(formatAmount(Double.parseDouble(balance)) + " VND");
                    showLoading(false);
                }).addOnFailureListener(e -> {
                    if (isAdded()) {
                        showLoading(false);
                    }
                });
            } else {
                showLoading(false);
            }
        }).addOnFailureListener(e -> {
            if (isAdded()) {
                showLoading(false);
            }
        });
        
        // Xử lý nhấn vào con mắt để ẩn/hiển thị số dư
        binding.ivToggleBalance.setOnClickListener(v -> {
            isBalanceVisible = !isBalanceVisible;
            if (isBalanceVisible) {
                binding.tvBalance.setText(formatAmount(Double.parseDouble(balance)) + " VND");
                binding.ivToggleBalance.setImageResource(R.drawable.ic_eye_on);
            } else {
                binding.tvBalance.setText(hiddenBalanceText);
                binding.ivToggleBalance.setImageResource(R.drawable.ic_eye_off);
            }
        });

        // Thiết lập sự kiện nhấn cho Button "Chuyển tiền"
        binding.tvTransfer.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(requireContext(), TransferActivity.class);
                startActivity(intent);
            } catch (Exception e) {
            }
        });

        // Thiết lập sự kiện nhấn cho Button "Thanh toán"
        binding.tvPayments.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(requireContext(), PaymentActivity.class);
                startActivity(intent);
            } catch (Exception e) {
            }
        });

        // Thiết lập sự kiện nhấn cho Button "Nạp tiền ĐT"
        binding.tvTopup.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(requireContext(), PhoneTopupActivity.class);
                startActivity(intent);
            } catch (Exception e) {
            }
        });

        binding.tvVNPayPayment.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), ScanVNPayActivity.class);
            startActivity(intent);
        });

        binding.tvSavings.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), SavingActivity.class);
            startActivity(intent);
        });

        binding.tvEditCustomerInfo.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), EditCustomerInfoActivity.class);
            startActivity(intent);
        });

        binding.tvCreateAccount.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(requireContext(), CreateAccountActivity.class);
                startActivity(intent);
            } catch (Exception e) {
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private String formatAmount(double amount) {
        NumberFormat numberFormat = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        return numberFormat.format(amount);
    }

    private void showLoading(boolean show) {
        binding.loadingOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}
