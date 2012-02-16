/*******************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *******************************************************************************/
/*
 * Created on Nov 25, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.server.task_management;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.ErrorMsg;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;

/**
 * @author klukas
 */
public class ThreadService {
	private static ThreadSafeOptions tsoTaskID = new ThreadSafeOptions();
	
	public long getTaskGroupID() {
		return tsoTaskID.getNextLong();
	}
	
	public static ExecutorService getService(final String name, int maximumThreadCount) {
		final ThreadSafeOptions tsoLA = new ThreadSafeOptions();
		ExecutorService run = Executors.newFixedThreadPool(maximumThreadCount, new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r);
				int i;
				synchronized (tsoLA) {
					tsoLA.addInt(1);
					i = tsoLA.getInt();
				}
				t.setName(name + " (" + i + ")");
				return t;
			}
		});
		return run;
	}
	
	public boolean waitToFinish(ExecutorService run) {
		run.shutdown();
		try {
			run.awaitTermination(365, TimeUnit.DAYS);
			return true;
		} catch (InterruptedException e) {
			ErrorMsg.addErrorMessage(e);
			return false;
		}
		
	}
}
