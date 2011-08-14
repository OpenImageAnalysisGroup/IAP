package de.ipk.ag_ba.gui.picture_gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Stack;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.Timer;

import org.SystemAnalysis;

import de.ipk.ag_ba.image.operations.blocks.BlockPipeline;

/**
 * @author klukas
 */
public class BackgroundThreadDispatcher {
	Stack<MyThread> todo = new Stack<MyThread>();
	Stack<Integer> todoPriorities = new Stack<Integer>();
	LinkedList<MyThread> runningTasks = new LinkedList<MyThread>();
	
	int indicator = 0;
	
	public static String projectLoading = " PLEASE WAIT - Loading Experimental Data";
	
	private static BackgroundThreadDispatcher myInstance = new BackgroundThreadDispatcher();
	
	private Thread sheduler = null;
	private StatusDisplay frame;
	private static HashSet<Thread> waitThreads = new HashSet<Thread>();
	
	public static void setFrameInstance(StatusDisplay mainFrame) {
		synchronized (myInstance) {
			if (myInstance == null)
				myInstance = new BackgroundThreadDispatcher();
			myInstance.frame = mainFrame;
		}
	}
	
	public static MyThread addTask(Runnable r, String name, int userPriority) {
		return addTask(new MyThread(r, name), userPriority);
	}
	
	public static MyThread addTask(MyThread t, int userPriority) {
		// System.out.println("Add task " + t.getName() + ", Priority: " + userPriority);
		synchronized (myInstance) {
			if (myInstance == null)
				myInstance = new BackgroundThreadDispatcher();
			synchronized (myInstance.todo) {
				myInstance.todo.push(t);
				myInstance.todoPriorities.push(new Integer(userPriority));
				myInstance.sheduler.interrupt();
			}
		}
		return t;
	}
	
	/**
	 * Get the number of running+scheduled tasks.
	 * 
	 * @return Number of scheduled+number of running tasks;
	 */
	public static int getWorkLoad() {
		return myInstance.getCountSheduledTasks() + myInstance.getCurrentRunningTasks();
	}
	
	int getCountSheduledTasks() {
		int load;
		synchronized (todo) {
			load = todo.size();
		}
		return load;
	}
	
	int getCurrentRunningTasks() {
		int load;
		synchronized (runningTasks) {
			ArrayList<MyThread> toBeRemoved = new ArrayList<MyThread>();
			for (Iterator<MyThread> it = runningTasks.iterator(); it.hasNext();) {
				MyThread t = it.next();
				if (!t.isAlive())
					toBeRemoved.add(t);
			}
			for (Iterator<MyThread> it = toBeRemoved.iterator(); it.hasNext();)
				myRemove(it.next());
			load = runningTasks.size();
		}
		return load;
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
				
				int exec = getCurrentRunningTasks();
				int wl = getCountSheduledTasks();
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
		t.start();
		
		sheduler = new Thread(new Runnable() {
			public void run() {
				schedulerCode();
			}
		});
		sheduler.start();
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
				System.out.println("high memory load: " + (used / 1024 / 1024) + " MB used, max: " + (max / 1024 / 1024) + " MB");
			}
			if (System.currentTimeMillis() - lastGC > 1000 * 30) {
				lastGC = System.currentTimeMillis();
				System.out.println("high memory load: " + (used / 1024 / 1024) + " MB used, max: " + (max / 1024 / 1024) + " MB --- GARBAGE COLLECTION");
				System.gc();
				used = r.totalMemory() - r.freeMemory();
				max = r.maxMemory();
				free = max - used;
				System.out.println("new memory load: " + (used / 1024 / 1024) + " MB used, max: " + (max / 1024 / 1024) + " MB");
			}
			return true;
		} else
			return false;
	}
	
	private static long lastPrint = 0;
	private static long lastGC = 0;
	
	protected void myRemove(MyThread rt) {
		synchronized (runningTasks) {
			runningTasks.remove(rt);
		}
		// synchronized (waitThreads) {
		// for (Thread t : waitThreads)
		// t.interrupt();
		// }
	}
	
	ExecutorService es = Executors.newCachedThreadPool();
	
	private void schedulerCode() {
		int moreLoad = 0;
		while (true) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				// PictureGUI.showError("Background thread dispatcher was interrupted.",
				// e);
				// kein Fehler! normal!
			}
			while (!todo.empty()) {
				MyThread t = null;
				synchronized (todo) {
					int maxPrio = Integer.MIN_VALUE;
					// search maximum priority
					for (int i = 0; i < todo.size(); i++) {
						int curPrio = (todoPriorities.get(i)).intValue();
						if (curPrio > maxPrio)
							maxPrio = curPrio;
					}
					// System.out.println(">MAX PRIO: " + maxPrio);
					// search oldest thread with maximum priority
					for (int i = 0; i < todo.size(); i++) {
						int curPrio = (todoPriorities.get(i)).intValue();
						if (curPrio == maxPrio) {
							// use that thread and run it
							t = todo.get(i);
							// System.out.println("Start thread " + t.getName() + ". blocked: " + waitThreads.size() + ", max run:" + maxTask + ", running: "
							// + runningTasks.size() + ", todo:" + todo.size());
							todo.remove(i);
							Integer prio = todoPriorities.get(i);
							todoPriorities.remove(i);
							t.setName(t.getNameNG() + ", priority:" + prio.toString());
							// System.out.println(">ABOUT TO START " + t.getNameNG() + " // current scheduler status priority: " + curPrio);
							break;
						}
					}
				}
				if (t != null) {
					t.setPriorityNG(Thread.MIN_PRIORITY);
					t.startNG(es);
					synchronized (runningTasks) {
						runningTasks.add(t);
					}
				}
				// System.out.println("Running tasks: " + runningTasks.size() + "/" + maxTask + " max");
				// wait until the number of running tasks gets below the
				// maximum
				// then a new one can be started above
				// in case there is a higher priority task waiting
				// (higher than all running tasks) then the loop is
				// stopped, it can run, too
				int maxTask = SystemAnalysis.getNumberOfCPUs();
				double load = 1;// AttributeHelper.windowsRunning() ? 1 : SystemAnalysisExt.getRealSystemCpuLoad();
				if (maxTask > 1 && load > 0) {
					if (load / maxTask < 1) {
						if (moreLoad < maxTask * 2) {
							// System.out.print(maxTask + " --> ");
							moreLoad += 0;// 1;
							maxTask = maxTask + moreLoad;
							// System.out.println(maxTask + " (Load too low: " + load + ")");
						}
					} else
						moreLoad -= 1;
				}
				while (runningTasks.size() - waitThreads.size() + 1 >= maxTask || highMemoryLoad(runningTasks)) {
					int highestRunningPrio = Integer.MIN_VALUE;
					try {
						Thread.sleep(5);
					} catch (InterruptedException e) {
						// kein Fehler, normal!
					}
					synchronized (runningTasks) {
						for (int i = 0; i < runningTasks.size(); i++) {
							MyThread rt = runningTasks.get(i);
							if (!rt.isAlive()) {
								myRemove(rt);
								i--;
							} else {
								String name = rt.getName();
								try {
									int thisPrio = Integer.parseInt(name.substring(name.indexOf(":") + ":".length()));
									if (thisPrio > highestRunningPrio)
										highestRunningPrio = thisPrio;
								} catch (Exception nfe) {
									System.err.println("Invalid thread name (priority can't be parsed): " + name);
								}
							}
						}
					}
					// int highestNOTrunningPrio = Integer.MIN_VALUE;
					// synchronized (todo) {
					// for (int i = 0; i < todo.size(); i++) {
					// int curPrio = (todoPriorities.get(i)).intValue();
					// if (curPrio > highestNOTrunningPrio)
					// highestNOTrunningPrio = curPrio;
					// }
					// }
					// if (highestNOTrunningPrio > highestRunningPrio)
					// break;
				}
			}
		}
	}
	
	private static void waitFor(HashSet<MyThread> threads) throws InterruptedException {
		try {
			synchronized (waitThreads) {
				waitThreads.add(Thread.currentThread());
			}
			if (!Thread.currentThread().getName().contains("wait;"))
				Thread.currentThread().setName("wait;" + Thread.currentThread().getName());
			for (MyThread t : threads) {
				t.getResult();
				updateTaskStatistics();
			}
		} finally {
			if (Thread.currentThread().getName().contains("wait;"))
				Thread.currentThread().setName(Thread.currentThread().getName().substring("wait;".length()));
			synchronized (waitThreads) {
				waitThreads.remove(Thread.currentThread());
			}
		}
		
	}
	
	public static void waitFor(MyThread[] threads) throws InterruptedException {
		HashSet<MyThread> t = new HashSet<MyThread>();
		for (MyThread m : threads)
			if (m != null)
				t.add(m);
		waitFor(t);
	}
	
	public static void waitFor(Collection<MyThread> threads) throws InterruptedException {
		HashSet<MyThread> t = new HashSet<MyThread>();
		for (MyThread m : threads)
			t.add(m);
		threads.clear();
		waitFor(t);
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
