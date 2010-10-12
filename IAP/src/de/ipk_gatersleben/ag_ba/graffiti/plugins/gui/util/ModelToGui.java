/*******************************************************************************
 * 
 *    Copyright (c) 2003-2009 Plant Bioinformatics Group, IPK Gatersleben
 * 
 *******************************************************************************/
/*
 * Created on May 5, 2010 by Christian Klukas
 */
package de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.util;

import info.clearthought.layout.TableLayout;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.ErrorMsg;
import org.ObjectRef;
import org.StringManipulationTools;
import org.graffiti.editor.GravistoService;

import de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.MainPanelComponent;
import de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.MyNavigationPanel;
import de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.PanelTarget;
import de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.calendar.MyCalendarIcon;
import de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.enums.ButtonDrawStyle;
import de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.interfaces.NavigationAction;
import de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.navigation_actions.Calendar2;
import de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.navigation_model.NavigationGraphicalEntity;
import de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.webstart.AIPmain;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;

/**
 * @author klukas
 * 
 */
public class ModelToGui {

	public static JComponent getNavigationButton(ButtonDrawStyle style, final NavigationGraphicalEntity n,
			final PanelTarget target, final MyNavigationPanel navPanel, final MyNavigationPanel actionPanel,
			final JComponent graphPanel) {

		if (n.getGUI() != null)
			return n.getGUI();

		int imgS = 48;

		if (style == ButtonDrawStyle.COMPACT_LIST)
			imgS = 32;

		ImageIcon icon;
		if (n.getIcon() != null) {
			icon = n.getIcon();
		} else {
			if (target == PanelTarget.NAVIGATION)
				icon = GravistoService.loadIcon(AIPmain.class, n.getNavigationImage(), -imgS, imgS);
			else
				icon = GravistoService.loadIcon(AIPmain.class, n.getActionImage(), -imgS, imgS);
		}

		final JButton n1 = new JButton("" + n.getTitle());
		switch (style) {
		case FLAT:
			n1.setBorderPainted(false);
			n1.setContentAreaFilled(false);
			if (target == PanelTarget.NAVIGATION)
				n1.setBorder(BorderFactory.createEmptyBorder(8, 4, 2, 4));
			else
				n1.setBorder(BorderFactory.createEmptyBorder(8, 8, 2, 8));
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
				if (n1.getText().startsWith("<html><u>"))
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
			checkButtonTitle(n, n1);
		}

		n1.setOpaque(false);

		if (style != ButtonDrawStyle.COMPACT_LIST) {
			n1.setVerticalTextPosition(SwingConstants.BOTTOM);
			n1.setHorizontalTextPosition(SwingConstants.CENTER);
		}
		n.setExecution(new Runnable() {
			public void run() {
				executeNavigation(n, target, navPanel, actionPanel, graphPanel, n1, null);
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
			return TableLayout.get3Split(n1, null, n.getSideGui(), TableLayout.PREFERRED, n.getSideGuiSpace(), n
					.getSideGuiWidth());
		else
			return n1;
	}

	private static void checkButtonTitle(final NavigationGraphicalEntity n, final JButton n1) {
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

	public static void executeNavigation(final NavigationGraphicalEntity srcNavGraphicslEntity,
			final PanelTarget target, final MyNavigationPanel navPanel, final MyNavigationPanel actionPanel,
			final JComponent graphPanel, final JButton n1, final Runnable optFinishAction) {
		executeNavigation(srcNavGraphicslEntity, target, navPanel, actionPanel, graphPanel, n1, optFinishAction, false);
	}

	public static void executeNavigation(final NavigationGraphicalEntity srcNavGraphicslEntity,
			final PanelTarget target, final MyNavigationPanel navPanel, final MyNavigationPanel actionPanel,
			final JComponent graphPanel, final JButton n1, final Runnable optFinishAction, final boolean recursive) {

		if (n1 != null && srcNavGraphicslEntity.isProcessing()) {
			if (n1.getText().equalsIgnoreCase("Please wait"))
				n1.setText("Please wait!");
			else
				n1.setText("Please wait");
			return;
		}

		srcNavGraphicslEntity.getAction().getStatusProvider().setCurrentStatusValue(-1);
		srcNavGraphicslEntity.setProcessing(true);
		checkButtonTitle(srcNavGraphicslEntity, n1);
		MyUtility.navigate(navPanel.getEntitySet(), srcNavGraphicslEntity.getTitle());
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
										+ StringManipulationTools.removeHTMLtags(s.replaceAll("<br>", "_br_")).replaceAll("_br_",
												"<br>").replaceAll("\n", "<br>"));
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
										+ StringManipulationTools.removeHTMLtags(s.replaceAll("<br>", "_br_")).replaceAll("_br_",
												"<br>").replaceAll("\n", "<br>"));
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
						ArrayList<NavigationGraphicalEntity> set = new ArrayList<NavigationGraphicalEntity>();
						for (NavigationGraphicalEntity ne : navPanel.getEntitySet()) {
							set.add(ne);
							if (ne == srcNavGraphicslEntity)
								break;
						}
						navPanel.setEntitySet(set);
					} else {
						ArrayList<NavigationGraphicalEntity> set = na.getResultNewNavigationSet(navPanel.getEntitySet());
						boolean execute = false;
						NavigationGraphicalEntity del = null;
						if (set != null)
							for (final NavigationGraphicalEntity src : set) {
								if (execute) {
									del = src;
									SwingUtilities.invokeLater(new Runnable() {
										@Override
										public void run() {
											executeNavigation(src, target, navPanel, actionPanel, graphPanel, n1, null, true);
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

					ArrayList<NavigationGraphicalEntity> actions = na.getResultNewActionSet();
					if (actions != null && na.getAdditionalEntities() != null)
						actions.addAll(na.getAdditionalEntities());
					actionPanel.setEntitySet(actions);

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
