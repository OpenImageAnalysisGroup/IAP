package org;

import java.awt.GraphicsEnvironment;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.SimpleDateFormat;
import java.util.Date;

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
						.println("INFO: GUI AVAILABILITY: SIMULATED HEADLESS, HEADLESS: "
								+ GraphicsEnvironment.isHeadless());
				first = false;
			}
			return true;
		}
		if (first) {
			System.out.println("INFO: HEADLESS: "
					+ GraphicsEnvironment.isHeadless());
			first = false;
		}
		return GraphicsEnvironment.isHeadless();
	}
	
	private static SimpleDateFormat sdf = new SimpleDateFormat();
	
	public static String getCurrentTime() {
		return sdf.format(new Date());
	}
	
	public static String getCurrentTime(long time) {
		return sdf.format(new Date(time));
	}
	
	public static String getWaitTime(long fullTime) {
		ProgressStatusService pss = new ProgressStatusService();
		String res = pss.getRemainTimeString(-1, fullTime / 1000, 2);
		return StringManipulationTools.stringReplace(res, "&nbsp;", " ");
	}
}
