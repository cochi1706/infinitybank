package com.mobile.infinitybank.controller;

import android.app.AlertDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.widget.EditText;
import android.widget.Toast;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.badge.BadgeDrawable;
import com.mobile.infinitybank.R;
import com.mobile.infinitybank.controller.fragment.MainFragment;
import com.mobile.infinitybank.controller.fragment.NotificationsFragment;
import com.mobile.infinitybank.controller.fragment.LocationFragment;
import com.mobile.infinitybank.controller.fragment.ProfileFragment;
import com.mobile.infinitybank.service.UserService;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {
    @Inject
    UserService userService;
    BottomNavigationView bottomNavigationView;
    ConnectivityManager connectivityManager;
    private Map<Integer, String> fragmentTagMap;

    private final ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
        @Override
        public void onAvailable(@NonNull Network network) {
            runOnUiThread(() -> Toast.makeText(MainActivity.this, "Đã kết nối mạng!", Toast.LENGTH_SHORT).show());
        }

        @Override
        public void onLost(@NonNull Network network) {
            runOnUiThread(() -> Toast.makeText(MainActivity.this, "Mất kết nối mạng!", Toast.LENGTH_SHORT).show());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Initialize fragmentTagMap
        fragmentTagMap = new HashMap<>();
        fragmentTagMap.put(R.id.nav_home, "MainFragment");
        fragmentTagMap.put(R.id.nav_notifications, "NotificationsFragment");
        fragmentTagMap.put(R.id.nav_location, "LocationFragment");
        fragmentTagMap.put(R.id.nav_profile, "ProfileFragment");

        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        // Đăng ký NetworkCallback để lắng nghe sự kiện thay đổi mạng
        NetworkRequest networkRequest = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build();
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();
            
            if (itemId == R.id.nav_home) {
                selectedFragment = new MainFragment();
            } else if (itemId == R.id.nav_notifications) {
                selectedFragment = new NotificationsFragment();
            } else if (itemId == R.id.nav_location) {
                selectedFragment = new LocationFragment();
            } else if (itemId == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment, fragmentTagMap.get(item.getItemId()))
                        .addToBackStack(null)
                        .commit();
            }
            return true;
        });

        // Set up notification badge
        int notificationCount = 1; // Example notification count
        if (notificationCount > 0) {
            BadgeDrawable badge = bottomNavigationView.getOrCreateBadge(R.id.nav_notifications);
            badge.setNumber(notificationCount);
            badge.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));
            badge.setVisible(true);
        }

        // Hiển thị fragment mặc định nếu chưa có fragment nào
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new MainFragment(), "MainFragment")
                    .commit();
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
        } else {
            // Khôi phục fragment đã chọn trước đó
            String currentFragmentTag = getSupportFragmentManager().getFragments().isEmpty() ? null :
                    getSupportFragmentManager().getFragments().get(getSupportFragmentManager().getFragments().size() - 1).getTag();
            if (currentFragmentTag != null) {
                for (Map.Entry<Integer, String> entry : fragmentTagMap.entrySet()) {
                    if (entry.getValue().equals(currentFragmentTag)) {
                        bottomNavigationView.setSelectedItemId(entry.getKey());
                        break;
                    }
                }
            }
        }

        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String pin = sharedPreferences.getString("pin", null);
        if (pin == null) {
            // Show dialog to create PIN
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Tạo mã PIN");
            builder.setMessage("Vui lòng tạo mã PIN 6 số để bảo vệ tài khoản của bạn");

            // Set up the input
            final EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
            input.setFilters(new InputFilter[] { new InputFilter.LengthFilter(6) });
            builder.setView(input);

            // Set up the buttons
            builder.setPositiveButton("Xác nhận", (dialog, which) -> {
                String newPin = input.getText().toString();
                if (newPin.length() == 6) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("pin", newPin);
                    editor.apply();
                    Toast.makeText(MainActivity.this, "Đã tạo mã PIN thành công", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Mã PIN phải có 6 số", Toast.LENGTH_SHORT).show();
                }
            });
            builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());

            builder.setCancelable(false);
            builder.show();
        }
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
            Fragment currentFragment = getSupportFragmentManager().getFragments().isEmpty() ? null :
                    getSupportFragmentManager().getFragments().get(getSupportFragmentManager().getFragments().size() - 1);
            if (currentFragment != null && currentFragment.getTag() != null) {
                for (Map.Entry<Integer, String> entry : fragmentTagMap.entrySet()) {
                    if (entry.getValue().equals(currentFragment.getTag())) {
                        bottomNavigationView.setSelectedItemId(entry.getKey());
                        break;
                    }
                }
            }
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Hủy đăng ký NetworkCallback khi Activity bị hủy
        connectivityManager.unregisterNetworkCallback(networkCallback);
    }

    public void logout() {
        userService.logout();
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isLoggedIn", false);
        editor.remove("pin");
        editor.apply();

        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}