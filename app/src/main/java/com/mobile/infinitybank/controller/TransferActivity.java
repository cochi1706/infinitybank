package com.mobile.infinitybank.controller;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaScannerConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.mobile.infinitybank.databinding.ActivityTransferBinding;
import com.mobile.infinitybank.model.BankAccount;
import com.mobile.infinitybank.model.Transaction;
import com.mobile.infinitybank.model.User;
import com.mobile.infinitybank.service.BankAccountService;
import com.mobile.infinitybank.service.TransactionService;
import com.mobile.infinitybank.service.UserService;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class TransferActivity extends AppCompatActivity {
    @Inject
    UserService userService;
    @Inject
    BankAccountService bankAccountService;
    @Inject
    TransactionService transactionService;
    private ActivityTransferBinding binding;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    private final Map<String, Double> accounts = new HashMap<>();
    private String selectedSourceAccount;
    private String selectedBank;
    private double transferAmount = 0.0;
    private User currentUser;
    private String otp;
    private boolean isLivenessDetectionPassed = false;
    private Map<String, String> bankMap = new HashMap<>();
    String bankCode;
    String recipientAccount;
    String recipientName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTransferBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        showLoading(true);
        userService.getCurrentUser().addOnSuccessListener(documentSnapshot -> {
            currentUser = documentSnapshot.toObject(User.class);
            String phone = currentUser.getPhoneNumber();
            String name = currentUser.getFullName();
            binding.etDescription.setText(name + " chuyển tiền từ Infinity Bank");
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
        String url = "https://api.banklookup.net/api/bank/list";
        bankMap.put("ACB", "ACB");
        bankMap.put("BIDV", "BIDV");
        bankMap.put("HDBank", "HDB");
        bankMap.put("ViettelMoney", "VTLMONEY");
        bankMap.put("Vietcombank", "VCB");
        bankMap.put("Techcombank", "TCB");
        bankMap.put("VPBank", "VPB");
        bankMap.put("VietinBank", "VTB");
        bankMap.put("MBBank", "MB");
        bankMap.put("Agribank", "VARB");
        bankMap.put("Sacombank", "SCB");
        bankMap.put("TPBank", "TPB");
        bankMap.put("VIB", "VIB");
        bankMap.put("BacABank", "BAB");
        bankMap.put("Eximbank", "EIB");
        bankMap.put("OceanBank", "OJB");
        bankMap.put("SHB", "SHB");
        bankMap.put("BVB", "BVB");
        bankMap.put("ABBank", "ABB");
        bankMap.put("PVcomBank", "PVCB");
    }

    private void setupViews() {
        // Setup source account spinner
        List<String> accountTypes = new ArrayList<>(accounts.keySet());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, accountTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerSourceAccount.setAdapter(adapter);

        // Setup initial balance display
        if (!accountTypes.isEmpty()) {
            selectedSourceAccount = accountTypes.get(0);
            updateBalanceDisplay();
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

        binding.spinnerRecipientBank.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedBank = (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedSourceAccount = null;
            }
        });

        // Amount input handling
        binding.etAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    String amountStr = s.toString().replace(",", "");
                    transferAmount = amountStr.isEmpty() ? 0.0 : Double.parseDouble(amountStr);
                } catch (NumberFormatException e) {
                }
            }
        });

        binding.btnTabInternal.setOnClickListener(a -> {
            binding.btnTabExternal.setBackgroundColor(Color.parseColor("#AE8ED0"));
            binding.btnTabInternal.setBackgroundColor(Color.parseColor("#5E1DA2"));
            binding.layoutBank.setVisibility(View.GONE);
            binding.etRecipientAccount.setText("");
        });
        binding.btnTabExternal.setOnClickListener(a -> {
            binding.btnTabInternal.setBackgroundColor(Color.parseColor("#AE8ED0"));
            binding.btnTabExternal.setBackgroundColor(Color.parseColor("#5E1DA2"));
            binding.layoutBank.setVisibility(View.VISIBLE);
            binding.etRecipientAccount.setText("6360391805(ví dụ BIDV)");
        });

        binding.btnCheckRecipient.setOnClickListener(a -> {
            recipientAccount = binding.etRecipientAccount.getText().toString().trim();
            if (recipientAccount.isEmpty()) {
                showMessage("Vui lòng nhập số tài khoản người nhận");
                return;
            }

            showLoading(true);
            if (binding.layoutBank.getVisibility() == View.VISIBLE) {
                bankCode = getBankCode(selectedBank);
                new BankAPIAsyncTask().execute("");
            }
            else {
                userService.getUserById(recipientAccount).addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        binding.tvRecipientName.setText(user.getFullName());
                    }
                    else {
                        showMessage("Số tài khoản người nhận không tồn tại");
                    }
                    showLoading(false);
                });
            }
        });

        binding.btn100K.setOnClickListener(a -> {
            binding.etAmount.setText("100000");
        });
        binding.btn500K.setOnClickListener(a -> {
            binding.etAmount.setText("500000");
        });
        binding.btn1M.setOnClickListener(a -> {
            binding.etAmount.setText("1000000");
        });

        binding.btnContinue.setOnClickListener(a -> {
            if (checkBeforeContinue()) {
                binding.layoutConfirmation.setVisibility(View.VISIBLE);
                binding.tvConfirmationDetails.setText(binding.etDescription.getText().toString() + " " + currencyFormat.format(transferAmount));
                sendOTP();
            }
        });

        binding.btnConfirm.setOnClickListener(a -> {
            if (transferAmount >= 10000000) {
                binding.btnVerifyFingerprint.setVisibility(View.VISIBLE);
            }
            else {
                binding.btnVerifyFingerprint.setVisibility(View.GONE);
            }
            performTransfer();
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

        binding.ivBack.setOnClickListener(v -> {
            finish();
        });

        binding.btnVerifyFingerprint.setOnClickListener(v -> {
            Intent intent = new Intent(this, LivenessDetectionActivity.class);
            startActivityForResult(intent, 1);
        });

        binding.btnBackToHome.setOnClickListener(v -> {
            finish();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                isLivenessDetectionPassed = true;
            }
            else {
                isLivenessDetectionPassed = false;
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

    private void updateBalanceDisplay() {
        if (selectedSourceAccount != null) {
            double balance = accounts.getOrDefault(selectedSourceAccount, 0.0);
            binding.tvCurrentBalance.setText("Số dư hiện tại: " + currencyFormat.format(balance));
        }
    }

    private boolean checkBeforeContinue() {
        if (selectedSourceAccount == null) {
            showMessage("Vui lòng chọn tài khoản nguồn");
            return false;
        }
        if (transferAmount <= 0) {
            showMessage("Số tiền không hợp lệ");
            return false;
        }

        double currentBalance = accounts.getOrDefault(selectedSourceAccount, 0.0);
        if (transferAmount > currentBalance) {
            showMessage("Số dư không đủ để thực hiện giao dịch");
            return false;
        }
        return true;
    }

    private void performTransfer() {
        recipientAccount = binding.etRecipientAccount.getText().toString().trim();
        recipientName = binding.tvRecipientName.getText().toString().trim();
        String description = binding.etDescription.getText().toString().trim();
        String detail = "Chuyển tiền từ " + selectedSourceAccount
                        + " đến " + recipientAccount + " - " + recipientName
                        + " với số tiền " + currencyFormat.format(transferAmount);
        String password = binding.etTransactionPassword.getText().toString();
        double currentBalance = accounts.getOrDefault(selectedSourceAccount, 0.0);
        String otpInput = binding.etOTP.getText().toString();

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
            showMessage("Mật khẩu giao dịch không đúng");
            return;
        }

        if (transferAmount >= 10000000) {
            isLivenessDetectionPassed = false;
            Intent intent = new Intent(this, LivenessDetectionActivity.class);
            startActivityForResult(intent, 1);
        }
        else {
            isLivenessDetectionPassed = true;
        }

        if (!isLivenessDetectionPassed) {
            showMessage("Xác thực không thành công");
            return;
        }
        
        String accountId = currentUser.getPhoneNumber() + "_" + selectedSourceAccount.toLowerCase();
        if (selectedSourceAccount.equals("Main")) {
            accountId = currentUser.getPhoneNumber();
        }
        if (binding.layoutBank.getVisibility() == View.VISIBLE) {
            bankAccountService.withdraw(accountId, recipientAccount, transferAmount);
            detail += " - " + recipientName;
        }
        else {
            bankAccountService.withdraw(accountId, transferAmount);
            recipientAccount += " - " + recipientName;
        }

        Transaction transaction = new Transaction(accountId, recipientAccount, -transferAmount, description, detail);
        transactionService.createTransaction(transaction);
        String transactionId = transaction.getId();
        detail = "Nhận " + currencyFormat.format(transferAmount) + " VND" + " từ tài khoản " + currentUser.getPhoneNumber() + " - " + currentUser.getFullName();
        Transaction transaction2 = new Transaction(recipientAccount, accountId, transferAmount, description, detail);
        transactionService.createTransaction(transaction2);

        // Perform transfer (mock)
        double newBalance = currentBalance - transferAmount;
        accounts.put(selectedSourceAccount, newBalance);
        updateBalanceDisplay();
        binding.layoutConfirmation.setVisibility(View.GONE);

        showMessage("Chuyển khoản thành công!");
        clearInputs();

        binding.btnContinue.setEnabled(false);
        binding.layoutConfirmation.setVisibility(View.GONE);
        binding.layoutResult.setVisibility(View.VISIBLE);
        binding.tvDetail.setText(description);
        binding.tvReceipt.setText("Mã giao dịch: " + transactionId);
    }

    private void clearInputs() {
        binding.etRecipientAccount.setText("");
        binding.etAmount.setText("");
        binding.etDescription.setText("");
        binding.etTransactionPassword.setText("");
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private String convertToWords(double amount) {
        try
        {
            String rs = "";
            String[] ch = { "không", "một", "hai", "ba", "bốn", "năm", "sáu", "bảy", "tám", "chín" };
            String[] rch = { "lẻ", "mốt", "", "", "", "lăm" };
            String[] u = { "", "mươi", "trăm", "ngàn", "", "", "triệu", "", "", "tỷ", "", "", "ngàn", "", "", "triệu" };
            String nstr = String.valueOf(amount);
            long[] n = new long[nstr.length()];
            int len = n.length;
            for (int i = 0; i < len; i++)
            {
            	n[len - 1 - i] = Long.valueOf(nstr.substring(i, i+1));
            }
            for (int i = len - 1; i >= 0; i--)
            {
                if (i % 3 == 2)// số 0 ở hàng trăm
                {
                    if (n[i] == 0 && n[i - 1] == 0 && n[i - 2] == 0) continue;//nếu cả 3 số là 0 thì bỏ qua không đọc
                }
                else if (i % 3 == 1) // số ở hàng chục
                {
                    if (n[i] == 0)
                    {
                        if (n[i - 1] == 0) { continue; }// nếu hàng chục và hàng đơn vị đều là 0 thì bỏ qua.
                        else
                        {
                            rs += " " + rch[(int)n[i]]; continue;// hàng chục là 0 thì bỏ qua, đọc số hàng đơn vị
                        }
                    }
                    if (n[i] == 1)//nếu số hàng chục là 1 thì đọc là mười
                    {
                        rs += " mười"; continue;
                    }
                }
                else if (i != len - 1)// số ở hàng đơn vị (không phải là số đầu tiên)
                {
                    if (n[i] == 0)// số hàng đơn vị là 0 thì chỉ đọc đơn vị
                    {
                        if (i + 2 <= len - 1 && n[i + 2] == 0 && n[i + 1] == 0) continue;
                        rs += " " + (i % 3 == 0 ? u[i] : u[i % 3]);
                        continue;
                    }
                    if (n[i] == 1)// nếu là 1 thì tùy vào số hàng chục mà đọc: 0,1: một / còn lại: mốt
                    {
                        rs += " " + ((n[i + 1] == 1 || n[i + 1] == 0) ? ch[(int)n[i]] : rch[(int)n[i]]);
                        rs += " " + (i % 3 == 0 ? u[i] : u[i % 3]);
                        continue;
                    }
                    if (n[i] == 5) // cách đọc số 5
                    {
                        if (n[i + 1] != 0) //nếu số hàng chục khác 0 thì đọc số 5 là lăm
                        {
                            rs += " " + rch[(int)n[i]];// đọc số 
                            rs += " " + (i % 3 == 0 ? u[i] : u[i % 3]);// đọc đơn vị
                            continue;
                        }
                    }
                }
                rs += (rs == "" ? " " : ", ") + ch[(int)n[i]];// đọc số
                rs += " " + (i % 3 == 0 ? u[i] : u[i % 3]);// đọc đơn vị
            }
            if (rs.charAt(rs.length() - 1) != ' ')
                rs += " đồng";
            else
                rs += "đồng";

            if (rs.length() > 2)
            {
                String rs1 = rs.substring(0, 2);
                rs1 = rs1.toUpperCase();
                rs = rs.substring(2);
                rs = rs1 + rs;
            }
            return rs.trim().replace("lẻ,", "lẻ").replace("mươi,", "mươi").replace("trăm,", "trăm").replace("mười,", "mười");
        }
        catch(Exception ex)
        {
        	ex.printStackTrace();
            return "";
        }
    }

    private void showLoading(boolean show) {
        binding.loadingOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    public String getBankCode(String bankName) {
        return this.bankMap.get(bankName);
    }

    @SuppressLint("StaticFieldLeak")
    private class BankAPIAsyncTask extends AsyncTask<String, Void, String> {
        String api_key = "e48964c8-8766-42a4-be6b-0e6747230c0ekey";
        String api_secret = "2719cf9c-1a06-4357-9eef-9354724fbd36secret";
        @Override
        protected String doInBackground(String... strings) {
            String curl = String.format(
                "curl --location 'https://api.banklookup.net/api/bank/id-lookup-prod' --header 'x-api-key: %s' --header 'x-api-secret: %s' --header 'Content-Type: application/json' --data '{\"bank\": \"%s\", \"account\": \"%s\"}'",
                api_key, api_secret, bankCode, recipientAccount);

            try {
                URL url = new URL("https://api.banklookup.net/api/bank/id-lookup-prod");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("x-api-key", api_key);
                con.setRequestProperty("x-api-secret", api_secret);
                con.setRequestProperty("Content-Type", "application/json");
                con.setDoOutput(true);

                String jsonInputString = String.format("{\"bank\": \"%s\", \"account\": \"%s\"}", bankCode, recipientAccount);
                try (java.io.OutputStream os = con.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                int responseCode = con.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    try {
                        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"));
                        StringBuilder response = new StringBuilder();
                        String responseLine;
                        while ((responseLine = in.readLine()) != null) {
                            response.append(responseLine.trim());
                        }
                        in.close();

                        JSONObject jsonResponse = new JSONObject(response.toString());
                        recipientName = jsonResponse.getJSONObject("data").getString("ownerName");
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                    return recipientName;
                } else {
                    throw new RuntimeException("Failed : HTTP error code : " + responseCode);
                }
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            showLoading(false);
            if (result != null) {
                binding.tvRecipientName.setText(recipientName);
            }
            else {
                showMessage("Có lỗi khi xử lý dữ liệu từ ngân hàng");
            }
        }

        @Override
        protected void onPreExecute() {
            showLoading(true);
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }
}