package de.ipk.ag_ba.gui.picture_gui.system;

import info.StopWatch;

import java.util.LinkedList;
import java.util.concurrent.Semaphore;

import org.ErrorMsg;

import de.ipk.ag_ba.gui.picture_gui.LocalComputeJob;

/**
 * @author Christian Klukas
 */
public class RunnerThread extends Thread {
	
	private boolean pleaseStop;
	private final LinkedList<LocalComputeJob> jobs;
	private final Semaphore jobModification;
	private final int index;
	private final StopWatch sw;
	private String currentTaskName = null;
	private int runtimeErrorCount = 0;
	private final StopWatch swCurrentTask;
	
	public RunnerThread(LinkedList<LocalComputeJob> jobs, Semaphore jobModification, int index) {
		this.jobs = jobs;
		this.jobModification = jobModification;
		this.index = index;
		this.sw = new StopWatch("Runner Thread " + index, false);
		this.swCurrentTask = new StopWatch(null, false);
	}
	
	@Override
	public void run() {
		int emptyCount = 0;
		do {
			LocalComputeJob nextTask = null;
			jobModification.acquireUninterruptibly();
			if (jobs.isEmpty()) {
				emptyCount++;
				if (emptyCount > 200)
					pleaseStop = true;
			} else {
				emptyCount = 0;
				pleaseStopAsked = 0;
				nextTask = jobs.removeLast();
			}
			jobModification.release();
			try {
				if (nextTask == null) {
					currentTaskName = null;
					Thread.sleep(50);
				} else {
					swCurrentTask.reset();
					currentTaskName = nextTask.getName();
					nextTask.run();
					swCurrentTask.stop();
				}
			} catch (Exception e) {
				runtimeErrorCount++;
				ErrorMsg.addErrorMessage(e);
			}
		} while (!stopRequested());
		sw.stop();
	}
	
	int pleaseStopAsked = 0;
	
	public void pleaseStop() {
		pleaseStopAsked++;
		if (pleaseStopAsked > 500)
			this.pleaseStop = true;
	}
	
	public boolean stopRequested() {
		return pleaseStop;
	}
	
	public int getIndex() {
		return index;
	}
	
	public long getUptime() {
		return sw.getTime();
	}
	
	public String getCurrentTaskName() {
		return currentTaskName;
	}
	
	public int getTaskExceptionCount() {
		return runtimeErrorCount;
	}
	
	public Long getTaskUptime() {
		if (currentTaskName != null && swCurrentTask != null)
			return swCurrentTask.getTime();
		else
			return null;
	}
	
}
