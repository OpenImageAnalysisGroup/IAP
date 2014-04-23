/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 17.10.2005 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.pathway_references;

import org.AttributeHelper;
import org.graffiti.graph.GraphElement;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.Algorithm;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.plugin.parameter.StringParameter;

public class PathwayReferenceAlgorithm extends AbstractAlgorithm implements Algorithm {
	
	private String targetURL;
	
	public String getName() {
		return "Add link to other network file...";
	}
	
	@Override
	public String getCategory() {
		return "Elements"; // "menu.edit";
	}
	
	@Override
	public boolean isLayoutAlgorithm() {
		return false;
	}
	
	@Override
	public void check() throws PreconditionException {
		if (graph == null)
			throw new PreconditionException("No active graph editor window found!");
		if (selection == null || selection.isEmpty())
			throw new PreconditionException("No graph elements are selected!");
	}
	
	@Override
	public String getDescription() {
		return "<html>" +
							"Please enter a file name for a cross-reference to another network.<br>" +
							"Use relative file path information if possible, to make this information<br>" +
							"robust against folder renaming and movement operations.<br>" +
							"<small>This information may be later retrieved by the node/edge<br>" +
							"context menu or the corresponding attribute editors in the side panel.";
	}
	
	@Override
	public Parameter[] getParameters() {
		String currentURL = "";
		for (GraphElement ge : selection.getElements()) {
			String u = (String) AttributeHelper.getAttributeValue(ge, "", "pathway_ref_url", null, "");
			if (u != null) {
				if (!currentURL.equals(u) && !currentURL.equals(""))
					currentURL = "~";
				else
					currentURL = u.replace(AttributeHelper.preFilePath, "");
			}
		}
		return new Parameter[] { new StringParameter(currentURL, "Referenced Pathway Filename", null) };
	}
	
	@Override
	public void setParameters(Parameter[] params) {
		int i = 0;
		this.targetURL = ((StringParameter) params[i++]).getString();
	}
	
	public void execute() {
		if (targetURL == null)
			return;
		String url = targetURL;
		if (!url.equals("~"))
			for (GraphElement ge : selection.getElements())
				AttributeHelper.setPathwayReference(ge, url);
	}
}
