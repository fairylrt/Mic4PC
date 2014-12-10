package bluetooth;

import javax.bluetooth.UUID;

public class UUIDGen {
	private int num = 10;
	private String orginal = "FA87C0D0AFAC11DE8A";
	private String macAddress;
	
	public UUIDGen(String mac) {
		StringBuffer macA=new StringBuffer("");;
		for (int i = 0; i < mac.length(); i++)
			if (mac.charAt(i) == ':')
				continue;
			else
				macA.append(mac.charAt(i));
		macAddress=macA.toString();
	}
	
	public boolean hasNext() {
		return (num < 99);
	}
	
	public UUID next() {
		return new UUID(orginal+Integer.toString(num)+macAddress,false);
	}
}