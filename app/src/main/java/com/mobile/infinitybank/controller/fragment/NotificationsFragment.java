package com.mobile.infinitybank.controller.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.firestore.DocumentSnapshot;
import com.mobile.infinitybank.adapter.NotificationAdapter;
import com.mobile.infinitybank.databinding.FragmentNotificationsBinding;
import com.mobile.infinitybank.model.NotificationItem;
import com.mobile.infinitybank.model.NotificationType;
import com.mobile.infinitybank.model.User;
import com.mobile.infinitybank.service.NotificationService;
import com.mobile.infinitybank.service.UserService;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class NotificationsFragment extends Fragment {
    @Inject
    NotificationService notificationService;
    @Inject
    UserService userService;
    
    private FragmentNotificationsBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Thiết lập RecyclerView
        binding.rvNotifications.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvNotifications.setHasFixedSize(true);

        List<NotificationItem> notificationItems = new ArrayList<>();
        List<NotificationItem> tmp = new ArrayList<>();

        notificationItems.add(NotificationItem.createHeader("Thông báo mới"));
        showLoading(true);
        userService.getCurrentUser().addOnSuccessListener(documentSnapshot -> {
            User currentUser = documentSnapshot.toObject(User.class);
            notificationService.get10NotificationsByUserId(currentUser.getId()).addOnSuccessListener(querySnapshot -> {
                Date today = new Date();
                today.setHours(0);
                for (DocumentSnapshot documentSnapshot2 : querySnapshot.getDocuments()) {
                    NotificationItem notificationItem = documentSnapshot2.toObject(NotificationItem.class);
                    if (notificationItem.getTime().after(today)) {
                        notificationItems.add(notificationItem);
                    }
                    else {
                        tmp.add(notificationItem);
                    }
                }
                notificationItems.add(NotificationItem.createHeader("Thông báo cũ"));
                notificationItems.addAll(tmp);

                // Thiết lập Adapter cho RecyclerView
                NotificationAdapter adapter = new NotificationAdapter(notificationItems);
                binding.rvNotifications.setAdapter(adapter);
            });
        });
        showLoading(false);

        // Xử lý nút quay lại
        binding.ivBack.setOnClickListener(v -> requireActivity().onBackPressed());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void showLoading(boolean show) {
        binding.loadingOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
    }
} 