/*******************************************************************************
 * Copyright (c) 2003-2009 Plant Bioinformatics Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Apr 27, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.gui.webstart;

import info.clearthought.layout.TableLayout;

import java.awt.FlowLayout;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import netscape.javascript.JSObject;

import org.ErrorMsg;
import org.ReleaseInfo;

import de.ipk.ag_ba.commands.ActionIapHome;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.MyNavigationPanel;
import de.ipk.ag_ba.gui.PanelTarget;
import de.ipk.ag_ba.gui.navigation_model.GUIsetting;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.FlowLayoutImproved;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProviderSupportingExternalCallImpl;

/**
 * @author klukas
 */
public class IAPgui {
	
	public static JComponent getNavigation(final BackgroundTaskStatusProviderSupportingExternalCallImpl myStatus,
			boolean secondWindow) {
		
		final JPanel graphPanel = new JPanel();
		
		graphPanel.setBackground(MyNavigationPanel.getTabColor());
		graphPanel.setOpaque(true);
		graphPanel.setLayout(TableLayout.getLayout(TableLayout.FILL, TableLayout.FILL));
		
		int vgap = 5;
		int hgap = 10;
		
		final MyNavigationPanel navigationPanel = new MyNavigationPanel(PanelTarget.NAVIGATION, graphPanel, null);
		navigationPanel.setOpaque(false);
		navigationPanel.setLayout(new FlowLayoutImproved(FlowLayout.LEFT, hgap, vgap));
		
		JPanel actionPanelRight = new JPanel();
		final MyNavigationPanel actionPanel = new MyNavigationPanel(PanelTarget.ACTION, graphPanel, actionPanelRight);
		actionPanel.setOpaque(false);
		actionPanel.setLayout(new FlowLayoutImproved(FlowLayout.LEFT, hgap, vgap));
		
		navigationPanel.setTheOther(actionPanel);
		actionPanel.setTheOther(navigationPanel);
		
		ActionIapHome home = new ActionIapHome(myStatus);
		GUIsetting guiSetting = new GUIsetting(navigationPanel, actionPanel, graphPanel);
		
		navigationPanel.setGuiSetting(guiSetting);
		actionPanel.setGuiSetting(guiSetting);
		
		final NavigationButton overView = new NavigationButton(home, guiSetting);
		
		overView.setTitle("Initialize");
		overView.setProcessing(true);
		
		ArrayList<NavigationButton> homeNavigation = new ArrayList<NavigationButton>();
		home.performActionCalculateResults(overView);
		navigationPanel.setEntitySet(home.getResultNewNavigationSet(homeNavigation));
		actionPanel.setEntitySet(home.getActionEntitySet());
		
		ErrorMsg.addOnAppLoadingFinishedAction(new Runnable() {
			// ErrorMsg.addOnAddonLoadingFinishedAction(new Runnable() {
			@Override
			public void run() {
				overView.setTitle("Overview");
				overView.setProcessing(false);
				try {
					JSObject win = JSObject.getWindow(ReleaseInfo.getApplet());
					Object o = win.eval("s = window.location.hash;");
					String h = o + "";
					h = URLDecoder.decode(h, "UTF-8");
					System.out.println("HASH: " + h);
					navigateTo(h, navigationPanel, actionPanel, graphPanel);
				} catch (Exception e) {
					// System.out.println("JavaScript and Browser window not accessible.");
					// navigateTo("Overview.MetaCrop.Carbohydrate Metabolism.Ascorbate biosynthesis",
					// navigationPanel, actionPanel, graphPanel, knownEntities);
					// navigateTo("Overview.DBE Database.User Login.AG PBI.klukas.AAT-Juniproben2004 (test)",
					// navigationPanel, actionPanel, graphPanel, knownEntities);
				}
			}
		});
		
		// JScrollPane navScroller = new MyScrollPane(navigationPanel, false);
		// navScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		// navigationPanel.setScrollpane(navScroller);
		
		// JScrollPane actionScroller = new MyScrollPane(actionPanel, false);
		// actionScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		// actionPanel.setScrollpane(actionScroller);
		
		JLabel lbl;
		if (!secondWindow && IAPmain.myClassKnown) {
			lbl = new JLabel("<html><h2><font color='red'>IAP Reloading Not Supported!</font></h2>"
					+ "<b>It is recommended to close any browser window and re-open this website,<br>"
					+ "otherwise this information system may not work reliable.</b><br><br>"
					+ "Technical background: reloading this applet is not yet supported");
		} else {
			lbl = new JLabel(getIntroTxt());
		}
		lbl.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
		graphPanel.add(new MainPanelComponent(lbl.getText()).getGUI(), "0,0");
		graphPanel.validate();
		
		JComponent res = TableLayout.get3SplitVertical(navigationPanel, TableLayout.getSplit(actionPanel,
				actionPanelRight, TableLayout.FILL, TableLayout.PREFERRED), graphPanel, TableLayout.PREFERRED,
				TableLayout.PREFERRED, TableLayout.FILL);
		
		navigateTo("IAP", navigationPanel, actionPanel, graphPanel);
		
		return res;
	}
	
	public static String getIntroTxt() {
		return "<html><h2>Welcome to IAP - the Integrated Analysis Platform!</h2>"
				+ "The Integrated Analysis Platform IAP is a systems biology cloud storage, analysis and visualization system, "
				+ "developed by the IPK research group Image Analysis.<br>"
				+ "<br>"
				+ "This information system is in alpha-stage. "
				+ "It may not work reliable, only a small subset of future functions are implemented.<br>"
				+ "<br>"
				+ "If you have any questions, don't hesitate to contact the group Image Analysis:<br>"
				+ "Dr. Christian Klukas, Tel. 763, <a href=\"mailto:klukas@ipk-gatersleben.de\">klukas@ipk-gatersleben.de</a>.<br><br>" +
				new LogService().getLatestNews(5,
						"<br>" +
								"<p>Latest changes:<br><br><ul>",
						"<li>", "", "<br><br>");
	}
	
	public static void navigateTo(final String target, NavigationButton src) {
		System.out.println("NAVIGATE: " + target);
		if (src == null)
			System.out.println("ERRR");
		if (src.getGUIsetting() == null)
			System.out.println("ERRRRRR");
		if (src.getGUIsetting().getNavigationPanel() == null)
			System.out.println("ERRRRRRRRRRR");
		NavigationButton button = src.getGUIsetting().getNavigationPanel().getEntitySet(false).iterator().next();
		
		final MyNavigationPanel navigationPanel = src.getGUIsetting().getNavigationPanel();
		final MyNavigationPanel actionPanel = src.getGUIsetting().getActionPanel();
		final JComponent graphPanel = src.getGUIsetting().getGraphPanel();
		
		Runnable rrr = new Runnable() {
			@Override
			public void run() {
				navigateTo(target, navigationPanel, actionPanel, graphPanel);
			}
		};
		button.executeNavigation(PanelTarget.ACTION, navigationPanel, actionPanel, graphPanel, null, rrr);
	}
	
	private static void navigateTo(String target, final MyNavigationPanel navigationPanel,
			final MyNavigationPanel actionPanel, final JComponent graphPanel) {
		
		if (target == null || target.length() == 0)
			return;
		if (target.startsWith("#"))
			target = target.substring("#".length());
		
		if (target.startsWith("Overview"))
			target = target.substring("Overview".length());
		if (target.startsWith("IAP"))
			target = target.substring("IAP".length());
		if (target.startsWith("."))
			target = target.substring(".".length());
		
		HashMap<String, NavigationButton> knownEntities = new HashMap<String, NavigationButton>();
		
		for (NavigationButton ne : actionPanel.getEntitySet(target.length() > 0)) {
			knownEntities.put(ne.getTitle(), ne);
			if (ne.getTitle().contains("(")) {
				knownEntities.put(ne.getTitle().substring(0, ne.getTitle().lastIndexOf("(")).trim(), ne);
			}
		}
		String thisTarget = target.split("\\.", 2)[0];
		if (thisTarget != null && thisTarget.length() > 0) {
			final String nextTarget = target.length() - thisTarget.length() > 1 ? target.substring(thisTarget.length()
					+ ".".length()) : "";
			NavigationButton button = knownEntities.get(thisTarget);
			if (button == null && thisTarget.contains("(")) {
				button = knownEntities.get(thisTarget.substring(0, thisTarget.lastIndexOf("(")).trim());
			}
			if (button != null) {
				Runnable rrr = new Runnable() {
					@Override
					public void run() {
						if (nextTarget.length() > 0) {
							navigateTo(nextTarget, navigationPanel, actionPanel, graphPanel);
						}
					}
				};
				button.executeNavigation(PanelTarget.ACTION, navigationPanel, actionPanel, graphPanel, null, rrr);
			} else {
				System.out.println("WARNING: Could not find target action: " + thisTarget);
				navigationPanel.getEntitySet(false).iterator().next().executeNavigation(
						PanelTarget.ACTION, navigationPanel, actionPanel, graphPanel, null, null);
			}
		}
	}
}
