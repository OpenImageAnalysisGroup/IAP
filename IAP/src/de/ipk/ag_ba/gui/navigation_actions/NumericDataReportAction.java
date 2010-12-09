/*******************************************************************************
 * Copyright (c) 2010 Image Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Nov 9, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.gui.navigation_actions;

import java.util.ArrayList;

import javax.swing.JTable;

import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Measurement;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;

/**
 * @author klukas
 */
public class NumericDataReportAction extends AbstractNavigationAction {

	private MongoDB m;
	private ExperimentReference experimentReference;
	private NavigationButton src;
	private JTable table;

	public NumericDataReportAction(String tooltip) {
		super(tooltip);
	}

	public NumericDataReportAction(MongoDB m, ExperimentReference experimentReference) {
		this("Show/export numeric data report");
		this.m = m;
		this.experimentReference = experimentReference;
	}

	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		return null;
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
		ArrayList<String> cols = new ArrayList<String>();
		cols.add("Plant");
		cols.add("Carrier");
		cols.add("Experiment");
		cols.add("Time");
		cols.add("Weight (before watering)");
		cols.add("Weight (after watering)");
		cols.add("Water");
		Object[] columns = cols.toArray();

		ExperimentInterface experiment = experimentReference.getData(m);
		ArrayList<ReportRow> rows = new ArrayList<ReportRow>();
		for (SubstanceInterface su : experiment) {
			if (su.getSubstanceName() == null)
				continue;

			if (su.getSubstanceName().equals("weight_before")) {

			}
			if (su.getSubstanceName().equals("water_weight")) {

			}
			if (su.getSubstanceName().equals("water_amount")) {

			}
			for (ConditionInterface c : su) {
				for (SampleInterface sa : c) {
					for (Measurement m : sa) {
						ReportRow r = new ReportRow();
						r.setPlant(c.getConditionId() + ": " + c.getConditionName());
						r.setCarrier(m.getReplicateID());
						r.setExperiment(experiment.getHeader().getExperimentname());
						r.setTime(sa.getSampleTime());
					}
				}
			}
		}

		ArrayList<NumericMeasurementInterface> workload = new ArrayList<NumericMeasurementInterface>();

		Object[][] rowdata = new Object[rows.size()][cols.size()];

		table = new JTable(rowdata, columns);
	}

	@Override
	public MainPanelComponent getResultMainPanel() {
		return new MainPanelComponent(table);
	}
}
