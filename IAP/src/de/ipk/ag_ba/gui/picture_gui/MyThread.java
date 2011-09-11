/*******************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *******************************************************************************/
/*
 * Created on Dec 5, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.gui.picture_gui;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.Semaphore;

import org.ErrorMsg;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;

import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;

/**
 * A thread which can be checked for run completion and for its results.
 * 
 * @author klukas
 */
public class MyThread extends Thread implements Runnable {
	
	public static final boolean NEW_SCHEDULER = true;
	
	private boolean finished = false;
	private boolean started = false;
	private final Runnable r;
	private final Semaphore sem;
	private String name;
	private final Runnable runCode;
	
	public MyThread(Runnable r, String name) {
		// synchronized (MyThread.class) {
		sem = BackgroundTaskHelper.lockGetSemaphore(null, 1);
		try {
			sem.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
			ErrorMsg.addErrorMessage(e);
		}
		// }
		this.name = name;
		this.runCode = r;
		this.r = r;
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
		if (r instanceof RunnableForResult) {
			sem.acquire();
			sem.release();
			RunnableForResult rc = (RunnableForResult) r;
			if (!finished)
				System.err.println("INTERNAL ERROR MYTHREAD 1");
			return rc.getResult();
		} else {
			sem.acquire();
			sem.release();
			if (!finished)
				System.err.println("INTERNAL ERROR MYTHREAD 2");
			return null;
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
	
	private static ThreadSafeOptions tso = new ThreadSafeOptions();
	
	public void startNG(ExecutorService es) {
		started = true;
		tso.addLong(1);
		if (NEW_SCHEDULER) {
			boolean submitted = false;
			do {
				// if (tso.getLong() % 40 == 0)
				// System.out.println();
				try {
					synchronized (es) {
						// ThreadPoolExecutor tpe = (ThreadPoolExecutor) es;
						// if (tpe.getQueue().size() > SystemAnalysis.getNumberOfCPUs()) {
						// System.out.print("h");
						// start();
						// } else {
						es.submit(this);
						submitted = true;
						if (tso.getLong() % 10 == 0) {
							// System.out.print(".");
						}
						// }
					}
				} catch (RejectedExecutionException ree) {
					System.out.print("w");
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						//
					}
					// start();
				}
			} while (!submitted);
		} else
			start();
	}
}
