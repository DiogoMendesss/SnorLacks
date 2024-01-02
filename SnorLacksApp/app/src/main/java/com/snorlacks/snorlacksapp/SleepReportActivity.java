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
import android.widget.TextView;
import android.widget.Toast;

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
    ArrayList<Double> bpmList;

    String nightDate;
    String nightStartTime;
    String nightEndTime;

    private DBHandler dbHandler;
    private SimpleDateFormat justDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private SimpleDateFormat nightTimeFormat = new SimpleDateFormat("h:mm a");
    int numberApneaEvents;

    ArrayList<Event> events;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sleep_report);

        dbHandler = DBHandler.getInstance(SleepReportActivity.this);

        constraintLayout = findViewById(R.id.constraintLayout);
        textViewApneaEvents = findViewById(R.id.txtApneaEvents);
        graphView = findViewById(R.id.idGraphView);
        textViewApneaEvents = findViewById(R.id.txtApneaEvents);
        textViewSleepStamps = findViewById((R.id.txtSleepStamps));
        textViewSleepQuality = findViewById((R.id.txtSleepQuality));
        imageViewSleepQuality = findViewById((R.id.imgSleepQuality));


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


            //LineGraphSeries<DataPoint> series = new LineGraphSeries<>();
/*
        int i = 0;
        while (events.get(i).getType() == "Falling asleep") {
            i++;
        }

        while (events.get(i).getType() == "normal") {
            i++;
        }

        while (events.get(i).getType() == "apnea") {
            i++;
        }

        while (events.get(i).getType() == "Awakening") {
            i++;
        }

 */


            // Create series for each event type
            LineGraphSeries<DataPoint> fallingAsleepSeries = new LineGraphSeries<>();
            LineGraphSeries<DataPoint> normalSeries = new LineGraphSeries<>();
            LineGraphSeries<DataPoint> apneaSeries = new LineGraphSeries<>();
            LineGraphSeries<DataPoint> awakeningSeries = new LineGraphSeries<>();

            // Iterate through events and add data points to respective series
            for (int i = 0; i < events.size(); i++) {
                Event event = events.get(i);
                double yValue = event.getBpm();
                int color;
                Date xValue = event.getDateAsDate(); // Use getDateAsDate() method
                DataPoint dataPoint = new DataPoint(xValue.getTime(), yValue);

                // Add data points to respective series based on event type
                switch (event.getType()) {
                    case "Falling asleep":
                        fallingAsleepSeries.appendData(dataPoint, true, events.size());
                        color = getColorForEventType(event.getType());
                        fallingAsleepSeries.setColor(color);
                        break;
                    case "normal":
                        normalSeries.appendData(dataPoint, true, events.size());
                        color = getColorForEventType(event.getType());
                        normalSeries.setColor(color);
                        break;
                    case "apnea":
                        normalSeries.appendData(dataPoint, true, events.size());
                        color = getColorForEventType(event.getType());
                        apneaSeries.setColor(color);
                        break;
                    case "Awakening":
                        awakeningSeries.appendData(dataPoint, true, events.size());
                        color = getColorForEventType(event.getType());
                        awakeningSeries.setColor(color);
                        break;
                }
            }



            // Add each series to the graph
            graphView.addSeries(fallingAsleepSeries);
            graphView.addSeries(normalSeries);
            graphView.addSeries(apneaSeries);
            graphView.addSeries(awakeningSeries);

            // on below line we are adding data to our graph view.
            //LineGraphSeries<DataPoint> series = new LineGraphSeries<>(bpmDataPoints.toArray(new DataPoint[0]));

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
            graphView.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(this)); // Format X-axis as date

            // Set custom bounds for the vertical axis to ensure all data points are visible
            double minY = findMinYValue(fallingAsleepSeries, normalSeries, apneaSeries, awakeningSeries);
            double maxY = findMaxYValue(fallingAsleepSeries, normalSeries, apneaSeries, awakeningSeries);
            graphView.getViewport().setYAxisBoundsManual(true);
            graphView.getViewport().setMinY(minY);
            graphView.getViewport().setMaxY(maxY);

            // Adjust the number of horizontal labels to display the x-values between each 5 samples
            //int numLabels = Math.min(events.size(), 5); // Display labels for every 5 samples
            int numLabels = events.size();
            graphView.getGridLabelRenderer().setNumHorizontalLabels(numLabels);


            // Show labels for each data point
            graphView.getGridLabelRenderer().setHumanRounding(false); // Disable rounding of labels
            graphView.getGridLabelRenderer().setHorizontalLabelsAngle(45); // Rotate labels for better visibility

            // Make the line thicker
            //series.setThickness(8); // Adjust the thickness as needed

            // Refresh graph
            graphView.invalidate();


            textViewApneaEvents.setText("No. suspected Apnea Events: " + numberApneaEvents);

            textViewSleepStamps.setText("Sleep started at " + nightStartTime + " and ended at: " + nightEndTime
            + "minY: " + minY + " maxY: " + maxY);

            if (numberApneaEvents == 0) {
                textViewSleepQuality.setText("Looks like you had a night well rested!!");
                imageViewSleepQuality.setImageResource(R.drawable.drowsy_doodle);
                constraintLayout.setBackgroundResource(R.drawable.good_sleep_report_background);
                getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.color.goodSleepBar));
                textViewApneaEvents.setTextColor(getResources().getColor(R.color.goodSleepText));
                textViewSleepQuality.setTextColor(getResources().getColor(R.color.goodSleepText));
                //series.setColor(getResources().getColor(R.color.goodSleepText));
            } else {
                textViewSleepQuality.setText("It seems you had a rough nigh...");
                imageViewSleepQuality.setImageResource(R.drawable.confuse_doodle);
                constraintLayout.setBackgroundResource(R.drawable.bad_sleep_report_background);
                getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.color.badSleepBar));
                textViewApneaEvents.setTextColor(getResources().getColor(R.color.badSleepText));
                textViewSleepQuality.setTextColor(getResources().getColor(R.color.badSleepText));
                //series.setColor(getResources().getColor(R.color.badSleepText));

            }
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

    // Method to get size based on event type
    private int getSizeForEventType(String eventType) {
        // Set a larger size for "Apnea" events, and a default size for others
        return eventType.equals("Apnea") ? 10 : 5;
    }

    // Method to find the minimum Y-value among all series
    private double findMinYValue(LineGraphSeries<DataPoint>... series) {
        double minY = Double.MAX_VALUE;
        int i = 0;
        for (LineGraphSeries<DataPoint> s : series) {
            if (!s.isEmpty()) {
                minY = Math.min(minY, s.getLowestValueY());
                Toast.makeText(this, "Min Y:(" + i + ")" + minY, Toast.LENGTH_SHORT).show();
                i++;
            }
        }
        return minY;
    }

    // Method to find the maximum Y-value among all series
    private double findMaxYValue(LineGraphSeries<DataPoint>... series) {
        double maxY = Double.MIN_VALUE;
        for (LineGraphSeries<DataPoint> s : series) {
            if (!s.isEmpty()) {
                maxY = Math.max(maxY, s.getHighestValueY());
            }
        }
        return maxY;
    }



}

