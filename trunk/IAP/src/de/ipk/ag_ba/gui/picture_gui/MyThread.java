/*******************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *******************************************************************************/
/*
 * Created on Dec 5, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.gui.picture_gui;


/**
 * @author klukas
 */
public class MyThread extends Thread {
	
	private boolean finished = false;
	private final Runnable r;
	
	public MyThread(Runnable r, String name) {
		super(r, name);
		this.r = r;
	}
	
	@Override
	public void run() {
		try {
			super.run();
		} finally {
			finished = true;
		}
	}
	
	public boolean isFinished() {
		return finished;
	}
	
	public Object getResult() {
		BackgroundThreadDispatcher.waitFor(new MyThread[] { this });
		RunnableForResult rc = (RunnableForResult) r;
		return rc.getResult();
	}
}
