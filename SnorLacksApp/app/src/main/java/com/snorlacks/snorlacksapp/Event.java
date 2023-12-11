package com.snorlacks.snorlacksapp;

public class Event {
    private int id;
    private String date;
    private String type;
    private int night;

    public Event(int id, String date, String type, int night) {
        this.id = id;
        this.date = date;
        this.type = type;
        this.night = night;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getNight() {
        return night;
    }

    public void setNight(int night) {
        this.night = night;
    }
}
