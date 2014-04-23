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
public class ActionMongoDbCompact extends AbstractNavigationAction implements NavigationAction {
	
	private final MongoDB m;
	
	public ActionMongoDbCompact(MongoDB m) {
		super("Compact database (may take several hours, database is offline during this time)");
		this.m = m;
	}
	
	private NavigationButton src;
	
	String result = "Internal Error";
	private static boolean started = false;
	
	public static boolean compactOperationRunning;
	
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
		started = true;
		ActionMongoDbCompact.compactOperationRunning = true;
		try {
			result = m.compact(status);
		} finally {
			ActionMongoDbCompact.compactOperationRunning = false;
		}
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		return new MainPanelComponent(result);
	}
	
	@Override
	public String getDefaultImage() {
		return IAPimages.getFileRoller();
	}
	
	@Override
	public String getDefaultNavigationImage() {
		return IAPimages.getFileRoller();
	}
	
	@Override
	public String getDefaultTitle() {
		if (!started)
			return "<html><center>Compact Database<br><small>Database goes offline, may take hours</small></center>";
		else
			return "<html><center>Compacting Database<br><small>Don't interrupt this long lasting operation</small></center>";
	}
	
	@Override
	public boolean requestTitleUpdates() {
		return false;
	}
}
