package de.ipk.ag_ba.commands.experiment.charting;

import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.gui.util.ExperimentReferenceInterface;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;

/**
 * @author klukas
 */
public class ExperimentReferenceWithFilterSupport extends ExperimentReference {
	
	public ExperimentReferenceWithFilterSupport(ExperimentHeaderInterface ehi, MongoDB m) {
		super(ehi, m);
	}
	
	public ExperimentReferenceWithFilterSupport(ExperimentReferenceInterface experiment) {
		super(experiment.getHeader(), experiment.getM());
		ExperimentInterface e = experiment.getExperimentPeek();
		if (e != null)
			this.experiment = e;
	}
}
