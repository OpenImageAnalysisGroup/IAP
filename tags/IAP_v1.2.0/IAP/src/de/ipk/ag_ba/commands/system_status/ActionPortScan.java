/*******************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *******************************************************************************/
/*
 * Created on Dec 11, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.commands.system_status;

import java.util.ArrayList;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.IAPservice;

/**
 * @author klukas
 */
public class ActionPortScan extends AbstractNavigationAction {
	
	private final String hostname;
	private ArrayList<String> scanResult;
	private final String image;
	
	public ActionPortScan(String hostname, String image) {
		super("Port-Scan");
		this.hostname = hostname;
		this.image = image;
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		scanResult = IAPservice.portScan(hostname, status);
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		StringBuilder htmlTextPanel = new StringBuilder("<html><b>Result of port-scan:</b><br>");
		htmlTextPanel.append("<ul>");
		for (String s : scanResult) {
			htmlTextPanel.append("<li>" + s);
		}
		htmlTextPanel.append("</ul>");
		return new MainPanelComponent(htmlTextPanel.toString());
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
		return null;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		return null;
	}
	
	@Override
	public String getDefaultTitle() {
		return hostname;
	}
	
	@Override
	public String getDefaultImage() {
		return image;
	}
}
