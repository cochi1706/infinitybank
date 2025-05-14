package com.mobile.infinitybank.controller.fragment;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.mobile.infinitybank.R;
import com.mobile.infinitybank.databinding.FragmentProfileBinding;
import com.google.android.material.tabs.TabLayout;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private NavController navController;
    private boolean isNavigatingFromTab = false; // Biến cờ để ngăn vòng lặp
    private static final String TAG = "ProfileFragment";
    private SharedPreferences sharedPreferences;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try {
            // Lấy NavHostFragment con và NavController
            NavHostFragment navHostFragment = (NavHostFragment) getChildFragmentManager()
                    .findFragmentById(R.id.profile_nav_host_fragment);
            if (navHostFragment == null) {
                Log.e(TAG, "NavHostFragment is null");
                return;
            }
            navController = navHostFragment.getNavController();

            // Thêm các tab vào TabLayout
            binding.tabLayout.removeAllTabs(); // Xóa các tab cũ để tránh trùng lặp
            binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Thông tin người dùng"));
            binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Thông tin tài khoản"));
            binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Lịch sử giao dịch"));
            binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Giao dịch tài khoản"));
            sharedPreferences = this.getActivity().getSharedPreferences("MyPrefs", MODE_PRIVATE);
            Boolean isEmployee = sharedPreferences.getBoolean("isEmployee", false);
            if (isEmployee) {
                binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Cập nhật lãi suất"));
            }

            // Đồng bộ TabLayout với NavController
            binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    if (tab != null) {
                        try {
                            isNavigatingFromTab = true; // Đánh dấu rằng điều hướng bắt nguồn từ tab
                            switch (tab.getPosition()) {
                                case 0:
                                    navController.navigate(R.id.userInfoFragment);
                                    break;
                                case 1:
                                    navController.navigate(R.id.accountInfoFragment);
                                    break;
                                case 2:
                                    navController.navigate(R.id.transactionHistoryFragment);
                                    break;
                                case 3:
                                    navController.navigate(R.id.depositWithdrawFragment);
                                    break;
                                case 4:
                                    navController.navigate(R.id.updateRateFragment);
                                    break;
                                default:
                                    Log.w(TAG, "Invalid tab position: " + tab.getPosition());
                            }
                        } catch (IllegalArgumentException e) {
                            Log.e(TAG, "Navigation error for tab " + tab.getPosition(), e);
                        } finally {
                            isNavigatingFromTab = false; // Reset cờ sau khi điều hướng
                        }
                    } else {
                        Log.w(TAG, "Tab is null");
                    }
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {
                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {
                    if (tab != null) {
                        Log.d(TAG, "Tab reselected: " + tab.getPosition());
                    }
                }

            });

            binding.ivBack.setOnClickListener(v -> {
                requireActivity().onBackPressed();
            });

            // Đồng bộ trạng thái tab khi điều hướng
            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                if (isNavigatingFromTab) {
                    return;
                }
                try {
                    int destinationId = destination.getId();
                    if (destinationId == R.id.userInfoFragment && binding.tabLayout.getSelectedTabPosition() != 0) {
                        binding.tabLayout.selectTab(binding.tabLayout.getTabAt(0));
                    } else if (destinationId == R.id.accountInfoFragment && binding.tabLayout.getSelectedTabPosition() != 1) {
                        binding.tabLayout.selectTab(binding.tabLayout.getTabAt(1));
                    } else if (destinationId == R.id.transactionHistoryFragment && binding.tabLayout.getSelectedTabPosition() != 2) {
                        binding.tabLayout.selectTab(binding.tabLayout.getTabAt(2));
                    } else if (destinationId == R.id.depositWithdrawFragment && binding.tabLayout.getSelectedTabPosition() != 3) {
                        binding.tabLayout.selectTab(binding.tabLayout.getTabAt(3));
                    } else if (destinationId == R.id.updateRateFragment && binding.tabLayout.getSelectedTabPosition() != 4) {
                        binding.tabLayout.selectTab(binding.tabLayout.getTabAt(4));
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error syncing tab with destination " + destination.getId(), e);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in onViewCreated", e);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        navController = null; // Tránh memory leak
    }
} 