package com.snorlacks.snorlacksapp;

import static com.snorlacks.snorlacksapp.MonitorFragment.checkApneaEvents;
import static com.snorlacks.snorlacksapp.MonitorFragment.cropEventArray;

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
import android.os.BatteryManager;
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
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;
import android.view.MenuItem;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;


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

import android.content.IntentFilter;


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


	private Button buttonSearch;

	//Battery icon
	private ImageView iv_battery;
	private TextView tv_battery;
	Handler handler;
	Runnable runnable;

	private int BATTERY_LEVEL = 65;
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

	private ViewPager viewPager;
	private BottomNavigationView bottomNav;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		bottomNav = findViewById(R.id.bottomNavigation);
		viewPager = findViewById(R.id.viewPager);

		setupViewPager();
		setupBottomNav();

		int middleFragmentIndex = 1;	// start in the MonitorFragment
		viewPager.setCurrentItem(middleFragmentIndex);
		bottomNav.setItemIconTintList(null);

		DBHandler dbHandler = DBHandler.getInstance(BioLibTestActivity.this);
		dbHandler.cleanDatabase(dbHandler.getWritableDatabase());

		Night night1 = new Night("2023-12-08","23:30" ,"7:22", 3);
		Night night2 = new Night("2023-12-14","23:30" ,"7:22", 4);
		Night night3 = new Night("2023-12-20","23:30" ,"7:22", 0);
		Night night4 = new Night("2024-02-05","23:30" ,"7:22", 69);
		Night night5 = new Night("2023-12-22","23:30" ,"7:22", 0);
		Night theNight = new Night("2023-12-12","22:51" ,"9:22");

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

		cropEventArray(events);
		theNight.setApneaEventsNumber(checkApneaEvents(events, 20));

		dbHandler.addNight(theNight);
		dbHandler.addEvents(events);

		bpmMonitored = dbHandler.getBpmValuesForNight("2023-12-12");

		//Battery icon
		iv_battery = (ImageView) findViewById(R.id.iv_battery);
		tv_battery = (TextView) findViewById(R.id.tv_battery);

		runnable = new Runnable() {
			@Override
			public void run() {
				tv_battery.setText(BATTERY_LEVEL + "%");
				if (BATTERY_LEVEL == 100) {
					iv_battery.setImageResource(R.drawable.baseline_battery_full);
				}
				if (BATTERY_LEVEL > 75 && BATTERY_LEVEL <= 99) {
					iv_battery.setImageResource(R.drawable.baseline_battery_2);
				}
				if (BATTERY_LEVEL > 50 && BATTERY_LEVEL <= 75) {
					iv_battery.setImageResource(R.drawable.baseline_battery_3);
				}
				if (BATTERY_LEVEL == 50) {
					iv_battery.setImageResource(R.drawable.baseline_battery_5);
				}
				if (BATTERY_LEVEL > 25 && BATTERY_LEVEL < 50) {
					iv_battery.setImageResource(R.drawable.baseline_battery_5);
				}
				if (BATTERY_LEVEL > 5 && BATTERY_LEVEL <= 25) {
					iv_battery.setImageResource(R.drawable.baseline_battery_6);
				}
				if (BATTERY_LEVEL <= 5) {
					iv_battery.setImageResource(R.drawable.baseline_battery_7);
				}
				if (BATTERY_LEVEL == 0) {
					iv_battery.setImageResource(R.drawable.baseline_battery_7);
				}
				handler.postDelayed(runnable, 5000);
			};
		};
		handler = new Handler();
		handler.postDelayed(runnable, 0);
	}
	private void setupViewPager() {
		MyPagerAdapter pagerAdapter = new MyPagerAdapter(getSupportFragmentManager());
		viewPager.setAdapter(pagerAdapter);

		viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

			@Override
			public void onPageSelected(int position) {
				// Sync BottomNavigationView with ViewPager
				bottomNav.getMenu().getItem(position).setChecked(true);
			}

			@Override
			public void onPageScrollStateChanged(int state) {}
		});
	}
	private void setupBottomNav() {
		bottomNav.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
			@Override
			public boolean onNavigationItemSelected(@NonNull MenuItem item) {
				int itemId = item.getItemId();
				if (itemId == R.id.reportsMenu) {
					viewPager.setCurrentItem(0); // ReportsFragment
					return true;
				} else if (itemId == R.id.monitorMenu) {
					viewPager.setCurrentItem(1); // MonitorFragment
					return true;
				} else if (itemId == R.id.settingsMenu) {
					viewPager.setCurrentItem(2); // SettingsFragment
					return true;
				} else {
					return false;
				}
			}
		});
	}
	private static class MyPagerAdapter extends FragmentPagerAdapter {
		// Handles the fragments for the bottom navigation bar

		public MyPagerAdapter(FragmentManager fm) {
			super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
		}

		@NonNull
		@Override
		public Fragment getItem(int position) {
			switch (position) {
				case 0:
					return new ReportsFragment();
				case 1:
					return new MonitorFragment();
				case 2:
					return new SettingsFragment();
				default:
					return null;
			}
		}

		@Override
		public int getCount() {
			return 3; // Number of fragments
		}
	}

}
