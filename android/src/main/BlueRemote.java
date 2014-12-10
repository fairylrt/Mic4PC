package main;

import javax.swing.JOptionPane;

import bluetooth.BlueService;
import components.SysTray;

/**
 * �������
 * @author royen
 * @since 2010.2.10
 */
public class BlueRemote {
	
	public static void main(String[] args){
		BlueService bs=new BlueService();
		
		if(bs.initialBluetooth()){
			new SysTray().show();
			bs.startService();
		}
		else{
			JOptionPane.showMessageDialog(null,"����ջ��ʼ��ʧ��,�����������֧������ģ�飡");
		}
	}
}
