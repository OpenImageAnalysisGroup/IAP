/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 17.11.2004 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.swing.SwingUtilities;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.ErrorMsg;
import org.graffiti.editor.MainFrame;
import org.jdom.Attribute;
import org.jdom.Element;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Experiment;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeader;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleAverage;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;

/**
 * @author Christian Klukas
 *         (c) 2004 IPK-Gatersleben
 */
public class GeneExpressionFileReader extends ExperimentDataFileReader {
	Element measurementCountElement;
	
	@Override
	public ExperimentInterface getXMLDataFromExcelTable(File excelFile, TableData myData,
						BackgroundTaskStatusProviderSupportingExternalCall statusProvider) {
		System.out.println("Read Template 2...");
		status1 = "Process file content...";
		status2 = "";
		int ec_number = getHeaderColumn(myData, "EC*");
		if (ec_number < 0)
			ec_number = getHeaderColumn(myData, "*clust-ID");
		ArrayList<Annotation> anno = new ArrayList<Annotation>();
		checkHeaderInfo(anno, myData, "spot", "spot");
		checkHeaderInfo(anno, myData, "info", "info");
		checkHeaderInfo(anno, myData, "cluster_id", "*clust-ID");
		checkHeaderInfo(anno, myData, "new_blast", "New blast result*");
		checkHeaderInfo(anno, myData, "new_blast_e_val", "*E-value*");
		checkHeaderInfo(anno, myData, "new_blast_score", "New Blast score");
		checkHeaderInfo(anno, myData, "affy_hit", "*affy*");
		checkHeaderInfo(anno, myData, "score", "score");
		checkHeaderInfo(anno, myData, "funcat", "*funcat*");
		checkHeaderInfo(anno, myData, "secure", "*secure*");
		
		int data_start_col = getFirstDataColumn(myData);
		
		status1 = "Process File-Content...";
		status2 = "Add Experiment-Header";
		ExperimentHeader header = getExperimentHeaderElement(excelFile, myData, /* experimentData */null);
		status1 = "Process File-Content...";
		status2 = "";
		
		ExperimentInterface e = getExperimentMeasurementsElement(excelFile, myData, new ExperimentData(), ec_number,
							data_start_col, anno);
		
		e.setHeader(header);
		
		return e;
		
		/*
		 * Element root = new Element("experimentdata"); Document doc = new
		 * Document(root);
		 * ExperimentData experimentData = new ExperimentData();
		 * status1="Process File-Content..."; status2="Add Experiment-Header";
		 * root.addContent(getExperimentHeaderElement(excelFile, myData,
		 * experimentData)); status1="Process File-Content..."; status2="";
		 * root.addContent(getExperimentMeasurementsElement( excelFile, myData,
		 * experimentData, ec_number, data_start_col, anno));
		 * return MappingData.getMappingsFromXMLdocument(doc);
		 */
	}
	
	/**
	 * Add a Annotation object to the anno - list, if the excelColumnHeader can
	 * be found in the data. If the column can not be found in the excel data,
	 * the annotation is not added to the list.
	 * 
	 * @param anno
	 * @param myData
	 * @param attributeName
	 * @param excelColumnHeader
	 */
	private void checkHeaderInfo(ArrayList<Annotation> anno, TableData myData, String attributeName,
						String excelColumnHeader) {
		if (getHeaderColumn(myData, excelColumnHeader) >= 0) {
			anno.add(new Annotation(attributeName, getHeaderColumn(myData, excelColumnHeader)));
		} else {
			// ErrorMsg.addErrorMessage("Information: Could not find Excel Column Header \""+excelColumnHeader+"\".");
		}
	}
	
	protected ExperimentInterface getExperimentMeasurementsElement(File excelFile, TableData myData,
						ExperimentData experimentData, int ec_number_col, int data_start_col, ArrayList<Annotation> anno) {
		
		if (checkStopp())
			return null;
		
		ExperimentInterface e = new Experiment();
		
		int sampleIDcount = 0;
		int dataCount = 0;
		int skipCount = 0;
		
		// INIT LIST OF ALL "LINES" (emb, per, ...)
		experimentData.searchIndividualPlantOrLineNames(myData, data_start_col, 1);
		
		// ********** EACH ROW BECOMES ONE SUBSTANCE ***********
		status1 = "Check substance definition column (" + ec_number_col + ")...";
		status2 = "";
		Set<String> knownSubstanceIds = new LinkedHashSet<String>();
		int errCnt = 0;
		for (int row = 2; row <= myData.getMaximumRow(); row++) {
			String val = myData.getUnicodeStringCellData(ec_number_col, row);
			if (val != null) {
				if (knownSubstanceIds.contains(val)) {
					ErrorMsg
										.addErrorMessage("<b>Duplicate substance row (row "
															+ row
															+ "). Substance ID: "
															+ val
															+ "</b><br>"
															+ "For this input format duplicate substance name definitions in different rows are not supported.<br>"
															+ "Please specify each substance in exactly one row. In case of replicates, use different columns, and specify the<br>"
															+ "replicate ID as defined by the input format. Use the same row for these replicates for one measured substance.");
					errCnt++;
				}
				knownSubstanceIds.add(val);
			}
		}
		knownSubstanceIds.clear();
		if (errCnt > 0) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					MainFrame.showMessageDialog("<html>For this input format each measured substance needs to be placed<br>"
										+ "in a single row. Multiple rows for the same substance are not supported.<br>"
										+ "Use different columns but the same row for different rows and time points.<br>"
										+ "Error details are available from the menu command Help/Error Messages", "Format Error");
				}
			});
		} else {
			
			for (int row = 2; row <= myData.getMaximumRow(); row++) {
				status1 = "Processing row " + (row - 1) + "/" + (myData.getMaximumRow() - 1) + ", " + skipCount
									+ " invalid rows skipped";
				status2 = "(" + dataCount + " measurement values)";
				if (checkStopp())
					return null;
				
				if (myData.getUnicodeStringCellData(ec_number_col, row) == null) {
					skipCount++;
					ErrorMsg.addErrorMessage("Data row with missing or invalid identifier (row " + row + ", column "
										+ ec_number_col + ")");
					continue;
				}
				
				SubstanceInterface substanceEntry = Experiment.getTypeManager().getNewSubstance();
				e.add(substanceEntry);
				
				substanceEntry.setRowId(row + "");
				// ********** ADD SUBSTANCE ATTRIBUTES **************
				// substance name set to EC number
				substanceEntry.setName(myData.getUnicodeStringCellData(ec_number_col, row));
				/*
				 * substanceEntry.getAttributes().add(new Attribute("name2",
				 * myData.getCellData(name2_col, row, "").toString()));
				 */
				// add annotations as attributes
				for (Annotation ann : anno) {
					substanceEntry.setAttribute(new Attribute(ann.getTitle(), myData.getCellData(ann.getColumn(), row, "")
										.toString()));
				}
				
				// ************** ADD LINE ELEMENTS **************
				int lineID = 0;
				for (Iterator<String> plantIt = experimentData.getPlantOrLineIterator(myData, 1, row); plantIt.hasNext();) {
					String plantOrLine = plantIt.next();
					lineID++;
					ConditionInterface lineEntry = Experiment.getTypeManager().getNewCondition(substanceEntry);
					substanceEntry.add(lineEntry);
					
					// ********** ADD LINE ATTRIBUTES ***********
					// ID
					lineEntry.setRowId(new Integer(lineID));
					// Name
					lineEntry.setSpecies(plantOrLine);
					// genotype, treatment
					lineEntry.setGenotype("");
					lineEntry.setTreatment("");
					
					// ********* ADD SAMPLES (TIME VALUES FOR ONE LINE like emb, per,
					// ...) **************
					for (Iterator<TimeAndPlantName> sampleIt = experimentData.getSampleTimeIterator(myData, plantOrLine,
										data_start_col, 1, row); sampleIt.hasNext();) {
						TimeAndPlantName timeAndPlantName = sampleIt.next();
						
						// ADD SAMPLE ENTRY
						SampleInterface sampleEntry = Experiment.getTypeManager().getNewSample(lineEntry);
						lineEntry.add(sampleEntry);
						// ADD SAMPLE ATTRIBUTES: ID, TIME, UNIT
						sampleEntry.setRowId(new Integer(++sampleIDcount));
						sampleEntry.setTime(new Integer(timeAndPlantName.getTime()));
						if (optTimeUnit != null && optTimeUnit.length() > 0)
							sampleEntry.setTimeUnit(optTimeUnit);
						else
							sampleEntry.setTimeUnit("day");
						
						ArrayList<DataColumnHeader> replicates = experimentData.getReplicateColumns(myData, timeAndPlantName,
											data_start_col, 1, row);
						ArrayList<ReplicateDouble> measurements = experimentData
											.getMeasurementValues(myData, replicates, row);
						// int numRepl = replicates.size();
						// ADD AVERAGE ENTRY
						SampleAverage averageEntry = Experiment.getTypeManager().getNewSampleAverage(sampleEntry);
						// ADD AVERAGE ATTRIBUTES: UNIT, REPLICATES, MIN, MAX, STDDEV
						if (optMeasurementUnit != null && optMeasurementUnit.length() > 0)
							averageEntry.setUnit(optMeasurementUnit);
						else
							averageEntry.setUnit("expression");
						averageEntry.setReplicateId(new Integer(replicates.size()));
						averageEntry.setMin(new Double(ExperimentData.getMinimum(measurements)));
						averageEntry.setMax(new Double(ExperimentData.getMaximum(measurements)));
						averageEntry.setStddev(new Double(ExperimentData.getStddev(measurements)));
						averageEntry.setValue(new Double(ExperimentData.getAverage(measurements)));
						sampleEntry.setSampleAverage(averageEntry);
						
						for (ReplicateDouble rd : measurements) {
							Double value = rd.doubleValue();
							// ADD DATA ENTRY
							NumericMeasurementInterface mesEntry = Experiment.getTypeManager().getNewMeasurement(sampleEntry);
							sampleEntry.add(mesEntry);
							// ADD DATA ATTRIBUTE: UNIT
							if (optMeasurementUnit != null && optMeasurementUnit.length() > 0)
								mesEntry.setUnit(optMeasurementUnit);
							else
								mesEntry.setUnit("expression");
							if (rd.getOptionalQualityAnnotation() != null)
								mesEntry.setQualityAnnotation(rd.getOptionalQualityAnnotation());
							mesEntry.setReplicateID(new Integer(rd.getReplicateNumber()));
							mesEntry.setValue(value);
							dataCount++;
						}
						status2 = dataCount + " measurement values processed";
					}
				}
				double pv = (row - 1d) / (myData.getMaximumRow() - 1d) * 100d;
				progressDouble = new Double(pv);
			}
		}
		// measurementCountElement.setText(new Integer(dataCount).toString());
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
	 * <imagefiles>0</imagefiles> <sizekb>0</sizekb> </experiment> <measurements>
	 * <substance id="3" name="Sucrose" formula="C12H22O11"
	 * substancegroup="sugar"> <line id="526" name="Vicia narbonensis"
	 * genotype="wild type" treatment="null"> <sample id="1285" time="-1"
	 * unit="-1"> <average unit="&#00181;mol/g" replicates="7"
	 * min="108.816751731602" max="125.640731640212"
	 * stddev="6.513326955309744">118.87748255289114</average> <data
	 * unit="&#00181;mol/g">108.816751731602</data> <data
	 * unit="&#00181;mol/g">125.640731640212</data> <data
	 * unit="&#00181;mol/g">125.598092379386</data> ...
	 */

	private ExperimentHeader getExperimentHeaderElement(File excelFile, TableData myData, ExperimentData experimentData) {
		
		ExperimentHeader res = new ExperimentHeader();
		
		if (optExperimentName != null && optExperimentName.length() > 0)
			res.setExperimentname(optExperimentName);
		else
			res.setExperimentname((excelFile != null ? excelFile.getName() : "unknown"));
		res.setRemark("");
		if (optCoordinatorValue != null && optCoordinatorValue.length() > 0)
			res.setCoordinator(optCoordinatorValue);
		else
			res.setCoordinator("");
		res.setDatabase("");
		res.setImportusername("");
		res.setImportdate((excelFile != null ? new Date(excelFile.lastModified()) : new Date()));
		res.setStartdate(new Date());
		res.setNumberOfFiles(0);
		res.setSizekb(0);
		return res;
	}
	
}
