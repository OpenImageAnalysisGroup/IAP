/*******************************************************************************
 * Copyright (c) 2010 Image Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Nov 9, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.commands.experiment.process;

import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JTable;

import org.SystemAnalysis;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.commands.experiment.process.report.ReportRow;
import de.ipk.ag_ba.commands.experiment.view_or_export.ActionDataProcessing;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Measurement;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;

/**
 * @author klukas
 */
public class ActionNumericDataReport extends AbstractNavigationAction implements ActionDataProcessing {
	
	private ExperimentReference experimentReference;
	private NavigationButton src;
	private JTable table;
	private final static String separator = "\t";
	
	public ActionNumericDataReport(String tooltip) {
		super(tooltip);
	}
	
	public ActionNumericDataReport() {
		this("Show/export numeric data report");
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		return new ArrayList<NavigationButton>();
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
		return "img/ext/watering.png";
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		this.src = src;
		ArrayList<String> cols = new ArrayList<String>();
		Object[] columns;
		ExperimentInterface experiment = experimentReference.getData();
		ArrayList<ReportRow> rows = new ArrayList<ReportRow>();
		if (SystemAnalysis.isHeadless()) {
			columns = cols.toArray();
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
	
	public String getCSVheader() {
		return "Plant ID" + separator + "Condition" + separator + "Species" + separator + "Genotype" + separator +
				"Variety" + separator + "GrowthCondition" + separator + "Treatment" + separator + "Sequence" + separator +
				"Day" + separator + "Time" + separator + "Day (Int)" + separator + "Day (Float)" + separator + "Weight A (g)" + separator +
				"Weight B (g)" + separator + "Water (weight-diff)" + separator + "RGB" + separator + "Fluo" + separator + "Nir"
				+ separator + "Other\r\n";
	}
	
	@Override
	public boolean isImageAnalysisCommand() {
		return false;
	}
	
	@Override
	public void setExperimentReference(ExperimentReference experimentReference) {
		this.experimentReference = experimentReference;
	}
}
