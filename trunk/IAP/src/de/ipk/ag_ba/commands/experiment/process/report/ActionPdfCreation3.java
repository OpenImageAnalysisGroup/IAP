/*******************************************************************************
 * Copyright (c) 2010 Image Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Nov 9, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.commands.experiment.process.report;

import iap.blocks.extraction.Trait;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JLabel;

import org.AttributeHelper;
import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.OpenFileDialogService;
import org.StringManipulationTools;
import org.SystemAnalysis;
import org.SystemOptions;
import org.apache.poi.common.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;
import org.graffiti.plugin.io.resources.FileSystemHandler;
import org.graffiti.plugin.io.resources.MyByteArrayInputStream;
import org.graffiti.plugin.io.resources.MyByteArrayOutputStream;
import org.graffiti.plugin.io.resources.ResourceIOManager;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.commands.experiment.ExportSetting;
import de.ipk.ag_ba.commands.experiment.process.report.pdf_report.PdfCreator;
import de.ipk.ag_ba.commands.experiment.process.report.pdf_report.clustering.DatasetFormatForClustering;
import de.ipk.ag_ba.commands.mongodb.ActionMongoOrLTexperimentNavigation;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.navigation_actions.SpecialCommandLineSupport;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.picture_gui.BackgroundThreadDispatcher;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.gui.util.IAPservice;
import de.ipk.ag_ba.gui.webstart.IAPmain;
import de.ipk.ag_ba.gui.webstart.IAPrunMode;
import de.ipk.ag_ba.image.structures.Image;
import de.ipk.ag_ba.plugins.IAPpluginManager;
import de.ipk.ag_ba.plugins.outlier.OutlierAnalysisGlobal;
import de.ipk.ag_ba.server.gwt.SnapshotDataIAP;
import de.ipk.ag_ba.server.gwt.UrlCacheManager;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Condition.ConditionInfo;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionFilter;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeader;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurement;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleInterface;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.BinaryMeasurement;

/**
 * @author klukas
 */
public class ActionPdfCreation3 extends AbstractNavigationAction implements SpecialCommandLineSupport, ConditionFilter {
	private ExperimentReference experimentReference;
	
	ArrayList<String> lastOutput = new ArrayList<String>();
	
	public static final String separator = ";";// "\t";// ";";// "\t";
	private final ThreadSafeOptions exportIndividualAngles, exportIndividualReplicates;
	
	private String finalResultFileLocation = "";
	
	private File targetDirectoryOrTargetFile = null;
	private ArrayList<ThreadSafeOptions> togglesFiltering;
	private final ArrayList<ThreadSafeOptions> divideDatasetBy;
	private boolean clustering;
	private ArrayList<ThreadSafeOptions> togglesInterestingProperties;
	private ThreadSafeOptions tsoBootstrapN, tsoSplitFirst, tsoSplitSecond;
	private boolean useIndividualReportNames;
	private String optCustomSubset;
	private ExportSetting optCustomSubsetDef;
	
	private ExperimentInterface ratioExperiment;
	
	private boolean ratioCalc;
	private boolean xlsxJPGdisabled = false;
	private final boolean xlsx;
	
	private NavigationButton src;
	
	private final boolean exportCommand;
	
	private final ThreadSafeOptions exportImages;
	
	public ActionPdfCreation3(String tooltip,
			ThreadSafeOptions exportIndividualAngles,
			ThreadSafeOptions exportIndividualReplicates,
			ThreadSafeOptions exportImages,
			boolean xlsx,
			ArrayList<ThreadSafeOptions> divideDatasetBy, boolean exportCommand) {
		super(tooltip);
		this.exportIndividualAngles = exportIndividualAngles;
		this.exportIndividualReplicates = exportIndividualReplicates;
		this.exportImages = exportImages;
		this.xlsx = xlsx;
		this.divideDatasetBy = divideDatasetBy;
		this.exportCommand = exportCommand;
	}
	
	public ActionPdfCreation3(
			ExperimentReference experimentReference,
			ArrayList<ThreadSafeOptions> divideDatasetBy,
			ThreadSafeOptions exportIndividualAngles,
			ThreadSafeOptions exportIndividualReplicates,
			ThreadSafeOptions exportImages,
			boolean xlsx,
			ArrayList<ThreadSafeOptions> togglesFiltering,
			ArrayList<ThreadSafeOptions> togglesInterestingProperties,
			ThreadSafeOptions tsoBootstrapN,
			ThreadSafeOptions tsoSplitFirst,
			ThreadSafeOptions tsoSplitSecond,
			boolean exportCommand) {
		this(experimentReference, divideDatasetBy, exportIndividualAngles, exportIndividualReplicates, exportImages,
				xlsx,
				togglesFiltering, togglesInterestingProperties,
				tsoBootstrapN,
				tsoSplitFirst, tsoSplitSecond, null, null, exportCommand);
	}
	
	public ActionPdfCreation3(
			ExperimentReference experimentReference,
			ArrayList<ThreadSafeOptions> divideDatasetBy,
			ThreadSafeOptions exportIndividualAngles,
			ThreadSafeOptions exportIndividualReplicates,
			ThreadSafeOptions exportImages,
			boolean xlsx,
			ArrayList<ThreadSafeOptions> togglesFiltering,
			ArrayList<ThreadSafeOptions> togglesInterestingProperties,
			ThreadSafeOptions tsoBootstrapN,
			ThreadSafeOptions tsoSplitFirst, ThreadSafeOptions tsoSplitSecond,
			String optCustomSubset,
			ExportSetting optCustomSubsetDef,
			boolean exportCommand) {
		this(getToolTipInfo(experimentReference, divideDatasetBy,
				exportIndividualAngles.getBval(0, false),
				exportIndividualReplicates.getBval(0, false),
				exportImages.getBval(0, false),
				xlsx,
				tsoBootstrapN, tsoSplitFirst, tsoSplitSecond),
				exportIndividualAngles, exportIndividualReplicates, exportImages, xlsx,
				divideDatasetBy, exportCommand);
		this.experimentReference = experimentReference;
		this.togglesFiltering = togglesFiltering;
		this.togglesInterestingProperties = togglesInterestingProperties;
		this.tsoBootstrapN = tsoBootstrapN;
		this.optCustomSubset = optCustomSubset;
		this.optCustomSubsetDef = optCustomSubsetDef;
		if (divideDatasetBy != null)
			for (ThreadSafeOptions tso : divideDatasetBy) {
				String s = (String) tso.getParam(0, "");
				if (tso.getBval(0, false)) {
					if (s.equals("Clustering"))
						clustering = true;
				}
			}
		this.tsoSplitFirst = tsoSplitFirst;
		this.tsoSplitSecond = tsoSplitSecond;
	}
	
	private static String getToolTipInfo(ExperimentReference experimentReference, ArrayList<ThreadSafeOptions> divideDatasetBy,
			boolean exportIndividualAngles,
			boolean exportIndividualReplicates,
			boolean exportImages,
			boolean xlsx, ThreadSafeOptions tsoBootstrapN, ThreadSafeOptions tsoSplitFirst, ThreadSafeOptions tsoSplitSecond) {
		if (divideDatasetBy == null)
			return null;
		return "Create report" +
				(exportIndividualAngles ? (xlsx ? " XLSX" : " CSV")
						: " PDF ("
								+ StringManipulationTools.getStringList(
										getArrayFrom(divideDatasetBy, tsoBootstrapN != null ? tsoBootstrapN.getInt() : -1, experimentReference.getHeader().getSequence(),
												tsoSplitFirst != null ? tsoSplitFirst.getBval(0, false) : false,
												tsoSplitSecond != null ? tsoSplitSecond.getBval(0, true) : true), ", ") + ")");
	}
	
	private static String[] getArrayFrom(ArrayList<ThreadSafeOptions> divideDatasetBy2, int nBootstrap, String stressDefinition,
			Boolean splitFirst,
			Boolean splitSecond) {
		
		ArrayList<String> res = new ArrayList<String>();
		boolean appendix = false;
		boolean ratio = false;
		boolean clustering = false;
		for (ThreadSafeOptions tso : divideDatasetBy2) {
			String s = (String) tso.getParam(0, "");
			if (tso.getBval(0, false)) {
				if (s.equals("Appendix"))
					appendix = true;
				else
					if (s.equals("Ratio"))
						ratio = true;
					else
						if (s.equals("Clustering"))
							clustering = true;
						else
							res.add(s);
			}
		}
		while (res.size() < 2)
			res.add("none");
		while (res.size() > 2)
			res.remove(2);
		if (appendix)
			res.add("TRUE");
		else
			res.add("FALSE");
		if (ratio)
			res.add("TRUE");
		else
			res.add("FALSE");
		if (clustering)
			res.add("TRUE");
		else
			res.add("FALSE");
		
		res.add(nBootstrap + "");
		
		String stressStart = "-1";
		String stressEnd = "-1";
		String stressType = "n"; // normal
		String stressLabel = "(not defined)";
		
		try {
			if (stressDefinition != null)
				for (String s : stressDefinition.split("//")) {
					s = s.trim();
					if (s.toUpperCase().startsWith("STRESS:")) {
						s = s.substring("Stress:".length());
						String[] def = s.split(";");
						stressLabel = def[3];
						stressStart = def[0];
						stressEnd = def[1];
						stressType = def[2];
					}
				}
		} catch (Exception e) {
			System.out.println(SystemAnalysis.getCurrentTime() + ">ERROR: Could not properly interpret stress definition: " + stressDefinition + ". Error: "
					+ e.getMessage());
		}
		
		res.add(stressStart);
		res.add(stressEnd);
		res.add(stressType);
		res.add(stressLabel);
		
		if (splitFirst != null && splitSecond != null) {
			if (splitFirst)
				res.add("TRUE");
			else
				res.add("FALSE");
			
			if (splitSecond)
				res.add("TRUE");
			else
				res.add("FALSE");
		} else {
			res.add("N/A");
			res.add("N/A");
		}
		
		return res.toArray(new String[] {});
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		if (ratioCalc && ratioExperiment != null) {
			ArrayList<NavigationButton> res = new ArrayList<NavigationButton>();
			res.add(new NavigationButton(new ActionMongoOrLTexperimentNavigation(new ExperimentReference(ratioExperiment)), src.getGUIsetting()));
			return res;
		} else
			return null;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
		ArrayList<NavigationButton> res = new ArrayList<NavigationButton>(currentSet);
		if (ratioCalc && ratioExperiment != null) {
			res.add(src);
			return res;
		} else
			return currentSet;
	}
	
	@Override
	public boolean requestTitleUpdates() {
		return true;
	}
	
	@Override
	public String getDefaultTitle() {
		String add = "";
		boolean foundTrue = false;
		if (togglesFiltering == null || togglesFiltering.size() == 0)
			foundTrue = true;
		else
			for (ThreadSafeOptions tso : togglesFiltering) {
				if (tso.getBval(0, true))
					foundTrue = true;
			}
		
		boolean foundTrueIP = false;
		if (togglesInterestingProperties == null || togglesInterestingProperties.size() == 0)
			foundTrueIP = true;
		else
			for (ThreadSafeOptions tso : togglesInterestingProperties) {
				if (tso.getBval(0, true))
					foundTrueIP = true;
			}
		
		if (!foundTrue && togglesFiltering.size() > 0) {
			add = "<br>[NO INPUT]";
		} else {
			if (!foundTrueIP) {
				if (clustering)
					add = "<br>[NO OVERVIEW AND CLUSTERING]";
				else
					add = "<br>[NO PROPERTY OVERVIEW]";
			}
		}
		if (optCustomSubset != null)
			add += "<br><small>(" + optCustomSubset + ")</small>";
		if (exportCommand) {
			if (exportIndividualAngles.getBval(0, false))
				add += "<br><small><font color='gray'>(separated for all camera angles)</font></small>";
			else
				add += "<br><small><font color='gray'>(summarized)</font></small>";
			
			if (!xlsx)
				return "Create CSV File" + add;
			else
				return "Create Spreadsheet (" + (xlsx ?
						(xlsxJPGdisabled || !exportImages.getBval(0, false) ? "XLSX" : "XLSX+JPG")
						: "CSV") + ")" + add;
		}
		if (SystemAnalysis.isHeadless()) {
			return "Create Report" + (xlsx ? " (XLSX)" : "")
					+ (exportIndividualAngles.getBval(0, false) ? " (side angles)" : " (avg) (" +
							StringManipulationTools.getStringList(
									getArrayFrom(divideDatasetBy, tsoBootstrapN.getInt(), experimentReference.getHeader().getSequence(),
											tsoSplitFirst.getBval(0, false), tsoSplitSecond.getBval(0, false)),
									", ") + ")") + add;
		} else {
			String[] arr = getArrayFrom(divideDatasetBy, tsoBootstrapN != null ? tsoBootstrapN.getInt() : -1,
					experimentReference.getHeader().getSequence(),
					tsoSplitFirst != null ? tsoSplitFirst.getBval(0, false) : false,
					tsoSplitSecond != null ? tsoSplitSecond.getBval(0, false) : false);
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
				return "<html><center>Create PDF (click here)<br><br>Specify overview" + (clustering ? "/<br>clustering " : "<br>") + "" +
						" properties --&gt;" + add;
			else
				return "Create PDF (click here)<br><br>Specify overview" + (clustering ? "/<br>clustering " : "") + "" +
						" properties --&gt;" + add;
		}
	}
	
	@Override
	public String getDefaultImage() {
		if (!xlsx)
			return "img/ext/gpl2/Gnome-Text-X-Generic-64.png";// IAPimages.getDownloadIcon();
		else
			return "img/ext/gpl2/Gnome-X-Office-Spreadsheet-64.png";
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		this.src = src;
		this.ratioCalc = false;
		finalResultFileLocation = "";
		ExperimentInterface experiment = experimentReference.getData(false, getStatusProvider());
		
		UrlCacheManager urlManager = new UrlCacheManager();
		
		if (SystemAnalysis.isHeadless() && !(targetDirectoryOrTargetFile != null)) {
			
		} else {
			SnapshotFilter snFilter = new MySnapshotFilter(togglesFiltering, experiment.getHeader().getGlobalOutlierInfo());
			
			boolean ratio = false;
			boolean clustering = false;
			System.out.println();
			if (divideDatasetBy != null)
				for (ThreadSafeOptions tso : divideDatasetBy) {
					System.out.println(SystemAnalysis.getCurrentTime() + ">TOGGLE: " + tso.getParam(0, "") + ":" + tso.getBval(0, false));
					if (((String) tso.getParam(0, "")).equals("Ratio"))
						ratio = tso.getBval(0, false);
					if (((String) tso.getParam(0, "")).equals("Clustering"))
						clustering = tso.getBval(0, false);
				}
			ConditionFilter cf = this;
			this.ratioCalc = true;
			if (ratio) {
				if (status != null)
					status.setCurrentStatusText2("Calculate stress-ratio");
				System.out.println("Substance-Count 0: " + experiment.size());
				experiment = experiment.calc().ratioDataset(
						new String[] { "norm", "sufficient", "control" },
						cf, snFilter,
						status);
				System.out.println("Substance-Count 1: " + experiment.size());
				if (status != null)
					status.setCurrentStatusText2("Calculate 3-segment linear model");
				System.out.println("Substance-Count 2: " + experiment.size());
				experiment.calc().fitThreeStepLinearModel("side.area.norm", "side.nir.intensity.mean", "side.hull.pc2.norm");
				System.out.println("Substance-Count 3: " + experiment.size());
				if (status != null)
					status.setCurrentStatusText2("Stress model calculated");
				
				this.ratioExperiment = experiment;
			} else
				this.ratioExperiment = null;
			
			PdfCreator p = new PdfCreator(targetDirectoryOrTargetFile);
			if (targetDirectoryOrTargetFile == null && useIndividualReportNames) {
				p.prepareTempDirectory();
				targetDirectoryOrTargetFile = p.getTempDirectory();
			}
			if (useIndividualReportNames)
				p.setUseIndividualReportNames(true);
			
			LinkedList<SnapshotDataIAP> snapshots;
			StringBuilderOrOutput csv = new StringBuilderOrOutput();
			if (!xlsx && !preventMainCSVexport) {
				if (p.getTempDirectory() == null)
					p.prepareTempDirectory();
				csv.setOutputFile(p.getTargetFile(xlsx, experimentReference.getHeader()));
			}
			
			HashMap<Integer, HashMap<Integer, Object>> row2col2value = new HashMap<Integer, HashMap<Integer, Object>>();
			if (!clustering)
				row2col2value = null;
			
			HashSet<NumericMeasurement> lowerSingle = new HashSet<NumericMeasurement>();
			HashSet<NumericMeasurement> upperSingle = new HashSet<NumericMeasurement>();
			
			HashSet<SampleInterface> lowerCombined = new HashSet<SampleInterface>();
			HashSet<SampleInterface> upperCombined = new HashSet<SampleInterface>();
			if (xlsx) {
				if (SystemOptions.getInstance().getBoolean("Export", "XLSX-Outliers do Grubbs Test", true)) {
					if (status != null)
						status.setCurrentStatusText2("Analyse replicates (for outliers)");
					System.out.println(SystemAnalysis.getCurrentTime() + ">Create snapshot data set");
					double threshold = SystemOptions.getInstance().getDouble("Export", "XLSX-Outliers Threshold", 0.05);
					boolean considerCondition = SystemOptions.getInstance().getBoolean("Export", "XLSX-Outliers Consider Metadata Condition", true);
					{
						OutlierAnalysisGlobal oa = new OutlierAnalysisGlobal(threshold, considerCondition, getExperimentReference(), lowerSingle, upperSingle, null,
								null, false);
						oa.analyse();
					}
					{
						OutlierAnalysisGlobal oa = new OutlierAnalysisGlobal(threshold, considerCondition, getExperimentReference(), null, null, lowerCombined,
								upperCombined,
								true);
						oa.analyse();
					}
				}
			}
			
			if (status != null)
				status.setCurrentStatusText2("Create snapshots");
			System.out.println(SystemAnalysis.getCurrentTime() + ">Create snapshot data set");
			StringBuilder indexHeader = new StringBuilder();
			String csvHeader = "";
			final ThreadSafeOptions written = new ThreadSafeOptions();
			
			{
				HashMap<String, Integer> indexInfo = new HashMap<String, Integer>();
				snapshots = IAPservice.getSnapshotsFromExperiment(
						urlManager, experiment, indexInfo, false,
						exportIndividualAngles.getBval(0, false), exportIndividualReplicates.getBval(0, false), snFilter, status, optCustomSubsetDef,
						exportCommand, lowerSingle, upperSingle, lowerCombined, upperCombined);
				if (snapshots != null && snaphotVisitor != null)
					for (SnapshotDataIAP s : snapshots)
						snaphotVisitor.visit(s);
				TreeMap<Integer, String> cola = new TreeMap<Integer, String>();
				for (String val : indexInfo.keySet())
					cola.put(indexInfo.get(val), val);
				for (String val : cola.values())
					indexHeader.append(separator + val);
				boolean addRowType = (exportIndividualAngles.getBval(0, false)) || (exportIndividualReplicates.getBval(0, false));
				csvHeader = getCSVheader(false,
						addRowType);
				csv.appendLine(csvHeader + indexHeader.toString() + "\r\n", written);
				if (row2col2value != null)
					row2col2value.put(0, getColumnValues((csvHeader + indexHeader.toString()).split(separator)));
			}
			if (status != null)
				status.setCurrentStatusValueFine(-1);
			
			System.out.println(SystemAnalysis.getCurrentTime() +
					">Snapshot data set has been created (" + snapshots.size() + " snapshots)");
			SXSSFWorkbook wb = xlsx ? new SXSSFWorkbook() : null;
			if (wb != null)
				wb.setCompressTempFiles(true);
			CellStyle style = null;
			CellStyle styleTL = null;
			
			CellStyle cellStyleDate = null;
			
			if (xlsx) {
				Sheet infoSheet = wb.createSheet("Experiment Info");
				Row header = infoSheet.createRow(0);
				style = wb.createCellStyle();
				styleTL = wb.createCellStyle();
				styleTL.setAlignment(CellStyle.ALIGN_LEFT);
				styleTL.setVerticalAlignment(CellStyle.VERTICAL_TOP);
				styleTL.setWrapText(true);
				
				wb.createCellStyle();
				
				CreationHelper createHelper = wb.getCreationHelper();
				cellStyleDate = wb.createCellStyle();
				cellStyleDate.setDataFormat(createHelper.createDataFormat().getFormat("m/d/yy h:mm"));
				
				Font font = wb.createFont();
				font.setBoldweight(Font.BOLDWEIGHT_BOLD);
				style.setFont(font);
				{
					Cell c = header.createCell(0);
					c.setCellValue("Property");
					c.setCellStyle(style);
				}
				{
					Cell c = header.createCell(1);
					c.setCellValue("Value");
					c.setCellStyle(style);
				}
				Map<String, Object> attributeValueMap = new LinkedHashMap<>();
				experiment.getHeader().fillAttributeMap(attributeValueMap, experiment.getNumberOfMeasurementValues());
				int row = 1;
				for (String field : attributeValueMap.keySet()) {
					Object val = attributeValueMap.get(field);
					if (val != null && !("" + val).isEmpty()) {
						if (field.equals("settings")) {
							String values = "" + attributeValueMap.get(field);
							if (!values.equals("null") && !values.isEmpty()) {
								Sheet settingsSheet = wb.createSheet("Analysis Settings");
								int rn = 0;
								for (String line : values.split("\\n")) {
									line = line.trim();
									Row r = settingsSheet.createRow(rn++);
									Cell c = r.createCell(0);
									c.setCellValue(line);
									if (line != null && line.startsWith("[") && line.endsWith("]"))
										c.setCellStyle(style);
								}
								settingsSheet.autoSizeColumn(0);
							}
						} else {
							
							Row r = infoSheet.createRow(row++);
							String v = StringManipulationTools.removeHTMLtags(ExperimentHeader.getNiceHTMLfieldNameMapping().get(field));
							
							if (field.equals("sizekb")) {
								try {
									String s = "" + attributeValueMap.get(field);
									attributeValueMap.put(field, Long.parseLong(s) / 1024 / 1024);
									val = attributeValueMap.get(field);
									v = StringManipulationTools.stringReplace(v, " (KB)", " (GB)");
								} catch (Exception nfe) {
									// empty
								}
							}
							if (field.equals("remark")) {
								try {
									String s = "" + attributeValueMap.get(field);
									s = StringManipulationTools.stringReplace(s, " // ", "\n");
									attributeValueMap.put(field, s);
									val = attributeValueMap.get(field);
								} catch (Exception nfe) {
									// empty
								}
							}
							if (field.equals("outliers")) {
								try {
									String s = "" + attributeValueMap.get(field);
									s = StringManipulationTools.stringReplace(s, "//", "\n");
									attributeValueMap.put(field, s);
									val = attributeValueMap.get(field);
								} catch (Exception nfe) {
									// empty
								}
							}
							Cell nc = r.createCell(0);
							nc.setCellValue(v);
							nc.setCellStyle(styleTL);
							if (val != null && val instanceof Date) {
								Cell cc = r.createCell(1);
								cc.setCellStyle(cellStyleDate);
								cc.setCellValue((Date) val);
							} else
								if (val != null && val instanceof Double)
									r.createCell(1).setCellValue((Double) val);
								else
									if (val != null && val instanceof Integer)
										r.createCell(1).setCellValue((Integer) val);
									else
										if (val != null && val instanceof Long)
											r.createCell(1).setCellValue((Long) val);
										else
											if (val != null && !("" + val).isEmpty()) {
												Cell c = r.createCell(1);
												c.setCellValue("" + val);
												c.setCellStyle(styleTL);
											}
						}
					}
				}
				infoSheet.autoSizeColumn(0);
				infoSheet.autoSizeColumn(1);
				
				Sheet columnSheet = wb.createSheet("Data Columns");
				
				columnSheet.createFreezePane(0, 1, 0, 1);
				
				String c = (csvHeader + indexHeader.toString()).trim();
				c = StringManipulationTools.stringReplace(c, "\r\n", "");
				c = StringManipulationTools.stringReplace(c, "\n", "");
				int rr = 0;
				header = columnSheet.createRow(0);
				{
					Cell cc = header.createCell(0);
					cc.setCellValue("Internal Property Name");
					cc.setCellStyle(style);
				}
				{
					Cell cc = header.createCell(1);
					cc.setCellValue("Analysis Result Column Name");
					cc.setCellStyle(style);
				}
				{
					Cell cc = header.createCell(2);
					cc.setCellValue("Description");
					cc.setCellStyle(style);
				}
				{
					Cell cc = header.createCell(3);
					cc.setCellValue("Additional Info and/or Documentation Source");
					cc.setCellStyle(style);
				}
				
				for (String h : c.split(separator)) {
					rr++;
					Row rrrr = columnSheet.createRow(rr);
					Cell cc0 = rrrr.createCell(0);
					cc0.setCellValue(h);
					cc0.setCellStyle(styleTL);
					
					String origH = h;
					String unit = null;
					if (h != null && h.endsWith(")") && h.contains("(")) {
						unit = h.substring(h.indexOf("(") + "(".length());
						unit = unit.substring(0, unit.length() - 1);
						h = h.substring(0, h.indexOf("(")).trim();
					}
					
					String niceName = new Trait(h).getNiceName();
					if (niceName == null) {
						niceName = origH;
						niceName = StringManipulationTools.stringReplace(niceName, "(", "[");
						niceName = StringManipulationTools.stringReplace(niceName, ")", "]");
					} else
						if (unit != null)
							niceName = niceName + " [" + unit + "]";
					
					if (disableNiceNameMapping)
						niceName = origH;
					
					Cell cc = rrrr.createCell(1);
					cc.setCellValue(niceName);
					cc.setCellStyle(styleTL);
					
					cc = rrrr.createCell(2);
					String desc = IAPpluginManager.getInstance().getDescriptionForCalculatedProperty(h);
					if (desc == null || desc.isEmpty())
						desc = IAPpluginManager.getInstance().getDescriptionForCalculatedProperty(origH);
					String source = null;
					if (desc != null && desc.contains("Source:")) {
						source = desc.substring(desc.indexOf("Source:") + "Source:".length()).trim();
						if (!source.isEmpty() && source.length() > 1)
							source = source.substring(0, 1).toUpperCase() + source.substring(1);
						desc = desc.substring(0, desc.indexOf("Source:")).trim();
					}
					cc.setCellValue(desc != null && !desc.isEmpty() ?
							StringManipulationTools.stringReplace(
									StringManipulationTools.getWordWrapString(StringManipulationTools.removeHTMLtags(desc), 70),
									"<br>", "\n")
							: "- no description available -");
					cc.setCellStyle(styleTL);
					if (source != null) {
						cc = rrrr.createCell(3);
						cc.setCellValue(StringManipulationTools.stringReplace(
								StringManipulationTools.getWordWrapString(StringManipulationTools.removeHTMLtags(source), 50),
								"<br>", "\n"));
						cc.setCellStyle(styleTL);
					}
				}
				
				columnSheet.autoSizeColumn(0);
				columnSheet.autoSizeColumn(1);
				columnSheet.autoSizeColumn(2);
				columnSheet.autoSizeColumn(3);
			}
			
			Sheet sheet = xlsx ? wb.createSheet("Analysis Results") : null;
			
			ArrayList<String> excelColumnHeaders = new ArrayList<String>();
			if (sheet != null) {
				// create Header row
				Row row = sheet.createRow(0);
				int col = 0;
				String c = (csvHeader + indexHeader.toString()).trim();
				c = StringManipulationTools.stringReplace(c, "\r\n", "");
				c = StringManipulationTools.stringReplace(c, "\n", "");
				for (String h : c.split(separator)) {
					
					String origH = h;
					String unit = null;
					if (h != null && h.endsWith(")") && h.contains("(")) {
						unit = h.substring(h.indexOf("(") + "(".length());
						unit = unit.substring(0, unit.length() - 1);
						h = h.substring(0, h.indexOf("(")).trim();
					}
					
					String niceName = new Trait(h).getNiceName();
					if (niceName == null) {
						niceName = origH;
						niceName = StringManipulationTools.stringReplace(niceName, "(", "[");
						niceName = StringManipulationTools.stringReplace(niceName, ")", "]");
					} else
						if (unit != null)
							niceName = niceName + " [" + unit + "]";
					
					if (disableNiceNameMapping)
						niceName = origH;
					
					Cell cc = row.createCell(col++);
					cc.setCellValue(niceName);
					excelColumnHeaders.add(h);
					if (style != null)
						cc.setCellStyle(style);
				}
			}
			
			if (xlsx) {
				if (status != null)
					status.setCurrentStatusText2(xlsx ? "Fill Excel Sheet" : "Prepare CSV content");
				experiment = null;
				File target = exportImages.getBval(0, false) ? OpenFileDialogService.getDirectoryFromUser("Select Target Directory") : null;
				String path = null;
				if (target != null) {
					customTargetFileName = target.getAbsolutePath() + "/" + StringManipulationTools.getFileSystemName(experimentReference.getExperimentName())
							+ ".xlsx";
					p.setCustomClusterTargetFile(customTargetFileName);
					path = target.getAbsolutePath() + "/images";
					if (!new File(path).exists())
						new File(path).mkdirs();
				} else
					xlsxJPGdisabled = true;
				setExcelSheetValues(snapshots, sheet, excelColumnHeaders, status, urlManager, path);
				snapshots = null;
			} else {
				if (status != null)
					status.setCurrentStatusText2("Create CSV file");
				
				boolean germanLanguage = false;
				int row = 1; // header is added before at row 0
				for (SnapshotDataIAP s : snapshots) {
					String rowContent = s.getCSVvalue(germanLanguage, separator, urlManager,
							(!exportIndividualAngles.getBval(0, false)) && (!exportIndividualReplicates.getBval(0, false)));
					csv.appendLine(rowContent, written);
					if (row2col2value != null) {
						row2col2value.put(row++, getColumnValues(rowContent.split(separator)));
						status.setCurrentStatusText2("Fill table in memory (row " + (row - 1) + ")");
					} else
						if (status != null)
							if (!csv.hasFileOutput())
								status.setCurrentStatusText2("Created in memory (" + csv.length() / 1024 / 1024 + " MB)");
							else
								status.setCurrentStatusText2("Write to output (" + csv.length() / 1024 / 1024 + " MB)");
				}
				
				snapshots = null;
			}
			if (xlsx) {
				csv = null;
				if (status != null)
					status.setCurrentStatusValue(-1);
				if (status != null)
					status.setCurrentStatusText2("Prepare saving of file...");
				System.out.println(SystemAnalysis.getCurrentTime() + ">Save to file");
				OutputStream out;
				if (customTargetFileName != null)
					out = new FileOutputStream(customTargetFileName, xlsx);
				else {
					p.prepareTempDirectory();
					if (targetDirectoryOrTargetFile == null)
						out = new FileOutputStream(p.getTargetFile(xlsx, experimentReference.getHeader()), xlsx);
					else
						out = new FileOutputStream(targetDirectoryOrTargetFile, xlsx);
				}
				if (status != null)
					status.setCurrentStatusText1("Generate XSLX");
				out = new BufferedOutputStream(out);
				FilterOutputStream fos = new FilterOutputStream(out) {
					@Override
					public void write(int b) throws IOException {
						super.write(b);
						written.addLong(1);
						if (written.getLong() % 1024 == 0)
							if (status != null)
								status.setCurrentStatusText2("Stored on disk: " + written.getLong() / 1024 + " KB");
					}
					
				};
				wb.write(fos);
				wb.dispose();
				if (status != null)
					status.setCurrentStatusValueFine(100d);
				System.out.println(SystemAnalysis.getCurrentTime() + ">File is saved (" + written.getLong() / 1024 / 1024 + " MB)");
				if (status != null)
					status.setCurrentStatusText1("Output complete");
				if (status != null)
					status.setCurrentStatusText2("File saved (" + written.getLong() / 1024 / 1024 + " MB)");
				
				if (customTargetFileName != null && (IAPmain.getRunMode() == IAPrunMode.SWING_MAIN || IAPmain.getRunMode() == IAPrunMode.SWING_APPLET)) {
					File f = new File(customTargetFileName);
					String tempDirectory = f.getParent();
					AttributeHelper.showInFileBrowser(tempDirectory + "", f.getName());
				} else
					if (IAPmain.getRunMode() == IAPrunMode.SWING_MAIN || IAPmain.getRunMode() == IAPrunMode.SWING_APPLET) {
						File f = targetDirectoryOrTargetFile;
						if (f == null)
							f = p.getTargetFile(xlsx, experimentReference.getHeader());
						
						finalResultFileLocation = FileSystemHandler.getURL(f).toString();
						
						String tempDirectory = f.getParent();
						AttributeHelper.showInFileBrowser(tempDirectory + "", f.getName());
					}
			} else {
				if (!preventMainCSVexport) {
					if (status != null)
						status.setCurrentStatusText2("Save CSV file");
					
					if (IAPmain.getRunMode() == IAPrunMode.SWING_MAIN || IAPmain.getRunMode() == IAPrunMode.SWING_APPLET) {
						File f = p.saveReportToFile(csv, xlsx, experimentReference.getHeader(), status, written);
						
						if (status != null)
							status.setCurrentStatusValueFine(100d);
						System.out.println(SystemAnalysis.getCurrentTime() + ">File is saved (" + written.getLong() / 1024 / 1024 + " MB)");
						if (status != null)
							status.setCurrentStatusText1("Output complete");
						if (status != null)
							status.setCurrentStatusText2("File saved (" + written.getLong() / 1024 / 1024 + " MB)");
						
						finalResultFileLocation = FileSystemHandler.getURL(f).toString();
						
						String tempDirectory = f.getParent();
						AttributeHelper.showInFileBrowser(tempDirectory + "", f.getName());
					}
					csv = null;
					
				}
				if (clustering) {
					DatasetFormatForClustering transform = new DatasetFormatForClustering();
					HashSet<Integer> singleFactorCol = findGroupingColumns(csvHeader);
					HashSet<Integer> otherFactorCols = findGroupingColumns(csvHeader, new String[] { "Day (Int)" }); // e.g. "Plant ID"
					
					ArrayList<String> clusteringProperties = new ArrayList<String>();
					
					for (ThreadSafeOptions tso : togglesInterestingProperties) {
						if (tso.getBval(0, true)) {
							String colHeader = (String) tso.getParam(0, "");
							String colNiceName = (String) tso.getParam(1, "");
							if (colHeader.equals("water_weight"))
								colHeader = "Water (weight-diff)";
							if (colHeader.equals("weight_before"))
								colHeader = "Weight A (g)";
							clusteringProperties.add(colHeader);
						}
					}
					
					// columns with relevant property values, e.g. height, width, ...
					HashSet<Integer> valueCols = findGroupingColumns(csvHeader + indexHeader.toString(),
							clusteringProperties.toArray(new String[] {}));
					System.out.println(SystemAnalysis.getCurrentTime() + ">CLUSTERING-VALUE-COLS: " + StringManipulationTools.getStringList(valueCols, ", "));
					if (valueCols.size() > 0) {
						HashMap<Integer, HashMap<Integer, Object>> transformed =
								transform.reformatMultipleFactorsToSingleFactor(row2col2value, singleFactorCol,
										otherFactorCols, valueCols);
						p.setCustomClusterTargetFile(customClusterTargetFile);
						p.saveClusterDataToFile(
								de.ipk.ag_ba.commands.experiment.process.report.pdf_report.clustering.DatasetFormatForClustering.print(transformed, separator), xlsx);
						if (preventMainCSVexport) {
							String tempDirectory = new File(customClusterTargetFile).getParent();
							AttributeHelper.showInFileBrowser(tempDirectory + "", new File(customClusterTargetFile).getName());
						}
					}
				}
				if (status != null)
					status.setCurrentStatusText2("File saved");
			}
			
			if (clustering) {
				if (status != null)
					status.setCurrentStatusText2("Create input for clustering");
				if (status != null)
					status.setCurrentStatusText2("Clustering input created");
			}
			
			if (!xlsx && !useIndividualReportNames)
				p.saveScripts(new String[] {
						"createDiagrams.R",
						"calcClusters.R",
						"diagramIAP.cmd",
						"diagramIAP.bat",
						// "report.tex",
						"reportCluster.tex",
						"reportDefGeneralSection.tex",
						"reportDefHead.tex",
						"reportFooter.tex",
						// "HSV_Farbtonskala.png",
						"linearPlotList.R",
						"linearMultiPlotList.R",
						"violinPlotList.R",
						"stackedPlotList.R",
						"boxPlotList.R",
						"spiderPlotList.R",
						"linerangePlotList.R",
						"sectionMapping.R",
						// "createMissingFiles.R",
						"HSB.png",
						"fluo_bin.png",
						"nir_bin.png",
						"section.png",
						"lab_a_b_bin.png",
						"hue_bin.png"
				});
			
			if (tsoBootstrapN != null)
				if (!exportIndividualAngles.getBval(0, false) && !xlsx) {
					if (status != null)
						status.setCurrentStatusText2("Generate report images and PDF");
					int timeoutMinutes = 30;
					if (tsoBootstrapN.getInt() > 100)
						timeoutMinutes = 60 * 12; // 12 h
					if (tsoBootstrapN.getInt() > 100)
						timeoutMinutes = 60 * 24 * 7; // 7*24h
					p.executeRstat(
							getArrayFrom(
									divideDatasetBy,
									tsoBootstrapN.getInt(),
									experimentReference.getHeader().getSequence(),
									tsoSplitFirst.getBval(0, false),
									tsoSplitSecond.getBval(0, false)
							),
							experiment, status,
							lastOutput, timeoutMinutes);
					p.getOutput();
					boolean ok = p.hasPDFcontent();
					if (ok) {
						AttributeHelper.showInBrowser(p.getPDFurl());
						finalResultFileLocation = p.getPdfIOurl().toString();
					} else {
						System.out.println(SystemAnalysis.getCurrentTime() + ">ERROR: No output file available");
						finalResultFileLocation = p.getTempDirectory().toString();
					}
					if (status != null)
						status.setCurrentStatusText2("Processing finished");
					
					// p.deleteDirectory();
					
					if (SystemOptions.getInstance().getBoolean("PDF Report Generation", "remove intermediate files (just keep PDF)", false)) {
						p.deleteAllWithout(new String[] { "report.pdf" },
								new String[] { "plots", "plotTex", "section" });
					}
					
				} else {
					p.openTargetDirectory();
					finalResultFileLocation = p.getTempDirectory().toString();
				}
		}
		if (status != null)
			status.setCurrentStatusValueFine(-1d);
	}
	
	private HashSet<Integer> findGroupingColumns(String csvHeader) {
		HashSet<Integer> res = new HashSet<Integer>();
		int col = 0;
		for (String colValue : csvHeader.split(separator)) {
			for (ThreadSafeOptions tso : divideDatasetBy) {
				String s = (String) tso.getParam(0, "");
				if (!s.equals(colValue)) {
					continue;
				} else
					System.out.println("Check Col Value = " + colValue);
				
				if (tso.getBval(0, false)) {
					System.out.println("S = " + s + " TRUE");
					if (s.equals("Appendix"))
						;
					else
						if (s.equals("Ratio"))
							;
						else
							if (s.equals("Clustering"))
								;
							else
								res.add(col);
				} else
					System.out.println("S = " + s + " FALSE");
				
			}
			col++;
		}
		return res;
	}
	
	private HashSet<Integer> findGroupingColumns(String csvHeader, String[] interestingValueColumns) {
		System.out.println(csvHeader);
		HashSet<Integer> res = new HashSet<Integer>();
		int col = 0;
		for (String columnName : csvHeader.split(separator)) {
			for (String s : interestingValueColumns) {
				if (!s.equals(columnName)) {
					if (columnName.contains("("))
						columnName = columnName.substring(0, columnName.indexOf("(")).trim();
					if (!s.equals(columnName))
						continue;
				}
				res.add(col);
			}
			col++;
		}
		return res;
	}
	
	private HashMap<Integer, Object> getColumnValues(String[] value) {
		HashMap<Integer, Object> res = new HashMap<Integer, Object>();
		int idx = 0;
		for (String v : value) {
			if (v != null && !v.trim().isEmpty())
				res.put(idx, v);
			idx++;
		}
		return res;
	}
	
	public static void setExcelSheetValues(
			LinkedList<SnapshotDataIAP> snapshotsToBeProcessed,
			Sheet sheet,
			ArrayList<String> excelColumnHeaders,
			BackgroundTaskStatusProviderSupportingExternalCall status,
			UrlCacheManager urlManager, String outpath) {
		System.out.println(SystemAnalysis.getCurrentTime() + ">Fill workbook");
		int rowNum = 1;
		Runtime r = Runtime.getRuntime();
		
		CreationHelper createHelper = sheet.getWorkbook().getCreationHelper();
		CellStyle cellStyleDate = sheet.getWorkbook().createCellStyle();
		cellStyleDate.setDataFormat(createHelper.createDataFormat().getFormat("m/d/yy h:mm"));
		
		CellStyle cellStylePercent = sheet.getWorkbook().createCellStyle();
		cellStylePercent.setDataFormat(createHelper.createDataFormat().getFormat("0.00%"));
		
		CellStyle hlink_style = sheet.getWorkbook().createCellStyle();
		org.apache.poi.ss.usermodel.Font hlink_font = sheet.getWorkbook().createFont();
		hlink_font.setUnderline(org.apache.poi.ss.usermodel.Font.U_SINGLE);
		hlink_font.setColor(IndexedColors.BLUE.getIndex());
		hlink_style.setFont(hlink_font);
		
		org.apache.poi.ss.usermodel.Font upperOutlierFont = sheet.getWorkbook().createFont();
		upperOutlierFont.setColor(IndexedColors.RED.getIndex());
		org.apache.poi.ss.usermodel.Font lowerOutlierFont = sheet.getWorkbook().createFont();
		lowerOutlierFont.setColor(IndexedColors.BLUE.getIndex());
		
		CellStyle upperOutlierPercentage = sheet.getWorkbook().createCellStyle();
		upperOutlierPercentage.setDataFormat(createHelper.createDataFormat().getFormat("0.00%"));
		upperOutlierPercentage.setFont(upperOutlierFont);
		
		CellStyle upperOutlierNoPercentage = sheet.getWorkbook().createCellStyle();
		upperOutlierNoPercentage.setFont(upperOutlierFont);
		
		CellStyle lowerOutlierPercentage = sheet.getWorkbook().createCellStyle();
		lowerOutlierPercentage.setDataFormat(createHelper.createDataFormat().getFormat("0.00%"));
		lowerOutlierPercentage.setFont(lowerOutlierFont);
		
		CellStyle lowerOutlierNoPercentage = sheet.getWorkbook().createCellStyle();
		lowerOutlierNoPercentage.setFont(lowerOutlierFont);
		
		HashSet<Integer> percentColumns = new HashSet<Integer>();
		HashSet<Integer> dateColumns = new HashSet<Integer>();
		for (int i = 0; i < excelColumnHeaders.size(); i++) {
			String ch = excelColumnHeaders.get(i);
			if (ch != null && ch.endsWith("(percent)"))
				percentColumns.add(i);
		}
		
		// Freeze some rows and columns
		sheet.createFreezePane(3, 1, 3, 1);
		
		// for (String s : BuiltinFormats.getAll())
		// System.out.println("format: " + s);
		
		boolean adjusted = false;
		int scnt = snapshotsToBeProcessed.size();
		int sidx = 0;
		
		for (Integer dateCol : dateColumns)
			sheet.setColumnWidth(dateCol, 5000);
		
		HashMap<String, org.apache.poi.ss.usermodel.Hyperlink> links = new HashMap<String, org.apache.poi.ss.usermodel.Hyperlink>();
		for (SnapshotDataIAP s : snapshotsToBeProcessed) {
			for (ArrayList<DateDoubleString> valueRow : s.getCSVobjects(urlManager)) {
				for (DateDoubleString o : valueRow) {
					if (o != null && o.getString() != null && !o.getString().isEmpty()) {
						if (outpath != null)
							if (o.getLink() != null) {
								ArrayList<BinaryMeasurement> bin = o.getBinaryData();
								if (bin != null && !bin.isEmpty() && bin.size() == 1) {
									for (BinaryMeasurement bm : bin) {
										if (bm.getURL() != null) {
											String of = null;
											if (o.getString().endsWith("jpg")) {
												of = outpath + "/" + o.getString();
											} else
												if (o.getString().endsWith("png") || o.getString().endsWith("tiff") || o.getString().endsWith("tif")) {
													of = outpath + "/" + StringManipulationTools.removeFileExtension(o.getString()) + ".jpg";
												}
											if (of != null) {
												org.apache.poi.ss.usermodel.Hyperlink l = createHelper.createHyperlink(Hyperlink.LINK_FILE);
												
												String fn = StringManipulationTools.stringReplace(
														new File(StringManipulationTools.removeFileExtension(o.getString()) + ".jpg").toURI().toString(),
														" ", "%20");
												fn = fn.substring(fn.lastIndexOf("/") + 1);
												l.setAddress("images/" + fn);
												
												links.put(of, l);
											}
										}
									}
								}
							}
					}
				}
			}
		}
		
		while (!snapshotsToBeProcessed.isEmpty() && !(status != null && status.wantsToStop())) {
			SnapshotDataIAP s = snapshotsToBeProcessed.poll();
			sidx++;
			progressOutput(snapshotsToBeProcessed, status, r, scnt, sidx);
			for (ArrayList<DateDoubleString> valueRow : s.getCSVobjects(urlManager)) {
				Row row = sheet.createRow(rowNum++);
				int colNum = 0;
				
				if (!adjusted && rowNum >= 4) {
					adjustColumnWidths(sheet, excelColumnHeaders, status);
					adjusted = true;
					if (status != null)
						status.setCurrentStatusText1("Rows remaining: " + snapshotsToBeProcessed.size());
				}
				
				for (DateDoubleString o : valueRow) {
					if (o != null && o.getString() != null && !o.getString().isEmpty()) {
						Cell c = row.createCell(colNum++);
						c.setCellValue(o.getString());
						if (outpath != null) {
							if (o.getLink() != null) {
								ArrayList<BinaryMeasurement> bin = o.getBinaryData();
								boolean ok = false;
								if (bin != null && !bin.isEmpty() && bin.size() == 1) {
									for (BinaryMeasurement bm : bin) {
										String of = null;
										if (bm.getURL() != null) {
											if (o.getString().endsWith("jpg")) {
												// direct copy
												// StringManipulationTools.removeFileExtension(o.getString()) + ".jpg"
												of = outpath + "/" + o.getString();
												if (new File(of).exists()) {
													ok = true;
												} else {
													FileOutputStream out;
													try {
														out = new FileOutputStream(new File(of));
														ResourceIOManager.copyContent(bm.getURL().getInputStream(), out);
														ok = true;
													} catch (Exception e) {
														System.out.println(SystemAnalysis.getCurrentTime() + ">WARNING: Could not create image file in output directory: "
																+ e.getMessage());
													}
												}
											} else
												if (o.getString().endsWith("png") || o.getString().endsWith("tiff") || o.getString().endsWith("tif")) {
													// convert
													of = outpath + "/" + StringManipulationTools.removeFileExtension(o.getString()) + ".jpg";
													final String off = of;
													if (new File(of).exists()) {
														ok = true;
													} else {
														try {
															final MyByteArrayOutputStream out = new MyByteArrayOutputStream();
															ResourceIOManager.copyContent(bm.getURL().getInputStream(), out);
															Runnable rr = new Runnable() {
																@Override
																public void run() {
																	MyByteArrayInputStream in = new MyByteArrayInputStream(out.getBuffTrimmed());
																	Image i;
																	try {
																		i = new Image(in);
																		i.saveToFile(off);
																	} catch (IOException e) {
																		System.out.println(SystemAnalysis.getCurrentTime()
																				+ ">WARNING: Could not create image file in output directory: "
																				+ e.getMessage());
																	}
																}
															};
															BackgroundThreadDispatcher.addTask(rr, "Convert Image to JPG");
															
															ok = true;
														} catch (Exception e) {
															System.out.println(SystemAnalysis.getCurrentTime() + ">WARNING: Could not create image file in output directory: "
																	+ e.getMessage());
														}
													}
												} else {
													// ignore
												}
										}
										if (ok) {
											// add link to image
											if (of != null) {
												org.apache.poi.ss.usermodel.Hyperlink file_link = links.get(of);
												if (file_link != null) {
													c.setHyperlink(file_link);
													c.setCellStyle(hlink_style);
												}
											}
										}
									}
								}
								
							}
						}
						// } else {
						// org.apache.poi.ss.usermodel.Hyperlink file_link = createHelper.createHyperlink(Hyperlink.LINK_FILE);
						// String fn =
						// StringManipulationTools.stringReplace(
						// new File(StringManipulationTools.removeFileExtension(o.getString()) + ".jpg").toURI().toString(),
						// " ", "%20");
						// fn = fn.substring(fn.lastIndexOf("/") + 1);
						// file_link.setAddress("images/" + fn);
						// c.setHyperlink(file_link);
						// c.setCellStyle(hlink_style);
						// }
					} else
						if (o != null && o.getDouble() != null) {
							Cell cell = row.createCell(colNum++);
							cell.setCellValue(o.getDouble());
							if (percentColumns.contains(colNum)) {
								cell.setCellStyle(cellStylePercent);
							}
							if (o.getFlag() != null) {
								boolean upper = o.getFlag();
								if (upper) {
									if (percentColumns.contains(colNum))
										cell.setCellStyle(upperOutlierPercentage);
									else
										cell.setCellStyle(upperOutlierNoPercentage);
								} else {
									if (percentColumns.contains(colNum))
										cell.setCellStyle(lowerOutlierPercentage);
									else
										cell.setCellStyle(lowerOutlierNoPercentage);
									
								}
							}
						} else
							if (o != null && o.getDate() != null) {
								dateColumns.add(colNum);
								Cell cell = row.createCell(colNum++);
								cell.setCellValue(o.getDate());
								cell.setCellStyle(cellStyleDate);
							} else
								colNum++;
				}
			}
		}
		
		if (!adjusted) {
			adjustColumnWidths(sheet, excelColumnHeaders, status);
			adjusted = true;
		}
		
		if (status != null)
			status.setCurrentStatusText1("Workbook is filled");
		
		System.out.println(SystemAnalysis.getCurrentTime() + ">Workbook is filled");
	}
	
	private static void progressOutput(LinkedList<SnapshotDataIAP> snapshotsToBeProcessed, BackgroundTaskStatusProviderSupportingExternalCall status, Runtime r,
			int scnt, int sidx) {
		if (status != null) {
			status.setCurrentStatusValueFine(100d * sidx / scnt);
			status.setCurrentStatusText1("Export data, remaining rows: " + snapshotsToBeProcessed.size());
			status.setCurrentStatusText2("<small><font color='gray'>Memory status: "
					+ r.freeMemory() / 1024 / 1024 + " MB free, " + r.totalMemory() / 1024 / 1024
					+ " total MB, " + r.maxMemory() / 1024 / 1024 + " max MB</font></small>");
		}
		System.out.println(SystemAnalysis.getCurrentTime() + ">Filling workbook, todo: " + snapshotsToBeProcessed.size() + " "
				+ r.freeMemory() / 1024 / 1024 + " MB free, " + r.totalMemory() / 1024 / 1024
				+ " total MB, " + r.maxMemory() / 1024 / 1024 + " max MB");
	}
	
	private static void adjustColumnWidths(Sheet sheet, ArrayList<String> excelColumnHeaders,
			BackgroundTaskStatusProviderSupportingExternalCall status) {
		for (int i = 0; i < excelColumnHeaders.size(); i++) {
			if (i >= 100)
				continue;
			if (status != null)
				status.setCurrentStatusText1("Adjust width of column " + (i + 1) + "/" + excelColumnHeaders.size() + "...");
			sheet.autoSizeColumn(i);
			// System.out.println("w=" + sheet.getColumnWidth(i));
			if (sheet.getColumnWidth(i) > 10000)
				sheet.setColumnWidth(i, 10000);
			else
				if (sheet.getColumnWidth(i) < 10)
					sheet.setColumnWidth(i, 10000);
		}
	}
	
	@Override
	public boolean filterConditionOut(ConditionInterface s) {
		if (togglesFiltering == null)
			return false;
		for (ThreadSafeOptions t : togglesFiltering) {
			if (matchCondition(t, s))
				return true;
		}
		return false;
	}
	
	@Override
	public String getDefaultTooltip() {
		String res = "<html>" + getToolTipInfo(experimentReference, divideDatasetBy,
				exportIndividualAngles.getBval(0, false), exportIndividualReplicates.getBval(0, false),
				exportImages.getBval(0, false),
				xlsx, tsoBootstrapN, tsoSplitFirst, tsoSplitSecond);
		synchronized (lastOutput) {
			res += "<br>Last output:<br>" + StringManipulationTools.getStringList(lastOutput, "<br>");
		}
		return res;
	}
	
	private boolean matchCondition(ThreadSafeOptions t, ConditionInterface s) {
		if (t.getBval(0, true))
			return false;
		// filter is active, check if snapshot matches criteria
		// e.g. tso.setParam(0, setting); // Condition, Species, Genotype, Variety, Treatment
		// e.g. tso.setParam(1, c);
		
		String field = (String) t.getParam(0, "");
		String content = (String) t.getParam(1, "");
		String value = null;
		if (field.equals("Condition"))
			value = s.getConditionName();
		else
			if (field.equals(ConditionInfo.SPECIES.toString()))
				value = s.getSpecies();
			else
				if (field.equals(ConditionInfo.GENOTYPE.toString()))
					value = s.getGenotype();
				else
					if (field.equals(ConditionInfo.VARIETY.toString()))
						value = s.getVariety();
					else
						if (field.equals(ConditionInfo.SEQUENCE.toString()))
							value = s.getSequence();
						else
							if (field.equals(ConditionInfo.GROWTHCONDITIONS.toString()))
								value = s.getGrowthconditions();
							else
								if (field.equals(ConditionInfo.TREATMENT.toString()))
									value = s.getTreatment();
		if (value == null)
			value = "(not specified)";
		else
			if (value.isEmpty())
				value = "(not specified)";
		
		return value.equals(content);
	}
	
	public static String replaceInvalidChars(String experimentName) {
		String res = StringManipulationTools.stringReplace(experimentName, ":", "_");
		res = StringManipulationTools.stringReplace(res, "\\", "");
		res = StringManipulationTools.stringReplace(res, "[", "|");
		res = StringManipulationTools.stringReplace(res, "]", "|");
		res = StringManipulationTools.stringReplace(res, "/", "_");
		return res;
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		if (SystemAnalysis.isHeadless())
			return new MainPanelComponent(new JLabel());
		else
			return new MainPanelComponent("The generated file will be shown or opened automatically in a moment.<br><br>File or folder location: <a href='"
					+ finalResultFileLocation + "'>" + finalResultFileLocation + "</a>");
	}
	
	public ExperimentReference getExperimentReference() {
		return experimentReference;
	}
	
	public static String getCSVheader(boolean addLineFeed, boolean addRowAndImageTypes) {
		return (addRowAndImageTypes ?
				"Row Type" + separator + "Angle" + separator : "") +
				"Plant ID" + separator + "Condition" + separator + "Species" + separator + "Genotype"
				+ separator + "Variety" + separator
				+ "GrowthCondition"
				+ separator + "Treatment" + separator + "Sequence" + separator + "Day" + separator + "Time" + separator + "Day (Int)"
				+ separator + "Day (Float)"
				+ separator + "Weight A (g)" + separator + "Weight B (g)"
				+ separator + "Water (sum of day)"
				+ separator + "Water (weight-diff)"
				+ (addRowAndImageTypes ?
						separator + "RGB" + separator + "FLUO" + separator + "NIR" + separator + "IR" + separator + "OTHER" +
								separator + "RGB Config" + separator + "FLUO Config" + separator + "NIR Config" + separator + "IR Config" + separator + "OTHER Config"
						: "")
				+ (addLineFeed ? "\r\n" : "");
	}
	
	long startTime;
	File ff;
	private String customTargetFileName;
	
	private String customClusterTargetFile;
	
	private boolean preventMainCSVexport;
	
	private SnapshotVisitor snaphotVisitor;
	
	private boolean disableNiceNameMapping;
	
	@Override
	public boolean prepareCommandLineExecution() throws Exception {
		targetDirectoryOrTargetFile = null;
		if (xlsx)
			return prepareCommandLineExecutionFile();
		else
			return prepareCommandLineExecutionDirectory(true);
	}
	
	@Override
	public void postProcessCommandLineExecution() {
		if (xlsx)
			postProcessCommandLineExecutionFile();
		else
			postProcessCommandLineExecutionDirectory(false);
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
	
	public boolean prepareCommandLineExecutionDirectory(boolean createTempDirectory) throws Exception {
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
	
	public void postProcessCommandLineExecutionDirectory(boolean openDirectory) {
		// long fs = written.getLong();
		// double mbps = fs / 1024d / 1024d / ((System.currentTimeMillis() - startTime) / 1000d);
		// System.out.println(SystemAnalysis.getCurrentTime() + ">INFO: " +
		// "Overall size of files is " + fs / 1024 / 1024 + " MB, " +
		// "t=" + SystemAnalysis.getWaitTimeShort(System.currentTimeMillis() - startTime - 1000) + ", " +
		// "speed=" + StringManipulationTools.formatNumber(mbps, "#.#") + " MB/s");
		if (targetDirectoryOrTargetFile != null)
			System.out.println(SystemAnalysis.getCurrentTime() + ">INFO: Data processing complete, " +
					"target directory contains " +
					targetDirectoryOrTargetFile.list().length + " files.");
		if (openDirectory & targetDirectoryOrTargetFile != null)
			try {
				AttributeHelper.showInFileBrowser(targetDirectoryOrTargetFile.getCanonicalPath(), null);
			} catch (Exception e) {
				e.printStackTrace();
			}
	}
	
	public void setExperimentReference(ExperimentReference er) {
		experimentReference = er;
	}
	
	public void setUseIndividualReportNames(boolean useIndividualReportNames) {
		this.useIndividualReportNames = useIndividualReportNames;
	}
	
	public void setCustomTargetFileName(String customTargetFileName) {
		this.customTargetFileName = customTargetFileName;
	}
	
	public void setClustering(boolean c) {
		clustering = c;
	}
	
	public void setCustomClusterTargetFileName(String customClusterTargetFile) {
		this.customClusterTargetFile = customClusterTargetFile;
	}
	
	public void setPreventMainCSVexport(boolean preventMainCSVexport) {
		this.preventMainCSVexport = preventMainCSVexport;
	}
	
	public void setCustomTargetFileName2(String fn) {
		this.targetDirectoryOrTargetFile = new File(fn);
	}
	
	public void setSnapshotVisitor(SnapshotVisitor sv) {
		this.snaphotVisitor = sv;
	}
	
	public void setDisableNiceNameMapping(boolean disableNiceNameMapping) {
		this.disableNiceNameMapping = disableNiceNameMapping;
	}
}
