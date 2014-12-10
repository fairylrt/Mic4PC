
package com.fairy.mic4pc;

import java.util.ArrayList;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class RoomListActivity extends Activity {

	private static final String TAG = "RoomListActivity";
	private BluetoothAdapter mBluetoothAdapter = null;
	private ArrayList<BluetoothDevice> mDevicesArrayList = new ArrayList<BluetoothDevice>();
	private ArrayAdapter<String> mDevicesArrayAdapter = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.e(TAG, "on create");
		// Setup the window
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.room_list);

		// Set result CANCELED incase the user backs out
		setResult(Activity.RESULT_CANCELED);

		// Initialize array adapters.
		mDevicesArrayAdapter = new ArrayAdapter<String>(this,
				R.layout.list_name);

		// Find and set up the ListView for paired devices
		ListView roomListView = (ListView) findViewById(R.id.devices);
		roomListView.setAdapter(mDevicesArrayAdapter);
		roomListView.setOnItemClickListener(mDeviceClickListener);

		// Register for broadcasts when a device is discovered
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		this.registerReceiver(mReceiver, filter);

		// Register for broadcasts when discovery has finished
		filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		this.registerReceiver(mReceiver, filter);

		// Get the local Bluetooth adapter
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		doDiscovery();

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.e(TAG, "on destroy");
		// Make sure we're not doing discovery anymore
		if (mBluetoothAdapter != null) {
			mBluetoothAdapter.cancelDiscovery();
		}

		// Unregister broadcast listeners
		this.unregisterReceiver(mReceiver);
	}

	/**
	 * Start device discover with the BluetoothAdapter
	 */
	private void doDiscovery() {
		Log.e(TAG, "do discovery");
		setProgressBarIndeterminateVisibility(true);
		setTitle(R.string.searching);

		// If we're already discovering, stop it
		if (mBluetoothAdapter.isDiscovering()) {
			mBluetoothAdapter.cancelDiscovery();
		}

		// Request discover from BluetoothAdapter
		mBluetoothAdapter.startDiscovery();
	}

	// The on-click listener for all devices in the ListView
	private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {

		public void onItemClick(AdapterView<?> parent, View v, int position,
				long id) {
			// Cancel discovery because it's costly and we're about to connect
			Log.e(TAG, "on item click listener");

			mBluetoothAdapter.cancelDiscovery();
			if (position < mDevicesArrayList.size()) {
				BluetoothDevice device = mDevicesArrayList.get(position);
				Intent intent = new Intent();
				intent.putExtra(CommConst.DEVICE_TO_CONNECT,
						(Parcelable) device);
				System.out.println(device.getName());
				// Set result and finish this Activity
				setResult(Activity.RESULT_OK, intent);
				finish();
			} else {
				doDiscovery();
				mDevicesArrayList.clear();
				mDevicesArrayAdapter.clear();
			}
		}
	};

	// The BroadcastReceiver that listens for discovered devices and
	// changes the title when discovery is finished
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.e(TAG, "broadcast receive");
			String action = intent.getAction();

			// When discovery finds a device
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				Log.e(TAG, "found one");
				// Get the BluetoothDevice object from the Intent
				BluetoothDevice device = intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				// If it's already paired, skip it, because it's been listed
				// already
				mDevicesArrayList.add(device);
				mDevicesArrayAdapter.add(device.getName().toString());
				// When discovery is finished, change the Activity title
			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED
					.equals(action)) {
				setProgressBarIndeterminateVisibility(false);
				setTitle(R.string.searched);
				mDevicesArrayAdapter.add(getResources().getText(
						R.string.search_again).toString());
			}
		}
	};

}
