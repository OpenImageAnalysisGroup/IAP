package de.ipk.ag_ba.gui.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.BackgroundTaskStatusProvider;
import org.ErrorMsg;

import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.server.task_management.BatchCmd;

public class ActionJobStatus extends AbstractNavigationAction {
	
	private final MongoDB m;
	private NavigationButton src;
	private BackgroundTaskStatusProvider jobStatus;
	
	public ActionJobStatus(MongoDB m) {
		super("Analyze Workload");
		this.m = m;
		
		final HashSet<String> jobIds = new HashSet<String>();
		
		this.jobStatus = new BackgroundTaskStatusProvider() {
			int remainingJobs = 0;
			int part_cnt = 0;
			
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
			
			@Override
			public double getCurrentStatusValueFine() {
				double finishedJobs = 0.00001;
				HashSet<String> activeJobsIds = new HashSet<String>();
				HashMap<Long, Integer> submissionTime2partCnt = new HashMap<Long, Integer>();
				try {
					for (BatchCmd b : ActionJobStatus.this.m.batchGetAllCommands()) {
						String jid = b.getString("_id");
						if (jid != null)
							activeJobsIds.add(jid);
						if (b.getCurrentStatusValueFine() > 0)
							finishedJobs += b.getCurrentStatusValueFine() / 100;
						if (jid != null) {
							jobIds.add(jid);
							
							if (!submissionTime2partCnt.containsKey(b.getSubmissionTime()))
								submissionTime2partCnt.put(b.getSubmissionTime(), b.getPartCnt());
						}
					}
					part_cnt = 0;
					for (String id : jobIds) {
						if (!activeJobsIds.contains(id))
							finishedJobs += 1;
					}
					for (Integer cnt : submissionTime2partCnt.values())
						part_cnt += cnt;
					
					remainingJobs = activeJobsIds.size();
					if (remainingJobs == 0)
						jobIds.clear();
				} catch (Exception e) {
					System.out.println("ERROR: " + e.getMessage());
				}
				if (part_cnt > 0)
					return 100d * (part_cnt - remainingJobs) / part_cnt;
				else
					return -1;
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
				return null;
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
	public BackgroundTaskStatusProvider getStatusProvider() {
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
