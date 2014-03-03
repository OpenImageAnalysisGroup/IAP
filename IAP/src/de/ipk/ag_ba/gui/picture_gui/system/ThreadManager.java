package de.ipk.ag_ba.gui.picture_gui.system;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;

import org.SystemAnalysis;
import org.SystemOptions;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;

import de.ipk.ag_ba.gui.picture_gui.LocalComputeJob;

public class ThreadManager {
	
	private static ThreadManager instance = new ThreadManager();
	
	SystemOptions so = SystemOptions.getInstance();
	
	RunnerThread[] threadArray = new RunnerThread[SystemAnalysis.getRealNumberOfCPUs() * 2];
	
	private final LinkedList<LocalComputeJob> jobs = new LinkedList<LocalComputeJob>();
	private final Semaphore jobModification = new Semaphore(1, true);
	
	private final ThreadSafeOptions runningTasks = new ThreadSafeOptions();
	
	private ThreadManager() {
		Timer res = new Timer("Background Thread Management", true);
		res.scheduleAtFixedRate(new TimerTask() {
			boolean[] started = new boolean[SystemAnalysis.getRealNumberOfCPUs() * 2];
			
			@Override
			public void run() {
				runningTasks.setInt(getRunningCount());
				if (started.length != SystemAnalysis.getRealNumberOfCPUs() * 2) {
					boolean[] new_started = new boolean[SystemAnalysis.getRealNumberOfCPUs() * 2];
					for (int i = 0; i < started.length; i++) {
						if (i < new_started.length)
							new_started[i] = started[i];
					}
					started = new_started;
				}
				if (jobs.size() > 0) {
					ArrayList<LocalComputeJob> remove = new ArrayList<LocalComputeJob>();
					jobModification.acquireUninterruptibly();
					for (LocalComputeJob j : jobs)
						if (j.isFinished())
							remove.add(j);
					for (LocalComputeJob j : remove)
						jobs.remove(j);
					jobModification.release();
					
					int desiredThreadCount = SystemAnalysis.getNumberOfCPUs() - 1;
					desiredThreadCount = modifyConcurrencyDependingOnMemoryStatus(desiredThreadCount);
					
					int maxCnt = Math.min(Math.min(jobs.size(), desiredThreadCount), threadArray.length);
					if (getRunningCount() > maxCnt) {
						// ask threads to stop execution
						int tooMany = getRunningCount() - maxCnt;
						int askedToStop = 0;
						if (tooMany > 0) {
							for (int idx = threadArray.length - 1; idx >= 0; idx--) {
								if (started[idx]) {
									threadArray[idx].pleaseStop();
									askedToStop++;
								}
								if (askedToStop >= tooMany)
									break;
							}
						}
					} else
						while (getRunningCount() < maxCnt) {
							// start new executor threads
							int idx = 0;
							for (boolean s : started) {
								if (!s) {
									threadArray[idx] = new RunnerThread(jobs, jobModification);
									started[idx] = true;
									threadArray[idx].setDaemon(false);
									threadArray[idx].setName("Background Thread " + (idx + 1));
									threadArray[idx].start();
								}
								idx++;
								if (getRunningCount() >= maxCnt)
									break;
							}
						}
					for (int idx = 0; idx < threadArray.length; idx++) {
						if (started[idx] && threadArray[idx].stopRequested() && !threadArray[idx].isAlive()) {
							threadArray[idx] = null;
							started[idx] = false;
						}
					}
				}
			}
			
			private int modifyConcurrencyDependingOnMemoryStatus(int nn) {
				SystemOptions.getInstance().getInteger("SYSTEM", "Reduce Workload Memory Usage Threshold Percent", 70);
				
				if (SystemOptions.getInstance().getBoolean("SYSTEM", "Reduce Workload in Low Memory Situation", false)) {
					if (SystemAnalysis.getMemoryMB() < 1500 && nn > 1) {
						System.out
								.println(SystemAnalysis.getCurrentTime()
										+ ">LOW SYSTEM MEMORY (less than 1500 MB), LIMITING CONCURRENCY TO 1");
						nn = 1;
					}
					if (SystemAnalysis.getMemoryMB() < 2000 && nn > 1) {
						System.out
								.println(SystemAnalysis.getCurrentTime()
										+ ">LOW SYSTEM MEMORY (less than 2000 MB), LIMITING CONCURRENCY TO 1");
						nn = 1;
					}
					if (SystemAnalysis.getMemoryMB() < 4000 && nn > 2) {
						System.out
								.println(SystemAnalysis.getCurrentTime()
										+ ">LOW SYSTEM MEMORY (less than 4000 MB), LIMITING CONCURRENCY TO 2");
						nn = 2;
					}
					
					if (nn > 1
							&& SystemAnalysis.getUsedMemoryInMB() > SystemAnalysis
									.getMemoryMB() * (double) SystemOptions.getInstance().getInteger("SYSTEM", "Reduce Workload Memory Usage Threshold Percent", 70)
									/ 100d) {
						System.out.println(SystemAnalysis.getCurrentTime()
								+ ">HIGH MEMORY UTILIZATION (>"
								+ SystemOptions.getInstance().getInteger("SYSTEM", "Reduce Workload Memory Usage Threshold Percent", 70)
								+ "%), REDUCING CONCURRENCY");
						nn = nn / 2;
					}
				}
				if (nn < 0)
					nn = 0;
				// System.out
				// .println(SystemAnalysis.getCurrentTime()
				// + ">SERIAL SNAPSHOT ANALYSIS... (max concurrent thread count: "
				// + nn + ")");
				return nn;
			}
			
			private int getRunningCount() {
				int res = 0;
				for (boolean s : started)
					if (s)
						res++;
				return res;
			}
		}, 1000, 1000);
	}
	
	public static ThreadManager getInstance() {
		return instance;
	}
	
	private static long lastPrint = 0;
	private static long lastGC = 0;
	
	private static long runIdx = 0;
	
	public void memTask(LocalComputeJob t, boolean forceMem) {
		boolean run = false;
		if (forceMem) {
			jobModification.acquireUninterruptibly();
			jobs.add(t);
			jobModification.release();
		} else {
			runIdx++;
		}
		if (runIdx > 2000)
			runIdx = 0;
		if (runIdx > 1000 && SystemAnalysis.getUsedMemoryInMB() > SystemAnalysis
				.getMemoryMB() * (double) SystemOptions.getInstance().getInteger("SYSTEM", "Issue GC Memory Usage Threshold Percent", 60) / 100d
				&& SystemOptions.getInstance().getBoolean("SYSTEM", "Issue GC upon high memory use", false)) {
			if (System.currentTimeMillis() - lastPrint > 1000) {
				lastPrint = System.currentTimeMillis();
				System.out
						.print(SystemAnalysis.getCurrentTime()
								+ ">HIGH MEMORY UTILIZATION (>" + SystemOptions.getInstance().getInteger("SYSTEM", "Issue GC Memory Usage Threshold Percent", 60)
								+ "%), Memory Load: " + SystemAnalysis.getUsedMemoryInMB() + "/" + SystemAnalysis.getMemoryMB() + " MB");
			}
			if (System.currentTimeMillis() - lastGC > 1000 * 30) {
				lastGC = System.currentTimeMillis();
				System.out.println();
				System.out
						.print(SystemAnalysis.getCurrentTime()
								+ ">ISSUE GARBAGE COLLECTION (" + SystemAnalysis.getUsedMemoryInMB() + "/" + SystemAnalysis.getMemoryMB() + " MB)... ");
				System.gc();
				System.out.println("FINISHED GC (" + SystemAnalysis.getUsedMemoryInMB() + "/" + SystemAnalysis
						.getMemoryMB() + " MB)");
			}
		}
		int maxWait = SystemAnalysis.getRealNumberOfCPUs() * 4;
		jobModification.acquireUninterruptibly();
		if (jobs.size() > maxWait)
			run = true;
		else {
			jobs.add(t);
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		jobModification.release();
		
		if (run)
			t.run();
	}
	
	public void directlyRunning(LocalComputeJob localComputeJob) {
		jobModification.acquireUninterruptibly();
		jobs.remove(localComputeJob);
		jobModification.release();
	}
	
	public int getNumberOfEnquedOrRunningTasks() {
		int cnt2;
		jobModification.acquireUninterruptibly();
		cnt2 = jobs.size();
		jobModification.release();
		int cnt = runningTasks.getInt();
		// System.out.println(SystemAnalysis.getCurrentTime() + ">INFO: Current Workload=" + cnt + ";" + cnt2 + ", CPU_CNT=" + SystemAnalysis.getNumberOfCPUs());
		return cnt + cnt2;
		// int cnt;
		// jobModification.acquireUninterruptibly();
		// cnt = jobs.size();
		// jobModification.release();
		// System.out.println(SystemAnalysis.getCurrentTime() + ">INFO: Current Workload=" + cnt + ", CPU_CNT=" + SystemAnalysis.getNumberOfCPUs());
		// return cnt;
	}
	
	public int getNumberOfRunningBackgroundTasks() {
		return runningTasks.getInt();
	}
}
