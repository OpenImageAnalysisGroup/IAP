package de.ipk.ag_ba.gui.picture_gui.system;

import java.util.ArrayList;

import org.ErrorMsg;

import de.ipk.ag_ba.gui.picture_gui.LocalComputeJob;

public class RunnerThread extends Thread {
	
	private boolean pleaseStop;
	private final ArrayList<LocalComputeJob> jobs;
	
	public RunnerThread(ArrayList<LocalComputeJob> jobs) {
		this.jobs = jobs;
	}
	
	@Override
	public void run() {
		do {
			LocalComputeJob nextTask = null;
			synchronized (jobs) {
				if (jobs.isEmpty())
					stopRequested();
				else
					nextTask = jobs.remove(jobs.size() - 1);
			}
			try {
				if (nextTask == null)
					Thread.sleep(50);
				else {
					nextTask.run();
				}
			} catch (Exception e) {
				ErrorMsg.addErrorMessage(e);
			}
		} while (!pleaseStop);
	}
	
	public void pleaseStop() {
		this.pleaseStop = true;
	}
	
	public boolean stopRequested() {
		return pleaseStop;
	}
	
}
