/*******************************************************************************
 * 
 *    Copyright (c) 2003-2009 Plant Bioinformatics Group, IPK Gatersleben
 * 
 *******************************************************************************/
/*
 * Created on Apr 27, 2010 by Christian Klukas
 */

package de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.webstart;

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

import de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.MainPanelComponent;
import de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.MyNavigationPanel;
import de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.PanelTarget;
import de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.nav.RimasNav;
import de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.navigation_actions.Home;
import de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.navigation_actions.Other;
import de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.navigation_actions.Phenotyping;
import de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.navigation_actions.ShowVANTED;
import de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.navigation_model.NavigationGraphicalEntity;
import de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.util.FlowLayoutImproved;
import de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.util.ModelToGui;
import de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.util.WebFolder;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProviderSupportingExternalCallImpl;

/**
 * @author klukas
 * 
 */
public class AIPgui {

	private static MyNavigationPanel globalNavigationPanel;

	/**
	 * @param myStatus
	 * @param jtp
	 * @return
	 */
	public static JComponent getNavigation(final BackgroundTaskStatusProviderSupportingExternalCallImpl myStatus) {

		final JPanel graphPanel = new JPanel();

		graphPanel.setBackground(MyNavigationPanel.getTabColor());
		graphPanel.setOpaque(true);
		graphPanel.setLayout(TableLayout.getLayout(TableLayout.FILL, TableLayout.FILL));

		JLabel lbl;
		if (AIPmain.myClassKnown) {
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
		AIPgui.globalNavigationPanel = navigationPanel;
		navigationPanel.setOpaque(false);
		navigationPanel.setLayout(new FlowLayoutImproved(FlowLayout.LEFT, hgap, vgap));

		JPanel actionPanelRight = new JPanel();
		final MyNavigationPanel actionPanel = new MyNavigationPanel(PanelTarget.ACTION, graphPanel, actionPanelRight);
		actionPanel.setOpaque(false);
		actionPanel.setLayout(new FlowLayoutImproved(FlowLayout.LEFT, hgap, vgap));

		navigationPanel.setTheOther(actionPanel);
		actionPanel.setTheOther(navigationPanel);

		final ArrayList<NavigationGraphicalEntity> homeNavigation = new ArrayList<NavigationGraphicalEntity>();

		final ArrayList<NavigationGraphicalEntity> homeActions = new ArrayList<NavigationGraphicalEntity>();

		final NavigationGraphicalEntity overView = new NavigationGraphicalEntity(new Home(homeActions, homeNavigation,
				myStatus), "Initialize", "img/pattern_graffiti_logo.png");

		homeNavigation.add(overView);

		homeActions.add(new NavigationGraphicalEntity(new Phenotyping(), "Phenotyping", "img/000Grad_3.png"));

		// homeActions.add(new NavigationGraphicalEntity(new DBElogin2(),
		// "DBE Database", "img/dbelogo2.png"));

		NavigationGraphicalEntity rimas = RimasNav.getRimas();
		homeActions.add(rimas);

		NavigationGraphicalEntity metaCrop = WebFolder
				.getBrowserNavigationEntity(
						null,
						"MetaCrop",
						"img/metacrop.png",
						"http://pgrc-16.ipk-gatersleben.de/wgrp/nwg/metacrop/",
						"Website",
						"img/browser.png",
						"http://metacrop.ipk-gatersleben.de",
						new String[] { ".gml", ".graphml" },
						""
								+ "<h2>MetaCrop</h2>"
								+ "MetaCrop is a web accessible database that summarizes diverse information about metabolic pathways "
								+ "in crop plants and allows automatic export of information for the creation of detailed metabolic models.<br><br>"
								+ "IAP as well as VANTED provide access to the exported MetaCrop pathways in a graphical and interactive way.<br>"
								+ "For background information and further information please visit the MetaCrop website, accessible by using the "
								+ "Website button, shown above.");
		homeActions.add(metaCrop);

		HashMap<String, String> folder2url = new HashMap<String, String>();
		folder2url.put("", "SBGN Spec.:http://www.nature.com/nbt/journal/v27/n8/full/nbt.1558.html");
		folder2url.put("Activity Flow", "Nat. Proc. (AF):http://precedings.nature.com/documents/3724/version/1");
		folder2url.put("Entity Relationship", "Nat. Proc. (ER):http://precedings.nature.com/documents/3724/version/1");
		folder2url.put("Process Description", "Nat. Proc. (PD):http://precedings.nature.com/documents/3724/version/1");
		NavigationGraphicalEntity sbgn = WebFolder
				.getBrowserNavigationEntity(
						folder2url,
						"SBGN-ED",
						"img/sbgn.png",
						"http://vanted.ipk-gatersleben.de/aip/sbgn-examples/",
						"SBGN-ED",
						"img/browser.png",
						"http://vanted.ipk-gatersleben.de/addons/sbgn-ed/",
						new String[] { ".gml", ".graphml" },
						"<h2>SBGN-ED - Editing, Translating and Validating of SBGN Maps</h2>"
								+ ""
								+ "SBGN-ED is a VANTED Add-on which allows to create and edit all three types of SBGN maps, "
								+ "that is Process Description, Entity Relationship and Activity Flow, to validate these "
								+ "maps according to the SBGN specifications, to translate maps from the KEGG and MetaCrop "
								+ "pathway databases into SBGN, and to export SBGN maps into several file and image formats.<br><br>"
								+ "SBGN-ED editing, translation and validation functions are available from within VANTED and IAP as "
								+ "soon as the SBGN-ED Add-on available from the mentioned website is downloaded and installed. "
								+ "The SBGN-ED website additionally contains documentation and additional background information.");
		homeActions.add(sbgn);

		NavigationGraphicalEntity examples = WebFolder
				.getBrowserNavigationEntity(
						null,
						"VANTED",
						// "img/vanted_examples.png",
						"img/vanted1_0.png",
						"http://vanted.ipk-gatersleben.de/examplefiles/",
						"Website",
						"img/browser.png",
						"http://vanted.ipk-gatersleben.de/",
						new String[] { ".gml", ".graphml" },
						"<h2>Welcome to VANTED - Visualization and Analysis of Networks containing Experimental Data</h2>"
								+ "This system makes it possible to load and edit graphs, which may represent biological pathways or functional hierarchies. "
								+ "It is possible to map experimental datasets onto the graph elements and visualize time series data or data of different "
								+ "genotypes or environmental conditions in the context of a the underlying biological processes. Built-in statistic "
								+ "functions allow a fast evaluation of the data (e.g. t-Test or correlation analysis).");

		NavigationGraphicalEntity startVanted = new NavigationGraphicalEntity(new ShowVANTED());

		examples.getAction().addAdditionalEntity(startVanted);

		homeActions.add(examples);

		NavigationGraphicalEntity serverStatusEntity = Other.getServerStatusEntity(true);
		homeActions.add(serverStatusEntity);

		for (NavigationGraphicalEntity ne : homeNavigation)
			ne.setProcessing(true);
		// for (NavigationEntity ne : homeActions)
		// ne.setProcessing(true);

		navigationPanel.setEntitySet(homeNavigation);
		actionPanel.setEntitySet(homeActions);

		ErrorMsg.addOnAppLoadingFinishedAction(new Runnable() {
			// ErrorMsg.addOnAddonLoadingFinishedAction(new Runnable() {
			public void run() {
				overView.setTitle("Overview");
				for (NavigationGraphicalEntity ne : homeNavigation)
					ne.setProcessing(false);
				for (NavigationGraphicalEntity ne : homeActions)
					ne.setProcessing(false);
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
				+ "This information system provides access to various bioinformatics ressources, "
				+ "developed at the IPK. The included data sources and tools have been "
				+ "mainly developed by members of the group Plant Bioinformatics and Image Analysis, "
				+ "with contributions from the group Bioinformatics and Information Technology.<br>"
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

	protected static void navigateTo(String target, final MyNavigationPanel navigationPanel,
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

		for (NavigationGraphicalEntity ne : actionPanel.getEntitySet()) {
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
			ModelToGui.executeNavigation(action, PanelTarget.ACTION, navigationPanel, actionPanel, graphPanel, null, rrr);
		}
	}

	public static void removeNavigationInfo(int leaveIntactFromFront, int preserveFromEnd) {
		preserveFromEnd = Math.abs(preserveFromEnd);
		ArrayList<NavigationGraphicalEntity> ne = globalNavigationPanel.getEntitySet();
		if (ne.size() > leaveIntactFromFront && ne.size() > preserveFromEnd) {
			ArrayList<NavigationGraphicalEntity> remove = new ArrayList<NavigationGraphicalEntity>();
			for (int i = 1; i <= ne.size(); i++) {
				boolean rem = false;
				if (i >= leaveIntactFromFront)
					rem = true;
				if (i >= ne.size() - preserveFromEnd)
					rem = false;
				if (rem)
					remove.add(ne.get(i));
			}
			for (NavigationGraphicalEntity r : remove)
				ne.remove(r);
		}
		globalNavigationPanel.setEntitySet(ne);
	}

}
