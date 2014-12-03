/*******************************************************************************
 * Copyright (c) 2003-2009 Plant Bioinformatics Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on May 5, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.gui.interfaces;

import java.util.ArrayList;

import org.BackgroundTaskStatusProviderSupportingExternalCall;

import de.ipk.ag_ba.datasources.http_folder.NavigationImage;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.navigation_actions.ParameterOptions;
import de.ipk.ag_ba.gui.navigation_actions.SideGuiComponent;
import de.ipk.ag_ba.gui.navigation_model.GUIsetting;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;

/**
 * @author klukas
 */
public interface NavigationAction {
	public void performActionCalculateResults(NavigationButton src) throws Exception;
	
	public void setStatusProvider(BackgroundTaskStatusProviderSupportingExternalCall status);
	
	/**
	 * @param currentSet
	 * @return List of resulting navigation buttons, shown after performing the
	 *         action. Add "NULL" as the last entry in the collection, to perform
	 *         a "reload", the same way as a bookmark action works.
	 */
	public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet);
	
	public ArrayList<NavigationButton> getResultNewActionSet();
	
	public MainPanelComponent getResultMainPanel();
	
	public void addAdditionalEntity(NavigationButton ne);
	
	public ArrayList<NavigationButton> getAdditionalEntities();
	
	public BackgroundTaskStatusProviderSupportingExternalCall getStatusProvider();
	
	public String getDefaultTitle();
	
	public String getDefaultTooltip();
	
	public String getDefaultNavigationImage();
	
	public String getDefaultImage();
	
	public NavigationImage getImageIconInactive();
	
	public NavigationImage getImageIconActive();
	
	public boolean requestTitleUpdates();
	
	public default boolean requestHighTitleUpdates() {
		return false;
	}
	
	public boolean requestRefresh();
	
	public ParameterOptions getParameters();
	
	public void setParameters(Object[] parameters);
	
	public void setSource(NavigationAction navigationAction, GUIsetting guiSetting);
	
	public boolean requestRightAlign();
	
	public SideGuiComponent getButtonGuiAddition();
}
