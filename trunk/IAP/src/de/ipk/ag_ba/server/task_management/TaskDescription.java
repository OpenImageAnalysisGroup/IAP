/*******************************************************************************
 * Copyright (c) 2010 Image Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Sep 30, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.server.task_management;

import info.StopWatch;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.ErrorMsg;
import org.SystemAnalysis;

import de.ipk.ag_ba.gui.util.ExperimentReferenceInterface;
import de.ipk.ag_ba.gui.webstart.IAPmain;
import de.ipk.ag_ba.gui.webstart.IAPrunMode;
import de.ipk.ag_ba.mongo.Batch;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.RunnableWithMappingData;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;

/**
 * @author klukas
 */
public class TaskDescription {
	
	private final BatchCmd cmd;
	
	private String analysisActionClassName, params;
	private long startTime;
	
	private boolean finishedIncomplete = false, finishedComplete = false;
	
	private ExperimentReferenceInterface experimentInput;
	
	private final String systemIP;
	
	protected boolean allowSystemExit = true;
	
	public static String getTaskDescriptionText(String hostIPs, String task, String params, String startTime) {
		return task + "$" + params + "$" + startTime;
	}
	
	public TaskDescription(BatchCmd cmd, ExperimentReferenceInterface experiment, String systemIP) {
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
			MongoDB.saveSystemErrorMessage("Invalid task-description: " + cmd, e);
			analysisActionClassName = null;
			params = null;
		}
	}
	
	@Override
	public String toString() {
		return "" + analysisActionClassName.substring(analysisActionClassName.lastIndexOf(".") + 1) + " of " + experimentInput.getExperimentName() + "";
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
		
		String timeInfo = "";
		if (batch.getAvailableResultDatabaseId() != null && batch.getAvailableResultImportDate() != null)
			timeInfo = " Partial calculation will be performed on data newer than " + batch.getAvailableResultImportDate()
					+ ". Available partial result data set: " + batch.getAvailableResultDatabaseId() + ".";
		
		MongoDB.saveSystemMessage("Start analysis of " + batch.getExperimentHeader().getExperimentName()
				+ " (" + (batch.getPartIdx() + 1) + "/" + batch.getPartCnt() + ").");
		
		final BackgroundTaskStatusProviderSupportingExternalCall statusProvider = action.getStatusProvider();
		
		batch.setStatusProvider(statusProvider);
		
		RunnableWithMappingData resultReceiver = getResultReceiver(batch, m, statusProvider);
		
		action.setWorkingSet(cmd.getPartIdx(), cmd.getPartCnt(), resultReceiver, cmd.getAvailableResultImportDate());
		
		String st = new SimpleDateFormat().format(new Date(startTime));
		
		BackgroundTaskHelper.issueSimpleTask("Batch: " + analysisActionClassName + " (start: " + st + ")",
				"Initializing", new Runnable() {
					@Override
					public void run() {
						try {
							action.performActionCalculateResults(null);
						} catch (Exception e) {
							MongoDB.saveSystemErrorMessage("Error calculating phenotypic data set!", e);
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
				if (experiment == null) {
					if (cmd != null && cmd.getStatusProvider() != null)
						cmd.getStatusProvider().setCurrentStatusText2("ERROR: NULL RESULT");
					finishedIncomplete = true;
					return;
				}
				if (experiment != null) {
					if (cmd != null && cmd.getStatusProvider() != null && cmd.getStatusProvider().wantsToStop()) {
						cmd.getStatusProvider().setCurrentStatusText2("INFO: EXECUTION INTERRUPTED BY USER REQUEST");
						finishedIncomplete = true;
						return;
					}
				}
				if (batch.getStatusProvider() != null)
					batch.getStatusProvider().setCurrentStatusText2("INFO: SAVING RESULT");
				if (cmd != null && cmd.getStatusProvider() != null)
					cmd.getStatusProvider().setCurrentStatusText2("INFO: SAVING RESULT");
				experiment.getHeader().setOriginDbId(batch.getExperimentDatabaseId());
				experiment.getHeader().setExperimentname(
						cmd.getRemoteCapableAnalysisActionClassName() + "§"
								+ batch.getPartIdx() + "§" + batch.getPartCnt() + "§"
								+ batch.getSubmissionTime() + "§"
								+ batch.getAvailableResultDatabaseId());
				System.out.println(SystemAnalysis.getCurrentTime() + ">INFO: Received calculation results. Job has been submitted at "
						+ SystemAnalysis.getCurrentTime(batch.getSubmissionTime()));
				experiment.getHeader().setImportusergroup("Temp");
				// System.out.println("Received result: " + experiment.getName());
				try {
					BatchCmd bcmd = m.batch().getCommand(batch);
					if (bcmd != null)
						if (SystemAnalysisExt.getHostName().equals(bcmd.getOwner())) {
							StopWatch sw = new StopWatch(SystemAnalysis.getCurrentTime() + ">SAVE EXPERIMENT " + experiment.getName(), false);
							m.saveExperiment(experiment, statusProvider, true, true);
							sw.printTime();
							// ExperimentInterface experiment2 = m.getExperiment(experiment.getHeader());
							MongoDB.saveSystemMessage("Analysis results have been saved (" + (bcmd.getPartIdx() + 1) + "/" + bcmd.getPartCnt() + ")");
							
							// boolean saveOverallDatasetIfPossible = SystemOptions.getInstance().getBoolean("IAP", "grid_auto_merge_batch_results", true);
							// if (saveOverallDatasetIfPossible)
							// m.processSplitResults().merge(false, statusProvider, bcmd);
							
							m.batch().claim(bcmd, CloudAnalysisStatus.FINISHED, false);
							
							boolean deleteCompletedJobs = true;
							if (deleteCompletedJobs)
								m.batch().delete(batch);
						} else {
							MongoDB.saveSystemMessage("INFO: Batch command, processed by " + SystemAnalysisExt.getHostNameNiceNoError()
									+ " has been claimed by " + bcmd.getOwner()
									+ ". Therefore analysis result is not saved.");
							System.out.println(SystemAnalysis.getCurrentTime() + ">INFO: Batch command, processed by " + SystemAnalysisExt.getHostNameNoError()
									+ " has been claimed by " + bcmd.getOwner()
									+ ". Therefore analysis result is not saved.");
						}
					finishedComplete = true;
				} catch (Exception e) {
					BatchCmd bcmd = m.batch().getCommand(batch);
					m.batch().claim(bcmd, CloudAnalysisStatus.FINISHED_INCOMPLETE, false);
					System.out.println(SystemAnalysis.getCurrentTime() + ">ERROR: " + e.getMessage());
					MongoDB.saveSystemErrorMessage("Could not merge result data set.", e);
					ErrorMsg.addErrorMessage(e);
					finishedIncomplete = true;
				}
				if (IAPmain.getRunMode() == IAPrunMode.CLOUD_HOST_BATCH_MODE && allowSystemExit) {
					System.out.println(">Cluster Execution Mode is active // FINISHED COMPUTE TASK");
					try {
						if (m.batch().getAll().size() > 0) {
							System.out.println(">SYSTEM.EXIT(1) (batch queue not empty)");
							Batch.pingHost(m, SystemAnalysisExt.getHostName(), Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Double.NaN, "system.exit");
							MongoDB.saveSystemMessage("Finished compute task, job queue is not empty - SYSTEM.EXIT(1)");
							System.exit(1);
						} else {
							System.out.println(">SYSTEM.EXIT(0) (batch queue is empty)");
							Batch.pingHost(m, SystemAnalysisExt.getHostName(), Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Double.NaN, "system.exit");
							MongoDB.saveSystemMessage("Finished compute task, job queue is empty - SYSTEM.EXIT(0)");
							System.exit(0);
						}
					} catch (Exception e) {
						MongoDB.saveSystemErrorMessage("Error post-processing cloud batch execution.", e);
					}
					System.exit(0);
				}
			}
			
			@Override
			public void setExperimenData(ExperimentInterface experiment) {
				this.experiment = experiment;
			}
		};
	}
	
	public boolean analysisFinishedComplete() {
		return finishedComplete;
	}
	
	public boolean analysisFinishedIncomplete() {
		return finishedIncomplete;
	}
	
	public BatchCmd getBatchCmd() {
		return cmd;
	}
	
}
