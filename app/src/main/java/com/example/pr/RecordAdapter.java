package com.example.pr;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class RecordAdapter extends RecyclerView.Adapter<RecordAdapter.RecordViewHolder> {

    private List<Record> recordList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(int position);
        void onDeleteClick(int position);
    }

    public RecordAdapter(List<Record> recordList, OnItemClickListener listener) {
        this.recordList = recordList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_record, parent, false);
        return new RecordViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecordViewHolder holder, int position) {
        Record record = recordList.get(position);

        holder.textViewClient.setText(record.getSurname() + " " + record.getName());
        holder.textViewPhone.setText("Тел: " + record.getPhone());
        holder.textViewClients.setText("Клиентов: " + record.getNumberClients());
        holder.textViewService.setText("Услуга: " + record.getServiceName());
        holder.textViewDate.setText("Запись: " + record.getRegistrationDate());

        // Обработчики кликов
        holder.itemView.setOnClickListener(v -> listener.onItemClick(position));
        holder.buttonDelete.setOnClickListener(v -> listener.onDeleteClick(position));
    }

    @Override
    public int getItemCount() {
        return recordList.size();
    }

    static class RecordViewHolder extends RecyclerView.ViewHolder {
        TextView textViewClient, textViewPhone, textViewClients, textViewService, textViewDate;
        ImageButton buttonDelete;

        public RecordViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewClient = itemView.findViewById(R.id.textViewClient);
            textViewPhone = itemView.findViewById(R.id.textViewPhone);
            textViewClients = itemView.findViewById(R.id.textViewClients);
            textViewService = itemView.findViewById(R.id.textViewService);
            textViewDate = itemView.findViewById(R.id.textViewDate);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
        }
    }
}