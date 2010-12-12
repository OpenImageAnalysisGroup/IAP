package de.ipk.ag_ba.gui.navigation_model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeSet;

import org.BackgroundTaskStatusProvider;
import org.graffiti.editor.MainFrame;

import de.ipk.ag_ba.datasources.http_folder.NavigationImage;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.rmi_server.task_management.BatchCmd;
import de.ipk.ag_ba.rmi_server.task_management.CloudAnalysisStatus;
import de.ipk.ag_ba.rmi_server.task_management.RemoteCapableAnalysisAction;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProviderSupportingExternalCallImpl;

public class RemoteExecutionWrapperAction implements NavigationAction {

	private final RemoteCapableAnalysisAction remoteAction;
	private final NavigationAction action;
	private final NavigationButton cm;

	public RemoteExecutionWrapperAction(NavigationAction navigationAction, NavigationButton cm) {
		this.remoteAction = (RemoteCapableAnalysisAction) navigationAction;
		this.action = navigationAction;
		this.cm = cm;
	}

	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		HashSet<String> targetIPs = remoteAction.getMongoDB().batchGetAvailableHosts(10000);
		if (targetIPs.isEmpty()) {
			MainFrame.showMessageDialog("No active compute node found.", "Information");
		} else {
			String remoteCapableAnalysisActionClassName = remoteAction.getClass().getCanonicalName();
			String remoteCapableAnalysisActionParams = null;
			String experimentInputMongoID = remoteAction.getMongoDatasetID();
			TreeSet<Integer> jobIDs = new TreeSet<Integer>();
			{
				int idx = 0;
				while (jobIDs.size() < 20)
					jobIDs.add(idx++);
			}
			for (int id : jobIDs) {
				BatchCmd cmd = new BatchCmd();
				cmd.setRunStatus(CloudAnalysisStatus.SCHEDULED);
				cmd.setSubmissionTime(System.currentTimeMillis());
				cmd.setTargetIPs(targetIPs);
				cmd.setSubTaskInfo(id, jobIDs.size());
				cmd.setRemoteCapableAnalysisActionClassName(remoteCapableAnalysisActionClassName);
				cmd.setRemoteCapableAnalysisActionParams(remoteCapableAnalysisActionParams);
				cmd.setExperimentMongoID(experimentInputMongoID);
				BatchCmd.enqueueBatchCmd(remoteAction.getMongoDB(), cmd);
				cm.getAction().performActionCalculateResults(src);
			}
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
		return cm.getAction().getResultNewActionSet();
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
}
