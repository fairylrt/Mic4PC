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
 ����ӳٷ�������¼ʱ�䣬����ӳٳ���һ��ʱ�䣬�򲻲��ţ���֤ʱ�̲������µģ�
 */
public class BlueService implements Runnable{
	//�����ʾ��
	private UUIDGen mUUIDGen = null;
	private UUID mUUID = null;
	private long time0;

	//������֪ͨ��
	private StreamConnectionNotifier notifier;

	/** �����豸������ */
	private LocalDevice localDevice=null;

	//�����¼
	ServiceRecord serviceRecord;	

	public BlueService(){
		initialBluetooth();
		mUUIDGen = new UUIDGen(localDevice.getBluetoothAddress());
		mUUID = mUUIDGen.next();
	}

	/**
	 * ��������
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

			//���������ӵĴ����߳�
			new Thread(new ProcessConnection(conn)).start();			
		}
	}

	/**
	 * ��ȡ�����ַ���
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
	 * ������ʼ��
	 * @return
	 */
	public boolean initialBluetooth(){
		boolean btReady=true;		
		System.out.println("������ʼ��");
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
	 * ����ͻ������ӵ��߳�
	 * @author royen
	 * @since 2010.1.25
	 */
	private class ProcessConnection implements Runnable{
		//������
		private StreamConnection conn=null;
		
		//��ȡ����
		
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
				long time=0;//���
				long dt=0;
				long timex=-100;
				byte[] bytes = new byte [bufSize];
				int calc=0;//�ж��Ƿ�ʧȥ������
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
					if (time==-538976290){//�ֻ�disconnect��
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
						calc++;//�ֻ����ߣ��ӳ���
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
