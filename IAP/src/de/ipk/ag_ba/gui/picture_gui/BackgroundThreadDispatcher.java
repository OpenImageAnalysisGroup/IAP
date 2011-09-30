package de.ipk.ag_ba.gui.picture_gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.swing.Timer;

import org.SystemAnalysis;

import de.ipk.ag_ba.image.operations.blocks.BlockPipeline;
import de.ipk.ag_ba.server.task_management.SystemAnalysisExt;

/**
 * @author klukas
 */
public class BackgroundThreadDispatcher {
	int indicator = 0;
	
	public static String projectLoading = " PLEASE WAIT - Loading Experimental Data";
	
	private static BackgroundThreadDispatcher myInstance = new BackgroundThreadDispatcher();
	
	private final Thread sheduler = null;
	private StatusDisplay frame;
	
	public static void setFrameInstance(StatusDisplay mainFrame) {
		synchronized (myInstance) {
			if (myInstance == null)
				myInstance = new BackgroundThreadDispatcher();
			myInstance.frame = mainFrame;
		}
	}
	
	public static MyThread addTask(Runnable r, String name, int userPriority, int parentPriority) throws InterruptedException {
		return addTask(new MyThread(r, name), userPriority, parentPriority);
	}
	
	public static MyThread addTask(MyThread t, int userPriority, int parentPriority) {
		if (t == null)
			return null;
		
		t.startNG(myInstance.es);
		
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
	
	private boolean highMemoryLoad(LinkedList<MyThread> runningTasks) {
		if (runningTasks.size() < 1)
			return false;
		Runtime r = Runtime.getRuntime();
		long used = r.totalMemory() - r.freeMemory();
		long max = r.maxMemory();
		long free = max - used;
		if (free < max * 0.1d) {
			if (System.currentTimeMillis() - lastPrint > 1000) {
				lastPrint = System.currentTimeMillis();
				System.out.println(SystemAnalysisExt.getCurrentTime() + ">high memory load: " + (used / 1024 / 1024) + " MB used, max: " + (max / 1024 / 1024)
						+ " MB");
			}
			if (System.currentTimeMillis() - lastGC > 1000 * 30) {
				lastGC = System.currentTimeMillis();
				System.out.println(SystemAnalysisExt.getCurrentTime() + ">high memory load: " + (used / 1024 / 1024) + " MB used, max: " + (max / 1024 / 1024)
						+ " MB --- GARBAGE COLLECTION");
				System.gc();
				used = r.totalMemory() - r.freeMemory();
				max = r.maxMemory();
				free = max - used;
				System.out.println(SystemAnalysisExt.getCurrentTime() + ">new memory load: " + (used / 1024 / 1024) + " MB used, max: " + (max / 1024 / 1024)
						+ " MB");
			}
			return true;
		} else
			return false;
	}
	
	private static long lastPrint = 0;
	private static long lastGC = 0;
	
	ExecutorService es = new ThreadPoolExecutor(
			SystemAnalysis.getNumberOfCPUs(),
			SystemAnalysis.getNumberOfCPUs(),
			10, TimeUnit.SECONDS,
			new ArrayBlockingQueue<Runnable>(SystemAnalysis.getNumberOfCPUs(), true),
			new ThreadFactory() {
				int idx = 1;
				
				@Override
				public Thread newThread(Runnable r) {
					Thread res = new Thread(r, "Background Pool Thread " + idx);
					idx++;
					return res;
				}
			});
	
	public static void waitFor(MyThread[] threads) throws InterruptedException {
		HashSet<MyThread> t = new HashSet<MyThread>();
		for (MyThread m : threads)
			if (m != null)
				t.add(m);
		
		if (t.size() > 0)
			waitFor(t);
	}
	
	public static void waitFor(Collection<MyThread> threads) throws InterruptedException {
		for (MyThread m : threads) {
			m.getResult();
		}
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
