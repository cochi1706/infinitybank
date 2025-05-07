package com.mobile.infinitybank.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mobile.infinitybank.R;
import com.mobile.infinitybank.model.Transaction;
import com.mobile.infinitybank.interfaces.OnTransactionClickListener;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class TransactionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_DATE_HEADER = 0;
    private static final int TYPE_TRANSACTION = 1;

    private final List<DisplayItem> displayItems;
    private final OnTransactionClickListener onTransactionClickListener;

    public TransactionAdapter(List<Transaction> transactions, OnTransactionClickListener listener) {
        this.onTransactionClickListener = listener;
        // Sort transactions by date in descending order (newest first)
        transactions.sort((t1, t2) -> {
            if (t1.getDate() == null || t2.getDate() == null) {
                return 0;
            }
            return t2.getDate().compareTo(t1.getDate());
        });
        this.displayItems = generateDisplayItems(transactions);
    }

    @Override
    public int getItemViewType(int position) {
        return displayItems.get(position) instanceof DateHeader ? TYPE_DATE_HEADER : TYPE_TRANSACTION;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_DATE_HEADER) {
            View view = inflater.inflate(R.layout.item_date_header, parent, false);
            return new DateHeaderViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_transaction, parent, false);
            return new TransactionViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        DisplayItem item = displayItems.get(position);
        if (holder instanceof DateHeaderViewHolder) {
            ((DateHeaderViewHolder) holder).bind((DateHeader) item);
        } else if (holder instanceof TransactionViewHolder) {
            ((TransactionViewHolder) holder).bind((TransactionItem) item);
        }
    }

    @Override
    public int getItemCount() {
        return displayItems.size();
    }

    private List<DisplayItem> generateDisplayItems(List<Transaction> transactions) {
        List<DisplayItem> items = new ArrayList<>();
        Map<String, List<Transaction>> groupedByDate = new TreeMap<>((o1, o2) -> o2.compareTo(o1)); // Descending order

        // Group transactions by date
        for (Transaction transaction : transactions) {
            String date = transaction.getDate().toString();
            groupedByDate.computeIfAbsent(date, k -> new ArrayList<>()).add(transaction);
        }

        // Create display items
        for (Map.Entry<String, List<Transaction>> entry : groupedByDate.entrySet()) {
            items.add(new DateHeader(entry.getKey()));
            for (Transaction transaction : entry.getValue()) {
                items.add(new TransactionItem(transaction));
            }
        }

        return items;
    }

    // ViewHolder for date headers
    private static class DateHeaderViewHolder extends RecyclerView.ViewHolder {
        private TextView tvDate;

        public DateHeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
        }

        public void bind(DateHeader header) {
            tvDate.setText(header.getDate());
        }
    }

    // ViewHolder for transactions
    private class TransactionViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvDescription;
        private final TextView tvDetails;
        private final TextView tvAmount;
        private final NumberFormat currencyFormat;
        private TextView tvDate;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvDetails = itemView.findViewById(R.id.tvDetails);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            tvDate = itemView.findViewById(R.id.tvDate);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && displayItems.get(position) instanceof TransactionItem) {
                    Transaction transaction = ((TransactionItem) displayItems.get(position)).getTransaction();
                    onTransactionClickListener.onTransactionClick(transaction);
                }
            });
        }

        public void bind(TransactionItem item) {
            Transaction transaction = item.getTransaction();
            tvDescription.setText(transaction.getDescription());
            tvDetails.setText(transaction.getDetails());

            String amountText = transaction.isIncoming() ?
                    "+" + currencyFormat.format(transaction.getAmount()) :
                    currencyFormat.format(transaction.getAmount());
            tvAmount.setText(amountText);
            tvDate.setText(new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault()).format(transaction.getDate()));

            int textColor = transaction.isIncoming() ?
                    itemView.getContext().getColor(android.R.color.holo_green_dark) :
                    itemView.getContext().getColor(android.R.color.holo_red_dark);
            tvAmount.setTextColor(textColor);
        }
    }

    // Abstract class for display items
    private abstract static class DisplayItem {
    }

    // Class for date headers
    private static class DateHeader extends DisplayItem {
        private final String date;

        public DateHeader(String date) {
            this.date = date;
        }

        public String getDate() {
            return date;
        }
    }

    // Class for transaction items
    private static class TransactionItem extends DisplayItem {
        private final Transaction transaction;

        public TransactionItem(Transaction transaction) {
            this.transaction = transaction;
        }

        public Transaction getTransaction() {
            return this.transaction;
        }
    }
} 