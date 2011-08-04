package de.ipk.ag_ba.gui.navigation_model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeSet;

import org.BackgroundTaskStatusProvider;
import org.ErrorMsg;
import org.bson.types.ObjectId;
import org.graffiti.editor.MainFrame;

import de.ipk.ag_ba.datasources.http_folder.NavigationImage;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.actions.ParameterOptions;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.server.task_management.BatchCmd;
import de.ipk.ag_ba.server.task_management.CloudAnalysisStatus;
import de.ipk.ag_ba.server.task_management.CloudHost;
import de.ipk.ag_ba.server.task_management.RemoteCapableAnalysisAction;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProviderSupportingExternalCallImpl;

public class RemoteExecutionWrapperAction implements NavigationAction {
	
	private final RemoteCapableAnalysisAction remoteAction;
	private final NavigationAction action;
	private final NavigationButton cm;
	private NavigationButton src;
	
	public RemoteExecutionWrapperAction(NavigationAction navigationAction, NavigationButton cm) {
		this.remoteAction = (RemoteCapableAnalysisAction) navigationAction;
		this.action = navigationAction;
		this.cm = cm;
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		this.src = src;
		ArrayList<CloudHost> targetIPs = remoteAction.getMongoDB().batchGetAvailableHosts(10000);
		if (false && targetIPs.isEmpty()) {
			MainFrame.showMessageDialog("No active compute node found.", "Information");
		} else {
			String remoteCapableAnalysisActionClassName = remoteAction.getClass().getCanonicalName();
			String remoteCapableAnalysisActionParams = null;
			String experimentInputMongoID = remoteAction.getMongoDatasetID();
			ExperimentHeaderInterface header = remoteAction.getMongoDB().getExperimentHeader(new ObjectId(experimentInputMongoID));
			int snapshotsPerJob = 100;
			int numberOfJobs = header.getNumberOfFiles() / snapshotsPerJob / 3;
			
			if (numberOfJobs < 1)
				numberOfJobs = 1;
			TreeSet<Integer> jobIDs = new TreeSet<Integer>();
			{
				int idx = 0;
				while (jobIDs.size() < numberOfJobs)
					jobIDs.add(idx++);
			}
			long st = System.currentTimeMillis();
			for (int id : jobIDs) {
				BatchCmd cmd = new BatchCmd();
				cmd.setRunStatus(CloudAnalysisStatus.SCHEDULED);
				cmd.setSubmissionTime(st);
				HashSet<String> ips = new HashSet<String>();
				for (CloudHost h : targetIPs)
					ips.add(h.getHostName());
				cmd.setTargetIPs(ips);
				cmd.setSubTaskInfo(id, jobIDs.size());
				cmd.setRemoteCapableAnalysisActionClassName(remoteCapableAnalysisActionClassName);
				cmd.setRemoteCapableAnalysisActionParams(remoteCapableAnalysisActionParams);
				cmd.setExperimentMongoID(experimentInputMongoID);
				BatchCmd.enqueueBatchCmd(remoteAction.getMongoDB(), cmd);
				cm.getAction().performActionCalculateResults(src);
			}
			System.out.println("Enqueued " + jobIDs.size() + " new jobs!");
		}
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
		ArrayList<NavigationButton> res = new ArrayList<NavigationButton>();
		res.add(currentSet.get(0));
		res.add(currentSet.get(1));
		res.add(cm);
		return res;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		try {
			cm.getAction().performActionCalculateResults(src);
			return cm.getAction().getResultNewActionSet();
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
		}
		return new ArrayList<NavigationButton>();
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		return cm.getAction().getResultMainPanel();
	}
	
	@Override
	public void addAdditionalEntity(NavigationButton ne) {
	}
	
	@Override
	public ArrayList<NavigationButton> getAdditionalEntities() {
		return null;
	}
	
	@Override
	public BackgroundTaskStatusProvider getStatusProvider() {
		// MainFrame.showMessageDialog("Remote execution not yet fully implemented!", "Internal Error");
		return new BackgroundTaskStatusProviderSupportingExternalCallImpl("Remote", "Start");
	}
	
	@Override
	public String getDefaultTitle() {
		return action.getDefaultTitle();
	}
	
	@Override
	public String getDefaultTooltip() {
		return action.getDefaultTooltip();
	}
	
	@Override
	public String getDefaultNavigationImage() {
		return action.getDefaultNavigationImage();
	}
	
	@Override
	public String getDefaultImage() {
		return action.getDefaultImage();
	}
	
	@Override
	public boolean getProvidesActions() {
		return true;
	}
	
	@Override
	public NavigationImage getImageIcon() {
		return null;
	}
	
	@Override
	public boolean requestTitleUpdates() {
		return false;
	}
	
	@Override
	public ParameterOptions getParameters() {
		return remoteAction == null ? null : remoteAction.getParameters();
	}
	
	@Override
	public void setParameters(Object[] parameters) {
		//
	}
	
}
