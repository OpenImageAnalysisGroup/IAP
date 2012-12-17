/*******************************************************************************
 * Copyright (c) 2010-2012 Image Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Nov 9, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.commands.vfs;

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
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
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.gui.webstart.HSMfolderTargetDataManager;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.postgresql.LemnaTecFTPhandler;
import de.ipk.vanted.plugin.VfsFileObject;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Experiment;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart.TextFile;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.BinaryMeasurement;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.MyImageIOhelper;

/**
 * @author klukas
 */
public class ActionDataExportToVfs extends AbstractNavigationAction {
	
	private final MongoDB m;
	private final ExperimentReference experimentReference;
	private NavigationButton src;
	// private String fn;
	private String mb;
	private int files, knownFiles, errorCount;
	private final ThreadSafeOptions tso = new ThreadSafeOptions();
	private String errorMessage;
	private boolean includeMainImages = true;
	private boolean includeReferenceImages = true;
	private boolean includeAnnotationImages = true;
	private final VirtualFileSystemVFS2 vfs;
	
	public ActionDataExportToVfs(MongoDB m,
			ExperimentReference experimentReference, VirtualFileSystemVFS2 vfs) {
		super("Copy to " + vfs.getTargetName() + " (" + vfs.getTransferProtocolName() + ")");
		this.m = m;
		this.experimentReference = experimentReference;
		this.vfs = vfs;
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
						+ "target server using the specified VfsFile transfer protocol.<br><br>", new Object[] {
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
		return "Copy to " + (vfs != null ? vfs.getTargetName() : "(undefined)");
	}
	
	@Override
	public String getDefaultImage() {
		return IAPimages.copyToServer();
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src)
			throws Exception {
		this.src = src;
		this.errorMessage = null;
		try {
			status.setCurrentStatusText1("Clone Experiment");
			final ExperimentInterface experiment = experimentReference.getData(
					m).clone();
			
			status.setCurrentStatusText1("Store Files...");
			
			experiment.setHeader(experimentReference.getHeader().clone());
			
			experiment.getHeader().setOriginDbId(
					experimentReference.getHeader().getDatabaseId());
			final ThreadSafeOptions written = new ThreadSafeOptions();
			
			this.files = determineNumberOfFilesInDataset(experiment);
			int idx = 0;
			
			knownFiles = 0;
			
			errorCount = 0;
			
			final HSMfolderTargetDataManager hsmManager = new HSMfolderTargetDataManager(
					vfs.getPrefix(), vfs.getTargetPathName());
			
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
				System.out.println("OK: VfsFile transfer of experiment "
						+ experimentReference.getExperimentName()
						+ " to " + vfs.getTargetName() + "/" + vfs.getTargetPathName() + " complete (saved " + idx
						+ " files). Saving XML... // "
						+ SystemAnalysis.getCurrentTime());
				status.setCurrentStatusText1("Finalize storage");
				status.setCurrentStatusText2("Index Created");
			} else {
				status.setCurrentStatusText1("Data Transfer Incomplete");
				status.setCurrentStatusText2("Could not save valid dataset");
				System.out.println("ERROR: VfsFile transfer of experiment "
						+ experimentReference.getExperimentName()
						+ " to " + vfs.getTargetName() + "/" + vfs.getTargetPathName() + " incomplete (" + errorCount + " errors). // "
						+ SystemAnalysis.getCurrentTime());
			}
			experiment.getHeader().setRemark(
					experiment.getHeader().getRemark()
							+ " // data transfer errors: " + errorCount);
			experiment.getHeader().setStorageTime(new Date());
			
			createIndexFiles(experiment, hsmManager, status);
			status.setCurrentStatusValueFine(100d);
			
			this.mb = (written.getLong() / 1024 / 1024) + "";
			tso.setParam(2, true);
		} catch (Exception e) {
			
			errorCount++;
			tso.setParam(2, true);
			
			// if (fn != null && fn.trim().length() > 0 && new VfsFilesystemFile(fn).exists())
			// new VfsFilesystemFile(fn).delete();
			this.errorMessage = e.getClass().getName() + ": " + e.getMessage();
		}
	}
	
	private int storeData(final ExperimentInterface experiment,
			final ThreadSafeOptions written, int idx,
			final HSMfolderTargetDataManager hsmManager, long startTime,
			ExecutorService es, final String substanceName,
			NumericMeasurementInterface nm) {
		
		// copy main binary VfsFile (url)
		if (nm instanceof BinaryMeasurement) {
			final BinaryMeasurement bm = (BinaryMeasurement) nm;
			if (bm.getURL() == null)
				return idx;
			IOurl unchangedURL = bm.getURL().copy();
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
						final VfsFileObject targetFile = vfs.newVfsFile(
								hsmManager.prepareAndGetDataFileNameAndPath(
										experiment.getHeader(), t, zefn.contains("#") ? zefn.split("#")[0] : zefn));
						boolean exists = targetFile.exists()
								&& targetFile.length() > 0;
						targetExists = exists;
						try {
							fileContent = copyBinaryFileContentToTarget(
									experiment, written, hsmManager, es,
									bm.getURL(), null, t, targetFile, exists,
									null);
						} catch (Exception e) {
							System.out
									.println("ERROR: DATA TRANSFER AND DATA STORAGE: "
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
								.println("ERROR: DATA TRANSFER AND DATA STORAGE: "
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
						final VfsFileObject targetFile = vfs.newVfsFile(
								hsmManager.prepareAndGetPreviewFileNameAndPath(
										experiment.getHeader(), t, zefn));
						boolean exists = targetFile.exists()
								&& targetFile.length() > 0;
						targetExists = exists;
						if (!exists) {
							InputStream is = null;
							
							byte[] previewData = ResourceIOManager.getPreviewImageContent(unchangedURL);
							if (previewData == null || previewData.length == 0)
								if (fileContent != null) {
									MyByteArrayInputStream bis = fileContent.get();
									if (bis != null)
										is = bis.getNewStream();
								}
							try {
								if (is == null && (previewData == null || previewData.length == 0)) {
									is = ResourceIOManager
											.getInputStreamMemoryCached(bm
													.getURL());
									if (is == null || is.available() <= 0) {
										System.out
												.println("ERROR: Input stream contains no content for image with URL "
														+ bm.getURL());
									}
								}
								
								MyByteArrayInputStream previewStream = null;
								if (previewData == null || previewData.length == 0) {
									if (is != null) {
										BufferedImage bimage = ImageIO.read(is);
										previewStream = MyImageIOhelper.getPreviewImageStream(bimage);
									}
								} else {
									previewStream = new MyByteArrayInputStream(previewData, previewData.length);
								}
								if (previewStream != null)
									copyBinaryFileContentToTarget(experiment,
											written, hsmManager, es, null,
											previewStream, t, targetFile,
											exists, null);
								else
									System.out
											.println("ERROR: Preview could not be created or saved.");
							} finally {
								if (is != null)
									is.close();
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
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
		
		// copy label binary VfsFile (label url)
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
								.startsWith(LemnaTecFTPhandler.PREFIX)) {
							String fn = bm.getLabelURL().getDetail();
							zefn = "label_"
									+ substanceName
									+ "_"
									+ fn.substring(fn.lastIndexOf("/")
											+ "/".length())
									+ getFileExtension(bm.getLabelURL()
											.getFileName());
						} else
							zefn = "label_"
									+ determineBinaryFileName(t, substanceName, nm,
											bm);
					
					final VfsFileObject targetFile = vfs.newVfsFile(
							hsmManager.prepareAndGetDataFileNameAndPath(
									experiment.getHeader(), t, zefn));
					
					copyBinaryFileContentToTarget(experiment, written,
							hsmManager, es, bm.getLabelURL(), null, t,
							targetFile, targetFile.exists(), null);
					
				} catch (Exception e) {
					System.out.println("ERROR: DATA DATA TRANSFER AND STORAGE: "
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
		
		// copy old reference label binary VfsFile (oldreference annotation)
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
								LemnaTecFTPhandler.PREFIX)) {
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
					
					final VfsFileObject targetFile = vfs.newVfsFile(
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
							targetFile.exists(), postProcess);
				} catch (Exception e) {
					System.out
							.println("ERROR: DATA DATA TRANSFER AND STORAGE OF OLDREFERENCE: "
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
		if (fileName.contains("#"))
			fileName = fileName.substring(fileName.lastIndexOf("#") + 1);
		return fileName;
	}
	
	private void createIndexFiles(final ExperimentInterface experiment,
			final HSMfolderTargetDataManager hsmManager,
			BackgroundTaskStatusProviderSupportingExternalCall optStatus) {
		try {
			long tsave = System.currentTimeMillis();
			int eidx = 0;
			LinkedHashMap<VfsFileObject, String> tempFile2fileName = new LinkedHashMap<VfsFileObject, String>();
			// for (ExperimentInterface ei : experiment.split()) {
			ExperimentInterface ei = experiment;
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
			// }
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
			final VfsFileObject targetFile, final boolean targetExists,
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
							in = url != null || optUrlContent == null ? ResourceIOManager
									.getInputStreamMemoryCached(url)
									: optUrlContent;
							if (in == null)
								System.out.println("No input for " + url);
							else {
								synchronized (es) {
									VfsFileObject f = vfs.newVfsFile(hsmManager
											.prepareAndGetDataFileNameAndPath(
													experiment.getHeader(), t,
													"in_progress_"
															+ UUID.randomUUID()
																	.toString()));
									BufferedOutputStream bos = new BufferedOutputStream(
											f.getOutputStream());
									try {
										if (in.getCount() > 0)
											bos.write(in.getBuff(), 0,
													in.getCount());
									} finally {
										bos.close();
									}
									written.addLong(in.getCount());
									in.close();
									if (t != null)
										f.setLastModified(t);
									f.setWritable(false);
									f.setExecutable(false);
									f.renameTo(targetFile, true);
								}
							}
						}
						if (url != null) {
							// System.out.println("Current URL: " + url);
							// System.out.println("Target File Name: " + targetFile.getName());
							url.setPrefix(vfs.getPrefix());
							url.setDetail("");
							String path = hsmManager
									.prepareAndGetDataFileNameAndPath(
											experiment.getHeader(), t,
											targetFile.getName().split("#", 2)[0]);
							// System.out.println("Path: " + path);
							path = path.substring(hsmManager.getPath().length() + File.separator.length());
							url.setDetail(path.substring(0, path.lastIndexOf(File.separator)));
							path = path.substring(path.lastIndexOf(File.separator) + File.separator.length());
							url.setFileName(path + "#"
									+ extractLastFileName(url.getFileName()));
						}
						if (optPostProcess != null)
							optPostProcess.run();
					} catch (Exception e) {
						e.printStackTrace();
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
	
	public static String determineBinaryFileName(long t, final String substanceName,
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
					+ id.getURL().getFileNameExtension();
			
		} else {
			zefn = bm.getURL().getFileName();
		}
		return zefn;
	}
	
	private void renameTempInProgressFilesToFinalFileNames(
			LinkedHashMap<VfsFileObject, String> tempFile2fileName) throws Exception {
		// rename all temp files
		for (VfsFileObject f : tempFile2fileName.keySet()) {
			VfsFileObject te = vfs.newVfsFile(tempFile2fileName.get(f));
			try {
				if (f != null && f.exists()) {
					f.renameTo(te, true);
					System.out.println("OK: Save XML of experiment "
							+ experimentReference.getExperimentName() + " as "
							+ te.getName() + " // "
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
			HashMap<VfsFileObject, String> tempFile2fileName, ExperimentInterface ei)
			throws Exception {
		String conditionIndexFileName = tsave + "_" + eidx + "_"
				+ ei.getHeader().getImportusername() + "_" + ei.getName()
				+ ".iap.index.csv";
		conditionIndexFileName = StringManipulationTools.stringReplace(
				conditionIndexFileName, ":", "-");
		VfsFileObject conditionFile = vfs.newVfsFile(
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
		conditionIndexFileContent.write(conditionFile.getOutputStream());
		tempFile2fileName
				.put(conditionFile,
						hsmManager
								.prepareAndGetTargetFileForConditionIndex(conditionIndexFileName));
	}
	
	private void storeIndexFile(final HSMfolderTargetDataManager hsmManager,
			long tsave, int eidx, HashMap<VfsFileObject, String> tempFile2fileName,
			ExperimentInterface ei) throws Exception {
		String indexFileName = tsave + "_" + eidx + "_"
				+ ei.getHeader().getImportusername() + "_" + ei.getName()
				+ ".iap.index.csv";
		indexFileName = StringManipulationTools.stringReplace(indexFileName,
				":", "-");
		
		VfsFileObject indexFile = vfs.newVfsFile(
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
		indexFileContent.write(indexFile.getOutputStream());
		tempFile2fileName.put(indexFile, hsmManager
				.prepareAndGetTargetFileForContentIndex(indexFileName));
	}
	
	private void storeXMLdataset(final ExperimentInterface experiment,
			final HSMfolderTargetDataManager hsmManager, long tsave, int eidx,
			LinkedHashMap<VfsFileObject, String> tempFile2fileName,
			ExperimentInterface ei,
			BackgroundTaskStatusProviderSupportingExternalCall optStatus)
			throws Exception {
		TextFile tf = new TextFile();
		tf.add(Experiment.getString(ei, optStatus));
		VfsFileObject f = vfs.newVfsFile(hsmManager.prepareAndGetDataFileNameAndPath(
				experiment.getHeader(), null, "in_progress_"
						+ UUID.randomUUID().toString()));
		tf.write(f.getOutputStream()); // to temp file
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
	
	public ExperimentReference getExperimentReference() {
		return experimentReference;
	}
	
	public MongoDB getMongoInstance() {
		return m;
	}
}
