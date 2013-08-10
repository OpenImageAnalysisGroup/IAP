/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder.clusterCommands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.AttributeHelper;
import org.Vector2d;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.MessageType;
import org.graffiti.graph.Edge;
import org.graffiti.graph.GraphElement;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.parameter.DoubleParameter;
import org.graffiti.plugin.parameter.Parameter;

public class IntroduceSelfEdgeBends extends AbstractAlgorithm {
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.Algorithm#getName()
	 */
	public String getName() {
		return "Introduce Bends for Self-Loops";
	}
	
	private double xdir = 15;
	private double ydir = 5;
	
	@Override
	public String getCategory() {
		return "Edges";
	}
	
	@Override
	public String getDescription() {
		return "<html>Introduce Bends for Self-Loops<br>" +
							"(edges which start and end with the same node<br><br>" +
							"Please specify the distance from the node-corners<br>" +
							"in X- and Y-direction:";
	}
	
	@Override
	public Parameter[] getParameters() {
		return new Parameter[] {
							new DoubleParameter(xdir, "X-Direction", "Ammount of space (X-direction) between right end of node shape and bend positions"),
							new DoubleParameter(ydir, "Y-Direction", "Ammount of space (Y-direction) between upper and lower end of node shape and bend positions"), };
	}
	
	@Override
	public void setParameters(Parameter[] params) {
		int i = 0;
		xdir = ((DoubleParameter) params[i++]).getDouble();
		ydir = ((DoubleParameter) params[i++]).getDouble();
	}
	
	@Override
	public void check() throws PreconditionException {
		if (graph == null)
			throw new PreconditionException("No graph available!");
		if (graph.getEdges().size() <= 0)
			throw new PreconditionException("Graph contains no edges!");
		boolean found = false;
		for (Edge e : graph.getEdges()) {
			if (e.getSource() == e.getTarget()) {
				found = true;
				break;
			}
		}
		if (!found)
			throw new PreconditionException("Graph contains no self-loops!");
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.Algorithm#execute()
	 */
	public void execute() {
		graph.getListenerManager().transactionStarted(this);
		try {
			graph.numberGraphElements();
			HashMap<String, Integer> knownCombinations = new HashMap<String, Integer>();
			int cnt = 0;
			for (GraphElement ge : getSelectedOrAllGraphElements()) {
				if (ge instanceof Edge) {
					Edge e = (Edge) ge;
					if (e.getSource() == e.getTarget()) {
						cnt++;
						
						Integer knownFrequency = knownCombinations.get(e.getSource().getID() + "§" + e.getTarget().getID());
						if (knownFrequency == null) {
							knownFrequency = new Integer(1);
						} else {
							knownFrequency = new Integer(knownFrequency + 1);
						}
						knownCombinations.put(e.getSource().getID() + "§" + e.getTarget().getID(), new Integer(knownFrequency));
						knownCombinations.put(e.getTarget().getID() + "§" + e.getSource().getID(), new Integer(knownFrequency));
						
						AttributeHelper.removeEdgeBends(e);
						AttributeHelper.addEdgeBends(e, getSelfLoopBends(e, xdir * knownFrequency, ydir * knownFrequency * knownFrequency));
						AttributeHelper.setEdgeBendStyle(e, "org.graffiti.plugins.views.defaults.SmoothLineEdgeShape");
					}
				}
			}
			MainFrame.showMessage(cnt + " self-loops have been detected and processed.", MessageType.INFO);
		} finally {
			graph.getListenerManager().transactionFinished(this);
		}
		// GraphHelper.introduceNewBends(workEdges, minPercent, edgeShape, (lineStyle==3 ? 2 : 1), massCenterFromSelection);
	}
	
	private Collection<Vector2d> getSelfLoopBends(Edge e, double xdir, double ydir) {
		Collection<Vector2d> result = new ArrayList<Vector2d>();
		Node a = e.getSource();
		Vector2d np = AttributeHelper.getPositionVec2d(a);
		Vector2d size = AttributeHelper.getSize(a);
		Vector2d p1 = new Vector2d(np.x + size.x / 2 + xdir, np.y - size.y / 2 - ydir);
		Vector2d p2 = new Vector2d(np.x + size.x / 2 + xdir, np.y + size.y / 2 + ydir);
		result.add(p1);
		result.add(p2);
		return result;
	}
	
	@Override
	public boolean mayWorkOnMultipleGraphs() {
		return true;
	}
	
}