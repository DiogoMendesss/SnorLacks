package com.snorlacks.snorlacksapp;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Night {
    private int id;
    private String start_date;
    private String end_date;
    private String sleep_time;

    public Night(int id, String start_date, String end_date) {
        this.id = id;
        this.start_date = start_date;
        this.end_date = end_date;
        calculateSleepTime();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getStart_date() {
        return start_date;
    }

    public void setStart_date(String start_date) {
        this.start_date = start_date;
    }

    public String getEnd_date() {
        return end_date;
    }

    public void setEnd_date(String end_date) {
        this.end_date = end_date;
    }

    public String getSleep_time() {
        return sleep_time;
    }

    public void setSleep_time(String sleep_time) {
        this.sleep_time = sleep_time;
    }

    private void calculateSleepTime() {
        SimpleDateFormat format = new SimpleDateFormat("h:mm a");

        try {
            Date startDate = format.parse(start_date);
            Date endDate = format.parse(end_date);

            long diffMillis = endDate.getTime() - startDate.getTime();
            long diffMinutes = diffMillis / (60 * 1000);

            // Calculate hours and minutes
            long hours = diffMinutes / 60;
            long minutes = diffMinutes % 60;

            // Format the result as "hh:mm"
            sleep_time = String.format("%02d:%02d", hours, minutes);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
