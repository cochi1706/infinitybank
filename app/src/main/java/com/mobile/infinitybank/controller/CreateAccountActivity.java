package com.mobile.infinitybank.controller;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.mobile.infinitybank.databinding.ActivityCreateAccountBinding;
import com.mobile.infinitybank.model.User;
import com.mobile.infinitybank.service.AuthValidationService;
import com.mobile.infinitybank.service.BankAccountService;
import com.mobile.infinitybank.service.PasswordHashing;
import com.mobile.infinitybank.service.UserService;

import java.util.Calendar;
import java.util.Date;

import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CreateAccountActivity extends AppCompatActivity {
    private ActivityCreateAccountBinding binding;
    @Inject
    AuthValidationService authValidationService;
    @Inject
    UserService userService;
    @Inject
    BankAccountService bankAccountService;

    User employee;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateAccountBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        showLoading(true);
        userService.getCurrentUser().addOnSuccessListener(documentSnapshot -> {
            employee = documentSnapshot.toObject(User.class);
            showLoading(false);
        });

        clearInput();

        // Initialize spinner
        binding.spinnerAccountType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedAccountType = parent.getItemAtPosition(position).toString();
                // Handle the selected account type
                if (selectedAccountType.equals("Tài khoản chính")) {
                    activateUnnecessaryFields();
                } else {
                    unacctivateUnnecessaryFields();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Handle case where nothing is selected
            }
        });

        // Initialize gender spinner
        binding.spinnerGender.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedGender = parent.getItemAtPosition(position).toString();
                // Handle the selected gender
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Handle case where nothing is selected
            }
        });

        binding.btnCreateAccount.setOnClickListener(v -> {
            String employeePassword = binding.etEmployeePassword.getText().toString().trim();
            if (PasswordHashing.checkPassword(employeePassword, employee.getPasswordHash())) {
                register();
            } else {
                Toast.makeText(this, "Mật khẩu nhân viên không chính xác", Toast.LENGTH_SHORT).show();
            }
            register();
        });
    }

    private void showLoading(boolean show) {
        binding.loadingOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.btnCreateAccount.setEnabled(!show);
    }

    private void clearInput() {
        binding.etCustomerPhone.setText("");
        binding.etCustomerName.setText("");
        binding.etCustomerEmail.setText("");
        binding.spinnerGender.setSelection(0);
        binding.spinnerAccountType.setSelection(0);
        binding.etDob.setText("");
    }

    private void createMainAccount(String phone, String fullName, Date dob, String email, String gender) {
        userService.isPhoneNumberExists(phone)
            .addOnSuccessListener(phoneExists -> {
                if (phoneExists.size() > 0) {
                    showLoading(false);
                    Toast.makeText(this, "Số điện thoại đã được sử dụng", Toast.LENGTH_SHORT).show();
                    return;
                }
                userService.register(phone, phone, phone + "123", fullName, dob, email, gender, "Customer")
                    .addOnSuccessListener(authResult -> {
                        showLoading(false);
                        Toast.makeText(this, "Đăng ký thành công", Toast.LENGTH_SHORT).show();
                        clearInput();
                    })
                    .addOnFailureListener(e -> {
                        showLoading(false);
                        String errorMessage = userService.getFirebaseErrorMessage(e);
                        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
                    });
            })
            .addOnFailureListener(e -> {
                showLoading(false);
                Toast.makeText(this, "Lỗi kiểm tra số điện thoại", Toast.LENGTH_SHORT).show();
            });
    }

    private void createSavingAccount(String phone) {
        userService.isPhoneNumberExists(phone)
            .addOnSuccessListener(phoneExists -> {
                if (phoneExists.size() == 0) {
                    showLoading(false);
                    Toast.makeText(this, "Chưa có tài khoản chính", Toast.LENGTH_SHORT).show();
                    return;
                }
                bankAccountService.registerSavingAccount(phone).addOnSuccessListener(result -> {
                    showLoading(false);
                    Toast.makeText(this, "Đăng ký tài khoản tiết kiệm thành công", Toast.LENGTH_SHORT).show();
                    clearInput();
                }).addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(this, "Lỗi đăng ký tài khoản tiết kiệm", Toast.LENGTH_SHORT).show();
                });
            })
            .addOnFailureListener(e -> {
                showLoading(false);
                Toast.makeText(this, "Lỗi kiểm tra số điện thoại", Toast.LENGTH_SHORT).show();
            });
    }

    private void createLoanAccount(String phone) {
        userService.isPhoneNumberExists(phone)
            .addOnSuccessListener(phoneExists -> {
                if (phoneExists.size() == 0) {
                    showLoading(false);
                    Toast.makeText(this, "Chưa có tài khoản chính", Toast.LENGTH_SHORT).show();
                    return;
                }
                bankAccountService.registerLoanAccount(phone).addOnSuccessListener(result -> {
                    showLoading(false);
                    Toast.makeText(this, "Đăng ký tài khoản vay thành công", Toast.LENGTH_SHORT).show();
                    clearInput();
                }).addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(this, "Lỗi đăng ký tài khoản vay", Toast.LENGTH_SHORT).show();
                });
            })
            .addOnFailureListener(e -> {
                showLoading(false);
                Toast.makeText(this, "Lỗi kiểm tra số điện thoại", Toast.LENGTH_SHORT).show();
            });
    }

    private void unacctivateUnnecessaryFields() {
        binding.etCustomerEmail.setBackgroundColor(Color.GRAY);
        binding.spinnerGender.setBackgroundColor(Color.GRAY);
        binding.etDob.setBackgroundColor(Color.GRAY);
        binding.etCustomerName.setBackgroundColor(Color.GRAY);
    }

    private void activateUnnecessaryFields() {
        binding.etCustomerEmail.setBackgroundColor(Color.WHITE);
        binding.spinnerGender.setBackgroundColor(Color.WHITE);
        binding.etDob.setBackgroundColor(Color.WHITE);
        binding.etCustomerName.setBackgroundColor(Color.WHITE);
    }

    private void register() {
        String phone = binding.etCustomerPhone.getText().toString().trim();
        String accountType = binding.spinnerAccountType.getSelectedItem().toString();

        if (!authValidationService.isPhoneValid(phone)) {
            Toast.makeText(this, "Số điện thoại không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);
        
        if (accountType.equals("Tài khoản chính")) {
            String fullName = binding.etCustomerName.getText().toString().trim();
            String day = binding.etDob.getText().toString().trim().substring(0, 2);
            String month = binding.etDob.getText().toString().trim().substring(3, 5);
            String year = binding.etDob.getText().toString().trim().substring(6, 10);
            Date dob = new Date(Integer.parseInt(year) - 1900, Integer.parseInt(month) - 1, Integer.parseInt(day));
            String email = binding.etCustomerEmail.getText().toString().trim();
            String gender = binding.spinnerGender.getSelectedItem().toString();
            if (!authValidationService.isEmailValid(email)) {
                Toast.makeText(this, "Email không hợp lệ", Toast.LENGTH_SHORT).show();
                showLoading(false);
                return;
            }
            createMainAccount(phone, fullName, dob, email, gender);
        } else if (accountType.equals("Tài khoản tiết kiệm")) {
            createSavingAccount(phone);
        } else {
            createLoanAccount(phone);
        }
    }
}
