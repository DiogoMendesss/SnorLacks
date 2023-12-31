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
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.ArrayList;
import java.util.GregorianCalendar;

import Bio.Library.namespace.BioLib;

// SDK v1.0.07 @MAR15
public class BioLibTestActivity extends AppCompatActivity {

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

//		buttonMonitor = findViewById(R.id.btnMonitor);
//		buttonGetSleepReport = findViewById(R.id.buttonGetSleepReport);

//		buttonMonitor.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View view) {
//
//				if (isMonitoring) {
//					/** STOP MONITORING ACTION */
//
//					endCalendar = Calendar.getInstance();
//					nightEndDate = fullDateFormat.format(endCalendar.getTime());
//					night.setEnd_date(nightEndDate);
//					night.calculateSleepTime();
//
//					//night.reset();
//
//					// sleep to awake background animation
//					animationView.setImageDrawable(sleep_to_awake);
//					sleep_to_awake.startTransition(2500);
//
//					// Change to "sleep" when the button is checked
//					buttonMonitor.startAnimation(fadeOut);
//					buttonMonitor.setBackgroundResource(R.drawable.awake);
//					buttonMonitor.startAnimation(fadeIn);
//
//					buttonDisconnect.setEnabled(true);
//					buttonSearch.setEnabled(true);
//					buttonGetSleepReport.setEnabled(true);
//
//					if(!events.isEmpty()) {
//						/*
//						bpmMonitored = bpm;
//						cropBpmArray(bpmMonitored);
//						apneaEvents = checkApneaEvents(bpmMonitored, APNEA_THRESHOLD);
//
//						 */
//
//						cropEventArray(events);
//						night.setApneaEventsNumber(checkApneaEvents(events, 20));
//
//						for (Event event : events){
//							//Toast.makeText(BioLibTestActivity.this, "entrou no for " + nightEndDate, Toast.LENGTH_SHORT).show();
//							dbHandler.addEvent(event);
//						}
//
//						dbHandler.addNight(night);
//
//
//						Toast.makeText(BioLibTestActivity.this, "Sleep monitoring stopped at " + nightEndDate, Toast.LENGTH_SHORT).show();
//					}
//					else Toast.makeText(BioLibTestActivity.this, "Empty array", Toast.LENGTH_SHORT).show();
//
//
//
//
//
//				} else {
//					/** START MONITORING ACTION */
//
//					nightStartCalendar = Calendar.getInstance();
//					nightStartDate = fullDateFormat.format(nightStartCalendar.getTime());
//					night.setStart_date(nightStartDate);
//					lastNightID = dbHandler.getLastNightID();
//
//
//					//buttonMonitor.setText("Stop Monitoring");
//
//					// awake to sleep background animation
//					animationView.setImageDrawable(awake_to_sleep);
//					awake_to_sleep.startTransition(2500);
//
//					// Change to "awake" when the button is checked
//					buttonMonitor.startAnimation(fadeOut);
//					buttonMonitor.setBackgroundResource(R.drawable.sleep);
//					buttonMonitor.startAnimation(fadeIn);
//
//					buttonDisconnect.setEnabled(false);
//					buttonSearch.setEnabled(false);
//					buttonGetSleepReport.setEnabled(false);
//
//					bpm.clear();
//
//
//					Toast.makeText(BioLibTestActivity.this, "Sleep monitoring started at " + nightStartDate, Toast.LENGTH_SHORT).show();
//
//				}
//
//				isMonitoring = !isMonitoring;
//			}
//			private void startMonitoring() {
//				// TODO: Add code to start monitoring
//				// Example: Start a background service, initiate sensor readings, etc.
//			}
//
//			private void stopMonitoring() {
//				// TODO: Add code to stop monitoring
//				// Example: Stop the background service, close sensor connections, etc.
//			}
//
//		});

//		buttonOpenCalendar = findViewById(R.id.buttonOpenCalendar);
//		buttonOpenCalendar.setOnClickListener( new View.OnClickListener() {
//			@Override
//			public void onClick(View view){
//				Intent intent = new Intent(BioLibTestActivity.this, CalendarActivity.class);
//				startActivity(intent);
//			}
//		});
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

//	public void OnDestroy() {
//		if (isConn) {
//			Disconnect();
//		}
//	}

//	public static double calculateMean(ArrayList<Integer> arrayList) {
//		if (arrayList == null || arrayList.isEmpty()) {
//			throw new IllegalArgumentException("Input ArrayList is null or empty");
//		}
//
//		int sum = 0;
//		for (int number : arrayList) {
//			sum += number;
//		}
//
//		return (double) sum / arrayList.size();
//	}
//    public void onActivityResult(int requestCode, int resultCode, Intent data)
//    {
//        switch (requestCode)
//        {
//	        case BioLib.REQUEST_ENABLE_BT:
//	            if (resultCode == Activity.RESULT_OK)
//	            {
//	            	Toast.makeText(getApplicationContext(), "Bluetooth is now enabled! ", Toast.LENGTH_SHORT).show();
//	            	text.append("Bluetooth is now enabled \n");
//
//	            	buttonConnect.setEnabled(true);
//	            	//buttonRequest.setEnabled(true);
//	                buttonDisconnect.setEnabled(false);
//	                //buttonGetRTC.setEnabled(false);
//	                //buttonSetRTC.setEnabled(false);
//	                //buttonSetLabel.setEnabled(false);
//	                //buttonGetDeviceId.setEnabled(false);
//	                //buttonGetAcc.setEnabled(false);
//
//	                text.append("Macaddress selected: " + address + " \n");
//	            }
//	            else
//	            {
//	            	Toast.makeText(getApplicationContext(), "Bluetooth not enabled! ", Toast.LENGTH_SHORT).show();
//	            	text.append("Bluetooth not enabled \n");
//	            	isConn = false;
//
//
//					buttonConnect.setEnabled(true);
//					//buttonRequest.setEnabled(true);
//					buttonDisconnect.setEnabled(false);
//					//buttonGetRTC.setEnabled(false);
//					//buttonSetRTC.setEnabled(false);
//					//buttonSetLabel.setEnabled(false);
//					//buttonGetDeviceId.setEnabled(false);
//					//buttonGetAcc.setEnabled(false);
//
//	            }
//	            break;
//
//	        case 0:
//	        	switch (resultCode)
//	        	{
//	        		case SearchDeviceActivity.CHANGE_MACADDRESS:
//	        			try
//	        			{
//	        				text.append("\nSelect new macaddress: ");
//	        				macaddress = data.getExtras().getString(SearchDeviceActivity.SELECT_DEVICE_ADDRESS);
//	        				Toast.makeText(getApplicationContext(), macaddress, Toast.LENGTH_SHORT).show();
//
//	        				text.append(macaddress);
//
//	        				address = macaddress;
//	        			}
//	        			catch (Exception ex)
//	        			{
//	        				Toast.makeText(getApplicationContext(), "ERROR: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
//	        			}
//	        			break;
//	        	}
//    			break;
//        }
//    }



//	//checkApneaEvent() returns true if for an event, a consecutive number of samples exceeds a threshold
//	public static ArrayList<Boolean> checkBpmApneaEvents(ArrayList<Double> bpmList, int threshold) {
//
//		ArrayList<Boolean> apneaEvents = new ArrayList<Boolean>();
//		double median = calculateMedian(bpmList);
//		for (int i = 0; i < bpmList.size(); i++) {
//			if (bpmList.get(i) > median + threshold) {
//				apneaEvents.add(Boolean.TRUE);
//			}
//			else apneaEvents.add(Boolean.FALSE);
//		}

//	protected void onDestroy() {
//		super.onDestroy();

//		if (lib.mBluetoothAdapter != null) {
//			try {
//				if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
//					// TODO: Consider calling
//					//    ActivityCompat#requestPermissions
//					// here to request the missing permissions, and then overriding
//					//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//					//                                          int[] grantResults)
//					// to handle the case where the user grants the permission. See the documentation
//					// for ActivityCompat#requestPermissions for more details.
//					return;
//				}
//				lib.mBluetoothAdapter.cancelDiscovery();
//			} catch (Exception e) {
//				System.out.println("PAM PAM");
//			}
//		}
//
//		lib = null;
//	}
//
//	/***
//	 * Disconnect from device.
//	 */
//	private void Disconnect() {
//		try {
//			lib.Disconnect();
//		} catch (Exception e) {
//			e.printStackTrace();
//		} finally {
//			Reset();
//		}
//	}
//
//	/***
//	 * Reset variables and UI.
//	 */
//	private void Reset() {
//		try {
//			/*
//			textBAT.setText("BAT: - - %");
//			textPULSE.setText("PULSE: - - bpm");
//			textPUSH.setText("PUSH-BUTTON: - - - ");
//			textRTC.setText("RTC: - - - ");
//			textDataReceived.setText("RECEIVED: - - - ");
//			textACC.setText("ACC:  X: - -  Y: - -  Z: - -");
//			textSDCARD.setText("SD CARD STATUS: - - ");
//			textECG.setText("Ecg stream: -- ");
//			textHR.setText("PEAK: --  BPMi: -- bpm  BPM: -- bpm  R-R: -- ms");
//			textBAT.setText("BAT: -- %");
//			textPULSE.setText("HR: -- bpm     Nb. Leads: -- ");
//			textDeviceId.setText("Device Id: - - - - - - - - - -");
//			textRadioEvent.setText(".");
//			textTimeSpan.setText("SPAN: - - - ");
//
//			SDCARD_STATE = 0;
//			BATTERY_LEVEL = 0;
//			PULSE = 0;
//			DATETIME_PUSH_BUTTON = null;
//			DATETIME_RTC = null;
//			DATETIME_TIMESPAN = null;
//			numOfPushButton = 0;
//			countEvent = 0;
//			accConf = "";
//			firmwareVersion = "";
//
//			 */
//		} catch (Exception ex) {
//			ex.printStackTrace();
//		}
//	}
//
//	/**
//	 * The Handler that gets information back from the BioLib
//	 */
//	private final Handler mHandler = new Handler() {
//		@Override
//		public void handleMessage(Message msg) {
//			switch (msg.what) {
//				case BioLib.MESSAGE_READ:
//					//textDataReceived.setText("RECEIVED: " + msg.arg1);
//					break;
//
//				case BioLib.MESSAGE_DEVICE_NAME:
//					mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
//					Toast.makeText(getApplicationContext(), "Connected to " + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
//					text.append("Connected to " + mConnectedDeviceName + " \n");
//					break;
//
//				case BioLib.MESSAGE_BLUETOOTH_NOT_SUPPORTED:
//					Toast.makeText(getApplicationContext(), "Bluetooth NOT supported. Aborting! ", Toast.LENGTH_SHORT).show();
//					text.append("Bluetooth NOT supported. Aborting! \n");
//					isConn = false;
//					break;
//
//				case BioLib.MESSAGE_BLUETOOTH_ENABLED:
//					Toast.makeText(getApplicationContext(), "Bluetooth is now enabled! ", Toast.LENGTH_SHORT).show();
//					text.append("Bluetooth is now enabled \n");
//					text.append("Macaddress selected: " + address + " \n");
//					buttonConnect.setEnabled(true);
//					//buttonRequest.setEnabled(true);
//					break;
//
//				case BioLib.MESSAGE_BLUETOOTH_NOT_ENABLED:
//					Toast.makeText(getApplicationContext(), "Bluetooth not enabled! ", Toast.LENGTH_SHORT).show();
//					text.append("Bluetooth not enabled \n");
//					isConn = false;
//					break;
//
//				case BioLib.REQUEST_ENABLE_BT:
//					Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//					startActivityForResult(enableIntent, BioLib.REQUEST_ENABLE_BT);
//					text.append("Request bluetooth enable \n");
//					break;
//
//				case BioLib.STATE_CONNECTING:
//					text.append("   Connecting to device ... \n");
//					break;
//
//				case BioLib.STATE_CONNECTED:
//					Toast.makeText(getApplicationContext(), "Connected to " + deviceToConnect.getName(), Toast.LENGTH_SHORT).show();
//					text.append("   Connect to " + deviceToConnect.getName() + " \n");
//					isConn = true;
//
//					buttonConnect.setEnabled(false);
//					//buttonRequest.setEnabled(false);
//					buttonDisconnect.setEnabled(true);
//					//buttonGetRTC.setEnabled(true);
//					//buttonSetRTC.setEnabled(true);
//					//buttonSetLabel.setEnabled(true);
//					//buttonGetDeviceId.setEnabled(true);
//					//buttonGetAcc.setEnabled(true);
//
//					break;
//
//				case BioLib.UNABLE_TO_CONNECT_DEVICE:
//					Toast.makeText(getApplicationContext(), "Unable to connect device! ", Toast.LENGTH_SHORT).show();
//					text.append("   Unable to connect device \n");
//					isConn = false;
//
//					buttonConnect.setEnabled(true);
//					//buttonRequest.setEnabled(true);
//					buttonDisconnect.setEnabled(false);
//					//buttonGetRTC.setEnabled(false);
//					//buttonSetRTC.setEnabled(false);
//					//buttonSetLabel.setEnabled(false);
//					//buttonGetDeviceId.setEnabled(false);
//					//buttonGetAcc.setEnabled(false);
//
//					break;
//
//				case BioLib.MESSAGE_DISCONNECT_TO_DEVICE:
//					Toast.makeText(getApplicationContext(), "Device connection was lost", Toast.LENGTH_SHORT).show();
//					text.append("   Disconnected from " + deviceToConnect.getName() + " \n");
//					isConn = false;
//
//
//					buttonConnect.setEnabled(true);
//					//buttonRequest.setEnabled(true);
//					buttonDisconnect.setEnabled(false);
//					//buttonGetRTC.setEnabled(false);
//					//buttonSetRTC.setEnabled(false);
//					//buttonSetLabel.setEnabled(false);
//					//buttonGetDeviceId.setEnabled(false);
//					//buttonGetAcc.setEnabled(false);
//
//					break;
//
//				case BioLib.MESSAGE_PUSH_BUTTON:
//					DATETIME_PUSH_BUTTON = (Date) msg.obj;
//					numOfPushButton = msg.arg1;
//					//textPUSH.setText("PUSH-BUTTON: [#" + numOfPushButton + "]" + DATETIME_PUSH_BUTTON.toString());
//					break;
//
//				case BioLib.MESSAGE_RTC:
//					DATETIME_RTC = (Date) msg.obj;
//					//textRTC.setText("RTC: " + DATETIME_RTC.toString());
//					break;
//
//				case BioLib.MESSAGE_TIMESPAN:
//					DATETIME_TIMESPAN = (Date) msg.obj;
//					//textTimeSpan.setText("SPAN: " + DATETIME_TIMESPAN.toString());
//					break;
//
//				case BioLib.MESSAGE_DATA_UPDATED:
//					BioLib.Output out = (BioLib.Output) msg.obj;
//					BATTERY_LEVEL = out.battery;
//					//textBAT.setText("BAT: " + BATTERY_LEVEL + " %");
//					PULSE = out.pulse;
//					//textPULSE.setText("HR: " + PULSE + " bpm     Nb. Leads: " + lib.GetNumberOfChannels());
//					break;
//
//				case BioLib.MESSAGE_SDCARD_STATE:
//
//					/*
//	            	SDCARD_STATE = (int)msg.arg1;
//
//	            	if (SDCARD_STATE == 1)
//	            		textSDCARD.setText("SD CARD STATE: ON");
//	            	else
//	            		textSDCARD.setText("SD CARD STATE: OFF");
//
//					 */
//					break;
//
//				case BioLib.MESSAGE_RADIO_EVENT:
//	            	/*textRadioEvent.setText("Radio-event: received ... ");
//
//	            	typeRadioEvent = (byte)msg.arg1;
//	            	infoRadioEvent = (byte[]) msg.obj;
//
//	            	String str = "";
//					try {
//						str = new String(infoRadioEvent, "UTF8");
//					} catch (UnsupportedEncodingException e) {
//						e.printStackTrace();
//					}
//	            	textRadioEvent.setText("Radio-event: " + typeRadioEvent + "[" + str + "]");
//
//	            	 */
//
//					break;
//
//				case BioLib.MESSAGE_FIRMWARE_VERSION:
//
//					/*
//	            	// Show firmware version in device VitalJacket ...
//	            	firmwareVersion = (String)msg.obj;
//
//					 */
//					break;
//
//				case BioLib.MESSAGE_DEVICE_ID:
//
//					/*
//	            	deviceId = (String)msg.obj;
//	            	textDeviceId.setText("Device Id: " + deviceId);
//
//					 */
//
//					break;
//
//				case BioLib.MESSAGE_ACC_SENSIBILITY:
//
//					/*
//	            	accSensibility = (byte)msg.arg1;
//	            	accConf = "4G";
//	            	switch (accSensibility)
//	            	{
//	            		case 0:
//	            			accConf = "2G";
//	            			break;
//
//	            		case 1:
//	            			accConf = "4G";
//	            			break;
//	            	}
//
//	            	textACC.setText("ACC [" + accConf + "]:  X: " + dataACC.X + "  Y: " + dataACC.Y + "  Z: " + dataACC.Z);
//
//					 */
//
//					break;
//
//	            case BioLib.MESSAGE_PEAK_DETECTION:
//	            	BioLib.QRS qrs = (BioLib.QRS)msg.obj;
//
//
//					/** EDITED CODE STARTS HERE*/
//					if (event_span<EVENT_SPAN){ //checks if the duration of the event hasn't overcome the EVENT_SPAN
//						eventStartCalendar = Calendar.getInstance();
//						eventStartDate = eventDateFormat.format(eventStartCalendar.getTime());
//						eventBpmi.add(qrs.bpmi);
//						peak_number++;
//						event_span += qrs.rr;
//					}
//					else{ //An event has completed and the mean bpm for that event is calculated (in minutes)
//						meanBpm = calculateMean(eventBpmi);
//						bpm.add(meanBpm);
//						event_span = 0;
//						peak_number=0;
//						eventBpmi.clear();
//						Toast.makeText(BioLibTestActivity.this, Double.toString(peak_number/(EVENT_SPAN*1000.0*60)), Toast.LENGTH_SHORT).show();
//
//						event = new Event(meanBpm, eventStartDate, nightStartDate);
//						events.add(event);
//					}
//
//					//textViewTestBPM.setText("Peak number: " + peak_number + "; Event duration: " + event_span);
//					textViewTestBPM.setText("PEAK: " + qrs.position + "  BPMi: " + qrs.bpmi + " bpm  BPM: " + qrs.bpm + " bpm  R-R: " + qrs.rr + " ms; Array size: " + bpm.size());
//
//					/** EDITED CODE ENDS HERE*/
//
//
//	            	//textHR.setText("PEAK: " + qrs.position + "  BPMi: " + qrs.bpmi + " bpm  BPM: " + qrs.bpm + " bpm  R-R: " + qrs.rr + " ms");
//	            	break;
//
//	            case BioLib.MESSAGE_ACC_UPDATED:
//
//					/*
//	            	dataACC = (BioLib.DataACC)msg.obj;
//
//	            	if (accConf == "")
//	            		textACC.setText("ACC:  X: " + dataACC.X + "  Y: " + dataACC.Y + "  Z: " + dataACC.Z);
//	            	else
//	            		textACC.setText("ACC [" + accConf + "]:  X: " + dataACC.X + "  Y: " + dataACC.Y + "  Z: " + dataACC.Z);
//
//					 */
//
//
//	            	break;
//
//	            case BioLib.MESSAGE_ECG_STREAM:
//
//					/*
//	            	try
//	            	{
//	            		textECG.setText("ECG received");
//	            		ecg = (byte[][]) msg.obj;
//	            		int nLeads = ecg.length;
//	            		nBytes = ecg[0].length;
//	            		textECG.setText("ECG stream: OK   nBytes: " + nBytes + "   nLeads: " + nLeads);
//	            	}
//
//	            	catch (Exception ex)
//	            	{
//	            		textECG.setText("ERROR in ecg stream");
//	            	}
//
//					 */
//
//	            	break;
//
//	            case BioLib.MESSAGE_TOAST:
//	                Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST), Toast.LENGTH_SHORT).show();
//	                break;
//            }
//        }
//    };
//
//
//	public static double calculateMean(ArrayList<Integer> arrayList) {
//		if (arrayList == null || arrayList.isEmpty()) {
//			throw new IllegalArgumentException("Input ArrayList is null or empty");
//		}
//
//		int sum = 0;
//		for (int number : arrayList) {
//			sum += number;
//		}
//
//		return (double) sum / arrayList.size();
//	}
//
//	public void onActivityResult(int requestCode, int resultCode, Intent data) {
//		switch (requestCode) {
//			case BioLib.REQUEST_ENABLE_BT:
//				if (resultCode == Activity.RESULT_OK) {
//					Toast.makeText(getApplicationContext(), "Bluetooth is now enabled! ", Toast.LENGTH_SHORT).show();
//					text.append("Bluetooth is now enabled \n");
//
//					buttonConnect.setEnabled(true);
//					//buttonRequest.setEnabled(true);
//					buttonDisconnect.setEnabled(false);
//					//buttonGetRTC.setEnabled(false);
//					//buttonSetRTC.setEnabled(false);
//					//buttonSetLabel.setEnabled(false);
//					//buttonGetDeviceId.setEnabled(false);
//					//buttonGetAcc.setEnabled(false);
//
//					text.append("Macaddress selected: " + address + " \n");
//				} else {
//					Toast.makeText(getApplicationContext(), "Bluetooth not enabled! ", Toast.LENGTH_SHORT).show();
//					text.append("Bluetooth not enabled \n");
//					isConn = false;
//
//
//					buttonConnect.setEnabled(true);
//					//buttonRequest.setEnabled(true);
//					buttonDisconnect.setEnabled(false);
//					//buttonGetRTC.setEnabled(false);
//					//buttonSetRTC.setEnabled(false);
//					//buttonSetLabel.setEnabled(false);
//					//buttonGetDeviceId.setEnabled(false);
//					//buttonGetAcc.setEnabled(false);
//
//				}
//				break;
//
//			case 0:
//				switch (resultCode) {
//					case SearchDeviceActivity.CHANGE_MACADDRESS:
//						try {
//							text.append("\nSelect new macaddress: ");
//							macaddress = data.getExtras().getString(SearchDeviceActivity.SELECT_DEVICE_ADDRESS);
//							Toast.makeText(getApplicationContext(), macaddress, Toast.LENGTH_SHORT).show();
//
//							text.append(macaddress);
//
//							address = macaddress;
//						} catch (Exception ex) {
//							Toast.makeText(getApplicationContext(), "ERROR: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
//						}
//						break;
//				}
//				break;
//		}
//	}
//
//	//checkApneaEvent() returns true if for an event, a consecutive number of samples exceeds a threshold
//	public static ArrayList<Boolean> checkApneaEvents(ArrayList<Double> bpmList, int threshold) {
//
//		ArrayList<Boolean> apneaEvents = new ArrayList<Boolean>();
//		double median = calculateMedian(bpmList);
//		for (int i = 0; i < bpmList.size(); i++) {
//			if (bpmList.get(i) > median + threshold) {
//				apneaEvents.add(Boolean.TRUE);
//			} else apneaEvents.add(Boolean.FALSE);
//		}
//
//		return apneaEvents;
//	}
//
//	public static void cropBpmArray(ArrayList<Double> bpmList) {
//
//		double median = calculateMedian(bpmList);
//
//		// Remove the first values until a sample is lesser than the median
//		while (!bpmList.isEmpty() && bpmList.get(0) >= median) {
//			bpmList.remove(0);
//		}
//
//		// Remove the last values until a sample is lesser than the median
//		while (!bpmList.isEmpty() && bpmList.get(bpmList.size() - 1) >= median) {
//			bpmList.remove(bpmList.size() - 1);
//		}
//	}
//
//	public static double calculateMedian(ArrayList<Double> numbers) {
//		// Check for empty list
//		if (numbers == null || numbers.isEmpty()) {
//			throw new IllegalArgumentException("The list is empty");
//		}
//
//		// Sort the ArrayList
//		ArrayList<Double> sorted_numbers = new ArrayList<Double>(numbers);
//		Collections.sort(sorted_numbers);
//
//		int size = sorted_numbers.size();
//		double median;
//
//		if (size % 2 == 0) {
//			// If the size is even, average the two middle elements
//			double middle1 = sorted_numbers.get(size / 2 - 1);
//			double middle2 = sorted_numbers.get(size / 2);
//			median = (middle1 + middle2) / 2.0;
//		} else {
//			// If the size is odd, take the middle element
//			median = sorted_numbers.get(size / 2);
//		}
//
//		return median;
//	}
//}


//	public static int checkApneaEvents(ArrayList<Event> bpmList, int threshold) {
//
//		int apneaEventsNumber = 0;
//		ArrayList<Boolean> apneaEvents = new ArrayList<Boolean>();
//		double median = calculateEventBpmMedian(bpmList);
//		for (int i = 0; i < bpmList.size(); i++) {
//			if (bpmList.get(i).getType() == null) {
//				if (bpmList.get(i).getBpm() > median + threshold) {
//					bpmList.get(i).setType("apnea");
//					apneaEventsNumber++;
//				} else bpmList.get(i).setType("normal");
//			}
//		}
//		return apneaEventsNumber;
//	}


//	public static void cropBpmArray(ArrayList<Double> bpmList){
//
//		double median = calculateMedian(bpmList);
//
//		// Remove the first values until a sample is lesser than the median
//		while (!bpmList.isEmpty() && bpmList.get(0) >= median) {
//			bpmList.remove(0);
//		}
//
//		// Remove the last values until a sample is lesser than the median
//		while (!bpmList.isEmpty() && bpmList.get(bpmList.size() - 1) >= median) {
//			bpmList.remove(bpmList.size() - 1);
//		}
//	}



//	public static void cropEventArray(ArrayList<Event> bpmList){
//
//		double median = calculateEventBpmMedian(bpmList);
//		int i = 0;
//		// Remove the first values until a sample is lesser than the median
//		while (bpmList.get(i).getBpm() > median) {
//			bpmList.get(i).setType("Falling asleep");
//			i++;
//		}
//
//		i=0;
//		// Remove the last values until a sample is lesser than the median
//		while (bpmList.get(bpmList.size() - 1 - i).getBpm() > median) {
//			bpmList.get(bpmList.size() - 1 - i).setType("Awakening");
//			i++;
//		}
//	}


//	public static double calculateMedian(ArrayList<Double> numbers) {
//		// Check for empty list
//		if (numbers == null || numbers.isEmpty()) {
//			throw new IllegalArgumentException("The list is empty");
//		}
//
//		// Sort the ArrayList
//		ArrayList<Double> sorted_numbers = new ArrayList<Double>(numbers);
//		Collections.sort(sorted_numbers);
//
//		int size = sorted_numbers.size();
//		double median;
//
//		if (size % 2 == 0) {
//			// If the size is even, average the two middle elements
//			double middle1 = sorted_numbers.get(size / 2 - 1);
//			double middle2 = sorted_numbers.get(size / 2);
//			median = (middle1 + middle2) / 2.0;
//		} else {
//			// If the size is odd, take the middle element
//			median = sorted_numbers.get(size / 2);
//		}
//
//		return median;
//	}



//	public static double calculateEventBpmMedian(ArrayList<Event> events) {
//		// Check for empty list
//		if (events == null || events.isEmpty()) {
//			throw new IllegalArgumentException("The list is empty");
//		}
//
//		// Sort the ArrayList
//		ArrayList<Event> sortedEvents = new ArrayList<>(events);
//		sortedEvents.sort((e1, e2) -> Double.compare(e1.getBpm(), e2.getBpm()));
//
//		int size = sortedEvents.size();
//		double median;
//
//		if (size % 2 == 0) {
//			// If the size is even, average the two middle elements
//			double middle1 = sortedEvents.get(size / 2 - 1).getBpm();
//			double middle2 = sortedEvents.get(size / 2).getBpm();
//			median = (middle1 + middle2) / 2.0;
//		} else {
//			// If the size is odd, take the middle element
//			median = sortedEvents.get(size / 2).getBpm();
//		}
//
//		return median;
//	}
}
