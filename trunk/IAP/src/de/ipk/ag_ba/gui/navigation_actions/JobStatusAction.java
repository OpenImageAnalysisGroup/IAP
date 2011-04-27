package de.ipk.ag_ba.gui.navigation_actions;

import java.util.ArrayList;
import java.util.HashSet;

import org.BackgroundTaskStatusProvider;
import org.ErrorMsg;

import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.server.task_management.BatchCmd;

public class JobStatusAction extends AbstractNavigationAction {
	
	private final MongoDB m;
	private NavigationButton src;
	private BackgroundTaskStatusProvider jobStatus;
	
	public JobStatusAction(MongoDB m) {
		super("Analyze Workload");
		this.m = m;
		
		final HashSet<String> jobIds = new HashSet<String>();
		
		this.jobStatus = new BackgroundTaskStatusProvider() {
			int remainingJobs = 0;
			
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
				try {
					for (BatchCmd b : JobStatusAction.this.m.batchGetAllCommands()) {
						if (b.getString("_id") != null)
							activeJobsIds.add(b.getString("_id"));
						if (b.getCurrentStatusValueFine() > 0)
							finishedJobs += b.getCurrentStatusValueFine() / 100;
						jobIds.add(b.getString("_id"));
					}
					for (String id : jobIds)
						if (!activeJobsIds.contains(id))
							finishedJobs += 1;
					remainingJobs = activeJobsIds.size();
					if (remainingJobs == 0)
						jobIds.clear();
				} catch (Exception e) {
					System.out.println("ERROR: " + e.getMessage());
				}
				if (jobIds.size() > 0)
					return 100d / jobIds.size() * finishedJobs;
				else
					return -1;
			}
			
			@Override
			public int getCurrentStatusValue() {
				return (int) getCurrentStatusValueFine();
			}
			
			@Override
			public String getCurrentStatusMessage1() {
				return remainingJobs + " remaining";
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
