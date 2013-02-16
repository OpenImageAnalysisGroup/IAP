/*******************************************************************************
 * Copyright (c) 2010 Image Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Sep 30, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.commands.database_tools;

import java.util.ArrayList;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.images.IAPimages;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk_gatersleben.ag_nw.graffiti.MyInputHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Experiment;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.MeasurementNodeType;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Substance3D;

/**
 * @author klukas
 */
public class ActionDeleteSelectedAnalysisSplitResults extends AbstractNavigationAction {
	
	private final MongoDB m;
	private int deletedTempDatasets;
	
	public ActionDeleteSelectedAnalysisSplitResults(final MongoDB m) {
		super("Deletes selected temporary analysis results");
		this.m = m;
	}
	
	@Override
	public String getDefaultImage() {
		return IAPimages.getCloseCross();
	}
	
	@Override
	public String getDefaultTitle() {
		return "<html><center>Deletes selected<br>" +
				"temporary analysis results</center>";
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		return null;
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		return new MainPanelComponent("Removed "
				+ deletedTempDatasets
				+ " intermediate result experiment data sets.");
	}
	
	@Override
	public boolean isProvidingActions() {
		return false;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
		return currentSet;
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		deletedTempDatasets = 0;
		ArrayList<ExperimentHeaderInterface> ehl = new ArrayList<ExperimentHeaderInterface>();
		ArrayList<String> descL = new ArrayList<String>();
		int idx = 0;
		ArrayList<ExperimentHeaderInterface> mmll = m.getExperimentList(null);
		for (ExperimentHeaderInterface ei : mmll) {
			idx++;
			status.setCurrentStatusText1("Process " + idx + "/" + mmll.size());
			status.setCurrentStatusValueFine(100d * idx / mmll.size());
			if (ei.getExperimentName() == null || ei.getExperimentName().length() == 0 || ei.getExperimentName().contains("§")) {
				status.setCurrentStatusText2("Load Dataset");
				ExperimentReference eD = new ExperimentReference(ei.getOriginDbId());
				String pe = eD.getExperimentName();
				ExperimentReference er = new ExperimentReference(ei, m);
				Experiment ee = (Experiment) er.getData(m);
				ehl.add(ei);
				String expN = ee.getName();
				int ml = ee.getNumberOfMeasurementValues();
				int ic = Substance3D.countMeasurementValues2(ee, MeasurementNodeType.IMAGE);
				descL.add(
						"<html>Analysis of " + pe + ":<br>"
								+ expN + "<br>"
								+ "Measurement values: " + ml + "<br>" +
								"Images: " + ic);
			} else
				status.setCurrentStatusText2("Skip Dataset");
		}
		ArrayList<Object> pl = new ArrayList<Object>();
		for (String desc : descL) {
			pl.add(desc);
			pl.add(Boolean.FALSE);
		}
		Object[] res = MyInputHelper.getInput("Select the experiments to be deleted:", "Select Datasets",
				pl.toArray());
		for (ExperimentHeaderInterface ei : m.getExperimentList(null)) {
			if (ei.getExperimentName() == null || ei.getExperimentName().length() == 0 || ei.getExperimentName().contains("§")) {
				// m.deleteExperiment(ei.getDatabaseId());
				deletedTempDatasets += 1;
			}
		}
	}
}
