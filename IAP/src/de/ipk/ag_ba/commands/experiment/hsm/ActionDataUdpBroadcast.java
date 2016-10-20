/*******************************************************************************
 * Copyright (c) 2011 Image Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Nov 9, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.commands.experiment.hsm;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.ErrorMsg;
import org.StringManipulationTools;
import org.SystemAnalysis;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;
import org.graffiti.plugin.io.resources.IOurl;
import org.graffiti.plugin.io.resources.MyByteArrayInputStream;
import org.graffiti.plugin.io.resources.ResourceIOManager;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.images.IAPimages;
import de.ipk.ag_ba.gui.navigation_actions.ParameterOptions;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReferenceInterface;
import de.ipk.ag_ba.gui.webstart.HSMfolderTargetDataManager;
import de.ipk.ag_ba.io_handler.hsm.HsmResourceIoHandler;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.postgresql.LTftpHandler;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Experiment;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.network.TabAglet;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart.TextFile;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.BinaryMeasurement;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.MyImageIOhelper;

/**
 * @author klukas
 */
public class ActionDataUdpBroadcast extends AbstractNavigationAction {
	private final MongoDB m;
	private final ExperimentReferenceInterface experimentReference;
	private NavigationButton src;
	private String fn;
	private String mb;
	private int files, knownFiles, errorCount;
	private final ThreadSafeOptions tso = new ThreadSafeOptions();
	private String errorMessage;
	private boolean includeMainImages = true;
	private boolean includeReferenceImages = true;
	private boolean includeAnnotationImages = true;
	
	public ActionDataUdpBroadcast(MongoDB m,
			ExperimentReferenceInterface experimentReference) {
		super("Broadcast dataset to other computers");
		this.m = m;
		this.experimentReference = experimentReference;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		return null;
	}
	
	@Override
	public ParameterOptions getParameters() {
		return new ParameterOptions(
				"<html>"
						+ "This commands copies the experiment and its connected binary data to the<br>"
						+ "all computers, reachable by UDP broadcast messages, which have IAP running<br>"
						+ "and the UDP-receive function enabled.<br><br>", new Object[] {
						"Copy images", includeMainImages,
						"Copy reference images", includeReferenceImages,
						"Copy annotation images", includeAnnotationImages });
	}
	
	@Override
	public void setParameters(Object[] parameters) {
		super.setParameters(parameters);
		if (parameters != null && parameters.length == 3) {
			int idx = 0;
			includeMainImages = (Boolean) parameters[idx++];
			includeReferenceImages = (Boolean) parameters[idx++];
			includeAnnotationImages = (Boolean) parameters[idx++];
		}
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewNavigationSet(
			ArrayList<NavigationButton> currentSet) {
		ArrayList<NavigationButton> res = new ArrayList<NavigationButton>(
				currentSet);
		// res.add(src);
		return res;
	}
	
	@Override
	public String getDefaultTitle() {
		return "Send to nearby computers";
	}
	
	@Override
	public String getDefaultImage() {
		return IAPimages.getWLAN();
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src)
			throws Exception {
		this.src = src;
		this.errorMessage = null;
		try {
			status.setCurrentStatusText1("Clone Experiment");
			final ExperimentInterface experiment = experimentReference.getData().clone();
			
			status.setCurrentStatusText1("Store Files...");
			
			experiment.setHeader(experimentReference.getHeader().clone());
			
			experiment.getHeader().setOriginDbId(
					experimentReference.getHeader().getDatabaseId());
			final ThreadSafeOptions written = new ThreadSafeOptions();
			
			this.files = determineNumberOfFilesInDataset(experiment);
			int idx = 0;
			
			knownFiles = 0;
			
			errorCount = 0;
			String hsmFolder = "";
			final HSMfolderTargetDataManager hsmManager = new HSMfolderTargetDataManager(
					"udp", hsmFolder);
			
			long startTime = System.currentTimeMillis();
			
			ExecutorService es = Executors.newFixedThreadPool(2);
			
			boolean simulate = false;
			
			for (SubstanceInterface su : experiment) {
				final String substanceName = su.getName();
				for (ConditionInterface co : su)
					for (SampleInterface sa : co) {
						for (NumericMeasurementInterface nm : sa) {
							if (simulate) {
								; // System.out.println("backup to hsm simu");
							} else
								idx = storeData(experiment, written, idx,
										hsmManager, startTime, es,
										substanceName, nm);
						}
					}
			}
			
			es.shutdown();
			es.awaitTermination(31, TimeUnit.DAYS);
			
			if (errorCount == 0) {
				status.setCurrentStatusText1("Finalize storage");
				status.setCurrentStatusText1("Write Index...");
				// save XML and header
				System.out.println("OK: File transfer of experiment "
						+ experimentReference.getExperimentName()
						+ " to HSM complete (saved " + idx
						+ " files). Saving XML... // "
						+ SystemAnalysis.getCurrentTime());
				status.setCurrentStatusText1("Finalize storage");
				status.setCurrentStatusText2("Index Created");
			} else {
				status.setCurrentStatusText1("Data Transfer Incomplete");
				status.setCurrentStatusText2("Could not save valid dataset");
				System.out.println("ERROR: File transfer of experiment "
						+ experimentReference.getExperimentName()
						+ " to HSM incomplete (" + errorCount + " errors). // "
						+ SystemAnalysis.getCurrentTime());
			}
			experiment.getHeader().setRemark(
					experiment.getHeader().getRemark()
							+ " // HSM transfer errors: " + errorCount);
			experiment.getHeader().setStorageTime(new Date());
			
			createIndexFiles(experiment, hsmManager, status);
			status.setCurrentStatusValueFine(100d);
			
			this.mb = (written.getLong() / 1024 / 1024) + "";
			tso.setParam(2, true);
		} catch (Exception e) {
			
			errorCount++;
			tso.setParam(2, true);
			
			if (fn != null && fn.trim().length() > 0 && new File(fn).exists())
				new File(fn).delete();
			this.errorMessage = e.getClass().getName() + ": " + e.getMessage();
		}
	}
	
	private int storeData(final ExperimentInterface experiment,
			final ThreadSafeOptions written, int idx,
			final HSMfolderTargetDataManager hsmManager, long startTime,
			ExecutorService es, final String substanceName,
			NumericMeasurementInterface nm) {
		
		// copy main binary file (url)
		if (nm instanceof BinaryMeasurement) {
			final BinaryMeasurement bm = (BinaryMeasurement) nm;
			if (bm.getURL() == null)
				return idx;
			boolean targetExists = false;
			Future<MyByteArrayInputStream> fileContent = null;
			final Long t = nm.getParentSample().getSampleFineTimeOrRowId();
			if (!includeMainImages) {
				targetExists = true;
				bm.setURL(null);
			} else {
				status.setCurrentStatusValueFine(100d * ((idx++) + knownFiles)
						/ files / 2d);
				{
					// store data
					String zefn = null;
					try {
						zefn = determineBinaryFileName(t, substanceName, nm, bm);
						final File targetFile = new File(
								hsmManager.prepareAndGetDataFileNameAndPath(
										experiment.getHeader(), t, zefn));
						boolean exists = false;
						targetExists = exists;
						try {
							fileContent = copyBinaryFileContentToTarget(
									experiment, written, hsmManager, es,
									bm.getURL(), null, t, targetFile, exists,
									null);
						} catch (Exception e) {
							System.out
									.println("ERROR: HSM TRANSFER AND DATA STORAGE: "
											+ e.getMessage()
											+ " // WILL RETRY IN 2 MINUTES // "
											+ SystemAnalysis
													.getCurrentTime());
							Thread.sleep(10 * 60 * 1000);
							// try 2nd time after 10 minutes
							fileContent = copyBinaryFileContentToTarget(
									experiment, written, hsmManager, es,
									bm.getURL(), null, t, targetFile, exists,
									null);
						}
						if (exists) {
							files--;
							idx--;
							knownFiles++;
						}
					} catch (Exception e) {
						System.out
								.println("ERROR: HSM TRANSFER AND DATA STORAGE: "
										+ e.getMessage()
										+ " // "
										+ zefn
										+ " // "
										+ SystemAnalysis.getCurrentTime());
						errorCount++;
					}
				}
			}
			
			if (!targetExists)
				if (nm instanceof ImageData) {
					// store preview icon
					String zefn = null;
					try {
						zefn = determineBinaryFileName(t, substanceName, nm, bm);
						final File targetFile = new File(
								hsmManager.prepareAndGetPreviewFileNameAndPath(
										experiment.getHeader(), t, zefn));
						boolean exists = false;
						targetExists = exists;
						if (!exists) {
							InputStream is = null;
							if (fileContent != null) {
								MyByteArrayInputStream bis = fileContent.get();
								if (bis != null)
									is = bis.getNewStream();
							}
							try {
								if (is == null) {
									is = ResourceIOManager
											.getInputStreamMemoryCached(bm
													.getURL());
									if (is.available() <= 0) {
										System.out
												.println("ERROR: Input stream contains no content for image with URL: "
														+ bm.getURL() + " -> " + bm.getLabelURL());
									}
								}
								BufferedImage bimage = ImageIO.read(is);
								MyByteArrayInputStream previewStream = MyImageIOhelper
										.getPreviewImageStream(bimage);
								if (previewStream != null)
									copyBinaryFileContentToTarget(experiment,
											written, hsmManager, es, null,
											previewStream, t, targetFile,
											exists, null);
								else
									System.out
											.println("ERROR: Preview could not be created or saved.");
							} finally {
								is.close();
							}
						}
					} catch (Exception e) {
						System.out.println("ERROR PREVIEW STORAGE: "
								+ e.getMessage() + " // " + zefn
								+ " // - error is ignored");
					}
				}
			String pre = "";
			status.setCurrentStatusText1(pre + "files: " + (knownFiles + files)
					+ ", copied: " + idx
					+ (knownFiles > 0 ? ", skipped: " + knownFiles + "" : ""));
			
			long currTime = System.currentTimeMillis();
			
			double speed = written.getLong() * 1000d / (currTime - startTime)
					/ 1024d / 1024d;
			status.setCurrentStatusText2((written.getLong() / 1024 / 1024)
					+ " MB, " + (int) speed + " MB/s");
		}
		
		// copy label binary file (label url)
		if (nm instanceof BinaryMeasurement) {
			final BinaryMeasurement bm = (BinaryMeasurement) nm;
			if (bm.getLabelURL() == null)
				return idx;
			
			if (!includeReferenceImages) {
				bm.setLabelURL(null);
			} else {
				long t = nm.getParentSample().getSampleFineTimeOrRowId();
				
				final String zefn;
				try {
					if (bm.getLabelURL().getPrefix().startsWith("mongo_"))
						zefn = "label_"
								+ substanceName
								+ "_"
								+ bm.getLabelURL().getDetail()
								+ getFileExtension(bm.getLabelURL()
										.getFileName());
					else
						if (bm.getLabelURL().getPrefix()
								.startsWith(LTftpHandler.PREFIX)) {
							String fn = bm.getLabelURL().getDetail();
							zefn = "label_"
									+ substanceName
									+ "_"
									+ fn.substring(fn.lastIndexOf("/")
											+ "/".length())
									+ getFileExtension(bm.getLabelURL()
											.getFileName());;
						} else
							zefn = "label_"
									+ determineBinaryFileName(t, substanceName, nm,
											bm);
					
					final File targetFile = new File(
							hsmManager.prepareAndGetDataFileNameAndPath(
									experiment.getHeader(), t, zefn));
					
					copyBinaryFileContentToTarget(experiment, written,
							hsmManager, es, bm.getLabelURL(), null, t,
							targetFile, false, null);
					
				} catch (Exception e) {
					System.out.println("ERROR: HSM DATA TRANSFER AND STORAGE: "
							+ e.getMessage() + " // "
							+ SystemAnalysis.getCurrentTime());
					e.printStackTrace();
					errorCount++;
				}
				long currTime = System.currentTimeMillis();
				
				double speed = written.getLong() * 1000d
						/ (currTime - startTime) / 1024d / 1024d;
				status.setCurrentStatusText2((written.getLong() / 1024 / 1024)
						+ " MB, " + (int) speed + " MB/s");
			}
		}
		
		// copy old reference label binary file (oldreference annotation)
		if (nm instanceof ImageData) {
			final ImageData id = (ImageData) nm;
			String oldRef = id.getAnnotationField("oldreference");
			if (oldRef == null || oldRef.isEmpty())
				return idx;
			if (!includeAnnotationImages) {
				String updatedOldReference = "";
				id.replaceAnnotationField("oldreference", updatedOldReference);
			} else {
				final IOurl oldRefUrl = new IOurl(oldRef);
				
				long t = nm.getParentSample().getSampleFineTimeOrRowId();
				
				final String zefn;
				try {
					if (oldRefUrl.getPrefix().startsWith("mongo_"))
						zefn = "label_oldreference_" + substanceName + "_"
								+ oldRefUrl.getDetail()
								+ getFileExtension(oldRefUrl.getFileName());
					else
						if (oldRefUrl.getPrefix().startsWith(
								LTftpHandler.PREFIX)) {
							String fn = oldRefUrl.getDetail();
							zefn = "label_oldreference_"
									+ substanceName
									+ "_"
									+ fn.substring(fn.lastIndexOf("/")
											+ "/".length())
									+ getFileExtension(oldRefUrl.getFileName());;
						} else
							zefn = "label_oldreference_"
									+ determineBinaryFileName(t, substanceName, nm,
											id);
					
					final File targetFile = new File(
							hsmManager.prepareAndGetDataFileNameAndPath(
									experiment.getHeader(), t, zefn));
					Runnable postProcess = new Runnable() {
						@Override
						public void run() {
							String updatedOldReference = oldRefUrl.toString();
							id.replaceAnnotationField("oldreference",
									updatedOldReference);
						}
					};
					copyBinaryFileContentToTarget(experiment, written,
							hsmManager, es, oldRefUrl, null, t, targetFile,
							false, postProcess);
				} catch (Exception e) {
					System.out
							.println("ERROR: HSM DATA TRANSFER AND STORAGE OF OLDREFERENCE: "
									+ e.getMessage()
									+ " // "
									+ SystemAnalysis.getCurrentTime());
					e.printStackTrace();
					errorCount++;
				}
				long currTime = System.currentTimeMillis();
				
				double speed = written.getLong() * 1000d
						/ (currTime - startTime) / 1024d / 1024d;
				status.setCurrentStatusText2((written.getLong() / 1024 / 1024)
						+ " MB, " + (int) speed + " MB/s");
			}
		}
		
		return idx;
	}
	
	private String getFileExtension(String fileName) {
		if (fileName.indexOf(".") > 0)
			return fileName.substring(fileName.lastIndexOf("."));
		else
			return "";
	}
	
	private String extractLastFileName(String fileName) {
		if (fileName.contains(File.separator))
			fileName = fileName.substring(fileName.lastIndexOf(File.separator));
		return fileName;
	}
	
	private void createIndexFiles(final ExperimentInterface experiment,
			final HSMfolderTargetDataManager hsmManager,
			BackgroundTaskStatusProviderSupportingExternalCall optStatus) {
		try {
			long tsave = System.currentTimeMillis();
			int eidx = 0;
			LinkedHashMap<File, String> tempFile2fileName = new LinkedHashMap<File, String>();
			for (ExperimentInterface ei : experiment.split()) {
				if (optStatus != null)
					optStatus.setCurrentStatusText1("Create XML File");
				storeXMLdataset(experiment, hsmManager, tsave, eidx,
						tempFile2fileName, ei, optStatus);
				if (optStatus != null)
					optStatus.setCurrentStatusText1("Create Condition File");
				storeConditionIndexFile(hsmManager, tsave, eidx,
						tempFile2fileName, ei);
				if (optStatus != null)
					optStatus.setCurrentStatusText1("Create Index File");
				storeIndexFile(hsmManager, tsave, eidx, tempFile2fileName, ei);
				
				eidx++;
			}
			renameTempInProgressFilesToFinalFileNames(tempFile2fileName);
		} catch (Exception err) {
			System.out.println("ERROR: Save XML of experiment "
					+ experimentReference.getExperimentName() + " failed: "
					+ err.getMessage() + " // "
					+ SystemAnalysis.getCurrentTime());
			ErrorMsg.addErrorMessage(err);
		}
	}
	
	private int determineNumberOfFilesInDataset(
			final ExperimentInterface experiment) {
		int files = 0;
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
		return files;
	}
	
	private Future<MyByteArrayInputStream> copyBinaryFileContentToTarget(
			final ExperimentInterface experiment,
			final ThreadSafeOptions written,
			final HSMfolderTargetDataManager hsmManager,
			final ExecutorService es, final IOurl url,
			final MyByteArrayInputStream optUrlContent, final Long t,
			final File targetFile, final boolean targetExists,
			final Runnable optPostProcess) throws InterruptedException {
		while (written.getInt() > 0)
			Thread.sleep(5);
		written.addInt(1);
		return es.submit(new Callable<MyByteArrayInputStream>() {
			@Override
			public MyByteArrayInputStream call() throws Exception {
				// BackgroundTaskHelper.lockAquire(hsmFolder, 1);
				MyByteArrayInputStream in = null;
				try {
					try {
						if (!targetExists) {
							in = url != null ? ResourceIOManager
									.getInputStreamMemoryCached(url)
									: optUrlContent;
							synchronized (es) {
								File f = new File(hsmManager
										.prepareAndGetDataFileNameAndPath(
												experiment.getHeader(), t,
												"in_progress_"
														+ UUID.randomUUID()
																.toString()));
								if (in.getCount() > 0)
									TabAglet.getInstance().getBroadCastTask()
											.addBinaryMessage(f.getAbsolutePath() + File.separator + f.getName(), in.getBuff(), in.getCount());
								written.addLong(in.getCount());
								in.close();
								if (t != null)
									f.setLastModified(t);
								f.setWritable(false);
								f.setExecutable(false);
								f.renameTo(targetFile);
							}
						}
						String hsmFolder = "";
						String fullPath = targetFile.getAbsolutePath();
						String subPath = fullPath.substring(hsmFolder.length());
						if (url != null) {
							url.setPrefix(HsmResourceIoHandler
									.getPrefix(hsmFolder));
							url.setDetail("");
							url.setFileName(subPath + "#"
									+ extractLastFileName(url.getFileName()));
						}
						if (optPostProcess != null)
							optPostProcess.run();
					} catch (Exception e) {
						System.out.println("ERROR: " + e.getMessage());
						errorCount++;
					}
				} finally {
					// BackgroundTaskHelper.lockRelease(hsmFolder);
					written.addInt(-1);
				}
				return in != null ? in.getNewStream() : null;
			}
		});
	}
	
	private String determineBinaryFileName(long t, final String substanceName,
			NumericMeasurementInterface nm, final BinaryMeasurement bm) {
		final String zefn;
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTimeInMillis(t);
		
		ImageData id;
		if (bm instanceof ImageData) {
			id = (ImageData) bm;
			zefn = (nm.getQualityAnnotation() != null ? nm
					.getQualityAnnotation() + " " : "")
					+ substanceName
					+ " "
					+ (id != null ? (id.getPosition() != null ? "DEG_"
							+ HSMfolderTargetDataManager.digit3(id
									.getPosition().intValue()) : "DEG_000")
							: "")
					+ " "
					+ "REPL_"
					+ HSMfolderTargetDataManager.digit3(id.getReplicateID())
					+ " "
					+ nm.getParentSample().getTimeUnit()
					+ "_"
					+ HSMfolderTargetDataManager.digit3(nm.getParentSample()
							.getTime())
					+ " "
					+ HSMfolderTargetDataManager.digit2(gc
							.get(GregorianCalendar.YEAR))
					+ "-"
					+ HSMfolderTargetDataManager.digit2((gc
							.get(GregorianCalendar.MONTH) + 1))
					+ "-"
					+ HSMfolderTargetDataManager.digit2(gc
							.get(GregorianCalendar.DAY_OF_MONTH))
					+ " "
					+ HSMfolderTargetDataManager.digit2(gc
							.get(GregorianCalendar.HOUR_OF_DAY))
					+ "_"
					+ HSMfolderTargetDataManager.digit2(gc
							.get(GregorianCalendar.MINUTE))
					+ "_"
					+ HSMfolderTargetDataManager.digit2(gc
							.get(GregorianCalendar.SECOND))
					+ " "
					+ id.getURL().getFileName();
			
		} else {
			zefn = bm.getURL().getFileName();
		}
		return zefn;
	}
	
	private void renameTempInProgressFilesToFinalFileNames(
			LinkedHashMap<File, String> tempFile2fileName) throws IOException {
		// rename all temp files
		for (File f : tempFile2fileName.keySet()) {
			File te = new File(tempFile2fileName.get(f));
			try {
				if (f != null && f.exists()) {
					f.renameTo(te);
					System.out.println("OK: Save XML of experiment "
							+ experimentReference.getExperimentName() + " as "
							+ te.getCanonicalPath() + " // "
							+ SystemAnalysis.getCurrentTime());
				}
			} catch (Exception e) {
				System.err.println("ERROR: Could not rename " + f.getName()
						+ " to " + te.getName());
			}
			
		}
	}
	
	private void storeConditionIndexFile(
			final HSMfolderTargetDataManager hsmManager, long tsave, int eidx,
			HashMap<File, String> tempFile2fileName, ExperimentInterface ei)
			throws IOException {
		String conditionIndexFileName = tsave + "_" + eidx + "_"
				+ ei.getHeader().getImportusername() + "_" + ei.getName()
				+ ".iap.index.csv";
		conditionIndexFileName = StringManipulationTools.stringReplace(
				conditionIndexFileName, ":", "-");
		File conditionFile = new File(
				hsmManager
						.prepareAndGetTargetFileForConditionIndex("in_progress_"
								+ UUID.randomUUID().toString()));
		TextFile conditionIndexFileContent = new TextFile();
		
		TreeMap<String, ArrayList<String>> conditionString2substance = new TreeMap<String, ArrayList<String>>();
		HashMap<String, ConditionInterface> conditionString2con = new HashMap<String, ConditionInterface>();
		
		for (SubstanceInterface si : ei) {
			for (ConditionInterface ci : si) {
				String cn = ci.getConditionName();
				if (!conditionString2substance.containsKey(cn))
					conditionString2substance.put(cn, new ArrayList<String>());
				conditionString2substance.get(cn).add(si.getName());
				conditionString2con.put(cn, ci);
			}
		}
		
		String experimentName = ei.getName();
		
		int conditionNumber = 0;
		LinkedHashMap<String, Object> conditionFields = new LinkedHashMap<String, Object>();
		for (String condString : conditionString2substance.keySet()) {
			ConditionInterface ci = conditionString2con.get(condString);
			conditionIndexFileContent.add(experimentName + ","
					+ conditionNumber + "," + ci.getConditionId() + ","
					+ "summary" + "," + condString);
			conditionNumber++;
		}
		conditionNumber = 0;
		for (String condString : conditionString2substance.keySet()) {
			conditionFields.clear();
			ConditionInterface ci = conditionString2con.get(condString);
			
			ci.fillAttributeMap(conditionFields);
			
			for (String key : conditionFields.keySet()) {
				conditionIndexFileContent.add(experimentName + ","
						+ conditionNumber + "," + ci.getConditionId() + ","
						+ key + "," + conditionFields.get(key));
			}
			conditionIndexFileContent.add(experimentName
					+ ","
					+ conditionNumber
					+ ","
					+ ci.getConditionId()
					+ ","
					+ "substances"
					+ ","
					+ StringManipulationTools.getStringList(
							conditionString2substance.get(condString), ";"));
			conditionNumber++;
		}
		conditionIndexFileContent.write(conditionFile);
		tempFile2fileName
				.put(conditionFile,
						hsmManager
								.prepareAndGetTargetFileForConditionIndex(conditionIndexFileName));
	}
	
	private void storeIndexFile(final HSMfolderTargetDataManager hsmManager,
			long tsave, int eidx, HashMap<File, String> tempFile2fileName,
			ExperimentInterface ei) throws IOException {
		String indexFileName = tsave + "_" + eidx + "_"
				+ ei.getHeader().getImportusername() + "_" + ei.getName()
				+ ".iap.index.csv";
		indexFileName = StringManipulationTools.stringReplace(indexFileName,
				":", "-");
		
		File indexFile = new File(
				hsmManager
						.prepareAndGetTargetFileForContentIndex("in_progress_"
								+ UUID.randomUUID().toString()));
		TextFile indexFileContent = new TextFile();
		LinkedHashMap<String, Object> header = new LinkedHashMap<String, Object>();
		ei.getHeader().fillAttributeMap(header,
				ei.getNumberOfMeasurementValues());
		String experimentName = ei.getName();
		for (String key : header.keySet()) {
			indexFileContent.add(experimentName + "," + key + ","
					+ header.get(key));
		}
		indexFileContent.write(indexFile);
		tempFile2fileName.put(indexFile, hsmManager
				.prepareAndGetTargetFileForContentIndex(indexFileName));
	}
	
	private void storeXMLdataset(final ExperimentInterface experiment,
			final HSMfolderTargetDataManager hsmManager, long tsave, int eidx,
			LinkedHashMap<File, String> tempFile2fileName,
			ExperimentInterface ei,
			BackgroundTaskStatusProviderSupportingExternalCall optStatus)
			throws IOException {
		TextFile tf = new TextFile();
		tf.add(Experiment.getString(ei, optStatus));
		File f = new File(hsmManager.prepareAndGetDataFileNameAndPath(
				experiment.getHeader(), null, "in_progress_"
						+ UUID.randomUUID().toString()));
		tf.write(f); // to temp file
		f.setExecutable(false);
		f.setWritable(false);
		if (ei.getStartDate() != null)
			f.setLastModified(ei.getStartDate().getTime());
		String xmlFileName = tsave + "_" + eidx + "_"
				+ ei.getHeader().getImportusername() + "_" + ei.getName()
				+ ".iap.vanted.bin";
		xmlFileName = StringManipulationTools.stringReplace(xmlFileName, ":",
				"-");
		tempFile2fileName.put(
				f,
				hsmManager.prepareAndGetDataFileNameAndPath(
						experiment.getHeader(), null, xmlFileName));
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		if (errorMessage == null)
			errorMessage = "";
		else {
			errorMessage = " " + errorMessage + "";
		}
		
		if (errorMessage.trim().length() > 0)
			return new MainPanelComponent("Output incomplete. Error: "
					+ errorMessage);
		else
			return new MainPanelComponent("The data has been exported (copied "
					+ mb + " MB, " + files + " files added, " + knownFiles
					+ " existing files have been skipped)." + errorMessage);
	}
	
	public ExperimentReferenceInterface getExperimentReference() {
		return experimentReference;
	}
	
	public MongoDB getMongoInstance() {
		return m;
	}
}
