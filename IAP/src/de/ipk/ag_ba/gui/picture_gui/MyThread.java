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
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;

import org.ErrorMsg;
import org.ObjectRef;
import org.SystemAnalysis;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;

import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;

/**
 * A thread which can be checked for run completion and for its results.
 * 
 * @author klukas
 */
public class MyThread extends Thread implements Runnable {
	private boolean finished = false;
	private final Semaphore sem;
	private String name;
	private final ObjectRef runCode;
	
	private static ThreadSafeOptions runningTasks = new ThreadSafeOptions();
	private static ArrayList<MyThread> waitingTasks = new ArrayList<MyThread>(SystemAnalysis.getNumberOfCPUs());
	
	private Runnable finishTask;
	
	private boolean mem;
	
	private Object runableResult;
	
	@SuppressWarnings("unused")
	private static Timer waitThread = initTimer();
	
	public MyThread(Runnable r, String name) throws InterruptedException {
		this.name = name;
		this.runCode = new ObjectRef();
		runCode.setObject(r);
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
		Runnable r;
		synchronized (runCode) {
			boolean tt = isRunningInThread();
			if (isStarted()) {
				if (tt)
					System.out.print(">W");
				return;
			} else
				r = (Runnable) runCode.removeObject();
			if (tt)
				System.out.print(">START");
		}
		
		try {
			runningTasks.addLong(1);
			try {
				if (r == null)
					System.out.println(SystemAnalysis.getCurrentTime() + ">ERROR: INTERNAL ERROR - MYTHREAD RUNCODE IS NULL");
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
		if (isRunningInThread()) {
			MyThread t;
			do {
				t = getWaitingTask();
				if (t != null && !t.isStarted()) {
					try {
						Thread.sleep(5);
					} catch (InterruptedException e) {
						ErrorMsg.addErrorMessage(e);
					}
					t.run();
				} else {
					try {
						Thread.sleep(20);
						t = getWaitingTask();
						if (t != null && !t.isStarted())
							t.run();
					} catch (InterruptedException e) {
						ErrorMsg.addErrorMessage(e);
					}
				}
			} while (t != null);
		}
	}
	
	private boolean isRunningInThread() {
		// System.out.println("Stack Size: " + Thread.currentThread().getStackTrace().length);
		return Thread.currentThread().getStackTrace().length < 6;
	}
	
	@Override
	public synchronized void start() {
		synchronized (runCode) {
			if (runCode.getObject() == null) {
				return;
			} else {
				super.start();
			}
		}
	}
	
	public boolean isFinished() {
		return finished;
	}
	
	public boolean isStarted() {
		return runCode.getObject() == null;
	}
	
	public Object getResult() throws InterruptedException {
		if (!isStarted())
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
		if (!isStarted() && !mem) {
			boolean direct = !threadedExecution;
			if (direct)
				run();
			else
				if (runningTasks.getLong() < SystemAnalysis.getNumberOfCPUs()) {
					start();
				} else {
					if (!memorized(false)) {
						run();
					}
				}
		}
	}
	
	private boolean memorized(boolean forceMem) {
		mem = true;
		synchronized (waitingTasks) {
			if (forceMem || waitingTasks.size() < SystemAnalysis.getNumberOfCPUs() * 4) {
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
			} while (t != null && t.isStarted());
			if (t != null && !t.isStarted()) {
				t.start();
			}
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
			} while (t != null && t.isStarted());
			if (t != null && !t.isStarted())
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
		if (!memorized(false)) {
			run();
		}
	}
	
	public void memTask(boolean forceMem) {
		if (!memorized(forceMem)) {
			run();
		}
	}
}
