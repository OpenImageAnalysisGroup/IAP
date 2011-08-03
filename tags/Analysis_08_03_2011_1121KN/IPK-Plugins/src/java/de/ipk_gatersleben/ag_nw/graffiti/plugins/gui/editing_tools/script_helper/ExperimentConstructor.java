/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 26.02.2006 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.ErrorMsg;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.DBEinputFileReader;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.ExperimentDataFileReader;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.TableData;

public class ExperimentConstructor {
	
	public static ExperimentInterface processData(
						String measuredSubstance, String measurementUnit,
						ArrayList<MemSample> memSamples,
						ArrayList<MemPlant> memPlants,
						String experimentStart,
						String experimentName,
						String coordinator,
						String optRemark,
						String optSequence) {
		
		ExperimentInterface doc = getDoc(measuredSubstance, measurementUnit, memSamples,
							memPlants,
							experimentStart,
							experimentName,
							coordinator,
							optRemark,
							optSequence);
		return doc;
	}
	
	@SuppressWarnings("deprecation")
	private static ExperimentInterface getDoc(
						String measuredSubstance,
						String measurementUnit,
						ArrayList<MemSample> memSamples,
						ArrayList<MemPlant> memPlants,
						String experimentStart,
						String experimentName,
						String coordinator,
						String optRemark,
						String optSequence) {
		ExperimentDataFileReader edr = new DBEinputFileReader();
		TableData xls = new TableData();
		addCellDataDirect(xls, 10, 4, "V1.2");
		
		Date startDate = new Date();
		try {
			startDate = new Date(experimentStart);
		} catch (Exception e) {
			// ErrorMsg.addErrorMessage(e);
		}
		
		// experiment header
		addCellDataDirect(xls, col("B"), 4, startDate); // start of experiment
		addCellDataDirect(xls, col("B"), 5, optRemark != null ? optRemark : ""); // remark
		addCellDataDirect(xls, col("B"), 6, experimentName); // experiment name (targetExp. should be a String in this context
		addCellDataDirect(xls, col("B"), 7, coordinator); // coordinator
		addCellDataDirect(xls, col("B"), 8, optSequence != null ? optSequence : ""); // sequence name
		
		// plant/genotypes
		int addCol = 0;
		for (MemPlant mp : memPlants) {
			addCellDataDirect(xls, col("B") + addCol, 11, addCol + 1d); // plant ID 1
			addCellDataDirect(xls, col("B") + addCol, 12, mp.getSpecies()); // species
			addCellDataDirect(xls, col("B") + addCol, 13, mp.getVariety()); // variety
			addCellDataDirect(xls, col("B") + addCol, 14, mp.getGenotype()); // genotype
			addCellDataDirect(xls, col("B") + addCol, 15, mp.getTreatment()); // treatment
			addCol++;
		}
		
		int col = 0;
		addCellDataDirect(xls, col("F") + col, 20, measuredSubstance); // Substance
		addCellDataDirect(xls, col("F") + col, 22, measurementUnit); // Unit
		processMeasValues(xls, 0, memSamples);
		
		// if (true)
		// xls.showDataDialog();
		return edr.getXMLDataFromExcelTable(null, xls, null);
	}
	
	private static void processMeasValues(TableData xls, int col, List<MemSample> samples) {
		int row = max(xls.getMaximumRow(col("A") - 1), xls.getMaximumRow(col - 1)) + 2;
		if (row < 23)
			row = 23;
		for (MemSample ms : samples) {
			addCellDataDirect(xls, col("A"), row, ms.getPlantID()); // plant ID
			addCellDataDirect(xls, col("B"), row, ms.getReplicateID()); // replicate #
			addCellDataDirect(xls, col("C"), row, ms.getTime()); // time
			addCellDataDirect(xls, col("D"), row, ms.getTimeUnit()); // time unit
			addCellDataDirect(xls, col("F") + col, row, ms.getValue()); // value
			row++;
		}
	}
	
	private static void addCellDataDirect(TableData xls, int col, int row, Object o) {
		xls.addCellData(col - 1, row - 1, o);
	}
	
	private static int max(int a, int b) {
		return a > b ? a : b;
	}
	
	private static int col(String col) {
		if (col.length() == 1) {
			char c1 = col.charAt(0);
			return c1 - 64;
		} else {
			ErrorMsg.addErrorMessage("Invalid column specification / Internal Error!");
			return -1;
		}
	}
	
}
