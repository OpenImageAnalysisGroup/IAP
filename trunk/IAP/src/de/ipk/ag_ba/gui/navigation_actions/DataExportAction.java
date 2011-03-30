/*******************************************************************************
 * Copyright (c) 2011 Image Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Nov 9, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.gui.navigation_actions;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipOutputStream;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;
import org.graffiti.plugin.io.resources.MyByteArrayInputStream;
import org.graffiti.plugin.io.resources.ResourceIOManager;

import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.images.IAPimages;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk_gatersleben.ag_nw.graffiti.FileHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.threading.SystemAnalysis;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.BinaryMeasurement;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.MeasurementNodeType;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Substance3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;

/**
 * @author klukas
 */
public class DataExportAction extends AbstractNavigationAction {
	
	private MongoDB m;
	private ExperimentReference experimentReference;
	private NavigationButton src;
	private String fn;
	private String mb;
	private int files;
	private final ThreadSafeOptions tso = new ThreadSafeOptions();
	private String errorMessage;
	
	// private JTable table;
	
	public DataExportAction(String tooltip) {
		super(tooltip);
	}
	
	public DataExportAction(MongoDB m, ExperimentReference experimentReference) {
		this("Export Data");
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
		return "Export Data";
	}
	
	@Override
	public String getDefaultImage() {
		return IAPimages.saveAsArchive();
	}
	
	private static WeakHashMap<String, DataExportAction> validLinks2action = new WeakHashMap<String, DataExportAction>();
	
	public static void setOutputStreamForAction(String uiid, OutputStream os) throws Exception {
		DataExportAction da = validLinks2action.get(uiid);
		if (da == null)
			throw new Exception("" +
					"Action ID is unknown, please click data export command " +
					"button again to initiate a new download.");
		da.tso.setParam(0, os);
		da.tso.setParam(2, false);
	}
	
	public static String getFileNameForAction(String uiid) throws Exception {
		DataExportAction da = validLinks2action.get(uiid);
		if (da == null)
			throw new Exception("" +
					"Action ID is unknown, please click data export command " +
					"button again to initiate a new download.");
		return (String) da.tso.getParam(1, null);
	}
	
	public static Long getExperimentSizeForAction(String uiid) throws Exception {
		DataExportAction da = validLinks2action.get(uiid);
		if (da == null)
			throw new Exception("" +
					"Action ID is unknown, please click data export command " +
					"button again to initiate a new download.");
		return (Long) da.tso.getParam(3, null);
	}
	
	public static void waitForFinishedDownloadAction(String uiid) throws Exception {
		DataExportAction da = validLinks2action.get(uiid);
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
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		this.src = src;
		this.errorMessage = null;
		try {
			OutputStream os;
			
			status.setCurrentStatusText1("Load Experiment");
			ExperimentInterface experiment = experimentReference.getData(m);
			
			String fsinfo = "";
			
			if (!GraphicsEnvironment.isHeadless()) {
				this.fn = FileHelper.getFileName(".zip", "Dataset Export", experimentReference.getExperimentName() + ".zip");
				if (fn == null)
					return;
				String outFilename = fn;
				
				os = new FileOutputStream(outFilename);
			} else {
				fn = experimentReference.getExperimentName() + ".zip";
				tso.setParam(1, experimentReference.getExperimentName() + ".zip");
				String id = UUID.randomUUID().toString();
				
				status.setCurrentStatusText2(null);
				
				status.setCurrentStatusText1("Determine File Size");
				
				tso.setParam(3, Substance3D.getFileSize(Substance3D.getAllFiles(experiment, MeasurementNodeType.IMAGE)));
				
				Long fs = (Long) tso.getParam(3, null);
				if (fs != null && fs > 0) {
					fsinfo = " (" + (fs / 1024 / 1024) + " MB)";
				}
				
				removeLostEntries();
				long s = System.currentTimeMillis();
				status.setCurrentStatusText1("Download Ready" + fsinfo + "@OTL:iap_gwt/img?zi=" + id);
				try {
					validLinks2action.put(id, this);
					do {
						Thread.sleep(100);
						os = (OutputStream) tso.getParam(0, null);
						long t = System.currentTimeMillis();
						if (t - s > 60000)
							break;
						if (t - s > 10000) {
							status.setCurrentStatusText1("Download Ready (" + (60 - (t - s) / 1000) + " s)@OTL:iap_gwt/img?zi=" + id);
						}
					} while (os == null);
					if (os == null)
						fn = null;
					tso.setParam(0, null);
				} finally {
					validLinks2action.remove(id);
				}
			}
			if (os == null)
				return;
			status.setCurrentStatusText1("Data Export@MSG:Download initiated..." + fsinfo);
			Thread.sleep(1000);
			
			// ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(os));
			
			final ZipOutputStream out = new ZipOutputStream(os);
			
			out.setLevel(0);
			out.setComment("Created with IAP by user " + SystemAnalysis.getUserName() + ".");
			out.setMethod(ZipOutputStream.DEFLATED);
			
			status.setCurrentStatusText1("Create ZIP");
			
			// filename:
			// SNAPSHOTNAME=Image Config_[GRAD]Grad
			// plantID SNAPSHOTNAME DATUM ZEIT.png
			
			GregorianCalendar gc = new GregorianCalendar();
			
			final ThreadSafeOptions written = new ThreadSafeOptions();
			
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
						for (NumericMeasurementInterface nm : sa) {
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
												(nm.getQualityAnnotation() != null ? nm.getQualityAnnotation() + " " : "") +
														nm.getParentSample().getParentCondition().getParentSubstance().getName() + " " +
														(id != null ? (id.getPosition() != null ? id.getPosition().intValue() + "Grad " : "0Grad") : "") + " " +
														nm.getParentSample().getTimeUnit() + "_" + nm.getParentSample().getTime() + " " +
														gc.get(GregorianCalendar.YEAR) + "-" +
														gc.get(GregorianCalendar.MONTH) + "-" +
														gc.get(GregorianCalendar.DAY_OF_MONTH) + " " +
														gc.get(GregorianCalendar.HOUR_OF_DAY) + "_" +
														gc.get(GregorianCalendar.MINUTE) + "_" +
														gc.get(GregorianCalendar.SECOND) + ".png";
										
									} else {
										zefn = bm.getURL().getFileName();
									}
									// bm.getURL().getFileName();
									
									final MyByteArrayInputStream in = ResourceIOManager.getInputStreamMemoryCached(bm.getURL());
									
									// out.putNextEntry(new ZipEntry(zefn));
									while (written.getInt() > 0)
										Thread.sleep(5);
									written.addInt(1);
									es.submit(new Runnable() {
										@Override
										public void run() {
											synchronized (out) {
												try {
													ZipArchiveEntry entry = new ZipArchiveEntry(zefn);
													entry.setSize(in.getCount());
													entry.setCrc(in.getCRC32());
													out.putNextEntry(entry);
													out.write(in.getBuff(), 0, in.getCount());
													out.closeEntry();
												} catch (IOException e) {
													System.err.println("ERROR: " + e.getMessage());
												}
												written.addLong(in.getCount());
												written.addInt(-1);
											}
										}
									});
									
									// int len;
									// while ((len = in.read(buf)) > 0) {
									// out.write(buf, 0, len);
									// written += len;
									// }
									// Complete the entry
									// out.closeEntry();
									in.close();
								} catch (Exception e) {
									System.err.println("ERROR: " + e.getMessage());
								}
								status.setCurrentStatusText1("Create ZIP: " + (written.getLong() / 1024 / 1024) + " MB");
								
								long currTime = System.currentTimeMillis();
								
								double speed = written.getLong() * 1000 / (currTime - startTime) / 1024 / 1024;
								status.setCurrentStatusText2("" + (int) speed + " MB/s");
							}
						}
					}
			
			es.shutdown();
			es.awaitTermination(31, TimeUnit.DAYS);
			out.flush();
			out.close();
			status.setCurrentStatusValueFine(100d);
			
			this.mb = (written.getLong() / 1024 / 1024) + "";
			tso.setParam(2, true);
		} catch (Exception e) {
			
			tso.setParam(2, true);
			
			if (fn != null && fn.trim().length() > 0 && new File(fn).exists())
				new File(fn).delete();
			this.errorMessage = e.getClass().getName() + ": " + e.getMessage();
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
		if (fn == null)
			return new MainPanelComponent("No output file has been generated." + errorMessage);
		else {
			if (errorMessage.trim().length() > 0)
				return new MainPanelComponent("Output incomplete. Error: " + errorMessage);
			else
				return new MainPanelComponent("The file " + fn + " has been created (size " + mb + " MB, " + files + " files)." + errorMessage);
		}
	}
	
	public ExperimentReference getExperimentReference() {
		return experimentReference;
	}
	
	public MongoDB getMongoInstance() {
		return m;
	}
}
