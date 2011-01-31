/*******************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *******************************************************************************/
/*
 * Created on Dec 5, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.gui.picture_gui;

import org.ErrorMsg;

import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BoundedSemaphore;

/**
 * @author klukas
 */
public class MyThread extends Thread {
	
	private boolean finished = false;
	private final Runnable r;
	private final BoundedSemaphore sem;
	
	public MyThread(Runnable r, String name) {
		super(r, name);
		sem = BackgroundTaskHelper.lockGetSemaphore(null, 1);
		try {
			sem.take(name);
		} catch (InterruptedException e) {
			e.printStackTrace();
			ErrorMsg.addErrorMessage(e);
		}
		this.r = r;
	}
	
	@Override
	public void run() {
		try {
			super.run();
		} finally {
			finished = true;
			try {
				sem.release(Thread.currentThread().getName());
			} catch (InterruptedException e) {
				e.printStackTrace();
				ErrorMsg.addErrorMessage(e);
			}
		}
	}
	
	public boolean isFinished() {
		return finished;
	}
	
	public Object getResult() throws InterruptedException {
		if (r instanceof RunnableForResult) {
			sem.take(Thread.currentThread().getName());
			sem.release(Thread.currentThread().getName());
			RunnableForResult rc = (RunnableForResult) r;
			if (!finished)
				System.out.println("ERRRRRRR");
			return rc.getResult();
		} else {
			sem.take(Thread.currentThread().getName());
			sem.release(Thread.currentThread().getName());
			if (!finished)
				System.out.println("ERRRRRRR");
			return null;
		}
	}
}
