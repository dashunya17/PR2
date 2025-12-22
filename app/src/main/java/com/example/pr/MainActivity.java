package com.example.pr;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AdminLoginDialogFragment.LoginDialogListener {

    private Button btnAdmin, btnRecord;
    private FirebaseFirestore db;
    private ListView listView;
    private List<Service> serviceList;
    private ArrayAdapter<String> adapter;
    private List<String> serviceTitles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Инициализация Firestore
        db = FirebaseFirestore.getInstance();
        serviceList = new ArrayList<>();
        serviceTitles = new ArrayList<>();

        // Инициализация UI элементов
        btnAdmin = findViewById(R.id.button);
        btnRecord = findViewById(R.id.button2);
        listView = findViewById(R.id.listView1);

        // Создание адаптера для ListView
        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, serviceTitles);
        listView.setAdapter(adapter);

        // Обработка клика по элементу списка
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position < serviceList.size()) {
                    showServiceDetails(position);
                }
            }
        });

        // Кнопка администратора
        btnAdmin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAdminLoginDialog();
            }
        });

        // Кнопка записи
        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RegistrationActivity.class);
                startActivity(intent);
            }
        });

        // Загрузка данных из Firestore
        loadServicesFromFirestore();
    }

    private void loadServicesFromFirestore() {

        db.collection("service")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if (task.isSuccessful()) {
                            serviceList.clear();
                            serviceTitles.clear();

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                try {
                                    // Получаем данные из документа
                                    String name = document.getString("name");
                                    String description = document.getString("description");
                                    String trainerName = document.getString("trainerName");
                                    String data = document.getString("data");
                                    String time = document.getString("time");

                                    // Получаем числовые значения
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

                                    // Формируем строку для отображения в списке
                                    String displayText = name + " - " + data + " " + time;
                                    if (price != null) {
                                        displayText += " (" + price + " руб)";
                                    }
                                    serviceTitles.add(displayText);

                                } catch (Exception e) {
                                    Toast.makeText(MainActivity.this,
                                            "Ошибка обработки документа: " + e.getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                }
                            }

                            adapter.notifyDataSetChanged();

                            // Показываем сообщение, если список пуст
                            if (serviceList.isEmpty()) {
                                Toast.makeText(MainActivity.this, "Нет доступных услуг", Toast.LENGTH_SHORT).show();
                                listView.setVisibility(View.GONE);
                            } else {
                                listView.setVisibility(View.VISIBLE);
                            }

                        } else {
                            Toast.makeText(MainActivity.this,
                                    "Ошибка загрузки: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                            Toast.makeText(MainActivity.this, "Ошибка загрузки данных", Toast.LENGTH_SHORT).show();
                            listView.setVisibility(View.GONE);
                        }
                    }
                });
    }

    private void showServiceDetails(int position) {
        if (position >= serviceList.size()) return;

        Service service = serviceList.get(position);

        // Создаем диалог с деталями услуги
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle(service.getName());

        String details =
                "Тренер: " + service.getTrainerName() + "\n" +
                        "Дата: " + service.getData() + "\n" +
                        "Время: " + service.getTime() + "\n" +
                        "Цена: " + service.getPrice() + " руб\n" +
                        "Продолжительность: " + service.getDuration() + " мин\n" +
                        "Максимум клиентов: " + service.getMaxClients() + "\n" +
                        "Доступные места: " + service.getAvailableSeats() + "\n\n";

        // Добавляем описание, если оно есть
        if (service.getDescription() != null && !service.getDescription().isEmpty()) {
            details += "Описание:\n" + service.getDescription();
        }

        builder.setMessage(details);
        builder.setPositiveButton("OK", null);

        // Добавляем кнопку "Записаться", если есть свободные места
        if (service.getAvailableSeats() > 0) {
            builder.setNeutralButton("Записаться", (dialog, which) -> {
                // Открываем активити для записи
                openRegistrationActivity(service);
            });
        }

        builder.show();
    }

    private void openRegistrationActivity(Service service) {
        Toast.makeText(MainActivity.this,
                "Запись на " + service.getName(),
                Toast.LENGTH_SHORT).show();

        // Открываем активити для записи на услугу
        Intent intent = new Intent(this, RegistrationActivity.class);
        intent.putExtra("service_id", service.getId());
        intent.putExtra("service_name", service.getName());
        intent.putExtra("service_data", service.getData());
        intent.putExtra("service_time", service.getTime());
        intent.putExtra("service_trainer", service.getTrainerName());
        intent.putExtra("service_price", service.getPrice());
        intent.putExtra("service_available_seats", service.getAvailableSeats());
        startActivity(intent);
    }

    public void showAdminLoginDialog() {
        try {
            AdminLoginDialogFragment dialog = new AdminLoginDialogFragment();
            dialog.setLoginDialogListener(this);
            dialog.show(getSupportFragmentManager(), "AdminLoginDialog");
        } catch (Exception e) {
            Toast.makeText(this, "Ошибка при открытии диалога: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    @Override
    public void onLoginSuccess() {
        Toast.makeText(this, "Доступ разрешён", Toast.LENGTH_SHORT).show();
        // После успешной авторизации можно открыть панель администратора
        Intent intent = new Intent(this,MainActivity2.class);
        startActivity(intent);
    }

    @Override
    public void onLoginFailure() {
        Toast.makeText(this, "Доступ запрещён", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Обновляем данные при возвращении на активити
        loadServicesFromFirestore();
    }
    public void OnClickRegistration(View view){
        Intent intent = new Intent(MainActivity.this, RegistrationActivity.class);
        startActivity(intent);
    }
}