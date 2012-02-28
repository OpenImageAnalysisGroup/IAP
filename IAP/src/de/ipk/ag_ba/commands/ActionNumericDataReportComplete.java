/*******************************************************************************
 * Copyright (c) 2010 Image Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Nov 9, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.commands;

import java.io.File;
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
import org.graffiti.plugin.algorithm.ThreadSafeOptions;

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
public class ActionNumericDataReportComplete extends AbstractNavigationAction implements SpecialCommandLineSupport {
	
	private MongoDB m;
	private ExperimentReference experimentReference;
	private NavigationButton src;
	
	ArrayList<String> lastOutput = new ArrayList<String>();
	
	private static final String separator = ";";// "\t";// ";";// "\t";
	private final boolean exportIndividualAngles;
	private final boolean xlsx;
	
	private File targetDirectoryOrTargetFile = null;
	private ArrayList<ThreadSafeOptions> toggles;
	private final ArrayList<ThreadSafeOptions> divideDatasetBy;
	
	public ActionNumericDataReportComplete(String tooltip,
			boolean exportIndividualAngles,
			ArrayList<ThreadSafeOptions> divideDatasetBy, boolean xlsx) {
		super(tooltip);
		this.exportIndividualAngles = exportIndividualAngles;
		this.divideDatasetBy = divideDatasetBy;
		this.xlsx = xlsx;
	}
	
	public ActionNumericDataReportComplete(MongoDB m, ExperimentReference experimentReference,
			boolean exportIndividualAngles, ArrayList<ThreadSafeOptions> divideDatasetBy, boolean xlsx,
			ArrayList<ThreadSafeOptions> toggles) {
		this("Create report" +
				(exportIndividualAngles ? (xlsx ? " XLSX" : " CSV")
						: " PDF (" + StringManipulationTools.getStringList(getArrayFrom(divideDatasetBy), ", ") + ")"),
				exportIndividualAngles,
				divideDatasetBy, xlsx);
		this.m = m;
		this.experimentReference = experimentReference;
		this.toggles = toggles;
	}
	
	private static String[] getArrayFrom(ArrayList<ThreadSafeOptions> divideDatasetBy2) {
		ArrayList<String> res = new ArrayList<String>();
		boolean appendix = false;
		for (ThreadSafeOptions tso : divideDatasetBy2) {
			String s = (String) tso.getParam(0, "");
			if (tso.getBval(0, false)) {
				if (s.equals("Appendix"))
					appendix = true;
				else
					res.add(s);
			}
		}
		while (res.size() < 2)
			res.add("none");
		if (appendix)
			res.add("TRUE");
		else
			res.add("FALSE");
		if (res.size() != 3)
			System.out.println("WARNING: " + res.size());
		return res.toArray(new String[] {});
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		return null;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
		ArrayList<NavigationButton> res = new ArrayList<NavigationButton>(currentSet);
		// res.add(src);
		return res;
	}
	
	@Override
	public boolean requestTitleUpdates() {
		return true;
	}
	
	@Override
	public String getDefaultTitle() {
		String add = "";
		boolean foundTrue = false;
		if (toggles == null || toggles.size() == 0)
			foundTrue = true;
		else
			for (ThreadSafeOptions tso : toggles) {
				if (tso.getBval(0, true))
					foundTrue = true;
			}
		if (!foundTrue)
			add = "<br>[NO INPUT]";
		if (exportIndividualAngles)
			return "Save " + (xlsx ? "XLSX" : "CSV") + " Data Table" + add;
		if (SystemAnalysis.isHeadless()) {
			return "Create Report" + (xlsx ? " (XLSX)" : "")
					+ (exportIndividualAngles ? " (side angles)" : " (avg) (" +
							StringManipulationTools.getStringList(
									getArrayFrom(divideDatasetBy), ", ") + ")") + add;
		} else {
			String[] arr = getArrayFrom(divideDatasetBy);
			String filter = StringManipulationTools.getStringList(
					arr, ", ");
			if (filter.endsWith(", TRUE"))
				filter = filter.substring(0, filter.length() - ", TRUE".length());
			if (filter.endsWith(", FALSE"))
				filter = filter.substring(0, filter.length() - ", FALSE".length());
			if (filter.endsWith(", none"))
				filter = filter.substring(0, filter.length() - ", none".length());
			filter = StringManipulationTools.stringReplace(filter, ", ", " and ");
			if (arr[2].equals("TRUE"))
				return "<html><center>Create PDF with Appendix<br>(all diagrams)" + add;
			else
				return "Create short PDF" + add;
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
		if (SystemAnalysis.isHeadless() && !(targetDirectoryOrTargetFile != null)) {
			
		} else {
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
			System.out.println(SystemAnalysis.getCurrentTime() +
					">Snapshot data set has been created (" + snapshots.size() + " snapshots)");
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
			
			PdfCreator p = new PdfCreator(targetDirectoryOrTargetFile);
			if (xlsx) {
				if (status != null)
					status.setCurrentStatusText2("Create XLSX");
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
					if (filterOut(toggles, s))
						continue;
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
				if (status != null)
					status.setCurrentStatusText2("Create CSV file");
			if (exportIndividualAngles) {
				if (!xlsx)
					for (SnapshotDataIAP s : snapshots) {
						if (filterOut(toggles, s))
							continue;
						boolean germanLanguage = false;
						csv.append(s.getCSVvalue(germanLanguage, separator));
					}
			} else {
				for (SnapshotDataIAP s : snapshots) {
					if (filterOut(toggles, s))
						continue;
					boolean germanLanguage = false;
					csv.append(s.getCSVvalue(germanLanguage, separator));
				}
			}
			if (xlsx) {
				csv = null;
				if (status != null)
					status.setCurrentStatusText2("Save to file");
				System.out.println(SystemAnalysis.getCurrentTime() + ">Save to file");
				p.prepareTempDirectory();
				if (targetDirectoryOrTargetFile == null)
					wb.write(new FileOutputStream(p.getSaveFile(xlsx), xlsx));
				else
					wb.write(new FileOutputStream(targetDirectoryOrTargetFile, xlsx));
				System.out.println(SystemAnalysis.getCurrentTime() + ">File is saved");
				if (status != null)
					status.setCurrentStatusText2("File saved");
			}
			else {
				byte[] result = csv.toString().getBytes();
				csv = null;
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
						"createDiagrams.r",
						"diagramIAP.cmd",
						"diagramIAP.bat",
						"report.tex"
				});
			
			if (!exportIndividualAngles && !xlsx) {
				if (status != null)
					status.setCurrentStatusText2("Generate report images and PDF");
				p.executeRstat(getArrayFrom(divideDatasetBy), experiment, status, lastOutput);
				p.getOutput();
				boolean ok = p.hasPDFcontent();
				if (ok)
					AttributeHelper.showInBrowser(p.getPDFurl());
				else
					System.out.println(SystemAnalysis.getCurrentTime() + ">ERROR: No output file available");
				if (status != null)
					status.setCurrentStatusText2("Processing finished");
				
				// p.deleteDirectory();
			} else {
				p.openTargetDirectory();
			}
		}
	}
	
	private boolean filterOut(ArrayList<ThreadSafeOptions> toggles, SnapshotDataIAP s) {
		if (toggles == null)
			return false;
		for (ThreadSafeOptions t : toggles) {
			if (match(t, s))
				return true;
		}
		return false;
	}
	
	@Override
	public String getDefaultTooltip() {
		String res = "<html>" + super.getDefaultTooltip();
		synchronized (lastOutput) {
			res += "<br>Last output:<br>" + StringManipulationTools.getStringList(lastOutput, "<br>");
		}
		return res;
	}
	
	private boolean match(ThreadSafeOptions t, SnapshotDataIAP s) {
		if (t.getBval(0, true))
			return false;
		// filter is active, check if snapshot matches criteria
		// e.g. tso.setParam(0, setting); // Condition, Species, Genotype, Variety, Treatment
		// e.g. tso.setParam(1, c);
		
		String field = (String) t.getParam(0, "");
		String content = (String) t.getParam(1, "");
		String value = null;
		if (field.equals("Condition"))
			value = s.getCondition();
		else
			if (field.equals("Species"))
				value = s.getSpecies();
			else
				if (field.equals("Genotype"))
					value = s.getGenotype();
				else
					if (field.equals("Variety"))
						value = s.getVariety();
					else
						if (field.equals("Growth condition"))
							value = s.getGrowthCondition();
						else
							if (field.equals("Treatment"))
								value = s.getTreatment();
		if (value == null)
			value = "(not specified)";
		else
			if (value.isEmpty())
				value = "(not specified)";
		
		return value.equals(content);
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
	
	long startTime;
	File ff;
	
	@Override
	public boolean prepareCommandLineExecution() throws Exception {
		targetDirectoryOrTargetFile = null;
		if (xlsx)
			return prepareCommandLineExecutionFile();
		else
			return prepareCommandLineExecutionDirectory();
	}
	
	@Override
	public void postProcessCommandLineExecution() {
		if (xlsx)
			postProcessCommandLineExecutionFile();
		else
			postProcessCommandLineExecutionDirectory();
	}
	
	public boolean prepareCommandLineExecutionFile() throws Exception {
		System.out.println();
		System.out.println(SystemAnalysis.getCurrentTime() + ">INFO: Command requires specification of an output file name.");
		System.out.println(SystemAnalysis.getCurrentTime() + ">INFO: If no path is specified, the file will be placed in the current directory.");
		System.out.println(SystemAnalysis.getCurrentTime() + ">READY: PLEASE ENTER FILENAME (ENTER NOTHING TO CANCEL OPERATION):");
		String fileName = SystemAnalysis.getCommandLineInput();
		if (fileName == null || fileName.trim().isEmpty())
			return false;
		else {
			File f = new File(fileName);
			if (f.exists()) {
				System.out.println(SystemAnalysis.getCurrentTime() + "WARNING: File exists (" + f.getAbsolutePath() + ")");
				System.out.println(SystemAnalysis.getCurrentTime() + "READY: Enter \"yes\" to overwrite, otherwise operation will be cancelled");
				String confirm = SystemAnalysis.getCommandLineInput();
				if (confirm != null && confirm.toUpperCase().indexOf("Y") >= 0)
					; // OK
				else
					return false;
			}
			System.out.print(SystemAnalysis.getCurrentTime() + ">INFO: Output to " + f.getAbsolutePath());
			// if (!f.canWrite()) {
			// System.out.println(SystemAnalysis.getCurrentTime() + "ERROR: Can't write to file (" + f.getAbsolutePath() + ")");
			// return false;
			// }
			targetDirectoryOrTargetFile = f;
			startTime = System.currentTimeMillis();
			ff = f;
			return true;
		}
	}
	
	public void postProcessCommandLineExecutionFile() {
		long fs = ff.length();
		System.out.println(SystemAnalysis.getCurrentTime() + ">INFO: " +
				"File size " + fs / 1024 / 1024 + " MB, " +
				"t=" + SystemAnalysis.getWaitTimeShort(System.currentTimeMillis() - startTime - 1000));
	}
	
	public boolean prepareCommandLineExecutionDirectory() throws Exception {
		System.out.println();
		System.out.println(SystemAnalysis.getCurrentTime() + ">INFO: Current directory is " + (new File("").getAbsolutePath()));
		System.out.println(SystemAnalysis.getCurrentTime() + ">INFO: Command requires specification of an empty output directory name.");
		System.out.println(SystemAnalysis.getCurrentTime() + ">INFO: If a part of the specified path is not existing, it will be created.");
		System.out.println(SystemAnalysis.getCurrentTime() + ">READY: PLEASE ENTER DIRECTORY STRUCTURE (ENTER NOTHING TO CANCEL OPERATION):");
		String outputDir = SystemAnalysis.getCommandLineInput();
		if (outputDir == null || outputDir.trim().isEmpty())
			return false;
		else {
			File f = new File(outputDir);
			if (!f.exists()) {
				if (!f.mkdirs()) {
					System.out.print(SystemAnalysis.getCurrentTime() + ">ERROR: Could not create directory structure (" + f.getAbsolutePath() + ")");
					System.out.println();
					return false;
				}
			}
			if (!f.isDirectory()) {
				System.out.print(SystemAnalysis.getCurrentTime() + ">ERROR: Output specifies a file instead of a directory (" + f.getAbsolutePath() + ")");
				System.out.println();
				return false;
			}
			String[] fl = f.list();
			if (fl.length > 0) {
				System.out.print(SystemAnalysis.getCurrentTime() + ">ERROR: Output directory contains " + fl.length + " files. It needs to be empty.");
				System.out.println();
				return false;
			}
			
			System.out.print(SystemAnalysis.getCurrentTime() + ">INFO: Output to " + f.getAbsolutePath());
			// if (!f.canWrite()) {
			// System.out.println(SystemAnalysis.getCurrentTime() + "ERROR: Can't write to file (" + f.getAbsolutePath() + ")");
			// return false;
			// }
			targetDirectoryOrTargetFile = f;
			startTime = System.currentTimeMillis();
			ff = f;
			return true;
		}
	}
	
	public void postProcessCommandLineExecutionDirectory() {
		// long fs = written.getLong();
		// double mbps = fs / 1024d / 1024d / ((System.currentTimeMillis() - startTime) / 1000d);
		// System.out.println(SystemAnalysis.getCurrentTime() + ">INFO: " +
		// "Overall size of files is " + fs / 1024 / 1024 + " MB, " +
		// "t=" + SystemAnalysis.getWaitTimeShort(System.currentTimeMillis() - startTime - 1000) + ", " +
		// "speed=" + StringManipulationTools.formatNumber(mbps, "#.#") + " MB/s");
		System.out.println(SystemAnalysis.getCurrentTime() + ">INFO: Data processing complete, target directory contains "
				+ targetDirectoryOrTargetFile.list().length + " files.");
	}
	
}
