/*******************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *******************************************************************************/
/*
 * Created on Dec 5, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.gui.picture_gui;

import java.util.concurrent.Semaphore;

import org.ErrorMsg;
import org.ObjectRef;
import org.SystemAnalysis;

import de.ipk.ag_ba.gui.picture_gui.system.ThreadManager;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;

/**
 * A thread which can be checked for run completion and for its results.
 * 
 * @author klukas
 */
public class LocalComputeJob implements Runnable {
	private boolean finished = false;
	private final Semaphore sem;
	private final String name;
	private final ObjectRef runCode;
	private Runnable finishTask;
	
	private Object runableResult;
	
	public LocalComputeJob(Runnable r, String name) throws InterruptedException {
		this.name = name;
		this.runCode = new ObjectRef();
		runCode.setObject(r);
		sem = BackgroundTaskHelper.lockGetSemaphore(null, 1);
		sem.acquire();
	}
	
	@Override
	public void run() {
		Runnable r;
		synchronized (runCode) {
			if (isStarted()) {
				return;
			} else
				r = (Runnable) runCode.removeObject();
		}
		
		try {
			try {
				if (r == null)
					System.out.println(SystemAnalysis.getCurrentTime() + ">ERROR: INTERNAL ERROR - MYTHREAD RUNCODE IS NULL");
				r.run();
				if (r instanceof RunnableForResult)
					runableResult = ((RunnableForResult) r).getResult();
			} catch (Error err1) {
				err1.printStackTrace();
				ErrorMsg.addErrorMessage(err1.getMessage());
			} catch (Exception err2) {
				ErrorMsg.addErrorMessage(err2);
			}
		} finally {
			finished = true;
			sem.release();
			if (finishTask != null)
				finishTask.run();
		}
	}
	
	public boolean isFinished() {
		return finished;
	}
	
	public boolean isStarted() {
		return runCode.getObject() == null;
	}
	
	public Object getResult() throws InterruptedException {
		if (!isStarted()) {
			ThreadManager.getInstance().directlyRunning(this);
			run();
		}
		sem.acquire();
		sem.release();
		if (!finished)
			ErrorMsg.addErrorMessage("INTERNAL ERROR MYTHREAD (NOT FINISHED!)");
		synchronized (this) {
			Object res = runableResult;
			runableResult = null;
			return res;
		}
	}
	
	@Override
	public String toString() {
		return "Background Task " + name;
	}
}
