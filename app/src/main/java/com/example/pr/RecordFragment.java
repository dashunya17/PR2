package com.example.pr;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class RecordFragment extends Fragment {

    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmpty, tvSelectedDate, tvTotalRecords;
    private Spinner spinnerServices;
    private Button btnSelectDate;

    private Calendar calendar;
    private String selectedDate;
    private List<Service> servicesList;
    private ArrayAdapter<String> servicesAdapter;
    private RecordAdapter recordAdapter;
    private List<Record> recordList;
    private Service selectedService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_record, container, false);

        // Инициализация Firestore
        db = FirebaseFirestore.getInstance();
        calendar = Calendar.getInstance();
        servicesList = new ArrayList<>();
        recordList = new ArrayList<>();

        // Инициализация UI элементов
        initViews(view);

        // Устанавливаем сегодняшнюю дату по умолчанию
        setCurrentDate();

        // Загружаем услуги на выбранную дату
        loadServicesForDate(selectedDate);

        return view;
    }

    private void initViews(View view) {
        tvSelectedDate = view.findViewById(R.id.tvSelectedDate);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        tvTotalRecords = view.findViewById(R.id.tvTotalRecords);
        spinnerServices = view.findViewById(R.id.spinnerServices);
        btnSelectDate = view.findViewById(R.id.btnSelectDate);
        recyclerView = view.findViewById(R.id.recyclerView);
        progressBar = view.findViewById(R.id.progressBar);

        // Настройка RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recordAdapter = new RecordAdapter(recordList, new RecordAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                showRecordDetails(position);
            }

            @Override
            public void onDeleteClick(int position) {
                deleteRecord(position);
            }
        });
        recyclerView.setAdapter(recordAdapter);

        // Настройка адаптера для Spinner
        servicesAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, new ArrayList<String>());
        servicesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerServices.setAdapter(servicesAdapter);

        // Обработчик выбора услуги из Spinner
        spinnerServices.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0 && position - 1 < servicesList.size()) {
                    selectedService = servicesList.get(position - 1);
                    loadRecordsForService(selectedService.getId());
                } else {
                    selectedService = null;
                    recordList.clear();
                    recordAdapter.notifyDataSetChanged();
                    updateTotalRecords();
                    tvEmpty.setText("Выберите услугу для просмотра записей");
                    tvEmpty.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedService = null;
            }
        });

        // Обработчик кнопки выбора даты
        btnSelectDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });
    }

    private void setCurrentDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        selectedDate = dateFormat.format(calendar.getTime());
        tvSelectedDate.setText("Выбранная дата: " + selectedDate);
    }

    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
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

        datePickerDialog.show();
    }

    private void loadServicesForDate(String date) {
        progressBar.setVisibility(View.VISIBLE);
        servicesList.clear();
        servicesAdapter.clear();
        servicesAdapter.add("-- Все услуги --");
        selectedService = null;

        // Очищаем список записей
        recordList.clear();
        recordAdapter.notifyDataSetChanged();
        updateTotalRecords();
        tvEmpty.setVisibility(View.VISIBLE);
        tvEmpty.setText("Выберите услугу для просмотра записей");
        recyclerView.setVisibility(View.GONE);

        // Загружаем услуги на выбранную дату
        db.collection("service")
                .whereEqualTo("data", date)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        progressBar.setVisibility(View.GONE);

                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                try {
                                    // Получаем данные услуги
                                    String name = document.getString("name");
                                    String trainerName = document.getString("trainerName");
                                    String data = document.getString("data");
                                    String time = document.getString("time");

                                    // Создаем объект Service
                                    Service service = new Service();
                                    service.setId(document.getId());
                                    service.setName(name != null ? name : "Не указано");
                                    service.setTrainerName(trainerName != null ? trainerName : "Не указан");
                                    service.setData(data != null ? data : "");
                                    service.setTime(time != null ? time : "");

                                    servicesList.add(service);

                                    // Формируем строку для отображения в Spinner
                                    String serviceInfo = service.getName() +
                                            " (" + service.getTime() + ")";
                                    servicesAdapter.add(serviceInfo);

                                } catch (Exception e) {
                                    Toast.makeText(requireContext(),
                                            "Ошибка обработки услуги: " + e.getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                }
                            }

                            servicesAdapter.notifyDataSetChanged();

                            if (servicesList.isEmpty()) {
                                Toast.makeText(requireContext(),
                                        "На выбранную дату нет услуг",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                // Если есть услуги, выбираем первую по умолчанию
                                spinnerServices.setSelection(1);
                            }

                        } else {
                            Toast.makeText(requireContext(),
                                    "Ошибка загрузки услуг: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void loadRecordsForService(String serviceId) {
        progressBar.setVisibility(View.VISIBLE);
        recordList.clear();
        recordAdapter.notifyDataSetChanged();

        // Загружаем записи для выбранной услуги
        db.collection("record")
                .whereEqualTo("IdService", serviceId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        progressBar.setVisibility(View.GONE);

                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                try {
                                    // Получаем данные записи
                                    String surname = document.getString("surname");
                                    String name = document.getString("name");
                                    String phone = document.getString("phone");
                                    Long numberClients = document.getLong("numberClients");
                                    String registrationDate = document.getString("registrationDate");
                                    String serviceName = document.getString("serviceName");
                                    String serviceDate = document.getString("serviceDate");
                                    String serviceTime = document.getString("serviceTime");
                                    Double totalPrice = document.getDouble("totalPrice");

                                    // Создаем объект Record
                                    Record record = new Record();
                                    record.setId(document.getId());
                                    record.setIdService(serviceId);
                                    record.setSurname(surname != null ? surname : "");
                                    record.setName(name != null ? name : "");
                                    record.setPhone(phone != null ? phone : "");
                                    record.setNumberClients(numberClients != null ? numberClients.intValue() : 0);
                                    record.setRegistrationDate(registrationDate != null ? registrationDate : "");
                                    record.setServiceName(serviceName != null ? serviceName : "");
                                    record.setServiceDate(serviceDate != null ? serviceDate : "");
                                    record.setServiceTime(serviceTime != null ? serviceTime : "");
                                    record.setTotalPrice(totalPrice != null ? totalPrice : 0.0);

                                    recordList.add(record);

                                } catch (Exception e) {
                                    Toast.makeText(requireContext(),
                                            "Ошибка обработки записи",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }

                            recordAdapter.notifyDataSetChanged();
                            updateTotalRecords();

                            // Показываем сообщение, если список пуст
                            if (recordList.isEmpty()) {
                                tvEmpty.setText("Нет записей на эту услугу");
                                tvEmpty.setVisibility(View.VISIBLE);
                                recyclerView.setVisibility(View.GONE);
                            } else {
                                tvEmpty.setVisibility(View.GONE);
                                recyclerView.setVisibility(View.VISIBLE);
                            }

                        } else {
                            Toast.makeText(requireContext(),
                                    "Ошибка загрузки записей: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                            tvEmpty.setText("Ошибка загрузки записей");
                            tvEmpty.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                        }
                    }
                });
    }

    private void updateTotalRecords() {
        if (recordList.isEmpty()) {
            tvTotalRecords.setText("Всего записей: 0");
        } else {
            int totalClients = 0;
            double totalRevenue = 0.0;

            for (Record record : recordList) {
                totalClients += record.getNumberClients();
                totalRevenue += record.getTotalPrice();
            }

            String text = String.format(Locale.getDefault(),
                    "Всего записей: %d | Клиентов: %d | Сумма: %.2f руб",
                    recordList.size(), totalClients, totalRevenue);

            tvTotalRecords.setText(text);
        }
    }

    private void showRecordDetails(int position) {
        if (position >= recordList.size()) return;

        Record record = recordList.get(position);

        // Создаем диалог с деталями записи
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireContext());
        builder.setTitle("Детали записи #" + record.getId().substring(0, 8));

        String details =
                "Услуга: " + record.getServiceName() + "\n" +
                        "Дата услуги: " + record.getServiceDate() + "\n" +
                        "Время услуги: " + record.getServiceTime() + "\n" +
                        "ID услуги: " + record.getIdService() + "\n\n" +
                        "Клиент: " + record.getSurname() + " " + record.getName() + "\n" +
                        "Телефон: " + record.getPhone() + "\n" +
                        "Количество клиентов: " + record.getNumberClients() + "\n" +
                        "Сумма: " + record.getTotalPrice() + " руб\n" +
                        "Дата записи: " + record.getRegistrationDate();

        builder.setMessage(details);
        builder.setPositiveButton("OK", null);

        // Кнопка для копирования номера телефона
        builder.setNeutralButton("Копировать телефон", (dialog, which) -> {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager)
                    requireContext().getSystemService(android.content.Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText(
                    "Телефон", record.getPhone());
            clipboard.setPrimaryClip(clip);
            Toast.makeText(requireContext(), "Номер скопирован", Toast.LENGTH_SHORT).show();
        });

        // Кнопка для удаления записи (только для администратора)
        builder.setNegativeButton("Удалить", (dialog, which) -> {
            deleteRecordWithConfirmation(position);
        });

        builder.show();
    }

    private void deleteRecordWithConfirmation(int position) {
        if (position >= recordList.size()) return;

        Record record = recordList.get(position);

        new android.app.AlertDialog.Builder(requireContext())
                .setTitle("Удаление записи")
                .setMessage("Вы уверены, что хотите удалить запись клиента " +
                        record.getSurname() + " " + record.getName() + "?")
                .setPositiveButton("Удалить", (dialog, which) -> deleteRecord(position))
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void deleteRecord(int position) {
        if (position >= recordList.size()) return;

        Record record = recordList.get(position);
        String recordId = record.getId();

        progressBar.setVisibility(View.VISIBLE);

        // Удаляем запись из Firestore
        db.collection("record")
                .document(recordId)
                .delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        progressBar.setVisibility(View.GONE);

                        if (task.isSuccessful()) {
                            // Увеличиваем количество доступных мест в услуге
                            updateServiceAvailableSeats(record.getIdService(), record.getNumberClients());

                            // Удаляем из локального списка
                            recordList.remove(position);
                            recordAdapter.notifyItemRemoved(position);
                            updateTotalRecords();

                            Toast.makeText(requireContext(), "Запись удалена", Toast.LENGTH_SHORT).show();

                            // Обновляем отображение
                            if (recordList.isEmpty()) {
                                tvEmpty.setText("Нет записей на эту услугу");
                                tvEmpty.setVisibility(View.VISIBLE);
                                recyclerView.setVisibility(View.GONE);
                            }

                        } else {
                            Toast.makeText(requireContext(),
                                    "Ошибка удаления: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void updateServiceAvailableSeats(String serviceId, int numberClients) {
        // Получаем текущее количество мест
        db.collection("service")
                .document(serviceId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<com.google.firebase.firestore.DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<com.google.firebase.firestore.DocumentSnapshot> task) {
                        if (task.isSuccessful() && task.getResult().exists()) {
                            Long currentSeats = task.getResult().getLong("availableSeats");
                            if (currentSeats != null) {
                                int newAvailableSeats = currentSeats.intValue() + numberClients;

                                // Обновляем количество мест
                                db.collection("service")
                                        .document(serviceId)
                                        .update("availableSeats", newAvailableSeats)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(requireContext(),
                                                            "Количество мест обновлено",
                                                            Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            }
                        }
                    }
                });
    }
}