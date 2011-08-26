package de.ipk.ag_ba.server.task_management;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;

import org.AttributeHelper;
import org.ErrorMsg;
import org.SystemInfo;

import oshi.software.os.windows.WindowsHardwareAbstractionLayer;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart.TextFile;

public class SystemAnalysisExt {
	
	public SystemInfoExt getSystemInfo() {
		return new SystemInfoExt();
	}
	
	private static int cpuSockets = -1;
	
	private static WindowsHardwareAbstractionLayer hal = AttributeHelper.windowsRunning() ? new WindowsHardwareAbstractionLayer() : null;
	
	public static int getNumberOfCpuSockets() {
		if (cpuSockets > 0)
			return cpuSockets;
		int res = -1;
		if (new File("/proc/cpuinfo").exists()) {
			res = getLinuxCpuInfoSetInfo("/proc/cpuinfo", "physical id");
		} else {
			if (SystemInfo.isMac()) {
				res = (int) getMacSysctl("hw.packages");
			}
		}
		cpuSockets = res;
		return res;
	}
	
	private static int physicalCores = -1;
	
	public static int getNumberOfCpuPhysicalCores() {
		if (physicalCores > 0)
			return physicalCores;
		int res = -1;
		if (new File("/proc/cpuinfo").exists()) {
			res = getLinuxCpuInfoSetInfo("/proc/cpuinfo", "physical id", "core id");
		} else {
			if (SystemInfo.isMac()) {
				res = (int) getMacSysctl("hw.physicalcpu");
			}
		}
		physicalCores = res;
		return res;
	}
	
	private static int logicCpuCount = -1;
	
	public static int getNumberOfCpuLogicalCores() {
		if (logicCpuCount > 0)
			return logicCpuCount;
		int res = -1;
		boolean runtime = true;
		if (runtime)
			res = Runtime.getRuntime().availableProcessors();
		else
			if (new File("/proc/cpuinfo").exists()) {
				res = getLinuxCpuInfoSetInfo("/proc/cpuinfo", "processor");
			} else {
				if (SystemInfo.isMac()) {
					res = (int) getMacSysctl("hw.logicalcpu");
				} else {
					if (hal != null) {
						return hal.getProcessors().length;
					}
				}
			}
		logicCpuCount = res;
		return res;
	}
	
	private static long physicalMemoryInGB = -1;
	
	public static NetworkIO getNetworkIoStats() {
		NetworkIO res = new NetworkIO();
		if (AttributeHelper.linuxRunning())
			if (new File("/proc/cpuinfo").exists()) {
				// res = getLinuxCpuInfoSetInfo("/proc/cpuinfo", "processor");
			}
		return res;
	}
	
	public static long getPhysicalMemoryInGB() {
		if (physicalMemoryInGB > 0)
			return physicalMemoryInGB;
		long res = -1;
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
			res = Math.round(getLinuxLastLineInfo("/proc/mtrr", "(", "MB)") / 1024d);
		} else {
			if (SystemInfo.isMac()) {
				res = Math.round(getMacSysctl("hw.memsize") / 1024d / 1024d / 1024d);
			} else {
				if (hal != null) {
					return Math.round(hal.getMemory().getTotal() / 1024d / 1024d / 1024d);
				}
			}
		}
		physicalMemoryInGB = res;
		return res;
	}
	
	private static HashMap<String, Long> setting2result = new HashMap<String, Long>();
	
	/**
	 * see > sysctl -a hw / sysctl -a
	 */
	private static long getMacSysctl(String setting) {
		if (setting2result.containsKey(setting))
			return setting2result.get(setting);
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
		setting2result.put(setting, result);
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
						if (l > result)
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
	
	private static int getLinuxNetworkStatInfo(String fileName, String columnHeader) {
		int result = -1;
		HashSet<String> settingValues = new HashSet<String>();
		try {
			TextFile tf = new TextFile(fileName);
			for (String s : tf) {
				// if (s.startsWith(setting) && s.contains(":"))
				// settingValues.add(s.split(":")[1].trim());
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
	
	private static String hostName = null;
	
	private static long startupTime = System.currentTimeMillis();
	
	public static String getHostName() throws UnknownHostException {
		if (hostName != null)
			return hostName;
		InetAddress local = getLocalHost();
		String hostNameR = local.getHostName();
		String ip = local.getHostAddress();
		
		String res = null;
		
		boolean retIP = true;
		if (retIP)
			res = ip + "_" + startupTime;
		else
			res = hostNameR;
		hostName = res;
		return res;
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
		InetAddress localHost = InetAddress.getByAddress(new byte[] { 127, 0, 0, 1 });
		try {
			localHost =
					InetAddress.getLocalHost();
			if (!localHost.isLoopbackAddress())
				return localHost;
			InetAddress[] addrs =
					getAllLocalUsingNetworkInterface();
			for (int i = 0; i < addrs.length; i++) {
				if (!addrs[i].isLoopbackAddress())
					return addrs[i];
			}
		} catch (Exception e) {
			System.out.println(SystemAnalysisExt.getCurrentTime() + ">ERROR: " + e.getMessage());
		}
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
		ArrayList<InetAddress> addresses = new ArrayList<InetAddress>();
		Enumeration<NetworkInterface> e = null;
		try {
			e =
					NetworkInterface.getNetworkInterfaces();
		} catch (SocketException ex) {
			throw new UnknownHostException("127.0.0.1");
		}
		while (e.hasMoreElements()) {
			NetworkInterface ni =
					e.nextElement();
			for (Enumeration<InetAddress> e2 = ni.getInetAddresses(); e2.hasMoreElements();) {
				addresses.add(e2.nextElement());
			}
		}
		InetAddress[] iAddresses = new InetAddress[addresses.size()];
		for (int i = 0; i < iAddresses.length; i++) {
			iAddresses[i] = addresses.get(i);
		}
		return iAddresses;
	}
	
	private static SimpleDateFormat sdf = new SimpleDateFormat();
	
	public static String getCurrentTime() {
		return sdf.format(new Date());
	}
	
	public static String getCurrentTime(long time) {
		return sdf.format(new Date(time));
	}
	
	private static long lastNanos = System.nanoTime();
	private static long lastProcessCPUnanos = getProcessNanos();
	
	public static double getRealSystemCpuLoad() {
		OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
		double res = operatingSystemMXBean.getSystemLoadAverage();
		
		if (res < 0) {
			// if (AttributeHelper.windowsRunning()) {
			// @SuppressWarnings("restriction")
			// com.sun.management.OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(
			// OperatingSystemMXBean.class);
			// return osBean.getSystemCpuLoad() * getNumberOfCpuLogicalCores();
			// } else
			if (AttributeHelper.windowsRunning()) {
				// java.lang.management.OperatingSystemMXBean os =
				// ManagementFactory.getOperatingSystemMXBean();
				//
				// if (os instanceof com.sun.management.OperatingSystemMXBean) {
				// long cpuTime = ((com.sun.management.OperatingSystemMXBean) os).getProcessCpuTime();
				// System.out.println("CPU time = " + cpuTime);
				// }
				long nowNanos = System.nanoTime();
				long nowProcessCPUnanos = getProcessNanos();
				if (nowNanos == lastNanos || nowProcessCPUnanos == lastProcessCPUnanos) {
					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					nowNanos = System.nanoTime();
					nowProcessCPUnanos = getProcessNanos();
				}
				
				if (nowNanos > lastNanos && nowProcessCPUnanos >= lastProcessCPUnanos) {
					long process = nowProcessCPUnanos - lastProcessCPUnanos;
					long time = nowNanos - lastNanos;
					double load = process / (double) time; // * getNumberOfCpuLogicalCores()
					lastNanos = nowNanos;
					lastProcessCPUnanos = nowProcessCPUnanos;
					// in this case only the CPU load of this process can be determined
					res = load;
				}
			}
			return res;
		} else
			return res;
	}
	
	private static long getProcessNanos() {
		try {
			java.lang.management.OperatingSystemMXBean os =
					ManagementFactory.getOperatingSystemMXBean();
			
			if (os instanceof com.sun.management.OperatingSystemMXBean) {
				long cpuTime = ((com.sun.management.OperatingSystemMXBean) os).getProcessCpuTime();
				return cpuTime;
			}
		} catch (Exception e) {
			return -2;
		}
		return -1;
	}
}
