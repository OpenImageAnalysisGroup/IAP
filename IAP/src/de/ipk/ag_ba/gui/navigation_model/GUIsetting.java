/*******************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *******************************************************************************/
/*
 * Created on Oct 23, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.gui.navigation_model;

import javax.swing.JComponent;
import javax.swing.JPanel;

import de.ipk.ag_ba.gui.MyNavigationPanel;

/**
 * @author klukas
 */
public class GUIsetting {

	private final MyNavigationPanel navigationPanel;
	private final MyNavigationPanel actionPanel;
	private final JPanel graphPanel;

	public GUIsetting(MyNavigationPanel navigationPanel, MyNavigationPanel actionPanel, JPanel graphPanel) {
		this.navigationPanel = navigationPanel;
		this.actionPanel = actionPanel;
		this.graphPanel = graphPanel;
	}

	public MyNavigationPanel getNavigationPanel() {
		return navigationPanel;
	}

	public MyNavigationPanel getActionPanel() {
		return actionPanel;
	}

	public JComponent getGraphPanel() {
		return graphPanel;
	}

}
