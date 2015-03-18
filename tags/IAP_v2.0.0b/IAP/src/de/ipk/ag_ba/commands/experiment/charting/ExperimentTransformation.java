package de.ipk.ag_ba.commands.experiment.charting;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;

/**
 * @author klukas
 */
public interface ExperimentTransformation extends DirtyNotificationSupport {
	public ExperimentInterface transform(ExperimentInterface input);
}
