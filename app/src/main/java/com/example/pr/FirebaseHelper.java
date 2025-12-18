package com.example.pr;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.CollectionReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseHelper {

    // 1. ОБЯЗАТЕЛЬНО: экземпляр FirebaseFirestore
    private final FirebaseFirestore db;

    // 2. ОПЦИОНАЛЬНО: ссылки на часто используемые коллекции
    private final CollectionReference adminCollection;

    public FirebaseHelper() {
        // Инициализация Firestore
        db = FirebaseFirestore.getInstance();

        // Инициализация ссылки на коллекцию admin
        adminCollection = db.collection("admin");
    }

    // 3. МЕТОДЫ ДЛЯ РАБОТЫ С ДАННЫМИ

    public void checkAdminCredentials(String inputLogin, String inputPassword,
                                      OnCheckCredentialsListener listener) {
        // Создание запроса к коллекции admin
        adminCollection
                .whereEqualTo("login", inputLogin)
                .whereEqualTo("password", inputPassword)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        boolean isValid = querySnapshot != null && !querySnapshot.isEmpty();
                        listener.onCredentialsChecked(isValid);
                    } else {
                        listener.onError(task.getException().getMessage());
                    }
                });
    }

    // 4. МЕТОД ДЛЯ ДОБАВЛЕНИЯ ДОКУМЕНТА
    public void addAdmin(String login, String password) {
        // Создание объекта данных
        Map<String, Object> admin = new HashMap<>();
        admin.put("login", login);
        admin.put("password", password);

        // Добавление документа с автоматическим ID
        adminCollection.add(admin)
                .addOnSuccessListener(documentReference -> {
                    // Документ успешно добавлен
                    String docId = documentReference.getId();
                })
                .addOnFailureListener(e -> {
                    // Обработка ошибки
                });
    }

    // 5. МЕТОД ДЛЯ ПОЛУЧЕНИЯ ВСЕХ АДМИНИСТРАТОРОВ
    public void getAllAdmins(OnAdminsLoadedListener listener) {
        adminCollection.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Получение данных из документа
                            String login = document.getString("login");
                            String password = document.getString("password");
                            // ... обработка данных
                        }
                    }
                });
    }

    // 6. ИНТЕРФЕЙСЫ ОБРАТНОГО ВЫЗОВА
    public interface OnCheckCredentialsListener {
        void onCredentialsChecked(boolean isValid);
        void onError(String errorMessage);
    }

    public interface OnAdminsLoadedListener {
        void onAdminsLoaded(List<Admin> admins);
        void onError(String errorMessage);
    }
}