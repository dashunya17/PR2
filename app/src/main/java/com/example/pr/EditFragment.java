package com.example.pr;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class EditFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private ServiceAdapter adapter;
    private List<Service> serviceList;

    private FirebaseFirestore db;

    // Диалог для редактирования
    private AlertDialog editDialog;
    private EditText editDialogName, editDialogDescription, editDialogTrainerName;
    private EditText editDialogPrice, editDialogDuration, editDialogMaxClients;
    private EditText editDialogAvailableSeats, editDialogData, editDialogTime;
    private Button editDialogButtonDate, editDialogButtonTime;
    private String currentEditDocumentId;
    private Calendar calendar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit, container, false);

        // Инициализация Firestore
        db = FirebaseFirestore.getInstance();
        calendar = Calendar.getInstance();
        serviceList = new ArrayList<>();

        // Инициализация UI
        recyclerView = view.findViewById(R.id.recyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmpty = view.findViewById(R.id.tvEmpty);

        // Настройка RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ServiceAdapter(serviceList, new ServiceAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                showServiceDetails(position);
            }

            @Override
            public void onEditClick(int position) {
                showEditDialog(position);
            }

            @Override
            public void onDeleteClick(int position) {
                showDeleteConfirmation(position);
            }
        });
        recyclerView.setAdapter(adapter);

        // Загрузка данных
        loadServices();

        return view;
    }

    private void loadServices() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        // ИСПРАВЛЕНО: Используем "service" вместо "services"
        db.collection("service") // <- Вот здесь была ошибка!
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        progressBar.setVisibility(View.GONE);

                        if (task.isSuccessful()) {
                            serviceList.clear();

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                try {
                                    // Получаем данные из документа
                                    String name = document.getString("name");
                                    String description = document.getString("description");
                                    String trainerName = document.getString("trainerName");
                                    String data = document.getString("data");
                                    String time = document.getString("time");

                                    Double price = document.getDouble("price");
                                    Long duration = document.getLong("duration");
                                    Long maxClients = document.getLong("maxClients");
                                    Long availableSeats = document.getLong("availableSeats");

                                    // Создаем объект Service
                                    Service service = new Service();
                                    service.setId(document.getId());
                                    service.setName(name != null ? name : "Не указано");
                                    service.setDescription(description != null ? description : "");
                                    service.setTrainerName(trainerName != null ? trainerName : "Не указан");
                                    service.setData(data != null ? data : "");
                                    service.setTime(time != null ? time : "");
                                    service.setPrice(price != null ? price : 0.0);
                                    service.setDuration(duration != null ? duration.intValue() : 0);
                                    service.setMaxClients(maxClients != null ? maxClients.intValue() : 0);
                                    service.setAvailableSeats(availableSeats != null ? availableSeats.intValue() : 0);

                                    serviceList.add(service);

                                } catch (Exception e) {
                                    Toast.makeText(getContext(),
                                            "Ошибка обработки услуги: " + e.getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                }
                            }

                            adapter.updateData(serviceList);

                            // Показываем сообщение, если список пуст
                            if (serviceList.isEmpty()) {
                                tvEmpty.setText("Нет доступных услуг");
                                tvEmpty.setVisibility(View.VISIBLE);
                                recyclerView.setVisibility(View.GONE);
                            } else {
                                tvEmpty.setVisibility(View.GONE);
                                recyclerView.setVisibility(View.VISIBLE);
                            }

                        } else {
                            Toast.makeText(getContext(),
                                    "Ошибка загрузки: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                            tvEmpty.setText("Ошибка загрузки данных");
                            tvEmpty.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                        }
                    }
                });
    }

    private void showServiceDetails(int position) {
        if (position >= serviceList.size()) return;

        Service service = serviceList.get(position);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(service.getName())
                .setMessage(
                        "Тренер: " + service.getTrainerName() + "\n" +
                                "Дата: " + service.getData() + "\n" +
                                "Время: " + service.getTime() + "\n" +
                                "Цена: " + service.getPrice() + " руб\n" +
                                "Продолжительность: " + service.getDuration() + " мин\n" +
                                "Макс. клиентов: " + service.getMaxClients() + "\n" +
                                "Доступные места: " + service.getAvailableSeats() + "\n\n" +
                                "Описание:\n" + service.getDescription()
                )
                .setPositiveButton("OK", null)
                .show();
    }

    private void showDeleteConfirmation(int position) {
        Service service = serviceList.get(position);

        new AlertDialog.Builder(getContext())
                .setTitle("Удаление услуги")
                .setMessage("Вы уверены, что хотите удалить услугу \"" + service.getName() + "\"?")
                .setPositiveButton("Удалить", (dialog, which) -> deleteService(position))
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void deleteService(int position) {
        if (position >= serviceList.size()) return;

        Service service = serviceList.get(position);
        String documentId = service.getId();

        if (TextUtils.isEmpty(documentId)) {
            Toast.makeText(getContext(), "Ошибка: ID документа отсутствует", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        // ИСПРАВЛЕНО: Используем "service" вместо "services"
        db.collection("service").document(documentId)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        progressBar.setVisibility(View.GONE);
                        serviceList.remove(position);
                        adapter.notifyItemRemoved(position);
                        Toast.makeText(getContext(), "Услуга удалена", Toast.LENGTH_SHORT).show();

                        // Обновляем отображение пустого списка
                        if (serviceList.isEmpty()) {
                            tvEmpty.setText("Нет доступных услуг");
                            tvEmpty.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Ошибка удаления: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showEditDialog(int position) {
        if (position >= serviceList.size()) return;

        Service service = serviceList.get(position);
        currentEditDocumentId = service.getId();

        // Создание диалога
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edit_service, null);

        initEditDialogViews(dialogView);
        populateEditDialogFields(service);

        builder.setView(dialogView)
                .setTitle("Редактирование услуги")
                .setPositiveButton("Сохранить", null)
                .setNegativeButton("Отмена", null);

        editDialog = builder.create();

        // Переопределяем обработчик кнопки "Сохранить"
        editDialog.setOnShowListener(dialog -> {
            Button saveButton = editDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            saveButton.setOnClickListener(v -> updateService());
        });

        editDialog.show();
    }

    private void initEditDialogViews(View dialogView) {
        editDialogName = dialogView.findViewById(R.id.editDialogName);
        editDialogDescription = dialogView.findViewById(R.id.editDialogDescription);
        editDialogTrainerName = dialogView.findViewById(R.id.editDialogTrainerName);
        editDialogPrice = dialogView.findViewById(R.id.editDialogPrice);
        editDialogDuration = dialogView.findViewById(R.id.editDialogDuration);
        editDialogMaxClients = dialogView.findViewById(R.id.editDialogMaxClients);
        editDialogAvailableSeats = dialogView.findViewById(R.id.editDialogAvailableSeats);
        editDialogData = dialogView.findViewById(R.id.editDialogData);
        editDialogTime = dialogView.findViewById(R.id.editDialogTime);
        editDialogButtonDate = dialogView.findViewById(R.id.editDialogButtonDate);
        editDialogButtonTime = dialogView.findViewById(R.id.editDialogButtonTime);

        // Настройка выбора даты и времени
        if (editDialogButtonDate != null) {
            editDialogButtonDate.setOnClickListener(v -> showDatePickerDialog());
        }
        if (editDialogButtonTime != null) {
            editDialogButtonTime.setOnClickListener(v -> showTimePickerDialog());
        }
    }

    private void populateEditDialogFields(Service service) {
        if (editDialogName != null) editDialogName.setText(service.getName());
        if (editDialogDescription != null) editDialogDescription.setText(service.getDescription());
        if (editDialogTrainerName != null) editDialogTrainerName.setText(service.getTrainerName());
        if (editDialogPrice != null) editDialogPrice.setText(String.valueOf(service.getPrice()));
        if (editDialogDuration != null) editDialogDuration.setText(String.valueOf(service.getDuration()));
        if (editDialogMaxClients != null) editDialogMaxClients.setText(String.valueOf(service.getMaxClients()));
        if (editDialogAvailableSeats != null) editDialogAvailableSeats.setText(String.valueOf(service.getAvailableSeats()));
        if (editDialogData != null) editDialogData.setText(service.getData());
        if (editDialogTime != null) editDialogTime.setText(service.getTime());
    }

    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
                    if (editDialogData != null) {
                        editDialogData.setText(dateFormat.format(calendar.getTime()));
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void showTimePickerDialog() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                getContext(),
                (view, hourOfDay, minute) -> {
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, minute);

                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    if (editDialogTime != null) {
                        editDialogTime.setText(timeFormat.format(calendar.getTime()));
                    }
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
        );
        timePickerDialog.show();
    }

    private void updateService() {
        // Валидация полей
        if (editDialogName == null || TextUtils.isEmpty(editDialogName.getText().toString())) {
            if (editDialogName != null) editDialogName.setError("Введите название");
            return;
        }

        try {
            // Подготовка данных для обновления
            Map<String, Object> updates = new HashMap<>();
            updates.put("name", editDialogName.getText().toString());
            updates.put("description", editDialogDescription != null ? editDialogDescription.getText().toString() : "");
            updates.put("trainerName", editDialogTrainerName != null ? editDialogTrainerName.getText().toString() : "");
            updates.put("data", editDialogData != null ? editDialogData.getText().toString() : "");
            updates.put("time", editDialogTime != null ? editDialogTime.getText().toString() : "");
            updates.put("price", editDialogPrice != null ? Double.parseDouble(editDialogPrice.getText().toString()) : 0.0);
            updates.put("duration", editDialogDuration != null ? Integer.parseInt(editDialogDuration.getText().toString()) : 0);
            updates.put("maxClients", editDialogMaxClients != null ? Integer.parseInt(editDialogMaxClients.getText().toString()) : 0);
            updates.put("availableSeats", editDialogAvailableSeats != null ? Integer.parseInt(editDialogAvailableSeats.getText().toString()) : 0);

            progressBar.setVisibility(View.VISIBLE);

            // ИСПРАВЛЕНО: Используем "service" вместо "services"
            db.collection("service").document(currentEditDocumentId)
                    .update(updates)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            progressBar.setVisibility(View.GONE);
                            if (editDialog != null) {
                                editDialog.dismiss();
                            }

                            // Обновление локального списка
                            loadServices();
                            Toast.makeText(getContext(), "Услуга обновлена", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(getContext(), "Ошибка обновления: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Некорректные числовые значения", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(getContext(), "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (editDialog != null && editDialog.isShowing()) {
            editDialog.dismiss();
        }
    }
}