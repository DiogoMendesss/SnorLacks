package com.snorlacks.snorlacksapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

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

    // below variable is for event table name.
    private static final String EVENT_TABLE_NAME = "Event";

    // below variable is for event id column.
    private static final String EVENT_ID_COL = "id";

    // below variable is for event date column.
    private static final String EVENT_DATE_COL = "date";

    // below variable is for event type column.
    private static final String EVENT_TYPE_COL = "type";

    // below variable is for event night column.
    private static final String EVENT_NIGHT_COL = "night";

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
                + SLEEP_TIME_COL + " TEXT);";

        String create_event_table = "CREATE TABLE " + EVENT_TABLE_NAME + " ("
                + EVENT_ID_COL + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + EVENT_DATE_COL + " TEXT,"
                + EVENT_TYPE_COL + " TEXT,"
                + EVENT_NIGHT_COL + " TEXT);";

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

    public void addNight(String start_date, String end_date, String sleep_time){

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(NIGHT_START_DATE_COL, start_date);
        cv.put(NIGHT_END_DATE_COL, end_date);
        cv.put(SLEEP_TIME_COL, sleep_time);

        long result = db.insert(NIGHT_TABLE_NAME, null, cv);
        if(result==-1){
            Toast.makeText(context, "NIGHT insert failed", Toast.LENGTH_SHORT).show();
        }else Toast.makeText(context, "NIGHT insert succeeded", Toast.LENGTH_SHORT).show();
    }

    public void addEvent(String date, String type, int night){

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(EVENT_DATE_COL, date);
        cv.put(EVENT_TYPE_COL, type);
        cv.put(EVENT_NIGHT_COL, night);

        long result = db.insert(EVENT_TABLE_NAME, null, cv);
        if(result==-1){
            Toast.makeText(context, "EVENT insert failed", Toast.LENGTH_SHORT).show();
        }else Toast.makeText(context, "EVENT insert succeeded", Toast.LENGTH_SHORT).show();
    }
}

