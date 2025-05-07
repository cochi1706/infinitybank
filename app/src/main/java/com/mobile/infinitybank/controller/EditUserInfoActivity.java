package com.mobile.infinitybank.controller;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.mobile.infinitybank.databinding.ActivityEditUserInfoBinding;
import com.mobile.infinitybank.model.UserInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EditUserInfoActivity extends AppCompatActivity {
    private ActivityEditUserInfoBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditUserInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Setup Toolbar
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Chỉnh sửa thông tin cá nhân");
        }

        // Get current user info from Intent
        ArrayList<UserInfo> currentUserInfo = getIntent().getParcelableArrayListExtra("currentUserInfo");
        if (currentUserInfo != null) {
            displayCurrentUserInfo(currentUserInfo);
        }

        // Handle save button
        binding.btnSave.setOnClickListener(v -> {
            // Get data from input fields
            String fullName = binding.etFullName.getEditText() != null ? 
                    binding.etFullName.getEditText().getText().toString().trim() : "";
            String dateOfBirth = binding.etDateOfBirth.getEditText() != null ? 
                    binding.etDateOfBirth.getEditText().getText().toString().trim() : "";
            String gender = binding.spinnerGender.getSelectedItem().toString();
            String email = binding.etEmail.getEditText() != null ? 
                    binding.etEmail.getEditText().getText().toString().trim() : "";
            String phoneNumber = binding.etPhoneNumber.getEditText() != null ? 
                    binding.etPhoneNumber.getEditText().getText().toString().trim() : "";

            // Validate data
            if (TextUtils.isEmpty(fullName) || TextUtils.isEmpty(dateOfBirth) || 
                TextUtils.isEmpty(gender) || TextUtils.isEmpty(email) || 
                TextUtils.isEmpty(phoneNumber)) {
                Toast.makeText(this, "Vui lòng điền đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create new UserInfo list from edited data
            List<UserInfo> updatedUserInfo = Arrays.asList(
                new UserInfo("Họ và tên", fullName),
                new UserInfo("Ngày sinh", dateOfBirth),
                new UserInfo("Giới tính", gender),
                new UserInfo("Email", email),
                new UserInfo("Số điện thoại", phoneNumber)
            );

            // Return data to UserInfoFragment
            Intent resultIntent = new Intent();
            resultIntent.putParcelableArrayListExtra("updatedUserInfo", new ArrayList<>(updatedUserInfo));
            setResult(Activity.RESULT_OK, resultIntent);

            Toast.makeText(this, "Lưu thông tin thành công!", Toast.LENGTH_SHORT).show();
            finish();
        });

        // Handle cancel button
        binding.btnCancel.setOnClickListener(v -> finish());
    }

    private void showConfirmationDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Xác nhận thay đổi thông tin")
            .setMessage("Bạn có chắc chắn muốn thay đổi thông tin?")
            .setPositiveButton("Có", (dialog, which) -> {
                finish();
            })
            .setNegativeButton("Không", (dialog, which) -> dialog.dismiss())
            .setCancelable(true)
            .show();
    }

    private void displayCurrentUserInfo(List<UserInfo> userInfoList) {
        for (UserInfo userInfo : userInfoList) {
            switch (userInfo.getLabel()) {
                case "Họ và tên":
                    if (binding.etFullName.getEditText() != null) {
                        binding.etFullName.getEditText().setText(userInfo.getValue());
                    }
                    break;
                case "Ngày sinh":
                    if (binding.etDateOfBirth.getEditText() != null) {
                        binding.etDateOfBirth.getEditText().setText(userInfo.getValue());
                    }
                    break;
                case "Giới tính":
                    if (binding.spinnerGender.getSelectedItem() != null) {
                        binding.spinnerGender.setSelection(binding.spinnerGender.getSelectedItemPosition());
                    }
                    break;
                case "Email":
                    if (binding.etEmail.getEditText() != null) {
                        binding.etEmail.getEditText().setText(userInfo.getValue());
                    }
                    break;
                case "Số điện thoại":
                    if (binding.etPhoneNumber.getEditText() != null) {
                        binding.etPhoneNumber.getEditText().setText(userInfo.getValue());
                    }
                    break;
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null; // Clean up ViewBinding
    }
} 