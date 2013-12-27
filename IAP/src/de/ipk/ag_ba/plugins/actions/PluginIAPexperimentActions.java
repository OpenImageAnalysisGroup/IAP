package de.ipk.ag_ba.plugins.actions;

import java.util.ArrayList;

import de.ipk.ag_ba.commands.experiment.ActionAnalysis;
import de.ipk.ag_ba.commands.experiment.ActionCmdLineTools;
import de.ipk.ag_ba.commands.experiment.ActionCopyExperiment;
import de.ipk.ag_ba.commands.experiment.ActionPdfReport;
import de.ipk.ag_ba.commands.experiment.ActionShowDataWithinVANTED;
import de.ipk.ag_ba.commands.experiment.ActionToolList;
import de.ipk.ag_ba.commands.experiment.ActionViewExportData;
import de.ipk.ag_ba.commands.experiment.clipboard.ActionCopyToClipboard;
import de.ipk.ag_ba.commands.experiment.process.ActionNumericDataReport;
import de.ipk.ag_ba.commands.experiment.view_or_export.ActionDataProcessing;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.gui.webstart.IAPmain;
import de.ipk.ag_ba.gui.webstart.IAPrunMode;
import de.ipk.ag_ba.plugins.AbstractIAPplugin;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.AbstractExperimentDataProcessor;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.ExperimentDataProcessingManager;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.ExperimentDataProcessor;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.PutIntoSidePanel;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.threading.SystemAnalysis;

/**
 * @author Christian Klukas
 */
public class PluginIAPexperimentActions extends AbstractIAPplugin {
	public PluginIAPexperimentActions() {
		System.out.println(SystemAnalysis.getCurrentTime() + ">INFO: IAP experiment action list plugin is beeing loaded");
	}
	
	@SuppressWarnings("unused")
	@Override
	public ActionDataProcessing[] getDataProcessingActions(ExperimentReference experimentReference) {
		ArrayList<ActionDataProcessing> actions = new ArrayList<ActionDataProcessing>();
		
		actions.add(new ActionViewExportData());
		if (experimentReference.getIniIoProvider() != null && experimentReference.getIniIoProvider().isAbleToSaveData())
			actions.add(new ActionAnalysis("Analysis Tasks"));
		actions.add(new ActionPdfReport("Automated diagram creation and report generation"));
		
		if (IAPmain.getRunMode() == IAPrunMode.WEB) {
			actions.add(new ActionNumericDataReport());
		}
		
		actions.add(new ActionCopyExperiment());
		actions.add(new ActionToolList("Various tools and less-often used data manipulation commands"));
		actions.add(new ActionCmdLineTools("Script-commands for data evaluation and filtering"));
		actions.add(new ActionCopyToClipboard());
		
		ActionDataProcessing action = new ActionShowDataWithinVANTED("Show in IAP-Data-Navigator", new PutIntoSidePanel(false));
		actions.add(action);
		
		if (false)
			for (ActionDataProcessing ne : getProcessExperimentDataWithVantedEntities()) {
				actions.add(ne);
			}
		
		if (experimentReference != null)
			for (ActionDataProcessing adp : actions)
				adp.setExperimentReference(experimentReference);
		
		return actions.toArray(new ActionDataProcessing[] {});
	}
	
	public static ArrayList<ActionDataProcessing> getProcessExperimentDataWithVantedEntities() {
		ArrayList<ActionDataProcessing> result = new ArrayList<ActionDataProcessing>();
		
		ArrayList<AbstractExperimentDataProcessor> validProcessors = new ArrayList<AbstractExperimentDataProcessor>();
		ArrayList<AbstractExperimentDataProcessor> optIgnoredProcessors = null;
		for (ExperimentDataProcessor ep : ExperimentDataProcessingManager.getExperimentDataProcessors())
			// check if ep is not ignored
			if (optIgnoredProcessors == null || !optIgnoredProcessors.contains(ep.getClass())) {
				validProcessors.add((AbstractExperimentDataProcessor) ep);
			}
		
		for (AbstractExperimentDataProcessor pp : validProcessors) {
			ActionDataProcessing action = new ActionShowDataWithinVANTED("Analyze Data", pp);
			result.add(action);
		}
		
		return result;
	}
	
}