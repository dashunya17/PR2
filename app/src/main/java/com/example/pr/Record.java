package com.example.pr;

import java.util.Date;

public class Record {
    private String id;
    private String IdService;
    private String surname;
    private String name;
    private String phone;
    private int numberClients;
    private String registrationDate;
    private String serviceName;
    private String serviceDate;
    private String serviceTime;
    private String serviceTrainer;
    private double totalPrice;
    private String status;

    public Record() {
        // Пустой конструктор для Firestore
    }

    public Record(String IdService, String surname, String name, String phone,
                  int numberClients, String serviceName, String serviceDate,
                  String serviceTime, String serviceTrainer, double servicePrice) {
        this.IdService = IdService;
        this.surname = surname;
        this.name = name;
        this.phone = phone;
        this.numberClients = numberClients;
        this.serviceName = serviceName;
        this.serviceDate = serviceDate;
        this.serviceTime = serviceTime;
        this.serviceTrainer = serviceTrainer;
        this.totalPrice = servicePrice * numberClients;
        this.registrationDate = new Date().toString();
        this.status = "pending";
    }

    // Геттеры и сеттеры
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getIdService() { return IdService; }
    public void setIdService(String IdService) { this.IdService = IdService; }

    public String getSurname() { return surname; }
    public void setSurname(String surname) { this.surname = surname; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public int getNumberClients() { return numberClients; }
    public void setNumberClients(int numberClients) { this.numberClients = numberClients; }

    public String getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(String registrationDate) { this.registrationDate = registrationDate; }

    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    public String getServiceDate() { return serviceDate; }
    public void setServiceDate(String serviceDate) { this.serviceDate = serviceDate; }

    public String getServiceTime() { return serviceTime; }
    public void setServiceTime(String serviceTime) { this.serviceTime = serviceTime; }

    public String getServiceTrainer() { return serviceTrainer; }
    public void setServiceTrainer(String serviceTrainer) { this.serviceTrainer = serviceTrainer; }

    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}