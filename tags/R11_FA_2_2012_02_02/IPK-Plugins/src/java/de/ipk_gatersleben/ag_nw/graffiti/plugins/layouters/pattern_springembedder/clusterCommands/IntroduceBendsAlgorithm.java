/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder.clusterCommands;

import java.util.HashSet;

import org.graffiti.graph.Edge;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.parameter.BooleanParameter;
import org.graffiti.plugin.parameter.IntegerParameter;
import org.graffiti.plugin.parameter.Parameter;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;

public class IntroduceBendsAlgorithm extends AbstractAlgorithm {
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.Algorithm#getName()
	 */
	public String getName() {
		return "Introduce Bends";
	}
	
	private int minPercent = 5;
	private int lineStyle = 2;
	private String edgeShape = "";
	private boolean massCenterFromSelection = true;
	
	// private int maxDistance = 6;
	
	@Override
	public String getCategory() {
		return "Edges";
	}
	
	@Override
	public String getDescription() {
		return "Introduce Bends Parameters";
	}
	
	@Override
	public Parameter[] getParameters() {
		return new Parameter[] {
							new IntegerParameter(lineStyle,
												"<html>" +
																	"<b>Select a drawing style:</b><br><small>" +
																	"&nbsp;&nbsp;&nbsp;Style <b>1</b> - Segmented Line (two segments)<br>" +
																	"&nbsp;&nbsp;&nbsp;Style <b>2</b> - Smooth Line (one bend)<br>" +
																	"&nbsp;&nbsp;&nbsp;Style <b>3</b> - Smooth Line (two bends)", "Select one of the edge drawing styles"),
							new IntegerParameter(minPercent, "Minimum Percentage",
												""),
							new BooleanParameter(massCenterFromSelection, "Calculate Center from Selection",
												"If selected, the mass center will be calculated from the selection instead from the whole graph")
		// new IntegerParameter(maxDistance, "Detect Loops - max length:",
		// "If this value is greater than 1, the algorithm is additionally applied to detected loops of the specified maximum length (number of nodes forming the loop).")
		};
	}
	
	@Override
	public void setParameters(Parameter[] params) {
		
		int i = 0;
		lineStyle = ((IntegerParameter) params[i++]).getInteger().intValue();
		minPercent = ((IntegerParameter) params[i++]).getInteger().intValue();
		
		if (lineStyle == 1)
			edgeShape = "org.graffiti.plugins.views.defaults.PolyLineEdgeShape";
		else
			edgeShape = "org.graffiti.plugins.views.defaults.SmoothLineEdgeShape";
		
		massCenterFromSelection = ((BooleanParameter) params[i++]).getBoolean().booleanValue();
		// maxDistance = ((IntegerParameter) params[i++]).getInteger().intValue();
		
	}
	
	@Override
	public void check() throws PreconditionException {
		if (graph == null)
			throw new PreconditionException("No graph available!");
		if (graph.getEdges().size() <= 0)
			throw new PreconditionException("Graph contains no edges!");
		if (lineStyle < 1 || lineStyle > 3)
			throw new PreconditionException("Drawing Style \"" + lineStyle + "\" not supported.<br>" +
								"Use the drawing style 1, 2 or 3!");
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.Algorithm#execute()
	 */
	public void execute() {
		HashSet<Edge> workEdges = new HashSet<Edge>();
		if (selection == null || selection.getEdges().size() == 0)
			workEdges.addAll(graph.getEdges());
		else
			workEdges.addAll(selection.getEdges());
		GraphHelper.introduceNewBends(graph, workEdges, minPercent, edgeShape, (lineStyle == 3 ? 2 : 1), massCenterFromSelection, getName(), true);
		// if (maxDistance>0) {
		// HashSet<Node> workNodes = new HashSet<Node>();
		// for (Edge e : workEdges) {
		// workNodes.add(e.getSource());
		// workNodes.add(e.getTarget());
		// }
		// HashSet<Node> checkTheseNodes = new HashSet<Node>(workNodes);
		// while (checkTheseNodes.size()>0) {
		// HashSet<Node> workNodesCopy = new HashSet<Node>(workNodes);
		// Node t = checkTheseNodes.iterator().next();
		// checkTheseNodes.remove(t);
		// ArrayList<Edge> circle = new ArrayList<Edge>();
		// if (circleFound && circle!=null && circle.size()>1) {
		// GraphHelper.introduceNewBends(getSubset(circle, workNodesCopy), minPercent, edgeShape, (lineStyle==3 ? 2 : 1), true);
		// for (Edge del : circle) {
		// checkTheseNodes.remove(del.getSource());
		// checkTheseNodes.remove(del.getTarget());
		// }
		// }
		// }
		// }
	}
	
	@Override
	public boolean mayWorkOnMultipleGraphs() {
		return true;
	}
	
	// private HashSet<Edge> getSubset(ArrayList<Edge> circle, HashSet<Node> workNodes) {
	// HashSet<Edge> result = new HashSet<Edge>();
	// for (Edge e : circle) {
	// if (workNodes.contains(e.getSource()) && workNodes.contains(e.getTarget()))
	// result.add(e);
	// }
	// return result;
	// }
	
}
