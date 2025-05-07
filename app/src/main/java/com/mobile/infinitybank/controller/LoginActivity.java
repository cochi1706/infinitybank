package com.mobile.infinitybank.controller;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.text.method.SingleLineTransformationMethod;
import android.view.View;
import android.widget.Toast;

import com.mobile.infinitybank.R;
import com.mobile.infinitybank.databinding.ActivityLoginBinding;
import com.mobile.infinitybank.model.User;
import com.mobile.infinitybank.service.AuthValidationService;
import com.mobile.infinitybank.service.UserService;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class LoginActivity extends AppCompatActivity {
    @Inject
    UserService userService;
    @Inject
    AuthValidationService authValidationService;

    private ActivityLoginBinding binding;
    private boolean isPasswordVisible = false;
    private Boolean isEmployee = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupListeners();
    }

    private void setupListeners() {
        // Toggle password visibility
        binding.ivTogglePassword.setOnClickListener(v -> {
            isPasswordVisible = !isPasswordVisible;
            if (isPasswordVisible) {
                binding.etPassword.setTransformationMethod(SingleLineTransformationMethod.getInstance());
                binding.ivTogglePassword.setImageResource(R.drawable.ic_eye_on);
            } else {
                binding.etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                binding.ivTogglePassword.setImageResource(R.drawable.ic_eye_off);
            }
            binding.etPassword.setSelection(binding.etPassword.getText().length());
        });

        // Forgot password
        binding.tvForgotPassword.setOnClickListener(v -> {
            Toast.makeText(this, "Vui lòng liên hệ hỗ trợ để lấy lại mật khẩu!", Toast.LENGTH_SHORT).show();
        });

        // Login button
        binding.btnLogin.setOnClickListener(v -> {
            String phone = binding.etPhone.getText().toString().trim();
            String password = binding.etPassword.getText().toString().trim();

            if (password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập mật khẩu!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!authValidationService.isPhoneValid(phone)) {
                showMessages("Số điện thoại không hợp lệ");
                return;
            }

            showLoading(true);
            login(phone, password);
        });
    }

    private void showLoading(boolean show) {
        binding.loadingOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.btnLogin.setEnabled(!show);
    }

    public void showMessages(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void login(String phone, String password) {
        userService.signIn(phone, password).addOnSuccessListener(authResult -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (userService.isUserSignedIn()) {
                userService.getCurrentUser().addOnSuccessListener(documentSnapshot -> {
                    User user = documentSnapshot.toObject(User.class);
                    isEmployee = ! user.getUserType().equals("customer");
                    SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("isLoggedIn", true);
                    editor.putBoolean("isEmployee", isEmployee);
                    editor.apply();
                });

                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                showLoading(false);
                showMessages("Thông tin đăng nhập không chính xác");
            }
        }).addOnFailureListener(e -> {
            showLoading(false);
            showMessages(userService.getFirebaseErrorMessage(e).toString());
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        if (userService.isUserSignedIn()) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
