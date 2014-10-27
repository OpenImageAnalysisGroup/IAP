package de.ipk.ag_ba.gui.picture_gui.system;

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
	
	public RunnerThread(LinkedList<LocalComputeJob> jobs, Semaphore jobModification) {
		this.jobs = jobs;
		this.jobModification = jobModification;
	}
	
	@Override
	public void run() {
		do {
			LocalComputeJob nextTask = null;
			jobModification.acquireUninterruptibly();
			if (jobs.isEmpty())
				pleaseStop = true;
			else {
				nextTask = jobs.removeLast();
			}
			jobModification.release();
			try {
				if (nextTask == null)
					Thread.sleep(50);
				else {
					nextTask.run();
				}
			} catch (Exception e) {
				ErrorMsg.addErrorMessage(e);
			}
		} while (!stopRequested());
	}
	
	public void pleaseStop() {
		this.pleaseStop = true;
	}
	
	public boolean stopRequested() {
		return pleaseStop;
	}
	
}
