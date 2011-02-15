package de.ipk.ag_ba.server.task_management;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;

import org.ErrorMsg;
import org.SystemInfo;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart.TextFile;

public class SystemAnalysisExt {
	
	public SystemInfoExt getSystemInfo() {
		return new SystemInfoExt();
	}
	
	public static int getNumberOfCpuSockets() {
		if (new File("/proc/cpuinfo").exists()) {
			return getLinuxCpuInfoSetInfo("/proc/cpuinfo", "physical id");
		} else {
			if (SystemInfo.isMac()) {
				return (int) getMacSysctl("hw.packages");
			}
		}
		return -1;
	}
	
	public static int getNumberOfCpuPhysicalCores() {
		if (new File("/proc/cpuinfo").exists()) {
			return getLinuxCpuInfoSetInfo("/proc/cpuinfo", "physical id", "core id");
		} else {
			if (SystemInfo.isMac()) {
				return (int) getMacSysctl("hw.physicalcpu");
			}
		}
		return -1;
	}
	
	public static int getNumberOfCpuLogicalCores() {
		if (new File("/proc/cpuinfo").exists()) {
			return getLinuxCpuInfoSetInfo("/proc/cpuinfo", "processor");
		} else {
			if (SystemInfo.isMac()) {
				return (int) getMacSysctl("hw.logicalcpu");
			}
		}
		return -1;
	}
	
	public static long getPhysicalMemoryInGB() {
		if (new File("/proc/mtrr").exists()) {
			/*
			 * [klukas@ba-13 ~]$ cat /proc/mtrr
			 * reg00: base=0x000000000 ( 0MB), size= 2048MB, count=1: write-back
			 * reg01: base=0x100000000 ( 4096MB), size= 4096MB, count=1: write-back
			 * reg02: base=0x200000000 ( 8192MB), size= 8192MB, count=1: write-back
			 * reg03: base=0x400000000 (16384MB), size=16384MB, count=1: write-back
			 * reg04: base=0x800000000 (32768MB), size=32768MB, count=1: write-back
			 * reg05: base=0x1000000000 (65536MB), size= 2048MB, count=1: write-back
			 */
			return getLinuxLastLineInfo("/proc/mtrr", "(", "MB)") / 1024;
		} else {
			if (SystemInfo.isMac()) {
				return getMacSysctl("hw.memsize") / 1024 / 1024 / 1024;
			}
		}
		return -1;
	}
	
	/**
	 * see > sysctl -a hw / sysctl -a
	 */
	private static long getMacSysctl(String setting) {
		long result = -1;
		try {
			Process p = Runtime.getRuntime().exec(new String[] {
					"sysctl",
					setting
			});
			BufferedReader input =
					new BufferedReader
						(new InputStreamReader(p.getInputStream()));
			String line;
			while ((line = input.readLine()) != null) {
				if (line.startsWith(setting)) {
					String n = line.split(":")[1].trim();
					result = Long.parseLong(n);
				}
			}
			input.close();
			p.waitFor();
		} catch (InterruptedException e) {
			ErrorMsg.addErrorMessage(e);
		} catch (IOException e) {
			ErrorMsg.addErrorMessage(e);
		}
		return result;
	}
	
	private static long getLinuxLastLineInfo(String fileName, String tagA, String tagB) {
		long result = -1;
		try {
			TextFile tf = new TextFile(fileName);
			for (String s : tf) {
				if (s.contains(tagA)) {
					s = s.substring(s.indexOf(tagA) + tagA.length());
					if (s.contains(tagB)) {
						s = s.substring(0, s.indexOf(tagB));
						long l = Long.parseLong(s.trim());
						result = l;
					}
				}
			}
		} catch (FileNotFoundException e) {
			ErrorMsg.addErrorMessage(e);
		} catch (IOException e) {
			ErrorMsg.addErrorMessage(e);
		}
		return result;
	}
	
	private static int getLinuxCpuInfoSetInfo(String fileName, String setting) {
		int result = -1;
		HashSet<String> settingValues = new HashSet<String>();
		try {
			TextFile tf = new TextFile(fileName);
			for (String s : tf) {
				if (s.startsWith(setting) && s.contains(":"))
					settingValues.add(s.split(":")[1].trim());
			}
			result = settingValues.size();
		} catch (FileNotFoundException e) {
			ErrorMsg.addErrorMessage(e);
		} catch (IOException e) {
			ErrorMsg.addErrorMessage(e);
		}
		return result;
	}
	
	private static int getLinuxCpuInfoSetInfo(String fileName, String settingA, String settingB) {
		int result = -1;
		HashSet<String> settingValues = new HashSet<String>();
		try {
			String a = "";
			TextFile tf = new TextFile(fileName);
			for (String s : tf) {
				if (s.startsWith(settingA) && s.contains(":"))
					a = s.split(":")[1].trim();
				if (s.startsWith(settingB) && s.contains(":"))
					settingValues.add(a + "/" + s.split(":")[1].trim());
			}
			result = settingValues.size();
		} catch (FileNotFoundException e) {
			ErrorMsg.addErrorMessage(e);
		} catch (IOException e) {
			ErrorMsg.addErrorMessage(e);
		}
		return result;
	}
	
	public static String getHostName() throws UnknownHostException {
		InetAddress local = getLocalHost();
		String hostName = local.getHostName();
		String ip = local.getHostAddress();
		
		boolean retIP = true;
		if (retIP)
			return ip;
		else
			return hostName;
		
	}
	
	// public domain source: http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4665037 //
	/**
	 * Returns an InetAddress representing the address
	 * of the localhost.
	 * Every attempt is made to find an address for this
	 * host that is not
	 * the loopback address. If no other address can
	 * be found, the
	 * loopback will be returned.
	 * 
	 * @return InetAddress - the address of localhost
	 * @throws UnknownHostException
	 *            - if there is a
	 *            problem determing the address
	 */
	public static InetAddress getLocalHost() throws
			UnknownHostException {
		InetAddress localHost =
				InetAddress.getLocalHost();
		if (!localHost.isLoopbackAddress())
			return localHost;
		InetAddress[] addrs =
				getAllLocalUsingNetworkInterface();
		for (int i = 0; i < addrs.length; i++) {
			if (!addrs[i].isLoopbackAddress())
				return addrs[i];
		}
		return localHost;
	}
	
	/**
	 * This method attempts to find all InetAddresses
	 * for this machine in a
	 * conventional way (via InetAddress). If only one
	 * address is found
	 * and it is the loopback, an attempt is made to
	 * determine the addresses
	 * for this machine using NetworkInterface.
	 * 
	 * @return InetAddress[] - all addresses assigned to
	 *         the local machine
	 * @throws UnknownHostException
	 *            - if there is a
	 *            problem determining addresses
	 */
	public static InetAddress[] getAllLocal() throws
			UnknownHostException {
		InetAddress[] iAddresses =
				InetAddress.getAllByName("127.0.0.1");
		if (iAddresses.length != 1)
			return iAddresses;
		if (!iAddresses[0].isLoopbackAddress())
			return iAddresses;
		return getAllLocalUsingNetworkInterface();
		
	}
	
	/**
	 * Utility method that delegates to the methods of
	 * NetworkInterface to
	 * determine addresses for this machine.
	 * 
	 * @return InetAddress[] - all addresses found from
	 *         the NetworkInterfaces
	 * @throws UnknownHostException
	 *            - if there is a
	 *            problem determining addresses
	 */
	private static InetAddress[]
			getAllLocalUsingNetworkInterface() throws
					UnknownHostException {
		ArrayList addresses = new ArrayList();
		Enumeration e = null;
		try {
			e =
					NetworkInterface.getNetworkInterfaces();
		} catch (SocketException ex) {
			throw new UnknownHostException("127.0.0.1");
		}
		while (e.hasMoreElements()) {
			NetworkInterface ni =
					(NetworkInterface) e.nextElement();
			for (Enumeration e2 =
					ni.getInetAddresses(); e2.hasMoreElements();) {
				addresses.add
						(e2.nextElement());
			}
		}
		InetAddress[] iAddresses = new
				InetAddress[addresses.size()];
		for (int i = 0; i < iAddresses.length; i++) {
			iAddresses[i] = (InetAddress)
					addresses.get(i);
		}
		return iAddresses;
	}
}
