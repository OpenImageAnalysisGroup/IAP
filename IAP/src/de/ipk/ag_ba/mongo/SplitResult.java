package de.ipk.ag_ba.mongo;

import java.util.ArrayList;
import java.util.HashSet;

import org.SystemAnalysis;

import de.ipk.ag_ba.server.task_management.TempDataSetDescription;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;

public class SplitResult {
	
	final ArrayList<ExperimentHeaderInterface> el;
	private final MongoDB m;
	
	public SplitResult(MongoDB m) {
		this.m = m;
		el = m.getExperimentList(null);
	}
	
	public HashSet<TempDataSetDescription> getSplitResultExperimentSets() {
		HashSet<TempDataSetDescription> availableTempDatasets = new HashSet<TempDataSetDescription>();
		HashSet<String> processedSubmissionTimes = new HashSet<String>();
		for (ExperimentHeaderInterface i : el) {
			if ((i.getExperimentType() + "").contains("Trash"))
				continue;
			String[] cc = i.getExperimentName().split("§");
			if (i.getImportusergroup() != null && i.getImportusergroup().equals("Temp") &&
					(cc.length == 4 || cc.length == 5)) {
				String className = cc[0];
				String idxCnt = cc[1];
				String partCnt = cc[2];
				String submTime = cc[3];
				String mergeWithDBid = cc.length == 5 ? cc[4] : "";
				if (!processedSubmissionTimes.contains(submTime)) {
					availableTempDatasets.add(new TempDataSetDescription(
							className, partCnt, submTime, i.getOriginDbId(), mergeWithDBid));
					System.out.println(SystemAnalysis.getCurrentTime() + "INFO: Found temp dataset: " + i.getExperimentName());
				}
				processedSubmissionTimes.add(submTime);
			}
		}
		return availableTempDatasets;
	}
	
	public ArrayList<ExperimentHeaderInterface> getAvailableTempDatasets() {
		ArrayList<ExperimentHeaderInterface> res = new ArrayList<ExperimentHeaderInterface>();
		for (ExperimentHeaderInterface ei : el) {
			if (ei.getExperimentName() == null || ei.getExperimentName().length() == 0 || ei.getExperimentName().contains("§")) {
				res.add(ei);
			}
		}
		return res;
	}
	
	public int deleteAvailableTempDatasets() throws Exception {
		int deletedTempDatasets = 0;
		for (ExperimentHeaderInterface ei : el) {
			if (ei.getExperimentName() == null || ei.getExperimentName().length() == 0 || ei.getExperimentName().contains("§")) {
				m.deleteExperiment(ei.getDatabaseId());
				deletedTempDatasets += 1;
			}
		}
		return deletedTempDatasets;
	}
	
}
