package com.snorlacks.snorlacksapp;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Night {
    //private int id;
    private String start_date;
    private String start_time;
    private String end_time;
    private String sleep_time;
    private int apneaEventsNumber;
    private int id;


    public Night() {
        this.start_date = null;
        this.start_time = null;
        this.end_time = null;
        this.sleep_time = null;
    }

    public Night(String start_date, String start_time, String end_time, int apneaEventsNumber) {
        this.start_date = start_date;
        this.start_time = start_time;
        this.end_time = end_time;
        sleep_time = calculateSleepTime();
        this.apneaEventsNumber = apneaEventsNumber;
    }

    public Night(String start_date, String start_time, String end_time) {
        this.start_date = start_date;
        this.start_time = start_time;
        this.end_time = end_time;
        sleep_time = calculateSleepTime();
        apneaEventsNumber = 0;
    }

    public void reset () {
        //this.id = -1;
        this.start_date = null;
        this.start_time = null;
        this.end_time = null;
        this.sleep_time = null;
    }


    public String getStart_date() {
        return start_date;
    }

    public void setStart_date(String start_date) {
        this.start_date = start_date;
    }
    public String getStart_time() {
        return start_time;
    }

    public void setStart_time(String start_time) {
        this.start_time = start_time;
    }

    public String getEnd_time() {
        return end_time;
    }

    public void setEnd_time(String end_time) {
        this.end_time = end_time;
    }

    public String getSleep_time() {
        return sleep_time;
    }

    public void setSleep_time(String sleep_time) {
        this.sleep_time = sleep_time;
    }

    public int getApneaEventsNumber() {
        return apneaEventsNumber;
    }

    public void setApneaEventsNumber(int apneaEventsNumber) {
        this.apneaEventsNumber = apneaEventsNumber;
    }

    public String calculateSleepTime() {
        SimpleDateFormat sleep_time_format = new SimpleDateFormat("HH:mm");

        try {
            Date startTime = sleep_time_format.parse(start_time);
            Date endTime = sleep_time_format.parse(end_time);

            if (endTime.before(startTime)) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(endTime);
                calendar.add(Calendar.DAY_OF_MONTH, 1); // Add one day to the end time
                endTime = calendar.getTime();
            }

            long diffMillis = endTime.getTime() - startTime.getTime();
            long diffMinutes = diffMillis / (60 * 1000);

            // Calculate hours and minutes
            long hours = diffMinutes / 60;
            long minutes = diffMinutes % 60;

            // Format the result as "hh:mm"
            return String.format("%02d:%02d", hours, minutes);
        } catch (ParseException e) {
            e.printStackTrace();
            return "error calculating time";
        }
    }
}
