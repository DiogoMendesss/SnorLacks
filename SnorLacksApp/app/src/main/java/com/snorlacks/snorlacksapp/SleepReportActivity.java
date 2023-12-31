package com.snorlacks.snorlacksapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationBarView;
import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class SleepReportActivity extends AppCompatActivity {

    GraphView graphView;
    TextView textViewApneaEvents;
    TextView textViewSleepStamps;
    TextView textViewSleepQuality;
    ImageView imageViewSleepQuality;
    ConstraintLayout constraintLayout;

    SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM d, ''yy");
    SimpleDateFormat timeStampFormat = new SimpleDateFormat("h:mm a");

    // Create a list to store DataPoint objects
    ArrayList<DataPoint> bpmDataPoints = new ArrayList<>();
    ArrayList<Calendar> timeStamps = new ArrayList<Calendar>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sleep_report);

        constraintLayout = findViewById(R.id.constraintLayout);
        textViewApneaEvents = findViewById(R.id.txtApneaEvents);
        graphView = findViewById(R.id.idGraphView);
        textViewApneaEvents = findViewById(R.id.txtApneaEvents);
        textViewSleepStamps = findViewById((R.id.txtSleepStamps));
        textViewSleepQuality = findViewById((R.id.txtSleepQuality));
        imageViewSleepQuality = findViewById((R.id.imgSleepQuality));


        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //ArrayList<Double> bpmList = (ArrayList<Double>) getIntent().getSerializableExtra("bpmList");
        ArrayList<Double> bpmList = null;
        ArrayList<Boolean> apneaEvents = (ArrayList<Boolean>) getIntent().getSerializableExtra("apneaEvents");
        LocalDate clickedDate = (LocalDate) getIntent().getSerializableExtra("clickedDate");

        // Convert LocalDate to Date
        Date date = Date.from(clickedDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        // Convert Date to Calendar
        Calendar startDate = Calendar.getInstance();
        startDate.setTime(date);

        int numberApneaEvents = 0;
        Boolean currentEvent = false;
        for (boolean event : apneaEvents) {
            if (event & !currentEvent) {
                numberApneaEvents++;
                currentEvent = true;
            } else if (!event & currentEvent)
                currentEvent = false;
        }


        //populate timeStamps array list, assuming that the datapoints are distanced 1 minute
        for (int i = 0; i < bpmList.size(); i++) {
            Calendar minIncrement = (Calendar) startDate.clone();
            minIncrement.add(Calendar.MINUTE,i);
            timeStamps.add(minIncrement);
        }

        for (int i = 0; i < bpmList.size(); i++) {
            Date xValue = timeStamps.get(i).getTime(); // Convert Calendar to millis
            double yValue = bpmList.get(i);
            bpmDataPoints.add(new DataPoint(xValue, yValue));
        }


        // on below line we are adding data to our graph view.
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(bpmDataPoints.toArray(new DataPoint[0]));

        // after adding data to our line graph series.
        // on below line we are setting
        // title for our graph view.
        String formattedDate = clickedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        graphView.setTitle("Sleep Report from " + formattedDate);

        // on below line we are setting
        // text color to our graph view.
        //graphView.setTitleColor(R.color.purple_200);

        // on below line we are setting
        // our title text size.
        graphView.setTitleTextSize(40);

        // on below line we are adding
        // data series to our graph view.
        graphView.addSeries(series);

        // Customize graph properties if needed
        graphView.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(this)); // Format X-axis as date
        graphView.getGridLabelRenderer().setNumHorizontalLabels(timeStamps.size()/60); // Adjust the number of horizontal labels

        // Adjustments for overlapping labels
        graphView.getGridLabelRenderer().setTextSize(24f); // Set text size
        graphView.getGridLabelRenderer().setHumanRounding(false); // Disable rounding of labels
        graphView.getGridLabelRenderer().setHorizontalLabelsAngle(45); // Rotate labels for better visibility


        // Refresh graph
        graphView.invalidate();

        textViewApneaEvents.setText("No. suspected Apnea Events: " + numberApneaEvents);

        //textViewSleepStamps.setText("Sleep started at " + timeStampFormat.format(startDate.getTime()) + " and ended at: " + timeStampFormat.format(endDate.getTime()));

        if (numberApneaEvents==0){
            textViewSleepQuality.setText("Looks like you had a night well rested!!");
            imageViewSleepQuality.setImageResource(R.drawable.drowsy_doodle);
            constraintLayout.setBackgroundResource(R.drawable.good_sleep_report_background);
            getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.color.goodSleepBar));
            textViewApneaEvents.setTextColor(getResources().getColor(R.color.goodSleepText));
            textViewSleepQuality.setTextColor(getResources().getColor(R.color.goodSleepText));
            series.setColor(getResources().getColor(R.color.goodSleepText));
        }
        else {
            textViewSleepQuality.setText("It seems you had a rough nigh...");
            imageViewSleepQuality.setImageResource(R.drawable.confuse_doodle);
            constraintLayout.setBackgroundResource(R.drawable.bad_sleep_report_background);
            getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.color.badSleepBar));
            textViewApneaEvents.setTextColor(getResources().getColor(R.color.badSleepText));
            textViewSleepQuality.setTextColor(getResources().getColor(R.color.badSleepText));
            series.setColor(getResources().getColor(R.color.badSleepText));

        }


    }

    // Custom Date formatter for the x-axis
    private static class DateAsXAxisLabelFormatter extends DefaultLabelFormatter {
        private final SimpleDateFormat dateFormat;
        private final Context context;

        // Constructor with Context argument
        public DateAsXAxisLabelFormatter(Context context) {
            this.context = context;
            this.dateFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
        }

        @Override
        public String formatLabel(double value, boolean isValueX) {
            if (isValueX) {
                // Convert Date to formatted date string
                return dateFormat.format(new Date((long) value));
            } else {
                return super.formatLabel(value, isValueX);
            }
        }
    }
}

