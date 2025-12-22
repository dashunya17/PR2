package com.example.pr;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ServiceAdapter extends RecyclerView.Adapter<ServiceAdapter.ServiceViewHolder> {

    private List<Service> serviceList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(int position);
        void onEditClick(int position);
        void onDeleteClick(int position);
    }

    public ServiceAdapter(List<Service> serviceList, OnItemClickListener listener) {
        this.serviceList = serviceList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_service_adapter, parent, false);
        return new ServiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceViewHolder holder, int position) {
        Service service = serviceList.get(position);

        holder.textViewName.setText(service.getName());
        holder.textViewTrainer.setText("Тренер: " + service.getTrainerName());
        holder.textViewDateTime.setText(service.getData() + " " + service.getTime());
        holder.textViewPrice.setText("Цена: " + service.getPrice() + " руб");
        holder.textViewSeats.setText("Места: " + service.getAvailableSeats() + "/" + service.getMaxClients());

        // Обработчики кликов
        holder.itemView.setOnClickListener(v -> listener.onItemClick(position));
        holder.buttonEdit.setOnClickListener(v -> listener.onEditClick(position));
        holder.buttonDelete.setOnClickListener(v -> listener.onDeleteClick(position));
    }

    @Override
    public int getItemCount() {
        return serviceList.size();
    }

    public void updateData(List<Service> newServiceList) {
        serviceList = newServiceList;
        notifyDataSetChanged();
    }

    public Service getServiceAt(int position) {
        return serviceList.get(position);
    }

    static class ServiceViewHolder extends RecyclerView.ViewHolder {
        TextView textViewName, textViewTrainer, textViewDateTime, textViewPrice, textViewSeats;
        View buttonEdit, buttonDelete;

        public ServiceViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.textViewName);
            textViewTrainer = itemView.findViewById(R.id.textViewTrainer);
            textViewDateTime = itemView.findViewById(R.id.textViewDateTime);
            textViewPrice = itemView.findViewById(R.id.textViewPrice);
            textViewSeats = itemView.findViewById(R.id.textViewSeats);
            buttonEdit = itemView.findViewById(R.id.buttonEdit);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
        }
    }
}