package de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.label_editing;

import org.AttributeHelper;
import org.Release;
import org.ReleaseInfo;
import org.StringManipulationTools;
import org.graffiti.graph.GraphElement;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.plugin.parameter.StringParameter;

public class RemoveParenthesisLabels extends AbstractAlgorithm {
	
	String tagA = "(";
	String tagB = ")";
	
	public void execute() {
		graph.getListenerManager().transactionStarted(this);
		try {
			for (GraphElement ge : getSelectedOrAllGraphElements()) {
				String lbl = AttributeHelper.getLabel(ge, "");
				if (lbl.length() > 0) {
					AttributeHelper.setAttribute(ge, "", "oldlabel", lbl);
					lbl = StringManipulationTools.removeTags(lbl, tagA, tagB).trim();
					AttributeHelper.setLabel(ge, lbl);
				}
			}
		} finally {
			graph.getListenerManager().transactionFinished(this);
		}
	}
	
	@Override
	public String getDescription() {
		return "<html>" +
							"Label parts between tags<br>" +
							"(and tags) will be removed.";
	}
	
	@Override
	public Parameter[] getParameters() {
		return new Parameter[] {
							new StringParameter(tagA, "Tag A", null),
							new StringParameter(tagB, "Tag B", null) };
	}
	
	@Override
	public void setParameters(Parameter[] params) {
		int i = 0;
		tagA = ((StringParameter) params[i++]).getString();
		tagB = ((StringParameter) params[i++]).getString();
	}
	
	@Override
	public String getCategory() {
		if (ReleaseInfo.getRunningReleaseStatus() == Release.KGML_EDITOR)
			return "Nodes";
		else
			return null;// "Elements";
	}
	
	public String getName() {
		if (ReleaseInfo.getRunningReleaseStatus() == Release.KGML_EDITOR)
			return null;
		else
			return "Remove parts of labels...";
	}
	
	@Override
	public boolean mayWorkOnMultipleGraphs() {
		return true;
	}
}
