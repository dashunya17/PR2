package com.example.pr;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RegistrationActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private EditText etSurname, etName, etPhone, etNumberClients;
    private TextView tvSelectedDate, tvServiceInfo;
    private Spinner spinnerServices;
    private Button btnSelectDate, btnRegister;
    private ProgressBar progressBar;

    private Calendar calendar;
    private String selectedDate;
    private List<Service> servicesList;
    private ArrayAdapter<String> servicesAdapter;
    private Service selectedService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        // Инициализация Firestore
        db = FirebaseFirestore.getInstance();
        calendar = Calendar.getInstance();
        servicesList = new ArrayList<>();

        // Инициализация UI элементов
        initViews();

        // Устанавливаем сегодняшнюю дату по умолчанию
        setCurrentDate();

        // Загружаем услуги на выбранную дату
        loadServicesForDate(selectedDate);
    }

    private void initViews() {
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        tvServiceInfo = findViewById(R.id.tvServiceInfo);
        etSurname = findViewById(R.id.etSurname);
        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        etNumberClients = findViewById(R.id.etNumberClients);
        spinnerServices = findViewById(R.id.spinnerServices);
        btnSelectDate = findViewById(R.id.btnSelectDate);
        btnRegister = findViewById(R.id.btnRegister);
        progressBar = findViewById(R.id.progressBar);

        // Настройка адаптера для Spinner
        servicesAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, new ArrayList<String>());
        servicesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerServices.setAdapter(servicesAdapter);

        // Обработчик выбора услуги из Spinner
        spinnerServices.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0 && position - 1 < servicesList.size()) {
                    selectedService = servicesList.get(position - 1);
                    displaySelectedServiceInfo();
                } else {
                    selectedService = null;
                    tvServiceInfo.setText("Выберите услугу для просмотра деталей");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedService = null;
                tvServiceInfo.setText("Выберите услугу для просмотра деталей");
            }
        });

        // Обработчик кнопки выбора даты
        btnSelectDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        // Обработчик кнопки регистрации
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerForService();
            }
        });

        // Обработчик изменения количества клиентов
        etNumberClients.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    validateNumberClients();
                }
            }
        });
        setupAutoScrollForEditTexts();
    }

    private void setupAutoScrollForEditTexts() {
        // Для фамилии
        etSurname.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    // Прокручиваем к полю фамилии
                    scrollToView(etSurname);
                }
            }
        });

        // Для имени
        etName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    scrollToView(etName);
                }
            }
        });

        // Для телефона
        etPhone.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    scrollToView(etPhone);
                }
            }
        });

        // Для количества клиентов
        etNumberClients.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    scrollToView(etNumberClients);
                }
            }
        });
    }
    private void scrollToView(final View view) {
        // Находим ScrollView
        final ScrollView scrollView = findViewById(R.id.scrollView);

        if (scrollView != null) {
            // Используем postDelayed для гарантированного скролла после обновления UI
            scrollView.post(new Runnable() {
                @Override
                public void run() {
                    // Рассчитываем положение для скролла
                    int scrollY = calculateScrollPosition(view, scrollView);
                    scrollView.smoothScrollTo(0, scrollY);
                }
            });
        }
    }

    private int calculateScrollPosition(View view, ScrollView scrollView) {
        // Получаем координаты View относительно ScrollView
        int[] location = new int[2];
        view.getLocationOnScreen(location);

        // Получаем координаты ScrollView
        int[] scrollLocation = new int[2];
        scrollView.getLocationOnScreen(scrollLocation);

        // Вычисляем смещение
        int y = location[1] - scrollLocation[1];

        // Добавляем отступ, чтобы View не было в самом верху
        int offset = 100; // 100 пикселей отступа сверху

        // Проверяем, чтобы не выйти за границы
        int maxScroll = scrollView.getChildAt(0).getHeight() - scrollView.getHeight();
        return Math.min(Math.max(0, y - offset), maxScroll);
    }
    private void setCurrentDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        selectedDate = dateFormat.format(calendar.getTime());
        tvSelectedDate.setText("Выбранная дата: " + selectedDate);
    }

    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, month);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
                        selectedDate = dateFormat.format(calendar.getTime());
                        tvSelectedDate.setText("Выбранная дата: " + selectedDate);

                        // Загружаем услуги на новую дату
                        loadServicesForDate(selectedDate);
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        // Устанавливаем минимальную дату (сегодня)
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);

        datePickerDialog.show();
    }

    private void loadServicesForDate(String date) {
        progressBar.setVisibility(View.VISIBLE);
        servicesList.clear();
        servicesAdapter.clear();
        servicesAdapter.add("-- Выберите услугу --");
        selectedService = null;
        tvServiceInfo.setText("Загрузка услуг...");

        // Загружаем услуги на выбранную дату из таблицы service
        db.collection("service")
                .whereEqualTo("data", date) // Фильтр по полю "data"
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        progressBar.setVisibility(View.GONE);

                        if (task.isSuccessful()) {
                            int availableServicesCount = 0;

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

                                    servicesList.add(service);

                                    // Формируем строку для отображения в Spinner
                                    String serviceInfo = service.getName() +
                                            " (" + service.getTime() +
                                            ", " + service.getAvailableSeats() +
                                            " мест)";
                                    servicesAdapter.add(serviceInfo);

                                    availableServicesCount++;

                                } catch (Exception e) {
                                    Toast.makeText(RegistrationActivity.this,
                                            "Ошибка обработки услуги: " + e.getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                }
                            }

                            servicesAdapter.notifyDataSetChanged();

                            if (availableServicesCount == 0) {
                                Toast.makeText(RegistrationActivity.this,
                                        "На " + date + " нет доступных услуг",
                                        Toast.LENGTH_LONG).show();
                                tvServiceInfo.setText("На выбранную дату нет доступных услуг");
                            } else {
                                tvServiceInfo.setText("Выберите услугу для просмотра деталей");
                                Toast.makeText(RegistrationActivity.this,
                                        "Найдено " + availableServicesCount + " услуг",
                                        Toast.LENGTH_SHORT).show();
                            }

                        } else {
                            Toast.makeText(RegistrationActivity.this,
                                    "Ошибка загрузки услуг: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                            tvServiceInfo.setText("Ошибка загрузки услуг");
                        }
                    }
                });
    }

    private void displaySelectedServiceInfo() {
        if (selectedService == null) return;

        String info = "Услуга: " + selectedService.getName() + "\n" +
                "Тренер: " + selectedService.getTrainerName() + "\n" +
                "Дата: " + selectedService.getData() + "\n" +
                "Время: " + selectedService.getTime() + "\n" +
                "Цена: " + selectedService.getPrice() + " руб\n" +
                "Продолжительность: " + selectedService.getDuration() + " мин\n" +
                "Максимум клиентов: " + selectedService.getMaxClients() + "\n" +
                "Доступные места: " + selectedService.getAvailableSeats();

        if (selectedService.getDescription() != null && !selectedService.getDescription().isEmpty()) {
            info += "\n\nОписание:\n" + selectedService.getDescription();
        }

        tvServiceInfo.setText(info);
    }

    private void validateNumberClients() {
        if (selectedService == null) return;

        String numberClientsStr = etNumberClients.getText().toString().trim();
        if (!TextUtils.isEmpty(numberClientsStr)) {
            try {
                int numberClients = Integer.parseInt(numberClientsStr);

                if (numberClients <= 0) {
                    etNumberClients.setError("Количество должно быть больше 0");
                } else if (numberClients > selectedService.getAvailableSeats()) {
                    etNumberClients.setError("Максимум " + selectedService.getAvailableSeats() + " мест");
                } else {
                    etNumberClients.setError(null);
                }
            } catch (NumberFormatException e) {
                etNumberClients.setError("Введите число");
            }
        }
    }

    private void registerForService() {
        // Валидация выбора услуги
        if (selectedService == null) {
            Toast.makeText(this, "Выберите услугу из списка", Toast.LENGTH_SHORT).show();
            spinnerServices.requestFocus();
            return;
        }

        // Получаем данные из полей ввода
        String surname = etSurname.getText().toString().trim();
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String numberClientsStr = etNumberClients.getText().toString().trim();

        // Валидация полей
        if (TextUtils.isEmpty(surname)) {
            etSurname.setError("Введите фамилию");
            etSurname.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(name)) {
            etName.setError("Введите имя");
            etName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(phone)) {
            etPhone.setError("Введите телефон");
            etPhone.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(numberClientsStr)) {
            etNumberClients.setError("Введите количество клиентов");
            etNumberClients.requestFocus();
            return;
        }

        int numberClients;
        try {
            numberClients = Integer.parseInt(numberClientsStr);
        } catch (NumberFormatException e) {
            etNumberClients.setError("Введите корректное число");
            etNumberClients.requestFocus();
            return;
        }

        // Проверка доступных мест
        if (numberClients <= 0) {
            etNumberClients.setError("Количество клиентов должно быть больше 0");
            etNumberClients.requestFocus();
            return;
        }

        if (numberClients > selectedService.getAvailableSeats()) {
            etNumberClients.setError("Доступно только " + selectedService.getAvailableSeats() + " мест");
            etNumberClients.requestFocus();
            return;
        }

        // Проверка формата телефона
        if (!isValidPhone(phone)) {
            etPhone.setError("Введите корректный номер телефона");
            etPhone.requestFocus();
            return;
        }

        // Создание и сохранение записи
        saveRecordToFirestore(surname, name, phone, numberClients);
    }

    private boolean isValidPhone(String phone) {
        // Удаляем все нецифровые символы
        String phoneDigits = phone.replaceAll("[^0-9]", "");
        // Проверяем, что осталось минимум 10 цифр
        return phoneDigits.length() >= 10;
    }

    private void saveRecordToFirestore(String surname, String name, String phone, int numberClients) {
        // Показываем прогресс
        progressBar.setVisibility(View.VISIBLE);
        btnRegister.setEnabled(false);

        // Получаем текущую дату и время
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault());
        String registrationDateTime = sdf.format(new Date());

        // Создаем запись для таблицы record
        Map<String, Object> record = new HashMap<>();
        record.put("IdService", selectedService.getId());
        record.put("surname", surname);
        record.put("name", name);
        record.put("phone", phone);
        record.put("numberClients", numberClients);
        record.put("registrationDate", registrationDateTime);

        // Дополнительная информация об услуге (для удобства)
        record.put("serviceName", selectedService.getName());
        record.put("serviceDate", selectedService.getData());
        record.put("serviceTime", selectedService.getTime());
        record.put("serviceTrainer", selectedService.getTrainerName());
        record.put("servicePrice", selectedService.getPrice());
        record.put("totalPrice", selectedService.getPrice() * numberClients);

        // Сохраняем в коллекцию "record"
        db.collection("record")
                .add(record)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        String recordId = documentReference.getId();

                        // Обновляем количество доступных мест в таблице service
                        updateServiceAvailableSeats(numberClients, recordId);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressBar.setVisibility(View.GONE);
                        btnRegister.setEnabled(true);

                        Toast.makeText(RegistrationActivity.this,
                                "Ошибка при сохранении записи: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateServiceAvailableSeats(int numberClients, String recordId) {
        int newAvailableSeats = selectedService.getAvailableSeats() - numberClients;

        db.collection("service")
                .document(selectedService.getId())
                .update("availableSeats", newAvailableSeats)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        progressBar.setVisibility(View.GONE);
                        btnRegister.setEnabled(true);

                        if (task.isSuccessful()) {
                            // Показываем успешное сообщение
                            showSuccessMessage(recordId, numberClients);

                            // Обновляем список услуг на текущую дату
                            loadServicesForDate(selectedDate);

                            // Очищаем форму
                            clearForm();
                        } else {
                            Toast.makeText(RegistrationActivity.this,
                                    "Ошибка при обновлении мест: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void showSuccessMessage(String recordId, int numberClients) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("✅ Запись успешно оформлена!");

        String message = "Номер записи: " + recordId.substring(0, 8) + "\n\n" +
                "Услуга: " + selectedService.getName() + "\n" +
                "Дата: " + selectedService.getData() + "\n" +
                "Время: " + selectedService.getTime() + "\n" +
                "Тренер: " + selectedService.getTrainerName() + "\n" +
                "Клиентов: " + numberClients + "\n" +
                "Цена за одного: " + selectedService.getPrice() + " руб\n" +
                "Общая сумма: " + (selectedService.getPrice() * numberClients) + " руб\n\n" +
                "Клиент: " + etSurname.getText().toString() + " " +
                etName.getText().toString() + "\n" +
                "Телефон: " + etPhone.getText().toString();

        builder.setMessage(message);
        builder.setPositiveButton("OK", (dialog, which) -> {
            // Диалог закрывается автоматически
        });

        builder.show();
    }

    private void clearForm() {
        etSurname.setText("");
        etName.setText("");
        etPhone.setText("");
        etNumberClients.setText("");
        spinnerServices.setSelection(0);
        selectedService = null;
        tvServiceInfo.setText("Выберите услугу для просмотра деталей");
    }

    // Обработчик кнопки "Назад"
    public void onBackClick(View view) {
        finish();
    }
}