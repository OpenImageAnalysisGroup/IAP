package de.ipk.ag_ba.gui.picture_gui.system;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import org.SystemAnalysis;
import org.SystemOptions;

import de.ipk.ag_ba.gui.picture_gui.LocalComputeJob;

public class ThreadExecution {
	
	private static ThreadExecution instance = new ThreadExecution();
	
	SystemOptions so = SystemOptions.getInstance();
	
	RunnerThread[] threadArray = new RunnerThread[SystemAnalysis.getRealNumberOfCPUs() * 2];
	
	private final ArrayList<LocalComputeJob> jobs = new ArrayList<LocalComputeJob>();
	
	private ThreadExecution() {
		Timer res = new Timer("ThreadExecution", true);
		res.scheduleAtFixedRate(new TimerTask() {
			boolean[] started = new boolean[SystemAnalysis.getRealNumberOfCPUs() * 2];
			
			@Override
			public void run() {
				if (jobs.size() > 0) {
					int desiredThreadCount = SystemAnalysis.getNumberOfCPUs();
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
									threadArray[idx] = new RunnerThread(jobs);
									started[idx] = true;
									threadArray[idx].setDaemon(false);
									threadArray[idx].setName("exec " + (idx + 1));
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
			
			private int getRunningCount() {
				int res = 0;
				for (boolean s : started)
					if (s)
						res++;
				return res;
			}
		}, 1000, 1000);
	}
	
	public static ThreadExecution getInstance() {
		return instance;
	}
	
	public void memTask(LocalComputeJob t) {
		synchronized (jobs) {
			jobs.add(t);
		}
	}
}
