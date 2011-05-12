/*******************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *******************************************************************************/
/*
 * Created on Jul 29, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.gui.actions;

import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JComponent;

import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.webstart.IAPmain;

public class ActionShowVANTED extends AbstractNavigationAction {
	
	private final boolean showInline = true;
	private NavigationButton src;
	
	public ActionShowVANTED() {
		super("Show IAP Online-Version of VANTED");
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) {
		this.src = src;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
		ArrayList<NavigationButton> res = new ArrayList<NavigationButton>(currentSet);
		res.add(src);
		return res;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		if (!showInline) {
			return null;
		} else
			return new ArrayList<NavigationButton>();
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		JComponent gui = IAPmain.showVANTED(showInline);
		if (gui != null)
			gui.setBorder(BorderFactory.createLoweredBevelBorder());
		return gui != null ? new MainPanelComponent(gui) : null;
	}
	
	@Override
	public String getDefaultImage() {
		return "img/vanted1_0.png";
	}
	
	@Override
	public boolean getProvidesActions() {
		return showInline;
	}
	
	@Override
	public String getDefaultTitle() {
		return "VANTED";
	}
}