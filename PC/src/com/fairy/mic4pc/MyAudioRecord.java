package com.fairy.mic4pc;

import java.util.LinkedList;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

public class MyAudioRecord extends AudioRecord{
	public final int bufSize = 640;
    public byte []     bytes ;
    public LinkedList<byte[]>  byteList ;
	
	public MyAudioRecord(){
		super(MediaRecorder.AudioSource.MIC,
				22050,
				AudioFormat.CHANNEL_IN_MONO,
				AudioFormat.ENCODING_PCM_16BIT,
				640);
		/*for(int i=22050;i<44102;i++)
			if (AudioRecord.getMinBufferSize(i,
				AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT)>640){
				System.out.println(i-1);
				break;}*/
		bytes = new byte [bufSize] ;
		byteList=new LinkedList<byte[]>();

	}
	
	public void stop(){
		super.stop();
	}
}
