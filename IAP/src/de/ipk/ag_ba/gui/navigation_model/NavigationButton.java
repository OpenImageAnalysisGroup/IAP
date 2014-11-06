/*******************************************************************************
 * Copyright (c) 2003-2009 Plant Bioinformatics Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on May 5, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.gui.navigation_model;

import info.clearthought.layout.TableLayout;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.ErrorMsg;
import org.ObjectRef;
import org.ProgressStatusService;
import org.StringManipulationTools;
import org.SystemAnalysis;
import org.graffiti.editor.GravistoService;
import org.graffiti.editor.MainFrame;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;

import de.ipk.ag_ba.commands.bookmarks.BookmarkAction;
import de.ipk.ag_ba.commands.bookmarks.ImageProvider;
import de.ipk.ag_ba.gui.IAPfeature;
import de.ipk.ag_ba.gui.IAPnavigationPanel;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.PanelTarget;
import de.ipk.ag_ba.gui.calendar.MyCalendarIcon;
import de.ipk.ag_ba.gui.calendar.NavigationButtonCalendar2;
import de.ipk.ag_ba.gui.enums.ButtonDrawStyle;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.interfaces.StyleAware;
import de.ipk.ag_ba.gui.navigation_actions.ParameterOptions;
import de.ipk.ag_ba.gui.navigation_actions.SideGuiComponent;
import de.ipk.ag_ba.gui.picture_gui.BackgroundThreadDispatcher;
import de.ipk.ag_ba.gui.util.MyUtility;
import de.ipk.ag_ba.gui.webstart.IAPgui;
import de.ipk.ag_ba.gui.webstart.IAPmain;
import de.ipk.ag_ba.gui.webstart.OnlyVerticalScrollPanel;
import de.ipk.ag_ba.server.task_management.CloundManagerNavigationAction;
import de.ipk.ag_ba.server.task_management.RemoteCapableAnalysisAction;
import de.ipk_gatersleben.ag_nw.graffiti.MyInputHelper;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskWindow;

/**
 * @author klukas
 */
public class NavigationButton implements StyleAware {
	private String title;
	private String navigationImage, actionImage;
	private NavigationAction action;
	private String tooltipHint;
	private JComponent gui;
	private JComponent sideGui;
	private double sideGuiSpace;
	private double sideGuiWidth;
	
	private boolean processing;
	private long processingStart = 0;
	
	private ImageIcon icon;
	
	private GUIsetting guiSetting;
	
	protected Runnable optFinishAction = null;
	private Runnable execution;
	private boolean rightAligned;
	private ProgressStatusService statusServer;
	private String optStaticIconId;
	private String overrideTitle;
	private boolean iconUpdated;
	private Object validIconCheckObject;
	private boolean enabled = true;
	private ActionEvent lastEvent;
	
	public NavigationButton(String overrideTitle, NavigationAction navigationAction, GUIsetting guiSetting) {
		this(navigationAction, guiSetting);
		this.overrideTitle = overrideTitle;
		assert guiSetting != null;
	}
	
	public NavigationButton(NavigationAction navigationAction, GUIsetting guiSetting) {
		assert guiSetting != null;
		if (navigationAction != null) {
			boolean enableRemoteTaskExecution = IAPmain.isSettingEnabled(IAPfeature.REMOTE_EXECUTION);
			if (enableRemoteTaskExecution && navigationAction instanceof RemoteCapableAnalysisAction
					&& ((RemoteCapableAnalysisAction) navigationAction).remotingEnabledForThisAction()) {
				RemoteCapableAnalysisAction rca = (RemoteCapableAnalysisAction) navigationAction;
				CloundManagerNavigationAction ra = new CloundManagerNavigationAction(rca.getMongoDB(), false);
				navigationAction = new RemoteExecutionWrapperAction(navigationAction,
						new NavigationButton(ra, guiSetting));
				
			}
			navigationAction.setSource(navigationAction, guiSetting);
			this.setTitle(navigationAction.getDefaultTitle());
			this.navigationImage = null;
			this.actionImage = null;
			SideGuiComponent sgc = navigationAction.getButtonGuiAddition();
			if (sgc != null) {
				sgc.setButton(this);
				setSideGUI(sgc.getSideGui(), sgc.getSideGuiSpace(), sgc.getSideGuiWidth());
			}
		}
		// if (guiSetting == null)
		// System.out.println("ERROR: GUI-SETTING VARIABLE NOT ASSIGNED (INTERNAL ERROR)");
		this.guiSetting = guiSetting;
		this.action = navigationAction;
	}
	
	public NavigationButton(NavigationAction navigationAction, String title, String image, GUIsetting guiSetting) {
		this(navigationAction, guiSetting);
		this.setTitle(title);
		this.navigationImage = image;
		this.actionImage = image;
		assert guiSetting != null;
	}
	
	public NavigationButton(NavigationAction navigationAction, String title, String navigationImage, String actionImage,
			GUIsetting guiSetting) {
		this(navigationAction, guiSetting);
		this.setTitle(title);
		this.navigationImage = navigationImage;
		this.actionImage = actionImage;
		assert guiSetting != null;
	}
	
	public NavigationButton(JComponent gui, GUIsetting guiSetting) {
		this.gui = gui;
		this.guiSetting = guiSetting;
		assert guiSetting != null;
	}
	
	public NavigationButton(BookmarkAction ba, BufferedImage image, GUIsetting guiSetting, String optStaticIconId) {
		this(ba, guiSetting);
		this.optStaticIconId = optStaticIconId;
		this.icon = image != null ? new ImageIcon(image) : null;
		assert guiSetting != null;
	}
	
	public JComponent getGUI() {
		return gui;
	}
	
	public void setToolTipText(String hint) {
		this.tooltipHint = hint;
	}
	
	public String getNavigationImage() {
		if (navigationImage != null)
			return navigationImage;
		else
			if (action != null)
				return action.getDefaultNavigationImage();
			else
				return null;
	}
	
	public String getActionImage() {
		if (actionImage != null)
			return actionImage;
		else
			if (action != null)
				return action.getDefaultImage();
			else
				return null;
	}
	
	private String lastTitle = null;
	private boolean requestingTitle = false;
	private WeakReference<JButton> weakButtonReference;
	private long lastGetTitleTime = Long.MAX_VALUE;
	
	public String getTitle() {
		Runnable r = new Runnable() {
			
			@Override
			public void run() {
				try {
					long start = System.currentTimeMillis();
					NavigationButton.this.lastTitle = getTitle(false);
					lastGetTitleTime = System.currentTimeMillis() - start;
				} finally {
					synchronized (NavigationButton.this) {
						requestingTitle = false;
						final JButton jb = weakButtonReference != null ? weakButtonReference.get() : null;
						if (jb != null)
							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run() {
									jb.setText(NavigationButton.this.lastTitle);
									jb.revalidate();
									jb.repaint();
								}
							});
					}
				}
			}
		};
		Thread l = null;
		synchronized (NavigationButton.this) {
			if (!requestingTitle) {
				requestingTitle = true;
				l = new Thread(r, "update button text");
				if (lastGetTitleTime > 20)
					l.start();
				else
					l.run();
			}
		}
		if (l != null)
			BackgroundThreadDispatcher.waitForResultWithTimeout(l, 100);
		
		String res = lastTitle != null ? lastTitle : "<html><font color='gray'>...";
		return res != null ? res : res + "";
	}
	
	public String getTitle(boolean forceProgressText) {
		String adt = action != null ? action.getDefaultTitle() : null;
		if (action != null && adt != null && adt.length() > 0)
			title = adt;
		
		if (overrideTitle != null)
			title = overrideTitle;
		
		long compTime = System.currentTimeMillis() - processingStart;
		if (!forceProgressText && !(isProcessing() || requestsTitleUpdates()) || compTime < 1000) {
			if (title != null && title.contains("<br>") && !title.startsWith("<html>"))
				return "<html><center>" + title;
			return title;
		} else {
			// if (requestsTitleUpdates() && !isProcessing())
			// return title;
			String dots = "";
			String dots2 = "";
			int speed = 500;
			int ndots = (int) ((compTime % (speed * 2)) / speed);
			String cc = "*";
			String cc2 = "*";
			if (ndots == 0)
				cc = (char) 0x25AE + "";// "\\";
			if (ndots == 1)
				cc = (char) 0x25AF + "";// "|";
			if (ndots == 0)
				cc2 = (char) 0x25AF + "";// "\\";
			if (ndots == 1)
				cc2 = (char) 0x25AE + "";// "|";
			// if (ndots == 2)
			// cc = (char) 0x25C6 + "";// "/";
			// if (ndots == 3)
			// cc = (char) 0x25F3 + "";// "-";
			// if (ndots == 4)
			// cc = (char) 0x25CB + "";// "-";
			
			if (requestsTitleUpdates()) {
				cc = "";
				cc2 = "";
			}
			
			while (dots.length() < 1 && cc.length() > 0)
				dots += cc;
			while (dots2.length() < 1 && cc2.length() > 0)
				dots2 += cc2;
			
			// dots = "<font color='#585BA2'>" + dots + "</font>"; //
			// dots2 = "<font color='#585BA2'>" + dots2 + "</font>";
			
			String progress = "";
			String s = "";
			double dp = action.getStatusProvider().getCurrentStatusValueFine();
			if (dp > 0) {
				if (dp > 0)
					progress = "" + (int) dp + "%";
				while (progress.length() < 5)
					progress = "_" + progress;
				progress = "";
				
				s = "<br>";
				int len = 20;// (dots + " " + title + progress).length();
				s += // "<font border-color='black' color='#000000'>" + // 585BA2 // bgcolor='#CCCCCC'
				getProgress( //
						(char) (0x25A0) + "",// "#",
						(char) (0x25A1) + "",// "-",
						len, dp);
				// + "</font>"; // &nbsp;
			}
			String line2 = "";
			String sm1 = "", sm2 = "", sm3 = "";
			if (action.getStatusProvider() != null) {
				sm1 = action.getStatusProvider().getCurrentStatusMessage1();
				sm2 = action.getStatusProvider().getCurrentStatusMessage2();
				sm3 = action.getStatusProvider().getCurrentStatusMessage3();
				if (sm2 != null && sm2.startsWith("<br>"))
					sm2 = sm2.substring("<br>".length());
				if (sm3 != null && sm3.startsWith("<br>"))
					sm3 = sm3.substring("<br>".length());
			}
			if (sm1 != null && sm1.length() > 0)
				line2 = sm1;
			if (sm2 != null && sm2.length() > 0)
				line2 += (sm1 != null && sm1.length() > 0 ? "<br>" : "") + sm2;
			if (sm3 != null && sm3.length() > 0)
				line2 += (line2 != null && sm2 != null && line2.length() + sm2.length() > 0 ? "<br>" : "<br>") + sm3;
			
			if (statusServer != null) {
				String eta = statusServer.getRemainTime((int) dp == -1, dp);
				if (eta.length() > 0) {
					if (line2.length() > 0 && sm3 != null)
						line2 += sm3.length() == 0 ? ", " : "";
					if (sm3 == null || sm3.length() == 0)
						line2 += " ~ " + eta;
				}
			}
			line2 = "<br>" + line2;
			// if (line2.length() > 0)
			// line2 = "<br>&nbsp;" + line2 + "&nbsp;";
			//
			// line2 = StringManipulationTools.stringReplace(line2, "<br><hr>", "<hr>");
			// line2 = StringManipulationTools.stringReplace(line2, "<p><hr>", "<hr>");
			
			// System.out.println(line2);
			
			if (dp < -1.01) {
				return "<html><center>" + dots + " " + "[REMOVE FROM UPDATE] " + title + progress + " " + dots2 + "" + s + line2;
			} else
				return "<html><center>" + dots + " " + title + progress + " " + dots2 + "" + s + line2;
		}
	}
	
	private String getProgress(String c, String o, int len, double currentStatusValue) {
		StringBuilder s = new StringBuilder();
		while (s.length() < len) {
			boolean inProgress = s.length() * 100d / len <= currentStatusValue;
			if (inProgress)
				s.append(c);
			else
				s.append(o);
		}
		return s.toString();
	}
	
	public NavigationAction getAction() {
		return action;
	}
	
	public String getToolTip() {
		// if (tooltipHint != null)
		// return tooltipHint;
		// else
		String res = action != null ? action.getDefaultTooltip() : tooltipHint;
		if (res == null)
			res = tooltipHint;
		return res;
	}
	
	public void setProcessing(boolean b) {
		this.processing = b;
		this.processingStart = System.currentTimeMillis();
		statusServer = new ProgressStatusService();
	}
	
	public boolean isProcessing() {
		return processing;
	}
	
	public boolean requestsTitleUpdates() {
		return action != null && action.requestTitleUpdates();
	}
	
	public void setSideGUI(JComponent sideGui, double sideGuiSpace, double sideGuiWidth) {
		this.sideGui = sideGui;
		this.sideGuiSpace = sideGuiSpace;
		this.sideGuiWidth = sideGuiWidth;
	}
	
	public JComponent getSideGui() {
		return sideGui;
	}
	
	public double getSideGuiSpace() {
		return sideGuiSpace;
	}
	
	public double getSideGuiWidth() {
		return sideGuiWidth;
	}
	
	public void setIcon(ImageIcon i, String optStaticIconId) {
		if (i != null) {
			iconUpdated = i != icon;
			this.icon = i;
		}
		if (optStaticIconId != null)
			this.optStaticIconId = optStaticIconId;
	}
	
	public boolean isIconUpdated() {
		return iconUpdated;
	}
	
	public ImageIcon getIconInactive() {
		if (action != null) {
			if (action.getImageIconInactive() != null)
				return action.getImageIconInactive().getImageIcon();
		}
		return icon;
	}
	
	public ImageIcon getIconActive() {
		if (action != null) {
			if (action.getImageIconActive() != null)
				return action.getImageIconActive().getImageIcon();
		}
		return icon;
	}
	
	public void performAction() {
		if (execution != null)
			execution.run();
	}
	
	public Runnable getExecution() {
		return execution;
	}
	
	public void setExecution(Runnable execution) {
		this.execution = execution;
	}
	
	public void setRightAligned(boolean b) {
		this.rightAligned = b;
	}
	
	public boolean isRightAligned() {
		return rightAligned || (getAction() != null && getAction().requestRightAlign());
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	@Override
	public void setButtonStyle(ButtonDrawStyle style) {
		// empty, override if needed
	}
	
	public static void updateButtonTitle(
			final WeakReference<NavigationButton> r_n,
			final WeakReference<JButton> r_n1,
			final Runnable iconUpdateCheck) {
		if (r_n1 == null || r_n.get() == null)
			return;
		
		final ObjectRef rr = new ObjectRef();
		Runnable r = new Runnable() {
			String lastImage = null;
			
			@Override
			public void run() {
				final NavigationButton n = r_n.get();
				JButton n1 = r_n1.get();
				boolean imageUpdated = false;
				if (n == null)
					return;
				if (n1 == null)
					return;
				if ((n.isProcessing() || n.requestsTitleUpdates()) && !n.isRemoved()) {
					if (n.getAction() != null && n.getAction().requestRefresh()) {
						BackgroundTaskHelper.executeLaterOnSwingTask(1000, new Runnable() {
							@Override
							public void run() {
								n.performAction();
							}
						});
					}
					String ai = n.getNavigationImage();
					if (lastImage == null)
						lastImage = ai;
					if (ai != null && !ai.equals(lastImage)) {
						lastImage = ai;
						if (iconUpdateCheck != null) {
							iconUpdateCheck.run();
							imageUpdated = true;
						}
					}
					n1.setText(n.getTitle());// + "!!!!!" + System.currentTimeMillis());
					if (n1.getText() != null && n1.getText().indexOf("Please wait") >= 0) {
						if (!n1.isVisible())
							System.out.println("UPDATING INVISIBLE BUTTON " + n1.getText());
						// if (n.getRunnableIconCheck() == null || n.getRunnableIconCheck() == rr.getObject()) {
						n.setRunnableIconCheck(rr.getObject());
						BackgroundTaskHelper.executeLaterOnSwingTask(2000, (Runnable) rr.getObject());
						// }
					} else
						if (!n1.getText().contains("[REMOVE FROM UPDATE]")) {
							if (!n1.isVisible())
								System.out.println("UPDATING INVISIBLE BUTTON " + n1.getText());
							// if (n.getRunnableIconCheck() == null || n.getRunnableIconCheck() == rr.getObject()) {
							n.setRunnableIconCheck(rr.getObject());
							BackgroundTaskHelper.executeLaterOnSwingTask(
									imageUpdated ? 500 : 500,
									(Runnable) rr.getObject());
							// }
						} else
							n.setRunnableIconCheck(new Object());
				} else {
					n1.setText(n.getTitle());// + "?????" + System.currentTimeMillis());
					n.setRunnableIconCheck(new Object());
				}
			}
		};
		rr.setObject(r);
		r.run();
	}
	
	protected Object getRunnableIconCheck() {
		return validIconCheckObject;
	}
	
	public void setRunnableIconCheck(Object validObject) {
		this.validIconCheckObject = validObject;
	}
	
	public static void checkButtonTitle(
			final WeakReference<NavigationButton> wn,
			final WeakReference<JButton> wn1) {
		if (wn1 == null || wn == null)
			return;
		final ObjectRef rr = new ObjectRef();
		Runnable r = new Runnable() {
			@Override
			public void run() {
				final NavigationButton n = wn.get();
				JButton n1 = wn1.get();
				if (n1 == null || n == null)
					return;
				if (n.getAction() != null && n.getAction().requestRefresh()) {
					BackgroundTaskHelper.executeLaterOnSwingTask(1000, new Runnable() {
						@Override
						public void run() {
							n.performAction();
						}
					});
				}
				if ((n.isProcessing() || n.requestsTitleUpdates()) && n1.isVisible()) {
					String title = n.getTitle();
					n1.setText(title);
					if (n1.getText().indexOf("Please wait") >= 0)
						BackgroundTaskHelper.executeLaterOnSwingTask(2000, (Runnable) rr.getObject());
					else
						BackgroundTaskHelper.executeLaterOnSwingTask(500, (Runnable) rr.getObject());
				} else {
					if (n1.isVisible()) {
						String title = n.getTitle();
						n1.setText(title);
					}
					BackgroundTaskHelper.executeLaterOnSwingTask(500, (Runnable) rr.getObject());
				}
			}
		};
		rr.setObject(r);
		r.run();
	}
	
	public void executeNavigation(final PanelTarget target, final IAPnavigationPanel navPanel,
			final IAPnavigationPanel actionPanel, final JComponent graphPanel, final JButton n1,
			final Runnable optFinishAction, final ButtonDrawStyle style) {
		executeNavigation(target, navPanel, actionPanel, graphPanel, n1, optFinishAction, false, style);
	}
	
	public void executeNavigation(final PanelTarget target, final IAPnavigationPanel navPanel,
			final IAPnavigationPanel actionPanel, final JComponent graphPanel, final JButton n1,
			final Runnable optFinishAction, final boolean recursive,
			final ButtonDrawStyle style) {
		final NavigationButton srcNavGraphicslEntity = this;
		if (n1 != null && srcNavGraphicslEntity.isProcessing()) {
			if (n1.getText().equalsIgnoreCase("Please wait"))
				n1.setText("Please wait!");
			else
				n1.setText("Please wait");
			return;
		}
		
		if (srcNavGraphicslEntity.getAction() != null) {
			if (srcNavGraphicslEntity.getAction().getStatusProvider() == null) {
				System.err.println("Internal Error: No Status-Provider available! Action: "
						+ srcNavGraphicslEntity.getAction().getDefaultTitle());
				return;
			}
			
			srcNavGraphicslEntity.getAction().getStatusProvider().setCurrentStatusValue(-1);
			srcNavGraphicslEntity.setProcessing(true);
			
			Runnable iconUpdateCheck = style != null ? getIconUpdateCheckRunnable(
					new WeakReference<NavigationButton>(srcNavGraphicslEntity), target,
					new WeakReference<JButton>(n1),
					getImageSize(style, target)) : null;
			NavigationButton.updateButtonTitle(
					new WeakReference<NavigationButton>(srcNavGraphicslEntity),
					new WeakReference<JButton>(n1), iconUpdateCheck);
			MyUtility.navigate(navPanel.getEntitySet(false), srcNavGraphicslEntity.getTitle());
			final NavigationAction na = srcNavGraphicslEntity.getAction();
			
			boolean execute = true;
			ParameterOptions params = na.getParameters();
			if (params != null && params.userRequestNeeded()) {
				Object[] res = MyInputHelper.getInput(params.getDescription(), srcNavGraphicslEntity.getTitle(), params.getParameterField());
				if (res == null) {
					execute = false;
					srcNavGraphicslEntity.setProcessing(false);
					if (n1 != null)
						n1.setText(srcNavGraphicslEntity.getTitle());
					// srcNavGraphicslEntity.getAction().getStatusProvider().setCurrentStatusValue(100);
				} else
					na.setParameters(res);
			}
			
			if (execute)
				BackgroundTaskHelper.issueSimpleTask(srcNavGraphicslEntity.getTitle(), "Please wait...", new Runnable() {
					@Override
					public void run() {
						try {
							na.performActionCalculateResults(srcNavGraphicslEntity);
							// Thread.sleep(10000);
						} catch (Exception e) {
							if (n1 != null)
								n1.setText("Error");
							ErrorMsg.addErrorMessage(e);
						}
					}
				}, new Runnable() {
					@Override
					public void run() {
						try {
							boolean reload = false;
							MainPanelComponent mpc = na.getResultMainPanel();
							if (mpc != null) {
								graphPanel.removeAll();
								JComponent gui = mpc.getGUI();
								if (ErrorMsg.getErrorMsgCount() > 0) {
									ArrayList<JComponent> errors = new ArrayList<JComponent>();
									for (String s : ErrorMsg.getErrorMessages()) {
										JLabel e = new JLabel("<html><table><tr><td>"
												+ StringManipulationTools.removeHTMLtags(s.replaceAll("<br>", "_br_"))
														.replaceAll("_br_", "<br>").replaceAll("\n", "<br>"));
										e.setOpaque(true);
										e.setBackground(new Color(255, 240, 240));
										e.setBorder(BorderFactory.createLoweredBevelBorder());
										errors.add(e);
									}
									ErrorMsg.clearErrorMessages();
									gui = TableLayout.getSplitVertical(TableLayout.getMultiSplitVertical(errors, 2),
											gui.getClientProperty("isHTML") != null ? new JScrollPane(new OnlyVerticalScrollPanel(gui)) : gui,
											TableLayout.PREFERRED, TableLayout.FILL);
								} else
									if (gui != null)
										gui = gui.getClientProperty("isHTML") != null ? new JScrollPane(new OnlyVerticalScrollPanel(gui)) : gui;
								if (gui != null)
									graphPanel.add(gui, "0,0");
								graphPanel.revalidate();
								graphPanel.repaint();
							} else {
								if (ErrorMsg.getErrorMsgCount() > 0) {
									graphPanel.removeAll();
									ArrayList<JComponent> errors = new ArrayList<JComponent>();
									for (String s : ErrorMsg.getErrorMessages()) {
										JLabel e = new JLabel("<html><table><tr><td>"
												+ StringManipulationTools.removeHTMLtags(s.replaceAll("<br>", "_br_"))
														.replaceAll("_br_", "<br>").replaceAll("\n", "<br>"));
										e.setOpaque(true);
										e.setBackground(new Color(255, 240, 240));
										e.setBorder(BorderFactory.createLoweredBevelBorder());
										errors.add(e);
									}
									ErrorMsg.clearErrorMessages();
									
									JComponent gui = TableLayout.getMultiSplitVertical(errors, 2);
									
									graphPanel.add(gui, "0,0");
									graphPanel.revalidate();
									graphPanel.repaint();
								}
							}
							
							if (target == PanelTarget.NAVIGATION) {
								ArrayList<NavigationButton> prior = new ArrayList<NavigationButton>();
								boolean includeBookmarks = false;
								ArrayList<NavigationButton> var = navPanel.getEntitySet(includeBookmarks);
								if (var != null)
									for (final NavigationButton ss : var) {
										if (ss != srcNavGraphicslEntity)
											prior.add(ss);
										else
											break;
									}
								ArrayList<NavigationButton> res = na.getResultNewNavigationSet(prior);
								if (res != null && res.size() > 0 && res.get(res.size() - 1) == null) {
									String path = IAPnavigationPanel.getTargetPath(res);
									IAPgui.navigateTo(path, NavigationButton.this, null);
								} else {
									if (res == null || res.isEmpty()) {
										if (res == null)
											res = new ArrayList<NavigationButton>();
										res.add(NavigationButton.this);
									}
									navPanel.setEntitySet(res);
								}
							} else {
								boolean includeBookmarks = false;
								ArrayList<NavigationButton> var = navPanel.getEntitySet(includeBookmarks);
								ArrayList<NavigationButton> set = na.getResultNewNavigationSet(var);
								boolean execute = false;
								NavigationButton del = null;
								if (set != null)
									if (set != null && set.size() > 0 && set.get(set.size() - 1) == null) {
										String path = IAPnavigationPanel.getTargetPath(set);
										IAPgui.navigateTo(path, NavigationButton.this, null);
										reload = true;
									} else {
										for (final NavigationButton src : set) {
											if (execute) {
												del = src;
												SwingUtilities.invokeLater(new Runnable() {
													@Override
													public void run() {
														src.executeNavigation(target, navPanel, actionPanel, graphPanel, n1, null, true, style);
													}
												});
												break;
											}
											if (src == srcNavGraphicslEntity)
												execute = !recursive;
										}
										if (del != null)
											set.remove(del);
										navPanel.setEntitySet(set);
									}
							}
							if (!reload) {
								ArrayList<NavigationButton> actions = na.getResultNewActionSet();
								if (actions != null && na.getAdditionalEntities() != null)
									actions.addAll(na.getAdditionalEntities());
								actionPanel.setEntitySet(actions);
							}
							if (optFinishAction != null) {
								BackgroundTaskHelper.executeLaterOnSwingTask(10, optFinishAction);
							}
							
						} finally {
							srcNavGraphicslEntity.setProcessing(false);
							if (n1 != null)
								n1.setText(srcNavGraphicslEntity.getTitle());
							// srcNavGraphicslEntity.getAction().getStatusProvider().setCurrentStatusValue(100);
						}
					}
				}, srcNavGraphicslEntity.getAction().getStatusProvider());
		}
	}
	
	public static JComponent getNavigationButton(final ButtonDrawStyle style, final NavigationButton n,
			final PanelTarget target, final IAPnavigationPanel navPanel, final IAPnavigationPanel actionPanel,
			final JComponent graphPanel) {
		
		if (n.getGUI() != null)
			return n.getGUI();
		
		int imgS = getImageSize(style, target);
		
		ImageIcon icon = null;
		if (n != null && n.getAction() != null && (n.getAction() instanceof ImageProvider)) {
			ImageProvider ip = (ImageProvider) n.getAction();
			if (ip.getImage() != null)
				icon = new ImageIcon(ip.getImage());
		}
		
		if (target == PanelTarget.NAVIGATION && n.getIconActive() != null && n.getIconActive().getImage() != null) {
			icon = new ImageIcon(GravistoService.getScaledImage(n.getIconActive().getImage(), -imgS, imgS));
		} else
			if (target == PanelTarget.ACTION && n.getIconInactive() != null && n.getIconInactive().getImage() != null) {
				icon = new ImageIcon(GravistoService.getScaledImage(n.getIconInactive().getImage(), -imgS, imgS));
			} else {
				if (target == PanelTarget.NAVIGATION) {
					icon = GravistoService.loadIcon(IAPmain.class, n.getNavigationImage(), -imgS, imgS, false);
					if (icon == null && n.getAction() != null) {
						icon = GravistoService.loadIcon(n.getAction().getClass(), n.getNavigationImage(), -imgS, imgS, true);
					}
					
				}
				if (target != PanelTarget.NAVIGATION || icon == null) {
					icon = GravistoService.loadIcon(IAPmain.class, n.getActionImage(), -imgS, imgS, false);
					if (icon == null && n.getAction() != null) {
						icon = GravistoService.loadIcon(n.getAction().getClass(), n.getActionImage(), -imgS, imgS, true);
					}
				}
			}
		if (icon != null)
			icon.setDescription(imgS + "");
		
		final NavigationAction ac = n.action;
		
		final JButton n1 = new JButton() {
			private static final long serialVersionUID = 1L;
			
			boolean removeCalled = false;
			
			@Override
			public void setText(String title) {
				super.setText(process(title));
				if (!removeCalled)
					if (title != null && title.contains("[REMOVE FROM UPDATE]")) {
						removeCalled = true;
						if (n.getGUIsetting() != null && n.getGUIsetting().getActionPanel() != null)
							n.getGUIsetting().getActionPanel().updateGUI();
					}
			}
			
			private String process(String title) {
				if (n.isProcessing() && ac != null && ac.getStatusProvider() != null && ac.getStatusProvider().pluginWaitsForUser())
					return title + "<hr><small>[Waiting for User-Interaction]<br>[Please right-click command button]</small>";
				if (n.isProcessing() && ac != null && ac.getStatusProvider() != null && ac.getStatusProvider().wantsToStop())
					return title + "<hr><small>[Command-Interruption Requested]<br>[Please wait]</small>";
				return title;
			}
			
			@Override
			public String getToolTipText(MouseEvent e) {
				return n.getToolTip();
			}
			
		};
		n.setButton(new WeakReference<JButton>(n1));
		n.getTitle();
		
		n1.setToolTipText(n.getToolTip());
		
		if (n.getAction() != null && !n.enabled)
			n1.setEnabled(false);
		
		switch (style) {
			case FLAT:
			case COMPACT_LIST:
			case COMPACT_LIST_2:
				int d = 1;
				if (style == ButtonDrawStyle.COMPACT_LIST_2) {
					n1.setMargin(new Insets(1, 1, 1, 1));
				}
				if (style == ButtonDrawStyle.COMPACT_LIST) {
					d = Integer.MAX_VALUE;
					icon = null;
					n1.setMargin(new Insets(1, 1, 1, 1));
					n1.setBorderPainted(false);
					n1.setContentAreaFilled(true);
					n1.setFont(Font.getFont(Font.SANS_SERIF));
				} else {
					n1.setBorderPainted(false);
					n1.setContentAreaFilled(false);
					if (target == PanelTarget.NAVIGATION)
						n1.setBorder(BorderFactory.createEmptyBorder(8 / d, 4 / d, 2 / d, 4 / d));
					else
						if (style == ButtonDrawStyle.FLAT)
							n1.setBorder(BorderFactory.createEmptyBorder(8 / d, 8 / d, 2 / d, 8 / d));
						else
							n1.setBorder(BorderFactory.createEmptyBorder(8 / d, 0 / d, 2 / d, 16 / d));
				}
				break;
			case TEXT:
				icon = null;
				// fall through
			case BUTTONS:
				n1.setBorderPainted(true);
				n1.setContentAreaFilled(true);
		}
		
		n1.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		
		n1.addMouseListener(new MouseListener() {
			@Override
			public void mouseReleased(MouseEvent e) {
				checkForContextMenu(n, ac, e);
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
				checkForContextMenu(n, ac, e);
			}
			
			private void checkForContextMenu(final NavigationButton n, final NavigationAction ac, MouseEvent e) {
				if (e.isPopupTrigger()) {
					if (ac != null && ac.getStatusProvider() != null) {
						JPopupMenu p = new JPopupMenu();
						boolean added = false;
						
						boolean showCommandToShowCommandInNewWindow = true;
						if (showCommandToShowCommandInNewWindow && ac != null) {
							JMenuItem menuItem = new JMenuItem("Execute Command, Show Results in New Window");
							menuItem.setIcon(GravistoService.loadIcon(IAPmain.class, "img/new_frame.png"));
							menuItem.addActionListener(navPanel.getNewWindowListener(ac, false));
							p.add(menuItem);
							added = true;
							
						}
						
						if (n.isProcessing() && n.getAction() != null) {
							AbstractAction a = new AbstractAction("Show Progress Dialog") {
								@Override
								public void actionPerformed(ActionEvent e) {
									createProgressDialogForRunningAction(n, ac);
								}
							};
							JMenuItem menuItem = new JMenuItem(a);
							menuItem.setIcon(GravistoService.loadIcon(IAPmain.class, "img/ext/gpl2/Gnome-System-Run-64.png", 16, 16, false));
							p.add(menuItem);
							added = true;
						}
						
						if (ac.getStatusProvider().wantsToStop()) {
							AbstractAction a = new AbstractAction("Stop is Requested (Command is still active)") {
								@Override
								public void actionPerformed(ActionEvent e) {
									MainFrame.getInstance().showMessageDialog(
											"<html>"
													+ "Command execution interruption has been requested.<br>"
													+ "Depending on the execution status, this request may be fullfilled with<br>"
													+ "a non-constant delay.",
											"Information");
								}
							};
							p.add(new JMenuItem(a));
							added = true;
						} else
							if (n.isProcessing()) {
								AbstractAction a = new AbstractAction("Request Stop (Command is active)") {
									@Override
									public void actionPerformed(ActionEvent e) {
										ac.getStatusProvider().pleaseStop();
										if (!ac.getStatusProvider().wantsToStop()) {
											MainFrame.getInstance().showMessageDialog(
													"<html>"
															+ "This command does not support interruption of its processing.<br><br>"
															+ "Please wait for command completion or quit the program to<br>"
															+ "stop command processing in complete.",
													"Information");
										}
									}
								};
								JMenuItem menuItem = new JMenuItem(a);
								menuItem.setIcon(GravistoService.loadIcon(IAPmain.class, "img/ext/gpl2/Gnome-Media-Playback-Stop-64.png", 16, 16, false));
								p.add(menuItem);
								added = true;
							}
						if (n.isProcessing() && ac.getStatusProvider().pluginWaitsForUser()) {
							AbstractAction a = new AbstractAction("Continue (Command waits for user-action)") {
								@Override
								public void actionPerformed(ActionEvent e) {
									ac.getStatusProvider().pleaseContinueRun();
								}
							};
							JMenuItem menuItem = new JMenuItem(a);
							menuItem.setIcon(GravistoService.loadIcon(IAPmain.class, "img/ext/gpl2/Gnome-Media-Playback-Start-64.png", 16, 16, false));
							p.add(menuItem);
							added = true;
						}
						
						if (added) {
							p.show(e.getComponent(), e.getX(), e.getY());
						} else {
							System.out.println(SystemAnalysis.getCurrentTime() + ">INFO: Command not active, popup-menu contains no commands and is not shown.");
							java.awt.Toolkit.getDefaultToolkit().beep();
						}
					}
				}
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
				// if (n1 != null && n1.getText() != null && n1.getText().startsWith("<html><u>"))
				// n1.setText(n1.getText().substring("<html><u>".length()));
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
				// n1.setText("<html><u>" + n1.getText());
			}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				//
				
			}
		});
		
		if (n instanceof NavigationButtonCalendar2) {
			if (style != ButtonDrawStyle.TEXT) {
				if (icon != null)
					icon = new MyCalendarIcon(icon, (NavigationButtonCalendar2) n, imgS);
				((NavigationButtonCalendar2) n).setPostUpdateRunner(new Runnable() {
					@Override
					public void run() {
						n1.repaint();
					}
				});
			} else
				icon = null;
		}
		
		if (icon != null)
			n1.setIcon(icon);
		
		final int imgSf = imgS;
		
		final Runnable iconUpdateCheck;
		
		if (n.isProcessing() || n.requestsTitleUpdates()) {
			iconUpdateCheck = getIconUpdateCheckRunnable(
					new WeakReference<NavigationButton>(n), target,
					new WeakReference<JButton>(n1), imgSf);
			NavigationButton.updateButtonTitle(
					new WeakReference<NavigationButton>(n),
					new WeakReference<JButton>(n1),
					iconUpdateCheck);
		} else {
			iconUpdateCheck = null;
			WeakReference<JButton> wn1 = new WeakReference<JButton>(n1);
			WeakReference<NavigationButton> wn = new WeakReference<NavigationButton>(n);
			NavigationButton.checkButtonTitle(wn, wn1);
		}
		
		n1.setOpaque(false);
		
		if (style != ButtonDrawStyle.COMPACT_LIST || target == PanelTarget.ACTION) {
			n1.setVerticalTextPosition(SwingConstants.BOTTOM);
			n1.setHorizontalTextPosition(SwingConstants.CENTER);
			if (style == ButtonDrawStyle.COMPACT_LIST)
				n1.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
		} else {
			n1.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
		}
		if (style == ButtonDrawStyle.COMPACT_LIST_2) {
			n1.setVerticalTextPosition(SwingConstants.BOTTOM);
			n1.setHorizontalTextPosition(SwingConstants.CENTER);
			n1.setFont(new Font(n1.getFont().getName(), Font.PLAIN, 10));
		}
		n.setExecution(new Runnable() {
			@Override
			public void run() {
				n.executeNavigation(target, navPanel, actionPanel, graphPanel, n1, iconUpdateCheck, style);
			}
		});
		n1.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				n.setLastEvent(e);
				n.performAction();
			}
		});
		
		if (n.getToolTip() != null && n.getToolTip().length() > 0)
			n1.setToolTipText(n.getToolTip());
		
		JComponent rr;
		if (n.getSideGui() != null)
			rr = TableLayout.get3Split(n1, null, n.getSideGui(), TableLayout.PREFERRED, n.getSideGuiSpace(),
					n.getSideGuiWidth());
		else
			rr = n1;
		
		// if (n.isRightAligned()) {
		// MarkComponent mc = new MarkComponent(rr, true, TableLayout.PREFERRED, false);
		// int r = 255;
		// int g = 180;
		// int b = 180;
		// mc.setMarkColor(new Color(r, g, b), null);
		// mc.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));
		// return mc;
		// } else
		return rr;
	}
	
	private void setButton(WeakReference<JButton> weakButtonReference) {
		this.weakButtonReference = weakButtonReference;
	}
	
	protected void setLastEvent(ActionEvent lastEvent) {
		this.lastEvent = lastEvent;
	}
	
	public static int defaultButtonSize = 48;
	
	private static int getImageSize(ButtonDrawStyle style, final PanelTarget target) {
		int imgS = defaultButtonSize;
		
		if (style == ButtonDrawStyle.COMPACT_LIST || style == ButtonDrawStyle.COMPACT_LIST_2)
			if (target == PanelTarget.NAVIGATION || style == ButtonDrawStyle.COMPACT_LIST_2)
				imgS = 25;// 32;
			else
				imgS = defaultButtonSize;
		return imgS;
	}
	
	private static Runnable getIconUpdateCheckRunnable(
			final WeakReference<NavigationButton> wr_n,
			final PanelTarget target,
			final WeakReference<JButton> wr_n1, final int imgSf) {
		Runnable iconUpdateCheck = new Runnable() {
			@Override
			public void run() {
				NavigationButton n = wr_n.get();
				if (n == null)
					return;
				JButton n1 = wr_n1.get();
				if (n1 == null)
					return;
				ImageIcon icon;
				if (target == PanelTarget.NAVIGATION && n.getIconActive() != null) {
					icon = new ImageIcon(GravistoService.getScaledImage(n.getIconActive().getImage(), -imgSf, imgSf));
				} else
					if (target == PanelTarget.ACTION && n.getIconInactive() != null) {
						icon = new ImageIcon(GravistoService.getScaledImage(n.getIconInactive().getImage(), -imgSf, imgSf));
					}
					else {
						if (target == PanelTarget.NAVIGATION)
							icon = GravistoService.loadIcon(IAPmain.class, n.getNavigationImage(), -imgSf, imgSf);
						else
							icon = GravistoService.loadIcon(IAPmain.class, n.getActionImage(), -imgSf, imgSf);
					}
				if (icon != null)
					icon.setDescription(imgSf + "");
				n1.setIcon(icon);
			}
		};
		return iconUpdateCheck;
	}
	
	public static void createProgressDialogForRunningAction(final NavigationButton n, final NavigationAction ac) {
		final BackgroundTaskWindow taskWindow = new BackgroundTaskWindow(false);
		taskWindow.setStatusProvider(ac.getStatusProvider(),
				ac.getDefaultTitle(), ac.getDefaultTitle());
		final ThreadSafeOptions tso = new ThreadSafeOptions();
		Timer checkStatus = new Timer(100, new ActionListener() {
			boolean finishedCalled = false;
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				taskWindow.updateSize();
				if (!n.isProcessing()) {
					if (!finishedCalled) {
						finishedCalled = true;
						taskWindow.dispose();
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
	
	@Override
	public String toString() {
		return StringManipulationTools.removeHTMLtags(getTitle());
	}
	
	public GUIsetting getGUIsetting() {
		return guiSetting;
	}
	
	public String getIconStaticId() {
		return optStaticIconId;
	}
	
	public void removedCleanup() {
		if (action.requestTitleUpdates())
			return;
		// System.out.println("REMOVED: " + StringManipulationTools.removeHTMLtags(getTitle()).trim());
		title = "<html>[REMOVE FROM UPDATE]<br>[this command needs refresh]<br>[go one step back in command history]<br>" + title;
		navigationImage = null;
		actionImage = null;
		action = null;
		tooltipHint = null;
		gui = null;
		processing = false;
		sideGui = null;
		icon = null;
		optFinishAction = null;
		execution = null;
		statusServer = null;
		guiSetting = null;
		optStaticIconId = null;
		overrideTitle = null;
		iconUpdated = false;
	}
	
	public boolean isRemoved() {
		boolean res = guiSetting == null || (
				!guiSetting.getNavigationPanel().getEntitySet(true).contains(this)
				&& !guiSetting.getActionPanel().getEntitySet(true).contains(this));
		return res;
	}
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	public ActionEvent getEventSource() {
		return lastEvent;
	}
}
