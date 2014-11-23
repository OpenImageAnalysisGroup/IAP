package de.ipk.ag_ba.gui.picture_gui.system;

import java.util.ArrayList;
import java.util.Collection;
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
	
	RunnerThread[] threadArray = new RunnerThread[SystemAnalysis.getRealNumberOfCPUs() * 4];
	
	private final LinkedList<LocalComputeJob> jobs = new LinkedList<LocalComputeJob>();
	private final Semaphore jobModification = new Semaphore(1, true);
	
	private final ThreadSafeOptions tsoStopRequestCount = new ThreadSafeOptions();
	
	private final ThreadSafeOptions runningTasks = new ThreadSafeOptions();
	
	private ThreadManager() {
		Timer res = new Timer("Background Thread Management", true);
		res.scheduleAtFixedRate(new TimerTask() {
			boolean[] started = new boolean[SystemAnalysis.getRealNumberOfCPUs() * 4];
			
			@Override
			public void run() {
				runningTasks.setInt(getRunningCount());
				if (started.length != SystemAnalysis.getRealNumberOfCPUs() * 4) {
					boolean[] new_started = new boolean[SystemAnalysis.getRealNumberOfCPUs() * 4];
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
					
					int desiredThreadCount = SystemAnalysis.getNumberOfCPUs();
					int idleTasks = 0;
					boolean checkForIdle = SystemOptions.getInstance().getBoolean("SYSTEM", "Detect Idle Tasks", true);
					if (checkForIdle) {
						for (RunnerThread t : threadArray) {
							if (t == null)
								continue;
							Long tu = t.getTaskUptime();
							if (tu != null && tu < 5000)
								continue;
							if ((t.getState() == Thread.State.BLOCKED ||
									t.getState() == Thread.State.WAITING ||
							t.getState() == Thread.State.TIMED_WAITING)) {
								idleTasks++;
							}
						}
					}
					if (idleTasks > 0)
						desiredThreadCount += idleTasks;
					desiredThreadCount = modifyConcurrencyDependingOnMemoryStatus(desiredThreadCount);
					
					int maxCnt = Math.min(Math.min(jobs.size(), desiredThreadCount), threadArray.length);
					if (getRunningCount() > maxCnt) {
						// ask threads to stop execution
						int tooMany = getRunningCount() - maxCnt;
						tsoStopRequestCount.setInt(tooMany);
					} else
						tsoStopRequestCount.setInt(0);
					while (getRunningCount() < maxCnt) {
						// start new executor threads
						int idx = 0;
						ArrayList<RunnerThread> toBeStarted = new ArrayList<>();
						for (boolean s : started) {
							if (!s) {
								threadArray[idx] = new RunnerThread(jobs, jobModification, idx, tsoStopRequestCount);
								started[idx] = true;
								threadArray[idx].setDaemon(false);
								threadArray[idx].setName("Background Thread " + (idx + 1));
								toBeStarted.add(threadArray[idx]);
							}
							idx++;
							if (getRunningCount() + toBeStarted.size() >= maxCnt)
								break;
						}
						for (RunnerThread tbs : toBeStarted)
							tbs.start();
					}
				}
				for (int idx = 0; idx < threadArray.length; idx++) {
					if (started[idx] && !threadArray[idx].isAlive()) {
						threadArray[idx] = null;
						started[idx] = false;
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
				return nn;
			}
			
			private int getRunningCount() {
				int res = 0;
				for (boolean s : started)
					if (s)
						res++;
				return res;
			}
		}, 100, 100);
	}
	
	public static ThreadManager getInstance() {
		return instance;
	}
	
	private static long lastPrint = 0;
	private static long lastGC = 0;
	
	private static long runIdx = 0;
	
	public void memTask(LocalComputeJob t, boolean forceMem) {
		memTask(t, forceMem, SystemAnalysis.getRealNumberOfCPUs() * 4);
	}
	
	public void memTask(LocalComputeJob t, boolean forceMem, int maxWait) {
		boolean run = false;
		if (maxWait < 0)
			maxWait = -maxWait;
		if (forceMem) {
			jobModification.acquireUninterruptibly();
			jobs.addFirst(t);
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
		int v = runningTasks.getInt();
		return v;
	}
	
	public Collection<RunnerThread> getRunningTasks() {
		Collection<RunnerThread> res = new LinkedList<>();
		for (RunnerThread t : threadArray)
			res.add(t);
		return res;
	}
	
	public int getTooMany() {
		return tsoStopRequestCount.getInt();
	}
}
