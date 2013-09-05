/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.services.task;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Semaphore;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.BackgroundTaskStatusProvider;
import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.HelperClass;
import org.ObjectRef;
import org.StringManipulationTools;
import org.SystemAnalysis;
import org.graffiti.editor.GravistoService;
import org.graffiti.editor.MainFrame;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;

/**
 * Used to easily let a long running task running in background,
 * with the possibility to provide a feedback of the progress,
 * 
 * @author klukas
 */
public class BackgroundTaskHelper implements HelperClass {
	private final Runnable workTask;
	private final BackgroundTaskStatusProvider statusProvider;
	private final String title, taskName;
	private final boolean autoClose;
	private final boolean showDialog;
	private Thread runThread;
	private boolean modal = false;
	
	private int initalShowDelay = 200;
	
	private static ArrayList<Object> runningTasksRefrenceObjects = new ArrayList<Object>();
	
	/**
	 * Creates a new BackgroundTaskHelper object.
	 * 
	 * @param workTask
	 *           The task to be executed.
	 * @param statusProvider
	 *           The statusProvider, probably linked to or returned
	 *           by the workTask object.
	 * @param title
	 *           The Title of the Dialog / Pane
	 * @param taskName
	 *           The Task Name
	 * @param autoClose
	 *           If set to true, the pane or dialog will automatically close
	 *           after completion of the job (with a timeout value)
	 * @param showDialogIsTrue_ShowPanelIsFalse
	 *           If set to true, a Dialog window will
	 *           be shown, if set to false, a pane will be shown in the GUI.
	 */
	public BackgroundTaskHelper(Runnable workTask,
			BackgroundTaskStatusProvider statusProvider,
			String title, String taskName, boolean autoClose,
			boolean showDialogIsTrue_ShowPanelIsFalse) {
		this.workTask = workTask;
		this.statusProvider = statusProvider;
		this.title = title;
		this.taskName = taskName;
		this.autoClose = autoClose;
		this.showDialog = showDialogIsTrue_ShowPanelIsFalse;
	}
	
	public BackgroundTaskHelper(Runnable workTask,
			BackgroundTaskStatusProvider statusProvider,
			String title, String taskName, boolean autoClose,
			boolean showDialogIsTrue_ShowPanelIsFalse, int intialShowDelayForStatusPanel) {
		this(workTask, statusProvider, title, taskName, autoClose, showDialogIsTrue_ShowPanelIsFalse);
		this.initalShowDelay = intialShowDelayForStatusPanel;
	}
	
	/**
	 * Opens a progress window and starts the <code>workTask</code> that was provided
	 * in the constructor.
	 * The <code>statusProvider</code> is used for retreiving the current status, which
	 * is then shown. The dialog contains a stop-button. The <code>statusProvider</code> should make sure that the <code>workTask</code> stops as soon as
	 * possible
	 * in case the <code>pleaseStop</code> method is called.
	 */
	public void startWork(final Object referenceObject) {
		BackgroundTaskGUIprovider taskWindow = null;
		if (!SystemAnalysis.isHeadless()) {
			if (showDialog)
				taskWindow = new BackgroundTaskWindow(modal);
			else
				taskWindow = new BackgroundTaskPanelEntry(false);
			taskWindow.setStatusProvider(statusProvider, title, taskName);
		}
		runThread = new Thread(workTask);
		final long currentTime = System.currentTimeMillis();
		if (taskName != null)
			runThread.setName(taskName);
		else
			runThread.setName("untitled background task");
		// runThread.setPriority(Thread.MIN_PRIORITY);
		synchronized (runningTasksRefrenceObjects) {
			runningTasksRefrenceObjects.add(referenceObject);
			runThread.start();
		}
		
		final BackgroundTaskGUIprovider taskWindowF = taskWindow;
		
		if (!SystemAnalysis.isHeadless())
			if (!showDialog) {
				// add panel to mainframe status area
				final MainFrame mf = GravistoService.getInstance().getMainFrame();
				if (mf != null) {
					if (title != null) {
						final ObjectRef tt = new ObjectRef();
						ActionListener al = new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								if (runThread.isAlive())
									mf.addStatusPanel((JPanel) taskWindowF);
								Timer t = (Timer) tt.getObject();
								if (t != null)
									t.stop();
							}
						};
						/**
						 * Show task panel entry with delay (no dialog for short tasks)
						 */
						if (autoClose) {
							Timer t = new Timer(initalShowDelay, al);
							tt.setObject(t);
							t.start();
						} else
							al.actionPerformed(null);
					}
				}
			}
		
		if (!SystemAnalysis.isHeadless()) {
			final ThreadSafeOptions tso = new ThreadSafeOptions();
			Timer checkStatus = new Timer(100, new ActionListener() {
				boolean finishedCalled = false;
				
				@Override
				public void actionPerformed(ActionEvent arg0) {
					if (!runThread.isAlive()) {
						if (!finishedCalled) {
							long endTime = System.currentTimeMillis();
							taskWindowF.setTaskFinished(autoClose, endTime - currentTime);
							finishedCalled = true;
						}
					}
					if (!taskWindowF.isProgressViewVisible()) {
						Timer checkStatusTimer = (Timer) tso.getParam(0, null);
						if (checkStatusTimer != null)
							checkStatusTimer.stop();
						synchronized (runningTasksRefrenceObjects) {
							runningTasksRefrenceObjects.remove(referenceObject);
						}
					}
				}
			});
			tso.setParam(0, checkStatus);
			checkStatus.start();
		} else {
			final ThreadSafeOptions tso = new ThreadSafeOptions();
			Timer checkStatus = new Timer(100, new ActionListener() {
				boolean finishedCalled = false;
				
				@Override
				public void actionPerformed(ActionEvent arg0) {
					if (!runThread.isAlive()) {
						if (!finishedCalled) {
							finishedCalled = true;
							synchronized (runningTasksRefrenceObjects) {
								runningTasksRefrenceObjects.remove(referenceObject);
							}
							Timer checkStatusTimer = (Timer) tso.getParam(0, null);
							if (checkStatusTimer != null)
								checkStatusTimer.stop();
						}
					}
				}
			});
			tso.setParam(0, checkStatus);
			checkStatus.start();
		}
	}
	
	public Thread getRunThread() {
		return runThread;
	}
	
	public static boolean isTaskWithGivenReferenceRunning(Object referenceObject) {
		boolean result;
		synchronized (runningTasksRefrenceObjects) {
			result = runningTasksRefrenceObjects.contains(referenceObject);
		}
		return result;
	}
	
	public static void issueSimpleTask(String taskName, String progressText, Runnable backgroundTask, Runnable finishSwingTask, BackgroundTaskStatusProvider sp) {
		SimpleBackgroundTask sbt = new SimpleBackgroundTask(progressText, "", backgroundTask, finishSwingTask);
		BackgroundTaskHelper bth = new BackgroundTaskHelper(sbt, sp, taskName, StringManipulationTools.removeHTMLtags(taskName), true, false);
		bth.startWork(taskName);
	}
	
	public static void issueSimpleTask(String taskName, String progressText, Runnable backgroundTask, Runnable finishSwingTask, BackgroundTaskStatusProvider sp,
			int delayForPanel) {
		SimpleBackgroundTask sbt = new SimpleBackgroundTask(progressText, "", backgroundTask, finishSwingTask);
		BackgroundTaskHelper bth = new BackgroundTaskHelper(sbt, sp, taskName, StringManipulationTools.removeHTMLtags(taskName), true, false, delayForPanel);
		bth.startWork(taskName);
	}
	
	public static void issueSimpleTaskInWindow(String taskName, String progressText, Runnable backgroundTask, Runnable finishSwingTask,
			BackgroundTaskStatusProvider sp) {
		SimpleBackgroundTask sbt = new SimpleBackgroundTask(progressText, "", backgroundTask, finishSwingTask);
		BackgroundTaskHelper bth = new BackgroundTaskHelper(sbt, sp, taskName, StringManipulationTools.removeHTMLtags(taskName), true, true);
		bth.startWork(taskName);
	}
	
	public static void issueSimpleTaskInWindow(String taskName, String progressText, Runnable backgroundTask, Runnable finishSwingTask,
			BackgroundTaskStatusProvider sp, boolean modal, boolean autoclose) {
		SimpleBackgroundTask sbt = new SimpleBackgroundTask(progressText, "", backgroundTask, finishSwingTask);
		BackgroundTaskHelper bth = new BackgroundTaskHelper(sbt, sp, taskName, StringManipulationTools.removeHTMLtags(taskName), autoclose, true);
		bth.setModalWindow(modal);
		bth.startWork(taskName);
	}
	
	public static void issueSimpleTask(String taskName, String progressText, Runnable backgroundTask1, Runnable finishSwingTask) {
		SimpleBackgroundTask sbt = new SimpleBackgroundTask(progressText, "", backgroundTask1, finishSwingTask);
		BackgroundTaskHelper bth = new BackgroundTaskHelper(sbt, sbt, taskName, StringManipulationTools.removeHTMLtags(taskName), true, false);
		bth.startWork(taskName);
	}
	
	public static void issueSimpleTask(String taskName, String progressText1, String progressText2, Runnable backgroundTask1, Runnable finishSwingTask) {
		SimpleBackgroundTask sbt = new SimpleBackgroundTask(progressText1, progressText2, backgroundTask1, finishSwingTask);
		BackgroundTaskHelper bth = new BackgroundTaskHelper(sbt, sbt, taskName, StringManipulationTools.removeHTMLtags(taskName), true, false);
		bth.startWork(taskName);
	}
	
	public static void issueSimpleTask(String taskName, String progressText1, String progressText2, Runnable backgroundTask1, Runnable finishSwingTask,
			boolean autoclose) {
		SimpleBackgroundTask sbt = new SimpleBackgroundTask(progressText1, progressText2, backgroundTask1, finishSwingTask);
		BackgroundTaskHelper bth = new BackgroundTaskHelper(sbt, sbt, taskName, StringManipulationTools.removeHTMLtags(taskName), autoclose, false);
		bth.startWork(taskName);
	}
	
	private void setModalWindow(boolean modal) {
		this.modal = modal;
	}
	
	public static void showMessage(String message1, String message2) {
		issueSimpleTask("Information Message", message1, message2, null, null);
	}
	
	public static void executeLaterOnSwingTask(int delay, final Runnable runnable) {
		final Timer t = new Timer(delay, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				runnable.run();
				((Timer) arg0.getSource()).stop();
			}
		});
		t.start();
	}
	
	private static HashMap<Object, Semaphore> device2sema = new HashMap<Object, Semaphore>();
	
	/**
	 * @param device
	 *           If specified, the Semaphore is saved in a hashmap and may be release
	 *           using the method lockRelease. If set to null, always a new semaphore
	 *           is returned.
	 * @param maxLoad
	 *           Max semaphore load.
	 * @return The desired semaphore.
	 */
	public static synchronized Semaphore lockGetSemaphore(Object device, int maxLoad) {
		boolean fair = true;
		if (device == null)
			return new Semaphore(maxLoad, fair);
		if (maxLoad > 0) {
			if (!device2sema.containsKey(device)) {
				device2sema.put(device, new Semaphore(maxLoad, fair));
			}
		}
		return device2sema.get(device);
	}
	
	public static void lockAquire(Object device, int maxLoad) {
		if (device == null)
			throw new UnsupportedOperationException("When using this method, a device needs to be specified!");
		Semaphore s = lockGetSemaphore(device, maxLoad);
		try {
			s.acquire();
		} catch (InterruptedException e) {
			// ErrorMsg.addErrorMessage(e);
			System.out.println("Information: Semaphore " + device + " InterruptedException (take)");
		}
	}
	
	public static void lockRelease(Object device) {
		if (device == null)
			throw new UnsupportedOperationException("When using this method, a device needs to be specified!");
		Semaphore s = lockGetSemaphore(device, -1);
		s.release();
	}
	
	public static void layoutLater(final JComponent jc) {
		executeLaterOnSwingTask(0, new Runnable() {
			@Override
			public void run() {
				jc.validate();
			}
		});
	}
	
	public static BackgroundTaskStatusProviderSupportingExternalCall getStatusHelperFor(final JButton saveB) {
		BackgroundTaskStatusProviderSupportingExternalCall res =
				new BackgroundTaskStatusProviderSupportingExternalCallImpl(saveB.getText(), "") {
					@Override
					public void setCurrentStatusText1(final String status) {
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								saveB.setText(status);
							}
						});
					}
					
				};
		return res;
	}
}

class SimpleBackgroundTask implements Runnable, BackgroundTaskStatusProvider {
	boolean finished = false;
	boolean executingSwingTask = false;
	String progressText1, progressText2;
	private final Runnable runTask1;
	private final Runnable runTask2swing;
	private boolean pleaseStop;
	
	public SimpleBackgroundTask(String progress1, String progress2, Runnable task1, Runnable task2swing) {
		this.finished = false;
		this.progressText1 = progress1;
		this.progressText2 = progress2;
		this.runTask1 = task1;
		this.runTask2swing = task2swing;
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProvider#getCurrentStatusValue()
	 */
	@Override
	public int getCurrentStatusValue() {
		if (finished || executingSwingTask)
			return 100;
		else
			return -1;
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProvider#getCurrentStatusValueFine()
	 */
	@Override
	public double getCurrentStatusValueFine() {
		return getCurrentStatusValue();
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProvider#getCurrentStatusMessage1()
	 */
	@Override
	public String getCurrentStatusMessage1() {
		return progressText1;
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProvider#getCurrentStatusMessage2()
	 */
	@Override
	public String getCurrentStatusMessage2() {
		return progressText2;
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProvider#pleaseStop()
	 */
	@Override
	public void pleaseStop() {
		pleaseStop = true;
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProvider#pluginWaitsForUser()
	 */
	@Override
	public boolean pluginWaitsForUser() {
		return false;
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProvider#pleaseContinueRun()
	 */
	@Override
	public void pleaseContinueRun() {
		// empty
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		finished = false;
		if (runTask1 != null)
			runTask1.run();
		else {
			setCurrentStatusValue(0);
			while (!pleaseStop) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					pleaseStop();
				}
			}
			setCurrentStatusValue(100);
		}
		if (runTask2swing != null) {
			executingSwingTask = true;
			SwingUtilities.invokeLater(runTask2swing);
			executingSwingTask = false;
		}
		finished = true;
	}
	
	@Override
	public void setCurrentStatusValue(int value) {
		// empty
	}
	
	@Override
	public String getCurrentStatusMessage3() {
		return null;
	}
}