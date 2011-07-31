package de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.label_editing;

import org.AttributeHelper;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.MessageType;
import org.graffiti.graph.GraphElement;
import org.graffiti.graphics.GraphicAttributeConstants;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.parameter.BooleanParameter;
import org.graffiti.plugin.parameter.Parameter;

/**
 * @author rohn, klukas
 */
public class RemoveHiddenLabelsAlgorithm extends AbstractAlgorithm {
	
	private boolean removeOnlyHidden = true;
	
	public String getName() {
		return "Remove annotation labels";
	}
	
	@Override
	public void setParameters(Parameter[] params) {
		super.setParameters(params);
	}
	
	@Override
	public String getDescription() {
		return "This command removes annotation labels.";
	}
	
	@Override
	public Parameter[] getParameters() {
		return new Parameter[] { new BooleanParameter(removeOnlyHidden, "Remove only hidden labels", "If enabled, only hidden labels will be removed.") };
	}
	
	public void execute() {
		int deletedCnt = 0, cntges = 0;
		for (GraphElement ge : getSelectedOrAllGraphElements()) {
			int markdelete = deletedCnt;
			boolean doIt = true;
			for (int k = 1; k < 100; k++)
				if (AttributeHelper.hasAttribute(ge, GraphicAttributeConstants.LABELGRAPHICS + String.valueOf(k))) {
					if (removeOnlyHidden) {
						// doIt = anchor != hidden;
					}
					if (doIt) {
						ge.removeAttribute(GraphicAttributeConstants.LABELGRAPHICS + String.valueOf(k));
						deletedCnt++;
					}
				}
			if (markdelete != deletedCnt)
				cntges++;
		}
		if (deletedCnt == 0)
			MainFrame.showMessage("<html>No hidden labels removed", MessageType.PERMANENT_INFO);
		else
			MainFrame.showMessageDialog(
					"<html>" + deletedCnt + " hidden labels of " + cntges + " graphelements have been deleted from graph<p><i>" + graph.getName(), "Information");
	}
	
	@Override
	public boolean mayWorkOnMultipleGraphs() {
		return true;
	}
	
}
