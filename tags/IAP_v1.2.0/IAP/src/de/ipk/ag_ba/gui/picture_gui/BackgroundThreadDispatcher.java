package de.ipk.ag_ba.gui.picture_gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashSet;

import javax.swing.Timer;

import org.SystemAnalysis;

import de.ipk.ag_ba.gui.picture_gui.system.ThreadManager;
import de.ipk.ag_ba.image.operations.blocks.BlockPipeline;

/**
 * @author klukas
 */
public class BackgroundThreadDispatcher {
	int indicator = 0;
	
	public static String projectLoading = " PLEASE WAIT - Loading Experimental Data";
	
	private static BackgroundThreadDispatcher myInstance = new BackgroundThreadDispatcher();
	
	private StatusDisplay frame;
	
	public static void setFrameInstance(StatusDisplay mainFrame) {
		synchronized (myInstance) {
			if (myInstance == null)
				myInstance = new BackgroundThreadDispatcher();
			myInstance.frame = mainFrame;
		}
	}
	
	public static LocalComputeJob addTask(Runnable r, String name) throws InterruptedException {
		return addTask(r, name, false);
	}
	
	public static LocalComputeJob addTask(Runnable r, String name, boolean forceMem) throws InterruptedException {
		return addTask(new LocalComputeJob(r, name), forceMem);
	}
	
	public static LocalComputeJob addTask(LocalComputeJob t) {
		return addTask(t, false);
	}
	
	public static LocalComputeJob addTask(LocalComputeJob t, boolean forceMem) {
		if (t == null)
			return null;
		
		ThreadManager.getInstance().memTask(t, forceMem);
		
		return t;
	}
	
	/**
	 * Get the number of running+scheduled tasks.
	 * 
	 * @return Number of scheduled+number of running tasks;
	 */
	public static int getWorkLoad() {
		return ThreadManager.getInstance().getNumberOfEnquedOrRunningTasks();
	}
	
	public BackgroundThreadDispatcher() {
		if (myInstance != null)
			return;
		
		final Timer t = new Timer(500, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String msg;
				String indicatorStr = "";
				if (indicator == 0)
					indicatorStr = "///";
				if (indicator == 1)
					indicatorStr = "---";
				if (indicator == 2)
					indicatorStr = "\\\\\\";
				if (indicator == 3)
					indicatorStr = "|||";
				
				indicator++;
				
				if (indicator >= 4 || indicator < 0)
					indicator = 0;
				
				int exec = -1;
				int wl = -1;
				if (exec > 0 || wl > 0) {
					msg = " " + indicatorStr + " perform operations (" + (wl + exec) + " remaining) " + indicatorStr;
				} else
					msg = "";
				if (frame != null && frame.getTitle().indexOf(projectLoading) == -1)
					frame.setTitle(msg.trim());
				// else
				// if (frame == null && indicator % 2 == 0 && msg.trim().length() > 0)
				// System.out.println(msg.trim());
			}
		});
		if (!SystemAnalysis.isHeadless())
			t.start();
	}
	
	public static void waitFor(LocalComputeJob[] threads) throws InterruptedException {
		HashSet<LocalComputeJob> t = new HashSet<LocalComputeJob>();
		for (LocalComputeJob m : threads)
			if (m != null)
				t.add(m);
		
		if (t.size() > 0)
			waitFor(t);
	}
	
	public static void waitFor(Collection<LocalComputeJob> threads) throws InterruptedException {
		threads = new ArrayList<LocalComputeJob>(threads);
		for (LocalComputeJob m : threads) {
			m.getResult();
		}
	}
	
	public static void waitButDontRun(ArrayList<LocalComputeJob> threads) throws InterruptedException {
		threads = new ArrayList<LocalComputeJob>(threads);
		do {
			LocalComputeJob m = threads.get(0);
			if (m.isFinished())
				threads.remove(0);
			Thread.sleep(100);
		} while (threads.size() > 0);
	}
	
	private static void updateTaskStatistics() {
		Calendar calendar = new GregorianCalendar();
		int minute = calendar.get(Calendar.MINUTE);
		synchronized (BlockPipeline.class) {
			taskExecutionsWithinCurrentMinute++;
			if (currentMinute != minute) {
				taskExecutionsWithinLastMinute = taskExecutionsWithinCurrentMinute;
				taskExecutionsWithinCurrentMinute = 0;
				currentMinute = minute;
			}
		}
	}
	
	public static int getTaskExecutionsWithinLastMinute() {
		return taskExecutionsWithinLastMinute;
	}
	
	private static int taskExecutionsWithinLastMinute = 0;
	private static int taskExecutionsWithinCurrentMinute = 0;
	private static int currentMinute = -1;
	
	public static void waitSec(int secondsDelay) {
		try {
			Thread.sleep(1000 * secondsDelay);
		} catch (InterruptedException e) {
			// empty
			e.printStackTrace();
		}
	}
	
	public static void runWithTimeout(long timeout, Runnable runnable) {
		runWithTimeout(timeout, runnable, "Background Task");
	}
	
	public static void runWithTimeout(long timeout, Runnable runnable, String desc) {
		Thread t = new Thread(runnable);
		t.setName(desc + " // Timeout=" + timeout);
		t.start();
		long start = System.currentTimeMillis();
		while (t.isAlive()) {
			if (System.currentTimeMillis() - start > timeout) {
				t.interrupt();
				return;
			} else {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					// empty
				}
			}
		}
	}
	
	public static void waitFor(ArrayList<LocalComputeJob> wait, Runnable runnable) throws InterruptedException {
		Thread t = new Thread(runnable);
		t.setName("Waiting for threads, idle task");
		t.start();
		waitFor(wait);
		t.interrupt();
	}
	
	public static int getBackgroundThreadCount() {
		return ThreadManager.getInstance().getNumberOfRunningBackgroundTasks();
	}
}
