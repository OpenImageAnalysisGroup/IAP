/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 06.09.2004 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti;

import java.net.InetAddress;

/**
 * @author Christian Klukas
 *         (c) 2004 IPK-Gatersleben
 */
public class UDPreceiveStructure {
	
	public byte[] data;
	public InetAddress sender;
	
	/**
	 * @param address
	 * @param data
	 */
	public UDPreceiveStructure(InetAddress address, byte[] data) {
		this.data = data;
		this.sender = address;
	}
	
	public String getSenderHostNameOrIP() {
		try {
			InetAddress[] inetAdd = InetAddress.getAllByName(sender.toString().substring(1));
			String result = "";
			for (int i = 0; i < inetAdd.length; i++) {
				result += ";" + inetAdd[i].getHostName();
			}
			return result.substring(1);
		} catch (java.net.UnknownHostException uhe) {
			return "Unknown host (" + sender.toString().substring(1) + ")";
		}
	}
}
