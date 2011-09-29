/*******************************************************************************
 * Copyright (c) 2010 Image Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Nov 9, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.gui.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

import javax.swing.JLabel;
import javax.swing.JTable;

import org.AttributeHelper;
import org.StringManipulationTools;
import org.SystemAnalysis;

import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.mongo.IAPservice;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.server.gwt.SnapshotDataIAP;
import de.ipk.ag_ba.server.pdf_report.PdfCreator;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Measurement;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;

/**
 * @author klukas
 */
public class ActionNumericDataReportComplete extends AbstractNavigationAction {
	
	private MongoDB m;
	private ExperimentReference experimentReference;
	private NavigationButton src;
	private JTable table;
	private static final String separator = "\t";
	
	public ActionNumericDataReportComplete(String tooltip) {
		super(tooltip);
	}
	
	public ActionNumericDataReportComplete(MongoDB m, ExperimentReference experimentReference) {
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
		if (SystemAnalysis.isHeadless())
			return "Download Report Files";
		else
			return "Create PDF Report";
	}
	
	@Override
	public String getDefaultImage() {
		return "img/ext/gpl2/Gnome-X-Office-Spreadsheet-64.png";
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
			boolean pdf = !SystemAnalysis.isHeadless();
			
			if (pdf) {
				
				ArrayList<SnapshotDataIAP> snapshots;
				StringBuilder csv = new StringBuilder();
				boolean water = false;
				String csvHeader = getCSVheader();
				if (!water) {
					HashMap<String, Integer> indexInfo = new HashMap<String, Integer>();
					snapshots = IAPservice.getSnapshotsFromExperiment(null, experiment, indexInfo, false);
					TreeMap<Integer, String> cola = new TreeMap<Integer, String>();
					for (String val : indexInfo.keySet())
						cola.put(indexInfo.get(val), val);
					StringBuilder indexHeader = new StringBuilder();
					for (String val : cola.values())
						indexHeader.append(separator + val);
					csvHeader = StringManipulationTools.stringReplace(csvHeader, "\r\n", "");
					csvHeader = StringManipulationTools.stringReplace(csvHeader, "\n", "");
					csv.append(csvHeader + indexHeader.toString() + "\r\n");
				} else {
					snapshots = IAPservice.getSnapshotsFromExperiment(null, experiment, null, false);
					csv.append(csvHeader);
				}
				for (SnapshotDataIAP s : snapshots) {
					boolean germanLanguage = false;
					csv.append(s.getCSVvalue(germanLanguage, separator));
				}
				byte[] result = csv.toString().getBytes();
				
				PdfCreator p = new PdfCreator(experiment);
				
				p.prepareTempDirectory();
				p.saveReportCSV(result);
				p.saveScripts(new String[] {
							"diagramForReportPDF.r",
							"diagramIAP.cmd",
							"diagramIAP.bat",
							"initLinux.r",
							"report2.tex", "createDiagramFromValuesLinux.r"
					});
				
				p.executeRstat();
				
				boolean ok = p.hasPDFcontent();
				
				AttributeHelper.showInBrowser(p.getPDFurl());
				
				// p.deleteDirectory();
				
			} else {
				
				cols.add("Condition");
				cols.add("Time");
				cols.add("Plant ID");
				// cols.add("Measurement"); // substance
				cols.add("Replicate ID"); // substance
				// cols.add("Value");
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
		}
		
		Object[][] rowdata = new Object[rows.size()][cols.size()];
		
		table = null;
		
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		if (SystemAnalysis.isHeadless())
			return new MainPanelComponent(new JLabel());
		else
			return new MainPanelComponent("The generated PDF report will be opened automatically in a moment.");
	}
	
	public ExperimentReference getExperimentReference() {
		return experimentReference;
	}
	
	public MongoDB getMongoInstance() {
		return m;
	}
	
	public String getCSVheader() {
		return "Plant ID" + separator + "Condition" + separator + "Species" + separator + "Genotype" + separator + "Variety" + separator + "GrowthCondition"
				+ separator + "Treatment" + separator + "Sequence" + separator + "Day" + separator + "Time" + separator + "Day (Int)"
				+ separator + "Weight A (g)" + separator + "Weight B (g)" + separator +
				"Water (weight-diff)" + separator + "Water (pumped)" + separator + "RGB" + separator + "FLUO" + separator + "NIR" + separator + "OTHER" +
				"\r\n";
	}
}
