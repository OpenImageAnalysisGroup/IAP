/*******************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *******************************************************************************/
/*
 * Created on Dec 5, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.gui.picture_gui;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;

import de.ipk.ag_ba.server.task_management.SystemAnalysisExt;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;

/**
 * A thread which can be checked for run completion and for its results.
 * 
 * @author klukas
 */
public class MyThread extends Thread implements Runnable {
	
	public static final boolean NEW_SCHEDULER = false;
	
	private boolean finished = false;
	private boolean started = false;
	private final Runnable r;
	private final Semaphore sem;
	private String name;
	private final Runnable runCode;
	
	public MyThread(Runnable r, String name) {
		this.name = name;
		this.runCode = r;
		this.r = r;
		sem = BackgroundTaskHelper.lockGetSemaphore(null, 1);
		do {
			try {
				sem.acquire();
			} catch (InterruptedException ie) {
				System.out.println("xi");
			}
		} while (sem.availablePermits() > 0);
	}
	
	@Override
	public void run() {
		try {
			started = true;
			// super.run();
			runCode.run();
			// } catch (Error err1) {
			// err1.printStackTrace();
			// } catch (Exception err2) {
			// err2.printStackTrace();
		} finally {
			finished = true;
			sem.release();
		}
		// System.out.print("f");
	}
	
	public boolean isFinished() {
		return finished;
	}
	
	public boolean isStarted() {
		return started;
	}
	
	public Object getResult() throws InterruptedException {
		synchronized (this) {
			if (!started)
				run();
			
			if (r instanceof RunnableForResult) {
				sem.acquire();
				sem.release();
				RunnableForResult rc = (RunnableForResult) r;
				if (!finished)
					System.err.println("INTERNAL ERROR MYTHREAD 1 (NOT FINISHED!)");
				return rc.getResult();
			} else {
				do {
					try {
						sem.acquire();
					} catch (InterruptedException ie) {
						// empty
					}
				} while (!finished);
				sem.release();
				if (!finished)
					System.err.println("INTERNAL ERROR MYTHREAD 2 (NOT FINISHED)");
				return null;
			}
		}
	}
	
	public String getNameNG() {
		return name;
	}
	
	public void setNameNG(String name) {
		this.name = name;
	}
	
	public boolean isAliveNG() {
		return !finished;
	}
	
	public Runnable getRunCode() {
		return this;
	}
	
	public void setPriorityNG(int minPriority) {
		setPriority(minPriority);
	}
	
	@Override
	public String toString() {
		return "Background Task " + name;
	}
	
	public void startNG(ExecutorService es) {
		if (!started) {
			started = true;
			if (NEW_SCHEDULER) {
				synchronized (es) {
					System.out.println(SystemAnalysisExt.getCurrentTime() + ">INFO: SUBMITTED TASK " + name + " TO EXECUTION TASK QUEUE");
					es.submit(this);
				}
			} else {
				start();
			}
		}
	}
}
