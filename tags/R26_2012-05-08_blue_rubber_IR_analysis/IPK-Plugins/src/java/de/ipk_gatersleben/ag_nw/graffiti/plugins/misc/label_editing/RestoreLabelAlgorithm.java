/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 17.01.2005 by Christian Klukas
 * (c) 2005 IPK Gatersleben, Group Network Analysis
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.label_editing;

import org.AttributeHelper;
import org.Release;
import org.ReleaseInfo;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.Edge;
import org.graffiti.graph.GraphElement;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;

/**
 * @author Christian Klukas
 *         (c) 2006, 2007 IPK Gatersleben, Group Network Analysis
 */
public class RestoreLabelAlgorithm extends AbstractAlgorithm {
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.Algorithm#getName()
	 */
	public String getName() {
		return "Restore labels...";
	}
	
	@Override
	public String getDescription() {
		return "<html>" +
							"Some commands offer a option to save current label text<br>" +
							"before a change to the label is done.<br>" +
							"If such a command has been executed before, this command will use<br>" +
							"'oldlabel' graph element attribute information to restore the previous<br>" +
							"label text.";
	}
	
	@Override
	public String getCategory() {
		return null;// "Elements";
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.Algorithm#execute()
	 */
	public void execute() {
		int idCnt = 0;
		for (GraphElement ge : getSelectedOrAllGraphElements()) {
			if (ReleaseInfo.getRunningReleaseStatus() == Release.KGML_EDITOR && ge instanceof Edge)
				continue;
			String oldlabel = (String) AttributeHelper.getAttributeValue(ge, "", "oldlabel", null, "");
			if (oldlabel != null) {
				AttributeHelper.setLabel(ge, oldlabel);
				idCnt++;
			}
		}
		MainFrame.showMessageDialog(idCnt + " labels have been restored", "Information");
	}
	
	@Override
	public boolean mayWorkOnMultipleGraphs() {
		return true;
	}
}
