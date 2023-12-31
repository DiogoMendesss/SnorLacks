package com.snorlacks.snorlacksapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;
import android.view.MenuItem;
import androidx.fragment.app.FragmentManager;



import com.google.android.material.bottomnavigation.BottomNavigationView;


import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.ArrayList;
import java.util.GregorianCalendar;

import Bio.Library.namespace.BioLib;

// SDK v1.0.07 @MAR15
public class BioLibTestActivity extends AppCompatActivity implements ReportsFragment.OnStartSleepReportListener{

	@Override
	public void onStartSleepReport(LocalDate clickedDate) {
		Log.e("BiolibTestActivity", "fragment-activity interface is working");

		// Start SleepReportActivity and pass the clickedDate as an extra
		Intent intent = new Intent(this, SleepReportActivity.class);
		intent.putExtra("clickedDate", clickedDate);
		startActivity(intent);
	}

	private BioLib lib = null;

	private String address = "";
	private String macaddress = "";
	private String mConnectedDeviceName = "";
	private BluetoothDevice deviceToConnect;

	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";

	private TextView text;
	private TextView textRTC;
	private TextView textPUSH;
	private TextView textPULSE;
	private TextView textBAT;
	private TextView textDataReceived;
	private TextView textSDCARD;
	private TextView textACC;
	private TextView textHR;
	private TextView textECG;
	private TextView textDeviceId;
	private TextView textRadioEvent;
	private TextView textTimeSpan;
	private TextView textViewTestBPM;

	private Button buttonConnect;
	private Button buttonDisconnect;
	private Button buttonGetRTC;
	private Button buttonSetRTC;
	private Button buttonRequest;
	private Button buttonSearch;
	private Button buttonSetLabel;
	private Button buttonGetDeviceId;
	private Button buttonGetAcc;



	private int BATTERY_LEVEL = 0;
	private int PULSE = 0;
	private Date DATETIME_PUSH_BUTTON = null;
	private Date DATETIME_RTC = null;
	private Date DATETIME_TIMESPAN = null;
	private int SDCARD_STATE = 0;
	private int numOfPushButton = 0;
	private BioLib.DataACC dataACC = null;
	private String deviceId = "";
	private String firmwareVersion = "";
	private byte accSensibility = 1;    // NOTE: 2G= 0, 4G= 1
	private byte typeRadioEvent = 0;
	private byte[] infoRadioEvent = null;
	private short countEvent = 0;

	private boolean isConn = false;

	private byte[][] ecg = null;
	private int nBytes = 0;

	private String accConf = "";


	/**
	 * EDITED CODE STARTS HERE
	 */
	private static final int APNEA_THRESHOLD = 20;
	private static final int EVENT_SPAN = 10000; // duration of an event in ms
	private int peak_number = 0; // variable to store how many beats happen in an event
	private int event_span = 0; //variable to store the time of an event
	private double meanBpm;
	public ArrayList<Double> bpm = new ArrayList<Double>(); //array that stores bpm values
	public ArrayList<Double> bpmMonitored = new ArrayList<Double>(); //array that stores bpm values
	public ArrayList<Integer> eventBpmi = new ArrayList<Integer>(); //array that stores bpm values
	public ArrayList<Event> events = new ArrayList<Event>(); //array that stores event instances

	public ArrayList<Boolean> apneaEvents = new ArrayList<Boolean>();

	private boolean isMonitoring = false;

	private Button buttonGetSleepReport;
	private Button buttonOpenCalendar;

	private ToggleButton buttonMonitor;

	private View clockBackground;

	public Calendar startCalendar;
	public Calendar nightStartCalendar;
	public Calendar eventStartCalendar;
	public Calendar endCalendar;

	private String nightStartDate;
	private String eventStartDate;
	private String nightEndDate;

	private SimpleDateFormat nightTimeFormat = new SimpleDateFormat("h:mm a");
	private SimpleDateFormat fullDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	private SimpleDateFormat eventDateFormat = new SimpleDateFormat("HH:mm:ss");

    private Night night = new Night();
	private int lastNightID;
    private Event event = new Event();



	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		// Get Sleep Report Button on Main Activity, goes to Sleep Report Activity
		// USE SYSTEM CLOCK
		BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
		bottomNavigation.setOnItemSelectedListener(navListener);
		// Load the default fragment
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.fragmentContainer, new MonitorFragment())
				.commit();

		DBHandler dbHandler = DBHandler.getInstance(BioLibTestActivity.this);
		dbHandler.cleanDatabase(dbHandler.getWritableDatabase());

		Night night1 = new Night("2023-12-08","23:30" ,"7:22", 3);
		Night night2 = new Night("2023-12-14","23:30" ,"7:22", 4);
		Night night3 = new Night("2023-12-20","23:30" ,"7:22", 0);
		Night night4 = new Night("2024-02-05","23:30" ,"7:22", 69);
		Night night5 = new Night("2023-12-22","23:30" ,"7:22", 0);

		dbHandler.addNight(night1);
		dbHandler.addNight(night2);
		dbHandler.addNight(night3);
		dbHandler.addNight(night4);
		dbHandler.addNight(night5);

		events.add(new Event(90, "some date", "2023-12-12"));
		events.add(new Event(80, "some date", "2023-12-12"));
		events.add(new Event(70, "some date", "2023-12-12"));
		events.add(new Event(60, "some date", "2023-12-12"));
		events.add(new Event(60, "some date", "2023-12-12"));
		events.add(new Event(60, "some date", "2023-12-12"));
		events.add(new Event(60, "some date", "2023-12-12"));
		events.add(new Event(85, "some date", "2023-12-12"));
		events.add(new Event(60, "some date", "2023-12-12"));
		events.add(new Event(60, "some date", "2023-12-12"));
		events.add(new Event(60, "some date", "2023-12-12"));
		events.add(new Event(70, "some date", "2023-12-12"));
		events.add(new Event(85, "some date", "2023-12-12"));

		bpmMonitored = dbHandler.getBpmValuesForNight("2023-12-12");

	}

	// Load the default fragment (Biolib)
	private BottomNavigationView.OnItemSelectedListener navListener =
			new BottomNavigationView.OnItemSelectedListener() {
				@Override
				public boolean onNavigationItemSelected(@NonNull MenuItem item) {
					Fragment selectedFragment = null;

					if (item.getItemId() == R.id.reportsMenu)
						selectedFragment = new ReportsFragment();
					else if (item.getItemId() == R.id.monitorMenu)
						selectedFragment = new MonitorFragment();
					else if (item.getItemId() == R.id.settingsMenu)
						selectedFragment = new SettingsFragment();

					if (selectedFragment != null) {
						getSupportFragmentManager().beginTransaction()
								.replace(R.id.fragmentContainer, selectedFragment)
								.commit();
					}
//	            	textRadioEvent.setText("Radio-event: " + typeRadioEvent + "[" + str + "]");

					return true;
				}
	};
}
