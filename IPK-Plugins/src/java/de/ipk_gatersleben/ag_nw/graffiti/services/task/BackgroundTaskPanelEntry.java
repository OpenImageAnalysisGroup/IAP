/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.services.task;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.BackgroundTaskStatusProvider;
import org.FolderPanel;
import org.ProgressStatusService;
import org.StringManipulationTools;
import org.SystemAnalysis;
import org.SystemInfo;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;
import org.graffiti.plugin.gui.ToolButton;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.helper_classes.MyGraphicsTools;
import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

public class BackgroundTaskPanelEntry extends JPanel implements BackgroundTaskGUIprovider {
	private static final long serialVersionUID = 1L;
	private static String stopText = "<html><small>Stop";
	private static String waitText = "<html><small>Wait";
	private static String closeText = "<html><small>OK";
	private static String autoCloseText = "<html><small>OK!";
	private static String proceedText = "<html><small>OK";
	
	private final JLabel taskStatusLabel;
	String taskMessage;
	private final JButton stopButton;
	private JButton continueButton;
	private final JProgressBar progressBar = new JProgressBar();
	private final JLabel status1 = new JLabel("<html>&nbsp;");
	private final JLabel status2 = new JLabel("<html>&nbsp;");
	private final JLabel timeRemainLabel = new JLabel("<html>&nbsp;");
	private final JPanel progressAndButton;
	protected int closeCountDown = 10; // 10*0.2sec = 2sec
	protected boolean continueButtonVisible = false;
	
	private BackgroundTaskStatusProvider statusProvider = null;
	
	private final HashSet<ActionListener> progressClosedActionListener = new HashSet<ActionListener>();
	private boolean taskFinished = false;
	
	private boolean inWindow = false;
	
	public BackgroundTaskPanelEntry(boolean inWindow) {
		
		this.inWindow = inWindow;
		
		timeRemainLabel.setToolTipText("Remaining time");
		
		// setPreferredSize(new Dimension(320, 180));
		// setLocationByPlatform(true);
		double border = 5;
		if (inWindow)
			border = 5;
		double[][] size = {
				{ border, TableLayoutConstants.FILL, border }, // Columns
				// XX {border, 20, 2, 20, 2, 20, 2, 20, border}
				{ border, TableLayoutConstants.PREFERRED, inWindow ? 0 : 2, TableLayoutConstants.PREFERRED, 2, TableLayoutConstants.PREFERRED, 2,
						TableLayoutConstants.PREFERRED, border }
				// fill
		}; // Rows
				
		// XX setMinimumSize(new Dimension(80, (int) Math.round(border+20+2+20+2+20+2+20+border)));
		// XX setMaximumSize(new Dimension(getMaximumSize().width, (int) Math.round(border+20+2+20+2+20+2+20+border)));
		
		/***********************************************/
		// border
		/**** taskStatusLabel ****/
		// 30 taskStatusLabel
		/***********************************************/
		// 5
		/**** status1 ****/
		// 30 status1
		/***********************************************/
		// 5
		/**** status2 ****/
		// 30 status2
		/***********************************************/
		// 5
		/**** progress | remainTime ** stopButton ****/
		// Progress, Stop
		/***********************************************/
		// border
		
		setLayout(new TableLayout(size));
		
		setDefaultBorder();
		
		taskStatusLabel = new JLabel("<html>&nbsp;");
		taskStatusLabel.setBackground(Color.LIGHT_GRAY);
		taskStatusLabel.setOpaque(true);
		taskStatusLabel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
		
		status1.setBorder(BorderFactory.createEmptyBorder(0, 2, 2, 0));
		status2.setBorder(BorderFactory.createEmptyBorder(0, 2, 2, 0));
		
		add(taskStatusLabel, "1,1");
		add(status1, "1,3");
		add(status2, "1,5");
		
		progressBar.setMinimum(0);
		progressBar.setMaximum(100);
		progressBar.setIndeterminate(true);
		
		stopButton = new JButton(stopText);
		
		stopButton.putClientProperty("JButton.buttonType", "textured");
		stopButton.putClientProperty("JComponent.sizeVariant", "small");
		int space = 0;
		if (stopButton.getPreferredSize().height > progressBar.getPreferredSize().height) {
			space = (stopButton.getPreferredSize().height - progressBar.getPreferredSize().height) / 2;
		}
		
		double border2 = 0;
		double[][] size2 = {
				{ border2, TableLayoutConstants.FILL, 5, 60, 5, 80, border2 }, // Columns
				// XX {border2, TableLayoutConstants.FILL, border2}
				{ border2, TableLayoutConstants.PREFERRED, border2 }
		}; // Rows
		
		progressAndButton = new JPanel();
		progressAndButton.setLayout(new TableLayout(size2));
		
		double border3 = 3;
		double[][] size3 = {
				{ 0, TableLayoutConstants.FILL, 0 }, // Columns
				// XX {border3, TableLayoutConstants.FILL, border3}
				{ border3, TableLayoutConstants.PREFERRED, border3 }
		}; // Rows
		JPanel jpForSmallerProgressBar = new JPanel();
		jpForSmallerProgressBar.setLayout(new TableLayout(size3));
		if (space == 0)
			jpForSmallerProgressBar.add(progressBar, "1,1");
		else
			jpForSmallerProgressBar.add(FolderPanel.getBorderedComponent(progressBar, space, 0, 0, 0), "1,1");
		jpForSmallerProgressBar.revalidate();
		
		progressAndButton.add(jpForSmallerProgressBar, "1,1");
		
		progressAndButton.add(timeRemainLabel, "3,1");
		progressAndButton.add(stopButton, "5,1");
		
		add(progressAndButton, "1,7");
		
		validate();
	}
	
	private void setDefaultBorder() {
		if (inWindow)
			return;
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					setDefaultBorder();
				}
			});
		} else
			setBorder(BorderFactory.createEtchedBorder(Color.LIGHT_GRAY, Color.WHITE));
	}
	
	private void setHighlightBorder() {
		if (inWindow)
			return;
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					setHighlightBorder();
				}
			});
		} else
			setBorder(BorderFactory.createLineBorder(Color.RED, 2));
	}
	
	private void blinkBorder() {
		if (inWindow)
			return;
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				setDefaultBorder();
				for (int i = 1; i <= 2; i++) {
					try {
						Thread.sleep(500);
					} catch (Exception e) {
					} ;
					setHighlightBorder();
					try {
						Thread.sleep(500);
					} catch (Exception e) {
					} ;
					setDefaultBorder();
				}
				try {
					Thread.sleep(500);
				} catch (Exception e) {
				} ;
				setHighlightBorder();
				try {
					Thread.sleep(5000);
				} catch (Exception e) {
				} ;
				setDefaultBorder();
			}
		});
		t.setName("Blink task panel entry border");
		t.start();
	}
	
	/**
	 * Call this in case the task is finished.
	 * 
	 * @param autoClose
	 */
	@Override
	public void setTaskFinished(boolean autoClose, long duration) {
		String title = (String) getClientProperty("title");
		if (taskStatusLabel.isVisible()) {
			if (title != null && title.toUpperCase().contains("ERROR"))
				taskStatusLabel.setText("<html>&nbsp;Could not correctly process requested operation!");
			else {
				taskStatusLabel.setText("<html>&nbsp;Task " + StringManipulationTools.removeHTMLtags(taskMessage) + " finished after " + (duration / 1000)
						+ " sec.");
			}
		}
		String t = (duration / 1000) + "";
		if (!t.equals("0"))
			System.out.println(SystemAnalysis.getCurrentTime() + ">INFO: Task " + StringManipulationTools.removeHTMLtags(taskMessage) + " finished after " + (duration / 1000) + " sec.");
		progressBar.setValue(100);
		taskFinished = true;
		if (autoClose || stopButton.getText().equals(closeText)) {
			stopButton.setText(autoCloseText);
		} else {
			if (!stopButton.getText().equals(autoCloseText))
				stopButton.setText(closeText);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.ipk_gatersleben.ag_nw.graffiti.BackgroundTaskGUIprovider#setStatusProvider(de.ipk_gatersleben.ag_nw.graffiti.BackgroundTaskStatusProvider,
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public void setStatusProvider(final BackgroundTaskStatusProvider statusProvider, String title, String taskMessage) {
		if (taskMessage == null)
			taskMessage = "" + System.currentTimeMillis();
		taskMessage = StringManipulationTools.stringReplace(taskMessage, "<br>", " ").trim();
		putClientProperty("title", taskMessage);
		this.taskMessage = taskMessage;
		this.statusProvider = statusProvider;
		if (taskStatusLabel.isVisible()) {
			if (taskMessage != null) {
				if (taskMessage.startsWith("<html>"))
					taskStatusLabel.setText(MyGraphicsTools.stringReplace(taskMessage, "<html>", "<html>&nbsp;"));
				else
					taskStatusLabel.setText(" " + taskMessage);
			}
		}
		final ProgressStatusService statusService = new ProgressStatusService();
		final ThreadSafeOptions tsoLinesS1 = new ThreadSafeOptions();
		final ThreadSafeOptions tsoLinesS2 = new ThreadSafeOptions();
		final Timer updateCheck = new Timer(100, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (SystemInfo.isMac()) {
					progressBar.putClientProperty("JProgressBar.style", "circular");
				}
				
				progressBar.setIndeterminate(statusProvider.getCurrentStatusValue() == -1);
				progressBar.setValue(statusProvider.getCurrentStatusValue());
				timeRemainLabel.setText("<html><small>" + statusService.getRemainTime(
						statusProvider.getCurrentStatusValue() == -1,
						statusProvider.getCurrentStatusValueFine()));
				String t1 = statusProvider.getCurrentStatusMessage1();
				if (t1 != null) {
					if (t1.indexOf("<") >= 0 && t1.indexOf(">") >= 0 && !t1.startsWith("<html>")) {
						t1 = StringManipulationTools.stringReplace(t1, "\n", "<br>");
						t1 = "<html><small>" + t1;
						if (t1.toUpperCase().indexOf("STACK:") > 0) {
							t1 = t1.substring(0, t1.toUpperCase().indexOf("STACK:"));
						}
					}
					String shortT1 = t1;
					if (t1.length() > 80 && t1.indexOf(" ", 70) >= 0) {
						shortT1 = t1.substring(0, t1.indexOf(" ", 70)) + "...";
					}
					if (t1.startsWith("<html>") && t1.length() > 80) {
						
					}
					if (shortT1 == null || shortT1.equals(""))
						shortT1 = "&nbsp;";
					status1.setText("<html>" + shortT1);
					status1.setToolTipText(t1);
				}
				String ss2 = statusProvider.getCurrentStatusMessage2();
				if (ss2 == null || ss2.equals(""))
					ss2 = "&nbsp;";
				status2.setText("<html>" + ss2);
				if (stopButton.getText().equals(autoCloseText) && closeCountDown-- <= 0) {
					ActionListener[] ll = stopButton.getActionListeners();
					for (int i = 0; i < ll.length; i++) {
						ll[i].actionPerformed(null);
					}
				}
				if (statusProvider.pluginWaitsForUser() && !continueButtonVisible) {
					continueButtonVisible = true;
					stopText = "Cancel";
					stopButton.setText(stopText);
					progressAndButton.remove(timeRemainLabel);
					continueButton.setEnabled(true);
					continueButton.setVisible(true);
					progressAndButton.add(continueButton, "3,1");
					progressAndButton.validate();
				} else
					if (!statusProvider.pluginWaitsForUser() && continueButtonVisible) {
						continueButtonVisible = false;
						continueButton.setEnabled(false);
						continueButton.setVisible(false);
						stopText = "Stop";
						stopButton.setText(stopText);
						progressAndButton.remove(continueButton);
						progressAndButton.add(timeRemainLabel, "3,1");
						progressAndButton.validate();
					}
				
				if (taskFinished && stopButton.getText().equals(waitText))
					stopButton.setText(autoCloseText);
				
				validate();
				
				if (inWindow) {
					if (tsoLinesS1.getInt() != StringManipulationTools.count(status1.getText(), "<br>")) {
						tsoLinesS1.setInt(StringManipulationTools.count(status1.getText(), "<br>"));
						FolderPanel.performDialogResize(status1);
					}
					if (tsoLinesS2.getInt() != StringManipulationTools.count(status2.getText(), "<br>")) {
						tsoLinesS2.setInt(StringManipulationTools.count(status2.getText(), "<br>"));
						FolderPanel.performDialogResize(status2);
					}
				}
			}
		});
		updateCheck.start();
		
		ActionListener[] ac = stopButton.getActionListeners();
		for (int i = 0; i < ac.length; i++) {
			stopButton.removeActionListener(ac[i]);
		}
		stopButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (stopButton.getText().equals(stopText)) {
					statusProvider.pleaseStop();
					stopButton.setText(waitText);
					if (continueButtonVisible)
						continueButton.doClick();
				}
				if (stopButton.getText().equals(waitText)) {
					stopButton.setText(waitText + "!");
					Timer t = new Timer(1000, new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent arg0) {
							stopButton.setText(waitText);
						}
					});
					t.setRepeats(false);
					t.start();
				}
				if (taskFinished || stopButton.getText().equals(closeText) ||
						stopButton.getText().equals(autoCloseText)) {
					updateCheck.stop();
					ToolButton.requestToolButtonFocus();
					setVisible(false);
					for (ActionListener al : progressClosedActionListener)
						al.actionPerformed(arg0);
				}
			}
		});
		
		continueButton = new JButton(proceedText);
		continueButton.putClientProperty("JButton.buttonType", "textured");
		continueButton.putClientProperty("JComponent.sizeVariant", "small");
		continueButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				continueButton.setEnabled(false);
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						stopText = "Stop";
						stopButton.setText(stopText);
						timeRemainLabel.setText("- -");
						progressAndButton.add(timeRemainLabel, "3,1");
						continueButton.setVisible(false);
						continueButtonVisible = false;
						progressAndButton.remove(continueButton);
						progressAndButton.invalidate();
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								statusProvider.pleaseContinueRun();
							}
						});
					}
				});
			}
		});
		if (title != null && title.toUpperCase().contains("ERROR"))
			blinkBorder();
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.ipk_gatersleben.ag_nw.graffiti.BackgroundTaskGUIprovider#isProgressViewVisible()
	 */
	@Override
	public boolean isProgressViewVisible() {
		return isVisible();
	}
	
	@Override
	public BackgroundTaskStatusProvider getStatusProvider() {
		return statusProvider;
	}
	
	public void disableTitleView() {
		taskStatusLabel.setVisible(false);
		taskStatusLabel.setText(null);
		validate();
	}
	
	public void addCloseActionListener(ActionListener al) {
		progressClosedActionListener.add(al);
	}
	
	@Override
	public String getTitle() {
		return taskStatusLabel.getText();
	}
	
	@Override
	public void setTitle(String text) {
		taskStatusLabel.setText(text);
	}
}