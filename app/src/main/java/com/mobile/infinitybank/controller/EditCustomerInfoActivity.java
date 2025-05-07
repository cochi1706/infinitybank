package com.mobile.infinitybank.controller;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.mobile.infinitybank.databinding.ActivityEditCustomerInfoBinding;
import com.mobile.infinitybank.model.User;
import com.mobile.infinitybank.service.UserService;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class EditCustomerInfoActivity extends AppCompatActivity {
    @Inject
    UserService userService;
    ActivityEditCustomerInfoBinding binding;

    User user;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditCustomerInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnCheckRecipient.setOnClickListener(v -> searchCustomer());
        binding.btnSave.setOnClickListener(v -> showConfirmationDialog());
        binding.btnCancel.setOnClickListener(v -> finish());
        binding.ivBack.setOnClickListener(v -> finish());
    }

    private void showConfirmationDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Xác nhận thay đổi thông tin")
            .setMessage("Bạn có chắc chắn muốn thay đổi thông tin?")
            .setPositiveButton("Có", (dialog, which) -> {
                updateCustomerInfo();
            })
            .setNegativeButton("Không", (dialog, which) -> dialog.dismiss())
            .setCancelable(true)
            .show();
    }

    private void updateCustomerInfo() {
        user.setFullName(binding.etFullName.getEditText().getText().toString());
        user.setEmail(binding.etEmail.getEditText().getText().toString());
        user.setPhoneNumber(binding.etPhoneNumber.getEditText().getText().toString());
        user.setDateOfBirth(new Date(binding.etDateOfBirth.getEditText().getText().toString()));
        user.setGender(binding.spinnerGender.getSelectedItem().toString());
        userService.update(user);
    }

    private void searchCustomer() {
        String userId = binding.etAccount.getText().toString();
        showLoading(true);
        userService.getUserById(userId).addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                user = documentSnapshot.toObject(User.class);
                binding.etFullName.getEditText().setText(user.getFullName());
                binding.etEmail.getEditText().setText(user.getEmail());
                binding.etPhoneNumber.getEditText().setText(user.getPhoneNumber());
                binding.etDateOfBirth.getEditText().setText(user.getDateOfBirth() != null ? new SimpleDateFormat("dd/MM/yyyy").format(user.getDateOfBirth()) : "");
                String gender = user.getGender();
                if (gender.equals("Nam")) {
                    binding.spinnerGender.setSelection(1);
                } 
                else if (gender.equals("Nữ")) {
                    binding.spinnerGender.setSelection(2);
                }
                else {
                    binding.spinnerGender.setSelection(0);
                }
                showLoading(false);
            }
            else {
                showLoading(false);
                Toast.makeText(this, "Không tìm thấy tài khoản", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading(boolean isLoading) {
        binding.loadingOverlay.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }
}