/*******************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *******************************************************************************/
/*
 * Created on Dec 5, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.gui.picture_gui;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
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
	private final Semaphore sem;
	private String name;
	private Runnable runCode;
	
	private static ThreadSafeOptions runningTasks = new ThreadSafeOptions();
	private static ArrayList<MyThread> waitingTasks = new ArrayList<MyThread>(SystemAnalysis.getNumberOfCPUs());
	
	private Runnable finishTask;
	
	private boolean mem;
	
	private boolean isRunningInThread;
	
	private Object runableResult;
	
	@SuppressWarnings("unused")
	private static Timer waitThread = initTimer();
	
	public MyThread(Runnable r, String name) throws InterruptedException {
		this.name = name;
		this.runCode = r;
		if (r == null)
			System.out.println("ERR");
		sem = BackgroundTaskHelper.lockGetSemaphore(null, 1);
		sem.acquire();
	}
	
	private static Timer initTimer() {
		Timer res = new Timer("Wait Thread Check", true);
		res.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				MyThread.checkWaitTasks();
			}
		}, 100, 100);
		return res;
	}
	
	@Override
	public void run() {
		try {
			started = true;
			runningTasks.addLong(1);
			try {
				Runnable r = runCode;
				runCode = null;
				if (r == null)
					System.out.println("ERR");
				r.run();
				if (r instanceof RunnableForResult)
					runableResult = ((RunnableForResult) r).getResult();
			} catch (Error err1) {
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
		if (isRunningInThread) {
			MyThread t;
			do {
				t = getWaitingTask();
				if (t != null && !t.started)
					t.run();
				else {
					try {
						Thread.sleep(20);
						t = getWaitingTask();
					} catch (InterruptedException e) {
						ErrorMsg.addErrorMessage(e);
					}
				}
			} while (t != null);
		}
	}
	
	@Override
	public synchronized void start() {
		this.isRunningInThread = true;
		this.started = true;
		super.start();
	}
	
	public boolean isFinished() {
		return finished;
	}
	
	public boolean isStarted() {
		return started;
	}
	
	public Object getResult() throws InterruptedException {
		if (!started)
			run();
		synchronized (this) {
			sem.acquire();
			sem.release();
			if (!finished)
				ErrorMsg.addErrorMessage("INTERNAL ERROR MYTHREAD (NOT FINISHED!)");
			Object res = runableResult;
			runableResult = null;
			return res;
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
		if (!started && !mem) {
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
						started = true;
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
		mem = true;
		synchronized (waitingTasks) {
			if (waitingTasks.size() < SystemAnalysis.getNumberOfCPUs()) {
				waitingTasks.add(this);
				return true;
			} else
				return false;
		}
	}
	
	public static void checkWaitTasks() {
		MyThread t = null;
		synchronized (waitingTasks) {
			do {
				if (waitingTasks.size() > 0 && runningTasks.getLong() < SystemAnalysis.getNumberOfCPUs()) {
					t = waitingTasks.remove(waitingTasks.size() - 1);
				} else
					t = null;
			} while (t != null && t.started);
			if (t != null && !t.started)
				t.start();
		}
	}
	
	public static MyThread getWaitingTask() {
		MyThread t = null;
		synchronized (waitingTasks) {
			do {
				if (waitingTasks.size() > 0 && runningTasks.getLong() < 4 * SystemAnalysis.getNumberOfCPUs()) {
					t = waitingTasks.remove(waitingTasks.size() - 1);
				} else
					t = null;
			} while (t != null && t.started);
			if (t != null && !t.started)
				return t;
			else
				return null;
		}
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
