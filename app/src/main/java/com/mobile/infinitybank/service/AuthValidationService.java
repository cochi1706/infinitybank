package com.mobile.infinitybank.service;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AuthValidationService {
    @Inject
    public AuthValidationService() {
    }

    public boolean isEmailValid(String email) {
        return email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$");
    }

    public boolean isPasswordValid(String password) {
        return password.length() >= 6;
    }

    public boolean isPhoneValid(String phone) {
        return phone.matches("^(?:\\d{9}|\\d{10})$");
    }

    public boolean isRepeatPasswordValid(String password, String repeatPassword) {
        return password.equals(repeatPassword);
    }

    public boolean isIdValid(String id) {
        return id.matches("^(?:\\d{16})$");
    }
}
