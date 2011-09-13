/*******************************************************************************
 * Copyright (c) 2010 Image Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Nov 9, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.gui.actions;

import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JTable;

import org.SystemAnalysis;

import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Measurement;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;

/**
 * @author klukas
 */
public class ActionNumericDataReport extends AbstractNavigationAction {
	
	private MongoDB m;
	private ExperimentReference experimentReference;
	private NavigationButton src;
	private JTable table;
	private final static String separator = "\t";
	
	public ActionNumericDataReport(String tooltip) {
		super(tooltip);
	}
	
	public ActionNumericDataReport(MongoDB m, ExperimentReference experimentReference) {
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
	public String getDefaultTitle() {
		return "Watering Table";
	}
	
	@Override
	public String getDefaultImage() {
		return "img/ext/applications-office.png";
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		this.src = src;
		ArrayList<String> cols = new ArrayList<String>();
		Object[] columns;
		ExperimentInterface experiment = experimentReference.getData(m);
		ArrayList<ReportRow> rows = new ArrayList<ReportRow>();
		if (SystemAnalysis.isHeadless()) {
			// cols.add("Plant");
			// cols.add("Carrier");
			// cols.add("Experiment");
			// cols.add("Time");
			// cols.add("Weight (before watering)");
			// cols.add("Weight (after watering)");
			// cols.add("Water");
			columns = cols.toArray();
			//
			// for (SubstanceInterface su : experiment) {
			// if (su.getName() == null)
			// continue;
			//
			// if (su.getName().equals("weight_before")) {
			//
			// }
			// if (su.getName().equals("water_weight")) {
			//
			// }
			// if (su.getName().equals("water_amount")) {
			//
			// }
			// for (ConditionInterface c : su) {
			// for (SampleInterface sa : c) {
			// for (Measurement m : sa) {
			// ReportRow r = new ReportRow();
			// r.setPlant(c.getConditionId() + ": " + c.getConditionName());
			// r.setCarrier(m.getReplicateID());
			// r.setExperiment(experiment.getHeader().getExperimentname());
			// r.setTime(sa.getSampleTime());
			// }
			// }
			// }
			// }
		} else {
			cols.add("Condition");
			// Species;Genotype;Variety;GrowthCondition;Treatment;Sequence;
			cols.add("Time");
			cols.add("Plant ID");
			cols.add("Measurement"); // substance
			cols.add("Replicate ID"); // substance
			cols.add("Value");
			columns = cols.toArray();
			
			HashMap<String, Integer> id2row = new HashMap<String, Integer>();
			for (SubstanceInterface su : experiment) {
				String sid = su.getName();
				String hue;
				int hueVal = 0;
				if (sid.contains("hue=")) {
					hue = sid.substring(sid.indexOf("hue=") + "hue=".length()).trim();
					try {
						hueVal = Integer.parseInt(hue);
					} catch (Exception e) {
						// empty
					}
				} else
					continue;
				for (ConditionInterface c : su) {
					String cid = sid + separator + c.getConditionId() + "-" + c.getConditionName();
					for (SampleInterface sa : c) {
						String said = cid + separator + sa.getSampleTime();
						for (Measurement m : sa) {
							String mid = said + separator + m.getReplicateID();
							if (m.getValue() > 0)
								System.out.println(mid + separator + hueVal + separator + m.getValue());
						}
					}
				}
			}
			
		}
		
		Object[][] rowdata = new Object[rows.size()][cols.size()];
		
		table = new JTable(rowdata, columns);
		
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		return new MainPanelComponent(table);
	}
	
	public ExperimentReference getExperimentReference() {
		return experimentReference;
	}
	
	public MongoDB getMongoInstance() {
		return m;
	}
	
	public String getCSVheader() {
		return "Plant ID" + separator + "Condition" + separator + "Species" + separator + "Genotype" + separator +
					"Variety" + separator + "GrowthCondition" + separator + "Treatment" + separator + "Sequence" + separator +
					"Day" + separator + "Time" + separator + "Day (Int)" + separator + "Weight A (g)" + separator +
					"Weight B (g)" + separator + "Water" + separator + "RGB" + separator + "Fluo" + separator + "Nir" + separator + "Other\r\n";
	}
}
