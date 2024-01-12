package com.snorlacks.snorlacksapp;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.Set;


public class SearchDeviceActivity extends Activity {
    public static String SELECT_DEVICE_ADDRESS = "device_address";
    public static final int CHANGE_MACADDRESS = 100;
    private static final int REQUEST_BLUETOOTH_PERMISSION = 1; // You can use any integer value

    private ListView mainListView;
    private ArrayAdapter<String> listAdapter;
    private String selectedValue = "";
    private BluetoothAdapter mBluetoothAdapter = null;
    private Button buttonOK;

    /**
     * @return
     */
    public String GetMacAddress() {
        return selectedValue;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search);

        buttonOK = (Button) findViewById(R.id.cmdOK);
        buttonOK.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent();
                zoomInAnimation(view);
                intent.putExtra(SELECT_DEVICE_ADDRESS, selectedValue);

                // Set result and finish this Activity
                setResult(CHANGE_MACADDRESS, intent);
                finish();
            }

        });

        try {
            mainListView = (ListView) findViewById(R.id.lstDevices);

            ArrayList<String> lstDevices = new ArrayList<String>();

            // Create ArrayAdapter using the planet list.
            listAdapter = new ArrayAdapter<String>(this, R.layout.simplerow, lstDevices);

            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mBluetoothAdapter != null) {
                if (mBluetoothAdapter.isEnabled()) {
                    // Listing paired devices

                    // Check Bluetooth permissions
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        Log.e("BluetoothConnection", "BT permission denied");
                        ActivityCompat.requestPermissions(SearchDeviceActivity.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_BLUETOOTH_PERMISSION);
                    } else {
                        Log.e("BluetoothConnection", "BT permission granted");

                        // Continue with Bluetooth operations
                        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                        if (mBluetoothAdapter != null) {
                            if (mBluetoothAdapter.isEnabled()) {
                                // Listing paired devices
                                Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
                                for (BluetoothDevice device : devices) {
                                    listAdapter.add(device.getAddress() + "   " + device.getName());
                                }
                            }
                        }
                    }

				}
			}
			mainListView.setAdapter( listAdapter );

            mainListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
            {
                @Override
                public void onItemClick( AdapterView<?> parent, View item, int position, long id)
                {
                	selectedValue = (String) listAdapter.getItem(position);

                	String[] aux = selectedValue.split("   ");
                	selectedValue = aux[0];

                    String txtMessage = "Device '" + selectedValue + "' selected.";
                    Toast.makeText(SearchDeviceActivity.this, txtMessage, Toast.LENGTH_SHORT).show();
                }
            });
        }
        catch (Exception ex) {
            Toast.makeText(SearchDeviceActivity.this, "BT Device click exception", Toast.LENGTH_SHORT).show();
            Log.e("BT error", String.valueOf(ex));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.activity_search_device, menu);
        return true;
    }
    private void zoomInAnimation(View view) {
        view.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(80)
                .start();
    }
}
