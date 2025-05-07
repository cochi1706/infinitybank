package com.mobile.infinitybank.controller;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.mobile.infinitybank.databinding.ActivityPaymentBinding;
import com.mobile.infinitybank.model.BankAccount;
import com.mobile.infinitybank.model.Bill;
import com.mobile.infinitybank.model.Transaction;
import com.mobile.infinitybank.model.User;
import com.mobile.infinitybank.service.BankAccountService;
import com.mobile.infinitybank.service.BillService;
import com.mobile.infinitybank.service.TransactionService;
import com.mobile.infinitybank.service.UserService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class PaymentActivity extends AppCompatActivity {

    @Inject
    UserService userService;
    @Inject
    BankAccountService bankAccountService;
    @Inject
    BillService billService;
    @Inject
    TransactionService transactionService;

    private ActivityPaymentBinding binding;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    private Map<String, Double> accounts = new HashMap<>();
    private Map<String, Double> bills = new HashMap<>();
    private List<String> billTypes = new ArrayList<>();
    private List<Bill> billList = new ArrayList<>();
    private String selectedSourceAccount;
    private String selectedBillType;
    private Bill selectedBill;
    private User currentUser;
    private int currentMonth;
    private String otp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPaymentBinding.inflate(getLayoutInflater());
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
                currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1;
                int currentYear = Calendar.getInstance().get(Calendar.YEAR);
                billService.getLast3BillByUserId(currentUser.getId()).addOnSuccessListener(querySnapshot3 -> {
                    if (querySnapshot3.isEmpty()) {
                        Bill newBill = new Bill(currentUser.getId(), "Tiền Internet", 120000, currentMonth, currentYear);
                        Bill finalNewBill = newBill;
                        billService.createBill(newBill).addOnSuccessListener(a -> {
                            bills.put(finalNewBill.getServiceType(), finalNewBill.getAmount());
                            billList.add(finalNewBill);
                        });
                        newBill = new Bill(currentUser.getId(), "Tiền điện", 100000, currentMonth, currentYear);
                        Bill finalNewBill1 = newBill;
                        billService.createBill(newBill).addOnSuccessListener(a -> {
                            bills.put(finalNewBill1.getServiceType(), finalNewBill1.getAmount());
                            billList.add(finalNewBill1);
                        });
                    }
                    for (DocumentSnapshot documentSnapshot3 : querySnapshot3.getDocuments()) {
                        Bill bill = documentSnapshot3.toObject(Bill.class);
                        if (bill.getMonth() == currentMonth && bill.getYear() == currentYear) {
                            bills.put(bill.getServiceType(), bill.getAmount());
                            billList.add(bill);
                        }
                        else {
                            Bill newBill = new Bill(currentUser.getId(), bill.getServiceType(), bill.getAmount(), currentMonth, currentYear);
                            billService.createBill(newBill).addOnSuccessListener(a -> {
                                bills.put(newBill.getServiceType(), newBill.getAmount());
                                billList.add(newBill);
                            });
                        }
                    }
                    setupViews();
                    setupListeners();
                    showLoading(false);
                });
            });
        });
        
    }

    private void setupViews() {
        // Setup source account spinner
        List<String> accountTypes = new ArrayList<>(accounts.keySet());
        ArrayAdapter<String> accountAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, accountTypes);
        accountAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerSourceAccount.setAdapter(accountAdapter);

        // Setup bill type spinner
        billTypes = new ArrayList<>(bills.keySet());
        ArrayAdapter<String> billAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, billTypes);
        billAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerBillType.setAdapter(billAdapter);

        // Setup initial values
        if (!accountTypes.isEmpty()) {
            selectedSourceAccount = accountTypes.get(0);
            updateBalanceDisplay();
        }
        if (!billTypes.isEmpty()) {
            selectedBillType = billTypes.get(0);
            updateBillAmount();
        }
    }

    private void setupListeners() {
        // Bill type selection
        binding.spinnerBillType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedBillType = (String) parent.getItemAtPosition(position);
                updateBillAmount();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedBillType = null;
                binding.tvBillAmount.setText("Số tiền: 0 VND");
            }
        });

        binding.spinnerSourceAccount.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedSourceAccount = (String) parent.getItemAtPosition(position);
                updateBalanceDisplay();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedSourceAccount = null;
                binding.tvCurrentBalance.setText("Số dư: 0 VND");
            }
        });

        binding.btnContinue.setOnClickListener(v -> {
            if (checkBeforeContinue()) {
                binding.layoutConfirmation.setVisibility(View.VISIBLE);
                binding.tvConfirmationDetails.setText("Thanh toán " + selectedBillType + " tháng " + currentMonth);
                sendOTP();
            }
        });

        binding.btnConfirm.setOnClickListener(v -> {
            performPayment();
        });

        binding.ivBack.setOnClickListener(v -> {
            finish();
        });

        binding.btnBackToHome.setOnClickListener(v -> {
            finish();
        });

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
    }

    private void updateBalanceDisplay() {
        if (selectedSourceAccount != null) {
            double balance = accounts.getOrDefault(selectedSourceAccount, 0.0);
            binding.tvCurrentBalance.setText("" + currencyFormat.format(balance));
        }
    }

    private boolean checkBeforeContinue() {
        if (selectedSourceAccount == null) {
            showMessage("Vui lòng chọn tài khoản nguồn");
            return false;
        }

        double currentBalance = accounts.getOrDefault(selectedSourceAccount, 0.0);
        double billAmount = bills.getOrDefault(selectedBillType, 0.0);
        if (billAmount > currentBalance) {
            showMessage("Số dư không đủ để thực hiện giao dịch");
            return false;
        }
        return true;
    }

    private void performPayment() {
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

        double billAmount = bills.getOrDefault(selectedBillType, 0.0);
        double currentBalance = accounts.getOrDefault(selectedSourceAccount, 0.0);

        if (billAmount > currentBalance) {
            showMessage("Số dư không đủ để thanh toán");
            return;
        }

        // Perform payment (mock)
        double newBalance = currentBalance - billAmount;
        accounts.put(selectedSourceAccount, newBalance);
        bills.put(selectedBillType, 0.0);
        updateBalanceDisplay();

        String accountId = currentUser.getPhoneNumber() + "_" + selectedSourceAccount.toLowerCase();
        if (selectedSourceAccount.equals("Main")) {
            accountId = currentUser.getPhoneNumber();
        }
        bankAccountService.withdraw(accountId, billAmount);
        selectedBill.setStatus("Đã thanh toán");
        selectedBill.setCompletedAt(new Date());
        billService.updateBill(selectedBill);

        Transaction transaction = new Transaction(accountId, selectedBill.getServiceType(), -billAmount, "Thanh toán hóa đơn",
                "Thanh toán hóa đơn " + selectedBill.getServiceType() + " tháng " + selectedBill.getMonth());
        transactionService.createTransaction(transaction);
        String transactionId = transaction.getId();

        showMessage("Thanh toán thành công!");
        binding.etTransactionPassword.setText("");
        
        binding.btnContinue.setEnabled(false);
        binding.layoutConfirmation.setVisibility(View.GONE);
        binding.layoutResult.setVisibility(View.VISIBLE);
        binding.tvReceipt.setText(transaction.getDetails() + "\nMã giao dịch: " + transactionId);
    }

    private void updateBillAmount() {
        if (selectedBillType != null) {
            double amount = bills.getOrDefault(selectedBillType, 0.0);
            binding.tvBillAmount.setText(currencyFormat.format(amount));
            for (Bill bill : billList) {
                if (bill.getServiceType().equals(selectedBillType)) {
                    selectedBill = bill;
                    break;
                }
            }
            binding.etBillNumber.setText(selectedBill.getId());
            if (selectedBill.getStatus().equals("Đã thanh toán")) {
                binding.tvBillAmount.setText(currencyFormat.format(amount) + " (Đã thanh toán)");
                binding.btnContinue.setEnabled(false);
            }
            else {
                binding.btnContinue.setEnabled(true);
            }
        }
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

    private void clearInput() {
        binding.etOTP.setText("");
        binding.etTransactionPassword.setText("");
        binding.spinnerBillType.setSelection(0);
        binding.etBillNumber.setText("");
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showLoading(boolean show) {
        binding.loadingOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
    }
} 