package org;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.Scanner;

import org.junit.Test;

public class SystemAnalysis {
	
	private static boolean fullPower = false;
	private static boolean halfPower = false;
	private static int fixedCPUload = 0;
	
	/**
	 * Use getNumberOfCPUs to determine the number of CPUs to be used for
	 * parallel computing. This method may be used for analysis of host system.
	 * 
	 * @return
	 */
	private static int realCPU = -1;
	
	public static int getRealNumberOfCPUs() {
		if (realCPU < 0)
			realCPU = Runtime.getRuntime().availableProcessors();
		return realCPU;
	}
	
	public static void setUseFullCpuPower(boolean b) {
		SystemAnalysis.fullPower = true;
	}
	
	public static void setUseHalfCpuPower(boolean b) {
		SystemAnalysis.halfPower = true;
	}
	
	public static int getNumberOfCPUs() {
		if (getEnvironmentInteger("SYSTEM_cpu_n", -1) > 0)
			return getEnvironmentInteger("SYSTEM_cpu_n", -1);
		if (fixedCPUload > 0)
			return fixedCPUload;
		int cpus = SystemOptions.getInstance().getInteger(
				"SYSTEM", "cpu_n", Runtime.getRuntime().availableProcessors());
		boolean useHalfCPUpower = SystemOptions.getInstance().getBoolean(
				"SYSTEM", "cpu_use_half_n", cpus > 6);
		if (fullPower)
			useHalfCPUpower = false;
		if (halfPower)
			useHalfCPUpower = true;
		if (useHalfCPUpower)
			return (int) (cpus / 2d > 0 ? cpus / 2d : 1);
		else
			return cpus;
	}
	
	public static int getEnvironmentInteger(String id, int defaultIfMissing) {
		Map<String, String> variables = System.getenv();
		for (Map.Entry<String, String> entry : variables.entrySet()) {
			String name = entry.getKey();
			if (name.equalsIgnoreCase(id)) {
				String value = entry.getValue();
				try {
					Integer v = Integer.parseInt(value);
					return v;
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}
		return defaultIfMissing;
	}
	
	public static int getNumberOfCPUs(
			int minimumCPUcountBeforeMultipleCPUsAreUsed) {
		int cpus = getNumberOfCPUs();
		if (cpus >= minimumCPUcountBeforeMultipleCPUsAreUsed)
			return cpus;
		else
			return 1;
	}
	
	public static String getUserName() {
		String res;
		if (AttributeHelper.windowsRunning())
			res = System.getenv("USERNAME");
		else
			res = System.getenv("USER");
		return res;
	}
	
	public static int getNumberOfCPUsMax(int maximum) {
		int res = getNumberOfCPUs();
		if (res < maximum)
			return res;
		else
			return maximum;
	}
	
	public static boolean isWindowsRunning() {
		return AttributeHelper.windowsRunning();
	}
	
	@Test
	public static void analyzeSystem() {
		OperatingSystemMXBean operatingSystemMXBean = ManagementFactory
				.getOperatingSystemMXBean();
		for (Method method : operatingSystemMXBean.getClass()
				.getDeclaredMethods()) {
			method.setAccessible(true);
			if (method.getName().startsWith("get")
					&& Modifier.isPublic(method.getModifiers())) {
				Object value;
				try {
					value = method.invoke(operatingSystemMXBean);
				} catch (Exception e) {
					value = e;
				} // try
				System.out.println(method.getName() + " = " + value);
			} // if
		} // for
	}
	
	public static long getRealSystemMemoryInMB() {
		OperatingSystemMXBean operatingSystemMXBean = ManagementFactory
				.getOperatingSystemMXBean();
		for (Method method : operatingSystemMXBean.getClass()
				.getDeclaredMethods()) {
			method.setAccessible(true);
			if (method.getName().startsWith("get")
					&& Modifier.isPublic(method.getModifiers())) {
				Object value;
				try {
					value = method.invoke(operatingSystemMXBean);
				} catch (Exception e) {
					value = e;
				} // try
				if (method.getName().equals("getTotalPhysicalMemorySize")) {
					Long l = (Long) value;
					return l / 1024 / 1024;
				}
			} // if
		} // for
		return -1;
	}
	
	/**
	 * The option -Xmx5g will not result in a result of 5 GB. it seems the java
	 * parameter does not use base of 1024 values but base of 1000 values.
	 * 
	 * @return
	 */
	public static long getMemoryMB() {
		return Runtime.getRuntime().maxMemory() / 1024 / 1024;
	}
	
	public static long getUsedMemoryInMB() {
		Runtime r = Runtime.getRuntime();
		long used = r.totalMemory() - r.freeMemory();
		return used / 1024 / 1024;
	}
	
	/**
	 * @return windows/linux/mac/other
	 */
	public static String getOperatingSystem() {
		if (AttributeHelper.windowsRunning())
			return "windows";
		if (AttributeHelper.linuxRunning())
			return "linux";
		if (AttributeHelper.macOSrunning())
			return "mac";
		return "other";
	}
	
	public static void setUseCpu(int cpus) {
		fixedCPUload = cpus;
	}
	
	private static boolean first = true;
	
	public static boolean simulateHeadless = false;
	
	public static boolean isHeadless() {
		if (simulateHeadless) {
			return true;
		}
		return GraphicsEnvironment.isHeadless();
	}
	
	private static SimpleDateFormat sdf = new SimpleDateFormat();
	private static DateFormat sdfInclSec = DateFormat.getTimeInstance();
	private static Scanner sc;
	
	public static String getCurrentTimeInclSec() {
		return sdfInclSec.format(new Date());
	}
	
	public static String getCurrentTimeInclSec(long time) {
		return sdfInclSec.format(new Date(time));
	}
	
	public static String getCurrentTime() {
		return sdf.format(new Date());
	}
	
	public static String getCurrentTime(long time) {
		return sdf.format(new Date(time));
	}
	
	public static String getWaitTime(Long fullTime) {
		if (fullTime == null)
			return "-";
		else
			return getWaitTime(fullTime, 2);
	}
	
	public static String getWaitTime(long fullTime, int n) {
		ProgressStatusService pss = new ProgressStatusService();
		fullTime += 0;
		String res = pss.getRemainTimeString(-1, fullTime / 1000, n);
		return StringManipulationTools.stringReplace(res, "&nbsp;", " ");
	}
	
	public static String getWaitTimeShort(long l) {
		String w = getWaitTime(l);
		w = StringManipulationTools.stringReplace(w, " years", "y");
		w = StringManipulationTools.stringReplace(w, " year", "y");
		w = StringManipulationTools.stringReplace(w, " months", "mo");
		w = StringManipulationTools.stringReplace(w, " month", "mo");
		w = StringManipulationTools.stringReplace(w, " weeks", "w");
		w = StringManipulationTools.stringReplace(w, " week", "w");
		w = StringManipulationTools.stringReplace(w, " days", "d");
		w = StringManipulationTools.stringReplace(w, " day", "d");
		w = StringManipulationTools.stringReplace(w, " hours", "h");
		w = StringManipulationTools.stringReplace(w, " hour", "h");
		w = StringManipulationTools.stringReplace(w, " min", "m");
		w = StringManipulationTools.stringReplace(w, " hour", "h");
		w = StringManipulationTools.stringReplace(w, " second", "s");
		w = StringManipulationTools.stringReplace(w, " sec", "s");
		return w;
	}
	
	public static void sleep(long ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public static String getCommandLineInput() {
		String input = null;
		if (System.console() == null) {
			try {
				if (SystemAnalysis.sc == null)
					SystemAnalysis.sc = new Scanner(System.in);
				input = sc.next();
				// sc.close();
			} catch (Exception e) {
				return null;
			}
		} else {
			input = System.console().readLine();
		}
		return input;
	}
	
	private static final long MILLISECS_PER_MINUTE = 60 * 1000;
	private static final long MILLISECS_PER_HOUR = 60 * MILLISECS_PER_MINUTE;
	private static final long MILLISECS_PER_DAY = 24 * MILLISECS_PER_HOUR;
	public static final String lineSeparator = System.getProperty("line.separator");
	
	public static long getUnixDay(long time, GregorianCalendar gc) {
		long offset = gc.get(Calendar.ZONE_OFFSET) + gc.get(Calendar.DST_OFFSET);
		long day = (long) Math.floor((time + offset) / ((double) MILLISECS_PER_DAY));
		return day;
	}
	
	public static String getDataTransferSpeedString(long transfered, long start, long end) {
		return getDataTransferSpeedString(transfered, transfered, start, end);
	}
	
	/**
	 * @return Transfer speed string as in these examples: 2.4 MB/sec, 4.0 MB/min, 6 MB/hour or 0.8 MB/day.
	 */
	public static String getDataTransferSpeedString(long overallTransfered, long transfered, long start, long end) {
		if (transfered <= 0)
			return "- transfer skipped -";
		double kiloBytesPerSecond = transfered / 1024d / ((end - start) / 1000d);
		double megaBytesPerSecond = transfered / 1024d / 1024d / ((end - start) / 1000d);
		double megaBytesPerMinute = megaBytesPerSecond * 60d;
		double megaBytesPerHour = megaBytesPerMinute * 60d;
		double megaBytesPerDay = megaBytesPerHour * 24d;
		String fS = "#.#";
		String fL = "#";
		String pre = getDataAmountString(overallTransfered) + ", ";
		if (kiloBytesPerSecond < 1024)
			return pre + StringManipulationTools.formatNumber(kiloBytesPerSecond, kiloBytesPerSecond > 10 ? fL : fS) + " KB/s";
		if (megaBytesPerSecond > 1)
			return pre + StringManipulationTools.formatNumber(megaBytesPerSecond, megaBytesPerSecond > 10 ? fL : fS) + " MB/s";
		else
			if (megaBytesPerMinute > 1)
				return pre + StringManipulationTools.formatNumber(megaBytesPerMinute, megaBytesPerMinute > 10 ? fL : fS) + " MB/m";
		if (megaBytesPerHour > 1)
			return pre + StringManipulationTools.formatNumber(megaBytesPerHour, megaBytesPerHour > 10 ? fL : fS) + " MB/h";
		else
			return pre + StringManipulationTools.formatNumber(megaBytesPerDay, megaBytesPerDay > 10 ? fL : fS) + " MB/d";
	}
	
	public static boolean isMacRunning() {
		return AttributeHelper.macOSrunning();
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
			System.out.println(SystemAnalysis.getCurrentTime() + ">ERROR: " + e.getMessage());
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
	
	public static int getCurrentTimeHour() {
		Calendar c = new GregorianCalendar();
		c.setTime(new Date(System.currentTimeMillis()));
		return c.get(Calendar.HOUR_OF_DAY);
	}
	
	public static int getCurrentTimeMinute() {
		Calendar c = new GregorianCalendar();
		c.setTime(new Date(System.currentTimeMillis()));
		return c.get(Calendar.MINUTE);
	}
	
	public static String getDataAmountString(long d) {
		if (d < 0)
			return "";
		if (d == 0)
			return "0 byte";
		if (d < 1024l)
			return d + " byte";
		if (d < 1024l * 1024l)
			return d / 1024l + " KB";
		if (d < 1024l * 1024l * 1024l)
			return StringManipulationTools.formatNumber(d / 1024d / 1024d, "#.#") + " MB";
		if (d < 1024l * 1024l * 1024l * 1024l)
			return StringManipulationTools.formatNumber(d / 1024d / 1024d / 1024d, "#.#") + " GB";
		if (d < 1024l * 1024l * 1024l * 1024l * 1024l)
			return StringManipulationTools.formatNumber(d / 1024d / 1024d / 1024d / 1024d, "#.#") + " TB";
		if (d < 1024l * 1024l * 1024l * 1024l * 1024l * 1024l)
			return StringManipulationTools.formatNumber(d / 1024d / 1024d / 1024d / 1024d / 1024d, "#.#") + " PB";
		return StringManipulationTools.formatNumber(d / 1024d / 1024d / 1024d / 1024d / 1024d / 1024d, "#.#") + " EB";
	}
	
	public static String[] getEnvArray() {
		String[] res = new String[System.getenv().size()];
		Map<String, String> m = System.getenv();
		int idx = 0;
		for (String key : m.keySet()) {
			res[idx] = key + "=" + m.get(key);
			idx++;
		}
		return res;
	}
	
	public static boolean isFileOpen(String fileName) throws IOException {
		if (isWindowsRunning()) {
			File file = new File(fileName);
			return !(file.renameTo(new File(fileName + ".test_rename")) && new File(fileName + ".test_rename").renameTo(new File(fileName)));
		} else {
			File file = new File(fileName);
			Process plsof = new ProcessBuilder(new String[] { "lsof", "|", "grep", file.getAbsolutePath() }).start();
			InputStream pis = plsof.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(pis));
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.contains(file.getAbsolutePath())) {
					reader.close();
					pis.close();
					plsof.destroy();
					return true;
				}
			}
			
			reader.close();
			pis.close();
			plsof.destroy();
			
			return false;
		}
	}
	
	public static String getDesktopFolder() {
		String home = System.getProperty("user.home");
		return home + File.separator + "Desktop";
	}
	
	public static String getFileSeparator() {
		return System.getProperty("file.separator");
	}
	
	public static boolean isRetina() {
		if (isHeadless())
			return false;
		GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
		final GraphicsDevice device = env.getDefaultScreenDevice();
		
		try {
			Field field = device.getClass().getDeclaredField("scale");
			
			if (field != null) {
				field.setAccessible(true);
				Object scale = field.get(device);
				
				if (scale instanceof Integer && ((Integer) scale).intValue() == 2) {
					return true;
				}
			}
		} catch (Exception ignore) {
		}
		return false;
	}
	
	public static float getHiDPIScaleFactor() {
		if (isHeadless())
			return 1f;
		GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
		final GraphicsDevice device = env.getDefaultScreenDevice();
		
		try {
			Field field = device.getClass().getDeclaredField("scale");
			
			if (field != null) {
				field.setAccessible(true);
				Object scale = field.get(device);
				
				if (scale instanceof Integer && ((Integer) scale).intValue() == 2) {
					return ((Integer) scale).intValue();
				}
			}
		} catch (Exception ignore) {
		}
		return 1f;
	}
}
