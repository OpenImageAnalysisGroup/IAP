package de.ipk.ag_ba.mongo;

import info.StopWatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.SystemAnalysis;
import org.SystemOptions;
import org.bson.types.ObjectId;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;

import de.ipk.ag_ba.gui.images.IAPexperimentTypes;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.gui.util.IAPservice;
import de.ipk.ag_ba.server.task_management.BatchCmd;
import de.ipk.ag_ba.server.task_management.CloudAnalysisStatus;
import de.ipk.ag_ba.server.task_management.TempDataSetDescription;
import de.ipk_gatersleben.ag_nw.graffiti.MyInputHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Experiment;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;
import de.ipk_gatersleben.ag_nw.graffiti.services.BackgroundTaskConsoleLogger;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.DataMappingTypeManager3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.MappingData3DPath;

public class SplitResult {
	
	final ArrayList<ExperimentHeaderInterface> el;
	private final MongoDB m;
	
	public SplitResult(MongoDB m) {
		this.m = m;
		el = m.getExperimentList(null);
	}
	
	public HashSet<TempDataSetDescription> getSplitResultExperimentSets(BatchCmd optRefCmd) {
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
				if (optRefCmd != null && !(optRefCmd.getSubmissionTime() + "").equals(submTime))
					continue;
				String mergeWithDBid = cc.length == 5 ? cc[4] : "";
				if (!processedSubmissionTimes.contains(submTime)) {
					availableTempDatasets.add(new TempDataSetDescription(
							className, partCnt, submTime, i.getOriginDbId(), mergeWithDBid));
					System.out.println(SystemAnalysis.getCurrentTime() + ">INFO: Found temp dataset: " + i.getExperimentName());
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
	
	private void doMerge(TempDataSetDescription tempDataSetDescription,
			ArrayList<ExperimentHeaderInterface> knownResults, boolean interactive, BackgroundTaskStatusProviderSupportingExternalCall optStatus,
			ExperimentReference optPreviousResultsToBeMerged) throws Exception {
		System.out.println("*****************************");
		System.out.println("MERGE INDEX: " + tempDataSetDescription.getPartCntI() + "/" + tempDataSetDescription.getPartCnt()
				+ ", RESULTS AVAILABLE: " + knownResults.size());
		
		ExperimentInterface e = new Experiment();
		
		if (optPreviousResultsToBeMerged != null) {
			String msg = "Previous analysis results, to be merged with current result, will now be loaded (" + optPreviousResultsToBeMerged.getExperimentName()
					+ ","
					+ optPreviousResultsToBeMerged.getHeader().getImportdate() + ")";
			System.out.println(msg);
			MongoDB.saveSystemMessage(msg);
			e = optPreviousResultsToBeMerged.getData();
		}
		
		long tFinish = System.currentTimeMillis();
		final int wl = knownResults.size();
		final ThreadSafeOptions tso = new ThreadSafeOptions();
		final Runtime r = Runtime.getRuntime();
		HashMap<ExperimentHeaderInterface, String> experiment2id =
				new HashMap<ExperimentHeaderInterface, String>();
		String originName = null;
		int nnii = 1;
		ExperimentHeaderInterface sourceHeader = null;
		for (ExperimentHeaderInterface ii : knownResults) {
			experiment2id.put(ii, ii.getDatabaseId());
			nnii++;
			if (sourceHeader == null) {
				ExperimentReference eRef = new ExperimentReference(ii.getOriginDbId());
				ExperimentHeaderInterface oriH = eRef.getHeader();
				sourceHeader = oriH;
			}
			if (originName == null) {
				ExperimentReference eRef = new ExperimentReference(ii.getOriginDbId());
				ExperimentHeaderInterface oriH = eRef.getHeader();
				
				String[] cc = ii.getExperimentName().split("§");
				String ana = cc[0];
				ana = ana.substring(ana.lastIndexOf(".") + ".".length());
				originName = ana + ": " + oriH.getExperimentName();
				if (optStatus != null)
					optStatus.setCurrentStatusText1("Merge " + knownResults.size() + " parts (" + originName + ")");
				MongoDB.saveSystemMessage(m, "Start combining analysis results for " + originName + " (" + knownResults.size() + " parts)");
			}
			if (r.freeMemory() * 0.8 > r.maxMemory()) {
				System.out.print(SystemAnalysis.getCurrentTime() + ">" + r.freeMemory() / 1024 / 1024 + " MB free, " + r.totalMemory() / 1024
						/ 1024
						+ " total MB, " + r.maxMemory() / 1024 / 1024 + " max MB>");
			}
			StopWatch s1 = new StopWatch(">m.getExperiment");
			BackgroundTaskConsoleLogger status = new BackgroundTaskConsoleLogger("", "", false);
			ExperimentInterface ei = m.getExperiment(ii, false, status);
			if (s1.getTime() > 30000)
				s1.printTime();
			String[] cc = ii.getExperimentName().split("§");
			tso.addInt(1);
			String msg = "loading: " + s1.getTime() + " ms";
			System.out.print(SystemAnalysis.getCurrentTime() + ">INFO: " + (tso.getInt() + 1) + "/" + wl + " // dataset: " + cc[1] + "/" + cc[2]
					+ " // loaded in " + s1.getTime() + " ms");
			if (optStatus != null)
				optStatus.setCurrentStatusValueFine(100d * tso.getInt() / wl);
			// for (String c : condS)
			// System.out.println(">Condition: " + c);
			
			StopWatch s = new StopWatch(">e.addMerge");
			e.addAndMerge(ei);
			System.out.println(" // merged in " + s.getTime() + " ms");
			if (optStatus != null)
				optStatus.setCurrentStatusText2("Processed " + (nnii - 1) + "/" + tempDataSetDescription.getPartCntI() + "<br>" +
						"<small><font color='gray'>(" + msg + ", merging: " + s.getTime() + " ms)</font></small>");
			
			if (s.getTime() > 30000)
				s.printTime();
		}
		if (optStatus != null)
			optStatus.setCurrentStatusText2("Merged data, prepare saving...");
		String sn = tempDataSetDescription.getRemoteCapableAnalysisActionClassName();
		if (sn.indexOf(".") > 0)
			sn = sn.substring(sn.lastIndexOf(".") + 1);
		e.getHeader().setDatabaseId("");
		for (SubstanceInterface si : e) {
			for (ConditionInterface ci : si) {
				ci.setExperimentName(e.getHeader().getExperimentName());
				ci.setExperimentType(IAPexperimentTypes.AnalysisResults + "");
			}
		}
		boolean superMerge = false;
		if (superMerge) {
			if (optStatus != null)
				optStatus.setCurrentStatusText1("Get mapping path objects");
			System.out.println(SystemAnalysis.getCurrentTime() + ">GET MAPPING PATH OBJECTS...");
			ArrayList<MappingData3DPath> mdpl = MappingData3DPath.get(e, false);
			e.clear();
			if (optStatus != null)
				optStatus.setCurrentStatusText1("Mapping path obhjects to experiment");
			System.out.println(SystemAnalysis.getCurrentTime() + ">MERGE " + mdpl.size() + " MAPPING PATH OBJECTS TO EXPERIMENT...");
			e = MappingData3DPath.merge(mdpl, false);
			if (optStatus != null)
				optStatus.setCurrentStatusText1("Created unified experiment");
			System.out.println(SystemAnalysis.getCurrentTime() + ">UNIFIED EXPERIMENT CREATED");
		}
		long tStart = tempDataSetDescription.getSubmissionTimeL();
		long tProcessing = tFinish - tStart;
		int nToDo = tempDataSetDescription.getPartCntI();
		int nFinish = knownResults.size();
		e.getHeader().setExperimentname(originName);
		e.getHeader().setExperimenttype(IAPexperimentTypes.AnalysisResults + "");
		e.getHeader().setImportusergroup(IAPexperimentTypes.AnalysisResults + "");
		{
			e.getHeader().setCoordinator(sourceHeader.getCoordinator());
			e.getHeader().setImportusername(sourceHeader.getImportusername());
			e.getHeader().setOriginDbId(sourceHeader.getDatabaseId());
			e.getHeader().setDatabase(sourceHeader.getDatabase());
		}
		e.getHeader().setRemark(
				e.getHeader().getRemark() +
						" // " + nFinish + " compute tasks finished // " + nToDo + " jobs scheduled at  " + SystemAnalysis.getCurrentTime(tStart) +
						" // processing time: " +
						SystemAnalysis.getWaitTime(tProcessing) + " // split results merged: " +
						SystemAnalysis.getCurrentTime());
		System.out.println("> T=" + IAPservice.getCurrentTimeAsNiceString());
		System.out.println("> PIPELINE PROCESSING TIME =" + SystemAnalysis.getWaitTime(tProcessing));
		System.out.println("*****************************");
		System.out.println("Merged Experiment: " + e.getName());
		System.out.println("Merged Measurements: " + e.getNumberOfMeasurementValues());
		
		System.out.println("> SAVE COMBINED EXPERIMENT...");
		m.saveExperiment(e, optStatus == null ? new BackgroundTaskConsoleLogger("", "", true) : optStatus, true, true);
		System.out.println("> COMBINED EXPERIMENT HAS BEEN SAVED");
		if (optStatus != null)
			optStatus.setCurrentStatusText1("Saved combined experiment " + e.getName());
		if (optStatus != null)
			optStatus.setCurrentStatusText2("Merge-time: " + SystemAnalysis.getWaitTime(System.currentTimeMillis() - tFinish));
		MongoDB.saveSystemMessage("Saved combined experiment " + e.getName() +
				" merging data took " +
				SystemAnalysis.getWaitTime(System.currentTimeMillis() - tFinish));
		// System.out.println("> DELETE TEMP DATA IS DISABLED!");
		// System.out.println("> DELETE TEMP DATA...");
		boolean deleteAfterMerge = SystemOptions.getInstance().getBoolean("IAP", "grid_auto_delete_temp_results", true);
		if (!deleteAfterMerge) {
			System.out.println("> MARK TEMP DATA AS TRASHED...");
			if (optStatus != null)
				optStatus.setCurrentStatusText1("Mark temp data as trashed");
		} else {
			System.out.println("> DELETE TEMP DATA...");
			if (optStatus != null)
				optStatus.setCurrentStatusText1("Delete temp data");
		}
		
		int idx = 0;
		int maxIdx = knownResults.size();
		for (ExperimentHeaderInterface i : knownResults) {
			try {
				if (i.getDatabaseId() != null && i.getDatabaseId().length() > 0) {
					ExperimentHeaderInterface hhh = m.getExperimentHeader(new ObjectId(experiment2id.get(i)));
					if (!deleteAfterMerge)
						m.setExperimentType(hhh, "Trash" + ";" + hhh.getExperimentType());
					else
						m.deleteExperiment(i.getDatabaseId());
				}
				if (optStatus != null)
					optStatus.setCurrentStatusValueFine(100d * (idx++) / maxIdx);
				
			} catch (Exception err) {
				if (!deleteAfterMerge)
					MongoDB.saveSystemErrorMessage("Could not mark experiment " + i.getExperimentName() +
							" as trashed", err);
				else
					MongoDB.saveSystemErrorMessage("Could not delete experiment " + i.getExperimentName(), err);
				System.out.println("Could not " + (deleteAfterMerge ? "delete" : "set delete-mark on") + " experiment " + i.getExperimentName() + " (" +
						err.getMessage() + ")");
			}
		}
		if (optStatus != null)
			optStatus.setCurrentStatusText1("Saved combined experiment " + e.getName());
		if (optStatus != null)
			optStatus.setCurrentStatusText2("Time to completion: " + SystemAnalysis.getWaitTime(System.currentTimeMillis() - tFinish));
		if (optStatus != null)
			optStatus.setCurrentStatusValueFine(100d);
		if (optStatus != null)
			optStatus.setCurrentStatusText2("Completed in " + SystemAnalysis.getWaitTime(System.currentTimeMillis() - tFinish) + "");
		System.out.println(SystemAnalysis.getCurrentTime() + "> COMPLETED");
	}
	
	public void merge(boolean interactive, BackgroundTaskStatusProviderSupportingExternalCall optStatus) throws Exception {
		merge(interactive, optStatus, null);
	}
	
	public void merge(boolean interactive, BackgroundTaskStatusProviderSupportingExternalCall optStatus, BatchCmd optRefCmd) throws Exception {
		DataMappingTypeManager3D.replaceVantedMappingTypeManager();
		
		HashSet<TempDataSetDescription> availableTempDatasets = getSplitResultExperimentSets(optRefCmd);
		for (TempDataSetDescription tempDataSetDescription : availableTempDatasets) {
			ArrayList<ExperimentHeaderInterface> knownResults = new ArrayList<ExperimentHeaderInterface>();
			HashSet<String> added = new HashSet<String>();
			for (ExperimentHeaderInterface i : el) {
				if (i.getExperimentName() != null && i.getExperimentName().contains("§")) {
					String[] cc = i.getExperimentName().split("§");
					if (i.getImportusergroup().equals("Temp") &&
							(cc.length == 4 || cc.length == 5)) {
						String className = cc[0];
						String partIdx = cc[1];
						String partCnt = cc[2];
						String submTime = cc[3];
						String mergeWithDBid = cc.length == 5 ? cc[4] : "";
						String bcn = tempDataSetDescription.getRemoteCapableAnalysisActionClassName();
						String bpn = tempDataSetDescription.getPartCnt();
						String bst = tempDataSetDescription.getSubmissionTime();
						String bmw = tempDataSetDescription.getMergeWithDBid();
						if (className.equals(bcn)
								&& partCnt.equals(bpn)
								&& submTime.equals(bst)
								&& !added.contains(partIdx)
								&& mergeWithDBid.equals(bmw)) {
							knownResults.add(i);
							added.add(partIdx);
						}
					}
				}
			}
			System.out.println(SystemAnalysis.getCurrentTime() + "> T=" + IAPservice.getCurrentTimeAsNiceString());
			System.out.println(SystemAnalysis.getCurrentTime() + "> TODO: " + tempDataSetDescription.getPartCntI() + ", FINISHED: "
					+ knownResults.size());
			{
				// check if source data is still available
				ExperimentHeaderInterface firstExp = knownResults.iterator().next();
				String oid = firstExp.getOriginDbId();
				ExperimentReference eRef = new ExperimentReference(oid);
				ExperimentHeaderInterface eRefHeader = eRef.getHeader();
				if (eRefHeader == null) {
					// source data set has been deleted meanwhile
					// therefore the analysis results will be deleted, too
					String msg = "Source data set for analysis results has been deleted, about to delete non-relevant result data (" + knownResults.size() + "/" +
							tempDataSetDescription.getPartCntI() + " parts)";
					System.out.println(msg);
					MongoDB.saveSystemMessage(msg);
					for (ExperimentHeaderInterface r : new ArrayList<ExperimentHeaderInterface>(knownResults)) {
						System.out.println(SystemAnalysis.getCurrentTime() + ">INFO: Delete " + r.getExperimentName());
						m.deleteExperiment(r.getDatabaseId());
						knownResults.remove(r);
					}
					continue;
				}
			}
			ExperimentReference optPreviousResultsToBeMerged = null;
			{
				// check if result data needs to be merged with previous calculation results
				String[] cc = knownResults.iterator().next().getExperimentName().split("§");
				String mergeWithDBid = cc.length == 5 ? cc[4] : "";
				if (!mergeWithDBid.isEmpty() && !mergeWithDBid.equals("null")) {
					ExperimentReference eRef = new ExperimentReference(mergeWithDBid);
					ExperimentHeaderInterface eRefHeader = eRef.getHeader();
					if (eRefHeader == null) {
						// source data set has been deleted meanwhile
						// therefore the analysis results will be deleted, too
						String msg = "Result data set to be merged with analysis results has been deleted, about to delete non-relevant result data ("
								+ knownResults.size() + "/" +
								tempDataSetDescription.getPartCntI() + " parts)";
						System.out.println(msg);
						MongoDB.saveSystemMessage(msg);
						for (ExperimentHeaderInterface r : knownResults) {
							m.deleteExperiment(r.getDatabaseId());
						}
						continue;
					} else {
						optPreviousResultsToBeMerged = eRef;
					}
				}
			}
			
			boolean addNewTasksIfMissing = false;
			Object[] res;
			if (interactive) {
				if (optStatus != null)
					optStatus.setCurrentStatusText1("User input required");
				if (SystemAnalysis.isHeadless()) {
					System.out.println(SystemAnalysis.getCurrentTime() + ">Analyzed experiment: "
							+ new ExperimentReference(knownResults.iterator().next().getOriginDbId()).getHeader().getExperimentName());
					if (tempDataSetDescription.getPartCntI() == knownResults.size()) {
						res = new Object[] { false };
					} else {
						System.out.println(SystemAnalysis.getCurrentTime() + ">Process incomplete data sets? TODO: " + tempDataSetDescription.getPartCntI()
								+ ", FINISHED: "
								+ knownResults.size());
						System.out.println("Add compute tasks for missing data? (ENTER yes/no)");
						String in = SystemAnalysis.getCommandLineInput();
						if (in == null)
							res = null;
						else
							if (in.toUpperCase().contains("Y"))
								res = new Object[] { true };
							else
								res = new Object[] { false };
					}
				} else
					res = MyInputHelper.getInput("<html>Process this data set? "
							+
							(knownResults.size() > 0 ?
									"<br>Analyzed experiment: "
											+ new ExperimentReference(knownResults.iterator().next().getOriginDbId()).getHeader().getExperimentName()
									: "")
							+ "<br>Required result sets: " + tempDataSetDescription.getPartCntI() + ", completed calculations: "
							+ knownResults.size() + (knownResults.size() == tempDataSetDescription.getPartCntI() ? " (all tasks completed)" : " (results missing)")
							+ "<br>"
							+ "<br>Click cancel to stop processing this and further datasets.", "Add compute tasks?", new Object[] {
							"<html>" +
									"Add compute tasks for missing data?<br>" +
									"If this is not selected and not all results are computed,<br>" +
									"this dataset will be ignored.", addNewTasksIfMissing
					});
			} else {
				// non-interactive
				res = new Object[] { false };
			}
			if (res == null) {
				if (optStatus != null)
					optStatus.setCurrentStatusText1("Processing cancelled");
				System.out.println(SystemAnalysis.getCurrentTime() + ">Processing cancelled upon user input.");
				return;
			} else {
				if (res[0] instanceof String)
					addNewTasksIfMissing = !((String) res[0]).contains("n");
				else
					addNewTasksIfMissing = (Boolean) res[0];
			}
			if (knownResults.size() < tempDataSetDescription.getPartCntI()) {
				if (addNewTasksIfMissing) {
					if (optStatus != null)
						optStatus.setCurrentStatusText1("Schedule missing compute tasks");
					// not everything has been computed (internal error)
					TreeSet<Integer> jobIDs = new TreeSet<Integer>();
					{
						int idx = 0;
						while (idx < tempDataSetDescription.getPartCntI()) {
							if (!added.contains(idx + "")) {
								System.out.println("Missing: " + idx);
								jobIDs.add(idx++);
							} else
								idx++;
						}
					}
					for (int jobID : jobIDs) {
						BatchCmd cmd = new BatchCmd();
						cmd.setRunStatus(CloudAnalysisStatus.SCHEDULED);
						cmd.setSubmissionTime(tempDataSetDescription.getSubmissionTimeL());
						cmd.setTargetIPs(new HashSet<String>());
						cmd.setSubTaskInfo(jobID, tempDataSetDescription.getPartCntI());
						cmd.setRemoteCapableAnalysisActionClassName(tempDataSetDescription.getRemoteCapableAnalysisActionClassName());
						cmd.setRemoteCapableAnalysisActionParams("");
						cmd.setExperimentMongoID(tempDataSetDescription.getOriginDBid());
						BatchCmd.enqueueBatchCmd(m, cmd);
						System.out.println("Enqueued new analysis task with ID " + jobID);
					}
				}
			} else
				if (knownResults.size() >= tempDataSetDescription.getPartCntI()) {
					try {
						if (optStatus != null)
							optStatus.setCurrentStatusText1("About to merge split result datasets");
						doMerge(tempDataSetDescription, knownResults, interactive, optStatus, optPreviousResultsToBeMerged);
					} catch (Exception e) {
						MongoDB.saveSystemErrorMessage("Could not properly merge temporary datasets.", e);
					}
				}
		}
	}
	
}
