package com.example.pr;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AddFragment extends Fragment {

    private EditText editTextName, editTextDescription, editTextTrainerName;
    private EditText editTextPrice, editTextDuration, editTextMaxClients;
    private EditText editTextAvailableSeats, editTextData, editTextTime;
    private Button buttonSelectDate, buttonSelectTime, buttonAddService;

    private FirebaseFirestore db;
    private Calendar calendar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add, container, false);

        // Инициализация Firestore
        db = FirebaseFirestore.getInstance();
        calendar = Calendar.getInstance();

        // Инициализация элементов интерфейса
        initViews(view);

        // Настройка обработчиков событий
        setupListeners();

        return view;
    }

    private void initViews(View view) {
        editTextName = view.findViewById(R.id.editTextName);
        editTextDescription = view.findViewById(R.id.editTextDescription);
        editTextTrainerName = view.findViewById(R.id.editTextTrainerName);
        editTextPrice = view.findViewById(R.id.editTextPrice);
        editTextDuration = view.findViewById(R.id.editTextDuration);
        editTextMaxClients = view.findViewById(R.id.editTextMaxClients);
        editTextAvailableSeats = view.findViewById(R.id.editTextAvailableSeats);
        editTextData = view.findViewById(R.id.editTextData);
        editTextTime = view.findViewById(R.id.editTextTime);

        buttonSelectDate = view.findViewById(R.id.buttonSelectDate);
        buttonSelectTime = view.findViewById(R.id.buttonSelectTime);
        buttonAddService = view.findViewById(R.id.buttonAddService);
    }

    private void setupListeners() {
        // Выбор даты
        buttonSelectDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });

        // Выбор времени
        buttonSelectTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePicker();
            }
        });

        // Добавление услуги в базу данных
        buttonAddService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addServiceToFirestore();
            }
        });
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
                    editTextData.setText(dateFormat.format(calendar.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void showTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                requireContext(),
                (view, hourOfDay, minute) -> {
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, minute);

                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    editTextTime.setText(timeFormat.format(calendar.getTime()));
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
        );
        timePickerDialog.show();
    }

    private void addServiceToFirestore() {
        // Получение данных из полей ввода
        String name = editTextName.getText().toString().trim();
        String description = editTextDescription.getText().toString().trim();
        String trainerName = editTextTrainerName.getText().toString().trim();
        String data = editTextData.getText().toString().trim();
        String time = editTextTime.getText().toString().trim();

        // Проверка заполненности обязательных полей
        if (name.isEmpty() || description.isEmpty() || trainerName.isEmpty() ||
                data.isEmpty() || time.isEmpty()) {
            Toast.makeText(requireContext(), "Заполните все обязательные поля", Toast.LENGTH_SHORT).show();
            return;
        }

        // Парсинг числовых значений
        try {
            double price = Double.parseDouble(editTextPrice.getText().toString().trim());
            int duration = Integer.parseInt(editTextDuration.getText().toString().trim());
            int maxClients = Integer.parseInt(editTextMaxClients.getText().toString().trim());
            int availableSeats = Integer.parseInt(editTextAvailableSeats.getText().toString().trim());

            // Создание объекта для записи в Firestore
            Map<String, Object> service = new HashMap<>();
            service.put("name", name);
            service.put("description", description);
            service.put("trainerName", trainerName);
            service.put("data", data);
            service.put("time", time);
            service.put("price", price);
            service.put("duration", duration);
            service.put("maxClients", maxClients);
            service.put("availableSeats", availableSeats);
            service.put("createdAt", System.currentTimeMillis()); // Дополнительное поле с timestamp

            // Добавление документа в коллекцию "services"
            db.collection("service")
                    .add(service)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Toast.makeText(requireContext(), "Услуга добавлена!", Toast.LENGTH_SHORT).show();
                            clearForm();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(requireContext(), "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), "Некорректные числовые значения", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearForm() {
        editTextName.setText("");
        editTextDescription.setText("");
        editTextTrainerName.setText("");
        editTextPrice.setText("");
        editTextDuration.setText("");
        editTextMaxClients.setText("");
        editTextAvailableSeats.setText("");
        editTextData.setText("");
        editTextTime.setText("");
    }
}