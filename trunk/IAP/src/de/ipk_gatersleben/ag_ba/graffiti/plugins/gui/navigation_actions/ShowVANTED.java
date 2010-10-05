/*******************************************************************************
 * 
 *    Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 * 
 *******************************************************************************/
/*
 * Created on Jul 29, 2010 by Christian Klukas
 */

package de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.navigation_actions;

import java.util.ArrayList;

import javax.swing.JFrame;

import org.ErrorMsg;
import org.graffiti.editor.MainFrame;

import de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.navigation_model.NavigationGraphicalEntity;

public class ShowVANTED extends AbstractNavigationAction {

	public ShowVANTED() {
		super("Show IAP Online-Version of VANTED");
	}

	@Override
	public void performActionCalculateResults(NavigationGraphicalEntity src) {
	}

	@Override
	public ArrayList<NavigationGraphicalEntity> getResultNewNavigationSet(ArrayList<NavigationGraphicalEntity> currentSet) {
		return null;
	}

	@Override
	public ArrayList<NavigationGraphicalEntity> getResultNewActionSet() {
		JFrame jf = (JFrame) ErrorMsg.findParentComponent(MainFrame.getInstance(), JFrame.class);
		if (jf != null && !jf.isVisible()) {
			jf.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			jf.setVisible(true);
		}

		return null;
	}

	@Override
	public String getDefaultImage() {
		return "img/vanted1_0.png";
	}

	@Override
	public String getDefaultTitle() {
		return "Show VANTED";
	}
}