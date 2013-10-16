package de.ipk.ag_ba.commands.mongodb;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;

public class ExperimentHeaderWithCustomToString {
	
	private final ExperimentHeaderInterface eh;
	
	public ExperimentHeaderWithCustomToString(ExperimentHeaderInterface eh) {
		this.eh = eh;
	}
	
	@Override
	public String toString() {
		return "<html><b>" + eh.getExperimentName() + "</b> (source: " + eh.getOriginDbId() + ")";
	}
	
	public ExperimentHeaderInterface getHeader() {
		return eh;
	}
}
