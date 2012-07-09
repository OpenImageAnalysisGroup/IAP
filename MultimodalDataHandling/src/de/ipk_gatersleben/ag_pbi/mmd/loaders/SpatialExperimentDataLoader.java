/*************************************************************************************
 * The MultimodalDataHandling Add-on is (c) 2008-2010 Plant Bioinformatics
 * Group,
 * IPK Gatersleben, http://bioinformatics.ipk-gatersleben.de
 * The source code for this project, which is developed by our group, is
 * available under the GPL license v2.0 available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html. By using this
 * Add-on and VANTED you need to accept the terms and conditions of this
 * license, the below stated disclaimer of warranties and the licenses of
 * the used libraries. For further details see license.txt in the root
 * folder of this project.
 ************************************************************************************/
package de.ipk_gatersleben.ag_pbi.mmd.loaders;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
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
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.SubstanceColumnInformation;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.TableData;
import de.ipk_gatersleben.ag_pbi.datahandling.TemplateLoader;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.NumericMeasurement3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Sample3D;

public class SpatialExperimentDataLoader extends TemplateLoader {
	
	private String experimentname;
	private String remark;
	private String coordinator;
	private Date importdate;
	private Date startdate;
	private boolean transposed = false;
	
	@Override
	public String[] getValidExtensions() {
		return new String[] { "xls", "xlsx" };
	}
	
	@Override
	public String toString() {
		return "Spatial Dataset";
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
			// transposed template
			transposed = tabledata.getCellData(10, 4, "empty").equals("V1ST");
			if (transposed)
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
		return exps;
	}
	
	private void readHeader(File file, TableData tabledata) {
		remark = (String) tabledata.getCellData(col("B"), 5, "");
		experimentname = (String) tabledata.getCellData(col("B"), 6, "");
		coordinator = (String) tabledata.getCellData(col("B"), 7, "");
		importdate = file == null ? new Date() : new Date(file.lastModified());
		String d = tabledata.getCellDataDate(col("B"), 4, "");
		startdate = d != null ? AttributeHelper.getDateFromString(d) : null;
	}
	
	@Override
	public boolean canProcess(File f) {
		return super.canProcess(f) && hasCorrectVersionCell(f);
	}
	
	private boolean hasCorrectVersionCell(File f) {
		try {
			Object magicfield = ExperimentDataFileReader.getExcelTableDataPeak(f, 5).getCellData(10, 4, "empty");
			return magicfield.equals("V1S") || magicfield.equals("V1ST");
		} catch (Exception e) {
			return false;
		}
	}
	
	private ExperimentInterface getExperimentDataFromFile(final File file, TableData tabledata, BackgroundTaskStatusProviderSupportingExternalCall status) {
		ExperimentInterface experiment = null;
		HashMap<Integer, ConditionInterface> conditions = getConditions(file, tabledata);
		HashMap<Integer, SampleInterface> samples = getSamples(tabledata);
		Collection<SubstanceColumnInformation> substanceColumnInformation = tabledata.getSubstanceColumnInformation("I");
		int currentColumn = 0;
		int maxColumn = substanceColumnInformation.size();
		for (SubstanceColumnInformation sci : substanceColumnInformation) {
			String substance = tabledata.getUnicodeStringCellData(sci.getFirstColumn(), 20);
			if (substance == null)
				continue;
			if (status != null) {
				status.setCurrentStatusText2("Process Substance " + substance + "...");
				status.setCurrentStatusValueFine(100d * currentColumn / maxColumn);
				if (status.wantsToStop()) {
					status.setCurrentStatusText1("Aborted...");
					return null;
				}
			}
			String columnList = sci.getColumnList();
			String unit = tabledata.getUnicodeStringCellData(sci.getFirstColumn(), 22);
			for (int columnMeasurement : sci.getColumns()) {
				if ((unit == null && tabledata.getUnicodeStringCellData(columnMeasurement, 22) != null) ||
						(unit != null && tabledata.getUnicodeStringCellData(columnMeasurement, 22) == null) ||
						(unit != null && tabledata.getUnicodeStringCellData(columnMeasurement, 22) != null &&
						!unit.equals(tabledata.getUnicodeStringCellData(columnMeasurement, 22)))) {
					// convert columnList (columns as integers) to columns
					// (columns in Excel notation)
					String columns = "";
					for (String column : columnList.split(","))
						if (columns.length() > 0)
							columns = columns + ", " + (transposed ? Integer.parseInt(column) + 19 : TableData.getExcelColumnName(Integer.parseInt(column)));
						else
							columns = columns + (transposed ? Integer.parseInt(column) + 19 : TableData.getExcelColumnName(Integer.parseInt(column)));
					
					if (transposed)
						MainFrame.showMessageDialog("<html>File " + file.toString() + " was ignored.<br>" +
								"Measurement unit for substance \"" + substance + "\" listed in " + (transposed ? "rows" : "columns") + " " +
								columns + " is not identical!", "Error");
					return null;
				}
				// measurement tool
				String measurementtool = "";
				if (tabledata.getUnicodeStringCellData(columnMeasurement, 21) != null)
					measurementtool = tabledata.getUnicodeStringCellData(columnMeasurement, 21);
				ExperimentInterface e = processSubstance(file, tabledata, conditions, samples,
						measurementtool, columnMeasurement, columnList);
				if (experiment == null)
					experiment = e;
				else
					experiment.addAll(e);
			}
			currentColumn++;
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
	
	private ExperimentInterface processSubstance(final File file, TableData tabledata,
			HashMap<Integer, ConditionInterface> conditions,
			HashMap<Integer, SampleInterface> samples,
			String measurementtool,
			int columnMeasurement, String columnList) {
		ExperimentInterface experiment = new Experiment();
		
		SubstanceInterface substance = Experiment.getTypeManager().getNewSubstance();
		substance.setName(tabledata.getUnicodeStringCellData(columnMeasurement, 20));
		substance.setRowId("column " + columnList);
		boolean validMeasurement;
		
		for (int row = 23; row <= tabledata.getMaximumRow(); row++) {
			SubstanceInterface substancecopy = substance.clone();
			int entityID;
			try {
				entityID = ((Double) tabledata.getCellData(col("A"), row, null)).intValue();
			} catch (Exception e) {
				if (transposed)
					ErrorMsg.addErrorMessage("Column " + TableData.getExcelColumnName(row - 19) + ", Row 20: no entity ID or entity ID not numerical!");
				else
					ErrorMsg.addErrorMessage("Row " + row + ", Column A: no entity ID or entity ID not numerical!");
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
			sample.setMeasurementtool(measurementtool);
			// check for valid measurement value at position (columnMeasurement, row)
			validMeasurement = false;
			Object o = tabledata.getCellData(columnMeasurement, row, null);
			Double od = new Double(0);
			if (o != null) {
				if (o instanceof Double) {
					od = (Double) o;
					validMeasurement = true;
				} else
					if (o instanceof String) {
						String os = StringManipulationTools.stringReplace((String) o, ",", ".");
						try {
							od = Double.parseDouble(os);
							validMeasurement = true;
						} catch (Exception e) {
							if (!(os.isEmpty() || os.equalsIgnoreCase("-") || os.equalsIgnoreCase("n/a") || os.equalsIgnoreCase("na")))
								if (transposed)
									ErrorMsg.addErrorMessage("Non-Numeric value \"" + os + "\" in column " + TableData.getExcelColumnName(row - 19) + ", row "
											+ (columnMeasurement + 19) + "!");
								else
									ErrorMsg.addErrorMessage("Non-Numeric value \"" + os + "\" in column " + TableData.getExcelColumnName(columnMeasurement) + ", row "
											+ row + "!");
						}
					} else
						if (transposed)
							ErrorMsg.addErrorMessage("Non-Numeric value \"" + o + "\" in column " + TableData.getExcelColumnName(row - 19) + ", row "
									+ (columnMeasurement + 19) + "!");
						else
							ErrorMsg.addErrorMessage("Non-Numeric value \"" + o + "\" in column " + TableData.getExcelColumnName(columnMeasurement) + ", row "
									+ row + "!");
			}
			// add only a valid measurement value
			if (validMeasurement)
				sample.add(getMeasurementData(tabledata, sample, columnMeasurement, columnList, row, od));
			condition.add(sample);
			substancecopy.add(condition);
			Substance.addAndMerge(experiment, substancecopy, false);
		}
		return experiment;
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
		// variety
		if (tabledata.getUnicodeStringCellData(columnSeries, 13) != null)
			series.setVariety(tabledata.getUnicodeStringCellData(columnSeries, 13));
		else
			series.setVariety("");
		// genotype
		if (tabledata.getUnicodeStringCellData(columnSeries, 14) != null)
			series.setGenotype(tabledata.getUnicodeStringCellData(columnSeries, 14));
		else {
			ErrorMsg.addErrorMessage("Genotype not defined in column " + TableData.getExcelColumnName(columnSeries));
			series.setGenotype("NOT DEFINED (INPUT ERROR)");
		}
		// growth conditions
		if (tabledata.getUnicodeStringCellData(columnSeries, 15) != null)
			series.setGrowthconditions(tabledata.getUnicodeStringCellData(columnSeries, 15));
		else
			series.setGrowthconditions("");
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
			SampleInterface sample, int columnMeasurement, String columnList,
			int row, Double od) {
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
		measurementdata.setValue(od.doubleValue());
		// measurement unit
		if (tabledata.getUnicodeStringCellData(columnMeasurement, 22) != null)
			measurementdata.setAttribute(new Attribute("unit", tabledata.getUnicodeStringCellData(columnMeasurement, 22)));
		else {
			// convert columnList (columns as integers) to columns (columns in
			// Excel notation)
			String columns = "";
			for (String column : columnList.split(","))
				if (columns.length() > 0)
					columns = columns + ", " + (transposed ? Integer.parseInt(column) + 19 : TableData.getExcelColumnName(Integer.parseInt(column)));
				else
					columns = columns + (transposed ? Integer.parseInt(column) + 19 : TableData.getExcelColumnName(Integer.parseInt(column)));
			ErrorMsg.addErrorMessage("No measurement unit given for substance listed in " + (transposed ? "row" : "column") + "(s) "
					+ columns + "!");
			measurementdata.setAttribute(new Attribute("unit", "n/a"));
		}
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
