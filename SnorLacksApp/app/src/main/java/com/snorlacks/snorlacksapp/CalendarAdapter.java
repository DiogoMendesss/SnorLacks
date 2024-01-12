package com.snorlacks.snorlacksapp;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarViewHolder> {

    private final ArrayList<LocalDate> daysOfMonth_LocalDate;
    private final OnItemListener onItemListener;
    private DBHandler dbHandler;
    public CalendarAdapter(ArrayList<LocalDate> daysOfMonth_LocalDate, OnItemListener onItemListener, DBHandler dbHandler) {
        this.dbHandler = dbHandler;
        if (dbHandler == null)
            Log.e("CalendarAdapter", "dbHandler is null");
        else
            Log.e("CalendarAdapter", "dbHandler is not null");

        // if the first 7 days are null the values are removed to prevent an empty line at the start
        if (sevenLeadingNulls(daysOfMonth_LocalDate))
            this.daysOfMonth_LocalDate = removeLeadingNulls(daysOfMonth_LocalDate);
        else {
            this.daysOfMonth_LocalDate = daysOfMonth_LocalDate;
        }
        this.onItemListener = onItemListener;

    }

    @NonNull
    @Override
    public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.calendar_cell,parent,false);
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = 170;
        return new CalendarViewHolder(onItemListener, view);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position)
    {
        int num_apneaEvents = -1;
        String dayText;


        LocalDate thisDay = daysOfMonth_LocalDate.get(position);
        Log.d("position", String.valueOf(position));
        Log.d("posDate", daysOfMonth_LocalDate != null ? daysOfMonth_LocalDate.toString() : "null");
        if (thisDay != null) {
            dayText = String.valueOf(thisDay.getDayOfMonth());
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String formattedDate = thisDay.format(formatter);
            num_apneaEvents = dbHandler.getApneaEventsForNight(formattedDate);
        }
        else
            dayText = "";

        //holder.indicator_circle.setImageResource(R.drawable.good_indicator_circle);
        if (dayText.equals(""))
            holder.indicator_circle.setVisibility(View.GONE);
        else if (num_apneaEvents==0) {
            holder.indicator_circle.setImageResource(R.drawable.calendar_cell_good_circle);
        } else if (num_apneaEvents>0) {
            holder.indicator_circle.setImageResource(R.drawable.calendar_cell_bad_circle);
        }
        holder.dayOfMonth.setText(dayText);
    }

    @Override
    public int getItemCount()
    {
        return daysOfMonth_LocalDate.size();
    }

    public interface OnItemListener
    {
        void onItemClick(int position, String dayText);
    }

    private boolean sevenLeadingNulls(ArrayList<LocalDate> list) {
        ArrayList<LocalDate> result = new ArrayList<>();
        boolean sevenLeadingNulls = false;
        int leadingNullCount = 0;

        for (LocalDate date : list) {
            if (date == null) {
                leadingNullCount++;
            } else {
                break;
            }
        }
        if (leadingNullCount == 7) {
            sevenLeadingNulls = true;
        }
        return sevenLeadingNulls;
    }
    private ArrayList<LocalDate> removeLeadingNulls(ArrayList<LocalDate> list) {
        ArrayList<LocalDate> result = new ArrayList<>();
        boolean foundFirstNonNull = false;

        for (LocalDate date : list) {
            if (date != null) {
                foundFirstNonNull = true;
            }

            if (foundFirstNonNull) {
                result.add(date);
            }
        }

        return result;
    }
}
