/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 06.09.2004 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.services.network;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import org.HelperClass;

import de.ipk_gatersleben.ag_nw.graffiti.UDPreceiveStructure;

/**
 * Creates a broadcast-service which allows sending and receiving of broadcast messages.
 * 
 * @author Christian Klukas
 *         (c) 2004 IPK-Gatersleben
 */
public class BroadCastService implements HelperClass {
	
	private int udpPortStart;
	private int udpPortEnd;
	private int maxMessageLen;
	private int udpBindPort;
	private String targetIP;
	
	private int inCount = 0;
	private int outCount = 0;
	
	private int inCountAll = 0;
	
	private DatagramSocket ds = null;
	
	/**
	 * Creates a broadcast-service which allows sending and receiving of broadcast messages.
	 * 
	 * @param udpPort
	 *           The UDP Port where the data is received and send.
	 * @param targetIP
	 *           The Target IP (mask), e.g. 255.255.255.255
	 * @param maxMessageLen
	 *           The maximum length of bytes that is allowed to send and receive by this service.
	 */
	BroadCastService(int udpPortStart, int udpPortEnd, String targetIP, int maxMessageLen) {
		this.udpPortStart = udpPortStart;
		this.udpPortEnd = udpPortEnd;
		this.udpBindPort = -1;
		this.targetIP = targetIP;
		this.maxMessageLen = maxMessageLen;
	}
	
	/**
	 * Creates a broadcast-service which allows sending and receiving of broadcast messages.
	 * A default target IP of 255.255.255.255 is used.
	 * 
	 * @param udpPort
	 *           The UDP Port where the data is received and send.
	 * @param maxMessageLen
	 *           The maximum length of bytes that is allowed to send and receive by this service.
	 */
	public BroadCastService(int udpPortStart, int udpPortEnd, int maxMessageLen) {
		this(udpPortStart, udpPortEnd, "255.255.255.255", maxMessageLen);
	}
	
	/**
	 * Sends a broadcast to all of the nodes the ip should be the ip for the
	 * network
	 * 
	 * @param msg
	 *           the message to be send
	 * @param ip
	 *           the ip address to use for the broadcast packet
	 * @throws IOException
	 *            if cannot send the packet
	 */
	public void sendBroadcast(byte[] msg) throws IOException, IllegalArgumentException {
		if (msg.length > maxMessageLen)
			throw new IllegalArgumentException("Broadcast Message Length is longer than specified maximum length.");
		
		for (int port = udpPortStart; port <= udpPortEnd; port++) {
			DatagramPacket dp = new DatagramPacket(
								msg,
								0,
								msg.length,
								InetAddress.getByName(targetIP),
								port);
			DatagramSocket ds = new DatagramSocket();
			ds.send(dp);
			ds.close();
			outCount++;
		}
	}
	
	/**
	 * Receives and returns the data contained in a broadcast message.
	 * Set timeout <= 0 if you wish to have no timeout
	 * 
	 * @throws IOException
	 */
	public UDPreceiveStructure receiveBroadcast(int timeout) throws IOException {
		
		if (udpBindPort == -1)
			udpBindPort = udpPortStart;
		
		boolean success = false;
		
		UDPreceiveStructure result = null;
		while (!success) {
			try {
				if (ds == null)
					ds = new DatagramSocket(udpBindPort);
				if (timeout > 0) {
					ds.setSoTimeout(timeout);
				}
				byte[] buf = new byte[maxMessageLen];
				DatagramPacket packet = new DatagramPacket(buf, buf.length);
				ds.receive(packet);
				packet.getAddress();
				result = new UDPreceiveStructure(packet.getAddress(), packet.getData());
				success = true;
				inCountAll++;
			} catch (InterruptedIOException iioe) {
				// empty
			} catch (BindException be) {
				udpBindPort++;
				if (udpBindPort > udpPortEnd)
					throw be;
			} catch (IOException e) {
				if (e instanceof BindException) {
					udpBindPort++;
					if (udpBindPort > udpPortEnd)
						throw e;
				} else
					throw e;
			}
		}
		if (success)
			return result;
		else
			return null;
	}
	
	/**
	 * 
	 */
	public void increaseInCount() {
		synchronized (this) {
			inCount++;
		}
	}
	
	public int getInCount() {
		return inCount;
	}
	
	public int getOutCount() {
		return outCount;
	}
	
	public int getOtherInCount() {
		return inCountAll - inCount;
	}
	
	public int getStartPort() {
		return udpPortStart;
	}
	
	public int getEndPort() {
		return udpPortEnd;
	}
	
	public int getBindPort() {
		return udpBindPort;
	}
}
