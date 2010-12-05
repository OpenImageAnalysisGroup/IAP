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
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.BackgroundTaskStatusProvider;
import org.ErrorMsg;
import org.ObjectRef;
import org.StringManipulationTools;
import org.graffiti.editor.GravistoService;

import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.MyNavigationPanel;
import de.ipk.ag_ba.gui.PanelTarget;
import de.ipk.ag_ba.gui.calendar.MyCalendarIcon;
import de.ipk.ag_ba.gui.enums.ButtonDrawStyle;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.interfaces.StyleAware;
import de.ipk.ag_ba.gui.navigation_actions.BookmarkAction;
import de.ipk.ag_ba.gui.navigation_actions.Calendar2;
import de.ipk.ag_ba.gui.util.MyUtility;
import de.ipk.ag_ba.gui.webstart.AIPgui;
import de.ipk.ag_ba.gui.webstart.AIPmain;
import de.ipk.ag_ba.rmi_server.task_management.RemoteCapableAnalysisAction;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.ProgressStatusService;

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

	public NavigationButton(NavigationAction navigationAction, GUIsetting guiSetting) {
		if (navigationAction != null) {
			if (navigationAction instanceof RemoteCapableAnalysisAction) {
				navigationAction = new RemoteExecutionWrapperAction(navigationAction);
			}
			this.setTitle(navigationAction.getDefaultTitle());
			this.navigationImage = navigationAction.getDefaultNavigationImage();
			this.actionImage = navigationAction.getDefaultImage();
		}
		if (guiSetting == null)
			System.out.println("ERROR: GUI-SETTING VARIABLE NOT ASSIGNED (INTERNAL ERROR)");
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

	public NavigationButton(BookmarkAction ba, BufferedImage image, GUIsetting guiSetting) {
		this(ba, guiSetting);
		this.icon = image != null ? new ImageIcon(image) : null;
	}

	public JComponent getGUI() {
		return gui;
	}

	public void setToolTipText(String hint) {
		this.tooltipHint = hint;
	}

	public String getNavigationImage() {
		return navigationImage;
	}

	public String getActionImage() {
		return actionImage;
	}

	public String getTitle() {
		long compTime = System.currentTimeMillis() - processingStart;
		if (!processing || compTime < 1000)
			return title;
		else {
			String dots = "";
			int ndots = (int) ((compTime % 1000) / 250);
			String cc = "";
			if (ndots == 0)
				cc = "\\";
			if (ndots == 1)
				cc = "|";
			if (ndots == 2)
				cc = "/";
			if (ndots == 3)
				cc = "-";

			while (dots.length() < 1)
				dots += cc;

			String progress = "";
			String s = "";
			if (action.getStatusProvider().getCurrentStatusValueFine() > 0) {
				if (action.getStatusProvider().getCurrentStatusValueFine() > 0)
					progress = "" + action.getStatusProvider().getCurrentStatusValue() + "%";
				while (progress.length() < 5)
					progress = "_" + progress;
				progress = "";

				s = "<br>";
				int len = (dots + " " + title + progress).length();
				s += getProgress("-", "..", len + 5, action.getStatusProvider().getCurrentStatusValueFine());
			}
			String line2 = "";
			if (action.getStatusProvider() != null && action.getStatusProvider().getCurrentStatusMessage1() != null
								&& action.getStatusProvider().getCurrentStatusMessage1().length() > 0)
				line2 = action.getStatusProvider().getCurrentStatusMessage1();
			if (statusServer != null) {
				BackgroundTaskStatusProvider status = action.getStatusProvider();
				String eta = statusServer.getRemainTime(status.getCurrentStatusValue() == -1,
									status.getCurrentStatusValueFine());
				if (eta.length() > 0) {
					if (line2.length() > 0)
						line2 += ", ";
					line2 += "" + eta;
				}
			}
			if (line2.length() > 0)
				line2 = "<br>&nbsp;" + line2 + "&nbsp;";
			dots = "<code>" + dots + "</code>";
			return "<html><small><b><center>" + dots + " " + title + progress + " " + dots + "" + s + line2;
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
		if (tooltipHint != null)
			return tooltipHint;
		else
			return action != null ? action.getDefaultTooltip() : null;
	}

	public void setProcessing(boolean b) {
		this.processing = b;
		this.processingStart = System.currentTimeMillis();
		statusServer = new ProgressStatusService();
	}

	public boolean isProcessing() {
		return processing;
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

	public void setIcon(ImageIcon i) {
		this.icon = i;
	}

	public ImageIcon getIcon() {
		if (action != null) {
			if (action.getImageIcon() != null)
				return action.getImageIcon();
		}
		return icon;
	}

	public boolean willProvideActions() {
		return action != null ? action.getProvidesActions() : false;
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

	public static void checkButtonTitle(final NavigationButton n, final JButton n1) {
		if (n1 == null)
			return;
		final ObjectRef rr = new ObjectRef();
		Runnable r = new Runnable() {
			public void run() {
				if (n.isProcessing() && n1.isVisible()) {
					n1.setText(n.getTitle());
					if (n1.getText().indexOf("Please wait") >= 0)
						BackgroundTaskHelper.executeLaterOnSwingTask(500, (Runnable) rr.getObject());
					else
						BackgroundTaskHelper.executeLaterOnSwingTask(100, (Runnable) rr.getObject());
				} else {
					if (n1.isVisible())
						n1.setText(n.getTitle());
				}
			}
		};
		rr.setObject(r);
		r.run();
	}

	public static void checkButtonTitle(final WeakReference<NavigationButton> wn, final WeakReference<JButton> wn1) {
		if (wn1 == null || wn == null)
			return;
		final ObjectRef rr = new ObjectRef();
		Runnable r = new Runnable() {
			public void run() {
				NavigationButton n = wn.get();
				JButton n1 = wn1.get();
				if (n1 == null || n == null)
					return;
				if (n.isProcessing() && n1.isVisible()) {
					n1.setText(n.getTitle());
					if (n1.getText().indexOf("Please wait") >= 0)
						BackgroundTaskHelper.executeLaterOnSwingTask(500, (Runnable) rr.getObject());
					else
						BackgroundTaskHelper.executeLaterOnSwingTask(100, (Runnable) rr.getObject());
				} else {
					if (n1.isVisible())
						n1.setText(n.getTitle());
					BackgroundTaskHelper.executeLaterOnSwingTask(100, (Runnable) rr.getObject());
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
			NavigationButton.checkButtonTitle(srcNavGraphicslEntity, n1);
			MyUtility.navigate(navPanel.getEntitySet(false), srcNavGraphicslEntity.getTitle());
			final NavigationAction na = srcNavGraphicslEntity.getAction();

			BackgroundTaskHelper.issueSimpleTask(srcNavGraphicslEntity.getTitle(), "Please wait...", new Runnable() {
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
				public void run() {
					try {
						boolean reload = false;
						MainPanelComponent mpc = na.getResultMainPanel();
						if (mpc != null) {
							graphPanel.setEnabled(false);
							graphPanel.setVisible(false);
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
								gui = TableLayout.getSplitVertical(TableLayout.getMultiSplitVertical(errors, 2), gui,
													TableLayout.PREFERRED, TableLayout.FILL);
							}
							graphPanel.add(gui, "0,0");
							graphPanel.validate();
							graphPanel.setEnabled(true);
							graphPanel.setVisible(true);
							graphPanel.repaint();
						} else {
							if (ErrorMsg.getErrorMsgCount() > 0) {
								graphPanel.setEnabled(false);
								graphPanel.setVisible(false);
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
								graphPanel.validate();
								graphPanel.setEnabled(true);
								graphPanel.setVisible(true);
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
								AIPgui.navigateTo(path, NavigationButton.this);
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
									AIPgui.navigateTo(path, NavigationButton.this);
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
						srcNavGraphicslEntity.getAction().getStatusProvider().setCurrentStatusValue(100);
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

		if (style == ButtonDrawStyle.COMPACT_LIST)
			if (target == PanelTarget.NAVIGATION)
				imgS = 25;// 32;
			else
				imgS = 48;

		ImageIcon icon;
		if (n.getIcon() != null) {
			icon = new ImageIcon(GravistoService.getScaledImage(n.getIcon().getImage(), -imgS, imgS));
		} else {
			if (target == PanelTarget.NAVIGATION)
				icon = GravistoService.loadIcon(AIPmain.class, n.getNavigationImage(), -imgS, imgS);
			else
				icon = GravistoService.loadIcon(AIPmain.class, n.getActionImage(), -imgS, imgS);
		}

		final JButton n1 = new JButton("" + n.getTitle());
		switch (style) {
			case FLAT:
			case COMPACT_LIST:
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

		n1.setToolTipText(n.getToolTip());
		n1.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		n1.addMouseListener(new MouseListener() {
			@Override
			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseExited(MouseEvent e) {
				if (n1 != null && n1.getText().startsWith("<html><u>"))
					n1.setText(n1.getText().substring("<html><u>".length()));
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				n1.setText("<html><u>" + n1.getText());
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				// TODO Auto-generated method stub

			}
		});

		if (n instanceof Calendar2) {
			if (style != ButtonDrawStyle.TEXT) {
				icon = new MyCalendarIcon(icon, (Calendar2) n, imgS);
				((Calendar2) n).setPostUpdateRunner(new Runnable() {
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

		if (n.isProcessing()) {
			NavigationButton.checkButtonTitle(n, n1);
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
		n.setExecution(new Runnable() {
			public void run() {
				n.executeNavigation(target, navPanel, actionPanel, graphPanel, n1, null);
			}
		});
		n1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				n.performAction();
			}
		});

		if (n.getToolTip() != null && n.getToolTip().length() > 0)
			n1.setToolTipText(n.getToolTip());

		if (n.getSideGui() != null)
			return TableLayout.get3Split(n1, null, n.getSideGui(), TableLayout.PREFERRED, n.getSideGuiSpace(),
								n.getSideGuiWidth());
		else
			return n1;
	}

	public GUIsetting getGUIsetting() {
		return guiSetting;
	}
}
