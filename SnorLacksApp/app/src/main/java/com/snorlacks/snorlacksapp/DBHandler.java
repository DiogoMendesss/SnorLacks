package com.snorlacks.snorlacksapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class DBHandler extends SQLiteOpenHelper {


    private Context context;
    // creating a constant variables for our database.
    // below variable is for our database name.
    private static final String DB_NAME = "SleepReportsDB.db";

    // below int is our database version
    private static final int DB_VERSION = 1;

    // below variable is for night table name.
    private static final String NIGHT_TABLE_NAME = "Night";

    // below variable is for night id column.
    private static final String NIGHT_ID_COL = "id";

    // below variable is for night start date column
    private static final String NIGHT_START_DATE_COL = "start_date";

    // below variable is for night end date column.
    private static final String NIGHT_END_DATE_COL = "end_date";

    // below variable is for sleep_time column.
    private static final String SLEEP_TIME_COL = "sleep_time";

    // below variable is for number of apnea events column.
    private static final String APNEA_EVENTS_COL = "apnea_events";

    // below variable is for event table name.
    private static final String EVENT_TABLE_NAME = "Event";

    // below variable is for event id column.
    private static final String EVENT_ID_COL = "id";

    // below variable is for event bpm column.
    private static final String EVENT_BPM_COL = "bpm";

    // below variable is for event date column.
    private static final String EVENT_DATE_COL = "date";

    // below variable is for event type column.
    private static final String EVENT_TYPE_COL = "type";

    // below variable is for event night column.
    private static final String EVENT_NIGHT_COL = "night";

    private SimpleDateFormat fullDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");


    // creating a constructor for our database handler.
    public DBHandler(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context;
    }

    // below method is for creating a database by running a sqlite query
    @Override
    public void onCreate(SQLiteDatabase db) {
        // on below line we are creating
        // an sqlite query and we are
        // setting our column names
        // along with their data types.
        String create_night_table = "CREATE TABLE " + NIGHT_TABLE_NAME + " ("
                + NIGHT_ID_COL + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + NIGHT_START_DATE_COL + " TEXT,"
                + NIGHT_END_DATE_COL + " TEXT,"
                + SLEEP_TIME_COL + " TEXT,"
                + APNEA_EVENTS_COL + " INTEGER);";

        String create_event_table = "CREATE TABLE " + EVENT_TABLE_NAME + " ("
                + EVENT_ID_COL + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + EVENT_BPM_COL + " REAL,"
                + EVENT_DATE_COL + " TEXT,"
                + EVENT_TYPE_COL + " TEXT,"
                + EVENT_NIGHT_COL + " TEXT REFERENCES " + NIGHT_TABLE_NAME + "(" + NIGHT_START_DATE_COL + ")" +  ");";

        // at last we are calling a exec sql
        // method to execute above sql query
        db.execSQL(create_night_table);
        db.execSQL(create_event_table);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + EVENT_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + NIGHT_TABLE_NAME);

        onCreate(db);
    }

    public void addNight(Night night){

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(NIGHT_START_DATE_COL, night.getStart_date());
        cv.put(NIGHT_END_DATE_COL, night.getEnd_date());
        cv.put(SLEEP_TIME_COL, night.getSleep_time());
        cv.put(APNEA_EVENTS_COL, night.getApneaEventsNumber());

        long result = db.insert(NIGHT_TABLE_NAME, null, cv);
        if(result==-1){
            Toast.makeText(context, "NIGHT insert failed", Toast.LENGTH_SHORT).show();
        }else Toast.makeText(context, "NIGHT insert succeeded", Toast.LENGTH_SHORT).show();
    }

    public void addEvent(Event event){

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(EVENT_BPM_COL, event.getBpm());
        cv.put(EVENT_DATE_COL, event.getDate());
        cv.put(EVENT_TYPE_COL, event.getType());
        cv.put(EVENT_NIGHT_COL, event.getNight());

        long result = db.insert(EVENT_TABLE_NAME, null, cv);
        if(result==-1){
            Toast.makeText(context, "EVENT insert failed", Toast.LENGTH_SHORT).show();
        }else Toast.makeText(context, "EVENT insert succeeded", Toast.LENGTH_SHORT).show();
    }

    public int getLastNightID(){
        int lastNightID = -1;

        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + NIGHT_ID_COL + " FROM " + NIGHT_TABLE_NAME +
                " ORDER BY " + NIGHT_ID_COL + " DESC LIMIT 1";

        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndex(NIGHT_ID_COL);

            // Check if the column exists in the result set
            if (columnIndex != -1) {
                lastNightID = cursor.getInt(columnIndex);
            } else {
                // Handle the case where the column is not found
                Log.e("Last Night ID", "Column not found: " + NIGHT_ID_COL);
                lastNightID = -1;
            }
        }

        cursor.close();
        return lastNightID;
    }

    public int getApneaEventsForNight(String startDateTime) {
        int apneaEvents = -1;

        // Check if the input string adheres to the expected format
        try {
            Date parsedDate = fullDateFormat.parse(startDateTime);
        } catch (ParseException e) {
            Log.e("Date Format", "Invalid date format: " + startDateTime);
            return apneaEvents; // or throw an exception, depending on your requirements
        }

        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + APNEA_EVENTS_COL + " FROM " + NIGHT_TABLE_NAME +
                " WHERE " + NIGHT_START_DATE_COL + " = ?";

        Cursor cursor = db.rawQuery(query, new String[]{startDateTime});

        if (cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndex(APNEA_EVENTS_COL);

            // Check if the column exists in the result set
            if (columnIndex != -1) {
                apneaEvents = cursor.getInt(columnIndex);
            } else {
                // Handle the case where the column is not found
                Log.e("Apnea Events", "Column not found: " + APNEA_EVENTS_COL);
                apneaEvents = -1;
            }
        }

        cursor.close();
        return apneaEvents;
    }

    public ArrayList<Double> getBpmValuesForNight(String startDateTime) {
        ArrayList<Double> bpmValues = new ArrayList<>();

        // Check if the input string adheres to the expected format
        try {
            Date parsedDate = fullDateFormat.parse(startDateTime);
        } catch (ParseException e) {
            Log.e("Date Format", "Invalid date format: " + startDateTime);
            return bpmValues; // or throw an exception, depending on your requirements
        }

        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + EVENT_BPM_COL + " FROM " + EVENT_TABLE_NAME +
                " WHERE " + EVENT_NIGHT_COL + " = ?";

        Cursor cursor = db.rawQuery(query, new String[]{startDateTime});

        if (cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndex(EVENT_BPM_COL);

            // Check if the column exists in the result set
            if (columnIndex != -1) {
                do {
                    double bpm = cursor.getDouble(columnIndex);
                    bpmValues.add(bpm);
                } while (cursor.moveToNext());
            } else {
                // Handle the case where the column is not found
                Log.e("BPM Values", "Column not found: " + EVENT_BPM_COL);
            }
        }

        cursor.close();
        return bpmValues;
    }


}

