/*******************************************************************************
 * Copyright (c) 2003-2009 Plant Bioinformatics Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Feb 2, 2010 by Christian Klukas
 */

package org.graffiti.editor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

/**
 * @author klukas
 */
public abstract class MemoryHog {
	
	public MemoryHog() {
		GravistoService.addKnownMemoryHog(this);
		
		Timer t = new Timer(60000, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (doFreeMemory()) {
					freeMemory();
				}
			}
		});
		t.setRepeats(true);
		
		boolean autofree = false;
		
		if (autofree)
			t.start();
	}
	
	static long lastUsageTime = 0;
	
	protected static boolean doFreeMemory() {
		return System.currentTimeMillis() - lastUsageTime > 2000;
	}
	
	protected static void noteRequest() {
		lastUsageTime = System.currentTimeMillis();
	}
	
	public abstract void freeMemory();
	
}
