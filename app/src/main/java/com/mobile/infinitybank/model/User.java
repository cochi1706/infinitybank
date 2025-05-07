package com.mobile.infinitybank.model;

import com.google.firebase.firestore.PropertyName;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class User {
    @PropertyName("id")
    private String id;
    
    @PropertyName("phoneNumber")
    private String phoneNumber;
    
    @PropertyName("email")
    private String email;
    
    @PropertyName("passwordHash")
    private String passwordHash;
    
    @PropertyName("userType")
    private String userType = "user";
    
    @PropertyName("fullName")
    private String fullName;
    
    @PropertyName("createdAt")
    private Date createdAt;
    
    @PropertyName("latitude")
    private String latitude = "";
    
    @PropertyName("dateOfBirth")
    private Date dateOfBirth;
    
    @PropertyName("biometricData")
    private String biometricData = "";
    
    @PropertyName("gender")
    private String gender;
    
    @PropertyName("isLocked")
    private boolean isLocked = false;

    public User(String phoneNumber, String email, String passwordHash,
                String fullName, Date dateOfBirth, String gender, String userType) {
        this.id = phoneNumber;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.userType = userType;
        this.createdAt = new Date();
    }

    public User(String phoneNumber, String email, String passwordHash,
                String fullName, Date dateOfBirth, String gender) {
        this.id = phoneNumber;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.userType = "customer";
        this.createdAt = new Date();
    }
}
