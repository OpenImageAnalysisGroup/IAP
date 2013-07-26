/*******************************************************************************
 * Copyright (c) 2003-2009 Plant Bioinformatics Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on May 5, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.commands;

import java.util.ArrayList;

import org.BackgroundTaskStatusProviderSupportingExternalCall;

import de.ipk.ag_ba.datasources.http_folder.NavigationImage;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.navigation_actions.ParameterOptions;
import de.ipk.ag_ba.gui.navigation_model.GUIsetting;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProviderSupportingExternalCallImpl;

/**
 * @author klukas
 */
public abstract class AbstractNavigationAction implements NavigationAction {
	
	private final ArrayList<NavigationButton> additionalEntities = new ArrayList<NavigationButton>();
	protected BackgroundTaskStatusProviderSupportingExternalCall status = new BackgroundTaskStatusProviderSupportingExternalCallImpl("", "");
	
	private String tooltip;
	protected NavigationAction srcAction;
	protected GUIsetting guiSetting;
	
	public AbstractNavigationAction(String tooltip) {
		this.tooltip = tooltip;
	}
	
	protected void setTooltip(String tooltip) {
		this.tooltip = tooltip;
	}
	
	@Override
	public void setStatusProvider(BackgroundTaskStatusProviderSupportingExternalCall status) {
		this.status = status;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
		ArrayList<NavigationButton> res = new ArrayList<NavigationButton>();
		res.addAll(currentSet);
		res.add(new NavigationButton(srcAction, guiSetting));
		return res;
	}
	
	@Override
	public boolean requestTitleUpdates() {
		return false;
	}
	
	@Override
	public ParameterOptions getParameters() {
		return null;
	}
	
	@Override
	public void setParameters(Object[] parameters) {
		// empty
	}
	
	/*
	 * (non-Javadoc)
	 * @see
	 * de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.webstart.NavigationAction
	 * #getResultMainPanel()
	 */
	@Override
	public MainPanelComponent getResultMainPanel() {
		return null;
	}
	
	@Override
	public void addAdditionalEntity(NavigationButton ne) {
		additionalEntities.add(ne);
	}
	
	@Override
	public ArrayList<NavigationButton> getAdditionalEntities() {
		return additionalEntities;
	}
	
	@Override
	public BackgroundTaskStatusProviderSupportingExternalCall getStatusProvider() {
		return status;
	}
	
	@Override
	public String getDefaultTitle() {
		return null;
	}
	
	@Override
	public String getDefaultNavigationImage() {
		return getDefaultImage();
	}
	
	@Override
	public String getDefaultImage() {
		return null;
	}
	
	@Override
	public String getDefaultTooltip() {
		return tooltip;
	}
	
	@Override
	public boolean isProvidingActions() {
		return true;
	}
	
	@Override
	public NavigationImage getImageIconInactive() {
		return null;
	}
	
	@Override
	public NavigationImage getImageIconActive() {
		return null;
	}
	
	@Override
	public void setSource(NavigationAction src, GUIsetting guiSetting) {
		this.srcAction = src;
		this.guiSetting = guiSetting;
	}
}
