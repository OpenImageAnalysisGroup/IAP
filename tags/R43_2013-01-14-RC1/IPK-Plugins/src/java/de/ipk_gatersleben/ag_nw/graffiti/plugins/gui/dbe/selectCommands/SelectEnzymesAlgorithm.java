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

import de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.sib_enzymes.EnzymeEntry;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.sib_enzymes.EnzymeService;

/**
 * @author Christian Klukas
 *         (c) 2004 IPK-Gatersleben
 */
public class SelectEnzymesAlgorithm extends AbstractAlgorithm implements
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
			if (ReleaseInfo.getIsAllowedFeature(FeatureSet.KEGG_ACCESS))
				return "Select enzymes";
			else
				return null;
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
		ArrayList<Node> enzymes = new ArrayList<Node>();
		for (Node n : graph.getNodes()) {
			String label = AttributeHelper.getLabel(n, null);
			boolean added = false;
			if (label != null) {
				EnzymeEntry ez = EnzymeService.getEnzymeInformation(label, false);
				if (ez != null && ez.isValid()) {
					enzymes.add(n);
					added = true;
				}
			}
			if (!added) {
				// check for other hints, that this is a enzyme
				String kegg_type = (String) AttributeHelper.getAttributeValue(n, "kegg", "kegg_type", null, new String(""), false);
				if (kegg_type != null && kegg_type.equalsIgnoreCase("enzyme")) {
					enzymes.add(n);
					added = true;
				}
			}
		}
		selection.addAll(enzymes);
		MainFrame.getInstance().getActiveEditorSession().getSelectionModel().selectionChanged();
		MainFrame.showMessage(enzymes.size() + " enzyme-nodes added to selection", MessageType.INFO);
	}
	
	@Override
	public boolean mayWorkOnMultipleGraphs() {
		return true;
	}
	
}
