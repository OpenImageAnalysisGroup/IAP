/*******************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *******************************************************************************/
/*
 * Created on Dec 5, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.gui.picture_gui;

import java.util.ArrayList;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;

import org.ErrorMsg;
import org.SystemAnalysis;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;

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
	
	private static ThreadSafeOptions runningTasks = new ThreadSafeOptions();
	private static ArrayList<MyThread> waitingTasks = new ArrayList<MyThread>(SystemAnalysis.getNumberOfCPUs());
	
	private Runnable finishTask;
	
	public MyThread(Runnable r, String name) throws InterruptedException {
		this.name = name;
		this.runCode = r;
		this.r = r;
		sem = BackgroundTaskHelper.lockGetSemaphore(null, 1);
		sem.acquire();
	}
	
	@Override
	public void run() {
		try {
			started = true;
			runningTasks.addLong(1);
			try {
				runCode.run();
			} catch (Error err1) {
				err1.printStackTrace();
				ErrorMsg.addErrorMessage(err1.getMessage());
			} catch (Exception err2) {
				ErrorMsg.addErrorMessage(err2);
			}
		} finally {
			finished = true;
			sem.release();
			runningTasks.addLong(-1);
			if (finishTask != null)
				finishTask.run();
		}
		checkWaitTasks();
	}
	
	public boolean isFinished() {
		return finished;
	}
	
	public boolean isStarted() {
		return started;
	}
	
	public Object getResult() throws InterruptedException {
		if (!started)
			startNG(null, false);
		synchronized (this) {
			if (r instanceof RunnableForResult) {
				sem.acquire();
				sem.release();
				RunnableForResult rc = (RunnableForResult) r;
				if (!finished)
					System.err.println("INTERNAL ERROR MYTHREAD 1 (NOT FINISHED!)");
				return rc.getResult();
			} else {
				sem.acquire();
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
	
	public synchronized void startNG(ThreadPoolExecutor es, boolean threadedExecution) {
		if (!started) {
			started = true;
			boolean direct = !threadedExecution;
			if (direct)
				run();
			else
				if (NEW_SCHEDULER) {
					try {
						es.execute(this);
					} catch (RejectedExecutionException rje) {
						run();
					}
				} else {
					if (runningTasks.getLong() < SystemAnalysis.getNumberOfCPUs()) {
						start();
					} else {
						if (!memorized()) {
							run();
						}
					}
				}
		}
	}
	
	private boolean memorized() {
		synchronized (waitingTasks) {
			// if (waitingTasks.size() < 20000) {// SystemAnalysis.getNumberOfCPUs()) {
			waitingTasks.add(this);
			return true;
			// } else
			// return false;
		}
	}
	
	public static void checkWaitTasks() {
		MyThread t = null;
		synchronized (waitingTasks) {
			do {
				if (waitingTasks.size() > 0 && runningTasks.getLong() < SystemAnalysis.getNumberOfCPUs()) {
					t = waitingTasks.remove(waitingTasks.size() - 1);
				}
			} while (t != null && t.started);
		}
		if (t != null)
			t.startNG(null, true);
	}
	
	public void setFinishrunnable(Runnable finishTask) {
		this.finishTask = finishTask;
	}
	
	public void messageTaskIsWaiting() {
		runningTasks.addLong(-1);
		checkWaitTasks();
	}
	
	public void messageTaskIsRunningAfterWait() {
		runningTasks.addLong(1);
	}
	
	public void memTask() {
		if (!memorized()) {
			run();
		}
	}
}
