package de.ipk_gatersleben.ag_nw.graffiti.plugins.algorithms.davidtest;

import org.graffiti.editor.MainFrame;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.Algorithm;

public class ShowStatisticsTab extends AbstractAlgorithm implements Algorithm {
	
	public void execute() {
		MainFrame.getInstance().showAndHighlightSidePanelTab("Statistics", true);
	}
	
	public String getName() {
		return "Correlation analysis, compare sample averages (e.g. t-Test)";
	}
	
}
