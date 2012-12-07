/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 31.08.2004 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.circle;

import org.graffiti.editor.MainFrame;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;

import de.ipk_gatersleben.ag_nw.graffiti.services.AlgorithmServices;

/**
 * @author Christian Klukas
 *         (c) 2004 IPK-Gatersleben
 */
public class CountCircularCrossingsAlgorithm extends AbstractAlgorithm {
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.extension.Extension#getName()
	 */
	public String getName() {
		return "Count Circular Edge Crossings";
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.extension.Extension#getCategory()
	 */
	@Override
	public String getCategory() {
		return "Analysis";
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.Algorithm#execute()
	 */
	public void execute() {
		if (selection.isEmpty())
			selection.addAll(graph.getNodes());
		int crossings = AlgorithmServices.getNumberOfCircularEdgeCrossings(selection.getNodes());
		MainFrame.showMessageDialog(
							"Number of edge-crossings (in case of a circular layout of the selection) is " + crossings + ".",
							"Edge-crossings of selected or all nodes");
	}
	
}
