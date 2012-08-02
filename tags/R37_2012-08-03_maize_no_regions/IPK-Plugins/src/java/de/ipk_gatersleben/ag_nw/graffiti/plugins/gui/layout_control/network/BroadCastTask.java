/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 06.09.2004 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.network;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TimerTask;

import javax.swing.SwingUtilities;

import org.ErrorMsg;

import de.ipk_gatersleben.ag_nw.graffiti.UDPreceiveStructure;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.database.dbe.DesEncrypter;
import de.ipk_gatersleben.ag_nw.graffiti.services.network.BroadCastService;

/**
 * @author Christian Klukas
 *         (c) 2004,2012 IPK-Gatersleben
 */
public class BroadCastTask extends TimerTask {
	
	private static int timeOffline = 31000;
	public static int timeDefaultBeatTime = 15000;
	
	BroadCastService broadCastService;
	
	private static String chatPass = "CHATSTATICENCRYPTION";
	
	// Create encrypter/decrypter class
	private static DesEncrypter encrypter = new DesEncrypter(chatPass);
	
	/**
	 * Contains byte[] arrays of data that was received.
	 */
	ArrayList<byte[]> inMessages = new ArrayList<byte[]>();
	
	HashMap<InetAddress, Long> knownHostsAndTime = new HashMap<InetAddress, Long>();
	
	/**
	 * Contains byte[] arrays of data to be sent.
	 */
	private static ArrayList<byte[]> outMessages = new ArrayList<byte[]>();
	
	BroadCastTask(final BroadCastService broadCastService, final Runnable receiver) {
		this.broadCastService = broadCastService;
		Thread receiveTask = new Thread(new Runnable() {
			@SuppressWarnings("deprecation")
			public void run() {
				boolean error = false;
				while (!error) {
					UDPreceiveStructure rec;
					try {
						rec = broadCastService.receiveBroadcast(100);
						byte[] msg = rec.data;
						String msgString = new String(msg, "UTF-8");
						if (msgString.startsWith("SYSTEM:")) {
							synchronized (knownHostsAndTime) {
								broadCastService.increaseInCount();
								int cntBefore = knownHostsAndTime.size();
								knownHostsAndTime.put(rec.sender, new Long(System.currentTimeMillis()));
								long currentTime = System.currentTimeMillis();
								ArrayList<InetAddress> toBeDeleted = new ArrayList<InetAddress>();
								for (Iterator<InetAddress> it = knownHostsAndTime.keySet().iterator(); it.hasNext();) {
									InetAddress key = it.next();
									Long accessTime = (Long) knownHostsAndTime.get(key);
									if (accessTime.longValue() + timeOffline < currentTime)
										toBeDeleted.add(key);
								}
								for (Iterator<InetAddress> it = toBeDeleted.iterator(); it.hasNext();) {
									knownHostsAndTime.remove(it.next());
								}
								if (cntBefore != knownHostsAndTime.size())
									SwingUtilities.invokeLater(receiver);
							}
						} else
							if (msgString.startsWith("MSG:")) {
								broadCastService.increaseInCount();
								msgString = msgString.substring(4);
								if (msgString.indexOf("§§§") > 0) {
									msgString = msgString.substring(0, msgString.indexOf("§§§"));
									// Decrypt
									msgString = encrypter.decrypt(msgString);
									Date dt = new GregorianCalendar().getTime();
									if (msgString != null) {
										synchronized (inMessages) {
											inMessages.add(new String("[" + ensure00(dt.getHours()) + ":" + ensure00(dt.getMinutes()) +
													"/" +
													rec.getSenderHostNameOrIP() +
													"]: " + msgString.trim()).getBytes("UTF-8"));
										}
										SwingUtilities.invokeLater(receiver);
									} else
										ErrorMsg.addErrorMessage("Emtpy String received.");
								}
							}
					} catch (IOException e) {
						error = true;
						ErrorMsg.addErrorMessage(e.getLocalizedMessage());
					}
				}
				System.err.println("Aglet Receiver has ended.");
			}
			
			private String ensure00(int value) {
				String result = new Integer(value).toString();
				if (result.length() < 2)
					result = "0" + result;
				return result;
			}
		}, "Aglet Broadcast Receiver");
		receiveTask.start();
	}
	
	public void addMessageToBeSent(String message) {
		// Encrypt
		message = encrypter.encrypt(message);
		
		synchronized (outMessages) {
			try {
				outMessages.add(new String("MSG:" + message + "§§§").getBytes("UTF-8"));
			} catch (UnsupportedEncodingException e) {
				ErrorMsg.addErrorMessage(e.getLocalizedMessage());
			}
		}
		sendOutMessages();
	}
	
	private void sendOutMessages() {
		try {
			synchronized (outMessages) {
				for (Iterator<byte[]> it = outMessages.iterator(); it.hasNext();) {
					byte[] outBytes = it.next();
					broadCastService.sendBroadcast(outBytes);
				}
				outMessages.clear();
			}
		} catch (IOException e) {
			ErrorMsg.addErrorMessage("Broadcast failed: " + e.getLocalizedMessage());
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		String defaultOutMsg = "SYSTEM: AGLET CLIENT SEARCH FOR SERVERS";
		try {
			broadCastService.sendBroadcast(defaultOutMsg.getBytes("UTF-8"));
			sendOutMessages();
		} catch (IOException e) {
			ErrorMsg.addErrorMessage("Broadcast failed: " + e.getLocalizedMessage());
		}
	}
	
	/**
	 * @return
	 */
	public ArrayList<byte[]> getInMessages() {
		synchronized (inMessages) {
			ArrayList<byte[]> result = new ArrayList<byte[]>(inMessages);
			inMessages.clear();
			return result;
		}
	}
	
	public List<InetAddress> getActiveHosts() {
		synchronized (knownHostsAndTime) {
			ArrayList<InetAddress> result = new ArrayList<InetAddress>(knownHostsAndTime.keySet());
			return result;
		}
	}
	
	public void addBinaryMessage(String fileName, byte[] buff, int count) throws UnsupportedEncodingException {
		fileName = encrypter.encrypt(fileName);
		byte[] fn = new String("FILENAME:" + fileName + "§§§").getBytes("UTF-8");
		int maxLen = broadCastService.getMaxBroadCastMessageLen();
		
		synchronized (outMessages) {
			byte[] largeMessage = concat(fn, buff);
			for (byte[] chunk : split(largeMessage, maxLen))
				outMessages.add(chunk);
		}
	}
	
	private ArrayList<byte[]> split(byte[] largeMessage, int maxLen) {
		ArrayList<byte[]> res = new ArrayList<byte[]>();
		
		return res;
	}
	
	byte[] concat(byte[] A, byte[] B) {
		byte[] C = new byte[A.length + B.length];
		System.arraycopy(A, 0, C, 0, A.length);
		System.arraycopy(B, 0, C, A.length, B.length);
		
		return C;
	}
	
	public static <T> T[] concatAll(T[] first, T[]... rest) {
		int totalLength = first.length;
		for (T[] array : rest) {
			totalLength += array.length;
		}
		T[] result = Arrays.copyOf(first, totalLength);
		int offset = first.length;
		for (T[] array : rest) {
			System.arraycopy(array, 0, result, offset, array.length);
			offset += array.length;
		}
		return result;
	}
}
