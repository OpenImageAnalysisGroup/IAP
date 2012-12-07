/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 17.01.2005 by Christian Klukas
 * (c) 2005 IPK Gatersleben, Group Network Analysis
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe;

import java.util.ArrayList;
import java.util.Collection;

import org.graffiti.editor.MainFrame;
import org.graffiti.editor.MessageType;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.parameter.Parameter;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NodeHelper;

/**
 * @author Christian Klukas
 *         (c) 2005 IPK Gatersleben, Group Network Analysis
 */
public class CombineMappingData extends AbstractAlgorithm {
	
	public String getName() {
		return "Merge Multiple Diagrams";
	}
	
	@Override
	public void check() throws PreconditionException {
		super.check();
		for (Node workNode : getSelectedOrAllNodes())
			if (new NodeHelper(workNode).hasDataMapping())
				return;
		throw new PreconditionException("Graph contains no mapped data");
	}
	
	@Override
	public String getCategory() {
		return "Mapping";
	}
	
	@Override
	public String getDescription() {
		return null;
	}
	
	@Override
	public Parameter[] getParameters() {
		return null;
	}
	
	@Override
	public void setParameters(Parameter[] params) {
		
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.Algorithm#execute()
	 */
	public void execute() {
		Collection<Node> workNodes = new ArrayList<Node>(getSelectedOrAllNodes());
		graph.getListenerManager().transactionStarted(this);
		mergeMultipleMappingsIntoSingleMapping(workNodes);
		graph.getListenerManager().transactionFinished(this);
		MainFrame.showMessage("Merged multiple data mappings into a single data mapping for the selected nodes ("
							+ workNodes.size() + ")", MessageType.INFO);
		GraphHelper.issueCompleteRedrawForActiveView();
	}
	
	public static void mergeMultipleMappingsIntoSingleMapping(Collection<Node> workNodes) {
		for (Node workNode : workNodes) {
			NodeHelper nh = new NodeHelper(workNode);
			nh.mergeMultipleMappings();
		}
	}
	
}
