/*******************************************************************************
 * Copyright (c) 2011 Image Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Nov 9, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.gui.actions.hsm;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
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

import org.ErrorMsg;
import org.StringManipulationTools;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;
import org.graffiti.plugin.io.resources.IOurl;
import org.graffiti.plugin.io.resources.MyByteArrayInputStream;
import org.graffiti.plugin.io.resources.ResourceIOManager;

import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.actions.AbstractNavigationAction;
import de.ipk.ag_ba.gui.images.IAPimages;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.gui.webstart.HSMfolderTargetDataManager;
import de.ipk.ag_ba.hsm.HsmResourceIoHandler;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.postgresql.LemnaTecFTPhandler;
import de.ipk.ag_ba.server.task_management.SystemAnalysisExt;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Experiment;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart.TextFile;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.BinaryMeasurement;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.MyImageIOhelper;

/**
 * @author klukas
 */
public class ActionDataExportToHsmFolder extends AbstractNavigationAction {
	
	private final MongoDB m;
	private final ExperimentReference experimentReference;
	private NavigationButton src;
	private String fn;
	private String mb;
	private int files, knownFiles, errorCount;
	private final ThreadSafeOptions tso = new ThreadSafeOptions();
	private String errorMessage;
	private final String hsmFolder;
	
	public ActionDataExportToHsmFolder(MongoDB m, ExperimentReference experimentReference, String hsmFolder) {
		super("Save in HSM Archive (" + hsmFolder + ")");
		this.m = m;
		this.experimentReference = experimentReference;
		this.hsmFolder = hsmFolder;
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
		return "Save in HSM Archive";
	}
	
	@Override
	public String getDefaultImage() {
		return IAPimages.saveToHsmArchive();
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		this.src = src;
		this.errorMessage = null;
		try {
			status.setCurrentStatusText1("Load Experiment");
			final ExperimentInterface experiment = experimentReference.getData(m).clone();
			
			status.setCurrentStatusText1("Store Files...");
			
			experiment.setHeader(experimentReference.getHeader().clone());
			
			final ThreadSafeOptions written = new ThreadSafeOptions();
			
			this.files = determineNumberOfFilesInDataset(experiment);
			int idx = 0;
			
			knownFiles = 0;
			
			errorCount = 0;
			
			final HSMfolderTargetDataManager hsmManager = new HSMfolderTargetDataManager(hsmFolder);
			
			long startTime = System.currentTimeMillis();
			
			ExecutorService es = Executors.newFixedThreadPool(2);
			
			for (SubstanceInterface su : experiment) {
				final String substanceName = su.getName();
				for (ConditionInterface co : su)
					for (SampleInterface sa : co) {
						for (NumericMeasurementInterface nm : sa) {
							
							// copy main binary file (url)
							if (nm instanceof BinaryMeasurement) {
								final BinaryMeasurement bm = (BinaryMeasurement) nm;
								if (bm.getURL() == null)
									continue;
								
								status.setCurrentStatusValueFine(100d * (idx++) / files);
								
								final Long t = nm.getParentSample().getRowId();
								Future<MyByteArrayInputStream> fileContent = null;
								{
									// store data
									String zefn = null;
									try {
										zefn = determineBinaryFileName(t, substanceName, nm, bm);
										final File targetFile = new File(hsmManager.prepareAndGetDataFileNameAndPath(experiment.getHeader(), t, zefn));
										boolean exists = targetFile.exists();
										fileContent = copyBinaryFileContentToTarget(experiment, written, hsmManager, es, bm.getURL(), null, t, targetFile, exists);
										if (exists) {
											files--;
											idx--;
											knownFiles++;
										}
									} catch (Exception e) {
										System.out.println("ERROR DATA STORAGE: " + e.getMessage() + " // " + zefn);
										errorCount++;
									}
								}
								if (nm instanceof ImageData) {
									// store preview icon
									String zefn = null;
									try {
										zefn = determineBinaryFileName(t, substanceName, nm, bm);
										final File targetFile = new File(hsmManager.prepareAndGetPreviewFileNameAndPath(experiment.getHeader(), t, zefn));
										boolean exists = targetFile.exists();
										if (!exists) {
											InputStream is = null;
											if (fileContent != null) {
												MyByteArrayInputStream bis = fileContent.get();
												if (bis != null)
													is = bis.getNewStream();
											}
											if (is == null)
												is = bm.getURL().getInputStream();
											MyByteArrayInputStream previewStream = MyImageIOhelper.getPreviewImageStream(ImageIO.read(is));
											copyBinaryFileContentToTarget(experiment, written, hsmManager, es, null, previewStream, t, targetFile, exists);
										}
									} catch (Exception e) {
										System.out.println("ERROR PREVIEW STORAGE: " + e.getMessage() + " // " + zefn + " // - error is ignored");
									}
								}
								String pre = "";
								status.setCurrentStatusText1(pre + "files: " + (knownFiles + files)
										+ ", copied: " + idx
										+ (knownFiles > 0 ? ", skipped: " + knownFiles + "" : ""));
								
								long currTime = System.currentTimeMillis();
								
								double speed = written.getLong() * 1000 / (currTime - startTime) / 1024 / 1024;
								status.setCurrentStatusText2((written.getLong() / 1024 / 1024) + " MB, " + (int) speed + " MB/s");
							}
							
							// copy label binary file (label url)
							if (nm instanceof BinaryMeasurement) {
								final BinaryMeasurement bm = (BinaryMeasurement) nm;
								if (bm.getLabelURL() == null)
									continue;
								
								long t = nm.getParentSample().getRowId();
								
								final String zefn;
								try {
									if (bm.getLabelURL().getPrefix().startsWith("mongo_"))
										zefn = "label_" + substanceName + "_" + bm.getLabelURL().getDetail() + getFileExtension(bm.getLabelURL().getFileName());
									else
										if (bm.getLabelURL().getPrefix().startsWith(LemnaTecFTPhandler.PREFIX)) {
											String fn = bm.getLabelURL().getDetail();
											zefn = "label_" + substanceName + "_" + fn.substring(fn.lastIndexOf("/") + "/".length())
													+ getFileExtension(bm.getLabelURL().getFileName());;
										} else
											zefn = "label_" + determineBinaryFileName(t, substanceName, nm, bm);
									
									final File targetFile = new File(hsmManager.prepareAndGetDataFileNameAndPath(experiment.getHeader(), t, zefn));
									
									copyBinaryFileContentToTarget(experiment, written, hsmManager, es, bm.getLabelURL(), null, t, targetFile, targetFile.exists());
									
								} catch (Exception e) {
									System.out.println("ERROR: " + e.getMessage());
									e.printStackTrace();
									errorCount++;
								}
								long currTime = System.currentTimeMillis();
								
								double speed = written.getLong() * 1000 / (currTime - startTime) / 1024 / 1024;
								status.setCurrentStatusText2((written.getLong() / 1024 / 1024) + " MB, " + (int) speed + " MB/s");
							}
							
						}
					}
			}
			
			es.shutdown();
			es.awaitTermination(1, TimeUnit.DAYS);
			
			if (errorCount == 0) {
				status.setCurrentStatusText1("Finalize storage");
				status.setCurrentStatusText1("Write Index...");
				// save XML and header
				System.out.println("OK: File transfer of experiment " + experimentReference.getExperimentName() + " to HSM complete (saved " + idx
						+ " files). Saving XML... // " + SystemAnalysisExt.getCurrentTime());
				createIndexFiles(experiment, hsmManager);
				status.setCurrentStatusText1("Finalize storage");
				status.setCurrentStatusText1("Index Created");
			} else {
				status.setCurrentStatusText1("Data Transfer Incomplete");
				status.setCurrentStatusText1("Could not save valid dataset");
				System.out.println("ERROR: File transfer of experiment " + experimentReference.getExperimentName() + " to HSM incomplete (" + errorCount
						+ " errors). // " + SystemAnalysisExt.getCurrentTime());
			}
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
	
	private void createIndexFiles(final ExperimentInterface experiment, final HSMfolderTargetDataManager hsmManager) {
		try {
			long tsave = System.currentTimeMillis();
			int eidx = 0;
			LinkedHashMap<File, String> tempFile2fileName = new LinkedHashMap<File, String>();
			for (ExperimentInterface ei : experiment.split()) {
				storeXMLdataset(experiment, hsmManager, tsave, eidx, tempFile2fileName, ei);
				storeConditionIndexFile(hsmManager, tsave, eidx, tempFile2fileName, ei);
				storeIndexFile(hsmManager, tsave, eidx, tempFile2fileName, ei);
				
				eidx++;
			}
			renameTempInProgressFilesToFinalFileNames(tempFile2fileName);
		} catch (Exception err) {
			System.out.println("ERROR: Save XML of experiment " + experimentReference.getExperimentName() + " failed: " + err.getMessage() + " // "
					+ SystemAnalysisExt.getCurrentTime());
			ErrorMsg.addErrorMessage(err);
		}
	}
	
	private int determineNumberOfFilesInDataset(final ExperimentInterface experiment) {
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
	
	private Future<MyByteArrayInputStream> copyBinaryFileContentToTarget(final ExperimentInterface experiment, final ThreadSafeOptions written,
			final HSMfolderTargetDataManager hsmManager, ExecutorService es, final IOurl url, final MyByteArrayInputStream optUrlContent, final Long t,
			final File targetFile, final boolean targetExists)
			throws InterruptedException {
		while (written.getInt() > 0)
			Thread.sleep(5);
		written.addInt(1);
		return es.submit(new Callable<MyByteArrayInputStream>() {
			@Override
			public MyByteArrayInputStream call() throws Exception {
				BackgroundTaskHelper.lockAquire(hsmFolder, 1);
				MyByteArrayInputStream in = null;
				try {
					try {
						if (!targetExists) {
							in = url != null ? ResourceIOManager.getInputStreamMemoryCached(url) : optUrlContent;
							File f = new File(hsmManager.prepareAndGetDataFileNameAndPath(experiment.getHeader(), t, "in_progress_"
										+ UUID.randomUUID().toString()));
							BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(f));
							if (in.getCount() > 0)
								bos.write(in.getBuff(), 0, in.getCount());
							bos.close();
							written.addLong(in.getCount());
							in.close();
							if (t != null)
								f.setLastModified(t);
							f.setWritable(false);
							f.setExecutable(false);
							f.renameTo(targetFile);
						}
						String fullPath = targetFile.getAbsolutePath();
						String subPath = fullPath.substring(hsmFolder.length());
						if (url != null) {
							url.setPrefix(HsmResourceIoHandler.getPrefix(hsmFolder));
							url.setDetail("");
							url.setFileName(subPath + "#" + extractLastFileName(url.getFileName()));
						}
					} catch (Exception e) {
						System.out.println("ERROR: " + e.getMessage());
						errorCount++;
					}
				} finally {
					BackgroundTaskHelper.lockRelease(hsmFolder);
					written.addInt(-1);
				}
				return in != null ? in.getNewStream() : null;
			}
		});
	}
	
	private String determineBinaryFileName(long t, final String substanceName, NumericMeasurementInterface nm, final BinaryMeasurement bm) {
		final String zefn;
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTimeInMillis(t);
		
		ImageData id;
		if (bm instanceof ImageData) {
			id = (ImageData) bm;
			zefn =
					(nm.getQualityAnnotation() != null ? nm.getQualityAnnotation() + " " : "")
							+
							substanceName
							+ " "
							+
							(id != null ? (id.getPosition() != null ? "DEG_" + HSMfolderTargetDataManager.digit3(id.getPosition().intValue())
									: "DEG_000") : "") + " " +
							"REPL_" + HSMfolderTargetDataManager.digit3(id.getReplicateID()) +
							" " +
							nm.getParentSample().getTimeUnit() + "_" + HSMfolderTargetDataManager.digit3(nm.getParentSample().getTime()) + " " +
							HSMfolderTargetDataManager.digit2(gc.get(GregorianCalendar.YEAR)) + "-" +
							HSMfolderTargetDataManager.digit2((gc.get(GregorianCalendar.MONTH) + 1)) + "-" +
							HSMfolderTargetDataManager.digit2(gc.get(GregorianCalendar.DAY_OF_MONTH)) + " " +
							HSMfolderTargetDataManager.digit2(gc.get(GregorianCalendar.HOUR_OF_DAY)) + "_" +
							HSMfolderTargetDataManager.digit2(gc.get(GregorianCalendar.MINUTE)) + "_" +
							HSMfolderTargetDataManager.digit2(gc.get(GregorianCalendar.SECOND)) + " " +
							id.getURL().getFileName();
			
		} else {
			zefn = bm.getURL().getFileName();
		}
		return zefn;
	}
	
	private void renameTempInProgressFilesToFinalFileNames(LinkedHashMap<File, String> tempFile2fileName) throws IOException {
		// rename all temp files
		for (File f : tempFile2fileName.keySet()) {
			File te = new File(tempFile2fileName.get(f));
			f.renameTo(te);
			System.out.println("OK: Save XML of experiment " + experimentReference.getExperimentName() + " as " + te.getCanonicalPath() + " // "
					+ SystemAnalysisExt.getCurrentTime());
		}
	}
	
	private void storeConditionIndexFile(final HSMfolderTargetDataManager hsmManager, long tsave, int eidx, HashMap<File, String> tempFile2fileName,
			ExperimentInterface ei) throws IOException {
		String conditionIndexFileName = tsave + "_" + eidx + "_" + ei.getHeader().getImportusername() + "_" + ei.getName() + ".iap.index.csv";
		
		File conditionFile = new File(hsmManager.prepareAndGetTargetFileForConditionIndex("in_progress_"
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
			conditionIndexFileContent.add(experimentName + "," + conditionNumber + "," + ci.getConditionId() + "," + "summary" + ","
					+ condString);
			conditionNumber++;
		}
		conditionNumber = 0;
		for (String condString : conditionString2substance.keySet()) {
			conditionFields.clear();
			ConditionInterface ci = conditionString2con.get(condString);
			
			ci.fillAttributeMap(conditionFields);
			
			for (String key : conditionFields.keySet()) {
				conditionIndexFileContent.add(experimentName + "," + conditionNumber + "," + ci.getConditionId() + "," + key + "," + conditionFields.get(key));
			}
			conditionIndexFileContent.add(experimentName + "," + conditionNumber + "," + ci.getConditionId() + "," + "substances" + ","
						+ StringManipulationTools.getStringList(conditionString2substance.get(condString), ";"));
			conditionNumber++;
		}
		conditionIndexFileContent.write(conditionFile);
		tempFile2fileName.put(conditionFile, hsmManager.prepareAndGetTargetFileForConditionIndex(conditionIndexFileName));
	}
	
	private void storeIndexFile(final HSMfolderTargetDataManager hsmManager, long tsave, int eidx, HashMap<File, String> tempFile2fileName,
			ExperimentInterface ei) throws IOException {
		String indexFileName = tsave + "_" + eidx + "_" + ei.getHeader().getImportusername() + "_" + ei.getName() + ".iap.index.csv";
		
		File indexFile = new File(hsmManager.prepareAndGetTargetFileForContentIndex("in_progress_"
					+ UUID.randomUUID().toString()));
		TextFile indexFileContent = new TextFile();
		LinkedHashMap<String, Object> header = new LinkedHashMap<String, Object>();
		ei.getHeader().fillAttributeMap(header, ei.getNumberOfMeasurementValues());
		String experimentName = ei.getName();
		for (String key : header.keySet()) {
			indexFileContent.add(experimentName + "," + key + "," + header.get(key));
		}
		indexFileContent.write(indexFile);
		tempFile2fileName.put(indexFile, hsmManager.prepareAndGetTargetFileForContentIndex(indexFileName));
	}
	
	private void storeXMLdataset(final ExperimentInterface experiment, final HSMfolderTargetDataManager hsmManager, long tsave, int eidx,
			LinkedHashMap<File, String> tempFile2fileName, ExperimentInterface ei)
			throws IOException {
		TextFile tf = new TextFile();
		tf.add(Experiment.getString(ei));
		File f = new File(hsmManager.prepareAndGetDataFileNameAndPath(experiment.getHeader(), null, "in_progress_"
					+ UUID.randomUUID().toString()));
		tf.write(f); // to temp file
		f.setExecutable(false);
		f.setWritable(false);
		if (ei.getStartDate() != null)
			f.setLastModified(ei.getStartDate().getTime());
		String xmlFileName = tsave + "_" + eidx + "_" + ei.getHeader().getImportusername() + "_" + ei.getName() + ".iap.vanted.bin";
		tempFile2fileName.put(f, hsmManager.prepareAndGetDataFileNameAndPath(experiment.getHeader(), null, xmlFileName));
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		if (errorMessage == null)
			errorMessage = "";
		else {
			errorMessage = " " + errorMessage + "";
		}
		
		if (errorMessage.trim().length() > 0)
			return new MainPanelComponent("Output incomplete. Error: " + errorMessage);
		else
			return new MainPanelComponent("The data has been exported (copied " + mb + " MB, " + files + " files added, " + knownFiles
						+ " existing files have been skipped)." + errorMessage);
	}
	
	public ExperimentReference getExperimentReference() {
		return experimentReference;
	}
	
	public MongoDB getMongoInstance() {
		return m;
	}
}
