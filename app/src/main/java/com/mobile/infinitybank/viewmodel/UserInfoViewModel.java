package com.mobile.infinitybank.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.mobile.infinitybank.model.User;
import com.mobile.infinitybank.model.UserInfo;
import com.mobile.infinitybank.service.UserService;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class UserInfoViewModel extends ViewModel {
    private final MutableLiveData<List<UserInfo>> userInfo = new MutableLiveData<>();
    private final UserService userService;

    @Inject
    public UserInfoViewModel(UserService userService) {
        this.userService = userService;
    }

    public LiveData<List<UserInfo>> getUserInfo() {
        return userInfo;
    }

    public void loadDataFromDatabase() {
            userService.getCurrentUser().addOnSuccessListener(documentSnapshot -> {
                User user = documentSnapshot.toObject(User.class);
            List<UserInfo> userInfoList = Arrays.asList(
                new UserInfo("Họ và tên", user.getFullName()),
                new UserInfo("Số điện thoại", user.getPhoneNumber()),
                new UserInfo("Email", user.getEmail()),
                new UserInfo("Mã KH (CIF)", user.getId()),
                new UserInfo("Ngày sinh", user.getDateOfBirth() != null ? new SimpleDateFormat("dd/MM/yyyy").format(user.getDateOfBirth()) : ""),
                new UserInfo("Giới tính", user.getGender()),
                new UserInfo("Ngày cấp", user.getCreatedAt() != null ? new SimpleDateFormat("dd/MM/yyyy").format(user.getCreatedAt()) : "")
            );
            userInfo.setValue(userInfoList);
        });
    }

    public void updateUserInfo(List<UserInfo> updatedUserInfo) {
        userInfo.setValue(updatedUserInfo);
        userService.getCurrentUser().addOnSuccessListener(documentSnapshot -> {
            User user = documentSnapshot.toObject(User.class);
            user.setFullName(updatedUserInfo.get(0).getValue());
            String dob = updatedUserInfo.get(1).getValue();
            String day = dob.substring(0, 2);
            String month = dob.substring(3, 5);
            String year = dob.substring(6, 10);
            user.setDateOfBirth(new Date(Integer.parseInt(year) - 1900, Integer.parseInt(month) - 1, Integer.parseInt(day)));
            user.setGender(updatedUserInfo.get(2).getValue());
            user.setEmail(updatedUserInfo.get(3).getValue());
            userService.update(user);
        });
    }
} 