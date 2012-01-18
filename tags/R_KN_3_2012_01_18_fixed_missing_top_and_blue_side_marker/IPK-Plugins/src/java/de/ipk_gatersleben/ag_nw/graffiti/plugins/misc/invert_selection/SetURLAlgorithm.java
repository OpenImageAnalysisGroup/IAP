/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 17.10.2005 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.invert_selection;

import org.AttributeHelper;
import org.FeatureSet;
import org.ReleaseInfo;
import org.graffiti.graph.GraphElement;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.Algorithm;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.plugin.parameter.StringParameter;

public class SetURLAlgorithm extends AbstractAlgorithm implements Algorithm {
	
	private String targetURL;
	
	public String getName() {
		if (ReleaseInfo.getIsAllowedFeature(FeatureSet.URL_NODE_ANNOTATION))
			return "Add URL-link to a web-resource...";
		else
			return null;
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
	}
	
	@Override
	public String getDescription() {
		return "<html>" +
							"Please enter the target URL for the reference information.<br><br>" +
							"<small>This information may be later retrieved by the network/node/edge<br>" +
							"context menu or the corresponding attribute editors in the side panel.";
	}
	
	@Override
	public Parameter[] getParameters() {
		String currentURL = "http://";
		if (selection.isEmpty()) {
			String u = AttributeHelper.getReferenceURL(graph);
			if (u != null) {
				if (!currentURL.equals(u) && !currentURL.equals("http://"))
					currentURL = "~";
				else
					currentURL = u;
			}
		} else {
			for (GraphElement ge : selection.getElements()) {
				String u = AttributeHelper.getReferenceURL(ge);
				if (u != null) {
					if (!currentURL.equals(u) && !currentURL.equals("http://"))
						currentURL = "~";
					else
						currentURL = u;
				}
			}
		}
		return new Parameter[] { new StringParameter(currentURL, "URL", null) };
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
		if (!url.equals("~")) {
			if (selection.isEmpty()) {
				AttributeHelper.setReferenceURL(graph, url);
			} else {
				for (GraphElement ge : selection.getElements())
					AttributeHelper.setReferenceURL(ge, url);
			}
		}
	}
}
