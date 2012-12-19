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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.ErrorMsg;
import org.ObjectRef;
import org.ProgressStatusService;
import org.StringManipulationTools;
import org.graffiti.editor.GravistoService;

import de.ipk.ag_ba.commands.bookmarks.BookmarkAction;
import de.ipk.ag_ba.gui.IAPfeature;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.MyNavigationPanel;
import de.ipk.ag_ba.gui.PanelTarget;
import de.ipk.ag_ba.gui.calendar.MyCalendarIcon;
import de.ipk.ag_ba.gui.calendar.NavigationButtonCalendar2;
import de.ipk.ag_ba.gui.enums.ButtonDrawStyle;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.interfaces.StyleAware;
import de.ipk.ag_ba.gui.navigation_actions.ParameterOptions;
import de.ipk.ag_ba.gui.util.MyUtility;
import de.ipk.ag_ba.gui.webstart.IAPgui;
import de.ipk.ag_ba.gui.webstart.IAPmain;
import de.ipk.ag_ba.gui.webstart.OnlyVerticalScrollPanel;
import de.ipk.ag_ba.server.task_management.CloundManagerNavigationAction;
import de.ipk.ag_ba.server.task_management.RemoteCapableAnalysisAction;
import de.ipk_gatersleben.ag_nw.graffiti.MyInputHelper;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;

/**
 * @author klukas
 */
public class NavigationButton implements StyleAware {
	
	private String title;
	private String navigationImage, actionImage;
	private NavigationAction action;
	private String tooltipHint;
	private JComponent gui;
	private boolean processing;
	
	private long processingStart = 0;
	private JComponent sideGui;
	private double sideGuiSpace;
	private double sideGuiWidth;
	private ImageIcon icon;
	
	protected Runnable optFinishAction = null;
	private Runnable execution;
	private boolean rightAligned;
	private ProgressStatusService statusServer;
	private final GUIsetting guiSetting;
	private String optStaticIconId;
	private String overrideTitle;
	private boolean iconUpdated;
	
	public NavigationButton(String overrideTitle, NavigationAction navigationAction, GUIsetting guiSetting) {
		this(navigationAction, guiSetting);
		this.overrideTitle = overrideTitle;
	}
	
	public NavigationButton(NavigationAction navigationAction, GUIsetting guiSetting) {
		if (navigationAction != null) {
			boolean enableRemoteTaskExecution = IAPmain.isSettingEnabled(IAPfeature.REMOTE_EXECUTION);
			if (enableRemoteTaskExecution && navigationAction instanceof RemoteCapableAnalysisAction) {
				RemoteCapableAnalysisAction rca = (RemoteCapableAnalysisAction) navigationAction;
				CloundManagerNavigationAction ra = new CloundManagerNavigationAction(rca.getMongoDB(), false);
				navigationAction = new RemoteExecutionWrapperAction(navigationAction,
						new NavigationButton(ra, guiSetting));
			}
			navigationAction.setSource(navigationAction, guiSetting);
			this.setTitle(navigationAction.getDefaultTitle());
			this.navigationImage = null;
			this.actionImage = null;
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
	}
	
	public NavigationButton(NavigationAction navigationAction, String title, String navigationImage, String actionImage,
			GUIsetting guiSetting) {
		this(navigationAction, guiSetting);
		this.setTitle(title);
		this.navigationImage = navigationImage;
		this.actionImage = actionImage;
	}
	
	public NavigationButton(JComponent gui, GUIsetting guiSetting) {
		this.gui = gui;
		this.guiSetting = guiSetting;
	}
	
	public NavigationButton(BookmarkAction ba, BufferedImage image, GUIsetting guiSetting, String optStaticIconId) {
		this(ba, guiSetting);
		this.optStaticIconId = optStaticIconId;
		this.icon = image != null ? new ImageIcon(image) : null;
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
	
	public String getTitle() {
		String res = getTitle(false);
		return res != null ? res : res + "";
	}
	
	public String getTitle(boolean forceProgressText) {
		String adt = action != null ? action.getDefaultTitle() : null;
		if (action != null && adt != null && adt.length() > 0)
			title = adt;
		
		if (overrideTitle != null)
			title = overrideTitle;
		
		long compTime = System.currentTimeMillis() - processingStart;
		if (!forceProgressText && !(isProcessing() || requestsTitleUpdates()) || compTime < 1000)
			return title;
		else {
			String dots = "";
			int speed = 500;
			int ndots = (int) ((compTime % (speed * 4)) / speed);
			String cc = "*";
			if (ndots == 0)
				cc = "\\";
			if (ndots == 1)
				cc = "|";
			if (ndots == 2)
				cc = "/";
			if (ndots == 3)
				cc = "-";
			
			if (requestsTitleUpdates()) {
				if (ndots < 2)
					cc = "";
				else
					cc = "";
			}
			
			while (dots.length() < 1 && cc.length() > 0)
				dots += cc;
			
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
				s += "[" + getProgress("#", "-", len + 5, dp) + "]";
			}
			String line2 = "";
			String sm1 = "", sm2 = "", sm3 = "";
			if (action.getStatusProvider() != null) {
				sm1 = action.getStatusProvider().getCurrentStatusMessage1();
				sm2 = action.getStatusProvider().getCurrentStatusMessage2();
				sm3 = action.getStatusProvider().getCurrentStatusMessage3();
			}
			if (sm1 != null && sm1.length() > 0)
				line2 = sm1;
			if (sm2 != null && sm2.length() > 0)
				line2 += (sm1 != null && sm1.length() > 0 ? "<p>" : "") + sm2;
			if (sm3 != null && sm3.length() > 0)
				line2 += (line2 != null && sm2 != null && line2.length() + sm2.length() > 0 ? "<p>" : "") + sm3;
			
			if (statusServer != null) {
				String eta = statusServer.getRemainTime((int) dp == -1, dp);
				if (eta.length() > 0) {
					if (line2.length() > 0 && sm3 != null)
						line2 += sm3.length() == 0 ? ", " : "<br>";
					if (sm3 == null || sm3.length() == 0)
						line2 += "" + eta;
				}
			}
			if (line2.length() > 0)
				line2 = "<br>&nbsp;" + line2 + "&nbsp;";
			dots = "" + dots + "";
			
			line2 = StringManipulationTools.stringReplace(line2, "<br><hr>", "<hr>");
			line2 = StringManipulationTools.stringReplace(line2, "<p><hr>", "<hr>");
			
			if (dp < -1.01) {
				System.out.println("Command " + title + " has lost its connection to the status provider.");
				return "<html><center>" + dots + " " + "[REMOVE FROM UPDATE] " + title + progress + " " + dots + "" + s + line2;
			} else
				return "<html><center>" + dots + " " + title + progress + " " + dots + "" + s + line2;
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
	
	public boolean willProvideActions() {
		return action != null ? action.isProvidingActions() : false;
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
		return rightAligned;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	@Override
	public void setButtonStyle(ButtonDrawStyle style) {
		// empty, override if needed
	}
	
	public static void checkButtonTitle(final NavigationButton n, final JButton n1,
			final Runnable iconUpdateCheck) {
		if (n1 == null)
			return;
		final ObjectRef rr = new ObjectRef();
		Runnable r = new Runnable() {
			String lastImage = null;
			
			@Override
			public void run() {
				if ((n.isProcessing() || n.requestsTitleUpdates()) && n1.isVisible()) {
					String ai = n.getNavigationImage();
					if (lastImage == null)
						lastImage = ai;
					if (ai != null && !ai.equals(lastImage)) {
						lastImage = ai;
						if (iconUpdateCheck != null)
							iconUpdateCheck.run();
					}
					n1.setText(n.getTitle());
					// System.out.println(n.getTitle());
					if (n1.getText() != null && n1.getText().indexOf("Please wait") >= 0)
						BackgroundTaskHelper.executeLaterOnSwingTask(2000, (Runnable) rr.getObject());
					else
						if (!n1.getText().contains("[REMOVE FROM UPDATE]"))
							BackgroundTaskHelper.executeLaterOnSwingTask(500, (Runnable) rr.getObject());
				} else {
					if (n1.isVisible())
						n1.setText(n.getTitle());
				}
			}
		};
		rr.setObject(r);
		r.run();
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
				NavigationButton n = wn.get();
				JButton n1 = wn1.get();
				if (n1 == null || n == null)
					return;
				if ((n.isProcessing() || n.requestsTitleUpdates()) && n1.isVisible()) {
					n1.setText(n.getTitle());
					if (n1.getText().indexOf("Please wait") >= 0)
						BackgroundTaskHelper.executeLaterOnSwingTask(2000, (Runnable) rr.getObject());
					else
						BackgroundTaskHelper.executeLaterOnSwingTask(500, (Runnable) rr.getObject());
				} else {
					if (n1.isVisible())
						n1.setText(n.getTitle());
					BackgroundTaskHelper.executeLaterOnSwingTask(500, (Runnable) rr.getObject());
				}
			}
		};
		rr.setObject(r);
		r.run();
	}
	
	public void executeNavigation(final PanelTarget target, final MyNavigationPanel navPanel,
			final MyNavigationPanel actionPanel, final JComponent graphPanel, final JButton n1,
			final Runnable optFinishAction) {
		executeNavigation(target, navPanel, actionPanel, graphPanel, n1, optFinishAction, false);
	}
	
	public void executeNavigation(final PanelTarget target, final MyNavigationPanel navPanel,
			final MyNavigationPanel actionPanel, final JComponent graphPanel, final JButton n1,
			final Runnable optFinishAction, final boolean recursive) {
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
			NavigationButton.checkButtonTitle(srcNavGraphicslEntity, n1, null);
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
									gui = gui.getClientProperty("isHTML") != null ? new JScrollPane(new OnlyVerticalScrollPanel(gui)) : gui;
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
									String path = MyNavigationPanel.getTargetPath(res);
									IAPgui.navigateTo(path, NavigationButton.this);
								} else
									navPanel.setEntitySet(res);
							} else {
								boolean includeBookmarks = false;
								ArrayList<NavigationButton> var = navPanel.getEntitySet(includeBookmarks);
								ArrayList<NavigationButton> set = na.getResultNewNavigationSet(var);
								boolean execute = false;
								NavigationButton del = null;
								if (set != null)
									if (set != null && set.size() > 0 && set.get(set.size() - 1) == null) {
										String path = MyNavigationPanel.getTargetPath(set);
										IAPgui.navigateTo(path, NavigationButton.this);
										reload = true;
									} else {
										for (final NavigationButton src : set) {
											if (execute) {
												del = src;
												SwingUtilities.invokeLater(new Runnable() {
													@Override
													public void run() {
														src.executeNavigation(target, navPanel, actionPanel, graphPanel, n1, null, true);
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
	
	public static JComponent getNavigationButton(ButtonDrawStyle style, final NavigationButton n,
			final PanelTarget target, final MyNavigationPanel navPanel, final MyNavigationPanel actionPanel,
			final JComponent graphPanel) {
		
		if (n.getGUI() != null)
			return n.getGUI();
		
		int imgS = 48;
		
		if (style == ButtonDrawStyle.COMPACT_LIST || style == ButtonDrawStyle.COMPACT_LIST_2)
			if (target == PanelTarget.NAVIGATION || style == ButtonDrawStyle.COMPACT_LIST_2)
				imgS = 25;// 32;
			else
				imgS = 48;
		
		ImageIcon icon = null;
		if (target == PanelTarget.NAVIGATION && n.getIconActive() != null && n.getIconActive().getImage() != null) {
			icon = new ImageIcon(GravistoService.getScaledImage(n.getIconActive().getImage(), -imgS, imgS));
		} else
			if (target == PanelTarget.ACTION && n.getIconInactive() != null && n.getIconInactive().getImage() != null) {
				icon = new ImageIcon(GravistoService.getScaledImage(n.getIconInactive().getImage(), -imgS, imgS));
			} else {
				if (target == PanelTarget.NAVIGATION)
					icon = GravistoService.loadIcon(IAPmain.class, n.getNavigationImage(), -imgS, imgS);
				if (target != PanelTarget.NAVIGATION || icon == null)
					icon = GravistoService.loadIcon(IAPmain.class, n.getActionImage(), -imgS, imgS);
			}
		if (icon != null)
			icon.setDescription(imgS + "");
		
		final JButton n1 = new JButton("" + n.getTitle()) {
			private static final long serialVersionUID = 1L;
			
			@Override
			public String getToolTipText(MouseEvent e) {
				return n.getToolTip();
			}
			
		};
		n1.setToolTipText(n.getToolTip());
		
		switch (style) {
			case FLAT:
			case COMPACT_LIST:
			case COMPACT_LIST_2:
				n1.setBorderPainted(false);
				n1.setContentAreaFilled(false);
				int d = 1;
				if (style == ButtonDrawStyle.COMPACT_LIST)
					d = 2;
				if (target == PanelTarget.NAVIGATION)
					n1.setBorder(BorderFactory.createEmptyBorder(8 / d, 4 / d, 2 / d, 4 / d));
				else
					if (style == ButtonDrawStyle.FLAT)
						n1.setBorder(BorderFactory.createEmptyBorder(8 / d, 8 / d, 2 / d, 8 / d));
					else
						n1.setBorder(BorderFactory.createEmptyBorder(8 / d, 0 / d, 2 / d, 16 / d));
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
				//
				
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
				//
				
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
		
		Runnable iconUpdateCheck = new Runnable() {
			@Override
			public void run() {
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
		
		if (n.isProcessing() || n.requestsTitleUpdates()) {
			NavigationButton.checkButtonTitle(n, n1, iconUpdateCheck);
		} else {
			WeakReference<JButton> wn1 = new WeakReference<JButton>(n1);
			WeakReference<NavigationButton> wn = new WeakReference<NavigationButton>(n);
			NavigationButton.checkButtonTitle(wn, wn1);
		}
		
		n1.setOpaque(false);
		
		if (style != ButtonDrawStyle.COMPACT_LIST || target == PanelTarget.ACTION) {
			n1.setVerticalTextPosition(SwingConstants.BOTTOM);
			n1.setHorizontalTextPosition(SwingConstants.CENTER);
		} else {
			n1.setFont(new Font(n1.getFont().getName(), Font.PLAIN, 12));
		}
		if (style == ButtonDrawStyle.COMPACT_LIST_2) {
			n1.setVerticalTextPosition(SwingConstants.BOTTOM);
			n1.setHorizontalTextPosition(SwingConstants.CENTER);
			n1.setFont(new Font(n1.getFont().getName(), Font.PLAIN, 11));
		}
		n.setExecution(new Runnable() {
			@Override
			public void run() {
				n.executeNavigation(target, navPanel, actionPanel, graphPanel, n1, null);
			}
		});
		n1.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
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
}
