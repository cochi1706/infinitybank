package com.mobile.infinitybank.service;

public class PasswordHashing {
    public static String hashPassword(String password) {
        String key = "Ch1C0";
        StringBuilder encryptedPassword = new StringBuilder();
        for (int i = 0; i < password.length(); i++) {
            encryptedPassword.append((char) (password.charAt(i) ^ key.charAt(i % key.length())));
        }
        return encryptedPassword.toString();
    }

    public static boolean checkPassword(String password, String hashedPassword) {
        return hashPassword(password).equals(hashedPassword);
    }
}
