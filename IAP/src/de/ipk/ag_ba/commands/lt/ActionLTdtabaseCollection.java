/*******************************************************************************
 * Copyright (c) 2010 Image Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Oct 8, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.commands.lt;

import java.util.ArrayList;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;

/**
 * @author klukas
 */
public class ActionLTdtabaseCollection extends AbstractNavigationAction implements NavigationAction {
	
	private NavigationButton src;
	private final ArrayList<NavigationButton> prepared;
	
	public ActionLTdtabaseCollection(ArrayList<NavigationButton> prepared) {
		super("Show list of unsorted (old) experiments");
		this.prepared = prepared;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		return prepared;
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
	}
	
	@Override
	public String getDefaultImage() {
		return "img/DBE2_logo-gray_s.png";
	}
	
	@Override
	public String getDefaultNavigationImage() {
		return "img/DBE2_logo_s.png";
	}
	
	@Override
	public String getDefaultTitle() {
		return "Unsorted Data";
	}
	
}
