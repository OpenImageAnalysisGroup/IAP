package de.ipk.ag_ba.plugins.actions;

import java.util.ArrayList;

import de.ipk.ag_ba.commands.experiment.tools.ActionObjectStatistics;
import de.ipk.ag_ba.commands.experiment.tools.ActionPerformanceTest;
import de.ipk.ag_ba.commands.experiment.tools.ActionRemerge;
import de.ipk.ag_ba.commands.experiment.tools.ActionResetConditionFromImageName;
import de.ipk.ag_ba.commands.experiment.tools.ActionSaveWebCamImagesSelectSource;
import de.ipk.ag_ba.commands.experiment.tools.ActionShowXML;
import de.ipk.ag_ba.commands.experiment.tools.ActionSortSubstances;
import de.ipk.ag_ba.commands.experiment.tools.ActionTestMongoIoReadSpeed;
import de.ipk.ag_ba.commands.experiment.view_or_export.ActionDataProcessing;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.plugins.AbstractIAPplugin;
import de.ipk.ag_ba.plugins.outlier.OutlierAnalysis;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.threading.SystemAnalysis;

/**
 * @author Christian Klukas
 */
public class PluginIAPexperimentToolActions extends AbstractIAPplugin {
	public PluginIAPexperimentToolActions() {
		System.out.println(SystemAnalysis.getCurrentTime() + ">INFO: IAP experiment tools plugin is beeing loaded");
	}
	
	@SuppressWarnings("unused")
	@Override
	public ActionDataProcessing[] getDataProcessingTools(ExperimentReference experimentReference) {
		ArrayList<ActionDataProcessing> res = new ArrayList<ActionDataProcessing>();
		
		res.add(new ActionPerformanceTest());
		res.add(new ActionSortSubstances());
		res.add(new ActionRemerge());
		res.add(new ActionResetConditionFromImageName());
		res.add(new ActionShowXML());
		res.add(new ActionObjectStatistics());
		res.add(new ActionSaveWebCamImagesSelectSource());
		if (false)
			res.add(new ActionTestMongoIoReadSpeed());
		
		res.add(new OutlierAnalysis());
		
		if (experimentReference != null)
			for (ActionDataProcessing adp : res)
				adp.setExperimentReference(experimentReference);
		
		return res.toArray(new ActionDataProcessing[] {});
	}
}