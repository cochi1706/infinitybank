package com.mobile.infinitybank.controller.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.DocumentSnapshot;
import com.mobile.infinitybank.databinding.FragmentDepositWithdrawBinding;
import com.mobile.infinitybank.model.BankAccount;
import com.mobile.infinitybank.model.Transaction;
import com.mobile.infinitybank.model.User;
import com.mobile.infinitybank.service.BankAccountService;
import com.mobile.infinitybank.service.TransactionService;
import com.mobile.infinitybank.service.UserService;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class DepositWithdrawFragment extends Fragment {

    @Inject
    UserService userService;
    @Inject
    BankAccountService bankAccountService;
    @Inject
    TransactionService transactionService;

    private FragmentDepositWithdrawBinding binding;
    private final Map<String, Double> accounts = new HashMap<>();
    private User currentUser;

    private String selectedAccount = null;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        try {
            binding = FragmentDepositWithdrawBinding.inflate(inflater, container, false);
            return binding.getRoot();
        } catch (Exception e) {
            Log.e("DepositWithdrawFragment", "Error inflating layout", e);
            throw e;
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        showLoading(true);
        userService.getCurrentUser().addOnSuccessListener(documentSnapshot -> {
            currentUser = documentSnapshot.toObject(User.class);
            String phone = currentUser.getPhoneNumber();
            bankAccountService.getBankAccounts(phone).addOnSuccessListener(querySnapshot -> {
                for (DocumentSnapshot documentSnapshot2 : querySnapshot.getDocuments()) {
                    BankAccount account = documentSnapshot2.toObject(BankAccount.class);
                    accounts.put(account.getType(), account.getBalance());
                }
                setupSpinner();
                setupListeners();
                showLoading(false);
            });
        });
    }

    private void setupSpinner() {
        // Danh sách tài khoản nguồn
        List<String> accountTypes = new ArrayList<>(accounts.keySet());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, accountTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerSourceAccount.setAdapter(adapter);
        binding.spinnerSourceAccount.setSelection(accountTypes.indexOf("Main"));

        // Hiển thị số dư hiện tại khi chọn tài khoản
        binding.spinnerSourceAccount.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedAccount = accountTypes.get(position);
                double balance = accounts.getOrDefault(selectedAccount, 0.0);
                NumberFormat numberFormat = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
                binding.tvCurrentBalance.setText("Số dư hiện tại: " + numberFormat.format(balance) + " VND");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedAccount = null;
                binding.tvCurrentBalance.setText("Số dư hiện tại: 0 VND");
            }
        });
    }

    private void setupListeners() {
        binding.btnDeposit.setOnClickListener(v -> {
            Double amount = null;
            try {
                amount = Double.parseDouble(binding.etDepositAmount.getText().toString());
            } catch (NumberFormatException ignored) {}
            String password = binding.etTransactionPassword.getText().toString();

            // Kiểm tra dữ liệu đầu vào
            if (!validateInput(amount, password)) return;

            // Cập nhật số dư
            updateBalance(amount, true);
            Toast.makeText(requireContext(), "Đã nạp " + formatAmount(amount) + " VND", Toast.LENGTH_SHORT).show();

            // Xóa dữ liệu nhập
            binding.etDepositAmount.getText().clear();
            binding.etTransactionPassword.getText().clear();
        });

        binding.btnWithdraw.setOnClickListener(v -> {
            Double amount = null;
            try {
                amount = Double.parseDouble(binding.etWithdrawAmount.getText().toString());
            } catch (NumberFormatException ignored) {}
            String password = binding.etTransactionPassword.getText().toString();

            // Kiểm tra dữ liệu đầu vào
            if (!validateInput(amount, password)) return;

            // Kiểm tra số dư
            double currentBalance = accounts.getOrDefault(selectedAccount, 0.0);
            if (amount > currentBalance) {
                Toast.makeText(requireContext(), "Số dư không đủ để rút " + formatAmount(amount) + " VND", Toast.LENGTH_SHORT).show();
                return;
            }

            // Cập nhật số dư
            updateBalance(-amount, false);
            Toast.makeText(requireContext(), "Đã rút " + formatAmount(amount) + " VND", Toast.LENGTH_SHORT).show();

            // Xóa dữ liệu nhập
            binding.etWithdrawAmount.getText().clear();
            binding.etTransactionPassword.getText().clear();
        });

        binding.etDepositAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    binding.layoutWithdraw.setVisibility(View.GONE);
                } else {
                    binding.layoutWithdraw.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.etWithdrawAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    binding.layoutDeposit.setVisibility(View.GONE);
                } else {
                    binding.layoutDeposit.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private boolean validateInput(Double amount, String password) {
        if (selectedAccount == null) {
            Toast.makeText(requireContext(), "Vui lòng chọn tài khoản nguồn", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (amount == null || amount <= 0) {
            Toast.makeText(requireContext(), "Số tiền không hợp lệ, vui lòng nhập số lớn hơn 0", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (password.isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng nhập mật khẩu giao dịch", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Giả lập xác thực mật khẩu (thay bằng logic thực tế, ví dụ kiểm tra với server)
        if (!password.equals("123456")) {
            Toast.makeText(requireContext(), "Mật khẩu giao dịch không đúng", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void updateBalance(double amount, boolean isDeposit) {
        double currentBalance = accounts.getOrDefault(selectedAccount, 0.0);
        double newBalance = currentBalance + amount;
        accounts.put(selectedAccount, newBalance);
        String accountId = currentUser.getPhoneNumber() + "_" + selectedAccount.toLowerCase();
        if (selectedAccount.equals("Main")) {
            accountId = currentUser.getPhoneNumber();
        }

        // Cập nhật số dư hiển thị
        NumberFormat numberFormat = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        binding.tvCurrentBalance.setText("Số dư hiện tại: " + numberFormat.format(newBalance) + " VND");

        if (isDeposit) {
            String details = "Nạp " + formatAmount(amount) + " VND" + " vào tài khoản " + selectedAccount;
            transactionService.createTransaction(new Transaction(accountId, accountId, amount, details, details));
            bankAccountService.updateBankAccount(accountId, amount);
        } else {
            String details = "Rút " + formatAmount(-amount) + " VND" + " từ tài khoản " + selectedAccount;
            transactionService.createTransaction(new Transaction(accountId, accountId, amount, details, details));
            bankAccountService.updateBankAccount(accountId, amount);
        }
    }

    private String formatAmount(double amount) {
        NumberFormat numberFormat = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        return numberFormat.format(amount);
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