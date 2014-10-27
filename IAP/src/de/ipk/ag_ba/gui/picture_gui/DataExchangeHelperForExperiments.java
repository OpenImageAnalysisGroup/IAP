/*
 * Copyright (c) 2003 IPK Gatersleben
 * $Id: DataExchangeHelperForExperiments.java,v 1.6 2010-11-05 09:46:50 klukas
 * Exp $
 */
package de.ipk.ag_ba.gui.picture_gui;

import info.clearthought.layout.TableLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.ErrorMsg;
import org.MeasurementFilter;
import org.StringManipulationTools;
import org.graffiti.editor.MainFrame;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;
import org.graffiti.plugin.io.resources.FileSystemHandler;
import org.graffiti.plugin.io.resources.IOurl;
import org.graffiti.plugin.io.resources.ResourceIOManager;

import com.mongodb.DB;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;

import de.ipk.ag_ba.commands.experiment.process.report.ActionPdfCreation3;
import de.ipk.ag_ba.commands.experiment.process.report.MySnapshotFilter;
import de.ipk.ag_ba.gui.images.IAPimages;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.mongo.DataStorageType;
import de.ipk.ag_ba.mongo.DatabaseStorageResult;
import de.ipk.ag_ba.mongo.DatabaseStorageResultWithURL;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.mongo.MongoResourceIOConfigObject;
import de.ipk.ag_ba.mongo.RunnableOnDB;
import de.ipk_gatersleben.ag_nw.graffiti.FileHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Experiment;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.MappingDataEntity;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProviderSupportingExternalCallImpl;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.BinaryMeasurement;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Condition3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.MappingData3DPath;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.MeasurementNodeType;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Sample3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Substance3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.volumes.VolumeData;

/**
 * @author Klukas
 */
public class DataExchangeHelperForExperiments {
	
	public static int getSizeOfExperiment(ExperimentReference m) {
		return -1;
	}
	
	public static void downloadFile(MongoDB m, final String hash,
			final File targetFile, final DataSetFileButton button,
			final MongoCollection collection) {
		try {
			m.processDB(new RunnableOnDB() {
				
				private DB db;
				
				@Override
				public void run() {
					try {
						// Blob b = CallDBE2WebService.getBlob(user, pass,
						// imageResult.getMd5());
						
						GridFS gridfs_images = new GridFS(db, collection
								.toString());
						System.out.println("Look for " + collection.toString()
								+ "-HASH: " + hash);
						GridFSDBFile fff = gridfs_images.findOne(hash);
						if (fff == null)
							System.out.println("NOT FOUND");
						if (fff != null) {
							System.out.println("FOUND, LENGTH="
									+ fff.getLength());
							InputStream bis = fff.getInputStream();
							
							FileOutputStream fos = new FileOutputStream(
									targetFile);
							int readBytes = 0;
							int pos = 0;
							long len = fff.getLength();
							byte[] readBuff = new byte[1024 * 1024];
							button.progress.setMaximum(100);
							while (0 < (readBytes = bis.read(readBuff))) {
								fos.write(readBuff, 0, readBytes);
								pos += readBytes;
								button.progress.setValue((int) ((double) pos
										/ len * 100d));
							}
							bis.close();
							fos.close();
							System.out.println("Created "
									+ targetFile.getAbsolutePath() + " ("
									+ targetFile.length() + " bytes, read "
									+ pos + ")");
						}
					} catch (Exception e1) {
						SupplementaryFilePanelMongoDB.showError("IOException",
								e1);
					}
				}
				
				@Override
				public void setDB(DB db) {
					this.db = db;
				}
			});
		} catch (Exception e) {
			SupplementaryFilePanelMongoDB.showError("IOException", e);
		}
	}
	
	public static DatabaseStorageResultWithURL insertHashedFile(final MongoDB m,
			final File file, File createTempPreviewImage, int isJavaImage,
			DataSetFileButton imageButton, MappingDataEntity tableName) {
		
		DatabaseStorageResult res = null;
		IOurl resultURL = null;
		try {
			MongoResourceIOConfigObject config = new MongoResourceIOConfigObject(null, DataStorageType.ANNOTATION_FILE);
			String targetFilename = file.getName();
			resultURL = m.getHandler().copyDataAndReplaceURLPrefix(new FileInputStream(file), targetFilename, config);
			
			if (createTempPreviewImage != null) {
				config = new MongoResourceIOConfigObject(null, DataStorageType.PREVIEW_ICON);
				m.getHandler().copyDataAndReplaceURLPrefix(new FileInputStream(createTempPreviewImage), targetFilename, config);
			}
			
			res = DatabaseStorageResult.STORED_IN_DB;
		} catch (Exception e1) {
			ErrorMsg.addErrorMessage(e1);
			res = DatabaseStorageResult.IO_ERROR_SEE_ERRORMSG;
		}
		
		return new DatabaseStorageResultWithURL(res, resultURL);
	}
	
	public static void fillFilePanel(final DataSetFilePanel filePanel,
			final MongoTreeNode mtdbe, final JTree expTree, final boolean isAnnotationSavePossible, final FilterConnector myFilterConnector)
			throws InterruptedException {
		LocalComputeJob r = new LocalComputeJob(new Runnable() {
			@Override
			public void run() {
				addFilesToPanel(filePanel, mtdbe, expTree, isAnnotationSavePossible, myFilterConnector);
			}
		}, "add files to panel");
		BackgroundThreadDispatcher.addTask(r);
	}
	
	static synchronized void addFilesToPanel(final DataSetFilePanel filePanel,
			final MongoTreeNode mt, final JTree expTree, boolean isAnnotationSavePossible, final FilterConnector myFilterConnector) {
		if (!mt.mayContainData())
			return;
		final StopObject stop = new StopObject(false);
		
		boolean cleared = false;
		final ArrayList<LocalComputeJob> executeLater = new ArrayList<LocalComputeJob>();
		Substance3D sub = null;
		Condition3D con = null;
		try {
			ArrayList<BinaryFileInfo> bbb = new ArrayList<BinaryFileInfo>();
			BinaryFileInfo primary = null;
			try {
				MappingDataEntity mde = mt.getTargetEntity();
				
				String files = mde.getFiles();
				if (files != null && !files.isEmpty()) {
					for (String url_string : files.split(";")) {
						IOurl url = new IOurl(url_string);
						BinaryFileInfo bfi = new BinaryFileInfo(url, null, false, mde);
						bfi.setIsAttachment(true);
						bbb.add(bfi);
					}
				}
				
				if (mde instanceof ImageData) {
					ImageData id = (ImageData) mde;
					primary = new BinaryFileInfo(id.getURL(), id.getLabelURL(),
							true, id);
				} else {
					if (mde instanceof VolumeData) {
						VolumeData id = (VolumeData) mde;
						primary = new BinaryFileInfo(id.getURL(), id.getLabelURL(),
								true, id);
					} else {
						if (mde instanceof Substance3D) {
							sub = (Substance3D) mde;
							con = null;
							primary = null;
							for (ConditionInterface c : sub)
								for (SampleInterface si : c) {
									if (si instanceof Sample3D) {
										Sample3D s3d = (Sample3D) si;
										for (NumericMeasurementInterface nmi : s3d
												.getMeasurements((MeasurementNodeType) null)) {
											if (nmi instanceof ImageData) {
												ImageData id = (ImageData) nmi;
												primary = new BinaryFileInfo(
														id.getURL(),
														id.getLabelURL(), true, id);
											} else
												if (nmi instanceof VolumeData) {
													VolumeData id = (VolumeData) nmi;
													primary = new BinaryFileInfo(
															id.getURL(),
															id.getLabelURL(), true, id);
												}
											if (primary != null)
												bbb.add(primary);
										}
									}
								}
							primary = null;
						} else
							if (mde instanceof Sample3D) {
								Sample3D s3dXX = (Sample3D) mde;
								String a = s3dXX.getTime() + "/" + s3dXX.getTimeUnit();
								primary = null;
								for (SampleInterface s3dI : s3dXX.getParentCondition()
										.getSortedSamples()) {
									String b = s3dI.getTime() + "/"
											+ s3dI.getTimeUnit();
									if (!a.equals(b))
										continue;
									Sample3D s3d = (Sample3D) s3dI;
									for (NumericMeasurementInterface nmi : s3d
											.getMeasurements((MeasurementNodeType) null)) {
										if (nmi instanceof ImageData) {
											ImageData id = (ImageData) nmi;
											primary = new BinaryFileInfo(id.getURL(),
													id.getLabelURL(), true, id);
										} else
											if (nmi instanceof VolumeData) {
												VolumeData id = (VolumeData) nmi;
												primary = new BinaryFileInfo(id.getURL(),
														id.getLabelURL(), true, id);
											}
										if (primary != null)
											bbb.add(primary);
									}
								}
								primary = null;
							} else {
								if (mde instanceof Condition3D) {
									Condition3D c3d = (Condition3D) mde;
									con = c3d;
									sub = (Substance3D) c3d.getParentSubstance();
									primary = null;
									for (SampleInterface si : c3d) {
										Sample3D s3d = (Sample3D) si;
										for (NumericMeasurementInterface nmi : s3d
												.getMeasurements((MeasurementNodeType) null)) {
											if (nmi instanceof ImageData) {
												ImageData id = (ImageData) nmi;
												IOurl urlMain = id.getURL();
												IOurl urlLabel = id.getLabelURL();
												primary = new BinaryFileInfo(urlMain, urlLabel, true, id, s3d.toString());
											} else
												if (nmi instanceof VolumeData) {
													VolumeData id = (VolumeData) nmi;
													IOurl urlMain = id.getURL();
													IOurl urlLabel = id.getLabelURL();
													primary = new BinaryFileInfo(urlMain, urlLabel, true, id, s3d.toString());
												}
											if (primary != null)
												bbb.add(primary);
										}
									}
									primary = null;
								}
							}
					}
				}
				
			} catch (Exception e) {
				ErrorMsg.addErrorMessage(e);
			}
			if (primary != null)
				bbb.add(primary);
			
			Map<String, Object> properties = new HashMap<String, Object>();
			mt.getTargetEntity().fillAttributeMap(properties);
			
			search: for (Entry<String, Object> e : properties.entrySet()) {
				if (e.getKey().startsWith("anno")) {
					Object v = e.getValue();
					if (v != null && v instanceof String) {
						String vss = (String) v;
						for (String vs : vss.split(";")) {
							String fileName = vs;
							if (vs.contains("#"))
								fileName = vs.split("#", 2)[0];
							if (!vs.contains("#")
									|| !fileName.equals("oldreference"))
								continue;
							if (vs.contains("#"))
								fileName = vs.split("#", 2)[1];
							if (primary != null)
								bbb.add(new BinaryFileInfo(primary.getFileNameLabel(), new IOurl(fileName), false, mt.getTargetEntity()));
							bbb.add(new BinaryFileInfo(new IOurl(fileName), null, false, mt.getTargetEntity()));
							break search;
						}
					}
				}
			}
			
			if (sub != null) {
				if (!cleared) {
					cleared = true;
					clearPanel(filePanel, mt, expTree);
				}
				processChartGenerator(executeLater, con != null ? con : sub, sub, mt, expTree, filePanel, bbb.isEmpty(), stop, isAnnotationSavePossible);
			}
			
			BinaryFileInfo lastBBB = null;
			Collection<DataSetFileButton> buttonsInThisView = new LinkedList<DataSetFileButton>();
			
			BinaryFileInfo lastInList = null;
			for (final BinaryFileInfo binaryFileInfo : bbb) {
				if (mt != expTree.getSelectionPath().getLastPathComponent())
					break;
				lastInList = binaryFileInfo;
				ImageResult imageResult = new ImageResult(null, binaryFileInfo);
				
				String cl = DataSetFileButton.getName(imageResult, null);
				if (cl != null)
					if (!myFilterConnector.matches(cl, false, false))
						continue;
				lastBBB = binaryFileInfo;
			}
			myFilterConnector.resetCount();
			for (final BinaryFileInfo binaryFileInfo : bbb) {
				if (mt != expTree.getSelectionPath().getLastPathComponent())
					break;
				ImageResult imageResult = new ImageResult(null, binaryFileInfo);
				
				String cl = DataSetFileButton.getName(imageResult, null);
				if (cl != null)
					if (!myFilterConnector.matches(cl, binaryFileInfo == lastInList, true))
						continue;
				
				boolean previewLoadAndConstructNeeded = false;
				
				ImageIcon previewImage = null;
				if (DataSetFileButton.ICON_WIDTH == 128) {
					try {
						byte[] pi = ResourceIOManager
								.getPreviewImageContent(binaryFileInfo
										.getFileNameMain());
						if (pi != null) {
							previewImage = new ImageIcon(pi);
						} else
							previewLoadAndConstructNeeded = true;
					} catch (Exception e) {
						previewLoadAndConstructNeeded = true;
					}
				} else {
					previewImage = new ImageIcon(MyImageIcon.loadingIcon.getAsBufferedImage());
					previewLoadAndConstructNeeded = true;
				}
				final DataSetFileButton imageButton = new DataSetFileButton(
						mt, imageResult, previewImage, mt.isReadOnly(), false, null, buttonsInThisView);
				
				synchronized (buttonsInThisView) {
					buttonsInThisView.add(imageButton);
				}
				if (binaryFileInfo.isPrimary())
					imageButton.setIsPrimaryDatabaseEntity();
				if (binaryFileInfo.isAttachment())
					imageButton.setIsAttachment();
				imageButton.setAdditionalFileNameInfo(binaryFileInfo.getAdditionalFileNameInfo());
				imageButton.setDownloadNeeded(!FileSystemHandler
						.isFileUrl(binaryFileInfo.getFileNameMain()));
				imageButton.setVerticalTextPosition(SwingConstants.BOTTOM);
				imageButton.setHorizontalTextPosition(SwingConstants.CENTER);
				
				if (!cleared) {
					cleared = true;
					clearPanel(filePanel, mt, expTree);
				}
				
				final boolean previewLoadAndConstructNeededF = previewLoadAndConstructNeeded;
				
				final boolean fIsLast = binaryFileInfo == lastBBB;
				
				SwingUtilities.invokeLater(processIcon(filePanel, mt, expTree,
						stop, executeLater, binaryFileInfo, imageButton,
						previewLoadAndConstructNeededF, fIsLast, isAnnotationSavePossible, true));
			}
			
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
		}
	}
	
	private static void processChartGenerator(ArrayList<LocalComputeJob> executeLater, final MappingDataEntity mde, final Substance3D sub,
			MongoTreeNode mt, JTree expTree, DataSetFilePanel filePanel, boolean isLast, StopObject stop, boolean isAnnotationSavePossible) {
		if (mt != expTree.getSelectionPath().getLastPathComponent())
			return;
		boolean addDataChart = true;
		if (mde != null && mde instanceof SubstanceInterface) {
			SubstanceInterface si = (SubstanceInterface) mde;
			boolean foundNumericValues = false;
			mainLoop: for (ConditionInterface ci : si)
				for (SampleInterface sai : ci)
					for (NumericMeasurementInterface nmi : sai) {
						if (!(nmi instanceof BinaryMeasurement)) {
							foundNumericValues = true;
							break mainLoop;
						}
					}
			if (!foundNumericValues)
				addDataChart = false;
		} else
			if (mde != null && mde instanceof ConditionInterface) {
				boolean foundNumericValues = false;
				mainLoop: for (SampleInterface sai : (ConditionInterface) mde)
					for (NumericMeasurementInterface nmi : sai) {
						if (!(nmi instanceof BinaryMeasurement)) {
							foundNumericValues = true;
							break mainLoop;
						}
					}
				if (!foundNumericValues)
					addDataChart = false;
			}
		
		if (addDataChart) {
			ImageIcon previewImage = new ImageIcon(IAPimages.getImage(IAPimages.getHistogramIcon()));
			
			final DataSetFileButton chartingButton = new DataSetFileButton(
					mt, null, previewImage, mt.isReadOnly(), true, (mde instanceof Condition3D) ?
							"<html><center>Create Data Chart<br>"
									+ "(specific condition)" : "<html><center>Create Data Chart<br>(selected&nbsp;property)", null);
			chartingButton.setAdditionalActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					final BackgroundTaskStatusProviderSupportingExternalCallImpl status = new BackgroundTaskStatusProviderSupportingExternalCallImpl(
							"Process data...", "");
					Runnable r = new Runnable() {
						@Override
						public void run() {
							final ExperimentInterface expf;
							if (mde instanceof Condition3D) {
								ExperimentInterface exp = Experiment.copyAndExtractSubtanceInclusiveData(sub, (ConditionInterface) mde);
								Collection<NumericMeasurementInterface> md = Substance3D.getAllMeasurements(exp);
								ArrayList<MappingData3DPath> mmd = new ArrayList<MappingData3DPath>();
								for (NumericMeasurementInterface nmi : md) {
									MappingData3DPath mp = new MappingData3DPath(nmi, true);
									mp.getConditionData().setVariety(mp.getConditionData().getVariety() != null && !mp.getConditionData().getVariety().isEmpty() ?
											mp.getConditionData().getVariety() + "/" + mp.getMeasurement().getQualityAnnotation()
											: mp.getMeasurement().getQualityAnnotation());
									
									mmd.add(mp);
								}
								expf = MappingData3DPath.merge(mmd, true, status);
								
							} else {
								status.setCurrentStatusText1("Extract subset " + sub.getName());
								ExperimentInterface exp = Experiment.copyAndExtractSubtanceInclusiveData(sub);
								if (status.wantsToStop())
									return;
								Collection<NumericMeasurementInterface> md = Substance3D.getAllMeasurements(exp);
								status.setCurrentStatusText1("Create dataset for plotting");
								expf = MappingData3DPath.merge(md, true, status);
							}
							if (status.wantsToStop())
								return;
							HashSet<String> speciesNames = new HashSet<String>();
							for (SubstanceInterface si : expf)
								for (ConditionInterface ci : si) {
									speciesNames.add(ci.getSpecies());
								}
							int idx = 1;
							for (SubstanceInterface si : expf)
								for (ConditionInterface ci : si)
									ci.setRowId(idx++);
							if (speciesNames.size() == 1)
								for (SubstanceInterface si : expf)
									for (ConditionInterface ci : si)
										ci.setSpecies(null);
							BackgroundTaskHelper.executeLaterOnSwingTask(0, new Runnable() {
								@Override
								public void run() {
									DataChartComponentWindow dccw = new DataChartComponentWindow(expf);
									dccw.setVisible(true);
								}
							});
							
						}
					};
					BackgroundTaskHelper.issueSimpleTaskInWindow("Plot data", "Process data...", r, null, status, true, true);
				}
			});
			chartingButton.setVerticalTextPosition(SwingConstants.BOTTOM);
			chartingButton.setHorizontalTextPosition(SwingConstants.CENTER);
			
			SwingUtilities.invokeLater(processIcon(filePanel, mt, expTree,
					stop, executeLater, null, chartingButton, false, false, isAnnotationSavePossible, true));
		}
		if (addDataChart) {
			ImageIcon previewImage = new ImageIcon(IAPimages.getImage(IAPimages.getHistogramIcon()));
			
			final DataSetFileButton chartingButton = new DataSetFileButton(
					mt, null, previewImage, mt.isReadOnly(), true, (mde instanceof Condition3D) ?
							"<html><center>Export Data (XLSX)<br>"
									+ "(specific condition)" : "<html><center>Export Data (XLSX)<br>(selected&nbsp;property)", null);
			chartingButton.setAdditionalActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					String defaultFileName = StringManipulationTools.getFileSystemName(
							sub.iterator().next().getExperimentHeader().getExperimentName()
									+ "_" + sub.getName() + ".xlsx");
					String fn = FileHelper.getFileName(".xlsx", "Excel File", defaultFileName);
					if (fn != null) {
						boolean xlsx = true;
						ActionPdfCreation3 action = new ActionPdfCreation3(
								(ExperimentReference) null,
								(ArrayList<ThreadSafeOptions>) null,
								new ThreadSafeOptions() /* false */,
								new ThreadSafeOptions() /* false */,
								xlsx,
								(ArrayList<ThreadSafeOptions>) null,
								(ArrayList<ThreadSafeOptions>) null,
								(ThreadSafeOptions) null,
								(ThreadSafeOptions) null,
								(ThreadSafeOptions) null,
								true);
						/*
						 * ExperimentReference experimentReference,
						 * ArrayList<ThreadSafeOptions> divideDatasetBy,
						 * ThreadSafeOptions exportIndividualAngles,
						 * boolean xlsx,
						 * ArrayList<ThreadSafeOptions> togglesFiltering,
						 * ArrayList<ThreadSafeOptions> togglesInterestingProperties,
						 * ThreadSafeOptions tsoBootstrapN,
						 * ThreadSafeOptions tsoSplitFirst,
						 * ThreadSafeOptions tsoSplitSecond,
						 * boolean exportCommand
						 */
						ExperimentInterface exp;
						if (mde instanceof Condition3D)
							exp = Experiment.copyAndExtractSubtanceInclusiveData(sub, (ConditionInterface) mde);
						else
							exp = Experiment.copyAndExtractSubtanceInclusiveData(sub);
						action.setExperimentReference(
								new ExperimentReference(exp));
						action.setUseIndividualReportNames(true);
						action.setStatusProvider(null);
						action.setSource(null, null);
						action.setCustomTargetFileName(fn);
						try {
							action.performActionCalculateResults(null);
						} catch (Exception e) {
							e.printStackTrace();
							MainFrame.getInstance().showMessageDialog("Could not perform operation: " + e.getMessage());
						}
					}
				}
				
			});
			chartingButton.setVerticalTextPosition(SwingConstants.BOTTOM);
			chartingButton.setHorizontalTextPosition(SwingConstants.CENTER);
			
			SwingUtilities.invokeLater(processIcon(filePanel, mt, expTree,
					stop, executeLater, null, chartingButton, false, isLast, isAnnotationSavePossible, true));
		}
	}
	
	private static Runnable processIcon(final DataSetFilePanel filePanel,
			final MongoTreeNode mt, final JTree expTree, final StopObject stop,
			final ArrayList<LocalComputeJob> executeLater,
			final BinaryFileInfo binaryFileInfo,
			final DataSetFileButton imageButton,
			final boolean previewLoadAndConstructNeededF, final boolean fIsLast,
			final boolean isAnnotationSavePossible, final boolean addInfoPanel) {
		final int tw = DataSetFileButton.ICON_WIDTH;
		return new Runnable() {
			@Override
			public void run() {
				// nur falls der Zielknoten immer noch ausgewählt ist,
				// soll der Button hinzugefügt werden
				if (mt == expTree.getSelectionPath().getLastPathComponent()
						&& DataSetFileButton.ICON_WIDTH == tw) {
					MeasurementFilter mf = new MySnapshotFilter(new ArrayList<ThreadSafeOptions>(), mt.getExperiment().getHeader().getGlobalOutlierInfo());
					final AnnotationInfoPanel aip = new AnnotationInfoPanel(
							imageButton, mt, mf);
					imageButton.setAnnotationInfoPanel(aip);
					final JComponent buttonAndInfo = binaryFileInfo == null || !binaryFileInfo.isPrimary() ? imageButton
							: TableLayout.getSplitVertical(imageButton, aip,
									TableLayout.PREFERRED,
									TableLayout.PREFERRED);
					
					if (isAnnotationSavePossible) {
						imageButton.addMouseListener(getML(aip));
						buttonAndInfo.addMouseListener(getML(aip));
					}
					if (addInfoPanel)
						filePanel.add(buttonAndInfo);
					else
						filePanel.add(imageButton);
					filePanel.validate();
					filePanel.repaint();
					filePanel.getScrollpane().validate();
					if (previewLoadAndConstructNeededF) {
						LocalComputeJob t;
						try {
							t = new LocalComputeJob(new Runnable() {
								@Override
								public void run() {
									if (mt == expTree.getSelectionPath()
											.getLastPathComponent()
											&& DataSetFileButton.ICON_WIDTH == tw) {
										final MyImageIcon myImage;
										try {
											myImage = new MyImageIcon(
													buttonAndInfo, // MainFrame.getInstance(),
													DataSetFileButton.ICON_WIDTH,
													DataSetFileButton.ICON_HEIGHT,
													binaryFileInfo
															.getFileNameMain(),
													binaryFileInfo
															.getFileNameLabel(),
													binaryFileInfo);
											myImage.imageAvailable = 1;
											try {
												BackgroundTaskHelper
														.executeLaterOnSwingTask(
																0,
																new Runnable() {
																	@Override
																	public void run() {
																		imageButton
																				.updateLayout(
																						null,
																						myImage,
																						myImage, false);
																	}
																});
											} catch (Exception e) {
												ErrorMsg.addErrorMessage(e);
											}
										} catch (MalformedURLException e) {
											// empty
										}
									}
								}
							}, "preview load and construct");
							executeLater.add(t);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						
					}
					Collections.reverse(executeLater);
					BackgroundTaskHelper.executeLaterOnSwingTask(10,
							new Runnable() {
								@Override
								public void run() {
									boolean isLast = fIsLast;
									if (isLast)
										for (LocalComputeJob ttt : executeLater)
											BackgroundThreadDispatcher.addTask(ttt, true, Integer.MAX_VALUE);
								}
							});
				} else
					stop.setStopWanted(true);
			}
			
			private MouseListener getML(final AnnotationInfoPanel aip) {
				return new MouseListener() {
					@Override
					public void mouseReleased(MouseEvent e) {
					}
					
					@Override
					public void mousePressed(MouseEvent e) {
					}
					
					@Override
					public void mouseExited(MouseEvent e) {
						aip.removeGuiLater();
					}
					
					@Override
					public void mouseEntered(MouseEvent e) {
						aip.addGui();
					}
					
					@Override
					public void mouseClicked(MouseEvent e) {
					}
				};
			}
		};
	}
	
	private static void clearPanel(final DataSetFilePanel filePanel,
			final MongoTreeNode mt, final JTree expTree) {
		Runnable r = new Runnable() {
			@Override
			public void run() {
				if (mt == expTree.getSelectionPath().getLastPathComponent()) {
					filePanel.removeAll();
					filePanel.validate();
					filePanel.repaint();
					filePanel.getScrollpane().validate();
				}
			}
		};
		if (SwingUtilities.isEventDispatchThread())
			r.run();
		else
			SwingUtilities.invokeLater(r);
	}
	
	public static void attachFileToEntity(MappingDataEntity targetEntity, DatabaseStorageResultWithURL res, String name) {
		if (name.contains(":"))
			name = StringManipulationTools.stringReplace(name, ";", "_");
		String currentValue = targetEntity.getFiles();
		if (currentValue == null || currentValue.isEmpty())
			targetEntity.setFiles(res.getResultURL() + "");
		else {
			LinkedHashSet<String> values = new LinkedHashSet<String>();
			for (String s : currentValue.split(";"))
				values.add(s);
			values.add(res.getResultURL() + "");
			targetEntity.setFiles(StringManipulationTools.getStringList(values, ";"));
		}
	}
}
