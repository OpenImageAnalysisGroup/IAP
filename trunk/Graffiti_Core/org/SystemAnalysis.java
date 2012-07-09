package org;

import java.awt.GraphicsEnvironment;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
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
	public static int getRealNumberOfCPUs() {
		return Runtime.getRuntime().availableProcessors();
	}
	
	public static void setUseFullCpuPower(boolean b) {
		SystemAnalysis.fullPower = true;
	}
	
	public static void setUseHalfCpuPower(boolean b) {
		SystemAnalysis.halfPower = true;
	}
	
	public static int getNumberOfCPUs() {
		if (fixedCPUload > 0)
			return fixedCPUload;
		boolean useHalfCPUpower = Runtime.getRuntime().availableProcessors() > 6;
		// useHalfCPUpower = false;
		if (fullPower)
			useHalfCPUpower = false;
		if (halfPower)
			useHalfCPUpower = true;
		int cpus = Runtime.getRuntime().availableProcessors();
		if (useHalfCPUpower)
			return (int) (cpus / 2d > 0 ? cpus / 2d : 1);
		else
			return cpus;
		// return (int) (cpus * 4d / 5d) > 1 ? (int) (cpus * 4d / 5d) : cpus;
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
			if (first) {
				System.out
						.println(getCurrentTime() + ">INFO: GUI availability is simulated headless, reality: "
								+ GraphicsEnvironment.isHeadless());
				first = false;
			}
			return true;
		}
		if (first) {
			System.out.println(getCurrentTime() + ">INFO: Headless state: " + GraphicsEnvironment.isHeadless());
			first = false;
		}
		return GraphicsEnvironment.isHeadless();
	}
	
	private static SimpleDateFormat sdf = new SimpleDateFormat();
	private static DateFormat sdfInclSec = DateFormat.getTimeInstance();
	
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
	
	public static String getWaitTime(long fullTime) {
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
			Scanner sc = new Scanner(System.in);
			input = sc.next();
		} else {
			input = System.console().readLine();
		}
		return input;
	}
	
	private static final long MILLISECS_PER_MINUTE = 60 * 1000;
	private static final long MILLISECS_PER_HOUR = 60 * MILLISECS_PER_MINUTE;
	private static final long MILLISECS_PER_DAY = 24 * MILLISECS_PER_HOUR;
	
	public static long getUnixDay(long time, GregorianCalendar gc) {
		long offset = gc.get(Calendar.ZONE_OFFSET) + gc.get(Calendar.DST_OFFSET);
		long day = (long) Math.floor((time + offset) / ((double) MILLISECS_PER_DAY));
		return day;
	}
}
