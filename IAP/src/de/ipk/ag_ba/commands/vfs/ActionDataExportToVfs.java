/*******************************************************************************
 * Copyright (c) 2010-2013 Image Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Nov 9, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.commands.vfs;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.ErrorMsg;
import org.StringManipulationTools;
import org.SystemAnalysis;
import org.SystemOptions;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;
import org.graffiti.plugin.io.resources.IOurl;
import org.graffiti.plugin.io.resources.MyByteArrayInputStream;
import org.graffiti.plugin.io.resources.MyByteArrayOutputStream;
import org.graffiti.plugin.io.resources.ResourceIOManager;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.datasources.file_system.VfsFileSystemSource;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.images.IAPimages;
import de.ipk.ag_ba.gui.navigation_actions.ParameterOptions;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.gui.util.IAPservice;
import de.ipk.ag_ba.gui.webstart.HSMfolderTargetDataManager;
import de.ipk.ag_ba.image.structures.Image;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.postgresql.LTftpHandler;
import de.ipk.vanted.plugin.VfsFileObject;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Experiment;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart.TextFile;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.BinaryMeasurement;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.MeasurementNodeType;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Substance3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.MyImageIOhelper;

/**
 * @author klukas
 */
public class ActionDataExportToVfs extends AbstractNavigationAction {
	private final MongoDB m;
	private final ArrayList<ExperimentReference> experimentReferences;
	private NavigationButton src;
	private String mb;
	private int files, knownFiles, errorCount;
	private final ThreadSafeOptions tso = new ThreadSafeOptions();
	private String errorMessage;
	private final boolean skipFilesAlreadyInStorageLocation = true;
	private final boolean includeMainImages = true;
	private final boolean includeReferenceImages = true;
	private final boolean includeAnnotationImages = true;
	private final VirtualFileSystemVFS2 vfs;
	private boolean skipClone;
	private final boolean ignoreOutliers;
	public String postResult = null;
	private final VfsFileSystemSource optFileSystemSource;
	private boolean skipCreateNewDBid;
	
	public ActionDataExportToVfs(MongoDB m,
			ExperimentReference experimentReference, VirtualFileSystemVFS2 vfs, boolean ignoreOutliers, VfsFileSystemSource optFileSystemSource) {
		super("Copy to " + vfs.getTargetName() + " (" + vfs.getTransferProtocolName() + ")");
		this.m = m;
		this.ignoreOutliers = ignoreOutliers;
		this.optFileSystemSource = optFileSystemSource;
		this.experimentReferences = new ArrayList<ExperimentReference>();
		this.experimentReferences.add(experimentReference);
		this.vfs = vfs;
	}
	
	public ActionDataExportToVfs(MongoDB m,
			ArrayList<ExperimentReference> experimentReference, VirtualFileSystemVFS2 vfs, boolean ignoreOutliers, VfsFileSystemSource optFileSystemSource) {
		super("Copy to " + vfs.getTargetName() + " (" + vfs.getTransferProtocolName() + ")");
		this.m = m;
		this.ignoreOutliers = ignoreOutliers;
		this.optFileSystemSource = optFileSystemSource;
		this.experimentReferences = new ArrayList<ExperimentReference>();
		this.experimentReferences.addAll(experimentReference);
		this.vfs = vfs;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		return new ArrayList<NavigationButton>();
	}
	
	@Override
	public ParameterOptions getParameters() {
		return null; /*
						 * new ParameterOptions(
						 * "<html>"
						 * + "This commands copies the experiment and its connected binary data to the<br>"
						 * + "target server using the specified VfsFile transfer protocol.<br><br>", new Object[] {
						 * "Don't copy again, if already in storage location", skipFilesAlreadyInStorageLocation,
						 * "Copy images", includeMainImages,
						 * "Copy reference images", includeReferenceImages,
						 * "Copy annotation images", includeAnnotationImages });
						 */
	}
	
	@Override
	public void setParameters(Object[] parameters) {
		super.setParameters(parameters);
		/*
		 * if (parameters != null && parameters.length == 3) {
		 * int idx = 0;
		 * skipFilesAlreadyInStorageLocation = (Boolean) parameters[idx++];
		 * includeMainImages = (Boolean) parameters[idx++];
		 * includeReferenceImages = (Boolean) parameters[idx++];
		 * includeAnnotationImages = (Boolean) parameters[idx++];
		 * }
		 */
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
			for (ExperimentReference experimentReference : experimentReferences) {
				ExperimentInterface experiment = experimentReference.getData();
				
				if (!skipClone) {
					status.setCurrentStatusText1("Clone Experiment");
					experiment = experiment.clone();
					experiment.setHeader(experimentReference.getHeader());
					if (ignoreOutliers) {
						status.setCurrentStatusText1("Process outliers");
						IAPservice.removeOutliers(experiment);
					}
				} else {
					if (ignoreOutliers)
						throw new UnsupportedOperationException("Internal Error: Outliers can't be ignored, if cloning of experiment needs be skipped");
				}
				
				status.setCurrentStatusText1("Store Files...");
				
				if (!skipClone)
					experiment.setHeader(experimentReference.getHeader().clone());
				
				if (skipCreateNewDBid)
					if (experimentReference.getHeader().getDatabaseId() != null && !experimentReference.getHeader().getDatabaseId().isEmpty())
						experiment.getHeader().setOriginDbId(experimentReference.getHeader().getDatabaseId());
				final ThreadSafeOptions written = new ThreadSafeOptions();
				final ThreadSafeOptions skipped = new ThreadSafeOptions();
				
				this.files = determineNumberOfFilesInDataset(experiment);
				int idx = 0;
				
				knownFiles = 0;
				
				errorCount = 0;
				
				final HSMfolderTargetDataManager hsmManager = new HSMfolderTargetDataManager(
						vfs.getPrefix(), vfs.getTargetPathName());
				
				long startTime = System.currentTimeMillis();
				
				ExecutorService es = Executors.newFixedThreadPool(SystemOptions.getInstance().getInteger("VFS", "Copy save threads", 4));
				HashSet<String> skippedFiles = new HashSet<String>();
				boolean simulate = false;
				
				for (SubstanceInterface su : experiment) {
					final String substanceName = su.getName();
					for (ConditionInterface co : su)
						for (SampleInterface sa : co) {
							for (NumericMeasurementInterface nm : sa) {
								if (simulate) {
									; // System.out.println("backup to hsm simu");
								} else
									idx = storeData(experiment, written, skipped, idx,
											hsmManager, startTime, es,
											substanceName, nm, skipFilesAlreadyInStorageLocation, skippedFiles);
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
					postResult = "Saved Changes (" + SystemAnalysis.getCurrentTime() + ")";
				} else {
					status.setCurrentStatusText1("Data Transfer Incomplete");
					status.setCurrentStatusText2("Could not save valid dataset");
					System.out.println("ERROR: VfsFile transfer of experiment "
							+ experimentReference.getExperimentName()
							+ " to " + vfs.getTargetName() + "/" + vfs.getTargetPathName() + " incomplete (" + errorCount + " errors). // "
							+ SystemAnalysis.getCurrentTime());
					postResult = "Saved Incomplete (" + errorCount + " errors)<br>(" + SystemAnalysis.getCurrentTime() + ")";
				}
				if (errorCount > 0)
					experiment.getHeader().setRemark(
							(experiment.getHeader().getRemark() != null && !experiment.getHeader().getRemark().isEmpty() ?
									experiment.getHeader().getRemark() + " // " : "") + "data transfer errors: " + errorCount);
				experiment.getHeader().setStorageTime(new Date());
				long kb = (written.getLong() + skipped.getLong()) / 1024;
				experiment.getHeader().setSizekb(kb);
				int files = Substance3D.countMeasurementValues(experiment, MeasurementNodeType.binaryTypes());
				experiment.getHeader().setNumberOfFiles(files);
				
				String indexFileName = createIndexFiles(experiment, hsmManager, status, experimentReference.getExperimentName());
				experiment.getHeader().setDatabaseId(vfs.getPrefix() + ":" + indexFileName);
				if (optFileSystemSource != null)
					experimentReference.getHeader().setExperimentHeaderHelper(
							optFileSystemSource.getExperimentHeaderHelper(vfs, indexFileName, experimentReference.getHeader()));
				experiment.getHeader().setDatabaseId(vfs.getPrefix() + ":" + indexFileName);
				status.setCurrentStatusValueFine(100d);
				this.mb = ((written.getLong() + skipped.getLong()) / 1024 / 1024) + "";
				tso.setParam(2, true);
			}
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
			errorCount++;
			tso.setParam(2, true);
			
			// if (fn != null && fn.trim().length() > 0 && new VfsFilesystemFile(fn).exists())
			// new VfsFilesystemFile(fn).delete();
			this.errorMessage = e.getClass().getName() + ": " + e.getMessage();
			postResult = e.getMessage() + "<br>(" + SystemAnalysis.getCurrentTime() + ")";;
		}
	}
	
	private int storeData(final ExperimentInterface experiment,
			final ThreadSafeOptions written, ThreadSafeOptions skipped, int idx,
			final HSMfolderTargetDataManager hsmManager, long startTime,
			ExecutorService es, final String substanceName,
			NumericMeasurementInterface nm, boolean dontStoreIfAlreadyInSameStorageLocationAvailable, HashSet<String> skippedFiles) {
		
		// copy main binary VfsFile (url)
		if (nm instanceof BinaryMeasurement) {
			final BinaryMeasurement bm = (BinaryMeasurement) nm;
			if (bm.getURL() != null) {
				IOurl unchangedURL = bm.getURL().copy();
				boolean targetExists = false;
				Long ttt = nm.getParentSample().getSampleFineTimeOrRowId();
				// assign exact sample time, if available
				final Long sampleFineTime = ttt == null ? 0l : ttt;
				
				if (dontStoreIfAlreadyInSameStorageLocationAvailable && unchangedURL != null && unchangedURL.getPrefix() != null
						&& unchangedURL.getPrefix().equals(hsmManager.getPrefix())) {
					// it is already saved at this storage location (but possibly in another sub-folder)
					targetExists = true;
					idx--;
					knownFiles++;
					status.setCurrentStatusValueFine(100d * ((idx++) + knownFiles) / files);
					try {
						if (!skippedFiles.contains(unchangedURL + "")) {
							skipped.addLong(vfs.getFileLength(unchangedURL));
							skippedFiles.add(unchangedURL + "");
						}
					} catch (Exception e) {
						e.printStackTrace();
						System.out
								.println(SystemAnalysis.getCurrentTime() + ">ERROR: DATA TRANSFER AND DATA STORAGE: "
										+ e.getMessage()
										+ " // "
										+ unchangedURL
										+ " // "
										+ SystemAnalysis.getCurrentTime());
						errorCount++;
					}
				} else
					if (!includeMainImages) {
						targetExists = true;
						bm.setURL(null);
					} else {
						status.setCurrentStatusValueFine(100d * ((idx++) + knownFiles) / files);
						{
							// store data
							String zefn = null;
							try {
								zefn = determineBinaryFileName(sampleFineTime, substanceName, nm, bm);
								zefn = zefn.substring(0, zefn.lastIndexOf(".")) + "."
										+ IAPservice.getTargetFileExtension(false, zefn.substring(zefn.lastIndexOf(".") + 1));
								zefn = zefn.contains("#") ? zefn.split("#")[0] : zefn;
								final VfsFileObject targetFile = vfs.newVfsFile(
										hsmManager.prepareAndGetDataFileNameAndPath(
												experiment.getHeader(), sampleFineTime,
												zefn), true);
								boolean exists = targetFile.exists();
								long len = exists ? targetFile.length() : 0;
								if (len == 0)
									exists = false;
								if (exists)
									skipped.addLong(len);
								targetExists = exists;
								try {
									copyBinaryFileContentToTarget(
											experiment, written, hsmManager, es,
											bm.getURL(), null, sampleFineTime, targetFile, exists,
											null, false);
								} catch (Exception e) {
									System.out
											.println(SystemAnalysis.getCurrentTime() + ">ERROR: DATA TRANSFER AND DATA STORAGE: "
													+ e.getMessage()
													+ " // WILL RETRY ONCE IN 5 MINUTES // "
													+ SystemAnalysis
															.getCurrentTime());
									Thread.sleep(5 * 60 * 1000);
									// try 2nd time after 5 minutes
									copyBinaryFileContentToTarget(
											experiment, written, hsmManager, es,
											bm.getURL(), null, sampleFineTime, targetFile, exists,
											null, false);
								}
								if (exists) {
									idx--;
									knownFiles++;
								}
							} catch (Exception e) {
								System.out
										.println(SystemAnalysis.getCurrentTime() + ">ERROR: DATA TRANSFER AND DATA STORAGE: "
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
						storePreviewIcon(experiment, written, skipped, hsmManager, es, substanceName, nm, bm, unchangedURL, sampleFineTime);
					}
				String pre = "";
				status.setCurrentStatusText1(pre + "files: " + files
						+ ", copied: " + idx
						+ (knownFiles > 0 ? ", skipped: " + knownFiles + "" : ""));
				
				long currTime = System.currentTimeMillis();
				
				status.setCurrentStatusText2(SystemAnalysis.getDataTransferSpeedString(written.getLong(), startTime, currTime) +
						", skipped " + SystemAnalysis.getDataAmountString(skipped.getLong()));
				
			}
		}
		
		// copy label binary VfsFile (label url)
		if (nm instanceof BinaryMeasurement) {
			final BinaryMeasurement bm = (BinaryMeasurement) nm;
			if (bm.getLabelURL() != null)
				storeLabelData(experiment, written, skipped, hsmManager, startTime, es, substanceName, nm, dontStoreIfAlreadyInSameStorageLocationAvailable, bm,
						skippedFiles);
		}
		
		// copy old reference label binary VfsFile (oldreference annotation)
		if (nm instanceof ImageData) {
			final ImageData id = (ImageData) nm;
			String oldRef = id.getAnnotationField("oldreference");
			if (oldRef != null && !oldRef.isEmpty())
				storeReferenceData(experiment, written, skipped, hsmManager, startTime, es, substanceName, nm, dontStoreIfAlreadyInSameStorageLocationAvailable,
						id, oldRef, skippedFiles);
		}
		
		return idx;
	}
	
	private void storeReferenceData(final ExperimentInterface experiment, final ThreadSafeOptions written, ThreadSafeOptions skipped,
			final HSMfolderTargetDataManager hsmManager, long startTime, ExecutorService es, final String substanceName, NumericMeasurementInterface nm,
			boolean dontStoreIfAlreadyInSameStorageLocationAvailable, final ImageData id, String oldRef, HashSet<String> skippedFiles) {
		final IOurl oldRefUrl = new IOurl(oldRef);
		if (dontStoreIfAlreadyInSameStorageLocationAvailable && oldRefUrl != null && oldRefUrl.getPrefix() != null
				&& oldRefUrl.getPrefix().equals(hsmManager.getPrefix())) {
			// it is already saved at this storage location (but possible in another sub-folder)
			try {
				if (!skippedFiles.contains(oldRefUrl + "")) {
					skipped.addLong(vfs.getFileLength(oldRefUrl));
					skippedFiles.add(oldRefUrl + "");
				}
			} catch (Exception e) {
				e.printStackTrace();
				System.out
						.println(SystemAnalysis.getCurrentTime() + ">ERROR: DATA TRANSFER AND DATA STORAGE: "
								+ e.getMessage()
								+ " // "
								+ oldRefUrl
								+ " // "
								+ SystemAnalysis.getCurrentTime());
				errorCount++;
			}
		} else
			if (!includeAnnotationImages) {
				String updatedOldReference = "";
				id.replaceAnnotationField("oldreference", updatedOldReference);
			} else {
				
				Long t = nm.getParentSample().getSampleFineTimeOrRowId();
				if (t == null)
					if (experiment.getHeader().getStartdate() != null)
						t = experiment.getHeader().getStartdate().getTime();
					else
						t = 0l;
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
					
					final VfsFileObject targetFile = vfs.newVfsFile(
							hsmManager.prepareAndGetDataFileNameAndPath(
									experiment.getHeader(), t, zefn), true);
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
							targetFile.exists(), postProcess, false);
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
				
				status.setCurrentStatusText2(SystemAnalysis.getDataTransferSpeedString(written.getLong(), startTime, currTime) +
						", skipped " + SystemAnalysis.getDataAmountString(skipped.getLong()));
			}
	}
	
	private void storeLabelData(final ExperimentInterface experiment, final ThreadSafeOptions written, ThreadSafeOptions skipped,
			final HSMfolderTargetDataManager hsmManager, long startTime, ExecutorService es, final String substanceName, NumericMeasurementInterface nm,
			boolean dontStoreIfAlreadyInSameStorageLocationAvailable, final BinaryMeasurement bm, HashSet<String> skippedFiles) {
		if (dontStoreIfAlreadyInSameStorageLocationAvailable && bm.getLabelURL() != null && bm.getLabelURL().getPrefix() != null
				&& bm.getLabelURL().getPrefix().equals(hsmManager.getPrefix())) {
			// it is already saved at this storage location (but possible in another sub-folder)
			try {
				if (!skippedFiles.contains(bm.getLabelURL() + "")) {
					skipped.addLong(vfs.getFileLength(bm.getLabelURL()));
					skippedFiles.add(bm.getLabelURL() + "");
				}
			} catch (Exception e) {
				e.printStackTrace();
				System.out
						.println(SystemAnalysis.getCurrentTime() + ">ERROR: DATA TRANSFER AND DATA STORAGE: "
								+ e.getMessage()
								+ " // "
								+ bm.getLabelURL()
								+ " // "
								+ SystemAnalysis.getCurrentTime());
				errorCount++;
			}
		} else
			if (!includeReferenceImages) {
				bm.setLabelURL(null);
			} else {
				Long t = nm.getParentSample().getSampleFineTimeOrRowId();
				if (t == null)
					t = 0l;
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
											.getFileName());
						} else
							zefn = "label_"
									+ determineBinaryFileName(t, substanceName, nm,
											bm);
					
					final VfsFileObject targetFile = vfs.newVfsFile(
							hsmManager.prepareAndGetDataFileNameAndPath(
									experiment.getHeader(), t, zefn), true);
					boolean exists = targetFile.exists();
					long len = exists ? targetFile.length() : 0;
					if (len == 0)
						exists = false;
					if (exists)
						skipped.addLong(len);
					copyBinaryFileContentToTarget(experiment, written,
							hsmManager, es, bm.getLabelURL(), null, t,
							targetFile, targetFile.exists(), null, false);
					
				} catch (Exception e) {
					System.out.println("ERROR: DATA DATA TRANSFER AND STORAGE: "
							+ e.getMessage() + " // "
							+ SystemAnalysis.getCurrentTime());
					e.printStackTrace();
					errorCount++;
				}
				long currTime = System.currentTimeMillis();
				
				status.setCurrentStatusText2(SystemAnalysis.getDataTransferSpeedString(written.getLong(), startTime, currTime) +
						", skipped " + SystemAnalysis.getDataAmountString(skipped.getLong()));
				
			}
	}
	
	private void storePreviewIcon(final ExperimentInterface experiment, final ThreadSafeOptions written, ThreadSafeOptions skipped,
			final HSMfolderTargetDataManager hsmManager,
			ExecutorService es, final String substanceName, NumericMeasurementInterface nm, final BinaryMeasurement bm, IOurl unchangedURL, final Long t) {
		// store preview icon
		String zefn = null;
		try {
			zefn = determineBinaryFileName(t, substanceName, nm, bm);
			final VfsFileObject targetFile = vfs.newVfsFile(
					hsmManager.prepareAndGetPreviewFileNameAndPath(
							experiment.getHeader(), t, zefn), true);
			boolean exists = targetFile.exists();
			long len = exists ? targetFile.length() : 0;
			if (exists)
				skipped.addLong(len);
			if (!exists) {
				InputStream is = null;
				
				byte[] previewData = ResourceIOManager.getPreviewImageContent(unchangedURL);
				try {
					if (is == null && (previewData == null || previewData.length == 0)) {
						is = ResourceIOManager
								.getInputStreamMemoryCached(bm
										.getURL());
						if (is == null || is.available() <= 0) {
							System.out
									.println(SystemAnalysis.getCurrentTime() + ">ERROR: Input stream contains no content for image with URL "
											+ bm.getURL());
						}
					}
					
					MyByteArrayInputStream previewStream = null;
					if (previewData == null || previewData.length == 0) {
						if (is != null) {
							try {
								BufferedImage bimage = ImageIO.read(is);
								previewStream = MyImageIOhelper.getPreviewImageStream(bimage);
							} catch (Exception error) {
								System.out
										.println(SystemAnalysis.getCurrentTime() + ">ERROR: Input stream could not be loaded as an image for image with URL "
												+ bm.getURL());
							}
						}
					} else {
						previewStream = new MyByteArrayInputStream(previewData, previewData.length);
					}
					if (previewStream != null)
						copyBinaryFileContentToTarget(experiment,
								written, hsmManager, es, null,
								previewStream, t, targetFile,
								exists, null, true);
					else
						System.out
								.println(SystemAnalysis.getCurrentTime() + ">ERROR: Preview could not be created or saved.");
				} finally {
					if (is != null)
						is.close();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(SystemAnalysis.getCurrentTime() + ">ERROR PREVIEW STORAGE: "
					+ e.getMessage() + " // " + zefn
					+ " // - error is ignored");
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
		if (fileName.contains("#"))
			fileName = fileName.substring(fileName.lastIndexOf("#") + 1);
		return fileName;
	}
	
	private String createIndexFiles(final ExperimentInterface experiment,
			final HSMfolderTargetDataManager hsmManager,
			BackgroundTaskStatusProviderSupportingExternalCall optStatus, String experimentSourceName) {
		try {
			long tsave = System.currentTimeMillis();
			if (skipCreateNewDBid) {
				try {
					String fn = experiment.getHeader().getExperimentHeaderHelper().getFileName();
					tsave = Long.parseLong(fn.substring(0, fn.indexOf("_")));
				} catch (Exception e) {
					System.out.println(SystemAnalysis.getCurrentTime() + ">WARNING: Could not in-place-update data structures. " +
							"Experiment actions may not use fully update data set information.");
				}
			}
			int eidx = 0;
			LinkedHashMap<VfsFileObject, String> tempFile2fileName = new LinkedHashMap<VfsFileObject, String>();
			
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
			String resName = storeIndexFile(hsmManager, tsave, eidx, tempFile2fileName, ei);
			
			renameTempInProgressFilesToFinalFileNames(tempFile2fileName, experimentSourceName);
			
			eidx++;
			return resName;
		} catch (Exception err) {
			System.out.println("ERROR: Save XML of experiment "
					+ experimentSourceName + " failed: "
					+ err.getMessage() + " // "
					+ SystemAnalysis.getCurrentTime());
			ErrorMsg.addErrorMessage(err);
			return null;
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
	
	@SuppressWarnings("resource")
	private void copyBinaryFileContentToTarget(
			final ExperimentInterface experiment,
			final ThreadSafeOptions written,
			final HSMfolderTargetDataManager hsmManager,
			final ExecutorService es, final IOurl url,
			final MyByteArrayInputStream optUrlContent, final Long t,
			final VfsFileObject targetFile, final boolean targetExists,
			final Runnable optPostProcess,
			final boolean isIconStorage) throws InterruptedException {
		while (written.getInt() > 0)
			Thread.sleep(5);
		written.addInt(1);
		
		InputStream in = null;
		try {
			try {
				if (!targetExists) {
					in = url != null || optUrlContent == null ? url.getInputStream()
							: optUrlContent;
					if (in == null)
						System.out.println("No input for " + url);
					else {
						String sourceFileExtension = url != null ? url.getFileNameExtension() : null;
						if (sourceFileExtension != null && sourceFileExtension.startsWith("."))
							sourceFileExtension = sourceFileExtension.substring(".".length());
						String targetFileExtension = IAPservice.getTargetFileExtension(isIconStorage, sourceFileExtension);
						if (url != null && sourceFileExtension != null && targetFileExtension != null && !sourceFileExtension.equals(targetFileExtension)) {
							// convert from PNG to JPG, if needed
							BufferedImage img = new Image(url).getAsBufferedImage();
							MyByteArrayOutputStream outNewFormat = new MyByteArrayOutputStream();
							ImageIO.write(img, targetFileExtension.toUpperCase(), outNewFormat);
							in = new MyByteArrayInputStream(outNewFormat.getBuffTrimmed());
						}
						synchronized (es) {
							String fn;
							if (isIconStorage)
								fn = hsmManager.prepareAndGetPreviewFileNameAndPath(
										experiment.getHeader(), t, "in_progress_" + UUID.randomUUID().toString());
							else
								fn = hsmManager.prepareAndGetDataFileNameAndPath(
										experiment.getHeader(), t, "in_progress_" + UUID.randomUUID().toString());
							
							VfsFileObject f = vfs.newVfsFile(fn, true);
							written.addLong(ResourceIOManager.copyContent(
									new BufferedInputStream(in),
									new BufferedOutputStream(f.getOutputStream()),
									-1));
							if (t != null)
								f.setLastModified(t);
							// f.setWritable(false);
							// f.setExecutable(false);
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
			written.addInt(-1);
		}
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
	
	private boolean renameTempInProgressFilesToFinalFileNames(
			LinkedHashMap<VfsFileObject, String> tempFile2fileName, String experimentSourceName) throws Exception {
		
		boolean allOK = true;
		// rename all temp files
		for (VfsFileObject f : tempFile2fileName.keySet()) {
			VfsFileObject te = vfs.newVfsFile(tempFile2fileName.get(f), true);
			try {
				if (f != null && f.exists()) {
					f.renameTo(te, true);
					System.out.println("OK: Save XML of experiment "
							+ experimentSourceName + " as "
							+ te.getName() + " // "
							+ SystemAnalysis.getCurrentTime());
				}
			} catch (Exception e) {
				allOK = false;
				System.err.println("ERROR: Could not rename " + f.getName()
						+ " to " + te.getName());
			}
		}
		return allOK;
	}
	
	private void storeConditionIndexFile(
			final HSMfolderTargetDataManager hsmManager, long tsave, int eidx,
			HashMap<VfsFileObject, String> tempFile2fileName, ExperimentInterface ei)
			throws Exception {
		String conditionIndexFileName =
				
				tsave + "_" + eidx + "_"
						+ HSMfolderTargetDataManager.filterBadChars(ei.getHeader().getImportusername(), true) + "_"
						+ HSMfolderTargetDataManager.filterBadChars(ei.getName(), true)
						+ ".iap.index.csv";
		
		VfsFileObject conditionFile = vfs.newVfsFile(
				hsmManager
						.prepareAndGetTargetFileForConditionIndex("in_progress_"
								+ UUID.randomUUID().toString()), true);
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
	
	private String storeIndexFile(final HSMfolderTargetDataManager hsmManager,
			long tsave, int eidx, HashMap<VfsFileObject, String> tempFile2fileName,
			ExperimentInterface ei) throws Exception {
		String indexFileName =
				
				tsave + "_" + eidx + "_"
						+ HSMfolderTargetDataManager.filterBadChars(ei.getHeader().getImportusername(), true) + "_" +
						HSMfolderTargetDataManager.filterBadChars(ei.getName(), true)
						+ ".iap.index.csv";
		indexFileName = StringManipulationTools.stringReplace(indexFileName,
				":", "-");
		
		VfsFileObject indexFile = vfs.newVfsFile(
				hsmManager
						.prepareAndGetTargetFileForContentIndex("in_progress_"
								+ UUID.randomUUID().toString()), true);
		TextFile indexFileContent = new TextFile();
		LinkedHashMap<String, Object> header = new LinkedHashMap<String, Object>();
		ei.getHeader().fillAttributeMap(header,
				ei.getNumberOfMeasurementValues());
		String experimentName = ei.getName();
		for (String key : header.keySet()) {
			indexFileContent.add(HSMfolderTargetDataManager.filterBadChars(experimentName, true) + "," + key + ","
					+ header.get(key));
		}
		OutputStream os = indexFile.getOutputStream();
		indexFileContent.write(os);
		String resName = hsmManager.prepareAndGetTargetFileForContentIndex(indexFileName);
		tempFile2fileName.put(indexFile, resName);
		if (resName.startsWith(vfs.getTargetPathName()))
			resName = resName.substring(vfs.getTargetPathName().length() + "/".length());
		return resName;
	}
	
	private void storeXMLdataset(final ExperimentInterface experiment,
			final HSMfolderTargetDataManager hsmManager, long tsave, int eidx,
			LinkedHashMap<VfsFileObject, String> tempFile2fileName,
			ExperimentInterface ei,
			BackgroundTaskStatusProviderSupportingExternalCall optStatus)
			throws Exception {
		VfsFileObject f = vfs.newVfsFile(hsmManager.prepareAndGetDataFileNameAndPath(
				experiment.getHeader(), null, "in_progress_"
						+ UUID.randomUUID().toString()), true);
		System.out.println(SystemAnalysis.getCurrentTime() + ">INFO: Dumping data structure into XML file. Prepare: perform GC. Memory Status: "
				+ SystemAnalysis.getUsedMemoryInMB() + " MB of RAM used)");
		System.gc();
		System.out.println(SystemAnalysis.getCurrentTime() + ">INFO: Begin dumping data structure into XML file... (Memory Status: "
				+ SystemAnalysis.getUsedMemoryInMB() + " MB of RAM used)");
		Experiment.write(ei, optStatus, f.getOutputStream()); // to temp file
		System.out.println(SystemAnalysis.getCurrentTime() + ">INFO: Finished dumping data structure into XML file. (Memory Status: "
				+ SystemAnalysis.getUsedMemoryInMB() + " MB of RAM used)");
		// f.setExecutable(false);
		// f.setWritable(false);
		if (ei.getStartDate() != null)
			f.setLastModified(ei.getStartDate().getTime());
		String xmlFileName = tsave + "_" + eidx + "_"
				+ HSMfolderTargetDataManager.filterBadChars(ei.getHeader().getImportusername(), true) + "_"
				+ HSMfolderTargetDataManager.filterBadChars(ei.getName(), true)
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
	
	public MongoDB getMongoInstance() {
		return m;
	}
	
	public void setSkipClone(boolean skipClone) {
		this.skipClone = skipClone;
	}
	
	public void setSkipUpdateDBid(boolean skipCreateNewDBid) {
		this.skipCreateNewDBid = skipCreateNewDBid;
	}
}
