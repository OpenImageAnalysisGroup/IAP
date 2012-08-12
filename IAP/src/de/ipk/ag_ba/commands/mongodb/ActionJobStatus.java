package de.ipk.ag_ba.commands.mongodb;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeMap;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.ErrorMsg;
import org.StringManipulationTools;
import org.SystemAnalysis;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
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
		
		this.jobStatus = new BackgroundTaskStatusProviderSupportingExternalCall() {
			int remainingJobs = 0;
			int part_cnt = 0;
			TreeMap<CloudAnalysisStatus, Integer> sss = new TreeMap<CloudAnalysisStatus, Integer>();
			
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
			
			private long firstStatusUpdate = -1l;
			private double firstStatusProgress = 0d;
			
			@Override
			public double getCurrentStatusValueFine() {
				double finishedJobs = 0.00001;
				HashSet<String> activeJobsIds = new HashSet<String>();
				TreeMap<String, Integer> submission2partCnt = new TreeMap<String, Integer>();
				Long firstSubmission = null;
				try {
					sss.clear();
					for (BatchCmd b : ActionJobStatus.this.m.batchGetAllCommands()) {
						String jid = b.getString("_id");
						if (jid != null)
							activeJobsIds.add(jid);
						double fs = b.getCurrentStatusValueFine();
						
						CloudAnalysisStatus rs = b.getRunStatus();
						if (!sss.containsKey(rs))
							sss.put(rs, 1);
						else
							sss.put(rs, sss.get(rs) + 1);
						
						if (rs == CloudAnalysisStatus.FINISHED_INCOMPLETE) {
							fs = 0;
						}
						if (fs >= 0)
							finishedJobs += fs / 100d;
						if (jid != null) {
							long st = b.getSubmissionTime();
							if (st > 0 && (firstSubmission == null || st < firstSubmission))
								firstSubmission = st;
							
							String id = b.getRemoteCapableAnalysisActionClassName() +
									"$" + b.getExperimentDatabaseId() +
									"$" + b.getSubmissionTime();
							
							if (!submission2partCnt.containsKey(id))
								submission2partCnt.put(id, b.getPartCnt());
						}
					}
					part_cnt = 0;
					for (Integer cnt : submission2partCnt.values())
						part_cnt += cnt;
					
					finishedJobs += part_cnt - remainingJobs;
					
					remainingJobs = activeJobsIds.size();
				} catch (Exception e) {
					System.out.println("ERROR: " + e.getMessage());
				}
				if (part_cnt > 0) {
					double value = 100d * (part_cnt - remainingJobs) / part_cnt;
					// status3provider.setCurrentStatusValueFine(value);
					
					long ct = System.currentTimeMillis();
					if (firstStatusUpdate < 0) {
						firstStatusUpdate = ct;
						firstStatusProgress = finishedJobs;
					}
					
					if (ct > firstStatusUpdate) {
						long processingTimePPP = ct - firstStatusUpdate;
						long processingTime = ct - firstSubmission;
						double progress = (finishedJobs - firstStatusProgress) / part_cnt;
						long fullTime = (long) (processingTimePPP / progress);
						remain = "eta: " + SystemAnalysis.getCurrentTime(ct + fullTime - processingTime) + ", overall: "
								+ SystemAnalysis.getWaitTimeShort(fullTime)
								+ ", remain: "
								+ SystemAnalysis.getWaitTimeShort(fullTime - processingTime);
						ArrayList<String> s = new ArrayList<String>();
						for (String ss : submission2partCnt.keySet()) {
							long l = Long.parseLong(ss.substring(ss.lastIndexOf("$")
									+ "$".length()));
							s.add(SystemAnalysis.getCurrentTime(l));
						}
						remain += "<br>starts: " + StringManipulationTools.getStringListMerge(s, ", ");
						long partTime = fullTime / part_cnt;
						remain += "<br>processed: " + StringManipulationTools.formatNumber(finishedJobs, "#.000") + " in "
								+ SystemAnalysis.getWaitTimeShort(processingTime)
								+ ", 1 task takes " + SystemAnalysis.getWaitTimeShort(partTime);
					} else
						remain = "";
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
				String detail = "";
				try {
					for (CloudAnalysisStatus cas : sss.keySet()) {
						Integer n = sss.get(cas);
						if (n != null) {
							if (detail.length() > 0)
								detail = detail + ", ";
							detail = detail + n + " " + cas.toString();
						}
					}
					if (detail.length() > 0)
						detail = "<br>(" + detail + ")";
				} catch (Exception e) {
					detail = " (error " + e.getMessage() + ")";
				}
				return (part_cnt - remainingJobs) + "/" + part_cnt + " completed" + detail;
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
