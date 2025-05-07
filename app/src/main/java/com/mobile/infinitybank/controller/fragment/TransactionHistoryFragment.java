package com.mobile.infinitybank.controller.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.mobile.infinitybank.R;
import com.mobile.infinitybank.adapter.TransactionAdapter;
import com.mobile.infinitybank.databinding.FragmentTransactionHistoryBinding;
import com.mobile.infinitybank.model.User;
import com.mobile.infinitybank.service.UserService;
import com.mobile.infinitybank.viewmodel.TransactionViewModel;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class TransactionHistoryFragment extends Fragment {

    private FragmentTransactionHistoryBinding binding;
    private TransactionViewModel viewModel;
    private static final String TAG = "TransactionHistoryFragment";
    @Inject
    UserService userService;

    private User currentUser;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        try {
            binding = FragmentTransactionHistoryBinding.inflate(inflater, container, false);
            return binding.getRoot();
        } catch (Exception e) {
            Log.e(TAG, "Error inflating layout", e);
            throw e;
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        showLoading(true);
        userService.getCurrentUser().addOnSuccessListener(documentSnapshot -> {
            currentUser = documentSnapshot.toObject(User.class);
            try {
                // Khởi tạo ViewModel
                viewModel = new ViewModelProvider(requireActivity()).get(TransactionViewModel.class);
    
                // Thiết lập RecyclerView
                binding.rvTransactionList.setLayoutManager(new LinearLayoutManager(getContext()));
    
                viewModel.loadTransactions(currentUser.getPhoneNumber());
    
                // Quan sát danh sách giao dịch từ ViewModel
                viewModel.getTransactions().observe(getViewLifecycleOwner(), transactions -> {
                    TransactionAdapter adapter = new TransactionAdapter(transactions, transaction -> {
                        viewModel.selectTransaction(transaction.getId());
                        NavHostFragment.findNavController(this).navigate(
                                R.id.action_transactionHistoryFragment_to_transactionDetailFragment
                        );
                    });
                    binding.rvTransactionList.setAdapter(adapter);
    
                    // Hiển thị thông báo nếu không có giao dịch
                    binding.tvNoTransactions.setVisibility(transactions.isEmpty() ? View.VISIBLE : View.GONE);
                });
    
                // Xử lý nút lọc (chưa triển khai logic, chỉ để sẵn)
                binding.btnFilterDate.setOnClickListener(v -> {
                    // TODO: Triển khai logic lọc theo ngày
                });
    
                binding.btnFilterCategory.setOnClickListener(v -> {
                    // TODO: Triển khai logic lọc theo danh mục
                    viewModel.refreshTransactions();
                });
            } catch (Exception e) {
                Log.e(TAG, "Error in onViewCreated", e);
            }
        });
        showLoading(false);
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