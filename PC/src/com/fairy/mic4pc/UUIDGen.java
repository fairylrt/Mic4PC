package com.fairy.mic4pc;

import java.util.UUID;

public class UUIDGen {
	private int num = 10;
	private String orginal = "fa87c0d0-afac-11de-8a";
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
		return UUID.fromString(orginal+Integer.toString(num)+"-"+macAddress);
	}
}
