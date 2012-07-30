package de.ipk.ag_ba.datasources;

import org.BackgroundTaskStatusProviderSupportingExternalCall;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;

public interface ExperimentLoader {
	public ExperimentInterface getExperiment(ExperimentHeaderInterface experimentReq,
			boolean interactiveGetExperimentSize,
			BackgroundTaskStatusProviderSupportingExternalCall optStatus) throws Exception;
	
	public boolean canHandle(String databaseId);
}
