/*******************************************************************************
 * Copyright (c) 2011 Image Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Nov 9, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.commands.experiment.view_or_export;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.ObjectRef;
import org.StringManipulationTools;
import org.SystemAnalysis;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;
import org.graffiti.plugin.io.resources.MyByteArrayInputStream;
import org.graffiti.plugin.io.resources.ResourceIOManager;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.images.IAPimages;
import de.ipk.ag_ba.gui.navigation_actions.SpecialCommandLineSupport;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk_gatersleben.ag_nw.graffiti.FileHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.BinaryMeasurement;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.MeasurementNodeType;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Substance3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;

/**
 * @author klukas
 */
public class ActionDataExportTar extends AbstractNavigationAction implements SpecialCommandLineSupport {
	private ExperimentReference experimentReference;
	private String fn;
	private String mb;
	private int files;
	private final ThreadSafeOptions tso = new ThreadSafeOptions();
	private String errorMessage;
	
	public ActionDataExportTar(String tooltip) {
		super(tooltip);
	}
	
	public ActionDataExportTar(ExperimentReference experimentReference) {
		this("Create TAR file");
		this.experimentReference = experimentReference;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		return null;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
		ArrayList<NavigationButton> res = new ArrayList<NavigationButton>(currentSet);
		return res;
	}
	
	@Override
	public String getDefaultTitle() {
		return "Create TAR file";
	}
	
	@Override
	public String getDefaultImage() {
		return IAPimages.saveAsArchive();
	}
	
	private static WeakHashMap<String, ActionDataExportTar> validLinks2action = new WeakHashMap<String, ActionDataExportTar>();
	
	public static void setOutputStreamForAction(String uiid, OutputStream os) throws Exception {
		ActionDataExportTar da = validLinks2action.get(uiid);
		if (da == null)
			throw new Exception("" +
					"Action ID is unknown, please click data export command " +
					"button again to initiate a new download.");
		da.tso.setParam(0, os);
		da.tso.setParam(2, false);
	}
	
	public static String getFileNameForAction(String uiid) throws Exception {
		ActionDataExportTar da = validLinks2action.get(uiid);
		if (da == null)
			throw new Exception("" +
					"Action ID is unknown, please click data export command " +
					"button again to initiate a new download.");
		return (String) da.tso.getParam(1, null);
	}
	
	public static Long getExperimentSizeForAction(String uiid) throws Exception {
		ActionDataExportTar da = validLinks2action.get(uiid);
		if (da == null)
			throw new Exception("" +
					"Action ID is unknown, please click data export command " +
					"button again to initiate a new download.");
		return (Long) da.tso.getParam(3, null);
	}
	
	public static void waitForFinishedDownloadAction(String uiid) throws Exception {
		ActionDataExportTar da = validLinks2action.get(uiid);
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
		this.errorMessage = null;
		try {
			OutputStream os;
			
			status.setCurrentStatusText1("Load Experiment");
			ExperimentInterface experiment = experimentReference.getData();
			
			String fsinfo = "";
			
			if (!SystemAnalysis.isHeadless()) {
				this.fn = FileHelper.getFileName(".tar", "TAR File Export", experimentReference.getExperimentName() + ".tar");
				if (fn == null)
					return;
				String outFilename = fn;
				
				os = new FileOutputStream(outFilename);
			} else {
				fn = experimentReference.getExperimentName() + ".tar";
				tso.setParam(1, experimentReference.getExperimentName() + ".tar");
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
				status.setCurrentStatusText1("Download Ready" + fsinfo + "@OTL:iap_gwt/img?ti=" + id);
				try {
					validLinks2action.put(id, this);
					do {
						Thread.sleep(100);
						os = (OutputStream) tso.getParam(0, null);
						long t = System.currentTimeMillis();
						if (t - s > 60000)
							break;
						if (t - s > 10000) {
							status.setCurrentStatusText1("Download Ready (" + (60 - (t - s) / 1000) + " s)@OTL:iap_gwt/img?ti=" + id);
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
			
			final TarArchiveOutputStream out = new TarArchiveOutputStream(os);
			
			status.setCurrentStatusText1("Create TAR");
			
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
					for (final SampleInterface sa : co) {
						for (NumericMeasurementInterface nm : sa) {
							if (nm instanceof BinaryMeasurement) {
								BinaryMeasurement bm = (BinaryMeasurement) nm;
								if (bm.getURL() == null)
									continue;
								
								status.setCurrentStatusValueFine(100d * (idx++) / files);
								
								Date t = new Date(nm.getParentSample().getSampleFineTimeOrRowId());
								gc.setTime(t);
								
								String zefn;
								ImageData id = (ImageData) bm;
								
								if (bm instanceof ImageData) {
									id = (ImageData) bm;
									zefn =
											(nm.getQualityAnnotation() != null ? nm.getQualityAnnotation() + " " : nm.getReplicateID() + "") +
													nm.getParentSample().getParentCondition().getParentSubstance().getName() + " " +
													nm.getParentSample().getTimeUnit() + "_" + nm.getParentSample().getTime() + " " +
													(id != null ? (id.getPosition() != null ?
															StringManipulationTools.formatNumber(id.getPosition(), "000")
																	+ "Grad" : "000Grad") : "") + " " +
													gc.get(GregorianCalendar.YEAR) + "-" +
													(gc.get(GregorianCalendar.MONTH) + 1) + "-" +
													gc.get(GregorianCalendar.DAY_OF_MONTH) + " " +
													gc.get(GregorianCalendar.HOUR_OF_DAY) + "_" +
													gc.get(GregorianCalendar.MINUTE) + "_" +
													gc.get(GregorianCalendar.SECOND) + " ";
									
									zefn += filter(90 - zefn.length() - ".png".length(), nm.getParentSample().getParentCondition())
											+ ".png";
									
								} else {
									zefn = bm.getURL().getFileName();
								}
								// bm.getURL().getFileName();
								
								final MyByteArrayInputStream in = ResourceIOManager.getInputStreamMemoryCached(bm.getURL());
								
								final ObjectRef error = new ObjectRef();
								if (in != null) {
									// out.putNextEntry(new ZipEntry(zefn));
									final String zefnf = zefn;
									if (in.getCount() > 0 && in.getBuff().length >= in.getCount()) {
										while (written.getInt() > 0)
											Thread.sleep(5);
										written.setInt(1);
										es.submit(new Runnable() {
											@Override
											public void run() {
												try {
													TarArchiveEntry entry = new TarArchiveEntry(zefnf);
													entry.setSize(in.getCount());
													entry.setModTime(sa.getSampleFineTimeOrRowId());
													out.putArchiveEntry(entry);
													out.write(in.getBuff(), 0, in.getCount());
													out.closeArchiveEntry();
													written.addLong(in.getCount());
												} catch (Exception e) {
													error.setObject(e);
												}
												written.setInt(-1);
											}
										});
									}
									
									in.close();
								}
								if (error.getObject() != null)
									throw (Exception) error.getObject();
								
								status.setCurrentStatusText1("Create TAR: " + (written.getLong() / 1024 / 1024) + " MB");
								
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
	}
	
	private String filter(int maxLen, ConditionInterface condition) {
		maxLen -= 2;
		String conditionName = max(maxLen / 3, condition.getGenotype() + "");
		maxLen += maxLen / 3 - conditionName.length();
		conditionName += ";" + max(maxLen / 2, condition.getSpecies() + "");
		maxLen += maxLen / 2 - conditionName.length();
		conditionName += ";" + max(maxLen, condition.getTreatment() + "");
		conditionName = StringManipulationTools.stringReplace(conditionName, " ", "_");
		conditionName = StringManipulationTools.stringReplace(conditionName, ":", "_");
		conditionName = StringManipulationTools.stringReplace(conditionName, "/", "_");
		conditionName = StringManipulationTools.stringReplace(conditionName, "\\", "_");
		return conditionName;
	}
	
	private String max(int i, String s) {
		if (s.length() > i)
			s = s.substring(0, i);
		return s;
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
	
	long startTime;
	File ff;
	
	@Override
	public boolean prepareCommandLineExecution() throws Exception {
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
			tso.setParam(0, new FileOutputStream(f));
			startTime = System.currentTimeMillis();
			ff = f;
			return true;
		}
	}
	
	@Override
	public void postProcessCommandLineExecution() {
		long fs = ff.length();
		double mbps = fs / 1024d / 1024d / ((System.currentTimeMillis() - startTime) / 1000d);
		System.out.println(SystemAnalysis.getCurrentTime() + ">INFO: " +
				"File size " + fs / 1024 / 1024 + " MB, " +
				"t=" + SystemAnalysis.getWaitTimeShort(System.currentTimeMillis() - startTime - 1000) + ", " +
				"speed=" + StringManipulationTools.formatNumber(mbps, "#.#") + " MB/s");
	}
}
