package de.ipk.ag_ba.server.task_management;

import org.SystemAnalysis;
import org.SystemInfo;

public class SystemInfoExt extends SystemInfo {
	
	private final double load;
	private final int cpuCountAvailable;
	private final int cpuSockets;
	private final int cpuPhysicalCores;
	private final int cpuLogicalCores;
	private final long physMemInGB;
	
	public SystemInfoExt() {
		load = SystemAnalysisExt.getRealSystemCpuLoad();
		cpuSockets = SystemAnalysisExt.getNumberOfCpuSockets();
		cpuPhysicalCores = SystemAnalysisExt.getNumberOfCpuPhysicalCores();
		cpuLogicalCores = SystemAnalysisExt.getNumberOfCpuLogicalCores();
		cpuCountAvailable = SystemAnalysis.getRealNumberOfCPUs();
		physMemInGB = SystemAnalysisExt.getPhysicalMemoryInGB();
	}
	
	public double getLoad() {
		return load;
	}
	
	public int getCpuCountAvailable() {
		return cpuCountAvailable;
	}
	
	public int getCpuSockets() {
		return cpuSockets;
	}
	
	public int getPhysicalCoresPerSocket() {
		return getCpuPhysicalCores() / getCpuSockets();
	}
	
	public int getCpuPhysicalCores() {
		return cpuPhysicalCores;
	}
	
	public int getCpuLogicalCores() {
		return cpuLogicalCores;
	}
	
	/**
	 * @return 1 = no HT, 2 = 2 threads per physical core
	 */
	public int getHyperThreadingFactor() {
		return getCpuLogicalCores() / getCpuPhysicalCores();
	}
	
	public long getPhysicalMemoryInGB() {
		return physMemInGB;
	}
	
}
