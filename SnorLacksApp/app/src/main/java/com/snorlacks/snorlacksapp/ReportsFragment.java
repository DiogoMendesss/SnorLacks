package com.snorlacks.snorlacksapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class ReportsFragment extends Fragment implements CalendarAdapter.OnItemListener {

    private Context fragmentContext;

    private OnStartSleepReportListener onStartSleepReportListener;

    private TextView textView_monthYear;
    private RecyclerView recyclerView_calendar;
    private LocalDate selectedDate;
    private DBHandler dbHandler;


    public ReportsFragment() {
        // Required empty public constructor
    }
    public static ReportsFragment newInstance(String param1, String param2) {
        return new ReportsFragment();
    }
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        fragmentContext = context;

        // Check if the hosting activity implements OnStartSleepReportListener
        if (context instanceof OnStartSleepReportListener) {
            onStartSleepReportListener = (OnStartSleepReportListener) context;
        } else {
            throw new ClassCastException(context.toString() + " must implement OnStartSleepReportListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dbHandler = DBHandler.getInstance(fragmentContext);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reports, container, false);

        Button button_PreviousMonth = view.findViewById(R.id.button_previousMonth);
        Button button_NextMonth = view.findViewById(R.id.button_nextMonth);

        button_PreviousMonth.setOnClickListener(this::action_previousMonth);
        button_NextMonth.setOnClickListener(this::action_nextMonth);

        recyclerView_calendar= view.findViewById(R.id.calendarRecyclerView);
        textView_monthYear = view.findViewById(R.id.textView_monthYear);
        selectedDate = LocalDate.now();
        setMonthView();

        // Inflate the layout for this fragment
        return view;
    }

    private void setMonthView()
    {
        textView_monthYear.setText(monthYear_fromDate(selectedDate));
        ArrayList<LocalDate> daysInMonth_localDate = daysInMonth_LocalDateArray(selectedDate);

        CalendarAdapter calendarAdapter = new CalendarAdapter(daysInMonth_localDate, this, dbHandler );
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(fragmentContext, 7);
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
        if (!dayText.equals("")) {
            // Get the day of the month as an integer
            int dayOfMonth = Integer.parseInt(dayText);

            // Create a new LocalDate with the year and month of selectedDate and the specified day
            LocalDate clickedDate = selectedDate.withDayOfMonth(dayOfMonth);

            // Format the selectedDateWithDay in the desired format "yyyy-MM-dd"
            String formattedDate = clickedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            int num_apneaEvents = dbHandler.getApneaEventsForNight(formattedDate);
            String message;
            if (num_apneaEvents<0){
                message = "Selected Date " + formattedDate;
            }
            else {
                message = "Apnea Events from date " + formattedDate + ": " + num_apneaEvents;
                onStartSleepReportListener.onStartSleepReport(clickedDate);
            }
            Toast.makeText(fragmentContext, message, Toast.LENGTH_LONG).show();
        }
    }

    public interface OnStartSleepReportListener {
        void onStartSleepReport(LocalDate clickedDate);
    }
}