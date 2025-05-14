package com.mobile.infinitybank.controller;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.biometric.BiometricPrompt;
import androidx.biometric.BiometricManager;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;

import android.text.InputFilter;
import android.text.InputType;
import android.widget.EditText;
import android.widget.Toast;

import com.mobile.infinitybank.R;

public class SplashActivity extends AppCompatActivity {
    private static final long SPLASH_DELAY = 2000; // 2 seconds delay
    private static final String PREFS_NAME = "MyPrefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Check login state using SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);

        // Create a delay effect of 2 seconds before navigation
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent;
            if (isLoggedIn) {
                // Check if biometric authentication is available
                BiometricManager biometricManager = BiometricManager.from(this);
                switch (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
                    case BiometricManager.BIOMETRIC_SUCCESS:
                        // Device can authenticate using biometrics
                        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                                .setTitle("Xác thực")
                                .setSubtitle("Vui lòng xác thực để tiếp tục")
                                .setNegativeButtonText("Hủy")
                                .build();

                        BiometricPrompt biometricPrompt = new BiometricPrompt(this,
                                ContextCompat.getMainExecutor(this),
                                new BiometricPrompt.AuthenticationCallback() {
                                    @Override
                                    public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                                        super.onAuthenticationSucceeded(result);
                                        // If authentication successful, navigate to MainActivity
                                        Intent mainIntent = new Intent(SplashActivity.this, MainActivity.class);
                                        startActivity(mainIntent);
                                        finish();
                                    }

                                    @Override
                                    public void onAuthenticationError(int errorCode, CharSequence errString) {
                                        super.onAuthenticationError(errorCode, errString);
                                        // Handle authentication error
                                        Toast.makeText(SplashActivity.this, "Lỗi xác thực: " + errString, Toast.LENGTH_SHORT).show();
                                        // Navigate to login screen on error
                                        showPinDialog();
                                    }
                                });

                        biometricPrompt.authenticate(promptInfo);
                        return;
                    case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                        Toast.makeText(this, "Thiết bị không hỗ trợ xác thực sinh trắc học", Toast.LENGTH_SHORT).show();
                        break;
                    case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                        Toast.makeText(this, "Xác thực sinh trắc học hiện không khả dụng", Toast.LENGTH_SHORT).show();
                        break;
                    case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                        Toast.makeText(this, "Chưa đăng ký xác thực sinh trắc học", Toast.LENGTH_SHORT).show();
                        break;
                }
                showPinDialog();
            } else {
                // If not logged in, navigate to LoginActivity
                intent = new Intent(SplashActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        }, SPLASH_DELAY);
    }

    public void showPinDialog() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        // Get PIN from SharedPreferences
        String savedPin = sharedPreferences.getString("pin", null);
                
        if (savedPin != null) {
            // Show PIN dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Nhập mã PIN");
            builder.setMessage("Vui lòng nhập mã PIN bạn");

            final EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
            input.setFilters(new InputFilter[] { new InputFilter.LengthFilter(6) });
            builder.setView(input);

            builder.setPositiveButton("Xác nhận", (dialog, which) -> {
                String enteredPin = input.getText().toString();
                if (enteredPin.equals(savedPin)) {
                    dialog.dismiss();
                    Intent mainIntent = new Intent(SplashActivity.this, MainActivity.class);
                    startActivity(mainIntent);
                    finish();
                } else {
                    Toast.makeText(this, "Mã PIN không đúng. Đăng nhập lại", Toast.LENGTH_SHORT).show();
                    // Navigate to login screen on wrong PIN
                    Intent loginIntent = new Intent(SplashActivity.this, LoginActivity.class);
                    startActivity(loginIntent);
                    finish();
                }
            });

            builder.setNegativeButton("Hủy", (dialog, which) -> {
                dialog.cancel();
                finish();
            });

            builder.setCancelable(false);
            builder.show();
        }
    }
} 