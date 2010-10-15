/*******************************************************************************
 * 
 *    Copyright (c) 2003-2009 Plant Bioinformatics Group, IPK Gatersleben
 * 
 *******************************************************************************/
/*
 * Created on Apr 28, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.gui.nav;

import java.util.HashMap;

import javax.swing.ImageIcon;

import de.ipk.ag_ba.gui.navigation_model.NavigationGraphicalEntity;
import de.ipk.ag_ba.gui.util.WebFolder;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.metacrop.RimasTab;

/**
 * @author klukas
 * 
 */
public class RimasNav {

	// private JComponent getRIMASdownloadGUI() {
	// String i = "<font color='gray'>@";
	// ArrayList<JComponent> pathways = new ArrayList<JComponent>();
	// pathways.add(
	// TableLayout.get3Split(
	// getPathwayButton("http://rimas.ipk-gatersleben.de/Pathways/AFLB3LEC1.gml",
	// "LEC1/AFL-B3 network",
	// "s_lec1aflb3network.png"),
	// new JLabel(), new JLabelHTMLlink(i,
	// "http://rimas.ipk-gatersleben.de/AFLB3network.htm"), TableLayout.FILL, 5,
	// TableLayout.PREFERRED));
	// pathways.add(
	// TableLayout.get3Split(
	// getPathwayButton("http://rimas.ipk-gatersleben.de/Pathways/AFLB3%20maturation.gml",
	// "LEC1/AFL-B3 factors and maturation gene control",
	// "s_lec1aflb3_maturation.png"),
	// new JLabel(), new JLabelHTMLlink(i,
	// "http://rimas.ipk-gatersleben.de/AFLB3maturation.htm"), TableLayout.FILL,
	// 5, TableLayout.PREFERRED));
	// pathways.add(
	// TableLayout.get3Split(
	// getPathwayButton("http://rimas.ipk-gatersleben.de/Pathways/AFLB3%20hormones.gml",
	// "LEC1/AFL-B3 factors and interactions with phytohormone metabolism",
	// "s_lec1aflb3_hormones.png"),
	// new JLabel(), new JLabelHTMLlink(i,
	// "http://rimas.ipk-gatersleben.de/AFLB3hormones.htm"), TableLayout.FILL, 5,
	// TableLayout.PREFERRED));
	// pathways.add(
	// TableLayout.get3Split(
	// getPathwayButton("http://rimas.ipk-gatersleben.de/Pathways/seed%20reg%20epigenetics%202-FS.gml",
	// "Epigenetic control of LEC1/AFL-B3 factors",
	// "s_lec1aflb3_epigenetics.png"),
	// new JLabel(), new JLabelHTMLlink(i,
	// "http://rimas.ipk-gatersleben.de/AFLB3epigenetics.htm"), TableLayout.FILL,
	// 5, TableLayout.PREFERRED));
	// return TableLayout.getMultiSplitVertical(pathways, 5);
	// }

	public static ImageIcon getIcon(String fn) {
		ClassLoader cl = RimasTab.class.getClassLoader();
		String path = RimasTab.class.getPackage().getName().replace('.', '/');
		try {
			ImageIcon i = new ImageIcon(cl.getResource(path + "/images/" + fn));
			return i;
		} catch (Exception e) {
			return null;
		}
	}

	private static String getIntroTxt() {
		String s = "<h2>RIMAS - Regulatory Interaction Maps of Arabidopsis Seed Development</h2>"
				+ "RIMAS contains detailed SBGN conforming network diagrams which reflect the interactions "
				+ "of transcription factor hierarchies, gene promoter elements, hormonal pathways, epigenetic "
				+ "processes and chromatin remodelling and provides an easy access to the relevant references.<br><br>"
				+ "IAP provides interactive access to the RIMAS pathways. RIMAS itself is a web-based information "
				+ "portal with additional detailed descriptions and background information about the investigated "
				+ "biological phenomena (please click the Website button above for access to detailed pathway descriptions).";
		return s;
	}

	public static NavigationGraphicalEntity getRimas() {
		HashMap<String, String> rimasDoku = new HashMap<String, String>();
		rimasDoku.put("", "Lit. Reference:http://www.cell.com/trends/plant-science/abstract/S1360-1385(10)00061-0");
		NavigationGraphicalEntity rimas = WebFolder.getBrowserNavigationEntity(rimasDoku, "RIMAS", "img/rimas.png",
				"http://rimas.ipk-gatersleben.de/Pathways/", "Website", "img/browser.png",
				"http://rimas.ipk-gatersleben.de/", new String[] { ".gml", ".graphml" }, getIntroTxt(), null);
		return rimas;
	}

	//
	// protected JComponent getPathwayButton(final String url, final String
	// title, String image) {
	// JButton res = new JButton("<html>"+title);
	// res.setIcon(getIcon(image));
	// res.setOpaque(false);
	// res.addActionListener(new ActionListener() {
	// public void actionPerformed(ActionEvent e) {
	// MyUtility.navigate(title);
	// if
	// (!MainFrame.getInstance().lookUpAndSwitchToNamedSession(url.replaceAll("%20",
	// " "))) {
	// try {
	// Graph g =
	// MainFrame.getInstance().getGraph(url.substring(url.lastIndexOf("/")).replaceAll("%20",
	// " "), new URL(url));
	// MainFrame.getInstance().showGraph(g, e);
	// } catch (MalformedURLException e1) {
	// ErrorMsg.addErrorMessage(e1);
	// }
	// }
	// }
	// });
	// return res;
	// }
	//	
}
