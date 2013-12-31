package de.ipk.ag_ba.plugins.scripts;

import java.util.ArrayList;

import de.ipk.ag_ba.commands.experiment.scripts.Rinfo.ScriptRinfo;
import de.ipk.ag_ba.commands.experiment.scripts.clustering.ScriptPVCLUST;
import de.ipk.ag_ba.commands.experiment.scripts.diagrams.ScriptPLOT;
import de.ipk.ag_ba.commands.experiment.view_or_export.ActionScriptBasedDataProcessing;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.plugins.AbstractIAPplugin;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.threading.SystemAnalysis;

/**
 * @author Christian Klukas
 */
public class PluginIAPscripts extends AbstractIAPplugin {
	public PluginIAPscripts() {
		System.out.println(SystemAnalysis.getCurrentTime() + ">INFO: IAP default scripts plugin is beeing loaded");
	}
	
	@Override
	public ActionScriptBasedDataProcessing[] getScriptBasedDataProcessingTools(ExperimentReference experimentReference) {
		ArrayList<ActionScriptBasedDataProcessing> result = new ArrayList<ActionScriptBasedDataProcessing>();
		// boolean addRIcon = SystemOptions.getInstance().getBoolean("File Import", "Show Load Files Icon", true);
		result.add(new ScriptRinfo());
		result.add(new ScriptPVCLUST());
		result.add(new ScriptPLOT());
		return result.toArray(new ActionScriptBasedDataProcessing[] {});
	}
}