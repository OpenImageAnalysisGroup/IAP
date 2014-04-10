/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 06.04.2005 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import org.AttributeHelper;
import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.ErrorMsg;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.kegg.CompoundEntry;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.kegg.CompoundService;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Experiment;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleAverageInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;

public class DBEinputFileReader extends ExperimentDataFileReader {
	
	int dataCount = 0;
	private String experimentname;
	private String remark;
	private String coordinator;
	private Date importdate;
	private Date startdate;
	
	public @Override
	Experiment getXMLDataFromExcelTable(File excelFile, TableData myData,
			BackgroundTaskStatusProviderSupportingExternalCall statusProvider) {
		
		if (myData.isDBEtransposedInputForm()) {
			status1 = "Process Transposed Input File...";
			// myData.showDataDialog();
			myData = new TableData(myData, true, 19);
			// myData.showDataDialog();
		} else
			status1 = "Process Input File...";
		
		Experiment e = new Experiment();
		
		status2 = "Add Experiment-Header";
		if (statusProvider != null)
			statusProvider.setCurrentStatusText1(status1);
		if (statusProvider != null)
			statusProvider.setCurrentStatusText2(status2);
		getExperimentHeaderElement(excelFile, myData);
		status1 = "Process File-Content...";
		status2 = "";
		if (statusProvider != null)
			statusProvider.setCurrentStatusText1(status1);
		if (statusProvider != null)
			statusProvider.setCurrentStatusText2(status2);
		getExperimentMeasurementsElement(e, myData, statusProvider);
		
		// for all conditions set experimentstuff
		
		progressDouble = new Double(100d);
		return e;
	}
	
	/*
	 * <experimentdata> <cachetime>Tue Nov 02 11:15:48 CET 2004</cachetime>
	 * <experiment experimentid="246">
	 * <experimentname>AAT-Berlin-Proben</experimentname> <remark>null</remark>
	 * <coordinator>Hardy Rolletschek</coordinator>
	 * <excelfileid>284</excelfileid> <importusername>hardy</importusername>
	 * <importdate>Wed Dec 01 15:57:00 CET 2004</importdate> <startdate>Wed Oct
	 * 20 00:00:00 CEST 2004</startdate> <measurements>221</measurements>
	 * <imagefiles>0</imagefiles> <sizekb>0</sizekb> </experiment>
	 * <measurements> <substance id="3" name="Sucrose" formula="C12H22O11"
	 * substancegroup="sugar"> <line id="526" name="Vicia narbonensis"
	 * genotype="wild type" treatment="null"> <sample id="1285" time="-1"
	 * unit="-1"> <average unit="&#00181;mol/g" replicates="7"
	 * min="108.816751731602" max="125.640731640212"
	 * stddev="6.513326955309744">118.87748255289114</average> <data
	 * unit="&#00181;mol/g">108.816751731602</data> <data
	 * unit="&#00181;mol/g">125.640731640212</data> <data
	 * unit="&#00181;mol/g">125.598092379386</data> ...
	 */
	
	private void getExperimentMeasurementsElement(Experiment e,
			TableData myData,
			BackgroundTaskStatusProviderSupportingExternalCall statusProvider) {
		if (checkStopp())
			return;
		
		int skipCount = 0;
		long sampleIDcount = 0;
		status1 = "Init compound database";
		status2 = "";
		if (statusProvider != null)
			statusProvider.setCurrentStatusText1(status1);
		if (statusProvider != null)
			statusProvider.setCurrentStatusText2(status2);
		CompoundEntry ce = CompoundService.getInformation("test");
		if (ce != null)
			status1 = "Processing data:";
		else
			status1 = "Processing data:";
		status2 = "";
		if (statusProvider != null)
			statusProvider.setCurrentStatusText1(status1);
		if (statusProvider != null)
			statusProvider.setCurrentStatusText2(status2);
		int progressSubstance = 0;
		int substCount = myData.getSubstanceColumnInformation("F").size();
		for (SubstanceColumnInformation sci : myData
				.getSubstanceColumnInformation("F")) {
			if (checkStopp())
				return;
			
			if (myData.getUnicodeStringCellData(sci.getFirstColumn(), 20) == null) {
				skipCount++;
				continue;
			}
			sampleIDcount = processSubstanceEntries(myData, e, sampleIDcount,
					sci);
			progressSubstance = progressSubstance + 1;
			double pv = ((double) progressSubstance / (double) substCount) * 100d;
			progressDouble = pv;
			status2 = dataCount + " values, " + sampleIDcount + " samples, "
					+ substCount + " substances";
			if (statusProvider != null)
				statusProvider.setCurrentStatusText2(status2);
			if (statusProvider != null)
				statusProvider
						.setCurrentStatusValueFine(progressDouble / 2 + 50d);
		}
		// setze datacount zu condition (wieviele measurements gemessen)
		// da.setText(new Integer(dataCount).toString());
		progressDouble = 100d;
		if (statusProvider != null)
			statusProvider.setCurrentStatusValueFine(progressDouble);
		if (statusProvider != null)
			statusProvider.setCurrentStatusText1("Dataset created");
		if (statusProvider != null)
			statusProvider
					.setCurrentStatusText2("Open Dataset-Tab (please wait)");
		
	}
	
	private long processSubstanceEntries(TableData myData, Experiment e,
			long sampleIDcount, SubstanceColumnInformation sci) {
		CompoundEntry ce;
		
		SubstanceInterface s = Experiment.getTypeManager().getNewSubstance();
		s.setRowId("column " + sci.getColumnList());
		// ********** ADD SUBSTANCE ATTRIBUTES **************
		// substance name
		String substName = myData.getUnicodeStringCellData(
				sci.getFirstColumn(), 20);
		s.setName(substName);
		// add formula information, if possible
		ce = CompoundService.getInformation(substName);
		if (ce != null) {
			s.setFormula(ce.getFormula());
		}
		int maxCol = myData.getMaximumCol();
		// ************** ADD LINE ELEMENTS **************
		for (int colPlant = col("B"); colPlant <= maxCol; colPlant++) {
			String plantOrLine = myData.getUnicodeStringCellData(colPlant, 12);
			if (plantOrLine == null)
				continue;
			status1 = "Processing data:";
			status2 = "" + dataCount + " values and " + sampleIDcount
					+ " samples";
			Object plantIDval = myData.getCellData(colPlant, 11, null);
			sampleIDcount = processLineEntries(myData, sampleIDcount, sci, s,
					colPlant, plantIDval, plantOrLine);
		}
		
		e.add(s);
		
		return sampleIDcount;
	}
	
	private long processLineEntries(TableData myData, long sampleIDcount,
			SubstanceColumnInformation sci, SubstanceInterface s, int colPlant,
			Object plantIDval, String plantOrLine) {
		int lineID = (plantIDval instanceof Integer) ? (Integer) plantIDval
				: ((Double) plantIDval).intValue();
		
		ConditionInterface c = Experiment.getTypeManager().getNewCondition(s);
		s.add(c);
		
		// ********** ADD LINE ATTRIBUTES ***********
		c.setRowId(lineID);
		c.setSpecies(plantOrLine);
		String genoType = myData.getUnicodeStringCellData(colPlant, 14);
		if (genoType == null) {
			ErrorMsg.addErrorMessage("Genotype not defined in column "
					+ colPlant);
			genoType = "NOT DEFINED (INPUT ERROR)";
		}
		c.setGenotype(genoType);
		if (myData.getUnicodeStringCellData(colPlant, 13) != null)
			c.setVariety(myData.getUnicodeStringCellData(colPlant, 13));
		else
			c.setVariety("");
		if (myData.getUnicodeStringCellData(colPlant, 15) != null)
			c.setGrowthconditions(myData.getUnicodeStringCellData(colPlant, 15));
		else
			c.setGrowthconditions("");
		if (myData.getUnicodeStringCellData(colPlant, 16) != null)
			c.setTreatment(myData.getUnicodeStringCellData(colPlant, 16));
		else
			c.setTreatment("");
		for (SampleEntry sample : myData.getSamples(sci, lineID)) {
			sampleIDcount = processSampleEntries(myData, sampleIDcount, sci, c,
					sample);
		}
		
		c.setExperimentName(experimentname);
		c.setExperimentCoordinator(coordinator);
		c.setExperimentImportdate(importdate);
		c.setExperimentRemark(remark);
		c.setExperimentStartDate(startdate);
		c.setExperimentType("");
		
		return sampleIDcount;
	}
	
	private long processSampleEntries(TableData myData, long sampleIDcount,
			SubstanceColumnInformation sci, ConditionInterface c,
			SampleEntry sample) {
		// ADD SAMPLE ENTRY
		SampleInterface s = Experiment.getTypeManager().getNewSample(c);
		c.add(s);
		
		// ADD SAMPLE ATTRIBUTES: ID, TIME, UNIT
		s.setSampleFineTimeOrRowId(++sampleIDcount);
		s.setTime(sample.getTime());
		if (sample.getOptFineTime() != null)
			s.setSampleFineTimeOrRowId(sample.getOptFineTime());
		
		// new Integer(processTimeData(new Double(sample.getTime()).toString())));
		s.setTimeUnit(sample.getTimeUnit());
		// ADD DATA ATTRIBUTE: MEASUREMENT TOOL
		String mesTool = myData.getUnicodeStringCellData(sci.getFirstColumn(),
				21);
		s.setMeasurementtool(mesTool == null ? "" : mesTool);
		ArrayList<ReplicateDouble> measurements = sample.getMeasurementValues();
		// ADD AVERAGE ENTRY
		
		SampleAverageInterface sa = Experiment.getTypeManager().getNewSampleAverage(s);
		s.setSampleAverage(sa);
		
		// ADD AVERAGE ATTRIBUTES: UNIT, REPLICATES, MIN, MAX, STDDEV
		sa.setUnit(sample.getMeasurementUnit());
		sa.setReplicateId(new Integer(measurements.size()));
		sa.setMin(new Double(sample.getMinimum()));
		sa.setMax(new Double(sample.getMaximum()));
		sa.setStddev(new Double(sample.getStddev()));
		sa.setValue(new Double(sample.getAverage()));
		for (ReplicateDouble itMes : measurements) {
			Double value = itMes.doubleValue();
			// ADD DATA ENTRY
			NumericMeasurementInterface m = Experiment.getTypeManager()
					.getNewMeasurement(s);
			s.add(m);
			// ADD DATA ATTRIBUTE: UNIT
			m.setUnit(checkNullError(
					myData.getUnicodeStringCellData(sci.getFirstColumn(), 22),
					"No measurement unit given for substance listed in column(s) "
							+ sci.getColumnList() + "!", "n/a"));
			m.setReplicateID(new Integer(itMes.getReplicateNumber()));
			if (itMes.getOptionalQualityAnnotation() != null)
				m.setQualityAnnotation(itMes.getOptionalQualityAnnotation());
			m.setValue(value);
			dataCount++;
		}
		status2 = dataCount + " measurement values processed";
		return sampleIDcount;
	}
	
	private String checkNullError(String value, String errorMessageIfNull,
			String replaceWithForNull) {
		if (value == null) {
			ErrorMsg.addErrorMessage(errorMessageIfNull);
			return replaceWithForNull;
		}
		return value;
	}
	
	// private String processTimeData(String timeVal) {
	// if (timeVal != null) {
	// if (timeVal.endsWith(".0"))
	// timeVal = StringManipulationTools.stringReplace(timeVal, ".0",
	// "");
	// }
	// return timeVal;
	// }
	
	private void getExperimentHeaderElement(File excelFile, TableData myData) {
		experimentname = (String) myData.getCellData(col("B"), 6, "");
		remark = (String) myData.getCellData(col("B"), 5, "");
		coordinator = (String) myData.getCellData(col("B"), 7, "");
		importdate = excelFile == null ? new Date() : new Date(
				excelFile.lastModified());
		startdate = AttributeHelper.getDateFromString(myData.getCellDataDate(
				col("B"), 4, ""));
		dataCount = 0;
	}
	
	private int col(String col) {
		if (col.length() == 1) {
			char c1 = col.charAt(0);
			return c1 - 64;
		} else {
			ErrorMsg.addErrorMessage("Invalid column specification / Internal Error!");
			return -1;
		}
	}
	
	@Override
	public String getCurrentStatusMessage3() {
		return null;
	}
	
	@Override
	public boolean wantsToStop() {
		return false;
	}
}
