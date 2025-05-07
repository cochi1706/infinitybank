package com.mobile.infinitybank.service;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.mobile.infinitybank.model.User;
import com.mobile.infinitybank.repository.UserRepository;

import java.util.Date;
import java.util.Objects;

import javax.inject.Inject;

public class UserService {
    FirebaseAuth firebaseAuth;
    UserRepository userRepository;
    AppStatusService appStatusService;
    BankAccountService bankAccountService;
    Context context;
    public String currentRole = "";
    public String currentId = "";

    @Inject
    public UserService(FirebaseAuth firebaseAuth, UserRepository userRepository, AppStatusService appStatusService,
            BankAccountService bankAccountService, Context context) {
        this.firebaseAuth = firebaseAuth;
        this.userRepository = userRepository;
        this.appStatusService = appStatusService;
        this.bankAccountService = bankAccountService;
        this.context = context;
        getRole();
    }

    public Task<AuthResult> register(String id, String phoneNumber, String password, String fullName,
            Date dateOfBirth, String email, String gender, String role) {
        String passwordHash = PasswordHashing.hashPassword(password);
        User user = new User(phoneNumber, email, passwordHash, fullName,
                dateOfBirth, gender, role);
        userRepository.createUser(user).addOnSuccessListener(task2 -> {
            bankAccountService.register(phoneNumber).addOnFailureListener(task3 -> {
                Log.e("UserService", "Error creating user", task3);
            });
        }).addOnFailureListener(task2 -> {
            Log.e("UserService", "Error creating user", task2);
        });

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        return firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(task -> {
                firebaseAuth.signOut();
                firebaseAuth.updateCurrentUser(currentUser);
            })
            .addOnFailureListener(task -> {
                userRepository.remove(phoneNumber);
                bankAccountService.removeBankAccount(phoneNumber);
            });
    }

    public Task<QuerySnapshot> signIn(String phoneNumber, String password) {
        return userRepository.findByField("phoneNumber", phoneNumber).addOnSuccessListener(querySnapshot -> {
            if (!querySnapshot.isEmpty()) {
                User tmp = querySnapshot.getDocuments().get(0).toObject(User.class);
                if (PasswordHashing.checkPassword(password, tmp.getPasswordHash())) {
                    firebaseAuth.signInWithEmailAndPassword(tmp.getEmail(), password);
                }
            }
        });
    }

    public void getRole() {
        if (isUserSignedIn()) {
            userRepository.findByField("email", getUserEmail()).addOnSuccessListener(querySnapshot -> {
                if (!querySnapshot.isEmpty()) {
                    User tmp = querySnapshot.getDocuments().get(0).toObject(User.class);
                    currentRole = tmp.getUserType();
                    currentId = tmp.getId();
                }
            });
        }
    }

    public Task<DocumentSnapshot> getCurrentUser() {
        return userRepository.findFirstByField("email", getUserEmail());
    }

    public Task<DocumentSnapshot> getUserDataRaw() {
        return userRepository.findFirstByField("email", getUserEmail());
    }

    public String getUserEmail() {
        return firebaseAuth.getCurrentUser().getEmail();
    }

    public Task<QuerySnapshot> findAllUser() {
        return userRepository.findAll();
    }

    public Task<Void> setUser(User user) {
        return userRepository.update(user.getEmail(), user);
    }

    public Task<Void> createUser(User user) {
        return userRepository.createUser(user);
    }

    public Task<Void> deleteUser(String email) {
        return userRepository.remove(email);
    }

    public boolean isUserSignedIn() {
        return firebaseAuth.getCurrentUser() != null;
    }

    public boolean logout() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            firebaseAuth.signOut();
            return true;
        }
        return false;
    }

    public String getFirebaseErrorMessage(Exception exception) {
        try {
            if (!appStatusService.isOnline()) {
                return "Không có kết nối internet.";
            }
            switch (Objects.requireNonNull(exception.getMessage())) {
                case "An internal error has occurred. [ INVALID_LOGIN_CREDENTIALS ]":
                    return "Sai tên email hoặc mật khẩu.";
                case "The email address is already in use by another account.":
                    return "Email đã được sử dụng để đăng ký.";
                default:
                    return "Có lỗi xảy ra. Vui lòng thử lại.";
            }
        } catch (Exception e) {
            return "Có lỗi xảy ra. Vui lòng thử lại.";
        }
    }

    public Task<DocumentSnapshot> getUserById(String id) {
        return userRepository.findFirstByField("id", id);
    }

    public Task<QuerySnapshot> isPhoneNumberExists(String phoneNumber) {
        return userRepository.findByField("phoneNumber", phoneNumber);
    }

    public Task<QuerySnapshot> isEmailExists(String email) {
        return userRepository.findByField("email", email);
    }

    public Task<Void> update(User user) {
        return userRepository.update(user.getId(), user);
    }
}
