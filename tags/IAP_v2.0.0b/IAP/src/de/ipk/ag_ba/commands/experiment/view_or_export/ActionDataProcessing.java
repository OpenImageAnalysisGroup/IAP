package de.ipk.ag_ba.commands.experiment.view_or_export;

import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.util.ExperimentReferenceInterface;

/**
 * @author Christian Klukas
 */
public interface ActionDataProcessing extends NavigationAction {
	boolean isImageAnalysisCommand();
	
	void setExperimentReference(ExperimentReferenceInterface experimentReference);
}
