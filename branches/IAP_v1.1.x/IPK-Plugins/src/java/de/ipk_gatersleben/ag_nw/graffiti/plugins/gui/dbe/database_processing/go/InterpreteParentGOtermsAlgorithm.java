/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 17.01.2005 by Christian Klukas
 * (c) 2005 IPK Gatersleben, Group Network Analysis
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe.database_processing.go;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Collection;
import java.util.HashMap;

import javax.swing.JLabel;
import javax.swing.JMenuItem;

import org.AttributeHelper;
import org.OpenFileDialogService;
import org.PositionGridGenerator;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.ProvidesNodeContextMenu;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NodeHelper;

/**
 * @author Christian Klukas
 *         (c) 2006 IPK Gatersleben, Group Network Analysis
 */
public class InterpreteParentGOtermsAlgorithm extends AbstractAlgorithm implements ProvidesNodeContextMenu {
	
	private static GoProcessing gp = null;
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.Algorithm#getName()
	 */
	public String getName() {
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.Algorithm#execute()
	 */
	public void execute() {
	}
	
	public static Node processGoHierarchy(PositionGridGenerator pgg, HashMap<String, Node> goTerm2goNode, GoProcessing gp, String goTerm, Graph g) {
		Node gn;
		if (!goTerm2goNode.containsKey(goTerm)) {
			gn = g.addNode(AttributeHelper.getDefaultGraphicsAttributeForNode(100d + Math.random() * 100d, 100d + Math.random() * 100d));
			NodeHelper gnh = new NodeHelper(gn);
			GOinformation gi = gp.getGOinformation(goTerm);
			String name = gi.getName();
			if (name == null)
				name = goTerm;
			gnh.setLabel(name);
			gnh.setPosition(pgg.getNextPosition());
			gnh.setClusterID(gi.getNamespace());
			gnh.setTooltip(gi.getDefStr());
			gnh.setAttributeValue("go", "term", goTerm);
			Dimension d = new JLabel(name).getPreferredSize();
			if (d.getHeight() < 50 && d.getWidth() < 2000)
				gnh.setSize(d.getWidth() + 15, d.getHeight() + 15);
			else {
				System.out.println("Strange label:");
				System.out.println("Text:" + name);
				System.out.println("Size:" + d.getWidth() + " x " + d.getHeight());
			}
			goTerm2goNode.put(goTerm, gn);
			
			Collection<String> parents = gi.getDirectParents();
			for (String gt : parents)
				connectNodeWithNodes(gn, processGoHierarchy(pgg, goTerm2goNode, gp, gt, g));
		}
		gn = goTerm2goNode.get(goTerm);
		return gn;
	}
	
	private static void connectNodeWithNodes(Node goNode, Node newGoNode) {
		if (goNode == null || newGoNode == null)
			return;
		if (!goNode.getNeighbors().contains(newGoNode)) {
			Edge e = goNode.getGraph().addEdge(newGoNode, goNode, true, AttributeHelper.getDefaultGraphicsAttributeForEdge(Color.BLACK, Color.BLACK, true));
			AttributeHelper.setBorderWidth(e, 3d);
		}
	}
	
	public JMenuItem[] getCurrentNodeContextMenuItem(final Collection<Node> selectedNodes) {
		JMenuItem addParents =
							new JMenuItem("Add GO-Term-Parent-Nodes");
		JMenuItem addChildren =
							new JMenuItem("Add GO-Term-Children-Nodes");
		
		addParents.addActionListener(getGoActionListener(selectedNodes, true));
		addChildren.addActionListener(getGoActionListener(selectedNodes, false));
		return new JMenuItem[] { addParents, addChildren };
	}
	
	private ActionListener getGoActionListener(final Collection<Node> selectedNodes, boolean addParentNodes) {
		ActionListener result = new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				if (gp == null) {
					File obofile = OpenFileDialogService.getFile(new String[] { ".obo-xml" }, "Gene Ontology File (*.obo-xml)");
					if (obofile == null)
						return;
					gp = new GoProcessing(obofile);
					if (!gp.isValid()) {
						gp = null;
						MainFrame.showMessageDialog("The input file could not be loaded. It may not be a valid gene-ontology obo-xml file!", "Error");
						return;
					}
				}
				if (gp == null)
					return;
				if (!selectedNodes.isEmpty()) {
				}
				graph.getListenerManager().transactionStarted(graph);
				graph.getListenerManager().transactionFinished(graph);
			}
		};
		return result;
	}
}
