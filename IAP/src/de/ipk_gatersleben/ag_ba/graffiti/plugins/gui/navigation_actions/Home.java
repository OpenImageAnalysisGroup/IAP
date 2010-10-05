package de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.navigation_actions;

import java.util.ArrayList;

import org.BackgroundTaskStatusProvider;

import de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.MainPanelComponent;
import de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.navigation_model.NavigationGraphicalEntity;
import de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.webstart.AIPgui;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProviderSupportingExternalCallImpl;

/**
 * @author klukas
 * 
 */
public final class Home extends AbstractNavigationAction {
	/**
	 * 
	 */
	private final ArrayList<NavigationGraphicalEntity> homeActions;
	/**
	 * 
	 */
	private final ArrayList<NavigationGraphicalEntity> homeNavigation;
	/**
	 * 
	 */
	private final BackgroundTaskStatusProviderSupportingExternalCallImpl myStatus;

	/**
	 * @param homeActions
	 * @param homeNavigation
	 * @param myStatus
	 */
	public Home(ArrayList<NavigationGraphicalEntity> homeActions, ArrayList<NavigationGraphicalEntity> homeNavigation,
			BackgroundTaskStatusProviderSupportingExternalCallImpl myStatus) {
		super("IAP Home");
		this.homeActions = homeActions;
		this.homeNavigation = homeNavigation;
		this.myStatus = myStatus;
	}

	@Override
	public void performActionCalculateResults(NavigationGraphicalEntity src) {
		// no calculation needed
	}

	@Override
	public ArrayList<NavigationGraphicalEntity> getResultNewNavigationSet(ArrayList<NavigationGraphicalEntity> currentSet) {
		return homeNavigation;
	}

	@Override
	public ArrayList<NavigationGraphicalEntity> getResultNewActionSet() {
		return homeActions;
	}

	@Override
	public MainPanelComponent getResultMainPanel() {
		return new MainPanelComponent(AIPgui.getIntroTxt());
	}

	@Override
	public BackgroundTaskStatusProvider getStatusProvider() {
		return myStatus;
	}
}