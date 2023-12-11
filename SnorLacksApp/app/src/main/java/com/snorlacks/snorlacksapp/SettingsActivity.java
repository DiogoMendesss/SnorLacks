package com.snorlacks.snorlacksapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.navigation.NavigationBarView;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        NavigationBarView bottomNavigation = findViewById(R.id.bottomNavigation);

        // Load the default fragment (SleepReport)
        bottomNavigation.setOnItemReselectedListener(new NavigationBarView.OnItemReselectedListener() {
            @Override
            public void onNavigationItemReselected(@NonNull MenuItem item) {

                if (item.getItemId() == R.id.reportsMenu) {
                    startActivity(new Intent(SettingsActivity.this, SleepReportActivity.class));
                } else if (item.getItemId() == R.id.monitorMenu) {
                    startActivity(new Intent(SettingsActivity.this, BioLibTestActivity.class));
                } else if (item.getItemId() == R.id.settingsMenu) {
                    startActivity(new Intent(SettingsActivity.this, SettingsActivity.class));
                }
            }
        });
    }
}