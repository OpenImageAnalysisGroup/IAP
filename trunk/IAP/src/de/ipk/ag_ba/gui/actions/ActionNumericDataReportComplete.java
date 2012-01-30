/*******************************************************************************
 * Copyright (c) 2010 Image Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Nov 9, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.gui.actions;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.TreeMap;

import javax.swing.JLabel;

import org.AttributeHelper;
import org.StringManipulationTools;
import org.SystemAnalysis;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.images.IAPimages;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.mongo.IAPservice;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.server.gwt.SnapshotDataIAP;
import de.ipk.ag_ba.server.pdf_report.PdfCreator;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;

/**
 * @author klukas
 */
public class ActionNumericDataReportComplete extends AbstractNavigationAction {
	
	private MongoDB m;
	private ExperimentReference experimentReference;
	private NavigationButton src;
	
	private static final String separator = ";";// "\t";// ";";// "\t";
	private final boolean exportIndividualAngles;
	private final String[] variant;
	private final boolean xlsx;
	
	public ActionNumericDataReportComplete(String tooltip, boolean exportIndividualAngles, String[] variant, boolean xlsx) {
		super(tooltip);
		this.exportIndividualAngles = exportIndividualAngles;
		this.variant = variant;
		this.xlsx = xlsx;
	}
	
	public ActionNumericDataReportComplete(MongoDB m, ExperimentReference experimentReference, boolean exportIndividualAngles, String[] variant, boolean xlsx) {
		this("Create report" +
				(exportIndividualAngles ? (xlsx ? " XLSX" : " CSV")
						: " PDF (" + StringManipulationTools.getStringList(variant, ", ") + ")"),
				exportIndividualAngles,
				variant, xlsx);
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
		if (exportIndividualAngles)
			return "Save " + (xlsx ? "XLSX" : "CSV") + " Data Table";
		if (SystemAnalysis.isHeadless()) {
			return "Download Report" + (xlsx ? " (XLSX)" : "")
					+ (exportIndividualAngles ? " (side angles)" : " (avg) (" + StringManipulationTools.getStringList(variant, ", ") + ")");
		} else {
			String filter = StringManipulationTools.getStringList(variant, ", ");
			if (filter.endsWith(", TRUE"))
				filter = filter.substring(0, filter.length() - ", TRUE".length());
			if (filter.endsWith(", FALSE"))
				filter = filter.substring(0, filter.length() - ", FALSE".length());
			if (filter.endsWith(", none"))
				filter = filter.substring(0, filter.length() - ", none".length());
			filter = StringManipulationTools.stringReplace(filter, ", ", " and ");
			if (variant[2].equals("TRUE"))
				return "<html><center>Create full PDF report<br>"
						+ (exportIndividualAngles ? " (side angles)" : " (" + filter + ")");
			else
				return "<html><center>Create short PDF report<br>(" + filter + ")";
		}
	}
	
	@Override
	public String getDefaultImage() {
		if (exportIndividualAngles)
			return IAPimages.getDownloadIcon();
		else
			return "img/ext/gpl2/Gnome-X-Office-Spreadsheet-64.png";
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		this.src = src;
		ExperimentInterface experiment = experimentReference.getData(m);
		if (SystemAnalysis.isHeadless()) {
			
		} else {
			boolean pdf = !SystemAnalysis.isHeadless();
			
			if (pdf) {
				
				ArrayList<SnapshotDataIAP> snapshots;
				StringBuilder csv = new StringBuilder();
				boolean water = false;
				String csvHeader = getCSVheader();
				if (status != null)
					status.setCurrentStatusText2("Create snapshots");
				System.out.println(SystemAnalysis.getCurrentTime() + ">Create snapshot data set");
				if (!water) {
					HashMap<String, Integer> indexInfo = new HashMap<String, Integer>();
					snapshots = IAPservice.getSnapshotsFromExperiment(
							null, experiment, indexInfo, false,
							exportIndividualAngles);
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
					snapshots = IAPservice.getSnapshotsFromExperiment(
							null, experiment, null, false, exportIndividualAngles);
					csv.append(csvHeader);
				}
				System.out.println(SystemAnalysis.getCurrentTime() + ">Snapshot data set has been created");
				Workbook wb = xlsx ? new XSSFWorkbook() : null;
				Sheet sheet = xlsx ? wb.createSheet(replaceInvalidChars(experimentReference.getExperimentName())) : null;
				if (sheet != null) {
					Row row = sheet.createRow(0);
					int col = 0;
					String c = csv.toString().trim();
					c = StringManipulationTools.stringReplace(c, "\r\n", "");
					c = StringManipulationTools.stringReplace(c, "\n", "");
					for (String h : c.split(separator))
						row.createCell(col++).setCellValue(h);
				}
				
				PdfCreator p = new PdfCreator();
				if (xlsx) {
					experiment = null;
					if (status != null)
						status.setCurrentStatusText2("Fill workbook");
					System.out.println(SystemAnalysis.getCurrentTime() + ">Fill workbook");
					Queue<SnapshotDataIAP> todo = new LinkedList<SnapshotDataIAP>(snapshots);
					snapshots = null;
					int rowNum = 1;
					Runtime r = Runtime.getRuntime();
					while (!todo.isEmpty()) {
						SnapshotDataIAP s = todo.poll();
						if (status != null)
							status.setCurrentStatusText1("Rows remaining: " + todo.size());
						if (status != null)
							status.setCurrentStatusText2("Memory status: "
									+ r.freeMemory() / 1024 / 1024 + " MB free, " + r.totalMemory() / 1024 / 1024
									+ " total MB, " + r.maxMemory() / 1024 / 1024 + " max MB");
						System.out.println(SystemAnalysis.getCurrentTime() + ">Filling workbook, todo: " + todo.size() + " "
								+ r.freeMemory() / 1024 / 1024 + " MB free, " + r.totalMemory() / 1024 / 1024
								+ " total MB, " + r.maxMemory() / 1024 / 1024 + " max MB");
						for (ArrayList<DateDoubleString> valueRow : s.getCSVobjects()) {
							Row row = sheet.createRow(rowNum++);
							int colNum = 0;
							for (DateDoubleString o : valueRow) {
								if (o.getString() != null && !o.getString().isEmpty())
									row.createCell(colNum++).setCellValue(o.getString());
								else
									if (o.getDouble() != null)
										row.createCell(colNum++).setCellValue(o.getDouble());
									else
										if (o.getDate() != null)
											row.createCell(colNum++).setCellValue(o.getDate());
										else
											colNum++;
							}
						}
					}
					System.out.println(SystemAnalysis.getCurrentTime() + ">Workbook is filled");
				} else
					if (exportIndividualAngles) {
						for (SnapshotDataIAP s : snapshots) {
							boolean germanLanguage = false;
							csv.append(s.getCSVvalue(germanLanguage, separator));
						}
					} else {
						for (SnapshotDataIAP s : snapshots) {
							boolean germanLanguage = false;
							csv.append(s.getCSVvalue(germanLanguage, separator));
						}
					}
				if (xlsx) {
					if (status != null)
						status.setCurrentStatusText2("Save to file");
					System.out.println(SystemAnalysis.getCurrentTime() + ">Save to file");
					p.prepareTempDirectory();
					wb.write(new FileOutputStream(p.getSaveFile(xlsx), xlsx));
					System.out.println(SystemAnalysis.getCurrentTime() + ">File is saved");
					if (status != null)
						status.setCurrentStatusText2("File saved");
				}
				else {
					byte[] result = csv.toString().getBytes();
					
					p.prepareTempDirectory();
					p.saveReportCSV(result, xlsx);
				}
				
				// p.saveScripts(new String[] {
				// "diagramForReportPDF.r",
				// "diagramIAP.cmd",
				// "diagramIAP.bat",
				// "initLinux.r",
				// "report2.tex", "createDiagramFromValuesLinux.r"
				// });
				if (!xlsx)
					p.saveScripts(new String[] {
							"createDiagramOneFile.r",
							"diagramIAP.cmd",
							"diagramIAP.bat",
							"initLinux.r",
							"report2.tex"
					});
				
				if (!exportIndividualAngles && !xlsx) {
					p.executeRstat(variant, experiment);
					
					boolean ok = p.hasPDFcontent();
					
					AttributeHelper.showInBrowser(p.getPDFurl());
					
					// p.deleteDirectory();
				} else {
					p.openTargetDirectory();
				}
				
			}
		}
	}
	
	private String replaceInvalidChars(String experimentName) {
		String res = StringManipulationTools.stringReplace(experimentName, ":", "_");
		return res;
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
		return "Angle" + separator + "Plant ID" + separator + "Condition" + separator + "Species" + separator + "Genotype" + separator + "Variety" + separator
				+ "GrowthCondition"
				+ separator + "Treatment" + separator + "Sequence" + separator + "Day" + separator + "Time" + separator + "Day (Int)"
				+ separator + "Weight A (g)" + separator + "Weight B (g)" + separator +
				"Water (weight-diff)" +
				separator + "Water (pumped)" + separator + "RGB" + separator + "FLUO" + separator + "NIR" + separator + "OTHER" +
				"\r\n";
	}
}
