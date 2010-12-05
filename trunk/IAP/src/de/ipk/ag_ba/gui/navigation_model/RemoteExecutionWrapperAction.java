package de.ipk.ag_ba.gui.navigation_model;

import java.util.ArrayList;
import java.util.HashSet;

import javax.swing.ImageIcon;

import org.BackgroundTaskStatusProvider;
import org.graffiti.editor.MainFrame;

import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.rmi_server.task_management.BatchCmd;
import de.ipk.ag_ba.rmi_server.task_management.CloudAnalysisStatus;
import de.ipk.ag_ba.rmi_server.task_management.RemoteCapableAnalysisAction;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProviderSupportingExternalCallImpl;

public class RemoteExecutionWrapperAction implements NavigationAction {

	private final RemoteCapableAnalysisAction remoteAction;
	private final NavigationAction action;

	public RemoteExecutionWrapperAction(NavigationAction navigationAction) {
		this.remoteAction = (RemoteCapableAnalysisAction) navigationAction;
		this.action = navigationAction;
	}

	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		HashSet<String> targetIPs = new MongoDB().batchGetAvailableHosts(10000);
		if (targetIPs.isEmpty()) {
			MainFrame.showMessageDialog("No active compute host found.", "Information");
		} else {
			String remoteCapableAnalysisActionClassName = remoteAction.getClass().getCanonicalName();
			String remoteCapableAnalysisActionParams = null;
			String experimentInputMongoID = remoteAction.getMongoDatasetID();
			BatchCmd cmd = new BatchCmd();
			cmd.setRunStatus(CloudAnalysisStatus.SCHEDULED);
			cmd.setSubmissionTime(System.currentTimeMillis());
			cmd.setTargetIPs(targetIPs);
			cmd.setRemoteCapableAnalysisActionClassName(remoteCapableAnalysisActionClassName);
			cmd.setRemoteCapableAnalysisActionParams(remoteCapableAnalysisActionParams);
			cmd.setExperimentMongoID(experimentInputMongoID);
			BatchCmd.enqueueBatchCmd(cmd);
		}
	}

	@Override
	public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
		return null;
	}

	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		return null;
	}

	@Override
	public MainPanelComponent getResultMainPanel() {
		return null;
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
		return false;
	}

	@Override
	public ImageIcon getImageIcon() {
		return null;
	}
}
