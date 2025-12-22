package com.example.pr;

public class Service {
    private String id;
    private String name;
    private String description;
    private String trainerName;
    private String data;
    private String time;
    private double price;
    private int duration;
    private int maxClients;
    private int availableSeats;

    public Service() {
    }

    public Service(String name, String description, String trainerName,
                   String data, String time, double price, int duration,
                   int maxClients, int availableSeats) {
        this.name = name;
        this.description = description;
        this.trainerName = trainerName;
        this.data = data;
        this.time = time;
        this.price = price;
        this.duration = duration;
        this.maxClients = maxClients;
        this.availableSeats = availableSeats;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getTrainerName() { return trainerName; }
    public void setTrainerName(String trainerName) { this.trainerName = trainerName; }

    public String getData() { return data; }
    public void setData(String data) { this.data = data; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }

    public int getMaxClients() { return maxClients; }
    public void setMaxClients(int maxClients) { this.maxClients = maxClients; }

    public int getAvailableSeats() { return availableSeats; }
    public void setAvailableSeats(int availableSeats) { this.availableSeats = availableSeats; }
}