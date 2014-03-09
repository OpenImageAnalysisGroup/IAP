package de.ipk.ag_ba.commands.experiment;

import java.util.ArrayList;

import org.graffiti.plugin.algorithm.ThreadSafeOptions;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.commands.experiment.process.report.ActionPdfCreation3;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;

/**
 * @author klukas
 */
final class ActionNumericExportCommands extends AbstractNavigationAction {
	private final ArrayList<ThreadSafeOptions> toggles;
	private final ExperimentReference experiment;
	
	ActionNumericExportCommands(
			String tooltip, ArrayList<ThreadSafeOptions> toggles,
			ExperimentReference experiment) {
		super(tooltip);
		this.toggles = toggles;
		this.experiment = experiment;
	}
	
	@Override
	public String getDefaultTitle() {
		return "Export Numeric Data";
	}
	
	@Override
	public String getDefaultImage() {
		return "img/colorhistogram.png";
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		// empty
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		ArrayList<NavigationButton> res = new ArrayList<NavigationButton>();
		res.add(new NavigationButton(
				new ActionPdfCreation3(
						experiment,
						toggles,
						false,
						true,
						null, null, null, null, null, null, ExportSetting.ALL),
				guiSetting));
	
		res.add(new NavigationButton(
				new ActionPdfCreation3(
						experiment,
						toggles,
						true,
						false,
						null, null, null, null, null),
				guiSetting));
		return res;
	}
}