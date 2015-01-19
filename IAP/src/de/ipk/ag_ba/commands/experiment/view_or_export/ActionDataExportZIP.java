/*******************************************************************************
 * Copyright (c) 2011 Image Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Nov 9, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.commands.experiment.view_or_export;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.zip.ZipOutputStream;

import org.ReleaseInfo;
import org.StringManipulationTools;
import org.SystemAnalysis;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;
import org.graffiti.plugin.io.resources.MyByteArrayInputStream;
import org.graffiti.plugin.io.resources.ResourceIOManager;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.images.IAPimages;
import de.ipk.ag_ba.gui.navigation_actions.SpecialCommandLineSupport;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.picture_gui.BackgroundThreadDispatcher;
import de.ipk.ag_ba.gui.picture_gui.LocalComputeJob;
import de.ipk.ag_ba.gui.util.ExperimentReferenceInterface;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;
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
 * Improvements in the class {@link ActionDataExportTar} (like the better handling
 * of the file names) are not contained in this command.
 * 
 * @author klukas
 */
public class ActionDataExportZIP extends AbstractNavigationAction implements SpecialCommandLineSupport, ActionDataProcessing {
	
	private ExperimentReferenceInterface er;
	private String fn;
	private String mb;
	private int files;
	private final ThreadSafeOptions tso = new ThreadSafeOptions();
	private String errorMessage;
	private ThreadSafeOptions jpg;
	private ThreadSafeOptions tsoQuality;
	
	public ActionDataExportZIP(String tooltip) {
		super(tooltip);
	}
	
	public ActionDataExportZIP(ExperimentReferenceInterface experimentReference, ThreadSafeOptions jpg, ThreadSafeOptions tsoQuality) {
		this("Create ZIP file");
		this.er = experimentReference;
		this.jpg = jpg;
		this.tsoQuality = tsoQuality;
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
		return "<html><center>"
				+ "Create ZIP file<br>"
				+ "<small><font color='gray'>(" + (jpg.getBval(0, false) ? "JPG " : "") + "Image Export)</font></small>";
	}
	
	@Override
	public String getDefaultImage() {
		return IAPimages.saveAsArchive();
	}
	
	private static WeakHashMap<String, ActionDataExportZIP> validLinks2action = new WeakHashMap<String, ActionDataExportZIP>();
	
	public static void setOutputStreamForAction(String uiid, OutputStream os) throws Exception {
		ActionDataExportZIP da = validLinks2action.get(uiid);
		if (da == null)
			throw new Exception("" +
					"Action ID is unknown, please click data export command " +
					"button again to initiate a new download.");
		da.tso.setParam(0, os);
		da.tso.setParam(2, false);
	}
	
	public static String getFileNameForAction(String uiid) throws Exception {
		ActionDataExportZIP da = validLinks2action.get(uiid);
		if (da == null)
			throw new Exception("" +
					"Action ID is unknown, please click data export command " +
					"button again to initiate a new download.");
		return (String) da.tso.getParam(1, null);
	}
	
	public static Long getExperimentSizeForAction(String uiid) throws Exception {
		ActionDataExportZIP da = validLinks2action.get(uiid);
		if (da == null)
			throw new Exception("" +
					"Action ID is unknown, please click data export command " +
					"button again to initiate a new download.");
		return (Long) da.tso.getParam(3, null);
	}
	
	public static void waitForFinishedDownloadAction(String uiid) throws Exception {
		ActionDataExportZIP da = validLinks2action.get(uiid);
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
			ExperimentReferenceInterface experimentReference = er;
			status.setCurrentStatusText1("Load Experiment");
			ExperimentInterface experiment = experimentReference.getData();
			
			String fsinfo = "";
			
			if (!SystemAnalysis.isHeadless()) {
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
			
			final ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(os));
			
			out.setLevel(0);
			out.setComment("Created with IAP V" + ReleaseInfo.IAP_VERSION_STRING + " by user " + SystemAnalysis.getUserName() + ".");
			// out.setMethod(ZipOutputStream.STORED);
			
			status.setCurrentStatusText1("Create ZIP");
			
			// filename:
			// SNAPSHOTNAME=Image Config_[GRAD]Grad
			// plantID SNAPSHOTNAME DATUM ZEIT.png
			
			GregorianCalendar gc = new GregorianCalendar();
			
			final ThreadSafeOptions written = new ThreadSafeOptions();
			final ThreadSafeOptions read = new ThreadSafeOptions();
			
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
			ThreadSafeOptions idx = new ThreadSafeOptions();
			
			long startTime = System.currentTimeMillis();
			ArrayList<LocalComputeJob> wait = new ArrayList<>();
			experiment.visitNumericMeasurements(
					null,
					(nm) -> {
						if (nm instanceof BinaryMeasurement) {
							BinaryMeasurement bm = (BinaryMeasurement) nm;
							if (bm.getURL() == null)
								return;
							
							if (status.wantsToStop())
								return;
							
							status.setCurrentStatusValueFine(100d * (idx.addInt(1)) / files);
							
							final String zefnSRC;
							final ImageData id = (ImageData) bm;
							try {
								if (bm instanceof ImageData) {
									zefnSRC = getImageFileExportNameForZIPexport(gc, id);
									
								} else {
									zefnSRC = bm.getURL().getFileName();
								}
								// bm.getURL().getFileName();
					
					wait.add(BackgroundThreadDispatcher.addTask(() -> {
						MyByteArrayInputStream inSRC;
						try {
							inSRC = ResourceIOManager.getInputStreamMemoryCached(bm.getURL());
						} catch (Exception e1) {
							errorMessage = e1.getMessage();
							inSRC = null;
						}
						if (inSRC == null)
							return;
						final SubstanceInterface suf = nm.getParentSample().getParentCondition().getParentSubstance();
						MyByteArrayInputStream in = inSRC;
						String zefn = zefnSRC;
						boolean closed = false;
						try {
							if (in.getCount() > 0) {
								read.addLong(in.getCount());
								if (jpg.getBval(0, false)) {
									String ext = bm.getURL().getFileNameExtension().toLowerCase();
									if (!ext.endsWith("jpg") && !ext.endsWith("jpeg")) {
										Image img = new Image(in);
										int ss = tsoQuality.getInt();
										CameraType ct = CameraType.fromString(suf.getName());
										if (ss > 0 && ss < 100 && ct != CameraType.NIR && ct != CameraType.IR)
											img = img.resize(img.getWidth() * ss / 100, img.getHeight() * ss / 100);
										in = img.getAsJPGstream((float) tsoQuality.getDouble());
										closed = true;
										zefn = StringManipulationTools.removeFileExtension(zefn) + ".jpg";
									}
								} else {
									Image img = new Image(in);
									int ss = tsoQuality.getInt();
									CameraType ct = CameraType.fromString(suf.getName());
									if (ss > 0 && ss < 100 && ct != CameraType.NIR && ct != CameraType.IR)
										img = img.resize(img.getWidth() * ss / 100, img.getHeight() * ss / 100);
									in = img.getAsPNGstream();
								}
								ZipArchiveEntry entry = new ZipArchiveEntry(zefn);
								entry.setSize(in.getCount());
								entry.setCrc(in.getCRC32());
								Date t;
								if (id.getParentSample().getSampleFineTimeOrRowId() != null)
									t = new Date(id.getParentSample().getSampleFineTimeOrRowId());
								else
									t = id.getParentSample().getParentCondition().getExperimentStorageDate();
								if (t == null)
									t = new Date();
								entry.setTime(t.getTime());
								synchronized (out) {
									out.putNextEntry(entry);
									int n = in.getCount();
									out.write(in.getBuff(), 0, n);
									out.closeEntry();
									written.addLong(n);
								}
							}
						} catch (IOException e) {
							System.err.println(SystemAnalysis.getCurrentTime() + ">ERROR: " + e.getMessage());
						} finally {
							// written.addInt(-1);
							if (!closed)
								try {
									in.close();
								} catch (IOException e) {
									System.err.println(SystemAnalysis.getCurrentTime() + ">ERROR: " + e.getMessage());
								}
						}
					}, "add file to ZIP"));
					
				} catch (Exception e) {
					System.out.println("ERROR: " + e.getMessage());
				}
				String pre = "Create ZIP...";
				status.setCurrentStatusText1(pre);
				long currTime = System.currentTimeMillis();
				if (read.getLong() > 0)
					status.setCurrentStatusText1("Added " + idx.getInt() + "/" + files + " files");
				status.setCurrentStatusText2("inp: " + SystemAnalysis.getDataTransferSpeedString(read.getLong(), startTime, currTime) + ", " +
						"out:<br>" + SystemAnalysis.getDataTransferSpeedString(written.getLong(), startTime, currTime) + " ("
						+ (int) (100d * written.getLong() / read.getLong()) + "% of inp)");
			}
		}	);
			BackgroundThreadDispatcher.waitFor(wait);
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
	
	public static String getImageFileExportNameForZIPexport(GregorianCalendar gc, ImageData id) {
		NumericMeasurementInterface nm = id;
		
		Date t;
		if (nm.getParentSample().getSampleFineTimeOrRowId() != null)
			t = new Date(nm.getParentSample().getSampleFineTimeOrRowId());
		else
			t = nm.getParentSample().getParentCondition().getExperimentStorageDate();
		if (t == null)
			t = new Date();
		gc.setTime(t);
		
		return (nm.getQualityAnnotation() != null ? nm.getQualityAnnotation() + " " : nm.getReplicateID() + "") +
				nm.getParentSample().getParentCondition().getParentSubstance().getName() + " " +
				nm.getParentSample().getTimeUnit() + "_" + StringManipulationTools.formatNumberAddZeroInFront(nm.getParentSample().getTime(), 2) + " " +
				(id != null ? (id.getPosition() != null ?
						StringManipulationTools.formatNumber(id.getPosition(), "000")
								+ "_deg " : "000_deg") : "") + " " +
				gc.get(GregorianCalendar.YEAR) + "-"
				+ StringManipulationTools.formatNumberAddZeroInFront((gc.get(GregorianCalendar.MONTH) + 1), 2) + "-"
				+ StringManipulationTools.formatNumberAddZeroInFront(gc.get(GregorianCalendar.DAY_OF_MONTH), 2) + " "
				+ StringManipulationTools.formatNumberAddZeroInFront(gc.get(GregorianCalendar.HOUR_OF_DAY), 2) + "_"
				+ StringManipulationTools.formatNumberAddZeroInFront(gc.get(GregorianCalendar.MINUTE), 2) + "_"
				+ StringManipulationTools.formatNumberAddZeroInFront(gc.get(GregorianCalendar.SECOND), 2) + ".png";
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
		String stop = "";
		if (status.wantsToStop())
			stop = "Cammand has been interrupted by the user. Created ZIP file may be incomplete! ";
		if (errorMessage == null)
			errorMessage = "";
		else {
			errorMessage = " " + errorMessage + "";
		}
		if (fn == null)
			return new MainPanelComponent(stop + "No output file has been generated." + errorMessage);
		else {
			if (errorMessage.trim().length() > 0)
				return new MainPanelComponent(stop + "Output incomplete. Last Error: " + errorMessage);
			else
				return new MainPanelComponent(stop + "The file " + fn + " has been created (size " + mb + " MB, " + files + " files)." + errorMessage);
		}
	}
	
	long startTime;
	File ff;
	
	@Override
	public boolean prepareCommandLineExecution() throws Exception {
		System.out.println();
		System.out.println(SystemAnalysis.getCurrentTime() + ">INFO: Command requires specification of an output file name.");
		System.out.println(SystemAnalysis.getCurrentTime() + ">INFO: If no path is specified, the file will be placed in the current directory.");
		System.out.println(SystemAnalysis.getCurrentTime() + ">READY: PLEASE ENTER FILENAME INCL. SUFFIX '.zip' (OR . TO CANCEL OPERATION):");
		String fileName = SystemAnalysis.getCommandLineInput();
		if (fileName == null || fileName.trim().isEmpty() || fileName.equals("."))
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
			System.out.println(SystemAnalysis.getCurrentTime() + ">INFO: Output to " + f.getAbsolutePath());
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
	
	@Override
	public boolean isImageAnalysisCommand() {
		return false;
	}
	
	@Override
	public void setExperimentReference(ExperimentReferenceInterface experimentReference) {
		this.er = experimentReference;
	}
}
