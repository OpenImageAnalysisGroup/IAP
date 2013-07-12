/*******************************************************************************
 * Copyright (c) 2010 Image Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Oct 8, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.commands.mongodb;

import java.util.ArrayList;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.images.IAPimages;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.mongo.MongoDB;

/**
 * @author klukas
 */
public class ActionMongoDbReorganize extends AbstractNavigationAction implements NavigationAction {
	
	private final MongoDB m;
	
	public ActionMongoDbReorganize(MongoDB m) {
		super("Delete stale binary files");
		this.m = m;
	}
	
	private NavigationButton src;
	
	String result = "Internal Error";
	
	private final boolean compactDatabase = false;
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		ArrayList<NavigationButton> result = new ArrayList<NavigationButton>();
		return result;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
		ArrayList<NavigationButton> result = new ArrayList<NavigationButton>(currentSet);
		result.add(src);
		return result;
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		this.src = src;
		result = m.cleanUp(status, compactDatabase);
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		return new MainPanelComponent(result);
	}
	
	@Override
	public String getDefaultImage() {
		return IAPimages.getFileCleaner();
	}
	
	@Override
	public String getDefaultNavigationImage() {
		return IAPimages.getFileCleaner();
	}
	
	@Override
	public String getDefaultTitle() {
		return "<html><center>Reorganize<br>" +
				"<small>Remove stale DB entries</small>" +
				"</center>";
	}
	
}
