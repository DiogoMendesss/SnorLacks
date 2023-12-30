package com.snorlacks.snorlacksapp;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class CalendarActivity extends AppCompatActivity implements CalendarAdapter.OnItemListener{

    private TextView textView_monthYear;
    private RecyclerView recyclerView_calendar;
    private LocalDate selectedDate;
    private DBHandler dbHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_reports);
        initWidgets();
        selectedDate = LocalDate.now();
        setMonthView();

        dbHandler = DBHandler.getInstance(CalendarActivity.this);
    }

    private void initWidgets()
    {
        recyclerView_calendar=findViewById(R.id.calendarRecyclerView);
        textView_monthYear = findViewById(R.id.textView_monthYear);
    }

    private void setMonthView()
    {
        textView_monthYear.setText(monthYear_fromDate(selectedDate));
        ArrayList<LocalDate> daysInMonth_localDate = daysInMonth_LocalDateArray(selectedDate);

        CalendarAdapter calendarAdapter = new CalendarAdapter(daysInMonth_localDate, this, dbHandler );
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), 7);
        recyclerView_calendar.setLayoutManager(layoutManager);
        recyclerView_calendar.setAdapter(calendarAdapter);

    }

    private ArrayList<String> daysInMonthArray(LocalDate date)
    {
        ArrayList<String> daysInMonthArray = new ArrayList<String>();
        YearMonth yearMonth = YearMonth.from(date);

        int daysInMonth = yearMonth.lengthOfMonth();

        LocalDate firstOfMonth = selectedDate.withDayOfMonth(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue();

        for (int i=1; i<=42; i++)
        {
            if (i <= dayOfWeek || i > daysInMonth + dayOfWeek) {
                daysInMonthArray.add("");
            } else {
                daysInMonthArray.add(String.valueOf(i - dayOfWeek));
            }
        }
        return daysInMonthArray;
    }

    private ArrayList<LocalDate> daysInMonth_LocalDateArray(LocalDate date)
    {
        ArrayList<LocalDate> daysInMonthArray = new ArrayList<LocalDate>();
        YearMonth yearMonth = YearMonth.from(date);

        int daysInMonth = yearMonth.lengthOfMonth();

        LocalDate firstOfMonth = selectedDate.withDayOfMonth(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue();

        for (int i=1; i<=42; i++)
        {
            if (i <= dayOfWeek || i > daysInMonth + dayOfWeek) {
                daysInMonthArray.add(null);
            } else {
                daysInMonthArray.add(selectedDate.withDayOfMonth(i-dayOfWeek));
            }
        }
        return daysInMonthArray;
    }

    private String monthYear_fromDate (LocalDate date)
    {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy");
        return date.format(formatter);
    }

    public void action_previousMonth(View view){
        selectedDate = selectedDate.minusMonths(1);
        setMonthView();
    }

    public void action_nextMonth(View view){
        selectedDate = selectedDate.plusMonths(1);
        setMonthView();
    }

    @Override
    public void onItemClick(int position, String dayText) {
        if(!dayText.equals(""))
        {
            String message = "Selected Date " + dayText + " " + monthYear_fromDate(selectedDate);
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
    }
}
