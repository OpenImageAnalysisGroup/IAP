/*******************************************************************************
 * Copyright (c) 2008 Plant Bioinformatics Group, IPK Gatersleben
 *******************************************************************************/
/**
 * @author Christian Klukas
 */
package org.graffiti.plugin.actions;

import java.awt.event.ActionListener;

import org.graffiti.attributes.Attribute;
import org.graffiti.graph.Graph;
import org.graffiti.graph.GraphElement;

public interface URLattributeAction {
	public String getPreIdentifyer();
	
	public String getCommandDescription(boolean shortDesc, boolean altDesc);
	
	public ActionListener getActionListener(Attribute displayable, Graph graph, GraphElement ge, boolean performAltCommand);
	
	public boolean supportsModifyCommand();
}
