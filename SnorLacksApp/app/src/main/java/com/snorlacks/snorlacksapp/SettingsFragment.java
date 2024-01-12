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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

import Bio.Library.namespace.BioLib;

public class SettingsFragment extends Fragment {

    private Context fragmentContext;


    public SettingsFragment() {
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
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        ListView listView = view.findViewById(R.id.infoSettings);

        // Example data (replace this with your actual data)
        String bulletOne = "1 • Pair smartphone with the vital jacket in android's bluetooth";
        String bulletTwo = "2 • Search and select the vital jacket in the bluetooth devices list (top left button)";
        String bulletThree = "3 • Click on the vital jacket button to connect (green status means connected)";
        String bulletFour = "4 • Click on the main monitor button to start monitoring";
        String[] listItems = {bulletOne, bulletTwo, bulletThree, bulletFour};

        // Create an ArrayAdapter and set it to the ListView
        ArrayAdapter<String> adapter = new ArrayAdapter<>(fragmentContext, R.layout.simplerow, listItems);

        listView.setAdapter(adapter);
        listView.setAdapter(adapter);


        return view;
    }




    public void onDestroy() {
        super.onDestroy();


    }

}