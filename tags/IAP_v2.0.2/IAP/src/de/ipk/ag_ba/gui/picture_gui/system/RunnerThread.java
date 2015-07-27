package de.ipk.ag_ba.gui.picture_gui.system;

import info.StopWatch;

import java.util.LinkedList;
import java.util.concurrent.Semaphore;

import org.ErrorMsg;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;

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
	private long runtimeErrorCount = 0;
	private final StopWatch swCurrentTask;
	private long runtimeExecCount = 0;
	private final ThreadSafeOptions tsoStopRequestCount;
	
	public RunnerThread(LinkedList<LocalComputeJob> jobs, Semaphore jobModification, int index, ThreadSafeOptions tsoStopRequestCount) {
		this.jobs = jobs;
		this.jobModification = jobModification;
		this.index = index;
		this.tsoStopRequestCount = tsoStopRequestCount;
		this.sw = new StopWatch("Runner Thread " + index, false);
		this.swCurrentTask = new StopWatch(null, false);
	}
	
	@Override
	public void run() {
		do {
			LocalComputeJob nextTask = null;
			jobModification.acquireUninterruptibly();
			if (jobs.isEmpty()) {
				pleaseStop = true;
			} else {
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
					runtimeExecCount++;
					swCurrentTask.stop();
				}
			} catch (Exception e) {
				runtimeErrorCount++;
				ErrorMsg.addErrorMessage(e);
			}
		} while (!pleaseStop && tsoStopRequestCount.getInt() <= 0);
		if (tsoStopRequestCount.getInt() > 0)
			tsoStopRequestCount.addInt(-1);
		sw.stop();
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
	
	public long getTaskExceptionCount() {
		return runtimeErrorCount;
	}
	
	public Long getTaskUptime() {
		if (currentTaskName != null && swCurrentTask != null)
			return swCurrentTask.getTime();
		else
			return null;
	}
	
	public long getTaskRuntimeCount() {
		return runtimeExecCount;
	}
}
