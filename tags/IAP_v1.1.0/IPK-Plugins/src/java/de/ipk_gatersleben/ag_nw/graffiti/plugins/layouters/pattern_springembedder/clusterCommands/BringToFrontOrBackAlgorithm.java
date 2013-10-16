/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder.clusterCommands;

import org.Release;
import org.ReleaseInfo;
import org.graffiti.graph.GraphElement;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.parameter.Parameter;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;

public class BringToFrontOrBackAlgorithm extends AbstractAlgorithm {
	
	boolean bringToFront = true;
	private boolean redrawViewWhenFinished = true;
	
	public BringToFrontOrBackAlgorithm() {
		bringToFront = true;
	}
	
	public BringToFrontOrBackAlgorithm(boolean toBack) {
		bringToFront = !toBack;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.Algorithm#getName()
	 */
	public String getName() {
		if (ReleaseInfo.getRunningReleaseStatus() != Release.KGML_EDITOR)
			return bringToFront ? "Move Elements to Front" : "Move Elements to Back";
		else
			return null;
	}
	
	@Override
	public boolean mayWorkOnMultipleGraphs() {
		return true;
	}
	
	@Override
	public String getCategory() {
		return "Layout";
	}
	
	@Override
	public Parameter[] getParameters() {
		return null;
	}
	
	@Override
	public void setParameters(Parameter[] params) {
		
	}
	
	@Override
	public void check() throws PreconditionException {
		if (graph == null)
			throw new PreconditionException("No graph available!");
		if (graph.getEdges().size() <= 0 && graph.getNodes().size() <= 0)
			throw new PreconditionException("Graph contains no elements!");
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.Algorithm#execute()
	 */
	public void execute() {
		int smallestViewID = Integer.MAX_VALUE;
		int greatestViewID = Integer.MIN_VALUE;
		
		for (GraphElement ge : graph.getGraphElements()) {
			if (ge.getViewID() < smallestViewID)
				smallestViewID = ge.getViewID();
			if (ge.getViewID() > greatestViewID)
				greatestViewID = ge.getViewID();
		}
		
		if (selection.isEmpty())
			selection.addAll(graph.getEdges());
		
		for (GraphElement ge : selection.getElements()) {
			if (bringToFront) {
				if (greatestViewID != Integer.MIN_VALUE)
					ge.setViewID(greatestViewID + 1);
			} else {
				if (smallestViewID != Integer.MAX_VALUE)
					ge.setViewID(smallestViewID - 1);
			}
		}
		
		if (redrawViewWhenFinished)
			GraphHelper.issueCompleteRedrawForGraph(graph);
	}
	
	public void setRedraw(boolean redrawViewWhenFinished) {
		this.redrawViewWhenFinished = redrawViewWhenFinished;
	}
	
}