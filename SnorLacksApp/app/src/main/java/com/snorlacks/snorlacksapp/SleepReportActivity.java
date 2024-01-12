package com.snorlacks.snorlacksapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.navigation.NavigationBarView;
import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.PointsGraphSeries;

import java.text.ParseException;
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

    Toolbar toolbar;
    TextView textViewApneaEvents;
    TextView textViewSleepStamps;
    TextView textViewSleepQuality;
    ImageView imageViewSleepQuality;
    ConstraintLayout constraintLayout;
    LinearLayout linearLayout1;
    LinearLayout linearLayout2;
    TextView textViewFallAsleepTime;
    TextView textViewSleepTime;
    TextView textViewAwakeningTime;
    TextView textViewMinBPM;
    TextView textViewMedianBPM;
    TextView textViewMaxBPM;

    String nightDate;
    String nightStartTime;
    String nightEndTime;

    private DBHandler dbHandler;
    private static SimpleDateFormat justDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    int numberApneaEvents;

    ArrayList<Event> events;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sleep_report);

        dbHandler = DBHandler.getInstance(SleepReportActivity.this);

        constraintLayout = findViewById(R.id.constraintLayout);
        linearLayout1 = findViewById(R.id.sleepTimeLayout);
        linearLayout2 = findViewById(R.id.sleepBPMLayout);
        toolbar = findViewById(R.id.toolbar);
        textViewApneaEvents = findViewById(R.id.txtApneaEvents);
        graphView = findViewById(R.id.idGraphView);
        textViewApneaEvents = findViewById(R.id.txtApneaEvents);
        textViewSleepStamps = findViewById((R.id.txtSleepStamps));
        textViewSleepQuality = findViewById((R.id.txtSleepQuality));
        imageViewSleepQuality = findViewById((R.id.imgSleepQuality));

        textViewFallAsleepTime = findViewById(R.id.txtViewFallTime);
        textViewSleepTime = findViewById(R.id.txtViewSleepTime);
        textViewAwakeningTime = findViewById(R.id.txtViewAwakeningTime);
        textViewMinBPM = findViewById(R.id.txtViewMinHRValue);
        textViewMedianBPM = findViewById(R.id.txtViewMedianHRValue);
        textViewMaxBPM = findViewById(R.id.txtViewMaxHRValue);


        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        LocalDate clickedDate = (LocalDate) getIntent().getSerializableExtra("clickedDate");

        // Convert LocalDate to Date
        Date date = Date.from(clickedDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        nightDate = justDateFormat.format(date);

        events = dbHandler.getEventsForNight(nightDate);

        // Check if events list is empty
        if (events.isEmpty()) {
            // If events list is empty, show a message in the first TextView
            textViewApneaEvents.setText("No sleep events recorded for this night.");
            // Hide other UI components
            graphView.setVisibility(View.GONE);
            textViewSleepStamps.setVisibility(View.GONE);
            textViewSleepQuality.setVisibility(View.GONE);
            imageViewSleepQuality.setVisibility(View.GONE);
            linearLayout1.setVisibility(View.GONE);
            linearLayout2.setVisibility(View.GONE);
        } else {

            // Show other UI components
            graphView.setVisibility(View.VISIBLE);
            textViewSleepStamps.setVisibility(View.VISIBLE);
            textViewSleepQuality.setVisibility(View.VISIBLE);
            imageViewSleepQuality.setVisibility(View.VISIBLE);

            numberApneaEvents = dbHandler.getApneaEventsForNight(nightDate);

            nightStartTime = dbHandler.getNightStartTime(nightDate);
            // Convert Date to Calendar
            Calendar startTime = Calendar.getInstance();
            startTime.setTime(date);

            nightEndTime = dbHandler.getNightEndTime(nightDate);


            // Create series for each event type
            LineGraphSeries<DataPoint> fallingAsleepSeries = new LineGraphSeries<>();
            LineGraphSeries<DataPoint> normalSeries = new LineGraphSeries<>();
            PointsGraphSeries<DataPoint> apneaSeries = new PointsGraphSeries<>();
            LineGraphSeries<DataPoint> awakeningSeries = new LineGraphSeries<>();

            // Iterate through events and add data points to respective series
            int i =0;
            for (Event event : events) {

                DataPoint dataPoint = new DataPoint(i, event.getBpm());
                i++;

                // Add data points to respective series based on event type
                switch (event.getType()) {
                    case "Falling asleep":
                        fallingAsleepSeries.appendData(dataPoint, true, events.size());
                        normalSeries.appendData(dataPoint, true, events.size());
                        break;

                    case "normal":
                        normalSeries.appendData(dataPoint, true, events.size());
                        break;

                    case "apnea":
                        apneaSeries.appendData(dataPoint, true, events.size());
                        normalSeries.appendData(dataPoint, true, events.size());
                        break;

                    case "Awakening":
                        awakeningSeries.appendData(dataPoint, true, events.size());
                        normalSeries.appendData(dataPoint, true, events.size());
                        break;
                }
            }


            fallingAsleepSeries.setColor(getResources().getColor(R.color.colorFallingAsleep));
            fallingAsleepSeries.setThickness(4);
            normalSeries.setColor(getResources().getColor(R.color.colorNormal));
            normalSeries.setThickness(4);
            apneaSeries.setColor(getResources().getColor(R.color.colorApnea));

            apneaSeries.setSize(15);
            apneaSeries.setShape(PointsGraphSeries.Shape.POINT); // Set the shape to POINT

            // Add each series to the graph
            awakeningSeries.setColor(getResources().getColor(R.color.colorAwakening));
            awakeningSeries.setThickness(4);

            // Add each series to the graph
            graphView.addSeries(normalSeries);
            graphView.addSeries(fallingAsleepSeries);
            graphView.addSeries(apneaSeries);
            graphView.addSeries(awakeningSeries);


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
            //graphView.addSeries(series);

            // Customize graph properties if needed
            //graphView.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(this)); // Format X-axis as date


            graphView.getViewport().setYAxisBoundsManual(true);
            graphView.getViewport().setMinX(normalSeries.getLowestValueX());
            graphView.getViewport().setMaxX(normalSeries.getHighestValueX());
            graphView.getViewport().setMinY(normalSeries.getLowestValueY()-10);
            graphView.getViewport().setMaxY(normalSeries.getHighestValueY()+20);

            // Adjust the number of horizontal labels to display the x-values between each n samples
            //int numLabels = Math.min(events.size(), 5); // Display labels for every n samples
            //int numLabels = events.size()/4;
            //graphView.getGridLabelRenderer().setNumHorizontalLabels(numLabels);



            // Show labels for each data point
            graphView.getGridLabelRenderer().setHumanRounding(false); // Disable rounding of labels
            graphView.getGridLabelRenderer().setHorizontalLabelsAngle(45); // Rotate labels for better visibility
            graphView.getGridLabelRenderer().setTextSize(30);

            // Make the line thicker
            //series.setThickness(8); // Adjust the thickness as needed

            // Refresh graph
            graphView.invalidate();

            graphView.getGridLabelRenderer().setHorizontalLabelsVisible(false);
            textViewApneaEvents.setText("Total Apnea Time: " + numberApneaEvents + " min");

            //textViewSleepStamps.setText("Sleep started at " + nightStartTime + " and ended at: " + nightEndTime);

            if (numberApneaEvents == 0) {
                textViewSleepQuality.setText("Feeling well rested?");
                imageViewSleepQuality.setImageResource(R.drawable.drowsy_doodle);
                constraintLayout.setBackgroundResource(R.drawable.good_sleep_report_background);
                toolbar.setBackgroundResource(R.drawable.good_sleep_report_background);
                getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.color.goodSleepBar));
                textViewApneaEvents.setTextColor(getResources().getColor(R.color.goodSleepText));
                textViewSleepQuality.setTextColor(getResources().getColor(R.color.goodSleepText));
            } else {
                textViewSleepQuality.setText("Had a rough night?");
                imageViewSleepQuality.setImageResource(R.drawable.confuse_doodle);
                constraintLayout.setBackgroundResource(R.drawable.bad_sleep_report_background);
                toolbar.setBackgroundResource(R.drawable.bad_sleep_report_background);
                getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.color.badSleepBar));
                textViewApneaEvents.setTextColor(getResources().getColor(R.color.badSleepText));
                textViewSleepQuality.setTextColor(getResources().getColor(R.color.badSleepText));
            }

            textViewFallAsleepTime.setText(getFallAsleepTime(events) + " min");
            textViewSleepTime.setText(getSleepTime(events) + "");
            textViewAwakeningTime.setText(getAwakeningTime(events) + " min");
            textViewMinBPM.setText(getMinBPM(events) + " bpm");
            textViewMedianBPM.setText((int) calculateEventBpmMedian(events) + " bpm");
            textViewMaxBPM.setText(getMaxBPM(events) + " bpm");

        }


    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
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

    // Method to get color based on event type
    private int getColorForEventType(String eventType) {
        switch (eventType) {
            case "Falling asleep":
                return getResources().getColor(R.color.colorFallingAsleep);
            case "Awakening":
                return getResources().getColor(R.color.colorAwakening);
            case "Normal":
                return getResources().getColor(R.color.colorNormal);
            case "Apnea":
                return getResources().getColor(R.color.colorApnea);
            default:
                return getResources().getColor(R.color.defaultColor);
        }
    }


    private int getFallAsleepTime (ArrayList<Event> events){
        int time = 0;
        for (Event event : events){
            if (event.getType().equals("Falling asleep")){
                time++;
            }
        }
        return time;
    }

    private String getSleepTime(ArrayList<Event> events) {
        int totalMinutes = 0;

        for (Event event : events) {
            if (event.getType().equals("normal") || event.getType().equals("apnea")) {
                totalMinutes++;
            }
        }

        return convertToHHMM(totalMinutes);
    }

    private String convertToHHMM(int totalMinutes) {
        int hours = totalMinutes / 60;
        int minutes = totalMinutes % 60;

        // Format the time as HH:mm
        return String.format("%02dh%02d", hours, minutes);
    }

    private int getAwakeningTime (ArrayList<Event> events){
        int time = 0;
        for (Event event : events){
            if (event.getType().equals("Awakening")){
                time++;
            }
        }
        return time;
    }

    private int getMinBPM (ArrayList<Event> events){
        int min = 1000;
        for (Event event : events){
            if (event.getBpm() < min){
                min = (int) event.getBpm();
            }
        }
        return min;
    }

    private int getMaxBPM (ArrayList<Event> events){
        int max = 0;
        for (Event event : events){
            if (event.getBpm() > max){
                max = (int) event.getBpm();
            }
        }
        return max;
    }

    public static double calculateEventBpmMedian(ArrayList<Event> events) {
        // Check for empty list
        if (events == null || events.isEmpty()) {
            throw new IllegalArgumentException("The list is empty");
        }

        // Sort the ArrayList
        ArrayList<Event> sortedEvents = new ArrayList<>(events);
        sortedEvents.sort((e1, e2) -> Double.compare(e1.getBpm(), e2.getBpm()));

        int size = sortedEvents.size();
        double median;

        if (size % 2 == 0) {
            // If the size is even, average the two middle elements
            double middle1 = sortedEvents.get(size / 2 - 1).getBpm();
            double middle2 = sortedEvents.get(size / 2).getBpm();
            median = (middle1 + middle2) / 2.0;
        } else {
            // If the size is odd, take the middle element
            median = sortedEvents.get(size / 2).getBpm();
        }

        return median;
    }
}

