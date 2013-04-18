/*******************************************************************************
 * Copyright (c) 2010 Image Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Oct 8, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.commands.lt;

import java.util.ArrayList;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.images.IAPimages;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;

/**
 * @author klukas
 */
public class ActionLemnaAssessment extends AbstractNavigationAction implements NavigationAction {
	
	public ActionLemnaAssessment() {
		super("Predict experiment storage and time requirements");
	}
	
	private NavigationButton src;
	
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
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		return new MainPanelComponent("This command has no function when using the desktop-mode of IAP.");
	}
	
	@Override
	public String getDefaultImage() {
		return IAPimages.getCloudResult();
	}
	
	@Override
	public String getDefaultNavigationImage() {
		return IAPimages.getCloudResult();
	}
	
	@Override
	public String getDefaultTitle() {
		return "Exp. Planning";
	}
	
}
