/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.rt_tree;

import java.util.HashMap;
import java.util.LinkedHashMap;

import org.ErrorMsg;
import org.graffiti.graph.AdjListGraph;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.Algorithm;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.selection.Selection;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NodeHelper;

public class MultiTreeLayout extends AbstractAlgorithm {
	
	public String getName() {
		// return null;
		return "Tree Layout (multi)";
	}
	
	@Override
	public String getCategory() {
		return "Layout";
	}
	
	@Override
	public boolean isLayoutAlgorithm() {
		return true;
	}
	
	@Override
	public Parameter[] getParameters() {
		RTTreeLayout rtl = new RTTreeLayout();
		rtl.attach(graph, selection);
		return rtl.getParameters();
	}
	
	@Override
	public String getDescription() {
		return "<html>" +
							"Multi Tree-Layout: This algorithm works best for graphs<br>" +
							"with multiple disconnected tree like graph structures.<br>" +
							"On general graphs it may not work correctly.";
	}
	
	@Override
	public void setParameters(Parameter[] params) {
	}
	
	public void execute() {
		int i = 0;
		HashMap<Integer, NodeHelper> id2node = new LinkedHashMap<Integer, NodeHelper>();
		for (Node n : graph.getNodes()) {
			NodeHelper nh = new NodeHelper(n, false);
			nh.setAttributeValue("temp", "iid", new Integer(i++));
			id2node.put(new Integer(i - 1), nh);
		}
		Graph graphCopy = new AdjListGraph();
		graphCopy.addGraph(graph);
		Integer resultType = new Integer(0);
		double maxXallGraphs = 0;
		for (Graph cc : GraphHelper.getConnectedComponents(graphCopy)) {
			double maxXthisGraph = 0;
			Algorithm layout = new RTTreeLayout();
			layout.attach(cc, new Selection());
			try {
				layout.check();
				layout.execute();
				for (Node n : cc.getNodes()) {
					NodeHelper nh = new NodeHelper(n, false);
					if (nh.getX() + nh.getWidth() / 2 > maxXthisGraph)
						maxXthisGraph = nh.getX() + nh.getWidth() / 2;
					Integer id = (Integer) nh.getAttributeValue("temp", "iid", null, resultType);
					if (id != null) {
						NodeHelper on = id2node.get(id);
						on.setPosition(nh.getX() + maxXallGraphs, nh.getY());
					}
				}
				// MainFrame.getInstance().showGraph(cc);
			} catch (Exception e) {
				ErrorMsg.addErrorMessage(e);
			}
			maxXallGraphs += maxXthisGraph + 20;
		}
	}
	
}