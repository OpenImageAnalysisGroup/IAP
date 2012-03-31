/*******************************************************************************
 * Copyright (c) 2011 Image Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Nov 9, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.commands;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.OpenFileDialogService;
import org.StringManipulationTools;
import org.SystemAnalysis;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;
import org.graffiti.plugin.io.resources.MyByteArrayInputStream;
import org.graffiti.plugin.io.resources.ResourceIOManager;

import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.images.IAPimages;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.BinaryMeasurement;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;

/**
 * @author klukas
 */
public class ActionDataExportAsFilesAction extends AbstractNavigationAction implements SpecialCommandLineSupport {
	
	private MongoDB m;
	private ExperimentReference experimentReference;
	private NavigationButton src;
	private File targetDirectory;
	private String mb;
	private int files;
	private final ThreadSafeOptions tso = new ThreadSafeOptions();
	private String errorMessage;
	
	// private JTable table;
	
	public ActionDataExportAsFilesAction(String tooltip) {
		super(tooltip);
	}
	
	public ActionDataExportAsFilesAction(MongoDB m, ExperimentReference experimentReference) {
		this("Export Data Files");
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
		// res.add(src);
		return res;
	}
	
	@Override
	public String getDefaultTitle() {
		return "Export Files";
	}
	
	@Override
	public String getDefaultImage() {
		return IAPimages.saveAsArchive();
	}
	
	private static WeakHashMap<String, ActionDataExportAsFilesAction> validLinks2action = new WeakHashMap<String, ActionDataExportAsFilesAction>();
	
	public static void setOutputStreamForAction(String uiid, OutputStream os) throws Exception {
		ActionDataExportAsFilesAction da = validLinks2action.get(uiid);
		if (da == null)
			throw new Exception("" +
					"Action ID is unknown, please click data export command " +
					"button again to initiate a new download.");
		da.tso.setParam(0, os);
		da.tso.setParam(2, false);
	}
	
	public static String getFileNameForAction(String uiid) throws Exception {
		ActionDataExportAsFilesAction da = validLinks2action.get(uiid);
		if (da == null)
			throw new Exception("" +
					"Action ID is unknown, please click data export command " +
					"button again to initiate a new download.");
		return (String) da.tso.getParam(1, null);
	}
	
	public static Long getExperimentSizeForAction(String uiid) throws Exception {
		ActionDataExportAsFilesAction da = validLinks2action.get(uiid);
		if (da == null)
			throw new Exception("" +
					"Action ID is unknown, please click data export command " +
					"button again to initiate a new download.");
		return (Long) da.tso.getParam(3, null);
	}
	
	public static void waitForFinishedDownloadAction(String uiid) throws Exception {
		ActionDataExportAsFilesAction da = validLinks2action.get(uiid);
		if (da == null)
			throw new Exception("" +
					"Action ID is unknown, please click data export command " +
					"button again to initiate a new download.");
		Boolean finished;
		long seconds = 0;
		do {
			finished = (Boolean) da.tso.getParam(2, false);
			Thread.sleep(1000);
			seconds++;
			if (seconds > 60 * 60 * 24 * 7)
				break;
		} while (!finished);
	}
	
	ThreadSafeOptions written;
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		this.src = src;
		this.errorMessage = null;
		this.written = new ThreadSafeOptions();
		try {
			status.setCurrentStatusText1("Load Experiment");
			ExperimentInterface experiment = experimentReference.getData(m);
			
			String fsinfo = "";
			
			if (!SystemAnalysis.isHeadless()) {
				
				this.targetDirectory = OpenFileDialogService.getDirectoryFromUser("Select Target Directory");
				if (targetDirectory == null)
					return;
			} else {
				if (targetDirectory == null)
					throw new UnsupportedOperationException("This command can't be executed in this environment.");
			}
			status.setCurrentStatusText1("Data Export@MSG:Download initiated..." + fsinfo);
			Thread.sleep(1000);
			
			// ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(os));
			
			status.setCurrentStatusText1("Start file export");
			
			// filename:
			// SNAPSHOTNAME=Image Config_[GRAD]Grad
			// plantID SNAPSHOTNAME DATUM ZEIT.png
			
			GregorianCalendar gc = new GregorianCalendar();
			
			this.files = 0;
			for (SubstanceInterface su : experiment)
				for (ConditionInterface co : su)
					for (SampleInterface sa : co) {
						for (NumericMeasurementInterface nm : sa) {
							if (nm instanceof BinaryMeasurement) {
								BinaryMeasurement bm = (BinaryMeasurement) nm;
								if (bm.getURL() == null)
									continue;
								files++;
							}
						}
					}
			int idx = 0;
			
			long startTime = System.currentTimeMillis();
			
			ExecutorService es = Executors.newFixedThreadPool(1);
			
			for (SubstanceInterface su : experiment)
				for (ConditionInterface co : su)
					for (SampleInterface sa : co) {
						for (final NumericMeasurementInterface nm : sa) {
							if (nm instanceof BinaryMeasurement) {
								BinaryMeasurement bm = (BinaryMeasurement) nm;
								if (bm.getURL() == null)
									continue;
								
								status.setCurrentStatusValueFine(100d * (idx++) / files);
								
								Date t = new Date(nm.getParentSample().getRowId());
								gc.setTime(t);
								
								final String zefn;
								ImageData id = (ImageData) bm;
								try {
									if (bm instanceof ImageData) {
										id = (ImageData) bm;
										zefn =
												(nm.getQualityAnnotation() != null ? nm.getQualityAnnotation() + " " : id.getReplicateID() + "") +
														nm.getParentSample().getParentCondition().getParentSubstance().getName() + " " +
														(id != null ? (id.getPosition() != null ?
																StringManipulationTools.formatNumber(id.getPosition(), "000")
																		+ "Grad " : "000Grad") : "") + " " +
														nm.getParentSample().getTimeUnit() + "_" + nm.getParentSample().getTime() + " " +
														gc.get(GregorianCalendar.YEAR) + "-" +
														(gc.get(GregorianCalendar.MONTH) + 1) + "-" +
														gc.get(GregorianCalendar.DAY_OF_MONTH) + " " +
														gc.get(GregorianCalendar.HOUR_OF_DAY) + "_" +
														gc.get(GregorianCalendar.MINUTE) + "_" +
														gc.get(GregorianCalendar.SECOND) + ".png";
										
									} else {
										zefn = bm.getURL().getFileName();
									}
									// bm.getURL().getFileName();
									
									final MyByteArrayInputStream in = ResourceIOManager.getInputStreamMemoryCached(bm.getURL());
									
									if (in != null) {
										// out.putNextEntry(new ZipEntry(zefn));
										while (written.getInt() > 0)
											Thread.sleep(5);
										written.addInt(1);
										es.submit(new Runnable() {
											@Override
											public void run() {
												synchronized (targetDirectory) {
													try {
														File f = new File(targetDirectory.getAbsolutePath() + File.separator + zefn);
														OutputStream os = new BufferedOutputStream(new FileOutputStream(f));
														ResourceIOManager.copyContent(in, os);
														f.setLastModified(nm.getParentSample().getRowId());
														in.close();
													} catch (IOException e) {
														System.out.println("ERROR: " + e.getMessage());
													}
													written.addLong(in.getCount());
													written.addInt(-1);
												}
											}
										});
										
									}
								} catch (Exception e) {
									System.out.println("ERROR: " + e.getMessage());
								}
								status.setCurrentStatusText1("Create Files: " + (written.getLong() / 1024 / 1024) + " MB");
								
								long currTime = System.currentTimeMillis();
								
								double speed = written.getLong() * 1000 / (currTime - startTime) / 1024 / 1024;
								status.setCurrentStatusText2("" + (int) speed + " MB/s");
							}
						}
					}
			
			es.shutdown();
			es.awaitTermination(31, TimeUnit.DAYS);
			status.setCurrentStatusValueFine(100d);
			
			this.mb = (written.getLong() / 1024 / 1024) + "";
			tso.setParam(2, true);
		} catch (Exception e) {
			
			tso.setParam(2, true);
			
			this.errorMessage = e.getClass().getName() + ": " + e.getMessage();
			System.out.println();
			System.out.println(SystemAnalysis.getCurrentTime() + ">ERROR: " + e.getMessage());
		}
		
		// ArrayList<String> cols = new ArrayList<String>();
		// cols.add("Plant");
		// cols.add("Carrier");
		// cols.add("Experiment");
		// cols.add("Time");
		// cols.add("Weight (before watering)");
		// cols.add("Weight (after watering)");
		// cols.add("Water");
		// Object[] columns = cols.toArray();
		//
		// ExperimentInterface experiment = experimentReference.getData(m);
		// ArrayList<ReportRow> rows = new ArrayList<ReportRow>();
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
		//
		// ArrayList<NumericMeasurementInterface> workload = new ArrayList<NumericMeasurementInterface>();
		//
		// Object[][] rowdata = new Object[rows.size()][cols.size()];
		//
		// table = new JTable(rowdata, columns);
	}
	
	private void removeLostEntries() {
		ArrayList<String> lost = new ArrayList<String>();
		for (String ss : validLinks2action.keySet())
			if (validLinks2action.get(ss) == null)
				lost.add(ss);
		for (String ss : lost)
			validLinks2action.remove(ss);
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		if (errorMessage == null)
			errorMessage = "";
		else {
			errorMessage = " " + errorMessage + "";
		}
		if (targetDirectory == null)
			return new MainPanelComponent("No output directory has been selected." + errorMessage);
		else {
			if (errorMessage.trim().length() > 0)
				return new MainPanelComponent("Output incomplete. Error: " + errorMessage);
			else
				return new MainPanelComponent("Files in directory " + targetDirectory.getAbsolutePath() + " have been created (size " + mb + " MB, " + files
						+ " files)." + errorMessage);
		}
	}
	
	public ExperimentReference getExperimentReference() {
		return experimentReference;
	}
	
	public MongoDB getMongoInstance() {
		return m;
	}
	
	long startTime;
	File ff;
	
	@Override
	public boolean prepareCommandLineExecution() throws Exception {
		targetDirectory = null;
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
			targetDirectory = f;
			startTime = System.currentTimeMillis();
			ff = f;
			return true;
		}
	}
	
	@Override
	public void postProcessCommandLineExecution() {
		long fs = written.getLong();
		double mbps = fs / 1024d / 1024d / ((System.currentTimeMillis() - startTime) / 1000d);
		System.out.println(SystemAnalysis.getCurrentTime() + ">INFO: " +
				"Overall size of files is " + fs / 1024 / 1024 + " MB, " +
				"t=" + SystemAnalysis.getWaitTimeShort(System.currentTimeMillis() - startTime - 1000) + ", " +
				"speed=" + StringManipulationTools.formatNumber(mbps, "#.#") + " MB/s");
	}
	
}
