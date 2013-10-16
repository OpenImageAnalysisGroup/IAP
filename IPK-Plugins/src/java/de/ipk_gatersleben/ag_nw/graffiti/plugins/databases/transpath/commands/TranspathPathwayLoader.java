/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.transpath.commands;

import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.FeatureSet;
import org.ReleaseInfo;
import org.graffiti.editor.MainFrame;
import org.graffiti.event.ListenerManager;
import org.graffiti.graph.AdjListGraph;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;

import de.ipk_gatersleben.ag_nw.graffiti.MyInputHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.transpath.TranspathPathway;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.transpath.TranspathService;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.kegg.MutableList;

public class TranspathPathwayLoader extends AbstractAlgorithm {
	
	public void execute() {
		
		TranspathPathway[] sel = getTranspathPathwaySelectionFromUser(TranspathService.getPathways());
		Graph g = new AdjListGraph(new ListenerManager());
		HashMap<String, Node> graphElementId2graphNode = new HashMap<String, Node>();
		for (TranspathPathway tp : sel) {
			tp.addElementsToGraph(g, tp.ID, graphElementId2graphNode);
		}
		MainFrame.getInstance().showGraph(g, getActionEvent());
	}
	
	public static TranspathPathway[] getTranspathPathwaySelectionFromUser(final Collection<TranspathPathway> pathways) {
		final MutableList pathwaySelection = new MutableList(new DefaultListModel());
		
		pathwaySelection.setPrototypeCellValue("<html>ÄÖyz");
		pathwaySelection.setFixedCellWidth(580);
		pathwaySelection.setFixedCellHeight(new JLabel("<html>AyÖÄ").getPreferredSize().height);
		
		for (TranspathPathway oe : pathways) {
			pathwaySelection.getContents().addElement(oe);
		}
		pathwaySelection.setSelectedIndex(0);
		
		final JLabel searchResult = new JLabel("<html><small><font color='gray'>" + pathways.size() + " pathways");
		
		JScrollPane pathwaySelectionScrollPane = new JScrollPane(pathwaySelection);
		
		pathwaySelectionScrollPane.setPreferredSize(new Dimension(600, 300));
		
		final JTextField filter = new JTextField("");
		
		filter.addKeyListener(new KeyListener() {
			
			public void keyPressed(KeyEvent e) {
				//
				
			}
			
			public void keyReleased(KeyEvent e) {
				//
				
			}
			
			public void keyTyped(KeyEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						String filterText = filter.getText().toUpperCase();
						
						pathwaySelection.getContents().clear();
						for (TranspathPathway oe : pathways) {
							if (oe.toString().toUpperCase().contains(filterText))
								pathwaySelection.getContents().addElement(oe);
						}
						searchResult.setText("<html><small><font color='gray'>" + pathwaySelection.getContents().size() + "/" + pathways.size() + " pathways shown");
					};
				});
			}
		});
		
		// MyOrganismSelectionDialog osd = new MyOrganismSelectionDialog();
		Object[] result = MyInputHelper.getInput(
							"Please select the desired pathway(s).<br>" +
												"<small>You may use the Search-Field to locate the " +
												"desired pathway.",
							"Select Pathways",
							new Object[] {
												"Select Pathways", pathwaySelectionScrollPane,
												"Search", filter,
												"", searchResult
				});
		if (result != null && pathwaySelection.getSelectedValue() != null) {
			Object[] ooo = pathwaySelection.getSelectedValues();
			ArrayList<TranspathPathway> res = new ArrayList<TranspathPathway>();
			for (Object o : ooo)
				res.add((TranspathPathway) o);
			TranspathPathway[] oe = res.toArray(new TranspathPathway[] {});
			return oe;
		}
		return null;
	}
	
	@Override
	public String getCategory() {
		return "Nodes";
	}
	
	public String getName() {
		if (ReleaseInfo.getIsAllowedFeature(FeatureSet.TRANSPATH_ACCESS))
			return "Load TRANSPATH (R) Pathway";
		else
			return null;
	}
	
}
