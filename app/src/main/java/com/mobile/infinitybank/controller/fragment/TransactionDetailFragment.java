package com.mobile.infinitybank.controller.fragment;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.mobile.infinitybank.R;
import com.mobile.infinitybank.databinding.FragmentTransactionDetailBinding;
import com.mobile.infinitybank.viewmodel.TransactionViewModel;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class TransactionDetailFragment extends Fragment {

    private FragmentTransactionDetailBinding binding;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    private TransactionViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTransactionDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @RequiresApi(Build.VERSION_CODES.M)
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(TransactionViewModel.class);

        // Quan sát giao dịch được chọn từ ViewModel
        viewModel.getSelectedTransaction().observe(getViewLifecycleOwner(), transaction -> {
            if (transaction != null) {
                // Hiển thị số tiền
                binding.tvAmount.setText(transaction.isIncoming() ?
                        "+" + currencyFormat.format(transaction.getAmount()) :
                        currencyFormat.format(transaction.getAmount()));
                binding.tvAmount.setTextColor(
                        transaction.isIncoming() ?
                                requireContext().getColor(android.R.color.holo_green_dark) :
                                requireContext().getColor(android.R.color.holo_red_dark)
                );

                // Hiển thị thông tin tài khoản
                binding.tvSourceAccount.setText(transaction.getSourceAccount());
                binding.tvDestinationAccount.setText(transaction.getDestinationAccount());

                // Hiển thị chi tiết giao dịch
                binding.tvTime.setText(new SimpleDateFormat("HH:mm dd/MM/yyyy").format(transaction.getDate()));
                binding.tvTransactionId.setText(transaction.getId());
                binding.tvDestination.setText(transaction.getDescription());
                binding.tvStatus.setText(transaction.getStatus());
                binding.tvDetails.setText(transaction.getDetails());

                // Hiển thị biểu tượng trạng thái
                binding.ivStatusIcon.setImageResource(
                        "Thành công".equals(transaction.getStatus()) ?
                                R.drawable.ic_success :
                                R.drawable.ic_failed
                );
                binding.ivStatusIcon.setVisibility(View.VISIBLE);
            }
        });

        // Xử lý nút quay lại
        binding.btnBack.setOnClickListener(v -> {
            viewModel.clearSelectedTransaction();
            requireActivity().onBackPressed();
        });

        // Xử lý nút "Thực hiện lại"
        binding.btnRepeat.setOnClickListener(v -> {
            // TODO: Triển khai logic thực hiện lại giao dịch
        });

        // Xử lý nút "Chia sẻ"
        binding.btnShare.setOnClickListener(v -> {
            String shareText = "Chi Tiết Giao Dịch\n" +
                    "Số tiền: " + binding.tvAmount.getText() + "\n" +
                    "Từ tài khoản: " + binding.tvSourceAccount.getText() + "\n" +
                    "Tới tài khoản: " + binding.tvDestinationAccount.getText() + "\n" +
                    "Thời gian: " + binding.tvTime.getText() + "\n" +
                    "Mã giao dịch: " + binding.tvTransactionId.getText();

            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
            shareIntent.setType("text/plain");
            startActivity(Intent.createChooser(shareIntent, "Chia sẻ giao dịch"));
        });

        // Xử lý nút "Xác nhận GD"
        binding.btnConfirm.setOnClickListener(v -> {
            // TODO: Triển khai logic xác nhận giao dịch
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 