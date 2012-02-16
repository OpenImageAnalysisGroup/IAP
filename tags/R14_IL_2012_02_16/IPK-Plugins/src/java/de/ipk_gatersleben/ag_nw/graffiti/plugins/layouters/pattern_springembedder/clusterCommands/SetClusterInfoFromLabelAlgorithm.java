/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder.clusterCommands;

import java.util.HashMap;

import org.AttributeHelper;
import org.Release;
import org.ReleaseInfo;
import org.graffiti.graph.GraphElement;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.PreconditionException;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;

public class SetClusterInfoFromLabelAlgorithm extends AbstractAlgorithm {
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.Algorithm#getName()
	 */
	public String getName() {
		if (ReleaseInfo.getRunningReleaseStatus() == Release.KGML_EDITOR)
			return null;
		else
			return "Copy node/edge label to cluster ID";
	}
	
	@Override
	public String getDescription() {
		return "<html>" +
							"About to use the node labels for the assignment of a cluster ID.<br><br>" +
							"<small>Hint: Commands dealing with alternative substance IDs, available from<br>" +
							"the mappings menu may be useful to temporaty modify the node labels, before<br>" +
							"using this command.";
	}
	
	@Override
	public String getCategory() {
		return "Elements"; // "menu.edit";
	}
	
	@Override
	public void check() throws PreconditionException {
		if (graph == null)
			throw new PreconditionException("No graph available!");
		if (graph.getNumberOfNodes() <= 0)
			throw new PreconditionException("Graph contains no graph elements!");
	}
	
	public void execute() {
		HashMap<GraphElement, String> ge2newClusterID = new HashMap<GraphElement, String>();
		for (GraphElement ge : getSelectedOrAllGraphElements()) {
			String lbl = AttributeHelper.getLabel(ge, "");
			ge2newClusterID.put(ge, lbl);
		}
		GraphHelper.applyUndoableClusterIdAssignment(graph, ge2newClusterID, getName(), true);
	}
	
	public boolean mayWorkOnMultipleGraphs() {
		return true;
	}
	
}