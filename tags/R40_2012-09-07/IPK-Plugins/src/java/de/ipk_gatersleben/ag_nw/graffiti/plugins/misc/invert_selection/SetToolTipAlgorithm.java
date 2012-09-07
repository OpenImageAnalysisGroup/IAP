/*******************************************************************************
 * Copyright (c) 2003-2009 Plant Bioinformatics Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 17.10.2005 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.invert_selection;

import org.AttributeHelper;
import org.FeatureSet;
import org.ReleaseInfo;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.Algorithm;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.plugin.parameter.StringParameter;

public class SetToolTipAlgorithm extends AbstractAlgorithm implements Algorithm {
	
	private String tooltip;
	
	public String getName() {
		if (ReleaseInfo.getIsAllowedFeature(FeatureSet.TOOLTIPS))
			return "Set Tooltip...";
		else
			return null;
	}
	
	@Override
	public String getCategory() {
		return "menu.edit";
	}
	
	@Override
	public boolean isLayoutAlgorithm() {
		return false;
	}
	
	@Override
	public void check() throws PreconditionException {
		if (graph == null)
			throw new PreconditionException("No active graph editor window found!");
		if (graph.getNumberOfNodes() <= 0)
			throw new PreconditionException("No graph nodes!");
	}
	
	@Override
	public String getDescription() {
		return "<html>" +
							"Please enter the tooltip information, displayed in the status-bar<br>" +
							"or as a mouse tooltip in exported HTML image maps when hovering the mouse<br>" +
							"over network node elements.";
	}
	
	@Override
	public Parameter[] getParameters() {
		String currentTooltip = "";
		for (Node n : getSelectedOrAllNodes()) {
			String u = AttributeHelper.getToolTipText(n);
			if (u != null) {
				if (!(currentTooltip.length() == 0) && !currentTooltip.equals(u) && !currentTooltip.equals("http://"))
					currentTooltip = "~";
				else
					currentTooltip = u;
			}
		}
		return new Parameter[] { new StringParameter(currentTooltip, "Tooltip Text", null) };
	}
	
	@Override
	public void setParameters(Parameter[] params) {
		int i = 0;
		this.tooltip = ((StringParameter) params[i++]).getString();
	}
	
	public void execute() {
		if (tooltip == null)
			return;
		String tooltiptext = tooltip;
		if (!tooltiptext.equals("~")) {
			for (Node n : getSelectedOrAllNodes())
				AttributeHelper.setToolTipText(n, tooltiptext);
		}
	}
}
