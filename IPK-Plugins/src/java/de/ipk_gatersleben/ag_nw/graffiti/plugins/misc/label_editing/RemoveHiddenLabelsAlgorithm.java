package de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.label_editing;

import org.AttributeHelper;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.MessageType;
import org.graffiti.graph.GraphElement;
import org.graffiti.graphics.GraphicAttributeConstants;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;

/**
 * @author rohn
 */
public class RemoveHiddenLabelsAlgorithm extends AbstractAlgorithm {
	
	public String getName() {
		return "Remove hidden labels";
	}
	
	public void execute() {
		int deletedCnt = 0, cntges = 0;
		for (GraphElement ge : getSelectedOrAllGraphElements()) {
			int markdelete = deletedCnt;
			for (int k = 1; k < 100; k++)
				if (AttributeHelper.hasAttribute(ge, GraphicAttributeConstants.LABELGRAPHICS + String.valueOf(k))) {
					ge.removeAttribute(GraphicAttributeConstants.LABELGRAPHICS + String.valueOf(k));
					deletedCnt++;
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
