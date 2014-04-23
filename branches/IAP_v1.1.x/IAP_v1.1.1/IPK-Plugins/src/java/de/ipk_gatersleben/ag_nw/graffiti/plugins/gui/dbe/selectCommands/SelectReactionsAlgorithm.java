/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 27.01.2005 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe.selectCommands;

import java.util.ArrayList;

import org.AttributeHelper;
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
public class SelectReactionsAlgorithm extends AbstractAlgorithm implements
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
		return "Select reactions";
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
		ArrayList<Node> reactions = new ArrayList<Node>();
		for (Node n : graph.getNodes()) {
			AttributeHelper.getLabel(n, null);
			// check for other hints, that this is a enzyme
			String kegg_reaction_type = (String) AttributeHelper.getAttributeValue(n, "kegg", "kegg_reaction_type", null, new String(""), false);
			if (AttributeHelper.isSBMLreaction(n))
				reactions.add(n);
			else {
				if (kegg_reaction_type != null && kegg_reaction_type.length() > 0)
					reactions.add(n);
				else {
					String kegg_reaction_id = (String) AttributeHelper.getAttributeValue(n, "kegg", "kegg_reaction", null, new String(""), false);
					if (kegg_reaction_id != null && kegg_reaction_id.length() > 0)
						reactions.add(n);
				}
			}
		}
		selection.addAll(reactions);
		MainFrame.getInstance().getActiveEditorSession().getSelectionModel().selectionChanged();
		MainFrame.showMessage(reactions.size() + " reaction-nodes added to selection", MessageType.INFO);
	}
	
	@Override
	public boolean mayWorkOnMultipleGraphs() {
		return true;
	}
}
