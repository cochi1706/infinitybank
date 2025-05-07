package com.mobile.infinitybank.controller.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.mobile.infinitybank.controller.EditUserInfoActivity;
import com.mobile.infinitybank.controller.MainActivity;
import com.mobile.infinitybank.model.UserInfo;
import com.mobile.infinitybank.databinding.FragmentUserInfoBinding;
import com.mobile.infinitybank.viewmodel.UserInfoViewModel;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class UserInfoFragment extends Fragment {

    private FragmentUserInfoBinding binding;
    private UserInfoViewModel viewModel;
    
    private final ActivityResultLauncher<Intent> editUserInfoLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                Intent data = result.getData();
                if (data != null) {
                    ArrayList<UserInfo> updatedUserInfo = data.getParcelableArrayListExtra("updatedUserInfo");
                    if (updatedUserInfo != null) {
                        viewModel.updateUserInfo(updatedUserInfo);
                        updateUserInfoUI(updatedUserInfo);
                    }
                }
            }
        }
    );

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentUserInfoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(UserInfoViewModel.class);
        
        // Load data from database
        showLoading(true);
        viewModel.loadDataFromDatabase();
        showLoading(false);

        // Observe data from ViewModel
        viewModel.getUserInfo().observe(getViewLifecycleOwner(), this::updateUserInfoUI);

        // Handle edit button
        binding.btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), EditUserInfoActivity.class);
            intent.putParcelableArrayListExtra("currentUserInfo", 
                new ArrayList<>(viewModel.getUserInfo().getValue() != null ? 
                    viewModel.getUserInfo().getValue() : new ArrayList<>()));
            editUserInfoLauncher.launch(intent);
        });

        // Handle logout button
        binding.btnLogout.setOnClickListener(v -> showLogoutConfirmationDialog());
    }

    private void updateUserInfoUI(List<UserInfo> userInfoList) {
        for (UserInfo userInfo : userInfoList) {
            switch (userInfo.getLabel()) {
                case "Họ và tên":
                    binding.tvFullName.setText(userInfo.getValue());
                    break;
                case "Mã KH (CIF)":
                    binding.tvCustomerId.setText(userInfo.getValue());
                    break;
                case "Số điện thoại":
                    binding.tvPhone.setText(userInfo.getValue());
                    break;
                case "Email":
                    binding.tvEmail.setText(userInfo.getValue());
                    break;
                case "Ngày sinh":
                    binding.tvDob.setText(userInfo.getValue());
                    break;
                case "Giới tính":
                    binding.tvGender.setText(userInfo.getValue());
                    break;
                case "Ngày cấp":
                    binding.tvIssueDate.setText(userInfo.getValue());
                    break;
            }
        }
    }

    private void showLogoutConfirmationDialog() {
        new AlertDialog.Builder(requireContext())
            .setTitle("Xác nhận đăng xuất")
            .setMessage("Bạn có chắc chắn muốn đăng xuất?")
            .setPositiveButton("Có", (dialog, which) -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).logout();
                }
            })
            .setNegativeButton("Không", (dialog, which) -> dialog.dismiss())
            .setCancelable(true)
            .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void showLoading(boolean show) {
        if (binding != null) {
            binding.loadingOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
}