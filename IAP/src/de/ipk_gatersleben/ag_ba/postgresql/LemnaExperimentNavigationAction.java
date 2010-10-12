/*******************************************************************************
 * 
 *    Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 * 
 *******************************************************************************/
/*
 * Created on Aug 18, 2010 by Christian Klukas
 */

package de.ipk_gatersleben.ag_ba.postgresql;

import java.util.ArrayList;

import org.ErrorMsg;

import de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.ImageAnalysisCommandManager;
import de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.MainPanelComponent;
import de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.navigation_actions.AbstractNavigationAction;
import de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.navigation_actions.Other;
import de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.navigation_model.NavigationGraphicalEntity;
import de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.util.ExperimentReference;
import de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.util.MyExperimentInfoPanel;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.threading.SystemAnalysis;

/**
 * @author klukas
 * 
 */
public class LemnaExperimentNavigationAction extends AbstractNavigationAction {
	private NavigationGraphicalEntity src;
	private ExperimentInterface experiment = null;
	private final ExperimentHeaderInterface experimentName;

	public LemnaExperimentNavigationAction(ExperimentHeaderInterface experimentName) {
		super("<html>Access LemnaTec Data Set");
		this.experimentName = experimentName;
	}

	@Override
	public ArrayList<NavigationGraphicalEntity> getResultNewActionSet() {
		ArrayList<NavigationGraphicalEntity> actions = new ArrayList<NavigationGraphicalEntity>();
		// actions.add(FileManager.getFileManagerEntity(login, pass,
		// ei.experimentName));

		if (experiment != null) {
			getDefaultActions(actions, experiment, experiment.getHeader(), true);
		}
		return actions;
	}

	public static void getDefaultActions(ArrayList<NavigationGraphicalEntity> actions, ExperimentInterface experiment,
			ExperimentHeaderInterface header, boolean imageAnalysis) {
		try {
			if (imageAnalysis)
				for (NavigationGraphicalEntity ne : ImageAnalysisCommandManager.getCommands(SystemAnalysis.getUserName(),
						null, new ExperimentReference(experiment)))
					actions.add(ne);
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
		}
		for (NavigationGraphicalEntity ne : Other.getProcessExperimentDataWithVantedEntities(null, null,
				new ExperimentReference(experiment))) {
			if (ne.getTitle().contains("Put data")) {
				ne.setTitle("View in VANTED");
				actions.add(ne);
			}
		}
	}

	@Override
	public ArrayList<NavigationGraphicalEntity> getResultNewNavigationSet(ArrayList<NavigationGraphicalEntity> currentSet) {
		ArrayList<NavigationGraphicalEntity> res = new ArrayList<NavigationGraphicalEntity>(currentSet);
		res.add(src);
		return res;
	}

	@Override
	public void performActionCalculateResults(NavigationGraphicalEntity src) throws Exception {
		this.src = src;
		if (experiment == null)
			experiment = new LemnaTecDataExchange().getExperiment(experimentName);
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
		ip.setExperimentInfo(SystemAnalysis.getUserName(), null, experiment.getHeader(), false, experiment);
		return new MainPanelComponent(ip, true);
	}
}
