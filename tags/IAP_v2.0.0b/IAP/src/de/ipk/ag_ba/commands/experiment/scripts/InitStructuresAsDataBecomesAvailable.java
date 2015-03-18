package de.ipk.ag_ba.commands.experiment.scripts;

import java.util.HashMap;
import java.util.HashSet;

import org.graffiti.plugin.algorithm.ThreadSafeOptions;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Condition.ConditionInfo;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;

/**
 * @author klukas
 */
public final class InitStructuresAsDataBecomesAvailable implements Runnable {
	private final AbstractRscriptExecutionAction abstractRscriptExecutionAction;
	
	/**
	 * @param abstractRscriptExecutionAction
	 */
	InitStructuresAsDataBecomesAvailable(AbstractRscriptExecutionAction abstractRscriptExecutionAction) {
		this.abstractRscriptExecutionAction = abstractRscriptExecutionAction;
	}
	
	@Override
	public void run() {
		HashMap<ConditionInfo, HashSet<String>> ci2vs = new HashMap<ConditionInfo, HashSet<String>>();
		ExperimentInterface ei = this.abstractRscriptExecutionAction.experimentReference.getExperimentPeek();
		HashSet<String> plantIDs = new HashSet<String>();
		for (SubstanceInterface si : ei) {
			for (ConditionInterface c : si) {
				for (SampleInterface sai : c) {
					for (NumericMeasurementInterface nmi : sai) {
						plantIDs.add(nmi.getQualityAnnotation());
					}
				}
				for (ThreadSafeOptions tso : this.abstractRscriptExecutionAction.metaDataColumns) {
					ConditionInfo ci = (ConditionInfo) tso.getParam(1, null);
					if (ci == ConditionInfo.IGNORED_FIELD)
						continue;
					if (!ci2vs.containsKey(ci))
						ci2vs.put(ci, new HashSet<String>());
					String v = c.getField(ci);
					if (v != null && !v.isEmpty()) {
						ci2vs.get(ci).add(v);
					}
				}
			}
		}
		ci2vs.put(ConditionInfo.IGNORED_FIELD, plantIDs);
		int selCnt = 0;
		for (ThreadSafeOptions tso : this.abstractRscriptExecutionAction.metaDataColumns) {
			ConditionInfo ci = (ConditionInfo) tso.getParam(1, null);
			int n = ci2vs.get(ci).size();
			String os = (String) tso.getParam(10, "");
			if (os.contains("("))
				os = os.substring(0, os.indexOf("(")).trim();
			tso.setParam(10, os + " (" + n + ")");
			tso.setInt(n);
			if (n < 2 || (ci == ConditionInfo.IGNORED_FIELD && selCnt > 0))
				tso.setBval(0, false);
			else
				selCnt++;
		}
	}
}