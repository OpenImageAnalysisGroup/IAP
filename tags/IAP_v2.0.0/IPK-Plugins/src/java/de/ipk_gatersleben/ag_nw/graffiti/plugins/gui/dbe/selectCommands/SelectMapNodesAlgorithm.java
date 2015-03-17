/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 27.01.2005 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe.selectCommands;

import java.util.ArrayList;

import org.AttributeHelper;
import org.FeatureSet;
import org.ReleaseInfo;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.MessageType;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.Algorithm;
import org.graffiti.plugin.algorithm.PreconditionException;

/**
 * @author Christian Klukas
 *         (c) 2004 IPK-Gatersleben
 */
public class SelectMapNodesAlgorithm extends AbstractAlgorithm implements
					Algorithm {
	
	@Override
	public void check() throws PreconditionException {
		if (graph == null)
			throw new PreconditionException("No active graph editor window found!");
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.Algorithm#getName()
	 */
	public String getName() {
		if (ReleaseInfo.getIsAllowedFeature(FeatureSet.KEGG_ACCESS_ENH))
			return null;
		else
			return "Select Map Nodes";
	}
	
	@Override
	public String getCategory() {
		return "menu.edit";
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.Algorithm#execute()
	 */
	public void execute() {
		boolean skipTitleNodes = false;
		if (ReleaseInfo.getIsAllowedFeature(FeatureSet.KEGG_ACCESS_ENH))
			skipTitleNodes = true;
		ArrayList<Node> mapNodes = new ArrayList<Node>();
		for (Node n : graph.getNodes()) {
			boolean added = false;
			String kegg_type = (String) AttributeHelper.getAttributeValue(n, "kegg", "kegg_type", null, new String(""), false);
			if (kegg_type != null && (kegg_type.equalsIgnoreCase("map") || kegg_type.equalsIgnoreCase("ko"))) {
				if (skipTitleNodes) {
					if (!(AttributeHelper.getLabel(n, "").indexOf("TITLE:") >= 0)) {
						mapNodes.add(n);
						added = true;
					}
				} else {
					mapNodes.add(n);
					added = true;
				}
			}
			if (!added) {
				String refURL = AttributeHelper.getPathwayReference(n);
				if (refURL != null && refURL.length() > 0) {
					if (!skipTitleNodes || n.getDegree() != 0) {
						mapNodes.add(n);
						added = true;
					}
				}
			}
		}
		selection.addAll(mapNodes);
		MainFrame.getInstance().getActiveEditorSession().getSelectionModel().selectionChanged();
		MainFrame.showMessage(mapNodes.size() + " map-nodes added to selection", MessageType.INFO);
	}
	
	@Override
	public boolean mayWorkOnMultipleGraphs() {
		return true;
	}
}
