/*******************************************************************************
 * Copyright (c) 2010 Image Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Sep 30, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.server.task_management;

import info.StopWatch;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.ErrorMsg;

import de.ipk.ag_ba.gui.images.IAPexperimentTypes;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.mongo.IAPservice;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Experiment;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.RunnableWithMappingData;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.MappingData3DPath;

/**
 * @author klukas
 */
public class TaskDescription {
	
	private final BatchCmd cmd;
	
	private String analysisActionClassName, params;
	private long startTime;
	
	private boolean finished = false;
	
	private ExperimentReference experimentInput;
	
	private final String systemIP;
	
	private boolean autoClose;
	
	public static String getTaskDescriptionText(String hostIPs, String task, String params, String startTime) {
		return task + "$" + params + "$" + startTime;
	}
	
	public TaskDescription(BatchCmd cmd, ExperimentReference experiment, String systemIP) {
		this.cmd = cmd;
		this.systemIP = systemIP;
		// ip $ analysisAction.className $ params $ unique key (time stamp)
		try {
			analysisActionClassName = cmd.getRemoteCapableAnalysisActionClassName();
			params = cmd.getRemoteCapableAnalysisActionParams();
			this.experimentInput = experiment;
			startTime = cmd.getSubmissionTime();
		} catch (Exception e) {
			System.out.println("Invalid task-description: " + cmd);
			analysisActionClassName = null;
			params = null;
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		return cmd.equals(((TaskDescription) obj).cmd);
	}
	
	public boolean isValid() {
		return cmd != null && (cmd.getTargetIPs().isEmpty() || cmd.getTargetIPs().contains(systemIP));
	}
	
	public void startWork(final BatchCmd batch, String hostName, String ip, final MongoDB m)
			throws ClassCastException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		final RemoteCapableAnalysisAction action = RemoteAnalysisRepository.getInstance().getNewAnalysisAction(
				analysisActionClassName);
		action.setParams(experimentInput, m, params);
		
		final BackgroundTaskStatusProviderSupportingExternalCall statusProvider = action.getStatusProvider();
		
		batch.setStatusProvider(statusProvider);
		
		RunnableWithMappingData resultReceiver = getResultReceiver(batch, m, statusProvider);
		
		action.setWorkingSet(cmd.getPartIdx(), cmd.getPartCnt(), resultReceiver);
		
		String st = new SimpleDateFormat().format(new Date(startTime));
		
		BackgroundTaskHelper.issueSimpleTask("Batch: " + analysisActionClassName + " (start: " + st + ")",
				"Initializing", new Runnable() {
					@Override
					public void run() {
						try {
							action.performActionCalculateResults(null);
						} catch (Exception e) {
							e.printStackTrace();
							ErrorMsg.addErrorMessage(e);
						}
					}
				}, null, statusProvider, -1000);
	}
	
	private RunnableWithMappingData getResultReceiver(final BatchCmd batch, final MongoDB m,
			final BackgroundTaskStatusProviderSupportingExternalCall statusProvider) {
		return new RunnableWithMappingData() {
			private ExperimentInterface experiment;
			
			@Override
			public void run() {
				// store dataset in mongo
				experiment.getHeader().setExperimentname(
						cmd.getRemoteCapableAnalysisActionClassName() + "§" + batch.getPartIdx() + "§" + batch.getPartCnt() + "§"
								+ batch.getSubmissionTime());
				System.out.println(SystemAnalysisExt.getCurrentTime() + ">INFO: Received calculation results. Job has been submitted at "
						+ SystemAnalysisExt.getCurrentTime(batch.getSubmissionTime()));
				experiment.getHeader().setImportusergroup("Temp");
				// System.out.println("Received result: " + experiment.getName());
				try {
					BatchCmd bcmd = m.batchGetCommand(batch);
					if (bcmd != null)
						if (SystemAnalysisExt.getHostName().equals(bcmd.getOwner())) {
							m.batchClearJob(batch);
							StopWatch sw = new StopWatch(SystemAnalysisExt.getCurrentTime() + ">SAVE EXPERIMENT " + experiment.getName(), false);
							m.saveExperiment(experiment, statusProvider, true);
							sw.printTime();
							// ExperimentInterface experiment2 = m.getExperiment(experiment.getHeader());
							
							ArrayList<ExperimentHeaderInterface> knownResults = new ArrayList<ExperimentHeaderInterface>();
							for (ExperimentHeaderInterface i : m.getExperimentList(null)) {
								if (i.getExperimentName() != null && i.getExperimentName().contains("§")) {
									String[] cc = i.getExperimentName().split("§");
									if (i.getImportusergroup().equals("Temp") && cc.length == 4) {
										String className = cc[0];
										String partCnt = cc[2];
										String submTime = cc[3];
										String bcn = cmd.getRemoteCapableAnalysisActionClassName();
										String bpn = cmd.getPartCnt() + "";
										String bst = batch.getSubmissionTime() + "";
										if (className.equals(bcn)
												&& partCnt.equals(bpn)
												&& submTime.equals(bst))
											knownResults.add(i);
										// else
										// System.out.println("NO FIT: " + i.getExperimentname());
									}
								}
							}
							System.out.println("> T=" + IAPservice.getCurrentTimeAsNiceString());
							System.out.println("> TODO: " + batch.getPartCnt() + ", FINISHED: " + knownResults.size());
							if (knownResults.size() >= batch.getPartCnt()) {
								System.out.println("*****************************");
								System.out.println("MERGE RESULTS:");
								System.out.println("TODO: " + batch.getPartCnt() + ", RESULTS FINISHED: " + knownResults.size());
								Experiment e = new Experiment();
								long tFinish = System.currentTimeMillis();
								boolean removeWaterAndWeightDataFromSubsequentDatasets = false;
								ArrayList<String> deleteIDs = new ArrayList<String>();
								int iii = 0;
								int mmm = knownResults.size();
								for (ExperimentHeaderInterface i : knownResults) {
									ExperimentInterface ei = m.getExperiment(i);
									if (removeWaterAndWeightDataFromSubsequentDatasets) {
										ArrayList<SubstanceInterface> toBeRemoved = new ArrayList<SubstanceInterface>();
										for (SubstanceInterface si : ei) {
											if (si.getName() != null && (si.getName().equals("weight_before") ||
													(si.getName().equals("water_weight") ||
													si.getName().equals("water_sum")))) {
												System.out.print(SystemAnalysisExt.getCurrentTime()
														+ ">INFO: Remove duplicate water and weight data before adding results to merged dataset");
												toBeRemoved.add(si);
											}
										}
										for (SubstanceInterface si : toBeRemoved) {
											ei.remove(si);
											System.out.print(".");
										}
										System.out.println();
									}
									removeWaterAndWeightDataFromSubsequentDatasets = true;
									// if (ei.getNumberOfMeasurementValues() > 0)
									// System.out.println("Measurements: " + ei.getNumberOfMeasurementValues());
									e.addAndMerge(ei);
									deleteIDs.add(i.getDatabaseId());
									System.out.println("*****************************");
									iii++;
									if (statusProvider != null) {
										statusProvider.setCurrentStatusText1("Merged dataset " + iii + "/" + mmm);
										statusProvider.setCurrentStatusValueFine(100d / mmm * iii);
									}
								}
								String sn = cmd.getRemoteCapableAnalysisActionClassName();
								if (sn.indexOf(".") > 0)
									sn = sn.substring(sn.lastIndexOf(".") + 1);
								e.getHeader().setExperimentname(sn + ": " + experimentInput.getExperimentName());
								e.getHeader().setExperimenttype(IAPexperimentTypes.AnalysisResults);
								e.getHeader().setImportusergroup(IAPexperimentTypes.AnalysisResults);
								e.getHeader().setDatabaseId("");
								for (SubstanceInterface si : e) {
									for (ConditionInterface ci : si) {
										ci.setExperimentName(e.getHeader().getExperimentName());
										ci.setExperimentType(IAPexperimentTypes.AnalysisResults);
									}
								}
								boolean superMerge = false;
								if (superMerge) {
									ArrayList<MappingData3DPath> mdpl = MappingData3DPath.get(e);
									if (statusProvider != null)
										statusProvider.setCurrentStatusText1("Merging Analysis Results");
									e = (Experiment) MappingData3DPath.merge(mdpl, false);
									if (statusProvider != null)
										statusProvider.setCurrentStatusText1("Merged Analysis Results");
								}
								long tStart = cmd.getSubmissionTime();
								long tProcessing = tFinish - tStart;
								long minutes = tProcessing / 1000 / 60;
								e.getHeader().setRemark(
										e.getHeader().getRemark() + " // processing time (min): " + minutes + " // finished: " + SystemAnalysisExt.getCurrentTime());
								System.out.println("> T=" + IAPservice.getCurrentTimeAsNiceString());
								System.out.println("> PIPELINE PROCESSING TIME (min)=" + minutes);
								System.out.println("*****************************");
								System.out.println("Merged Experiment: " + e.getName());
								// System.out.println("Merged Measurements: " + e.getNumberOfMeasurementValues());
								m.saveExperiment(e, statusProvider, true); // new BackgroundTaskConsoleLogger("", "", true)
								int idx = 0;
								int max = deleteIDs.size();
								if (statusProvider != null)
									statusProvider.setCurrentStatusText1("Saved Merged Analysis Results");
								for (String delID : deleteIDs) {
									m.deleteExperiment(delID);
									idx++;
									if (statusProvider != null) {
										statusProvider.setCurrentStatusText2("Deleted temp result " + idx + "/" + max);
										statusProvider.setCurrentStatusValueFine(100d / max * idx);
									}
								}
							}
						} else
							System.out.println(SystemAnalysisExt.getCurrentTime() + ">INFO: Batch command, processed by " + SystemAnalysisExt.getHostName()
									+ " has been claimed by " + bcmd.getOwner()
									+ ". Therefore analysis result is not saved.");
				} catch (Exception e) {
					System.out.println(SystemAnalysisExt.getCurrentTime() + ">ERROR: " + e.getMessage());
					e.printStackTrace();
				}
				finished = true;
				if (autoClose) {
					System.out.println("> Cluster Execution Mode is active // FINISHED COMPUTE TASK");
					System.out.println("> SYSTEM.EXIT");
					System.exit(0);
				}
			}
			
			@Override
			public void setExperimenData(ExperimentInterface experiment) {
				this.experiment = experiment;
			}
		};
	}
	
	public boolean analysisFinished() {
		return finished;
	}
	
	public BatchCmd getBatchCmd() {
		return cmd;
	}
	
	public void setSystemExitAfterCompletion(boolean autoClose) {
		this.autoClose = autoClose;
	}
}
