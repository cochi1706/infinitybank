package com.mobile.infinitybank.controller;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.mobile.infinitybank.databinding.ActivityRegisterSavingBinding;
import com.mobile.infinitybank.model.User;
import com.mobile.infinitybank.service.BankAccountService;
import com.mobile.infinitybank.service.UserService;

import java.util.Random;
import java.util.concurrent.Executor;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class RegisterSavingActivity extends AppCompatActivity {
    @Inject
    UserService userService;
    @Inject
    BankAccountService bankAccountService;

    private ActivityRegisterSavingBinding binding;

    private Executor executor;
//    private BiometricPrompt biometricPrompt;
//    private BiometricPrompt.PromptInfo promptInfo;

    String phone;
    String otp;
    User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterSavingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupClickListeners();
        loadUserInfo();
//        setupBiometric();
    }

    private void setupClickListeners() {
        binding.ivBack.setOnClickListener(v -> finish());

        binding.ivHelp.setOnClickListener(v -> {
            // TODO: Show help dialog
            Toast.makeText(this, "Trợ giúp đang được phát triển", Toast.LENGTH_SHORT).show();
        });

        binding.btnContinue.setOnClickListener(v -> {
            if (!binding.cbAgreeTerms.isChecked()) {
                showMessage("Vui lòng đồng ý với điều khoản và điều kiện");
                return;
            }
            binding.layoutConfirmation.setVisibility(View.VISIBLE);
            sendOTP();
        });

//        btnVerifyFingerprint.setOnClickListener(v -> {
//            biometricPrompt.authenticate(promptInfo);
//        });

        binding.btnConfirm.setOnClickListener(v -> {
            performRegister();
        });
    }

    private void loadUserInfo() {
        showLoading(true);
        userService.getCurrentUser().addOnSuccessListener(documentSnapshot -> {
            currentUser = documentSnapshot.toObject(User.class);
            phone = currentUser.getPhoneNumber();
            binding.tvFullName.setText(currentUser.getFullName());
            binding.tvCustomerId.setText(currentUser.getId());
            binding.tvPhone.setText(currentUser.getPhoneNumber());
            binding.tvEmail.setText(currentUser.getEmail());
            showLoading(false);
        });
    }

//    private void setupBiometric() {
//        executor = ContextCompat.getMainExecutor(this);
//        biometricPrompt = new BiometricPrompt(this, executor,
//                new BiometricPrompt.AuthenticationCallback() {
//                    @Override
//                    public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
//                        super.onAuthenticationSucceeded(result);
//                        Toast.makeText(RegisterSavingActivity.this,
//                                "Xác thực vân tay thành công", Toast.LENGTH_SHORT).show();
//                        // TODO: Handle successful authentication
//                    }
//
//                    @Override
//                    public void onAuthenticationError(int errorCode, CharSequence errString) {
//                        super.onAuthenticationError(errorCode, errString);
//                        Toast.makeText(RegisterSavingActivity.this,
//                                "Lỗi xác thực: " + errString, Toast.LENGTH_SHORT).show();
//                    }
//                });
//
//        promptInfo = new BiometricPrompt.PromptInfo.Builder()
//                .setTitle("Xác thực vân tay")
//                .setSubtitle("Đặt ngón tay lên cảm biến để xác thực")
//                .setNegativeButtonText("Hủy")
//                .build();
//    }

    private void sendOTP() {
        // Tạo OTP ngẫu nhiên 6 số
        Random random = new Random();
        otp = String.format("%06d", random.nextInt(1000000));

        // Gửi OTP qua email
        String email = currentUser.getEmail();
        if (email != null && !email.isEmpty()) {
            String subject = "Mã OTP xác nhận giao dịch";
            String message = "Mã OTP của bạn là: " + otp + "\nMã này có hiệu lực trong 5 phút.";

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("message/rfc822");
            intent.putExtra(Intent.EXTRA_EMAIL, new String[] { email });
            intent.putExtra(Intent.EXTRA_SUBJECT, subject);
            intent.putExtra(Intent.EXTRA_TEXT, message);
            startActivity(Intent.createChooser(intent, "Chọn email client"));
        } else {
            showMessage("Không tìm thấy email để gửi OTP");
        }
    }
    private void performRegister () {
        String otpInput = binding.etOTP.getText().toString();
        String password = binding.etTransactionPassword.getText().toString();
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String pin = sharedPreferences.getString("pin", "123456");

        if (password.isEmpty() && otp.isEmpty()) {
            showMessage("Vui lòng nhập mật khẩu PIN hoặc OTP");
            return;
        }

        if (!otpInput.equals(otp)) {
            showMessage("Mã OTP không đúng");
        }

        if (otpInput.equals(otp)) {

        } else if (!password.equals(pin)) {
            showMessage("Mật khẩu PIN không đúng");
            return;
        }

        showLoading(true);
        bankAccountService.registerSavingAccount(phone).addOnSuccessListener(result -> {
            showLoading(false);
            Toast.makeText(this, "Đăng ký tài khoản tiết kiệm thành công", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            showLoading(false);
            Toast.makeText(this, "Lỗi đăng ký tài khoản tiết kiệm", Toast.LENGTH_SHORT).show();
        });
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showLoading(boolean show) {
        binding.loadingOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
} 