package com.fairy.mic4pc;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Date;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class BluetoothSender {
	private static final String TAG = "BluetoothSender";
	private BluetoothDevice rDevice = null;
	private BluetoothSocket mSocket = null;
	private Sender mSender=null;
	private MyAudioRecord mAudioRecord = null;
	private static UUIDGen mUUIDGen = null;
	private Handler mHandler = null;
	private long time0;
	
	public BluetoothSender(Handler h,BluetoothDevice device,MyAudioRecord ar){
		Log.e(TAG, "BluetoothSender create");
		mHandler = h;
		rDevice = device;
		mUUIDGen = new UUIDGen(rDevice.getAddress());
		mAudioRecord = ar;
	}
	
	public void connect() {
		new Thread() {
			public void run() {
				Log.e(TAG, "start");
				while (true) {
					try {
						mSocket = rDevice
								.createRfcommSocketToServiceRecord(mUUIDGen.next());
						System.out.println("connect");
						mSocket.connect();
						time0=new Date().getTime();
						break;
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						System.out.println("fail");
						//if (!mUUIDGen.hasNext()) {
							show(CommConst.CONNECT_FAIL);
							BluetoothSender.this.stop();
							return;
						//}
					}
				}
				show(CommConst.CONNECT_SUCCESS);
			}
		}.start();

	}
	
	public void pause(){
		Log.e(TAG, "BluetoothSender Pause");
		if (mSender != null && mSender.isAlive())
			mSender.pause();
	}
	
	public void stop() {
		Log.e(TAG, "BluetoothSender Stop");
		if (mSender != null && mSender.isAlive())
			mSender.kill();
		if (mSocket != null)
			try {
				mSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	public void start(){
		Log.e(TAG,"start");
		if (mSender==null){
			mSender = new Sender(mSocket,mAudioRecord);
			mSender.start();
		}
		else{
			mSender.restart();
		}
	}
	
	private void show(char warn) {
		Bundle bundle = new Bundle();
		bundle.putChar(CommConst.YOUR_TYPE, warn);
		Message msg = mHandler.obtainMessage(warn);
		msg.setData(bundle);
		mHandler.sendMessage(msg);
	}
	
	private class Sender extends Thread {
		private DataOutputStream dataout;
		private MyAudioRecord mAudioRecord = null;
		private volatile boolean pauseFlag;
		private volatile boolean stopFlag;
		
		public Sender(BluetoothSocket socket,MyAudioRecord ar) {
			try {
				Log.e(TAG, "Sender ππ‘Ï");
				mAudioRecord= ar;
				dataout = new DataOutputStream(socket.getOutputStream());
				pauseFlag=false;
				stopFlag=false;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		public byte[] intToByteArray(long t) {   
			int len=8;  
			byte[] result = new byte[len];
			  for(int i=0;i<len;i++){
				  result[i]=(byte)((t>>(len-1-i)*8) & 0xFF);
			  }
			  return result;
			 }
		
		public byte[] newByte(long t,byte[] bytes){
			byte[] ans=new byte[8+bytes.length];
			byte[] tmp=intToByteArray(t);
			for(int i=0;i<8;i++)
				ans[i]=tmp[i];
			for(int i=0;i<bytes.length;i++)
				ans[i+8]=bytes[i];
			return ans;
		}
		@SuppressWarnings("finally")
		public void run() {	
			try {
				Log.e(TAG, "Sender reading");
				byte [] bytesCopy;
				mAudioRecord.startRecording();
				while(true){
					mAudioRecord.read(mAudioRecord.bytes, 0, mAudioRecord.bufSize);
					bytesCopy = mAudioRecord.bytes.clone();
					//String temp=new String(bytesCopy);
					//System.out.println(temp);
					byte[] tempbyte;
					if (!pauseFlag){
						tempbyte=newByte(new Date().getTime()-time0,bytesCopy);
					}else{
						tempbyte=newByte(0,bytesCopy);
					}
					if (!stopFlag){
						dataout.write(tempbyte,0,tempbyte.length);
						dataout.flush();
					}
					//mAudioRecord.byteList.add(bytesCopy);
				}
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.e(TAG, "Sender crash");
				BluetoothSender.this.show(CommConst.LOST_SERVER);
			}
			finally{
				BluetoothSender.this.stop();
				Log.e(TAG, "Sender stop");
				return;
			}
		}
		public void pause(){
			Log.e(TAG,"Sender pause");
			pauseFlag=true;
		}
		public void restart(){
			Log.e(TAG,"Sender resume");
			pauseFlag=false;
		}
		public void kill(){
			Log.e(TAG,"Sender kill");
			stopFlag=true;
			byte[] tempbyte=new byte[648];
			for(int i=0;i<648;i++){
				tempbyte[i]=(byte) 0xEF;
			}
			try {
				dataout.write(tempbyte,0,tempbyte.length);
				dataout.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}
	
}
