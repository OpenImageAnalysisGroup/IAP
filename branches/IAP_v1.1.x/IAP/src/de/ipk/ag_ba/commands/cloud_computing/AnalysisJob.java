package de.ipk.ag_ba.commands.cloud_computing;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;

public class AnalysisJob {
	
	private final ExperimentHeaderInterface eh;
	private final AnalysisStatus status;
	
	public AnalysisJob(ExperimentHeaderInterface eh, AnalysisStatus status) {
		this.eh = eh;
		this.status = status;
	}
	
	public ExperimentHeaderInterface getExperimentHeader() {
		return eh;
	}
	
	public AnalysisStatus getAnalysisStatus() {
		return status;
	}
	
}
