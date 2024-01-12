package com.snorlacks.snorlacksapp;

import static com.snorlacks.snorlacksapp.MonitorFragment.checkApneaEvents;
import static com.snorlacks.snorlacksapp.MonitorFragment.cropEventArray;

import static java.security.AccessController.getContext;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;


import com.google.android.material.bottomnavigation.BottomNavigationView;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.ArrayList;
import java.util.Random;

import Bio.Library.namespace.BioLib;


// SDK v1.0.07 @MAR15
public class BioLibTestActivity extends AppCompatActivity implements ReportsFragment.OnStartSleepReportListener, MonitorFragment.OnStartSleepReportListener{

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

	private TextView textViewTestBPM;


	//Battery icon
	private ImageView iv_battery;
	private TextView tv_battery;


	private int BATTERY_LEVEL = 65;

	private int PULSE;


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

		DBHandler dbHandler;
		dbHandler = DBHandler.getInstance(BioLibTestActivity.this);
		//dbHandler.cleanDatabase();

		/** SOME SAMPLES ARE ADDED JUST FOR EASIER TESTING AND DATABASE POPULATION
		  */

		Night night1 = new Night("2023-12-08","23:30" ,"7:22", 3);
		Night night2 = new Night("2023-12-14","23:30" ,"7:22", 4);
		Night night3 = new Night("2023-12-20","23:30" ,"7:22", 0);
		Night night4 = new Night("2024-02-05","23:30" ,"7:22", 65);
		Night night5 = new Night("2023-12-22","23:30" ,"7:22", 0);


		dbHandler.addNight(night1);
		dbHandler.addNight(night2);
		dbHandler.addNight(night3);
		dbHandler.addNight(night4);
		dbHandler.addNight(night5);

		ArrayList<Event> simevents; //array that stores event instances
		ArrayList<Event> simevents2; //array that stores event instances


		Night simNight = new Night("2024-01-07","23:03" ,"8:03");
		simevents = generateMockBPMData(6, "23:03:10", "2024-01-07");
		cropEventArray(simevents);
		simNight.setApneaEventsNumber(checkApneaEvents(simevents, 20));
		dbHandler.addNight(simNight);
		dbHandler.addEvents(simevents);

		Night simNight2 = new Night("2024-01-04","23:03" ,"8:03");
		simevents2 = generateMockBPMData(0, "23:03:10", "2024-01-04");
		cropEventArray(simevents2);
		simNight2.setApneaEventsNumber(checkApneaEvents(simevents2, 20));
		dbHandler.addNight(simNight2);
		dbHandler.addEvents(simevents2);

		/** TESTING SAMPLES ENDS HERE */





		//Battery icon
		iv_battery = (ImageView) findViewById(R.id.iv_battery);
		tv_battery = (TextView) findViewById(R.id.tv_battery);


		/*
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

		 */
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

	public static ArrayList<Event> generateMockBPMData(int numApneas, String startTime, String date) {
		ArrayList<Event> events = new ArrayList<>();
		SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

		try {
			Date monitoringStartTime = dateFormat.parse(startTime);
			Date currentTime = monitoringStartTime;

			Random random = new Random();

			for (int i = 0; i < 8 * 60; i++) { // 8 hours * 60 minutes
				if (numApneas > 0 && random.nextDouble() < 0.02) { // Simulate apnea (2% chance per minute)
					simulateApnea(events, monitoringStartTime, date);
					numApneas--;
				} else {
					int bpm = random.nextInt(11) + 50; // Random BPM between 50 and 60
					String timeString = dateFormat.format(currentTime);
					events.add(new Event(bpm, timeString, date));
				}

				currentTime = new Date(currentTime.getTime() + 60 * 1000); // Move to the next minute
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return events;
	}

	private static void simulateApnea(ArrayList<Event> events, Date startTime, String date) {
		int bpm = 60;

		// Increase BPM to 80 over 3 minutes
		for (int i = 0; i < 3; i++) {
			bpm += 5;
			String timeString = formatTime(startTime, i + 1);
			events.add(new Event(bpm, timeString, date));
		}

		// Stay at 80 BPM for 5 minutes
		for (int i = 0; i < 5; i++) {
			String timeString = formatTime(startTime, i + 4); // Add 4 minutes because the previous loop added 3 minutes
			events.add(new Event(80, timeString, date));
		}

		// Decrease BPM to 50 over 3 minutes
		for (int i = 0; i < 3; i++) {
			bpm -= 10;
			String timeString = formatTime(startTime, i + 9); // Add 9 minutes because the previous loop added 5 minutes
			events.add(new Event(bpm, timeString, date));
		}
	}

	private static String formatTime(Date startTime, int minutesToAdd) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
		Date newTime = new Date(startTime.getTime() + minutesToAdd * 60 * 1000);
		return dateFormat.format(newTime);
	}

	// The Handler that gets information back from the BioLib
	@SuppressLint("HandlerLeak")
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case BioLib.MESSAGE_DATA_UPDATED:
					BioLib.Output out = (BioLib.Output) msg.obj;
					BATTERY_LEVEL = out.battery;

					// Update battery level views
					updateBatteryViews();

					PULSE = out.pulse;
					//textPULSE.setText("HR: " + PULSE + " bpm     Nb. Leads: " + lib.GetNumberOfChannels());
					break;
			}
		}
	};

	// Method to update battery level views
	private void updateBatteryViews() {
		tv_battery.setText(BATTERY_LEVEL + "%");

		if (BATTERY_LEVEL == 100) {
			iv_battery.setImageResource(R.drawable.baseline_battery_full);
		} else if (BATTERY_LEVEL > 75 && BATTERY_LEVEL <= 99) {
			iv_battery.setImageResource(R.drawable.baseline_battery_2);
		} else if (BATTERY_LEVEL > 50 && BATTERY_LEVEL <= 75) {
			iv_battery.setImageResource(R.drawable.baseline_battery_3);
		} else if (BATTERY_LEVEL == 50) {
			iv_battery.setImageResource(R.drawable.baseline_battery_5);
		} else if (BATTERY_LEVEL > 25 && BATTERY_LEVEL < 50) {
			iv_battery.setImageResource(R.drawable.baseline_battery_5);
		} else if (BATTERY_LEVEL > 5 && BATTERY_LEVEL <= 25) {
			iv_battery.setImageResource(R.drawable.baseline_battery_6);
		} else {
			iv_battery.setImageResource(R.drawable.baseline_battery_7);
		}

		if (BATTERY_LEVEL <= 5) {
			iv_battery.setImageResource(R.drawable.baseline_battery_7);
		}

		if (BATTERY_LEVEL == 0) {
			iv_battery.setImageResource(R.drawable.baseline_battery_7);
		}
	}


}
