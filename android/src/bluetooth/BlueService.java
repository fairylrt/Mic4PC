package bluetooth;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import javax.bluetooth.LocalDevice;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

/**
 解决延迟方法，记录时间，如果延迟超过一定时间，则不播放，保证时刻播放最新的！
 */
public class BlueService implements Runnable{
	//服务标示符
	private UUIDGen mUUIDGen = null;
	private UUID mUUID = null;
	private long time0;

	//流连接通知器
	private StreamConnectionNotifier notifier;

	/** 本地设备管理类 */
	private LocalDevice localDevice=null;

	//服务记录
	ServiceRecord serviceRecord;	

	public BlueService(){
		initialBluetooth();
		mUUIDGen = new UUIDGen(localDevice.getBluetoothAddress());
		mUUID = mUUIDGen.next();
	}

	/**
	 * 开启服务
	 */
	public void startService(){
		new Thread(this).start();
		System.out.print("Start Service");
	}

	public void run() {
		try{
			notifier=(StreamConnectionNotifier)Connector.open(getConnectionStr());
			serviceRecord=localDevice.getRecord(notifier);
		}
		catch(Exception ex){
			System.out.println("occur exception here: "+ex.getMessage());
		}

		while(true){
			StreamConnection conn=null;
			try{
				conn=notifier.acceptAndOpen();
				time0=new Date().getTime();
				
			}
			catch(Exception ex){
				System.out.println("occur exception when accept connection~");
				continue;
			}	

			//开启对连接的处理线程
			new Thread(new ProcessConnection(conn)).start();			
		}
	}

	/**
	 * 获取连接字符串
	 * @return
	 */
	private String getConnectionStr(){
		StringBuffer sb=new StringBuffer("btspp://");
		sb.append("localhost").append(":");
		sb.append(mUUID.toString());
		sb.append(";name=BlueMessage");
		sb.append(";authorize=false");
		return sb.toString();
	}	


	/**
	 * 蓝牙初始化
	 * @return
	 */
	public boolean initialBluetooth(){
		boolean btReady=true;		
		System.out.println("蓝牙初始化");
		try{
			localDevice=LocalDevice.getLocalDevice();
			/*if(!localDevice.setDiscoverable(DiscoveryAgent.GIAC)){
				btReady=false;
			}*/
		}
		catch(Exception e){
			btReady=false;
			e.printStackTrace();
		}		

		return btReady;
	}

	/**
	 * 处理客户端连接的线程
	 * @author royen
	 * @since 2010.1.25
	 */
	private class ProcessConnection implements Runnable{
		//连接流
		private StreamConnection conn=null;
		
		//读取流中
		
		public ProcessConnection(StreamConnection conn){
			this.conn=conn;		
		}
		
		private SourceDataLine getLine(AudioFormat audioFormat)  {
	        SourceDataLine res = null;
	        DataLine.Info info = new DataLine.Info(SourceDataLine.class,
		                audioFormat);
	        try{
		    	res = (SourceDataLine) AudioSystem.getLine(info);
		    	res.open(audioFormat);
		    }
		    catch (Exception e){
		    	System.out.println(e.getClass());
		    }
		    return res;
		}

		public long parse(byte[] bytes){
			long ans=0;
			for(int i=0;i<8;i++){
				ans+=((int)(bytes[i]& 0xFF))<<((7-i)*8);
			}
			return ans;
		}
		
		public void run() {	
			System.out.println("client connected...");
			
			try{
				InputStream is= conn.openInputStream();
				
				AudioFormat af = new AudioFormat(
					    22050,
					    16,
					    1,
					    true,
					    false);
				SourceDataLine line = getLine(af);
				if (line==null)
					System.out.println("null");
				int bufSize=648;
				line.open(af, 640);
				System.out.println("hehehe");
				line.start();
				int inBytes = 0;
				long time=0;//秒表
				long dt=0;
				long timex=-100;
				byte[] bytes = new byte [bufSize];
				int calc=0;//判断是否失去连接用
				while (true){
					try{
						inBytes = is.read(bytes, 0, bufSize);
						time=parse(bytes);
						dt=new Date().getTime()-time0;
						//String temp=new String(bytes);
						//System.out.println(temp);
					}
					catch (IOException e)  
					{
						e.printStackTrace();
					}
					System.out.println(dt-time);
					if (time==-538976290){//手机disconnect了
						BlueService.this.startService();
						line.close();
						return;
					}
					if ((inBytes >= 0)&&(Math.abs(dt-time)<200)&&(timex!=time)) {
						int	outBytes = line.write(bytes, 8, inBytes-8);
						System.out.println(outBytes);
						calc=0;
					}
					else if (timex==time){
						calc++;//手机掉线，延迟了
						if (calc>1000){
							BlueService.this.startService();
							line.close();
							return;
						}
					}
					timex=time;
					
				}
				
			}
			catch(Exception ex){
				System.out.println("occur exception ,message is "+ex.getClass());
			}
		}

	}

}
