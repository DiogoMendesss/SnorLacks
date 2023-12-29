package com.snorlacks.snorlacksapp;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class CalendarViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
private final CalendarAdapter.OnItemListener onItemListener;
    public final TextView dayOfMonth;
    public final ImageView indicator_circle;

    public CalendarViewHolder(CalendarAdapter.OnItemListener onItemListener, @NonNull View itemView) {
        super(itemView);
        this.onItemListener = onItemListener;
        dayOfMonth=itemView.findViewById(R.id.text_dayCell);
        indicator_circle = itemView.findViewById(R.id.bad_indicator_circle);
        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        onItemListener.onItemClick(getAdapterPosition(), (String) dayOfMonth.getText());
    }
}
