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
		return -1;
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
	
	// private static final ThreadSafeOptions tso = new ThreadSafeOptions();
	//
	// private static ExecutorService execSvc = Executors.newFixedThreadPool(SystemAnalysis.getNumberOfCPUs(), new ThreadFactory() {
	// @Override
	// public Thread newThread(Runnable arg0) {
	// Thread t = new Thread(arg0);
	// t.setName("BackgroundThread " + tso.getNextLong());
	// return t;
	// }
	// });
	//
	// public static ExecutorService getExecutorService() {
	// return execSvc;
	// }
	//
	// @SuppressWarnings("unchecked")
	// public static List<Future<FlexibleImage>> invokeAll(Collection jobs) throws InterruptedException {
	// return invokeAll(execSvc, jobs);
	// }
	//
	// private static <T> List<Future<T>> invokeAll(
	// ExecutorService threadPool, Collection<Callable<T>> tasks)
	// throws InterruptedException {
	// if (tasks == null)
	// throw new NullPointerException();
	// List<Future<T>> futures = new ArrayList<Future<T>>(tasks.size());
	// boolean done = false;
	// try {
	// for (Callable<T> t : tasks) {
	// FutureTask<T> f = new FutureTask<T>(t);
	// futures.add(f);
	// threadPool.execute(f);
	// }
	// // force unstarted futures to execute using the current thread
	// for (Future<T> f : futures)
	// ((FutureTask) f).run();
	// for (Future<T> f : futures) {
	// if (!f.isDone()) {
	// try {
	// f.get();
	// } catch (CancellationException ignore) {
	// } catch (ExecutionException ignore) {
	// }
	// }
	// }
	// done = true;
	// return futures;
	// } finally {
	// if (!done)
	// for (Future<T> f : futures)
	// f.cancel(true);
	// }
	// }
	//
}
