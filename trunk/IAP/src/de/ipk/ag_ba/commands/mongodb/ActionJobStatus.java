package de.ipk.ag_ba.commands.mongodb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.ErrorMsg;
import org.StringManipulationTools;
import org.SystemAnalysis;
import org.SystemOptions;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.commands.database_tools.ActionArchiveAnalysisJobs;
import de.ipk.ag_ba.commands.database_tools.ActionDeleteAnalysisJobs;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.server.task_management.BatchCmd;
import de.ipk.ag_ba.server.task_management.CloudAnalysisStatus;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProviderSupportingExternalCallImpl;

public class ActionJobStatus extends AbstractNavigationAction {
	
	private final MongoDB m;
	private NavigationButton src;
	private BackgroundTaskStatusProviderSupportingExternalCall jobStatus;
	
	public ActionJobStatus(MongoDB m) {
		super("Analyze or Modify Workload");
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
					for (BatchCmd b : ActionJobStatus.this.m.batch().getAll()) {
						String jid = b.getString("_id");
						// System.out.println(b.getRunStatus());
						if (jid != null && b.getRunStatus() != CloudAnalysisStatus.FINISHED)
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
					e.printStackTrace();
					System.out.println("ERROR: " + e.getMessage());
				}
				if (part_cnt > 0) {
					double value = 100d * (part_cnt - remainingJobs) / part_cnt;
					// status3provider.setCurrentStatusValueFine(value);
					
					long ct = System.currentTimeMillis();
					if ((firstStatusProgress < 0.1 || firstStatusUpdate < 0 || firstStatusProgress > finishedJobs) && part_cnt != 0 && firstSubmission != null) {
						firstStatusUpdate = ct;
						firstStatusProgress = finishedJobs;
						// System.out.println(">progress init");
					}
					
					if (ct > firstStatusUpdate && firstSubmission != null) {
						long processingTimePPP = ct - firstStatusUpdate;
						long processingTime = ct - firstSubmission;
						double progress = (finishedJobs - firstStatusProgress) / part_cnt;
						if (progress > 0) {
							long fullTime = (long) (processingTimePPP / progress);
							remain = "";
							// // "eta: " + SystemAnalysis.getCurrentTime(ct + (long) (fullTime * (1d - value / 100d)))
							// // + ", overall: "
							// // + SystemAnalysis.getWaitTimeShort(fullTime)
							// // + ", remain: "
							// // + SystemAnalysis.getWaitTimeShort((long) (fullTime * (1d - value / 100d)));
							// SystemAnalysis.getWaitTimeShort((long) (fullTime * (1d - value / 100d)))
							// + " / "
							// + SystemAnalysis.getWaitTimeShort(fullTime);
							
							ArrayList<String> s = new ArrayList<String>();
							for (String ss : submission2partCnt.keySet()) {
								long l = Long.parseLong(ss.substring(ss.lastIndexOf("$")
										+ "$".length()));
								s.add(SystemAnalysis.getCurrentTime(l));
							}
							remain += "starts: " + StringManipulationTools.getStringListMerge(s, ", ");
							long partTime = fullTime / part_cnt; // current speed
							if (SystemOptions.getInstance().getBoolean("IAP", "Show Overall Task Speed", true))
								partTime = (long) (processingTime / finishedJobs); // historic overall speed
							remain += "<br>processed: " + StringManipulationTools.formatNumber(finishedJobs, "#.000") + " in "
									+ SystemAnalysis.getWaitTimeShort(processingTime)
									+ ", " + SystemAnalysis.getWaitTime(partTime, 1) + "/task";
						}
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
							detail = detail + n + " " + cas.toNiceString();
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
				
			}
			
			@Override
			public boolean wantsToStop() {
				return false;
			}
			
			@Override
			public void setCurrentStatusText1(String status) {
				
			}
			
			@Override
			public void setCurrentStatusText2(String status) {
				
			}
			
			@Override
			public void setCurrentStatusValueFineAdd(double smallProgressStep) {
				
			}
			
			@Override
			public String getCurrentStatusMessage3() {
				return status3provider.getCurrentStatusMessage3();
			}
			
			@Override
			public void setPrefix1(String prefix1) {
				
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
		
		NavigationButton archiveJobs = new NavigationButton(
				new ActionArchiveAnalysisJobs(m), guiSetting);
		res.add(archiveJobs);
		
		try {
			HashMap<String, ExperimentHeaderInterface> dbId2header = new HashMap<String, ExperimentHeaderInterface>();
			final HashMap<String, ArrayList<NavigationButton>> set = new HashMap<String, ArrayList<NavigationButton>>();
			final HashMap<String, ArrayList<BatchCmd>> setBatchCmds = new HashMap<String, ArrayList<BatchCmd>>();
			final HashMap<String, Integer> setMaxSize = new HashMap<String, Integer>();
			for (BatchCmd b : m.batch().getAll()) {
				if (!dbId2header.containsKey(b.getExperimentDatabaseId()))
					dbId2header.put(b.getExperimentDatabaseId(), b.getExperimentHeader());
				ExperimentHeaderInterface ehi = dbId2header.get(b.getExperimentDatabaseId());
				NavigationButton n = new NavigationButton(new BatchInformationAction(b, m), src.getGUIsetting());
				n.setProcessing(true);
				
				String desc = "<html>" + ehi.getExperimentName() + "<br>"
						+ " Job submission " + SystemAnalysis.getCurrentTime(b.getSubmissionTime()) + "<br>"
						+ "(" + b.getRunStatus() + ")";
				
				if (!set.containsKey(desc)) {
					set.put(desc, new ArrayList<NavigationButton>());
					setBatchCmds.put(desc, new ArrayList<BatchCmd>());
				}
				set.get(desc).add(n);
				setMaxSize.put(desc, b.getPartCnt());
			}
			
			for (String desc : set.keySet()) {
				final String fd = desc;
				NavigationAction setCmd = new AbstractNavigationAction(desc) {
					@Override
					public void performActionCalculateResults(NavigationButton src) throws Exception {
					}
					
					@Override
					public String getDefaultTitle() {
						return "<html><center>" + fd + " (" + set.get(fd).size()
								+ "/" + setMaxSize.get(fd) + ")";
					}
					
					@Override
					public String getDefaultImage() {
						return "img/ext/gpl2/Gtk-Dnd-Multiple-64.png";
					}
					
					@Override
					public ArrayList<NavigationButton> getResultNewActionSet() {
						ArrayList<NavigationButton> res = new ArrayList<NavigationButton>();
						res.addAll(set.get(fd));
						if (res.size() > 0 &&
								(
								((BatchInformationAction) res.iterator().next().getAction()).getCmd()
										.getRunStatus() == CloudAnalysisStatus.SCHEDULED) ||
								((BatchInformationAction) res.iterator().next().getAction()).getCmd()
										.getRunStatus() == CloudAnalysisStatus.ARCHIVED) {
							ArrayList<BatchCmd> commandList = new ArrayList<BatchCmd>();
							for (NavigationButton nb : res) {
								if (((BatchInformationAction) nb.getAction()).getCmd()
										.getRunStatus() == CloudAnalysisStatus.SCHEDULED ||
										((BatchInformationAction) nb.getAction()).getCmd()
												.getRunStatus() == CloudAnalysisStatus.ARCHIVED) {
									commandList.add(((BatchInformationAction) nb.getAction()).getCmd());
								}
							}
							NavigationButton archiveJobs = new NavigationButton(
									new ActionArchiveAnalysisJobs(m, commandList), guiSetting);
							archiveJobs.setRightAligned(true);
							NavigationButton removeJobs = new NavigationButton(
									new ActionDeleteAnalysisJobs(m, commandList), guiSetting);
							removeJobs.setRightAligned(true);
							res.add(0, removeJobs);
							res.add(0, archiveJobs);
						}
						return res;
					}
				};
				res.add(new NavigationButton(setCmd, guiSetting));
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
	
	int sec = 0;
	
	@Override
	public String getDefaultImage() {
		this.sec++;
		switch (sec % 6) {
			case 0:
				return "img/ext/applications-system-07.5.png";
			case 1:
				return "img/ext/applications-system-15.png";
			case 2:
				return "img/ext/applications-system-22.5.png";
			case 3:
				return "img/ext/applications-system-30.png";
			case 4:
				return "img/ext/applications-system-37.5.png";
			case 5:
				return "img/ext/applications-system-45.png";
		}
		return null;
	}
	
	@Override
	public boolean isProvidingActions() {
		return true;
	}
	
	@Override
	public boolean requestTitleUpdates() {
		return true;
	}
}
