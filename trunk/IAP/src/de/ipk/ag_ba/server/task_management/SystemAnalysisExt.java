package de.ipk.ag_ba.server.task_management;

import java.awt.AWTException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.AttributeHelper;
import org.ErrorMsg;
import org.StringManipulationTools;
import org.SystemAnalysis;
import org.SystemInfo;

import oshi.software.os.windows.WindowsHardwareAbstractionLayer;
import util.Screenshot;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart.TextFile;

/**
 * @author klukas
 */
public class SystemAnalysisExt {
	
	public SystemInfoExt getSystemInfo() {
		return new SystemInfoExt();
	}
	
	private static int cpuSockets = -1;
	
	public static Screenshot getScreenshot() throws IOException, AWTException {
		return new Screenshot();
	}
	
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
		InetAddress local = SystemAnalysis.getLocalHost();
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
	
	public static SimpleDateFormat sdf = new SimpleDateFormat();
	
	private static long lastNanos = System.nanoTime();
	private static long lastProcessCPUnanos = getProcessNanos();
	
	private static double lastLoad = -1;
	
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
			lastLoad = res;
			return res;
		} else {
			lastLoad = res;
			return res;
		}
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
	
	public static String getStatus(String pre, final String preLine, String lineBreak, String follow) {
		StringBuilder res = new StringBuilder();
		SystemInfoExt info = new SystemInfoExt();
		res.append(preLine + "Sockets        : " + info.getCpuSockets() + lineBreak);
		res.append(preLine + "Cores p. sock. : " + info.getPhysicalCoresPerSocket() + lineBreak);
		res.append(preLine + "Physical Cores : " + info.getCpuPhysicalCores() + lineBreak);
		res.append(preLine + "Logical Cores  : " + info.getCpuLogicalCores() + lineBreak);
		res.append(preLine + "Log./phys. core: " + info.getHyperThreadingFactor() + lineBreak);
		res.append(preLine + "CPUs (avail.)  : " + info.getCpuCountAvailable() + lineBreak);
		res.append(preLine + "Phys. mem. (GB): " + info.getPhysicalMemoryInGB() + lineBreak);
		res.append(preLine + "System load    : " + StringManipulationTools.formatNumber(info.getLoad(), "#.#") + lineBreak);
		if (res != null && res.length() > 0)
			return pre + res.toString() + follow;
		else
			return res.toString();
	}
	
	public static String getStorageStatus(String pre, final String preLine, String lineBreak, String follow) {
		StringBuilder res = new StringBuilder();
		
		for (File lfw : SystemAnalysisExt.myListRoots()) {
			long fs = lfw.getFreeSpace();
			long ts = lfw.getTotalSpace();
			long free = fs / 1024 / 1024 / 1024;
			long size = ts / 1024 / 1024 / 1024;
			long used = (ts - fs) / 1024 / 1024 / 1024;
			int prc = (int) (100d * (1d - free / (double) size));
			res.append(preLine + lfw.toString() + " -> " + free + " GB free (" + size + " GB, " + prc + "% used)" + lineBreak);
		}
		
		if (res != null && res.length() > 0)
			return pre + res.toString() + follow;
		else
			return res.toString();
	}
	
	public static ArrayList<File> myListRoots() {
		ArrayList<File> res = new ArrayList<File>();
		for (File f : File.listRoots()) {
			res.add(f);
		}
		String[] roots = new String[] { // IAPmain.getHSMfolder(),
		"/media/data4", "/home", "/Users",
				"/backups", "/data0", "/media/16TB" };
		for (String r : roots) {
			if (r == null)
				continue;
			if (res.contains(r))
				continue;
			File hsm = new File(r);
			if (hsm.exists())
				res.add(hsm);
		}
		return res;
	}
	
	/**
	 * @return IP_startupMS
	 */
	public static String getHostNameNoError() {
		try {
			return getHostName();
		} catch (UnknownHostException e) {
			return "(unknown host)";
		}
	}
	
	public static String getHostNameNiceNoError() {
		try {
			String res = getHostName();
			if (res != null && res.contains("_")) {
				String[] parts = res.split("_", 2);
				return parts[0] + " (started " + SystemAnalysis.getCurrentTimeInclSec(Long.parseLong(parts[1])) + ")";
			}
			return res;
		} catch (UnknownHostException e) {
			return "(unknown host)";
		}
	}
	
	public static double getRealSystemCpuLoad(boolean cached) {
		if (!cached)
			return getRealSystemCpuLoad();
		else
			return lastLoad;
	}
}
