package de.ipk.ag_ba.gui.navigation_model;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.TreeSet;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.ErrorMsg;

import de.ipk.ag_ba.datasources.http_folder.NavigationImage;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.navigation_actions.ParameterOptions;
import de.ipk.ag_ba.gui.navigation_actions.SideGuiComponent;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.server.task_management.BatchCmd;
import de.ipk.ag_ba.server.task_management.CloudAnalysisStatus;
import de.ipk.ag_ba.server.task_management.RemoteCapableAnalysisAction;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProviderSupportingExternalCallImpl;

public class RemoteExecutionWrapperAction implements NavigationAction {
	
	private final RemoteCapableAnalysisAction remoteAction;
	private final NavigationAction action;
	private final NavigationButton cm;
	private NavigationButton src;
	private Date newestImportDate;
	private String databaseIdOfNewestResultData;
	
	public RemoteExecutionWrapperAction(NavigationAction navigationAction, NavigationButton cm) {
		this.remoteAction = (RemoteCapableAnalysisAction) navigationAction;
		this.action = navigationAction;
		this.cm = cm;
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		this.src = src;
		// ArrayList<CloudHost> targetIPs = remoteAction.getMongoDB().batchGetAvailableHosts(10000);
		// if (false && targetIPs.isEmpty()) {
		// MainFrame.showMessageDialog("No active compute node found.", "Information");
		// } else {
		String remoteCapableAnalysisActionClassName = remoteAction.getClass().getCanonicalName();
		String remoteCapableAnalysisActionParams = null;
		String experimentInputMongoID = remoteAction.getDatasetID();
		int numberOfJobs = remoteAction.getNumberOfJobs();
		
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
			// for (CloudHost h : targetIPs)
			// ips.add(h.getHostName());
			cmd.setTargetIPs(ips);
			cmd.setSubTaskInfo(id, jobIDs.size());
			cmd.setRemoteCapableAnalysisActionClassName(remoteCapableAnalysisActionClassName);
			cmd.setRemoteCapableAnalysisActionParams(remoteCapableAnalysisActionParams);
			cmd.setExperimentMongoID(experimentInputMongoID);
			cmd.setCpuTargetUtilization(remoteAction.getCpuTargetUtilization());
			cmd.setNewstAvailableData(newestImportDate, databaseIdOfNewestResultData);
			MongoDB m = remoteAction.getMongoDB();
			if (m == null)
				m = MongoDB.getDefaultCloud();
			BatchCmd.enqueueBatchCmd(m, cmd);
			if (cm != null)
				cm.getAction().performActionCalculateResults(src);
		}
		System.out.println("Enqueued " + jobIDs.size() + " new jobs!");
		// }
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
		ArrayList<NavigationButton> res = new ArrayList<NavigationButton>();
		if (currentSet != null && currentSet.size() > 2) {
			res.add(currentSet.get(0));
			res.add(currentSet.get(1));
		}
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
	public BackgroundTaskStatusProviderSupportingExternalCall getStatusProvider() {
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
	public NavigationImage getImageIconInactive() {
		return null;
	}
	
	@Override
	public NavigationImage getImageIconActive() {
		return getImageIconInactive();
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
	
	@Override
	public void setStatusProvider(BackgroundTaskStatusProviderSupportingExternalCall status) {
		if (remoteAction != null)
			remoteAction.setStatusProvider(status);
	}
	
	@Override
	public void setSource(NavigationAction navigationAction, GUIsetting guiSetting) {
	}
	
	public void setNewestAvailableData(Date newestImportDate, String databaseIdOfNewestResultData) {
		this.newestImportDate = newestImportDate;
		this.databaseIdOfNewestResultData = databaseIdOfNewestResultData;
	}
	
	@Override
	public boolean requestRefresh() {
		return false;
	}
	
	@Override
	public boolean requestRightAlign() {
		return remoteAction != null && remoteAction.requestRightAlign();
	}
	
	@Override
	public SideGuiComponent getButtonGuiAddition() {
		return remoteAction != null ? remoteAction.getButtonGuiAddition() : null;
	}
}
