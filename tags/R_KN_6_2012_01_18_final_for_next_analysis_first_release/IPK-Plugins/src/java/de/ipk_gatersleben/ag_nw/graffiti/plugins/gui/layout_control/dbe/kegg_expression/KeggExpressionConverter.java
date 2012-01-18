/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.kegg_expression;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JLabel;
import javax.swing.JTextField;

import org.FolderPanel;
import org.StringManipulationTools;

import de.ipk_gatersleben.ag_nw.graffiti.MyInputHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.TableData;

public class KeggExpressionConverter {
	
	ArrayList<KeggExpressionDataset> datasets;
	HashMap<String, JTextField> filename2organism = new HashMap<String, JTextField>();
	HashMap<String, JTextField> filename2organismId = new HashMap<String, JTextField>();
	HashMap<String, JTextField> filename2time = new HashMap<String, JTextField>();
	HashMap<String, JTextField> filename2replicate = new HashMap<String, JTextField>();
	
	String expName = "expression analysis";
	String coordinator = "experiment coordinator";
	String startOfExp = "";
	String timeUnit = "-1";
	String measurementUnit = "expression";
	
	public KeggExpressionConverter(ArrayList<KeggExpressionDataset> datasets) {
		this.datasets = datasets;
	}
	
	public String getDesiredExperimentName() {
		return expName;
	}
	
	public String getDesiredCoordinatorValue() {
		return coordinator;
	}
	
	public String getDesiredTimeUnit() {
		return timeUnit;
	}
	
	public String getDesiredMeasurementUnit() {
		return measurementUnit;
	}
	
	public void getDescriptionDataFromUser() {
		// ask user for:
		// experiment name
		// coordinator
		// start of experiment
		// time unit
		//
		// fileName | Plant-ID | Time-Point | Replicate ID
		// ------------------------------------------------
		// (no edit) | ? | ? | ?
		//
		// evaluate datasets for fileName, and ask for remaining field data
		
		FolderPanel table = new FolderPanel("[organism] [line or treatment] [time point] [replicate]", false, true, false, null);
		table.setMaximumRowCount(10);
		for (KeggExpressionDataset ked : datasets) {
			String filename = ked.getFileName();
			if (filename == null)
				filename = "";
			JLabel fnLbl = new JLabel("<html>" + filename + ":&nbsp;&nbsp;");
			String on = ked.getOrganismName();
			if (on == null)
				on = "";
			JTextField organismId = new JTextField(on + "");
			String defaultTime = "-1";
			try {
				filename = StringManipulationTools.stringReplace(filename, "\"", "");
				filename = StringManipulationTools.stringReplace(filename, "\'", "");
				if (filename.endsWith(".0"))
					filename = filename.substring(0, filename.length() - ".0".length());
				defaultTime = Integer.parseInt(filename) + "";
			} catch (Exception e) {
				defaultTime = "-1";
			}
			String gt = "genotype";
			if (defaultTime.equalsIgnoreCase("-1"))
				gt = filename;
			JTextField organismInput = new JTextField(gt);
			JTextField timePointInput = new JTextField(defaultTime);
			JTextField replicateInput = new JTextField("1");
			filename2organismId.put(filename, organismId);
			filename2organism.put(filename, organismInput);
			filename2time.put(filename, timePointInput);
			filename2replicate.put(filename, replicateInput);
			table.addGuiComponentRow(fnLbl, TableLayout.get3Split(
								TableLayout.getSplit(organismId, organismInput, 40, 70),
								timePointInput, replicateInput,
								TableLayoutConstants.PREFERRED, 30, 30, 10, 5), false);
		}
		table.layoutRows();
		
		Object[] res = MyInputHelper.getInput("<html>" +
							"Please specify additional dataset information:<br>" +
							"* these values are optional<br>" +
							"time-point and replicate fields need to be filled with<br>" +
							"whole numbers (0, 1, 2, ...). If no time points need to be<br>" +
							"specified, enter -1 for all time points and also -1 for the<br>" +
							"time unit setting.<br>" +
							"If only one replicate has been measured, enter 1 as its<br>" +
							"replicate ID, otherwise specify different whole numbers.",
							"Prepare Dataset",
							new Object[] {
												"Experiment - Name", expName,
												"Coordinator*", coordinator,
												"Start of Experiment*", "",
												"Time Unit*", timeUnit,
												"Measurement Unit*", measurementUnit,
												"", table

							});
		if (res != null) {
			int i = 0;
			expName = (String) res[i++];
			coordinator = (String) res[i++];
			startOfExp = (String) res[i++];
			timeUnit = (String) res[i++];
			measurementUnit = (String) res[i++];
		} else {
			filename2organismId.clear();
			filename2organism.clear();
			filename2time.clear();
			filename2replicate.clear();
		}
	}
	
	public TableData getDatasetTable() {
		TableData tab = new TableData();
		int row = 1;
		tab.addCellData(1 - 1, row - 1, "clust-ID"); // gene identifier column header
		tab.addCellData(2 - 1, row - 1, "info");
		row++;
		int col = 3;
		HashMap<String, Integer> knownGene2row = new HashMap<String, Integer>();
		for (KeggExpressionDataset ked : datasets) {
			String filename = ked.getFileName();
			if (filename2organismId.get(filename) == null)
				continue;
			String organism = filename2organismId.get(filename).getText();
			String plant = filename2organism.get(filename).getText();
			String time = filename2time.get(filename).getText();
			String repl = filename2replicate.get(filename).getText();
			if (ked.isTrueKeggExpressionFormatControlTarget()) {
				String columnHeaderA = time + "_" + plant + "-control" + "_" + repl;
				String columnHeaderB = time + "_" + plant + "-target" + "_" + repl;
				String columnHeaderC = time + "_" + plant + "-LOG2(target/control)" + "_" + repl;
				tab.addCellData(col - 1, 1 - 1, columnHeaderA);
				tab.addCellData(col + 1 - 1, 1 - 1, columnHeaderB);
				tab.addCellData(col + 2 - 1, 1 - 1, columnHeaderC);
			} else {
				String columnHeader = time + "_" + plant + "_" + repl;
				tab.addCellData(col - 1, 1 - 1, columnHeader);
			}
			String orgPrefix;
			if (organism != null && organism.length() > 0)
				orgPrefix = organism + ":";
			else
				orgPrefix = "";
			for (KeggExpressionDatapoint dp : ked.getDataPoints()) {
				String activeGeneId = orgPrefix + dp.getGeneId();
				if (dp.getGeneId().indexOf("^") >= 0) {
					activeGeneId = StringManipulationTools.stringReplace(activeGeneId, "^", "^" + orgPrefix);
				}
				if (knownGene2row.containsKey(activeGeneId)) {
					int knownRow = knownGene2row.get(activeGeneId);
					if (ked.isTrueKeggExpressionFormatControlTarget())
						tab.addCellData(col - 1, knownRow - 1, dp.getControlValue());
					else
						tab.addCellData(col - 1, knownRow - 1, dp.getControlValue() + dp.getOptQualityTag(":", ""));
					
					if (ked.isTrueKeggExpressionFormatControlTarget())
						tab.addCellData(col + 1 - 1, knownRow - 1, dp.getTargetValue());
					if (ked.isTrueKeggExpressionFormatControlTarget())
						tab.addCellData(col + 2 - 1, knownRow - 1, dp.getLog2TargetDivControlValue());
				} else {
					knownGene2row.put(activeGeneId, row);
					tab.addCellData(1 - 1, row - 1, activeGeneId);
					if (dp.getOptX() != null && dp.getOptY() != null)
						tab.addCellData(2 - 1, row - 1, dp.getOptX() + ":" + dp.getOptY());
					
					if (ked.isTrueKeggExpressionFormatControlTarget())
						tab.addCellData(col - 1, row - 1, dp.getControlValue());
					else
						tab.addCellData(col - 1, row - 1, dp.getControlValue() + dp.getOptQualityTag(":", ""));
					
					if (ked.isTrueKeggExpressionFormatControlTarget())
						tab.addCellData(col + 1 - 1, row - 1, dp.getTargetValue());
					if (ked.isTrueKeggExpressionFormatControlTarget())
						tab.addCellData(col + 2 - 1, row - 1, dp.getLog2TargetDivControlValue());
					row++;
				}
			}
			col++;
			if (ked.isTrueKeggExpressionFormatControlTarget()) {
				col++;
				col++;
			}
		}
		return tab;
	}
}