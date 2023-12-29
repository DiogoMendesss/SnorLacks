package com.snorlacks.snorlacksapp;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

import Bio.Library.namespace.BioLib;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MonitorFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MonitorFragment extends Fragment {

    private Context fragmentContext;

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
    private ToggleButton buttonMonitor;

    private View clockBackground;

    public Calendar startCalendar;
    public Calendar nightStartCalendar;
    public Calendar eventStartCalendar;
    public Calendar endCalendar;

    private String nightStartDate;
    private String eventStartDate;
    private String nightEndDate;

    private SimpleDateFormat nightDateFormat = new SimpleDateFormat("h:mm a");
    private SimpleDateFormat fullDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private SimpleDateFormat eventDateFormat = new SimpleDateFormat("HH:mm:ss");

    private Night night = new Night();
    private int lastNightID;
    private Event event = new Event();


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public MonitorFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        fragmentContext = context;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MonitorFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MonitorFragment newInstance(String param1, String param2) {
        MonitorFragment fragment = new MonitorFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_monitor, container, false);

        textViewTestBPM = view.findViewById(R.id.txtViewTestBPM);

        buttonMonitor = view.findViewById(R.id.btnMonitor);
        buttonGetSleepReport = view.findViewById(R.id.buttonGetSleepReport);
        buttonConnect = (Button) view.findViewById(R.id.buttonConnect);
        buttonDisconnect = (Button) view.findViewById(R.id.buttonDisconnect);
        buttonSearch = (Button) view.findViewById(R.id.buttonSearch);

        clockBackground = view.findViewById(R.id.clockBackground);

        // used for gradient animation
        ConstraintLayout mainLayout = view.findViewById(R.id.mainLayout);
        ImageView animationView = view.findViewById(R.id.animationView);

        TransitionDrawable sleep_to_awake = new TransitionDrawable(new Drawable[]{
                ContextCompat.getDrawable(fragmentContext, R.drawable.sleep_background),
                ContextCompat.getDrawable(fragmentContext, R.drawable.awake_background)
        });
        TransitionDrawable awake_to_sleep = new TransitionDrawable(new Drawable[]{
                ContextCompat.getDrawable(fragmentContext, R.drawable.awake_background),
                ContextCompat.getDrawable(fragmentContext, R.drawable.sleep_background)
        });
        if (view == null)
            Toast.makeText(fragmentContext, "BUTTON IS NULL", Toast.LENGTH_SHORT).show();

        DBHandler dbHandler = DBHandler.getInstance(fragmentContext);

        textViewTestBPM = view.findViewById(R.id.txtViewTestBPM);
        textViewTestBPM.setText("Last night ID: " + dbHandler.getLastNightID());

        Night night1 = new Night("2023-12-08 22:53", "2023-12-09 8:53", "a lot", 3);
        Night night2 = new Night("2023-12-14 23:45", "2023-12-15 10:34", "a lottt", 4);
        Night night3 = new Night("2023-12-20 23:45", "2023-12-21 10:34", "a lottt", 0);

        dbHandler.addNight(night1);
        dbHandler.addNight(night2);
        dbHandler.addNight(night3);

        events.add(new Event(90, "some date", "2023-12-12 11:45"));
        events.add(new Event(80, "some date", "2023-12-12 11:45"));
        events.add(new Event(70, "some date", "2023-12-12 11:45"));
        events.add(new Event(60, "some date", "2023-12-12 11:45"));
        events.add(new Event(60, "some date", "2023-12-12 11:45"));
        events.add(new Event(60, "some date", "2023-12-12 11:45"));
        events.add(new Event(60, "some date", "2023-12-12 11:45"));
        events.add(new Event(85, "some date", "2023-12-12 11:45"));
        events.add(new Event(60, "some date", "2023-12-12 11:45"));
        events.add(new Event(60, "some date", "2023-12-12 11:45"));
        events.add(new Event(60, "some date", "2023-12-12 11:45"));
        events.add(new Event(70, "some date", "2023-12-12 11:45"));
        events.add(new Event(85, "some date", "2023-12-12 11:45"));

        Toast.makeText(fragmentContext, "Apnea events at 2023-12-08 10:53: " + dbHandler.getApneaEventsForNight("2023-12-08 10:53"), Toast.LENGTH_SHORT).show();

        bpmMonitored = dbHandler.getBpmValuesForNight("2023-12-12 11:45");
        if (!bpmMonitored.isEmpty()) {
            textViewTestBPM.setText("Array size: " + bpmMonitored.size() + "First value: " + bpmMonitored.get(0) + "Last value: " + bpmMonitored.get(bpmMonitored.size() - 1));
        } else textViewTestBPM.setText("empty array");

        // main button to start and stop sleep monitoring
        buttonMonitor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(fragmentContext, "CLICKED", Toast.LENGTH_SHORT).show();
                if (isMonitoring) {

                    nightEndDate = fullDateFormat.format(endCalendar.getTime());
                    night.setEnd_date(nightEndDate);
                    night.calculateSleepTime();

                    night.reset();

                    // sleep to awake background animation
                    int sleepToAwakeTime = 1000;
                    animationView.setImageDrawable(sleep_to_awake);
                    sleep_to_awake.startTransition(sleepToAwakeTime);

                    // Change to "awake" when the button is checked
                    // 1 is fast, 2 is slow
                    fadeOutAnimation(clockBackground, 1);
                    fadeOutAnimation(buttonConnect, 1);
                    zoomInAnimation(view);
                    clockBackground.setBackgroundResource(R.drawable.clock_background_awake);
                    buttonMonitor.setBackgroundResource(R.drawable.awake);
                    fadeInAnimation(clockBackground, 1);
                    fadeInAnimation(buttonConnect, 1);

                    buttonDisconnect.setEnabled(true);
                    buttonSearch.setEnabled(true);
                    buttonGetSleepReport.setEnabled(true);

                    if (!events.isEmpty()) {
						/*
						bpmMonitored = bpm;
						cropBpmArray(bpmMonitored);
						apneaEvents = checkApneaEvents(bpmMonitored, APNEA_THRESHOLD);

						 */

                        cropEventArray(events);
                        night.setApneaEventsNumber(checkApneaEvents(events, 20));

                        for (Event event : events) {
                            //Toast.makeText(BioLibTestActivity.this, "entrou no for " + nightEndDate, Toast.LENGTH_SHORT).show();
                            dbHandler.addEvent(event);
                        }

                        dbHandler.addNight(night);


                        Toast.makeText(fragmentContext, "Sleep monitoring stopped at " + nightEndDate, Toast.LENGTH_SHORT).show();
                    } else
                        Toast.makeText(fragmentContext, "Empty array", Toast.LENGTH_SHORT).show();

                } else {/** START MONITORING ACTION */
                    nightStartCalendar = Calendar.getInstance();
                    nightStartDate = fullDateFormat.format(nightStartCalendar.getTime());
                    night.setStart_date(nightStartDate);
                    lastNightID = dbHandler.getLastNightID();

                    zoomInAnimation(view);

                    nightStartCalendar = Calendar.getInstance();
                    nightStartDate = fullDateFormat.format(nightStartCalendar.getTime());
                    night.setStart_date(nightStartDate);
                    lastNightID = dbHandler.getLastNightID();

                    // awake to sleep background animation
                    Animation fadeInSlow = AnimationUtils.loadAnimation(fragmentContext, R.anim.fade_in_slow);
                    Animation fadeOutSlow = AnimationUtils.loadAnimation(fragmentContext, R.anim.fade_out_slow);
                    int awakeToSleepTime = 1500;
                    animationView.setImageDrawable(awake_to_sleep);
                    awake_to_sleep.startTransition(awakeToSleepTime);

                    // Change to "sleep" when the button is checked
                    fadeOutAnimation(clockBackground, 2);
                    fadeOutAnimation(buttonConnect, 2);
                    zoomInAnimation(view);
                    clockBackground.setBackgroundResource(R.drawable.clock_background_sleep);
                    buttonMonitor.setBackgroundResource(R.drawable.sleep);
                    fadeInAnimation(clockBackground, 2);
                    fadeInAnimation(buttonConnect, 2);

                    buttonDisconnect.setEnabled(false);
                    buttonSearch.setEnabled(false);
                    buttonGetSleepReport.setEnabled(false);

                    bpm.clear();


                    Toast.makeText(fragmentContext, "Sleep monitoring started", Toast.LENGTH_SHORT).show();

                }

                isMonitoring = !isMonitoring;
                Log.d("YourTag", "ToggleButton clicked!");
            }


            private void zoomInAnimation(View view) {
                Animation zoomIn = AnimationUtils.loadAnimation(fragmentContext, R.anim.zoomy);
                view.startAnimation(zoomIn);
            }

            private void fadeInAnimation(View view, int time) {
                // time = 1 is fast, time = 2 is slow
                Animation fadeIn;
                switch (time) {
                    case 1:
                        fadeIn = AnimationUtils.loadAnimation(fragmentContext, R.anim.fade_in_fast);
                        break;
                    case 2:
                        fadeIn = AnimationUtils.loadAnimation(fragmentContext, R.anim.fade_in_slow);
                        break;
                    default:
                        fadeIn = AnimationUtils.loadAnimation(fragmentContext, R.anim.fade_in_fast);
                }
                view.startAnimation(fadeIn);
            }

            private void fadeOutAnimation(View view, int time) {
                // time = 1 is fast, time = 2 is slow
                Animation fadeIn;
                switch (time) {
                    case 1:
                        fadeIn = AnimationUtils.loadAnimation(fragmentContext, R.anim.fade_out_fast);
                        break;
                    case 2:
                        fadeIn = AnimationUtils.loadAnimation(fragmentContext, R.anim.fade_out_slow);
                        break;
                    default:
                        fadeIn = AnimationUtils.loadAnimation(fragmentContext, R.anim.fade_out_fast);
                }
                view.startAnimation(fadeIn);
            }

        });

//         Get Sleep Report Button on Main Activity, goes to Sleep Report Activity
//         USE SYSTEM CLOCK
        buttonGetSleepReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(fragmentContext, "Getting Sleep report", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(fragmentContext, SleepReportActivity.class);

				/*
				// ========= Simulate Data - Apnea Night ======================
				ArrayList<Double> mockBpm = new ArrayList<Double>();
				ArrayList<Boolean> mockApneaEvents = new ArrayList<Boolean>();
				for (int i=0;i<(60*8+2);i++) {
					if ((i>60 & i<65))
						mockBpm.add(10.0+0.5*(i-60));
					else if (i>=65 & i<70)
						mockBpm.add(12.5-0.5*(i-65));
					else if ((i>200 & i<205))
						mockBpm.add(10.0+0.5*(i-200));
					else if (i>=205 & i<210)
						mockBpm.add(12.5-0.5*(i-205));
					else mockBpm.add(10.0);
				}
				for (double bpmEvent : mockBpm){
					if (bpmEvent > 10.0)
						mockApneaEvents.add(true);
					else mockApneaEvents.add(false);
				}

				// =============================================
				// ========= Simulate Data - Non Apnea Night ======================
				ArrayList<Double> noApneaBpm = new ArrayList<Double>();
				ArrayList<Boolean> noApneaEvents = new ArrayList<Boolean>();
				for (int i=0;i<(60*8+2);i++){
					noApneaBpm.add(10.0);
					noApneaEvents.add(false);
				}
				// ================================================================

				bpm = mockBpm;
				apneaEvents = mockApneaEvents;

				 */

                intent.putExtra("bpmList", bpmMonitored);
                intent.putExtra("apneaEvents", apneaEvents);
                intent.putExtra("startDate", startCalendar);
                intent.putExtra("endDate", endCalendar);

                startActivity(intent);
            }
        });

        // __________________________________________

//        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        // ###################################################
        // MACADDRESS:
        address = "00:23:FE:00:0B:59";
        // ###################################################


        text = (TextView) view.findViewById(R.id.lblStatus);
        text.setText("");


		/*
        textRTC = (TextView) findViewById(R.id.lblRTC);
    	textPUSH = (TextView) findViewById(R.id.lblButton);
    	textPULSE = (TextView) findViewById(R.id.lblPulse);
    	textBAT = (TextView) findViewById(R.id.lblBAT);
    	textDataReceived = (TextView) findViewById(R.id.lblData);
    	textSDCARD = (TextView) findViewById(R.id.lblSDCARD);
    	textACC = (TextView) findViewById(R.id.lblACC);
    	textHR = (TextView) findViewById(R.id.lblHR);
    	textECG = (TextView) findViewById(R.id.lblECG);
    	textDeviceId = (TextView) findViewById(R.id.lblDeviceId);
    	textRadioEvent = (TextView) findViewById(R.id.textRadioEvent);
    	textTimeSpan  = (TextView) findViewById(R.id.lblTimeSpan);

		 */

        try {
            lib = new BioLib(fragmentContext, mHandler);
            text.append("Init BioLib \n");
        } catch (Exception e) {
            text.append("Error to init BioLib \n");
            e.printStackTrace();
        }


        buttonConnect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Connect();
            }

            /***
             * Connect to device.
             */
            private void Connect() {
                try {
                    deviceToConnect = lib.mBluetoothAdapter.getRemoteDevice(address);

                    Reset();

                    text.setText("");
                    lib.Connect(address, 5);
                } catch (Exception e) {
                    text.setText("Error to connect device: " + address);
                    e.printStackTrace();
                }
            }

        });


        buttonDisconnect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Disconnect();
            }
        });

		/*
        buttonSetRTC = (Button) findViewById(R.id.buttonSetRTC);
        buttonSetRTC.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View view)
            {
            	try
            	{
            		Date date = new Date();
					lib.SetRTC(date);
				}
            	catch (Exception e)
				{
					e.printStackTrace();
				}
            }
        });

        buttonGetRTC = (Button) findViewById(R.id.buttonGetRTC);
        buttonGetRTC.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View view)
            {
            	try
            	{
					lib.GetRTC();
				}
            	catch (Exception e)
				{
					e.printStackTrace();
				}
            }
        });

        buttonRequest = (Button) findViewById(R.id.buttonRequestData);
        buttonRequest.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View view)
            {
            	RequestData();
            }

			private void RequestData()
			{
				try
				{
					deviceToConnect =  lib.mBluetoothAdapter.getRemoteDevice(address);

					Reset();
					text.setText("");
					lib.Request(address, 30);
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
				}
			}
        });

		 */


        buttonSearch.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Search(view);
            }

            /*
             * Search for bluetooth devices.
             */
            private void Search(View view) {
                try {
                    Intent myIntent = new Intent(view.getContext(), SearchDeviceActivity.class);
                    startActivityForResult(myIntent, 0);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });


		/*
        buttonSetLabel = (Button) findViewById(R.id.buttonSetLabel);
        buttonSetLabel.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View view)
            {
            	try
            	{
            		/*
            		// SAMPLE 1: Sample of radio event: send array of bytes (10Bytes maximum)
            		byte type = 1;
            		// Maximum 10 bytes to send device [Optional]
            		byte[] info = new byte[4];
            		info[0] = 0x31; // 1 ascii table
            		info[1] = 0x32; // 2 ascii table
            		info[2] = 0x33; // 3 ascii table
            		info[3] = 0x34; // 4 ascii table

            		textRadioEvent.setText("Start send");
					if (lib.SetBytesToRadioEvent(type, info))
					{
						countEvent++;
						textRadioEvent.setText("REvent: " + countEvent);
					}
					else
						textRadioEvent.setText("Error");

		 */

					/*
            		// SAMPLE 2: Sample of radio event: send string (10 char maximum)
					byte type = 2;
					String info = "5678";
					textRadioEvent.setText("Start send");
					if (lib.SetStringToRadioEvent(type, info))
					{
						countEvent++;
						textRadioEvent.setText("REvent: " + countEvent);
					}
					else
						textRadioEvent.setText("Error");

				}
            	catch (Exception e)
				{
					e.printStackTrace();
				}
            }
        });
*/
        /*
        buttonGetDeviceId = (Button) findViewById(R.id.buttonGetDeviceId);
        buttonGetDeviceId.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View view)
            {
            	try
            	{
					lib.GetDeviceId();
				}
            	catch (Exception e)
				{
					e.printStackTrace();
				}
            }
        });

        buttonGetAcc = (Button) findViewById(R.id.buttonGetAcc);
        buttonGetAcc.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View view)
            {
            	try
            	{
					lib.GetAccSensibility();
				}
            	catch (Exception e)
				{
					e.printStackTrace();
				}
            }
        });

         */

        buttonConnect.setEnabled(true);
        //buttonRequest.setEnabled(true);
        buttonDisconnect.setEnabled(false);
        //buttonGetRTC.setEnabled(false);
        //buttonSetRTC.setEnabled(false);
        //buttonSetLabel.setEnabled(false);
        //buttonGetDeviceId.setEnabled(false);
        //buttonGetAcc.setEnabled(false);


        return view;
    }

    /***
     * Disconnect from device.
     */
    private void Disconnect() {
        try {
            lib.Disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Reset();
        }
    }

    /***
     * Reset variables and UI.
     */
    private void Reset() {
        try {
			/*
			textBAT.setText("BAT: - - %");
			textPULSE.setText("PULSE: - - bpm");
			textPUSH.setText("PUSH-BUTTON: - - - ");
			textRTC.setText("RTC: - - - ");
			textDataReceived.setText("RECEIVED: - - - ");
			textACC.setText("ACC:  X: - -  Y: - -  Z: - -");
			textSDCARD.setText("SD CARD STATUS: - - ");
			textECG.setText("Ecg stream: -- ");
			textHR.setText("PEAK: --  BPMi: -- bpm  BPM: -- bpm  R-R: -- ms");
			textBAT.setText("BAT: -- %");
			textPULSE.setText("HR: -- bpm     Nb. Leads: -- ");
			textDeviceId.setText("Device Id: - - - - - - - - - -");
			textRadioEvent.setText(".");
			textTimeSpan.setText("SPAN: - - - ");

			SDCARD_STATE = 0;
			BATTERY_LEVEL = 0;
			PULSE = 0;
			DATETIME_PUSH_BUTTON = null;
			DATETIME_RTC = null;
			DATETIME_TIMESPAN = null;
			numOfPushButton = 0;
			countEvent = 0;
			accConf = "";
			firmwareVersion = "";

			 */
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    /**
     * The Handler that gets information back from the BioLib
     */
    /**
     * The Handler that gets information back from the BioLib
     */
    public void OnDestroy() {
        if (isConn) {
            Disconnect();
        }
    }

    public void onDestroy() {
        super.onDestroy();

        if (lib.mBluetoothAdapter != null) {
            try {
                if (ActivityCompat.checkSelfPermission(fragmentContext, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                lib.mBluetoothAdapter.cancelDiscovery();
            } catch (Exception e) {
                System.out.println("PAM PAM");
            }
        }

        lib = null;
    }

    /**
     * //	 * The Handler that gets information back from the BioLib
     * //
     */
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BioLib.MESSAGE_READ:
                    //textDataReceived.setText("RECEIVED: " + msg.arg1);
                    break;

                case BioLib.MESSAGE_DEVICE_NAME:
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(fragmentContext, "Connected to " + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    text.append("Connected to " + mConnectedDeviceName + " \n");
                    break;

                case BioLib.MESSAGE_BLUETOOTH_NOT_SUPPORTED:
                    Toast.makeText(fragmentContext, "Bluetooth NOT supported. Aborting! ", Toast.LENGTH_SHORT).show();
                    text.append("Bluetooth NOT supported. Aborting! \n");
                    isConn = false;
                    break;

                case BioLib.MESSAGE_BLUETOOTH_ENABLED:
                    Toast.makeText(fragmentContext, "Bluetooth is now enabled! ", Toast.LENGTH_SHORT).show();
                    text.append("Bluetooth is now enabled \n");
                    text.append("Macaddress selected: " + address + " \n");
                    buttonConnect.setEnabled(true);
                    //buttonRequest.setEnabled(true);
                    break;

                case BioLib.MESSAGE_BLUETOOTH_NOT_ENABLED:
                    Toast.makeText(fragmentContext, "Bluetooth not enabled! ", Toast.LENGTH_SHORT).show();
                    text.append("Bluetooth not enabled \n");
                    isConn = false;
                    break;

                case BioLib.REQUEST_ENABLE_BT:
                    Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableIntent, BioLib.REQUEST_ENABLE_BT);
                    text.append("Request bluetooth enable \n");
                    break;

                case BioLib.STATE_CONNECTING:
                    text.append("   Connecting to device ... \n");
                    break;

                case BioLib.STATE_CONNECTED:
                    Toast.makeText(fragmentContext, "Connected to " + deviceToConnect.getName(), Toast.LENGTH_SHORT).show();
                    text.append("   Connect to " + deviceToConnect.getName() + " \n");
                    isConn = true;

                    buttonConnect.setEnabled(false);
                    //buttonRequest.setEnabled(false);
                    buttonDisconnect.setEnabled(true);
                    //buttonGetRTC.setEnabled(true);
                    //buttonSetRTC.setEnabled(true);
                    //buttonSetLabel.setEnabled(true);
                    //buttonGetDeviceId.setEnabled(true);
                    //buttonGetAcc.setEnabled(true);

                    break;

                case BioLib.UNABLE_TO_CONNECT_DEVICE:
                    Toast.makeText(fragmentContext, "Unable to connect device! ", Toast.LENGTH_SHORT).show();
                    text.append("   Unable to connect device \n");
                    isConn = false;

                    buttonConnect.setEnabled(true);
                    //buttonRequest.setEnabled(true);
                    buttonDisconnect.setEnabled(false);
                    //buttonGetRTC.setEnabled(false);
                    //buttonSetRTC.setEnabled(false);
                    //buttonSetLabel.setEnabled(false);
                    //buttonGetDeviceId.setEnabled(false);
                    //buttonGetAcc.setEnabled(false);

                    break;

                case BioLib.MESSAGE_DISCONNECT_TO_DEVICE:
                    Toast.makeText(fragmentContext, "Device connection was lost", Toast.LENGTH_SHORT).show();
                    text.append("   Disconnected from " + deviceToConnect.getName() + " \n");
                    isConn = false;


                    buttonConnect.setEnabled(true);
                    //buttonRequest.setEnabled(true);
                    buttonDisconnect.setEnabled(false);
                    //buttonGetRTC.setEnabled(false);
                    //buttonSetRTC.setEnabled(false);
                    //buttonSetLabel.setEnabled(false);
                    //buttonGetDeviceId.setEnabled(false);
                    //buttonGetAcc.setEnabled(false);

                    break;

                case BioLib.MESSAGE_PUSH_BUTTON:
                    DATETIME_PUSH_BUTTON = (Date) msg.obj;
                    numOfPushButton = msg.arg1;
                    //textPUSH.setText("PUSH-BUTTON: [#" + numOfPushButton + "]" + DATETIME_PUSH_BUTTON.toString());
                    break;

                case BioLib.MESSAGE_RTC:
                    DATETIME_RTC = (Date) msg.obj;
                    //textRTC.setText("RTC: " + DATETIME_RTC.toString());
                    break;

                case BioLib.MESSAGE_TIMESPAN:
                    DATETIME_TIMESPAN = (Date) msg.obj;
                    //textTimeSpan.setText("SPAN: " + DATETIME_TIMESPAN.toString());
                    break;

                case BioLib.MESSAGE_DATA_UPDATED:
                    BioLib.Output out = (BioLib.Output) msg.obj;
                    BATTERY_LEVEL = out.battery;
                    //textBAT.setText("BAT: " + BATTERY_LEVEL + " %");
                    PULSE = out.pulse;
                    //textPULSE.setText("HR: " + PULSE + " bpm     Nb. Leads: " + lib.GetNumberOfChannels());
                    break;

                case BioLib.MESSAGE_SDCARD_STATE:

					/*
	            	SDCARD_STATE = (int)msg.arg1;

	            	if (SDCARD_STATE == 1)
	            		textSDCARD.setText("SD CARD STATE: ON");
	            	else
	            		textSDCARD.setText("SD CARD STATE: OFF");

					 */
                    break;

                case BioLib.MESSAGE_RADIO_EVENT:
	            	/*textRadioEvent.setText("Radio-event: received ... ");

	            	typeRadioEvent = (byte)msg.arg1;
	            	infoRadioEvent = (byte[]) msg.obj;

	            	String str = "";
					try {
						str = new String(infoRadioEvent, "UTF8");
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
	            	textRadioEvent.setText("Radio-event: " + typeRadioEvent + "[" + str + "]");

	            	 */

                    break;

                case BioLib.MESSAGE_FIRMWARE_VERSION:

					/*
	            	// Show firmware version in device VitalJacket ...
	            	firmwareVersion = (String)msg.obj;

					 */
                    break;

                case BioLib.MESSAGE_DEVICE_ID:

					/*
	            	deviceId = (String)msg.obj;
	            	textDeviceId.setText("Device Id: " + deviceId);

					 */

                    break;

                case BioLib.MESSAGE_ACC_SENSIBILITY:

					/*
	            	accSensibility = (byte)msg.arg1;
	            	accConf = "4G";
	            	switch (accSensibility)
	            	{
	            		case 0:
	            			accConf = "2G";
	            			break;

	            		case 1:
	            			accConf = "4G";
	            			break;
	            	}

	            	textACC.setText("ACC [" + accConf + "]:  X: " + dataACC.X + "  Y: " + dataACC.Y + "  Z: " + dataACC.Z);

					 */

                    break;

                case BioLib.MESSAGE_PEAK_DETECTION:
                    BioLib.QRS qrs = (BioLib.QRS) msg.obj;


                    /** EDITED CODE STARTS HERE*/
                    if (event_span < EVENT_SPAN) { //checks if the duration of the event hasn't overcome the EVENT_SPAN
                        eventStartCalendar = Calendar.getInstance();
                        eventStartDate = eventDateFormat.format(eventStartCalendar.getTime());
                        eventBpmi.add(qrs.bpmi);
                        peak_number++;
                        event_span += qrs.rr;
                    } else { //An event has completed and the mean bpm for that event is calculated (in minutes)
                        meanBpm = calculateMean(eventBpmi);
                        bpm.add(meanBpm);
                        event_span = 0;
                        peak_number = 0;
                        eventBpmi.clear();
                        Toast.makeText(fragmentContext, Double.toString(peak_number / (EVENT_SPAN * 1000.0 * 60)), Toast.LENGTH_SHORT).show();

                        event = new Event(meanBpm, eventStartDate, nightStartDate);
                        events.add(event);
                    }

                    //textViewTestBPM.setText("Peak number: " + peak_number + "; Event duration: " + event_span);
                    textViewTestBPM.setText("PEAK: " + qrs.position + "  BPMi: " + qrs.bpmi + " bpm  BPM: " + qrs.bpm + " bpm  R-R: " + qrs.rr + " ms; Array size: " + bpm.size());

                    /** EDITED CODE ENDS HERE*/


                    //textHR.setText("PEAK: " + qrs.position + "  BPMi: " + qrs.bpmi + " bpm  BPM: " + qrs.bpm + " bpm  R-R: " + qrs.rr + " ms");
                    break;

                case BioLib.MESSAGE_ACC_UPDATED:

					/*
	            	dataACC = (BioLib.DataACC)msg.obj;

	            	if (accConf == "")
	            		textACC.setText("ACC:  X: " + dataACC.X + "  Y: " + dataACC.Y + "  Z: " + dataACC.Z);
	            	else
	            		textACC.setText("ACC [" + accConf + "]:  X: " + dataACC.X + "  Y: " + dataACC.Y + "  Z: " + dataACC.Z);

					 */


                    break;

                case BioLib.MESSAGE_ECG_STREAM:

					/*
	            	try
	            	{
	            		textECG.setText("ECG received");
	            		ecg = (byte[][]) msg.obj;
	            		int nLeads = ecg.length;
	            		nBytes = ecg[0].length;
	            		textECG.setText("ECG stream: OK   nBytes: " + nBytes + "   nLeads: " + nLeads);
	            	}

	            	catch (Exception ex)
	            	{
	            		textECG.setText("ERROR in ecg stream");
	            	}

					 */

                    break;

                case BioLib.MESSAGE_TOAST:
                    Toast.makeText(getContext(), msg.getData().getString(TOAST), Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    public static double calculateMean(ArrayList<Integer> arrayList) {
        if (arrayList == null || arrayList.isEmpty()) {
            throw new IllegalArgumentException("Input ArrayList is null or empty");
        }

        int sum = 0;
        for (int number : arrayList) {
            sum += number;
        }

        return (double) sum / arrayList.size();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case BioLib.REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(fragmentContext, "Bluetooth is now enabled! ", Toast.LENGTH_SHORT).show();
                    text.append("Bluetooth is now enabled \n");

                    buttonConnect.setEnabled(true);
                    //buttonRequest.setEnabled(true);
                    buttonDisconnect.setEnabled(false);
                    //buttonGetRTC.setEnabled(false);
                    //buttonSetRTC.setEnabled(false);
                    //buttonSetLabel.setEnabled(false);
                    //buttonGetDeviceId.setEnabled(false);
                    //buttonGetAcc.setEnabled(false);

                    text.append("Macaddress selected: " + address + " \n");
                } else {
                    Toast.makeText(fragmentContext, "Bluetooth not enabled! ", Toast.LENGTH_SHORT).show();
                    text.append("Bluetooth not enabled \n");
                    isConn = false;


                    buttonConnect.setEnabled(true);
                    //buttonRequest.setEnabled(true);
                    buttonDisconnect.setEnabled(false);
                    //buttonGetRTC.setEnabled(false);
                    //buttonSetRTC.setEnabled(false);
                    //buttonSetLabel.setEnabled(false);
                    //buttonGetDeviceId.setEnabled(false);
                    //buttonGetAcc.setEnabled(false);

                }
                break;

            case 0:
                switch (resultCode) {
                    case SearchDeviceActivity.CHANGE_MACADDRESS:
                        try {
                            text.append("\nSelect new macaddress: ");
                            macaddress = data.getExtras().getString(SearchDeviceActivity.SELECT_DEVICE_ADDRESS);
                            Toast.makeText(fragmentContext, macaddress, Toast.LENGTH_SHORT).show();

                            text.append(macaddress);

                            address = macaddress;
                        } catch (Exception ex) {
                            Toast.makeText(fragmentContext, "ERROR: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        break;
                }
                break;
        }
    }

    //checkApneaEvent() returns true if for an event, a consecutive number of samples exceeds a threshold
    public static int checkApneaEvents(ArrayList<Event> bpmList, int threshold) {

        int apneaEventsNumber = 0;
        ArrayList<Boolean> apneaEvents = new ArrayList<Boolean>();
        double median = calculateEventBpmMedian(bpmList);
        for (int i = 0; i < bpmList.size(); i++) {
            if (bpmList.get(i).getType() == null) {
                if (bpmList.get(i).getBpm() > median + threshold) {
                    bpmList.get(i).setType("apnea");
                    apneaEventsNumber++;
                } else bpmList.get(i).setType("normal");
            }
        }
        return apneaEventsNumber;
    }

    public static void cropBpmArray(ArrayList<Double> bpmList) {

        double median = calculateMedian(bpmList);

        // Remove the first values until a sample is lesser than the median
        while (!bpmList.isEmpty() && bpmList.get(0) >= median) {
            bpmList.remove(0);
        }

        // Remove the last values until a sample is lesser than the median
        while (!bpmList.isEmpty() && bpmList.get(bpmList.size() - 1) >= median) {
            bpmList.remove(bpmList.size() - 1);
        }
    }

    public static void cropEventArray(ArrayList<Event> bpmList) {

        double median = calculateEventBpmMedian(bpmList);
        int i = 0;
        // Remove the first values until a sample is lesser than the median
        while (bpmList.get(i).getBpm() > median) {
            bpmList.get(i).setType("Falling asleep");
            i++;
        }

        i = 0;
        // Remove the last values until a sample is lesser than the median
        while (bpmList.get(bpmList.size() - 1 - i).getBpm() > median) {
            bpmList.get(bpmList.size() - 1 - i).setType("Awakening");
            i++;
        }
    }

    public static double calculateMedian(ArrayList<Double> numbers) {
        // Check for empty list
        if (numbers == null || numbers.isEmpty()) {
            throw new IllegalArgumentException("The list is empty");
        }

        // Sort the ArrayList
        ArrayList<Double> sorted_numbers = new ArrayList<Double>(numbers);
        Collections.sort(sorted_numbers);

        int size = sorted_numbers.size();
        double median;

        if (size % 2 == 0) {
            // If the size is even, average the two middle elements
            double middle1 = sorted_numbers.get(size / 2 - 1);
            double middle2 = sorted_numbers.get(size / 2);
            median = (middle1 + middle2) / 2.0;
        } else {
            // If the size is odd, take the middle element
            median = sorted_numbers.get(size / 2);
        }

        return median;
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