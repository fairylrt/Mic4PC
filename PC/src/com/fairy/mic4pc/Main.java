package com.fairy.mic4pc;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class Main extends Activity {

	private static final String TAG = "Main";

	private Button buttonConnect = null;
	private Button buttonDisconnect = null;
	private Button buttonTurnOn = null;
	private Button buttonTurnOff = null;
	private BluetoothAdapter mBluetoothAdapter = null;
	private BluetoothSender mBluetoothSender = null;
	private MyAudioRecord mAudioRecord = null;
	private BluetoothDevice rDevice = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		Log.e(TAG, "On Create");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		// 获得蓝牙适配器
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, R.string.no_bluetooth, Toast.LENGTH_LONG)
					.show();
			return;
		}
		// 如果蓝牙没开启，则开启
		if (!mBluetoothAdapter.isEnabled()) {
			startActivity(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));
		}
		buttonConnect = (Button) findViewById(R.id.buttonConnect);
		buttonDisconnect = (Button) findViewById(R.id.buttonDisconnect);
		buttonTurnOn = (Button) findViewById(R.id.buttonTurnOn);
		buttonTurnOff = (Button) findViewById(R.id.buttonTurnOff);
		buttonConnect.setOnClickListener(new ConnectOnClickListener());
		mAudioRecord=new MyAudioRecord();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Log.e(TAG, "On destroy");
		mAudioRecord.release();
		mBluetoothSender.stop();
	}

	class ConnectOnClickListener implements OnClickListener {

		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			Log.e(TAG, "ConnectOnClickListener");
			Intent roomIntent = new Intent(Main.this,
					RoomListActivity.class);
			startActivityForResult(roomIntent, 0);
		}
	}
	
	class TurnOnOnClickListener implements OnClickListener {

		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			Log.e(TAG, "TurnOnOnClickListener");
			Toast.makeText(getApplicationContext(), "Turn On",
					Toast.LENGTH_SHORT).show();
			mBluetoothSender.start();
			buttonTurnOn.setOnClickListener(null);
			buttonTurnOff.setOnClickListener(new TurnOffOnClickListener());
		}
	}
	
	class TurnOffOnClickListener implements OnClickListener {

		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			Log.e(TAG, "TurnOffOnClickListener");
			Toast.makeText(getApplicationContext(), "Turn Off",
					Toast.LENGTH_SHORT).show();
			mBluetoothSender.pause();
			buttonTurnOff.setOnClickListener(null);
			buttonTurnOn.setOnClickListener(new TurnOnOnClickListener());
		}
	}
	class DisconnectOnClickListener implements OnClickListener {

		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			Log.e(TAG, "DisconnectOnClickListener");
			mBluetoothSender.stop();
			Toast.makeText(getApplicationContext(), "Disconnect",
					Toast.LENGTH_SHORT).show();
			buttonConnect.setOnClickListener(new ConnectOnClickListener());
			buttonDisconnect.setOnClickListener(null);
			buttonTurnOff.setOnClickListener(null);
			buttonTurnOn.setOnClickListener(null);
		}
	}
	

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.e(TAG, "onActivityResult");
		if (resultCode == Activity.RESULT_OK) {
			rDevice = (BluetoothDevice) data
					.getParcelableExtra(CommConst.DEVICE_TO_CONNECT);
			mBluetoothSender = new BluetoothSender(mHandler,rDevice,mAudioRecord);
			mBluetoothSender.connect();
			buttonConnect.setOnClickListener(null);
		}
	}
	
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case CommConst.LOST_SERVER:
				Log.e(TAG, "handler:lost server");
				String lostSv = getResources().getString(R.string.lost_server);
				Toast.makeText(getApplicationContext(), lostSv,
						Toast.LENGTH_SHORT).show();
				buttonConnect.setOnClickListener(new ConnectOnClickListener());
				buttonDisconnect.setOnClickListener(null);
				buttonTurnOn.setOnClickListener(null);
				buttonTurnOff.setOnClickListener(null);
				break;
			case CommConst.CONNECT_FAIL:
				Log.e(TAG, "handler:connect fail");
				String connectF = getResources().getString(
						R.string.conncect_fail);
				Toast.makeText(getApplicationContext(), connectF,
						Toast.LENGTH_SHORT).show();
				buttonConnect.setOnClickListener(new ConnectOnClickListener());
				break;
			case CommConst.CONNECT_SUCCESS:
				Log.e(TAG, "handler:connect success");
				String connectS = getResources().getString(
						R.string.conncect_success);
				Toast.makeText(getApplicationContext(), connectS,
						Toast.LENGTH_SHORT).show();
				buttonDisconnect.setOnClickListener(new DisconnectOnClickListener());
				buttonTurnOn.setOnClickListener(new TurnOnOnClickListener());
				break;
			}
		}
	};

}
