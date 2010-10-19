/*******************************************************************************
 * 
 *    Copyright (c) 2003-2009 Plant Bioinformatics Group, IPK Gatersleben
 * 
 *******************************************************************************/
/*
 * Created on May 5, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.gui.interfaces;

import java.util.ArrayList;

import org.BackgroundTaskStatusProvider;

import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.navigation_model.NavigationGraphicalEntity;

/**
 * @author klukas
 * 
 */
public interface NavigationAction {
	public void performActionCalculateResults(NavigationGraphicalEntity src) throws Exception;

	public ArrayList<NavigationGraphicalEntity> getResultNewNavigationSet(ArrayList<NavigationGraphicalEntity> currentSet);

	public ArrayList<NavigationGraphicalEntity> getResultNewActionSet();

	public MainPanelComponent getResultMainPanel();

	public void setOneTimeFinishAction(Runnable runnable);

	public void addAdditionalEntity(NavigationGraphicalEntity ne);

	public ArrayList<NavigationGraphicalEntity> getAdditionalEntities();

	public BackgroundTaskStatusProvider getStatusProvider();

	public String getDefaultTitle();

	public String getDefaultTooltip();

	public String getDefaultNavigationImage();

	public String getDefaultImage();

	public boolean getProvidesActions();
}
