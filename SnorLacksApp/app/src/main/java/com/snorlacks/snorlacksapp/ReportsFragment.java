package com.snorlacks.snorlacksapp;

import android.content.Context;
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


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ReportsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ReportsFragment extends Fragment implements CalendarAdapter.OnItemListener {

    private Context fragmentContext;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";


    private TextView textView_monthYear;
    private RecyclerView recyclerView_calendar;
    private LocalDate selectedDate;
    private DBHandler dbHandler;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ReportsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ReportsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ReportsFragment newInstance(String param1, String param2) {
        ReportsFragment fragment = new ReportsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        fragmentContext = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        dbHandler = DBHandler.getInstance(fragmentContext);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reports, container, false);

        Button button_PreviousMonth = view.findViewById(R.id.button_previousMonth);
        Button button_NextMonth = view.findViewById(R.id.button_nextMonth);

        button_PreviousMonth.setOnClickListener(v -> action_previousMonth(v));
        button_NextMonth.setOnClickListener(v -> action_nextMonth(v));

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
        if(!dayText.equals(""))
        {
            String message = "Selected Date " + dayText + " " + monthYear_fromDate(selectedDate);
            Toast.makeText(fragmentContext, message, Toast.LENGTH_LONG).show();
        }
    }


}