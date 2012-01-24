/*******************************************************************************
 * Copyright (c) 2003-2009 Plant Bioinformatics Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Mar 3, 2010 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.metacrop;

import info.clearthought.layout.TableLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;

import org.ErrorMsg;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.Graph;
import org.graffiti.plugin.inspector.InspectorTab;
import org.graffiti.plugin.view.GraphView;
import org.graffiti.plugin.view.View;

import de.ipk_gatersleben.ag_nw.graffiti.JLabelHTMLlink;

/**
 * @author klukas
 */
public class RimasTab extends InspectorTab {
	private static final long serialVersionUID = 1L;
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.inspector.InspectorTab#visibleForView(org.graffiti.plugin.view.View)
	 */
	@Override
	public boolean visibleForView(View v) {
		return v == null || v instanceof GraphView;
	}
	
	public RimasTab() {
		setLayout(new TableLayout(new double[][] {
							{ 5, TableLayout.FILL, 5 },
							{
												TableLayout.PREFERRED,
												5,
												TableLayout.PREFERRED,
												5,
												TableLayout.PREFERRED,
												10,
												TableLayout.PREFERRED,
												5,
												TableLayout.FILL
							} }));
		
		add(new JLabel("<html>" +
							"<h2>RIMAS SBGN maps</h2>"),
							"1,0");
		add(
							new JLabel(
												"<html>"
																	+
																	"<u>R</u>egulatory <u>I</u>nteraction <u>M</u>aps of <u>A</u>rabidopsis <u>S</u>eed Development<br><br>"
																	+
																	"RIMAS is a web-based information portal and provides a comprehensive regularly updated overview of regulatory pathways and genetic interactions during Arabidopsis embryo and seed development."),
							"1,2");
		add(new JLabelHTMLlink("http://rimas.ipk-gatersleben.de", "http://rimas.ipk-gatersleben.de"), "1,4");
		add(new JLabel("<html>" +
							"Click button to download or switch to map:"),
							"1,6");
		add(getRIMASdownloadGUI(), "1,8");
	}
	
	private JComponent getRIMASdownloadGUI() {
		String i = "<font color='gray'>@";
		ArrayList<JComponent> pathways = new ArrayList<JComponent>();
		pathways.add(
							TableLayout.get3Split(
												getPathwayButton("http://rimas.ipk-gatersleben.de/Pathways/AFLB3LEC1.gml",
																	"LEC1/AFL-B3 network",
																	"s_lec1aflb3network.png"),
												new JLabel(), new JLabelHTMLlink(i, "http://rimas.ipk-gatersleben.de/AFLB3network.htm"), TableLayout.FILL, 5,
												TableLayout.PREFERRED));
		pathways.add(
							TableLayout.get3Split(
												getPathwayButton("http://rimas.ipk-gatersleben.de/Pathways/AFLB3%20maturation.gml",
																	"LEC1/AFL-B3 factors and maturation gene control",
																	"s_lec1aflb3_maturation.png"),
												new JLabel(), new JLabelHTMLlink(i, "http://rimas.ipk-gatersleben.de/AFLB3maturation.htm"), TableLayout.FILL, 5,
												TableLayout.PREFERRED));
		pathways.add(
							TableLayout.get3Split(
												getPathwayButton("http://rimas.ipk-gatersleben.de/Pathways/AFLB3%20hormones.gml",
																	"LEC1/AFL-B3 factors and interactions with phytohormone metabolism",
																	"s_lec1aflb3_hormones.png"),
												new JLabel(), new JLabelHTMLlink(i, "http://rimas.ipk-gatersleben.de/AFLB3hormones.htm"), TableLayout.FILL, 5,
												TableLayout.PREFERRED));
		pathways.add(
							TableLayout.get3Split(
												getPathwayButton("http://rimas.ipk-gatersleben.de/Pathways/seed%20reg%20epigenetics%202-FS.gml",
																	"Epigenetic control of LEC1/AFL-B3 factors",
																	"s_lec1aflb3_epigenetics.png"),
												new JLabel(), new JLabelHTMLlink(i, "http://rimas.ipk-gatersleben.de/AFLB3epigenetics.htm"), TableLayout.FILL, 5,
												TableLayout.PREFERRED));
		return TableLayout.getMultiSplitVertical(pathways, 5);
	}
	
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
	
	protected JComponent getPathwayButton(final String url, String title, String image) {
		JButton res = new JButton("<html>" + title);
		// res.setIcon(getIcon(image));
		res.setOpaque(false);
		res.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!MainFrame.getInstance().lookUpAndSwitchToNamedSession(url.replaceAll("%20", " "))) {
					try {
						Graph g = MainFrame.getInstance().getGraph(url.substring(url.lastIndexOf("/")).replaceAll("%20", " "), new URL(url));
						MainFrame.getInstance().showGraph(g, e);
					} catch (MalformedURLException e1) {
						ErrorMsg.addErrorMessage(e1);
					}
				}
			}
		});
		return res;
	}
	
	@Override
	public String getName() {
		return getTitle();
	}
	
	@Override
	public String getTitle() {
		return "RIMAS";
	}
	
}
