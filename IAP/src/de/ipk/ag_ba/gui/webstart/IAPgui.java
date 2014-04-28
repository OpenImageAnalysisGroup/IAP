/*******************************************************************************
 * Copyright (c) 2003-2009 Plant Bioinformatics Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Apr 27, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.gui.webstart;

import info.clearthought.layout.TableLayout;

import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.ErrorMsg;
import org.ReleaseInfo;
import org.StringManipulationTools;

import de.ipk.ag_ba.commands.ActionHome;
import de.ipk.ag_ba.gui.IAPnavigationPanel;
import de.ipk.ag_ba.gui.IAPoptions;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.PanelTarget;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.navigation_model.GUIsetting;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.FlowLayoutImproved;
import de.ipk.ag_ba.plugins.vanted_vfs.NavigationButtonFilter;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProviderSupportingExternalCallImpl;

/**
 * @author klukas
 */
public class IAPgui {
	
	public static JComponent getMainGUIcontent(
			final BackgroundTaskStatusProviderSupportingExternalCallImpl myStatus,
			boolean secondWindow,
			NavigationAction optCustomHomeAction) {
		return getMainGUIcontent(myStatus, secondWindow, optCustomHomeAction, null);
	}
	
	public static JComponent getMainGUIcontent(
			final BackgroundTaskStatusProviderSupportingExternalCallImpl myStatus,
			boolean secondWindow,
			NavigationAction optCustomHomeAction,
			NavigationButtonFilter optNavigationButtonFilter) {
		
		final JPanel graphPanel = new JPanel();
		
		graphPanel.setBackground(IAPnavigationPanel.getTabColor());
		graphPanel.setOpaque(true);
		graphPanel.setLayout(TableLayout.getLayout(TableLayout.FILL, TableLayout.FILL));
		
		int vgap = 5;
		int hgap = 10;
		
		final IAPnavigationPanel navigationPanel = new IAPnavigationPanel(PanelTarget.NAVIGATION, graphPanel, null);
		navigationPanel.setOpaque(false);
		navigationPanel.setLayout(new FlowLayoutImproved(FlowLayout.LEFT, hgap, vgap));
		
		JPanel actionPanelRight = new JPanel();
		final IAPnavigationPanel actionPanel = new IAPnavigationPanel(PanelTarget.ACTION, graphPanel, actionPanelRight);
		actionPanel.setNavigationButtonFilter(optNavigationButtonFilter);
		actionPanel.setOpaque(false);
		actionPanel.setLayout(new FlowLayoutImproved(FlowLayout.LEFT, hgap, vgap));
		
		navigationPanel.setTheOther(actionPanel);
		actionPanel.setTheOther(navigationPanel);
		
		final NavigationAction home = optCustomHomeAction != null ? optCustomHomeAction : new ActionHome(myStatus);
		GUIsetting guiSetting = new GUIsetting(navigationPanel, actionPanel, graphPanel);
		
		navigationPanel.setGuiSetting(guiSetting);
		actionPanel.setGuiSetting(guiSetting);
		
		final NavigationButton overView = new NavigationButton(home, guiSetting);
		
		// overView.setTitle("Initialize");
		// overView.setProcessing(true);
		
		ErrorMsg.addOnAppLoadingFinishedAction(new Runnable() {
			// ErrorMsg.addOnAddonLoadingFinishedAction(new Runnable() {
			@Override
			public void run() {
				// overView.setTitle("Overview");
				// overView.setProcessing(false);
				// try {
				// JSObject win = JSObject.getWindow(ReleaseInfo.getApplet());
				// Object o = win.eval("s = window.location.hash;");
				// String h = o + "";
				// h = URLDecoder.decode(h, "UTF-8");
				// System.out.println("HASH: " + h);
				// navigateTo(h, navigationPanel, actionPanel, graphPanel);
				// } catch (Exception e) {
				// // System.out.println("JavaScript and Browser window not accessible.");
				// // navigateTo("Overview.MetaCrop.Carbohydrate Metabolism.Ascorbate biosynthesis",
				// // navigationPanel, actionPanel, graphPanel, knownEntities);
				// // navigateTo("Overview.DBE Database.User Login.AG PBI.klukas.AAT-Juniproben2004 (test)",
				// // navigationPanel, actionPanel, graphPanel, knownEntities);
				// }
			}
		});
		
		// JScrollPane navScroller = new MyScrollPane(navigationPanel, false);
		// navScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		// navigationPanel.setScrollpane(navScroller);
		
		// JScrollPane actionScroller = new MyScrollPane(actionPanel, false);
		// actionScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		// actionPanel.setScrollpane(actionScroller);
		
		final ArrayList<NavigationButton> homeNavigation = new ArrayList<NavigationButton>();
		try {
			home.performActionCalculateResults(overView);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		navigationPanel.setEntitySet(home.getResultNewNavigationSet(homeNavigation));
		actionPanel.setEntitySet(home.getResultNewActionSet());
		
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
		graphPanel.revalidate();
		
		JComponent res = TableLayout.get3SplitVertical(navigationPanel, TableLayout.getSplit(actionPanel,
				actionPanelRight, TableLayout.FILL, TableLayout.PREFERRED), graphPanel, TableLayout.PREFERRED,
				TableLayout.PREFERRED, TableLayout.FILL);
		
		// navigateTo("IAP", navigationPanel, actionPanel, graphPanel);
		res.revalidate();
		return res;
	}
	
	public static String getIntroTxt() {
		try {
			return "<html><h2><font face='Arial'>Welcome to IAP - the Integrated Analysis Platform! <small>V" + ReleaseInfo.IAP_VERSION_STRING
					+ "</small></font></h2>"
					+ "<font face='Arial'>The Integrated Analysis Platform IAP is a systems biology cloud storage, analysis and visualization system. "
					+ "It is focused on high-throughput plant phenotyping and developed by the IPK research group 'image analysis'.<br>"
					+ "<br>"
					+ "You find information on how to use this software and additional reference information by clicking the command button 'About'.<br>"
					+ "Use the first row of buttons to go back to any previously selected command or to return to this 'Start'-screen." +
					new LogService().getLatestNews(IAPoptions.getInstance().getInteger("NEWS", "show_n_items", 0),
							"<br>" +
									"<p>Latest system messages:<br><br><ul>",
							"<li>", "", "<br><br>") + "</font>";
		} catch (Exception e) {
			e.printStackTrace();
			return "<html><h2>Exception while getting intro-text: " + e.getMessage() + "</h2>";
		}
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
		
		final IAPnavigationPanel navigationPanel = src.getGUIsetting().getNavigationPanel();
		final IAPnavigationPanel actionPanel = src.getGUIsetting().getActionPanel();
		final JComponent graphPanel = src.getGUIsetting().getGraphPanel();
		
		Runnable rrr = new Runnable() {
			@Override
			public void run() {
				navigateTo(target, navigationPanel, actionPanel, graphPanel);
			}
		};
		button.executeNavigation(PanelTarget.ACTION, navigationPanel, actionPanel, graphPanel, null, rrr, null);
	}
	
	private static void navigateTo(String target, final IAPnavigationPanel navigationPanel,
			final IAPnavigationPanel actionPanel, final JComponent graphPanel) {
		
		if (target == null || target.length() == 0)
			return;
		if (target.startsWith("#"))
			target = target.substring("#".length());
		
		if (target.startsWith("Overview"))
			target = target.substring("Overview".length());
		if (target.startsWith("IAP"))
			target = target.substring("IAP".length());
		if (target.startsWith("Start"))
			target = target.substring("Start".length());
		if (target.startsWith("."))
			target = target.substring(".".length());
		
		HashMap<String, NavigationButton> knownEntities = new HashMap<String, NavigationButton>();
		
		for (NavigationButton ne : actionPanel.getEntitySet(target.length() > 0)) {
			knownEntities.put(IAPnavigationPanel.replaceBadChars(ne.getTitle()), ne);
			if (ne.getTitle().contains("(")) {
				String t = IAPnavigationPanel.replaceBadChars(ne.getTitle().substring(0, ne.getTitle().lastIndexOf("(")).trim());
				System.out.println(t);
				knownEntities.put(t, ne);
			}
		}
		String thisTarget = target.split("\\.", 2)[0];
		if (thisTarget != null && thisTarget.length() > 0) {
			String nextTarget = target.length() - thisTarget.length() > 1 ? target.substring(thisTarget.length()
					+ ".".length()) : "";
			// nextTarget = IAPnavigationPanel.replaceBadChars(nextTarget);
			NavigationButton button = knownEntities.get(thisTarget);
			if (button == null && (thisTarget.contains("_") || thisTarget.contains("("))) {
				String tt = thisTarget.substring(0, thisTarget.indexOf("(")).trim();
				button = knownEntities.get(tt);
				tt = StringManipulationTools.stringReplace(thisTarget.substring(0, thisTarget.lastIndexOf("(")).trim(), "_", ".");
				if (button == null)
					button = knownEntities.get(tt);
			}
			
			if (button == null)
				System.out.println("Upcoming problem...");
			if (button != null) {
				final String nt = nextTarget;
				Runnable rrr = new Runnable() {
					@Override
					public void run() {
						if (nt.length() > 0) {
							navigateTo(nt, navigationPanel, actionPanel, graphPanel);
						}
					}
				};
				button.executeNavigation(PanelTarget.ACTION, navigationPanel, actionPanel, graphPanel, null, rrr, null);
			} else {
				System.out.println("WARNING: Could not find target action: " + thisTarget);
				// navigationPanel.getEntitySet(false).iterator().next().executeNavigation(
				// PanelTarget.ACTION, navigationPanel, actionPanel, graphPanel, null, null);
			}
		}
	}
}
