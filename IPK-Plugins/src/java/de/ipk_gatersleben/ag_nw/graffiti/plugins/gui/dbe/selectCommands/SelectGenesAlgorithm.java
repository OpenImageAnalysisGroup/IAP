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
import org.graffiti.plugin.algorithm.PreconditionException;

/**
 * @author Christian Klukas
 *         (c) 2004 IPK-Gatersleben
 */
public class SelectGenesAlgorithm extends AbstractAlgorithm {
	
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
			return "Select Genes";
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
		ArrayList<Node> geneNodes = new ArrayList<Node>();
		for (Node n : graph.getNodes()) {
			boolean added = false;
			if (!added) {
				// check for other hints, that this is a gene
				String kegg_type = (String) AttributeHelper.getAttributeValue(n, "kegg", "kegg_type", null, new String(""), false);
				if (kegg_type != null && kegg_type.equalsIgnoreCase("gene")) {
					geneNodes.add(n);
					added = true;
				}
			}
		}
		selection.addAll(geneNodes);
		MainFrame.getInstance().getActiveEditorSession().getSelectionModel().selectionChanged();
		MainFrame.showMessage(geneNodes.size() + " gene-nodes added to selection", MessageType.INFO);
	}
	
	@Override
	public boolean mayWorkOnMultipleGraphs() {
		return true;
	}
}
