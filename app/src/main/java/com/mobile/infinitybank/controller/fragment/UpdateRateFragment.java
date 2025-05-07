package com.mobile.infinitybank.controller.fragment;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.mobile.infinitybank.databinding.FragmentUpdateRateBinding;
import com.mobile.infinitybank.model.BankAccount;
import com.mobile.infinitybank.model.User;
import com.mobile.infinitybank.service.BankAccountService;
import com.mobile.infinitybank.service.UserService;
import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class UpdateRateFragment extends Fragment {

    private FragmentUpdateRateBinding binding;
    private static final String TAG = "UpdateRateFragment";

    @Inject
    BankAccountService bankAccountService;

    @Inject
    UserService userService;

    private String pin;
    private BankAccount bankAccount;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        try {
            binding = FragmentUpdateRateBinding.inflate(inflater, container, false);
            return binding.getRoot();
        } catch (Exception e) {
            Log.e(TAG, "Error inflating layout", e);
            throw e;
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try {
            setupListeners();
        } catch (Exception e) {
            Log.e(TAG, "Error in onViewCreated", e);
        }

        SharedPreferences sharedPreferences = this.getActivity().getSharedPreferences("MyPrefs", MODE_PRIVATE);
        pin = sharedPreferences.getString("pin", "123456");
    }

    private void setupListeners() {
        binding.btnCheckRecipient.setOnClickListener(v -> searchCustomer());

        binding.btnUpdateRate.setOnClickListener(v -> {
            if (binding.tvRecipientName.getText().toString().isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng nhập thông tin khách hàng", Toast.LENGTH_SHORT).show();
                return;
            }
            Double newRate = null;
            try {
                newRate = Double.parseDouble(binding.etInterestRate.getText().toString());
            } catch (NumberFormatException ignored) {}
            String employeePassword = binding.etEmployeePassword.getText().toString();


            if (newRate == null || newRate <= 0) {
                Toast.makeText(requireContext(), "Lãi suất không hợp lệ, vui lòng nhập số lớn hơn 0", Toast.LENGTH_SHORT).show();
                return;
            }

            if (employeePassword.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng nhập mật khẩu nhân viên", Toast.LENGTH_SHORT).show();
                return;
            }

            // Giả lập xác thực mật khẩu (thay bằng logic thực tế, ví dụ kiểm tra với server)
            if (!employeePassword.equals(pin)) {
                Toast.makeText(requireContext(), "Mật khẩu nhân viên không đúng", Toast.LENGTH_SHORT).show();
                return;
            }

            bankAccount.setInterestRate(newRate);
            bankAccountService.updateBankAccount(bankAccount);

            Toast.makeText(
                    requireContext(),
                    "Cập nhật lãi suất cho " + binding.tvRecipientName.getText() + " thành công: " + newRate + "%",
                    Toast.LENGTH_SHORT
            ).show();

            // Cập nhật giao diện
            binding.tvCurrentInterestRate.setText(newRate + "%");
            binding.etInterestRate.getText().clear();
            binding.etEmployeePassword.getText().clear();
            binding.etAccount.setText("");
            binding.tvRecipientName.setText("");
        });
    }

    private void searchCustomer() {
        String userId = binding.etAccount.getText().toString();
        if (userId.isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng nhập tài khoản", Toast.LENGTH_SHORT).show();
            return;
        }
        showLoading(true);
        userService.getUserById(userId).addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                User user = documentSnapshot.toObject(User.class);
                binding.tvRecipientName.setText(user.getFullName());
            }
            else {
                showLoading(false);
                Toast.makeText(requireContext(), "Không tìm thấy người dùng", Toast.LENGTH_SHORT).show();
                binding.tvRecipientName.setText("");
                return;
            }
        });
        bankAccountService.getBankAccount(userId + "_saving").addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                bankAccount = documentSnapshot.toObject(BankAccount.class);
                binding.tvCurrentInterestRate.setText(bankAccount.getInterestRate() + "%");
                showLoading(false);
            }
            else {
                showLoading(false);
                binding.tvCurrentInterestRate.setText("");
                Toast.makeText(requireContext(), "Người dùng chưa có tài khoản tiết kiệm", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading(boolean isLoading) {
        binding.loadingOverlay.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 