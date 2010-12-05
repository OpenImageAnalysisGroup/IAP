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

	public MyThread(Runnable r, String name) {
		super(r, name);
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
}
