package com.example.pr;

import java.util.HashMap;
import java.util.Map;

public class Admin {
    private String login;
    private String password;

    // 1. ОБЯЗАТЕЛЬНО: пустой конструктор для Firestore
    public Admin() {}

    public Admin(String login, String password) {
        this.login = login;
        this.password = password;
    }

    // 2. Стандартные геттеры и сеттеры
    // Firestore сопоставит их с полями "login" и "password" автоматически
    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("login", login);
        result.put("password", password);
        return result;
    }
}