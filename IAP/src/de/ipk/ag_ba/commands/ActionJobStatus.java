package de.ipk.ag_ba.commands;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeMap;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.ErrorMsg;
import org.StringManipulationTools;
import org.SystemAnalysis;

import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.server.task_management.BatchCmd;
import de.ipk.ag_ba.server.task_management.CloudAnalysisStatus;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProviderSupportingExternalCallImpl;

public class ActionJobStatus extends AbstractNavigationAction {
	
	private final MongoDB m;
	private NavigationButton src;
	private BackgroundTaskStatusProviderSupportingExternalCall jobStatus;
	
	public ActionJobStatus(MongoDB m) {
		super("Analyze Workload");
		this.m = m;
		
		final HashSet<String> jobIds = new HashSet<String>();
		
		this.jobStatus = new BackgroundTaskStatusProviderSupportingExternalCall() {
			int remainingJobs = 0;
			int part_cnt = 0;
			
			BackgroundTaskStatusProviderSupportingExternalCallImpl status3provider = new BackgroundTaskStatusProviderSupportingExternalCallImpl("", "");
			
			@Override
			public void setCurrentStatusValue(int value) {
				
			}
			
			@Override
			public boolean pluginWaitsForUser() {
				return false;
			}
			
			@Override
			public void pleaseStop() {
			}
			
			@Override
			public void pleaseContinueRun() {
			}
			
			private String remain = "";
			
			@Override
			public double getCurrentStatusValueFine() {
				double finishedJobs = 0.00001;
				HashSet<String> activeJobsIds = new HashSet<String>();
				TreeMap<String, Integer> submission2partCnt = new TreeMap<String, Integer>();
				Long firstSubmission = null;
				try {
					for (BatchCmd b : ActionJobStatus.this.m.batchGetAllCommands()) {
						String jid = b.getString("_id");
						if (jid != null)
							activeJobsIds.add(jid);
						double fs = b.getCurrentStatusValueFine();
						
						if (b.getRunStatus() == CloudAnalysisStatus.FINISHED_INCOMPLETE)
							fs = 0;
						if (fs >= 0)
							finishedJobs += fs / 100d;
						if (jid != null) {
							jobIds.add(jid);
							
							long st = b.getSubmissionTime();
							if (st > 0 && (firstSubmission == null || st < firstSubmission))
								firstSubmission = st;
							
							String id = b.getRemoteCapableAnalysisActionClassName() + "$" + b.getExperimentDatabaseId() + "$" + b.getSubmissionTime();
							
							if (!submission2partCnt.containsKey(id))
								submission2partCnt.put(id, b.getPartCnt());
						}
					}
					part_cnt = 0;
					for (Integer cnt : submission2partCnt.values())
						part_cnt += cnt;
					
					finishedJobs += part_cnt - remainingJobs;
					
					remainingJobs = activeJobsIds.size();
					if (remainingJobs == 0)
						jobIds.clear();
				} catch (Exception e) {
					System.out.println("ERROR: " + e.getMessage());
				}
				if (part_cnt > 0) {
					double value = 100d * (part_cnt - remainingJobs) / part_cnt;
					// status3provider.setCurrentStatusValueFine(value);
					
					if (firstSubmission != null) {
						long ct = System.currentTimeMillis();
						long processingTime = ct - firstSubmission;
						double progress = finishedJobs / part_cnt;
						long fullTime = (long) (processingTime / progress);
						remain = "eta: " + SystemAnalysis.getCurrentTime(ct + fullTime - processingTime) + ", overall: "
								+ SystemAnalysis.getWaitTimeShort(fullTime) + ", remain: " + SystemAnalysis.getWaitTimeShort(fullTime - processingTime);
						ArrayList<String> s = new ArrayList<String>();
						for (String ss : submission2partCnt.keySet()) {
							long l = Long.parseLong(ss.substring(ss.lastIndexOf("$") + "$".length()));
							s.add(SystemAnalysis.getCurrentTime(l));
						}
						remain += "<br>starts: " + StringManipulationTools.getStringListMerge(s, ", ");
						long partTime = fullTime / part_cnt;
						remain += "<br>processed: " + StringManipulationTools.formatNumber(finishedJobs, "#.000") + " in "
								+ SystemAnalysis.getWaitTimeShort(processingTime) + ", 1 task takes " + SystemAnalysis.getWaitTimeShort(partTime);
					}
					return value;
				} else {
					status3provider.setCurrentStatusValueFine(-1);
					return -1;
				}
			}
			
			@Override
			public int getCurrentStatusValue() {
				return (int) getCurrentStatusValueFine();
			}
			
			@Override
			public String getCurrentStatusMessage1() {
				return remainingJobs + "/" + part_cnt + " remaining";
			}
			
			@Override
			public String getCurrentStatusMessage2() {
				return remain;
			}
			
			@Override
			public void setCurrentStatusValueFine(double value) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public boolean wantsToStop() {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public void setCurrentStatusText1(String status) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setCurrentStatusText2(String status) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setCurrentStatusValueFineAdd(double smallProgressStep) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public String getCurrentStatusMessage3() {
				return status3provider.getCurrentStatusMessage3();
			}
			
			@Override
			public void setPrefix1(String prefix1) {
				// TODO Auto-generated method stub
				
			}
		};
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		this.src = src;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
		ArrayList<NavigationButton> res = new ArrayList<NavigationButton>(currentSet);
		res.add(src);
		return res;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		ArrayList<NavigationButton> res = new ArrayList<NavigationButton>();
		try {
			for (BatchCmd b : m.batchGetAllCommands()) {
				NavigationButton n;
				n = new NavigationButton(new BatchInformationAction(b, m), src.getGUIsetting());
				n.setProcessing(true);
				// n.setRightAligned(true);
				res.add(n);
			}
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
		}
		return res;
	}
	
	@Override
	public BackgroundTaskStatusProviderSupportingExternalCall getStatusProvider() {
		return jobStatus;
	}
	
	@Override
	public String getDefaultTitle() {
		return "Tasks";
	}
	
	@Override
	public String getDefaultImage() {
		return "img/ext/applications-system.png";
	}
	
	@Override
	public boolean getProvidesActions() {
		return true;
	}
	
	@Override
	public boolean requestTitleUpdates() {
		return true;
	}
}
