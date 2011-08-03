/*******************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *******************************************************************************/
/*
 * Created on Dec 5, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.gui.picture_gui;

import java.util.concurrent.Semaphore;

import org.ErrorMsg;

import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;

/**
 * A thread which can be checked for run completion and for its results.
 * 
 * @author klukas
 */
public class MyThread implements Runnable { // extends Thread {

	private boolean finished = false;
	private final Runnable r;
	private final Semaphore sem;
	private String name;
	private Runnable runCode;
	
	public MyThread(Runnable r, String name) {
		// super(r, name);
		this.name = name;
		this.runCode = r;
		sem = BackgroundTaskHelper.lockGetSemaphore(null, 1);
		try {
			sem.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
			ErrorMsg.addErrorMessage(e);
		}
		this.r = r;
	}
	
	@Override
	public void run() {
		try {
			// super.run();
			runCode.run();
		} catch (Error err1) {
			err1.printStackTrace();
		} catch (Exception err2) {
			err2.printStackTrace();
		} finally {
			finished = true;
			sem.release();
		}
	}
	
	public boolean isFinished() {
		return finished;
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
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public boolean isAlive() {
		return !finished;
	}
	
	public Runnable getRunCode() {
		return this;
	}
}
