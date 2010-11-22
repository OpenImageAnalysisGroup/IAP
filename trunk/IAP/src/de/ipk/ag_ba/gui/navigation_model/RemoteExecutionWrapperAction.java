package de.ipk.ag_ba.gui.navigation_model;

import java.util.ArrayList;
import java.util.HashSet;

import org.BackgroundTaskStatusProvider;

import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.rmi_server.task_management.BatchCmd;
import de.ipk.ag_ba.rmi_server.task_management.RemoteCapableAnalysisAction;

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
		String remoteCapableAnalysisActionClassName = remoteAction.getClass().getCanonicalName();
		String remoteCapableAnalysisActionParams = null;
		String experimentInputMongoID = remoteAction.getMongoDatasetID();
		BatchCmd.enqueueBatchCmd(targetIPs, remoteCapableAnalysisActionClassName, remoteCapableAnalysisActionParams, experimentInputMongoID);
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
		return null;
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
}
