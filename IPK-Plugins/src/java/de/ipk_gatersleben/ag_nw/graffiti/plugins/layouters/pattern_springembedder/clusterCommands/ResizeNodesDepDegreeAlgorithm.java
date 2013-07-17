/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder.clusterCommands;

import org.AttributeHelper;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.parameter.IntegerParameter;
import org.graffiti.plugin.parameter.Parameter;

public class ResizeNodesDepDegreeAlgorithm extends AbstractAlgorithm {
	
	int maxNodeSize = 60;
	int minNodeSize = 10;
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.Algorithm#getName()
	 */
	public String getName() {
		return null;
		// return "Resize depending on Node-Degree";
	}
	
	@Override
	public String getCategory() {
		return "Nodes";
	}
	
	@Override
	public Parameter[] getParameters() {
		return new Parameter[] {
							new IntegerParameter(minNodeSize, "Min. Node-Size",
												"The minimum size of a node"),
							new IntegerParameter(maxNodeSize, "Max. Node-Size",
												"The maximum size of a node") };
	}
	
	@Override
	public void setParameters(Parameter[] params) {
		int i = 0;
		minNodeSize = ((IntegerParameter) params[i++]).getInteger().intValue();
		maxNodeSize = ((IntegerParameter) params[i++]).getInteger().intValue();
	}
	
	@Override
	public void check() throws PreconditionException {
		if (graph == null)
			throw new PreconditionException("No graph available!");
		if (graph.getNodes().size() <= 0)
			throw new PreconditionException("Graph is empty!");
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.Algorithm#execute()
	 */
	public void execute() {
		graph.getListenerManager().transactionStarted(this);
		int minD = Integer.MAX_VALUE;
		int maxD = Integer.MIN_VALUE;
		for (Node n : graph.getNodes()) {
			int deg = n.getNeighbors().size();
			if (deg < minD)
				minD = deg;
			if (deg > maxD)
				maxD = deg;
		}
		for (Node n : graph.getNodes()) {
			int deg = n.getNeighbors().size();
			double sz;
			if (minD == maxD) {
				sz = (maxNodeSize + minNodeSize) / 2;
			} else {
				sz = (double) (deg - minD)
									/ (double) (maxD - minD) * (maxNodeSize - minNodeSize)
									+ minNodeSize;
			}
			AttributeHelper.setSize(n, sz, sz);
		}
		graph.getListenerManager().transactionFinished(this);
	}
}