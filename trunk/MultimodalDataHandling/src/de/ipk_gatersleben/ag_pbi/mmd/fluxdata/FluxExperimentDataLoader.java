package de.ipk_gatersleben.ag_pbi.mmd.fluxdata;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.AttributeHelper;
import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.ErrorMsg;
import org.StringManipulationTools;
import org.graffiti.editor.MainFrame;
import org.jdom.Attribute;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Experiment;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Sample;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Substance;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.ExperimentDataFileReader;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.ExperimentDataPresenter;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.TableData;
import de.ipk_gatersleben.ag_pbi.datahandling.TemplateLoader;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.NumericMeasurement3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Sample3D;

public class FluxExperimentDataLoader extends TemplateLoader {
	private String experimentname;
	private String remark;
	private String coordinator;
	private Date importdate;
	private Date startdate;
	
	private HashMap<String, Double> substanceWeight = new HashMap<String, Double>();
	
	// private int missedweights = 0;
	
	@Override
	public String[] getValidExtensions() {
		return new String[] { "xls" };
	}
	
	@Override
	public String toString() {
		return "Flux Dataset";
	}
	
	@Override
	public List<ExperimentInterface> process(final List<File> files, final ExperimentDataPresenter receiver,
			BackgroundTaskStatusProviderSupportingExternalCall status) {
		return getExperimentDataFromFileList(files, status);
	}
	
	private List<ExperimentInterface> getExperimentDataFromFileList(List<File> files, BackgroundTaskStatusProviderSupportingExternalCall status) {
		if (status != null)
			status.setCurrentStatusValue(-1);
		
		List<ExperimentInterface> exps = new ArrayList<ExperimentInterface>();
		for (File f : files) {
			if (status != null) {
				status.setCurrentStatusText1("Process File " + f.getName());
				if (status.wantsToStop()) {
					status.setCurrentStatusText1("Aborted...");
					break;
				}
			}
			TableData tabledata = ExperimentDataFileReader.getExcelTableData(f);
			readHeader(f, tabledata);
			
			// transpose it, because then it is easier to read
			tabledata = new TableData(tabledata, true, 19);
			
			ExperimentInterface experimentdata = getExperimentDataFromFile(f, tabledata, status);
			if (experimentdata != null)
				exps.add(experimentdata);
		}
		if (status != null) {
			status.setCurrentStatusValue(100);
			status.setCurrentStatusText1("Finished");
			status.setCurrentStatusText2("");
		}
		// MainFrame.showMessage(missedweights+" substance weights could not be Files", MessageType.ERROR);
		return exps;
	}
	
	private void readHeader(File file, TableData tabledata) {
		remark = (String) tabledata.getCellData(col("B"), 5, "");
		experimentname = (String) tabledata.getCellData(col("B"), 6, "");
		coordinator = (String) tabledata.getCellData(col("B"), 7, "");
		importdate = file == null ? new Date() : new Date(file.lastModified());
		String d = tabledata.getCellDataDate(col("B"), 4, "");
		startdate = d != null ? AttributeHelper.getDateFromString(d) : null;
		substanceWeight = getSubstanceWeights(tabledata);
		// missedweights = 0;
	}
	
	@Override
	public boolean canProcess(File f) {
		return super.canProcess(f) && hasCorrectVersionCell(f);
	}
	
	private boolean hasCorrectVersionCell(File f) {
		try {
			TableData td = ExperimentDataFileReader.getExcelTableDataPeak(f, 5);
			return td.getCellData(4, 2, "empty").equals("V1.0F");
		} catch (Exception e) {
			return false;
		}
	}
	
	private HashMap<String, Double> getSubstanceWeights(TableData tabledata) {
		HashMap<String, Double> substanceWights = new HashMap<String, Double>();
		
		for (int col = col("F"); tabledata.getCellData(col, 4, null) != null; col++) {
			Object substance = tabledata.getCellData(col, 4, null);
			Object weight = tabledata.getCellData(col, 5, null);
			
			if (weight != null && substance != null) {
				if (substance instanceof String && ((String) substance).length() > 0 && weight instanceof Double) {
					if (substanceWights.containsKey(substance))
						ErrorMsg.addErrorMessage("Substance \"" + substance + "\" occured more than once in the weight list. Ignoring last occurence...");
					else
						substanceWights.put((String) substance, (Double) weight);
				} else {
					if (!(weight instanceof Double))
						ErrorMsg.addErrorMessage("Weight in cell " + TableData.getExcelColumnName(col) + ":5 is not a number! Ignoring...");
					if (!(substance instanceof String && ((String) substance).length() > 0))
						ErrorMsg.addErrorMessage("Substance in cell " + TableData.getExcelColumnName(col) + ":4 is empty! Ignoring...");
				}
			}
		}
		return substanceWights;
	}
	
	private ExperimentInterface getExperimentDataFromFile(final File file, TableData tabledata, BackgroundTaskStatusProviderSupportingExternalCall status) {
		
		ExperimentInterface experiment = null;
		HashMap<Integer, ConditionInterface> conditions = getConditions(file, tabledata);
		HashMap<Integer, SampleInterface> samples = getSamples(tabledata);
		
		if (conditions == null || samples == null)
			return null;
		// ArrayList<SubstanceColumnInformation> substanceColumnInformation = (ArrayList<SubstanceColumnInformation>) tabledata.getSubstanceColumnInformation("F",
		// true);
		
		// int currentColumn = 0;
		// int maxColumn = substanceColumnInformation.size();
		// for (int idx = 0; idx < substanceColumnInformation.size(); idx++) {
		// SubstanceColumnInformation sci = substanceColumnInformation.get(idx);
		for (int col = 6; col < tabledata.getMaximumCol(); col++) {
			String substance = tabledata.getUnicodeStringCellData(col, 20);
			if (substance == null)
				continue;
			
			// ArrayList<Double> qualityvalues = null;
			// if (substanceColumnInformation.size() > idx + 1) {
			// String nextSubstance = tabledata.getUnicodeStringCellData(substanceColumnInformation.get(idx + 1).getFirstColumn(), 20);
			// if (nextSubstance.equals("quality")) {
			// qualityvalues = new ArrayList<Double>();
			// for (int row = 23; row <= tabledata.getMaximumRow(); row++)
			// qualityvalues.add(getMeasurementFromTable(sci.getFirstColumn() + 1, row, tabledata));
			// }
			// }
			if (status != null) {
				status.setCurrentStatusText2("Processing Reaction " + substance + "...");
				status.setCurrentStatusValueFine(100d * col / tabledata.getMaximumCol());
				if (status.wantsToStop()) {
					status.setCurrentStatusText1("Aborted...");
					return null;
				}
			}
			
			// measurement tool
			String reactionname = "<empty>";
			if (tabledata.getUnicodeStringCellData(col, 21) != null)
				reactionname = tabledata.getUnicodeStringCellData(col, 21);
			else
				ErrorMsg.addErrorMessage("No reactionname specified! Using &#060;empty&#062; instead!");
			ExperimentInterface e = processReaction(file, tabledata, conditions, samples,
					reactionname, col);
			
			if (e == null) {
				ErrorMsg.addErrorMessage("Errors occured when parsing reaction in column " + (col + 18) + "! Ignoring...");
				continue;
			}
			
			if (experiment == null)
				experiment = e;
			else
				experiment.addAll(e);
		}
		// add sample IDs
		long sampleID = 1;
		if (experiment == null)
			return null;
		
		for (SubstanceInterface e : experiment)
			for (ConditionInterface condition : e.getConditions(null))
				for (SampleInterface sample : condition) {
					sample.setSampleFineTimeOrRowId(sampleID);
					sampleID++;
				}
		return experiment;
	}
	
	private ExperimentInterface processReaction(final File file, TableData tabledata,
			HashMap<Integer, ConditionInterface> conditions,
			HashMap<Integer, SampleInterface> samples,
			String reactionname,
			int col) {
		ExperimentInterface experiment = new Experiment();
		
		FluxReaction reaction = new FluxReaction(tabledata.getUnicodeStringCellData(col, 20));
		if (!reaction.isCorrect())
			return null;
		
		int cnt = 0;
		for (FluxReactant fr : reaction.getAllReactants()) {
			
			SubstanceInterface substance = Experiment.getTypeManager().getNewSubstance();
			if (reaction.isLeftReactant(fr))
				substance.setName(fr.getName() + "^" + reactionname);
			else
				substance.setName(reactionname + "^" + fr.getName());
			
			substance.setInfo(reactionname);
			
			substance.setRowId("column " + col + "/" + (cnt++));
			
			for (int row = 23; row <= tabledata.getMaximumRow(); row++) {
				SubstanceInterface substancecopy = substance.clone();
				int entityID;
				try {
					Object cond = tabledata.getCellData(col("A"), row, null);
					if (cond.equals("quality"))
						continue;
					entityID = ((Double) cond).intValue();
				} catch (Exception e) {
					ErrorMsg.addErrorMessage("Column " + TableData.getExcelColumnName(row - 19) + ", Row 20: no entity ID or \"quality\" value specified!");
					continue;
				}
				ConditionInterface condition;
				if (conditions.get(entityID) == null) {
					MainFrame.showMessageDialog("<html>File " + file.toString() + " was ignored.<br>" +
							"No corresponding column found in row 11 for entity ID " + entityID + "!", "Error");
					return null;
				}
				condition = conditions.get(entityID).clone(substancecopy);
				SampleInterface sample = samples.get(row).clone(condition);
				sample.setMeasurementtool("");
				// check for valid measurement value at position (columnMeasurement,
				// row)
				Double od = getMeasurementFromTable(col, row, tabledata);
				
				// get the quality value, if exists
				Double quality = null;
				Object optQuality = tabledata.getCellData(col("A"), row + 1, null);
				if (optQuality != null && optQuality.equals("quality"))
					quality = getMeasurementFromTable(col, row + 1, tabledata);
				
				// add only a valid measurement value
				if (od != null)
					sample.add(getMeasurementData(tabledata, sample, col, row, od, quality, fr));
				condition.add(sample);
				substancecopy.add(condition);
				Substance.addAndMerge(experiment, substancecopy, false);
			}
		}
		return experiment;
	}
	
	public Double getMeasurementFromTable(int col, int row, TableData tabledata) {
		Object o = tabledata.getCellData(col, row, null);
		
		if (o == null)
			return null;
		
		if (o instanceof Double)
			return (Double) o;
		
		if (o instanceof String) {
			String os = StringManipulationTools.stringReplace((String) o, ",", ".");
			try {
				return Double.parseDouble(os);
			} catch (Exception e) {
				if (!(os.isEmpty() || os.equalsIgnoreCase("-") || os.equalsIgnoreCase("n/a") || os.equalsIgnoreCase("na")))
					ErrorMsg.addErrorMessage("Non-Numeric value \"" + os + "\" in column " + TableData.getExcelColumnName(row - 19) + ", row " + (col + 19) + "!");
			}
		} else
			ErrorMsg.addErrorMessage("Non-Numeric value \"" + o.toString() + "\" in column " + TableData.getExcelColumnName(row - 19) + ", row " + (col + 19)
					+ "!");
		return null;
	}
	
	private HashMap<Integer, ConditionInterface> getConditions(File file, TableData tabledata) {
		
		HashMap<Integer, ConditionInterface> conditions = new HashMap<Integer, ConditionInterface>();
		for (int column = col("B"); column <= tabledata.getMaximumCol(); column++)
			try {
				Object obj = tabledata.getCellData(column, 11, null);
				if (obj != null) {
					int id = ((Double) obj).intValue();
					conditions.put(new Integer(id), getConditionData(file, tabledata, column));
				}
			} catch (Exception e) {
				MainFrame.showMessageDialog("<html>File " + file.toString() + " was ignored.<br>" +
						"Row 11, Column " + TableData.getExcelColumnName(column) + ": entity ID not numerical!", "Error");
				return null;
			}
		return conditions;
	}
	
	private ConditionInterface getConditionData(File file, TableData tabledata, int columnSeries) {
		ConditionInterface series = Experiment.getTypeManager().getNewCondition(null);
		
		series.setExperimentName(experimentname);
		series.setExperimentCoordinator(coordinator);
		series.setExperimentImportdate(importdate);
		series.setExperimentRemark(remark);
		series.setExperimentStartDate(startdate);
		series.setExperimentType("");
		
		// id
		series.setRowId(((Double) tabledata.getCellData(columnSeries, 11, null)).intValue());
		// species
		if (tabledata.getUnicodeStringCellData(columnSeries, 12) != null)
			series.setSpecies(tabledata.getUnicodeStringCellData(columnSeries, 12));
		else {
			ErrorMsg.addErrorMessage("Species not defined in column " + TableData.getExcelColumnName(columnSeries));
			series.setSpecies("NOT DEFINED (INPUT ERROR)");
		}
		// // variety
		// if (tabledata.getUnicodeStringCellData(columnSeries, 13) != null)
		// series.setVariety(tabledata.getUnicodeStringCellData(columnSeries, 13));
		// else
		// series.setVariety("");
		// genotype
		if (tabledata.getUnicodeStringCellData(columnSeries, 14) != null)
			series.setGenotype(tabledata.getUnicodeStringCellData(columnSeries, 14));
		else {
			ErrorMsg.addErrorMessage("Genotype not defined in column " + TableData.getExcelColumnName(columnSeries));
			series.setGenotype("NOT DEFINED (INPUT ERROR)");
		}
		// // growth conditions
		// if (tabledata.getUnicodeStringCellData(columnSeries, 15) != null)
		// series.setGrowthconditions(tabledata.getUnicodeStringCellData(columnSeries, 15));
		// else
		// series.setGrowthconditions("");
		// treatment
		if (tabledata.getUnicodeStringCellData(columnSeries, 16) != null)
			series.setTreatment(tabledata.getUnicodeStringCellData(columnSeries, 16));
		else
			series.setTreatment("");
		return series;
		
	}
	
	private HashMap<Integer, SampleInterface> getSamples(TableData tabledata) {
		
		HashMap<Integer, SampleInterface> samples = new HashMap<Integer, SampleInterface>();
		for (int row = 23; row <= tabledata.getMaximumRow(); row++) {
			samples.put(new Integer(row), getSampleData(tabledata, row));
		}
		return samples;
		
	}
	
	private SampleInterface getSampleData(TableData tabledata, int row) {
		SampleInterface sample = Experiment.getTypeManager().getNewSample(null);
		// time
		Object timeObj = tabledata.getCellData(col("C"), row, null);
		if (timeObj != null && timeObj instanceof Double)
			if (((Double) timeObj).toString().endsWith(".0"))
				sample.setAttribute(new Attribute("time", ((Double) timeObj).toString().replace(".0", "")));
			else
				sample.setAttribute(new Attribute("time", ((Double) timeObj).toString()));
		else
			sample.setAttribute(new Attribute("time", "-1"));
		// time unit
		if (tabledata.getUnicodeStringCellData(col("D"), row) != null)
			sample.setTimeUnit(tabledata.getUnicodeStringCellData(col("D"), row));
		else
			sample.setTimeUnit(Sample.UNSPECIFIED_TIME_STRING);
		// component
		if (tabledata.getUnicodeStringCellData(col("E"), row) != null)
			((Sample3D) sample).setComponent(tabledata.getUnicodeStringCellData(col("E"), row));
		else
			((Sample3D) sample).setComponent("");
		return sample;
	}
	
	private NumericMeasurementInterface getMeasurementData(TableData tabledata,
			SampleInterface sample, int col,
			int row, Double od, Double quality, FluxReactant fr) {
		NumericMeasurementInterface measurementdata = Experiment.getTypeManager().getNewMeasurement(sample);
		// replicate ID
		Object replicateIDObj = tabledata.getCellData(col("B"), row, null);
		if (replicateIDObj != null && replicateIDObj instanceof Double)
			measurementdata.setReplicateID(((Double) replicateIDObj).intValue());
		else
			measurementdata.setReplicateID(-1);
		// position
		Object posObj = tabledata.getCellData(col("F"), row, null);
		if (posObj != null && posObj instanceof Double)
			((NumericMeasurement3D) measurementdata).setPosition((Double) posObj);
		// else //leave empty
		// ((NumericMeasurement3D)measurementdata).setPosition(-1);
		// position unit
		if (tabledata.getUnicodeStringCellData(col("G"), row) != null)
			((NumericMeasurement3D) measurementdata).setPositionUnit(tabledata.getUnicodeStringCellData(col("G"), row));
		// else //leave empty
		// ((NumericMeasurement3D)measurementdata).setPositionUnit("-1");
		// measurement value
		
		double valueFromTable = od.doubleValue();
		// for weighting the substances via the global weight list (eg. c-fluxes...)
		Double weight = substanceWeight.get(fr.getName());
		if (weight != null)
			valueFromTable *= weight;
		// else
		// missedweights ++;
		// for weighting according to the stocheometric coefficient
		valueFromTable *= fr.getCoeff();
		
		measurementdata.setValue(valueFromTable);
		
		if (quality != null)
			measurementdata.setQualityAnnotation(quality.toString());
		
		// measurement unit
		if (tabledata.getUnicodeStringCellData(col, 22) != null)
			measurementdata.setAttribute(new Attribute("unit", tabledata.getUnicodeStringCellData(col, 22)));
		else
			measurementdata.setAttribute(new Attribute("unit", "n/a"));
		
		return measurementdata;
		
	}
	
	private int col(String col) {
		if (col.length() == 1)
			return col.charAt(0) - 64;
		else
			ErrorMsg.addErrorMessage("Internal Error: Invalid column specification!");
		return -1;
		
	}
	
}