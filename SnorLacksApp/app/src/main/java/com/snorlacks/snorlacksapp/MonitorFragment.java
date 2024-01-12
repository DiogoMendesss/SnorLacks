package com.snorlacks.snorlacksapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
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
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

import Bio.Library.namespace.BioLib;

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
    private TextView textViewTestBPM;

    private Button buttonBluetooth;

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

    /** EDITED CODE STARTS HERE */
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
    private boolean isConnected = false;

    private ToggleButton buttonMonitor;
    private android.widget.ImageButton buttonVJ;
    private ImageView iconStatusVJ;
    private View cableStatusVJ;
    private TextView clickLabelMonitor;
    private TextView clickLabelVJ;

    private View clockBackground;
    private ImageView imageClock;

    public Calendar nightStartCalendar;
    public Calendar eventStartCalendar;
    public Calendar endCalendar;

    private String nightStartDate;
    private String nightStartTime;
    private String eventStartDate;
    private String nightEndTime;

    private SimpleDateFormat sleepTimeFormat = new SimpleDateFormat("h:mm a");
    private SimpleDateFormat fullDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private SimpleDateFormat justDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private SimpleDateFormat eventDateFormat = new SimpleDateFormat("HH:mm:ss");

    private Night night = new Night();
    private int lastNightID;
    private Event event = new Event();

    public MonitorFragment() {
        // Required empty public constructor
    }
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        fragmentContext = context;
    }
    public static MonitorFragment newInstance(String param1, String param2) {
        return new MonitorFragment();

    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_monitor, container, false);

        textViewTestBPM = view.findViewById(R.id.txtViewTestBPM);
        buttonMonitor = view.findViewById(R.id.btnMonitor);
        buttonVJ = (android.widget.ImageButton) view.findViewById(R.id.buttonVJ);
        buttonBluetooth = (Button) view.findViewById(R.id.buttonBluetooth);
        iconStatusVJ = (ImageView) view.findViewById(R.id.iconStatusVJ);
        cableStatusVJ = (View) view.findViewById(R.id.cableStatusVJ);

        ConstraintLayout mainLayout = view.findViewById(R.id.mainLayout);
        ImageView animationView = view.findViewById(R.id.animationView);
        imageClock = view.findViewById(R.id.imageClock);
        clockBackground = view.findViewById(R.id.clockBackground);
        clickLabelMonitor = view.findViewById(R.id.clickLabelMonitor);
        clickLabelVJ = view.findViewById(R.id.clickLabelVJ);

        TransitionDrawable sleep_to_awake = new TransitionDrawable(new Drawable[]{
                ContextCompat.getDrawable(fragmentContext, R.drawable.sleep_background),
                ContextCompat.getDrawable(fragmentContext, R.drawable.awake_background)});
        TransitionDrawable awake_to_sleep = new TransitionDrawable(new Drawable[]{
                ContextCompat.getDrawable(fragmentContext, R.drawable.awake_background),
                ContextCompat.getDrawable(fragmentContext, R.drawable.sleep_background)});

        //  Database stuff
        DBHandler dbHandler = DBHandler.getInstance(fragmentContext);
        SQLiteDatabase db = dbHandler.getWritableDatabase();
        textViewTestBPM.setText("Last night ID: " + dbHandler.getLastNightID());

        //  Sleep monitoring (start, stop, data)
        buttonMonitor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (isMonitoring) { // STOP MONITORING ACTION

                    endCalendar = Calendar.getInstance();
                    nightEndTime = sleepTimeFormat.format(endCalendar.getTime());
                    night.setEnd_time(nightEndTime);
                    night.calculateSleepTime();
                    //Toast.makeText(fragmentContext, "sleep time " + night.getSleep_time(), Toast.LENGTH_SHORT).show();
                    night.reset();

                    // sleep to awake background animation
                    int sleepToAwakeTime = 1000;
                    animationView.setImageDrawable(sleep_to_awake);
                    sleep_to_awake.startTransition(sleepToAwakeTime);

                    // Change to "awake" when the button is checked (1 is fast, 2 is slow)
                    fadeOutAnimation(clockBackground, 2);
                    fadeOutAnimation(buttonMonitor, 2);
                    clickZoomAnimation(buttonMonitor);
                    clockBackground.setBackgroundResource(R.drawable.clock_background_awake);
                    buttonMonitor.setBackgroundResource(R.drawable.awake);
                    fadeInAnimation(clockBackground, 2);
                    fadeInAnimation(buttonMonitor, 1);

                    // Button enabling/disabling
                    buttonVJ.setEnabled(true);
                    buttonBluetooth.setEnabled(true);

                    if (!events.isEmpty()) {
						/*
						bpmMonitored = bpm;
						cropBpmArray(bpmMonitored);
						apneaEvents = checkApneaEvents(bpmMonitored, APNEA_THRESHOLD);
						 */
                        cropEventArray(events);
                        night.setApneaEventsNumber(checkApneaEvents(events, 20));

                        for (Event event : events) {
                            dbHandler.addEvent(event);
                        }
                        dbHandler.addNight(night);

                        Toast.makeText(fragmentContext, "Sleep monitoring stopped at " + nightEndTime, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(fragmentContext, "Empty array at time: " + sleepTimeFormat.format(endCalendar.getTime()), Toast.LENGTH_SHORT).show();
                    }
                } else {    /* START MONITORING ACTION */
                    nightStartCalendar = Calendar.getInstance();
                    nightStartDate = justDateFormat.format(nightStartCalendar.getTime());
                    nightStartTime = sleepTimeFormat.format(nightStartCalendar.getTime());
                    night.setStart_date(nightStartDate);
                    night.setStart_time(nightStartTime);
                    lastNightID = dbHandler.getLastNightID();

                    // awake to sleep background animation
                    int awakeToSleepTime = 1500;
                    animationView.setImageDrawable(awake_to_sleep);
                    awake_to_sleep.startTransition(awakeToSleepTime);

                    // Change to "sleep" when the button is checked
                    fadeOutAnimation(clockBackground, 1);
                    fadeOutAnimation(buttonMonitor, 1);
                    clickZoomAnimation(buttonMonitor);
                    clockBackground.setBackgroundResource(R.drawable.clock_background_sleep);
                    buttonMonitor.setBackgroundResource(R.drawable.sleep);
                    fadeInAnimation(clockBackground, 1);
                    fadeInAnimation(buttonMonitor, 1);

                    // Button enabling/disabling
                    buttonVJ.setEnabled(false);
                    buttonBluetooth.setEnabled(false);
                    clickLabelMonitor.setVisibility(View.GONE);

                    bpm.clear();

                    Toast.makeText(fragmentContext, "Sleep monitoring started", Toast.LENGTH_SHORT).show();
                }
                isMonitoring = !isMonitoring;
            }
        });
        buttonVJ.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                zoomInAnimation(view);
                if (isConnected) {
                    Disconnect();
                } else {
                    Connect();
                    clickLabelVJ.setVisibility(View.GONE);
                }
                isConnected = !isConnected;
            }
            /*
                Connect to vital jacket device
            */
            private void Connect() {
                try {
                    deviceToConnect = lib.mBluetoothAdapter.getRemoteDevice(address);

                    Reset();

                    text.setText("");
                    lib.Connect(address, 5);

                    iconStatusVJ.setBackgroundResource(R.drawable.active);
                    cableStatusVJ.setBackgroundResource(R.drawable.vj_connection_active);

                } catch (Exception e) {
                    text.setText("Error to connect device: " + address);
                    e.printStackTrace();
                    iconStatusVJ.setBackgroundResource(R.drawable.inactive);
                    cableStatusVJ.setBackgroundResource(R.drawable.vj_connection_inactive);
                }
            }

        });
        buttonBluetooth.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                zoomInAnimation(view);
                Search(view);
            }

            // Search for bluetooth devices.
            private void Search(View view) {
                try {
                    Intent myIntent = new Intent(view.getContext(), SearchDeviceActivity.class);
                    startActivityForResult(myIntent, 0);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        buttonVJ.setEnabled(true);
        buttonBluetooth.setEnabled(true);


        // MAC_ADDRESS:
        address = "00:23:FE:00:0B:59";

        text = (TextView) view.findViewById(R.id.lblStatus);
        text.setText("");

        try {
            lib = new BioLib(fragmentContext, mHandler);
            text.append("Init BioLib \n");
        } catch (Exception e) {
            text.append("Error to init BioLib \n");
            e.printStackTrace();
        }

        return view;
    }

    // Disconnect from device.
    private void Disconnect() {
        try {
            lib.Disconnect();
            iconStatusVJ.setBackgroundResource(R.drawable.inactive);
            cableStatusVJ.setBackgroundResource(R.drawable.vj_connection_inactive);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Reset();
        }
    }

    // Reset variables and UI.
    private void Reset() {
        try {
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
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

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
                    // Permission check
                    return;
                }
                lib.mBluetoothAdapter.cancelDiscovery();
            } catch (Exception e) {
                System.out.println("PAM PAM");
            }
        }
        lib = null;
    }

    // The Handler that gets information back from the BioLib
    @SuppressLint("HandlerLeak")
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
                    buttonVJ.setEnabled(true);
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

                    buttonVJ.setEnabled(false);

                    break;

                case BioLib.UNABLE_TO_CONNECT_DEVICE:
                    Toast.makeText(fragmentContext, "Unable to connect device! ", Toast.LENGTH_SHORT).show();
                    text.append("   Unable to connect device \n");
                    isConn = false;

                    buttonVJ.setEnabled(true);

                    break;

                case BioLib.MESSAGE_DISCONNECT_TO_DEVICE:
                    Toast.makeText(fragmentContext, "Device connection was lost", Toast.LENGTH_SHORT).show();
                    text.append("   Disconnected from " + deviceToConnect.getName() + " \n");
                    isConn = false;


                    buttonVJ.setEnabled(true);

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


                    /** EDITED CODE STARTS HERE */
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
                    textViewTestBPM.setText("PEAK: " + qrs.position + "  BPMi: " + qrs.bpmi + " bpm  BPM: " + qrs.bpm +
                            " bpm  R-R: " + qrs.rr + " ms; Array size: " + bpm.size());
                    /** EDITED CODE ENDS HERE */

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

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case BioLib.REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(fragmentContext, "Bluetooth is now enabled! ", Toast.LENGTH_SHORT).show();
                    text.append("Bluetooth is now enabled \n");

                    buttonVJ.setEnabled(true);

                    text.append("Macaddress selected: " + address + " \n");
                } else {
                    Toast.makeText(fragmentContext, "Bluetooth not enabled! ", Toast.LENGTH_SHORT).show();
                    text.append("Bluetooth not enabled \n");
                    isConn = false;


                    buttonVJ.setEnabled(true);

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

    public static int checkApneaEvents(ArrayList<Event> bpmList, int threshold) {
        //checkApneaEvent() returns true if for an event, a consecutive number of samples exceeds a threshold
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

    private void zoomInAnimation(View view) {
        view.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(80)
                .start();
    }
    private void zoomOutAnimation(View view) {
        view.animate()
                .scaleX(1.03f)
                .scaleY(1.03f)
                .setDuration(80)
                .start();
    }
    private void clickZoomAnimation(View view) {
        view.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        // Zoom-out animation after the zoom-in animation completes
                        buttonMonitor.animate()
                                .scaleX(1.0f)
                                .scaleY(1.0f)
                                .setDuration(30)
                                .start();
                    }
                })
                .start();
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
        Animation fadeOut;
        switch (time) {
            case 1:
                fadeOut = AnimationUtils.loadAnimation(fragmentContext, R.anim.fade_out_fast);
                break;
            case 2:
                fadeOut = AnimationUtils.loadAnimation(fragmentContext, R.anim.fade_out_slow);
                break;
            default:
                fadeOut = AnimationUtils.loadAnimation(fragmentContext, R.anim.fade_out_fast);
        }
        view.startAnimation(fadeOut);
    }
}