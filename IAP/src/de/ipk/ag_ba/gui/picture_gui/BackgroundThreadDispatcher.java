package de.ipk.ag_ba.gui.picture_gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadFactory;

import javax.swing.Timer;

import org.graffiti.plugin.algorithm.ThreadSafeOptions;

import de.ipk.ag_ba.image_utils.FlexibleImage;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.threading.SystemAnalysis;

/**
 * @author klukas
 */
public class BackgroundThreadDispatcher {
	Stack<Thread> todo = new Stack<Thread>();
	Stack<Integer> todoPriorities = new Stack<Integer>();
	LinkedList<Thread> runningTasks = new LinkedList<Thread>();
	int maxTask = SystemAnalysis.getNumberOfCPUs();

	int indicator = 0;

	public static String projectLoading = " PLEASE WAIT - Loading Experimental Data";

	private static BackgroundThreadDispatcher myInstance = new BackgroundThreadDispatcher();

	private Thread sheduler = null;
	private StatusDisplay frame;
	private static ArrayList<Thread> waitThreads = new ArrayList<Thread>();

	public static void setFrameInstance(StatusDisplay mainFrame) {
		synchronized (myInstance) {
			if (myInstance == null)
				myInstance = new BackgroundThreadDispatcher();
			myInstance.frame = mainFrame;
		}
	}

	public static Thread addTask(Runnable r, String name, int userPriority) {
		return addTask(new Thread(r, name), userPriority);
	}

	public static Thread addTask(Thread t, int userPriority) {
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
			ArrayList<Thread> toBeRemoved = new ArrayList<Thread>();
			for (Iterator<Thread> it = runningTasks.iterator(); it.hasNext();) {
				Thread t = it.next();
				if (!t.isAlive())
					toBeRemoved.add(t);
			}
			for (Iterator<Thread> it = toBeRemoved.iterator(); it.hasNext();)
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
				else
					if (frame == null && indicator % 2 == 0 && msg.trim().length() > 0)
						System.out.println(msg.trim());
			}
		});
		t.start();

		sheduler = new Thread(new Runnable() {
			public void run() {
				while (true) {
					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						// PictureGUI.showError("Background thread dispatcher was interrupted.",
						// e);
						// kein Fehler! normal!
					}
					while (!todo.empty()) {
						Thread t = null;
						synchronized (todo) {
							int maxPrio = Integer.MIN_VALUE;
							// search maximum priority
							for (int i = 0; i < todo.size(); i++) {
								int curPrio = (todoPriorities.get(i)).intValue();
								if (curPrio > maxPrio)
									maxPrio = curPrio;
							}
							// search first thread with maximum priority
							for (int i = 0; i < todo.size(); i++) {
								int curPrio = (todoPriorities.get(i)).intValue();
								if (curPrio == maxPrio) {
									// use that thread and run it
									t = todo.get(i);
									todo.remove(i);
									Integer prio = todoPriorities.get(i);
									todoPriorities.remove(i);
									t.setName(prio.toString());
									break;
								}
							}
						}
						int blocked = 0;
						if (t != null) {
							t.setPriority(Thread.MIN_PRIORITY);
							t.start();
							synchronized (runningTasks) {
								runningTasks.add(t);
								if (!(t.getState()==Thread.State.RUNNABLE))
									blocked++;
							}
						}
						System.out.println("Blocked: "+blocked);
						// wait until the number of running tasks gets below the
						// maximum
						// then a new one can be started above
						// in case there is a higher priority task waiting
						// (higher than all running tasks) then the loop is
						// stopped, it can run, too
						while (runningTasks.size() - blocked >= maxTask) {
							int highestRunningPrio = Integer.MIN_VALUE;
							try {
								Thread.sleep(5);
							} catch (InterruptedException e) {
								// PictureGUI.showError("Background thread dispatcher was interrupted (2).",
								// e);
								// kein Fehler, normal!
							}
							synchronized (runningTasks) {
								for (int i = 0; i < runningTasks.size(); i++) {
									Thread rt = runningTasks.get(i);
									if (!rt.isAlive()) {
										myRemove(rt);
										i--;
									} else {
										int thisPrio = Integer.parseInt(rt.getName());
										if (thisPrio > highestRunningPrio)
											highestRunningPrio = thisPrio;
									}
								}
							}
							int highestNOTrunningPrio = Integer.MIN_VALUE;
							synchronized (todo) {
								for (int i = 0; i < todo.size(); i++) {
									int curPrio = (todoPriorities.get(i)).intValue();
									if (curPrio > highestNOTrunningPrio)
										highestNOTrunningPrio = curPrio;
								}
							}
							if (highestNOTrunningPrio > highestRunningPrio)
								break;
						}
					}
				}
			}
		});
		sheduler.start();
	}

	protected void myRemove(Thread rt) {
		synchronized (runningTasks) {
			runningTasks.remove(rt);
		}
		synchronized (waitThreads) {
			for (Thread t : waitThreads)
				t.interrupt();
		}
	}

	public static void waitFor(Thread[] threads) {
		try {
			synchronized (waitThreads) {
				waitThreads.add(Thread.currentThread());
			}
			boolean oneRunning = false;
			do {
				for (Thread t : threads) {
					if (t.getState() != Thread.State.TERMINATED) {
						oneRunning = true;
						break;
					}
				}
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// empty
				}
			} while (oneRunning);
		} finally {
			synchronized (waitThreads) {
				waitThreads.remove(Thread.currentThread());
			}
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
