/*******************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *******************************************************************************/
/*
 * Created on Aug 18, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.postgresql;

import java.util.ArrayList;

import org.ErrorMsg;

import de.ipk.ag_ba.gui.ImageAnalysisCommandManager;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.navigation_actions.AbstractNavigationAction;
import de.ipk.ag_ba.gui.navigation_actions.Other;
import de.ipk.ag_ba.gui.navigation_model.GUIsetting;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.gui.util.MyExperimentInfoPanel;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;

/**
 * @author klukas
 */
public class LemnaExperimentNavigationAction extends AbstractNavigationAction {
	private NavigationButton src;
	private ExperimentInterface experiment = null;
	private final ExperimentHeaderInterface experimentName;
	
	public LemnaExperimentNavigationAction(ExperimentHeaderInterface experimentName) {
		super("<html>Access LemnaTec Data Set");
		this.experimentName = experimentName;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		ArrayList<NavigationButton> actions = new ArrayList<NavigationButton>();
		// actions.add(FileManager.getFileManagerEntity(login, pass,
		// ei.experimentName));
		
		if (experiment != null) {
			getDefaultActions(actions, experiment, experiment.getHeader(), true, src.getGUIsetting(), null);
		}
		return actions;
	}
	
	public static void getDefaultActions(ArrayList<NavigationButton> actions, ExperimentInterface experiment,
						ExperimentHeaderInterface header, boolean imageAnalysis, GUIsetting guiSetting, MongoDB m) {
		try {
			if (imageAnalysis)
				for (NavigationButton ne : ImageAnalysisCommandManager.getCommands(m, new ExperimentReference(experiment), guiSetting))
					actions.add(ne);
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
		}
		for (NavigationButton ne : Other.getProcessExperimentDataWithVantedEntities(m,
							new ExperimentReference(experiment), guiSetting)) {
			if (ne.getTitle().contains("Put data")) {
				ne.setTitle("Analyze with VANTED");
				actions.add(ne);
			}
		}
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
		ArrayList<NavigationButton> res = new ArrayList<NavigationButton>(currentSet);
		res.add(src);
		return res;
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		this.src = src;
		if (experiment == null)
			experiment = new LemnaTecDataExchange().getExperiment(experimentName, status);
	}
	
	@Override
	public String getDefaultImage() {
		return "img/000Grad_3-gray.png";
	}
	
	@Override
	public String getDefaultNavigationImage() {
		return "img/000Grad_3.png";
	}
	
	@Override
	public String getDefaultTitle() {
		return experimentName.getExperimentname();
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		MyExperimentInfoPanel ip = new MyExperimentInfoPanel();
		ip.setExperimentInfo(null, experiment.getHeader(), false, experiment);
		return new MainPanelComponent(ip, true);
	}
}
