/*******************************************************************************
 * 
 *    Copyright (c) 2003-2009 Plant Bioinformatics Group, IPK Gatersleben
 * 
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

import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.MyNavigationPanel;
import de.ipk.ag_ba.gui.PanelTarget;
import de.ipk.ag_ba.gui.navigation_actions.HomeAction;
import de.ipk.ag_ba.gui.navigation_model.NavigationGraphicalEntity;
import de.ipk.ag_ba.gui.util.FlowLayoutImproved;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProviderSupportingExternalCallImpl;

/**
 * @author klukas
 * 
 */
public class AIPgui {

	public static JComponent getNavigation(final BackgroundTaskStatusProviderSupportingExternalCallImpl myStatus,
			boolean secondWindow) {

		final JPanel graphPanel = new JPanel();

		graphPanel.setBackground(MyNavigationPanel.getTabColor());
		graphPanel.setOpaque(true);
		graphPanel.setLayout(TableLayout.getLayout(TableLayout.FILL, TableLayout.FILL));

		JLabel lbl;
		if (!secondWindow && AIPmain.myClassKnown) {
			lbl = new JLabel("<html><h2><font color='red'>IAP Reloading Not Supported!</font></h2>"
					+ "<b>It is recommended to close any browser window and re-open this website,<br>"
					+ "otherwise this information system may not work reliable.</b><br><br>"
					+ "Technical background: reloading this applet is not yet supported");
		} else {
			lbl = new JLabel(getIntroTxt());
		}
		lbl.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
		graphPanel.add(new MainPanelComponent(lbl.getText()).getGUI(), "0,0");
		// graphPanel.add(lbl, "0,0");
		graphPanel.validate();

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

		HomeAction home = new HomeAction(myStatus);
		final NavigationGraphicalEntity overView = new NavigationGraphicalEntity(home);

		overView.setTitle("Initialize");
		overView.setProcessing(true);

		ArrayList<NavigationGraphicalEntity> homeNavigation = new ArrayList<NavigationGraphicalEntity>();
		home.performActionCalculateResults(overView);
		navigationPanel.setEntitySet(home.getResultNewNavigationSet(homeNavigation));
		actionPanel.setEntitySet(home.getActionEntitySet());

		ErrorMsg.addOnAppLoadingFinishedAction(new Runnable() {
			// ErrorMsg.addOnAddonLoadingFinishedAction(new Runnable() {
			public void run() {
				overView.setTitle("Overview");
				overView.setProcessing(false);
				HashMap<String, NavigationGraphicalEntity> knownEntities = new HashMap<String, NavigationGraphicalEntity>();
				try {
					JSObject win = JSObject.getWindow(ReleaseInfo.getApplet());
					Object o = win.eval("s = window.location.hash;");
					String h = o + "";
					h = URLDecoder.decode(h, "UTF-8");
					System.out.println("HASH: " + h);
					navigateTo(h, navigationPanel, actionPanel, graphPanel, knownEntities);
				} catch (Exception e) {
					System.out.println("JavaScript and Browser window not accessible.");

					// navigateTo("Overview.MetaCrop.Carbohydrate Metabolism.Ascorbate biosynthesis",
					// navigationPanel, actionPanel, graphPanel, knownEntities);
					// navigateTo("Overview.DBE Database.User Login.AG PBI.klukas.AAT-Juniproben2004 (test)",
					// navigationPanel, actionPanel, graphPanel, knownEntities);
				}
			}
		});

		return TableLayout.get3SplitVertical(navigationPanel, TableLayout.getSplit(actionPanel, actionPanelRight,
				TableLayout.FILL, TableLayout.PREFERRED), graphPanel, TableLayout.PREFERRED, TableLayout.PREFERRED,
				TableLayout.FILL);
	}

	public static String getIntroTxt() {
		return "<html><h2>Welcome to IAP - the Integrated Analysis Platform!</h2>"
				+ "The Integrated Analysis Platform IAP is a systems biology cloud storage, analysis and visualization system, "
				+ "developed by the IPK research group Image Analysis.<br>"
				+ "Additionally, it provides access to various bioinformatics ressources, "
				+ "developed at the IPK. The included data sources and tools have been "
				+ "mainly developed by members of the group Plant Bioinformatics and Image Analysis, "
				+ "partly with contributions from the group Bioinformatics and Information Technology. To get details about the included data sources and information systems, click the included Website- and Reference-Links.<br>"
				+ "<br>"
				+ "This information system is in alpha-stage not meant for productive work. "
				+ "It may not work reliable, only a small subset of future functions are implemented.<br>"
				+ "<br>"
				+ "If you have any questions, don't hesitate to contact the group Image Analysis:<br>"
				+ "Dr Christian Klukas, Tel. 763, <a href=\"mailto:klukas@ipk-gatersleben.de\">klukas@ipk-gatersleben.de</a><br>"
				+ "<br><small>"
				+ "Hints: Before working with the system, please wait until the label of the Overview button "
				+ "stops to spin. It is possible to copy or bookmark the URL from the browser window "
				+ "in order to be able to quickly navigate again to the same information in the future.";
	}

	public static void navigateTo(String target, NavigationGraphicalEntity src) {
		HashMap<String, NavigationGraphicalEntity> knownEntities = new HashMap<String, NavigationGraphicalEntity>();
		MyNavigationPanel navigationPanel = null;
		MyNavigationPanel actionPanel = null;
		JComponent graphPanel = null;
		navigateTo(target, navigationPanel, actionPanel, graphPanel, knownEntities);
	}

	public static void navigateTo(String target, final MyNavigationPanel navigationPanel,
			final MyNavigationPanel actionPanel, final JComponent graphPanel,
			final HashMap<String, NavigationGraphicalEntity> knownEntities) {

		if (target == null || target.length() == 0)
			return;
		if (target.startsWith("#"))
			target = target.substring("#".length());

		if (target.startsWith("Overview"))
			target = target.substring("Overview".length());
		if (target.startsWith("."))
			target = target.substring(".".length());

		for (NavigationGraphicalEntity ne : actionPanel.getEntitySet(target.length() > 0)) {
			knownEntities.put(ne.getTitle(), ne);
		}
		String thisTarget = target.split("\\.", 2)[0];
		final String nextTarget = target.length() - thisTarget.length() > 1 ? target.substring(thisTarget.length()
				+ ".".length()) : "";
		NavigationGraphicalEntity action = knownEntities.get(thisTarget);
		if (action != null) {
			Runnable rrr = new Runnable() {
				@Override
				public void run() {
					if (nextTarget.length() > 0) {
						navigateTo(nextTarget, navigationPanel, actionPanel, graphPanel, knownEntities);
					}
				}
			};
			action.getAction().setOneTimeFinishAction(rrr);
			action.executeNavigation(PanelTarget.ACTION, navigationPanel, actionPanel, graphPanel, null, rrr);
		}
	}
}
