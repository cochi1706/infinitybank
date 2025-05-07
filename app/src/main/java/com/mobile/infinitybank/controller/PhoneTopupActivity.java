package com.mobile.infinitybank.controller;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.mobile.infinitybank.databinding.ActivityPhoneTopupBinding;
import com.mobile.infinitybank.model.BankAccount;
import com.mobile.infinitybank.model.Transaction;
import com.mobile.infinitybank.model.User;
import com.mobile.infinitybank.service.BankAccountService;
import com.mobile.infinitybank.service.TransactionService;
import com.mobile.infinitybank.service.UserService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class PhoneTopupActivity extends AppCompatActivity {

    private ActivityPhoneTopupBinding binding;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    private final Map<String, Double> accounts = new HashMap<>();
    private String selectedSourceAccount;
    private String selectedProvider;
    private double selectedAmount = 0.0;
    private double currentBalance = 0.0;
    @Inject
    UserService userService;
    @Inject
    BankAccountService bankAccountService;
    @Inject
    TransactionService transactionService;
    private User currentUser;
    private String otp;

    // Mock data
    private final List<String> providers = Arrays.asList("Viettel", "Vinaphone", "Mobifone", "Vietnamobile");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPhoneTopupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        showLoading(true);
        userService.getCurrentUser().addOnSuccessListener(documentSnapshot -> {
            currentUser = documentSnapshot.toObject(User.class);
            String phone = currentUser.getPhoneNumber();
            bankAccountService.getBankAccounts(phone).addOnSuccessListener(querySnapshot -> {
                for (DocumentSnapshot documentSnapshot2 : querySnapshot.getDocuments()) {
                    BankAccount account = documentSnapshot2.toObject(BankAccount.class);
                    if (account.getType().equals("Loan")) {
                        continue;
                    }
                    accounts.put(account.getType(), account.getBalance());
                }
                setupViews();
                setupListeners();
                showLoading(false);
            });
        });
    }

    private void setupViews() {
        // Setup source account spinner
        List<String> accountTypes = new ArrayList<>(accounts.keySet());
        ArrayAdapter<String> accountAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, accountTypes);
        accountAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerSourceAccount.setAdapter(accountAdapter);

        // Setup provider spinner
        ArrayAdapter<String> providerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, providers);
        providerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerNetwork.setAdapter(providerAdapter);

        // Setup initial values
        if (!accountTypes.isEmpty()) {
            selectedSourceAccount = accountTypes.get(0);
            updateBalanceDisplay();
        }
        if (!providers.isEmpty()) {
            selectedProvider = providers.get(0);
        }
    }

    private void setupListeners() {
        // Source account selection
        binding.spinnerSourceAccount.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedSourceAccount = (String) parent.getItemAtPosition(position);
                updateBalanceDisplay();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedSourceAccount = null;
                binding.tvCurrentBalance.setText("Số dư hiện tại: 0 VND");
            }

        });

        // Provider selection
        binding.spinnerNetwork.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedProvider = (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedProvider = null;
            }
        });

        binding.btn10K.setOnClickListener(v -> { 
            binding.etTopupAmount.setText("10000");
        });
        binding.btn50K.setOnClickListener(v -> { 
            binding.etTopupAmount.setText("50000");
        });
        binding.btn100K.setOnClickListener(v -> { 
            binding.etTopupAmount.setText("100000");
        });

        // Topup button
        binding.btnContinue.setOnClickListener(v -> {
            if (checkBeforeContinue()) {
                binding.layoutConfirmation.setVisibility(View.VISIBLE);
                sendOTP();
            }
        });

        binding.btnConfirm.setOnClickListener(v -> {
            performTopup();
        });

        binding.ivBack.setOnClickListener(v -> finish());

        binding.btnDownloadReceipt.setOnClickListener(v -> {
            // Get the receipt view
            View receiptView = binding.layoutResult;

            // Create a bitmap of the receipt view
            receiptView.setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(receiptView.getDrawingCache());
            receiptView.setDrawingCacheEnabled(false);

            try {
                // Get the Pictures directory
                String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
                File imageFile = new File(path + "/infinitybank_" + System.currentTimeMillis() + ".png");

                // Save the bitmap to file
                FileOutputStream fos = new FileOutputStream(imageFile);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.close();

                // Notify media scanner to make image visible in gallery
                MediaScannerConnection.scanFile(this,
                        new String[]{imageFile.toString()},
                        null,
                        null);

                showMessage("Biên lai đã được lưu vào thư viện ảnh");
            } catch (IOException e) {
                showMessage("Không thể lưu biên lai");
            }
        });

        binding.btnBackToHome.setOnClickListener(v -> {
            finish();
        });
    }

    private void updateBalanceDisplay() {
        if (selectedSourceAccount != null) {
            double balance = accounts.getOrDefault(selectedSourceAccount, 0.0);
            currentBalance = balance;
            binding.tvCurrentBalance.setText("Số dư hiện tại: " + currencyFormat.format(balance));
        }
    }

    private boolean checkBeforeContinue() {
        if (selectedSourceAccount == null) {
            showMessage("Vui lòng chọn tài khoản nguồn");
            return false;
        }

        double currentBalance = accounts.getOrDefault(selectedSourceAccount, 0.0);
        if (selectedAmount > currentBalance) {
            showMessage("Số dư không đủ để thực hiện giao dịch");
            return false;
        }

        if (binding.etPhoneNumber.getText().toString().isEmpty() || binding.etPhoneNumber.getText().toString().length() < 10) {
            showMessage("Số điện thoại không hợp lệ");
            return false;
        }            
        return true;
    }

    private void performTopup() {
        String otpInput = binding.etOTP.getText().toString();
        String password = binding.etTransactionPassword.getText().toString();
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String pin = sharedPreferences.getString("pin", "123456");
        selectedAmount = Double.parseDouble(binding.etTopupAmount.getText().toString());

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

        String accountId = currentUser.getPhoneNumber() + "_" + selectedSourceAccount.toLowerCase();
        if (selectedSourceAccount.equals("Main")) {
            accountId = currentUser.getPhoneNumber();
        }
        bankAccountService.withdraw(accountId, selectedAmount);

        Transaction transaction = new Transaction(accountId, selectedProvider, -selectedAmount, "Nạp tiền điện thoại",
                "Nạp tiền điện thoại cho số điện thoại " + binding.etPhoneNumber.getText().toString());
        transactionService.createTransaction(transaction);
        String transactionId = transaction.getId();

        // Perform topup (mock)
        double newBalance = currentBalance - selectedAmount;
        accounts.put(selectedSourceAccount, newBalance);
        updateBalanceDisplay();

        showMessage("Nạp tiền thành công!");
        clearInputs();

        binding.btnContinue.setEnabled(false);
        binding.layoutConfirmation.setVisibility(View.GONE);
        binding.layoutResult.setVisibility(View.VISIBLE);
        binding.tvReceipt.setText(transaction.getDetails() + "\nMã giao dịch: " + transactionId);
    }

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

    private void clearInputs() {
        binding.etPhoneNumber.setText("");
        binding.etTopupAmount.setText("");
        binding.etTransactionPassword.setText("");
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showLoading(boolean show) {
        binding.loadingOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
    }
} 