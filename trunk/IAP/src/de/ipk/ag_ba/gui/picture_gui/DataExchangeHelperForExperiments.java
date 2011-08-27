/*
 * Copyright (c) 2003 IPK Gatersleben
 * $Id: DataExchangeHelperForExperiments.java,v 1.6 2010-11-05 09:46:50 klukas
 * Exp $
 */
package de.ipk.ag_ba.gui.picture_gui;

import info.clearthought.layout.TableLayout;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.ErrorMsg;
import org.graffiti.editor.GravistoService;
import org.graffiti.editor.MainFrame;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;
import org.graffiti.plugin.io.resources.FileSystemHandler;
import org.graffiti.plugin.io.resources.IOurl;
import org.graffiti.plugin.io.resources.ResourceIOManager;

import com.mongodb.DB;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;

import de.ipk.ag_ba.mongo.DatabaseStorageResult;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.mongo.RunnableOnDB;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.MappingDataEntity;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleInterface;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Condition3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.MeasurementNodeType;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Sample3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Substance3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.volumes.VolumeData;

/**
 * @author Klukas
 */
public class DataExchangeHelperForExperiments {
	
	public static int getSizeOfExperiment(MongoDB m, ExperimentInterface experimentName) {
		return -1;
	}
	
	public static void downloadFile(MongoDB m, final String hash, final File targetFile,
						final DataSetFileButton button, final MongoCollection collection) {
		try {
			m.processDB(new RunnableOnDB() {
				
				private DB db;
				
				@Override
				public void run() {
					try {
						// Blob b = CallDBE2WebService.getBlob(user, pass,
						// imageResult.getMd5());
						
						GridFS gridfs_images = new GridFS(db, collection.toString());
						System.out.println("Look for " + collection.toString() + "-HASH: " + hash);
						GridFSDBFile fff = gridfs_images.findOne(hash);
						if (fff == null)
							System.out.println("NOT FOUND");
						if (fff != null) {
							System.out.println("FOUND, LENGTH=" + fff.getLength());
							InputStream bis = fff.getInputStream();
							
							FileOutputStream fos = new FileOutputStream(targetFile);
							int readBytes = 0;
							int pos = 0;
							long len = fff.getLength();
							byte[] readBuff = new byte[1024 * 1024];
							button.progress.setMaximum(100);
							while (0 < (readBytes = bis.read(readBuff))) {
								fos.write(readBuff, 0, readBytes);
								pos += readBytes;
								button.progress.setValue((int) ((double) pos / len * 100d));
							}
							bis.close();
							fos.close();
							System.out.println("Created " + targetFile.getAbsolutePath() + " (" + targetFile.length()
												+ " bytes, read " + pos + ")");
						}
					} catch (Exception e1) {
						SupplementaryFilePanelMongoDB.showError("IOException", e1);
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
	
	public static DatabaseStorageResult insertHashedFile(final MongoDB m, final File file,
						File createTempPreviewImage, int isJavaImage, DataSetFileButton imageButton, MappingDataEntity tableName) {
		
		final ThreadSafeOptions tso = new ThreadSafeOptions();
		try {
			m.processDB(new RunnableOnDB() {
				private DB db;
				
				public void run() {
					String hash;
					try {
						hash = GravistoService.getHashFromFile(file, m.getHashType());
						GridFS gridfs_annotation = new GridFS(db, "annotations");
						m.saveAnnotationFile(gridfs_annotation, hash, file);
						// GridFS gridfs_images = new GridFS(db, "images");
						GridFSDBFile fff = gridfs_annotation.findOne(hash);
						if (fff != null) {
							tso.setParam(0, DatabaseStorageResult.EXISITING_NO_STORAGE_NEEDED);
							tso.setParam(1, hash);
							return;
						} else {
							try {
								m.saveAnnotationFile(gridfs_annotation, hash, file);
								tso.setParam(0, DatabaseStorageResult.STORED_IN_DB);
								tso.setParam(1, hash);
								return;
							} catch (IOException e) {
								ErrorMsg.addErrorMessage(e);
								tso.setParam(0, DatabaseStorageResult.IO_ERROR_SEE_ERRORMSG);
								return;
							}
						}
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
				
				public void setDB(DB db) {
					this.db = db;
				}
			});
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
			return null;
		}
		((DatabaseStorageResult) tso.getParam(0, null)).setMD5((String) tso.getParam(1, ""));
		return (DatabaseStorageResult) tso.getParam(0, null);
	}
	
	public static void fillFilePanel(final DataSetFilePanel filePanel, final MongoTreeNode mtdbe, final JTree expTree,
						final MongoDB m) {
		MyThread r = new MyThread(new Runnable() {
			public void run() {
				addFilesToPanel(filePanel, mtdbe, expTree, m);
			}
		}, "add files to panel");
		BackgroundThreadDispatcher.addTask(r, 1000, 0);
	}
	
	static synchronized void addFilesToPanel(final DataSetFilePanel filePanel, final MongoTreeNode mt,
						final JTree expTree, MongoDB m) {
		if (!mt.mayContainData())
			return;
		final StopObject stop = new StopObject(false);
		
		boolean cleared = false;
		
		try {
			ArrayList<BinaryFileInfo> bbb = new ArrayList<BinaryFileInfo>();
			BinaryFileInfo primary = null;
			try {
				MappingDataEntity mde = mt.getTargetEntity();
				if (mde instanceof ImageData) {
					ImageData id = (ImageData) mde;
					primary = new BinaryFileInfo(id.getURL(), id.getLabelURL(), true, id);
				} else
					if (mde instanceof VolumeData) {
						VolumeData id = (VolumeData) mde;
						primary = new BinaryFileInfo(id.getURL(), id.getLabelURL(), true, id);
					} else {
						if (mde instanceof Substance3D) {
							Substance3D sub = (Substance3D) mde;
							primary = null;
							for (ConditionInterface c : sub)
								for (SampleInterface si : c) {
									if (si instanceof Sample3D) {
										Sample3D s3d = (Sample3D) si;
										for (NumericMeasurementInterface nmi : s3d.getMeasurements((MeasurementNodeType) null)) {
											if (nmi instanceof ImageData) {
												ImageData id = (ImageData) nmi;
												primary = new BinaryFileInfo(id.getURL(), id.getLabelURL(), true, id);
											} else
												if (nmi instanceof VolumeData) {
													VolumeData id = (VolumeData) nmi;
													primary = new BinaryFileInfo(id.getURL(), id.getLabelURL(), true, id);
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
								for (SampleInterface s3dI : s3dXX.getParentCondition().getSortedSamples()) {
									String b = s3dI.getTime() + "/" + s3dI.getTimeUnit();
									if (!a.equals(b))
										continue;
									Sample3D s3d = (Sample3D) s3dI;
									for (NumericMeasurementInterface nmi : s3d.getMeasurements((MeasurementNodeType) null)) {
										if (nmi instanceof ImageData) {
											ImageData id = (ImageData) nmi;
											primary = new BinaryFileInfo(id.getURL(), id.getLabelURL(), true, id);
										} else
											if (nmi instanceof VolumeData) {
												VolumeData id = (VolumeData) nmi;
												primary = new BinaryFileInfo(id.getURL(), id.getLabelURL(), true, id);
											}
										if (primary != null)
											bbb.add(primary);
									}
								}
								primary = null;
							} else {
								if (mde instanceof Condition3D) {
									Condition3D c3d = (Condition3D) mde;
									primary = null;
									for (SampleInterface si : c3d) {
										Sample3D s3d = (Sample3D) si;
										for (NumericMeasurementInterface nmi : s3d.getMeasurements((MeasurementNodeType) null)) {
											if (nmi instanceof ImageData) {
												ImageData id = (ImageData) nmi;
												IOurl urlMain = new IOurl(id.getURL().toString() + " (" + s3d.toString() + ")");
												IOurl urlLabel = new IOurl(id.getLabelURL().toString() + " (" + s3d.toString() + ")");
												primary = new BinaryFileInfo(urlMain, urlLabel, true, id);
											} else
												if (nmi instanceof VolumeData) {
													VolumeData id = (VolumeData) nmi;
													IOurl urlMain = new IOurl(id.getURL().toString() + " (" + s3d.toString() + ")");
													IOurl urlLabel = new IOurl(id.getLabelURL().toString() + " (" + s3d.toString() + ")");
													primary = new BinaryFileInfo(urlMain, urlLabel, true, id);
												}
											if (primary != null)
												bbb.add(primary);
										}
									}
									primary = null;
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
			
			for (Entry<String, Object> e : properties.entrySet()) {
				if (e.getKey().startsWith("anno")) {
					Object v = e.getValue();
					if (v != null && v instanceof String) {
						String vs = (String) v;
						String fileName = vs;
						bbb.add(new BinaryFileInfo(new IOurl(fileName), null, false, mt.getTargetEntity()));
					}
				}
			}
			
			final ArrayList<MyThread> executeLater = new ArrayList<MyThread>();
			BinaryFileInfo lastBBB = null;
			if (bbb.size() > 0)
				lastBBB = bbb.get(bbb.size() - 1);
			for (final BinaryFileInfo binaryFileInfo : bbb) {
				if (mt != expTree.getSelectionPath().getLastPathComponent())
					break;
				ImageResult imageResult = new ImageResult(null, binaryFileInfo);
				boolean previewLoadAndConstructNeeded = false;
				
				ImageIcon previewImage = null;
				// if (FileSystemHandler.isFileUrl(binaryFileInfo.getFileNameMain())) {
				// MyImageIcon myImage = new MyImageIcon(MainFrame.getInstance(), DataSetFileButton.ICON_WIDTH,
				// DataSetFileButton.ICON_HEIGHT, binaryFileInfo.getFileNameMain(),
				// binaryFileInfo.getFileNameLabel(), binaryFileInfo);
				// myImage.imageAvailable = 1;
				// previewImage = myImage;
				// } else
				// // if (LemnaTecFTPhandler.isLemnaTecFtpUrl(binaryFileInfo.getFileNameMain())) {
				// previewImage = null;
				// previewLoadAndConstructNeeded = true;
				// } else {
				if (DataSetFileButton.ICON_WIDTH == 128) { // binaryFileInfo.getFileNameMain().getPrefix().startsWith("mongo_") &&
					byte[] pi = ResourceIOManager.getPreviewImageContent(binaryFileInfo.getFileNameMain());
					if (pi != null)
						previewImage = new ImageIcon(pi);
					else
						previewLoadAndConstructNeeded = true;
				} else {
					previewImage = null;
					previewLoadAndConstructNeeded = true;
				}
				// }
				final DataSetFileButton imageButton = new DataSetFileButton(m, mt, imageResult,
									previewImage, mt.isReadOnly());
				if (binaryFileInfo.isPrimary())
					imageButton.setIsPrimaryDatabaseEntity();
				
				imageButton.setDownloadNeeded(!FileSystemHandler.isFileUrl(binaryFileInfo.getFileNameMain()));
				imageButton.setVerticalTextPosition(SwingConstants.BOTTOM);
				imageButton.setHorizontalTextPosition(SwingConstants.CENTER);
				
				if (!cleared) {
					cleared = true;
					clearPanel(filePanel, mt, expTree);
				}
				
				final boolean previewLoadAndConstructNeededF = previewLoadAndConstructNeeded;
				
				final boolean fIsLast = binaryFileInfo == lastBBB;
				
				SwingUtilities.invokeLater(processIcon(filePanel, mt, expTree, stop, executeLater, binaryFileInfo,
									imageButton, previewLoadAndConstructNeededF, fIsLast));
			}
			
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
		}
	}
	
	private static Runnable processIcon(final DataSetFilePanel filePanel, final MongoTreeNode mt, final JTree expTree,
						final StopObject stop, final ArrayList<MyThread> executeLater, final BinaryFileInfo binaryFileInfo,
						final DataSetFileButton imageButton, final boolean previewLoadAndConstructNeededF, final boolean fIsLast) {
		final int tw = DataSetFileButton.ICON_WIDTH;
		return new Runnable() {
			public void run() {
				// nur falls der Zielknoten immer noch ausgewählt ist,
				// soll der Button hinzugefügt werden
				if (mt == expTree.getSelectionPath().getLastPathComponent() &&
									DataSetFileButton.ICON_WIDTH == tw) {
					final AnnotationInfoPanel aip = new AnnotationInfoPanel(imageButton, mt);
					JComponent buttonAndInfo = TableLayout.getSplitVertical(imageButton, aip, TableLayout.PREFERRED,
							TableLayout.PREFERRED);
					imageButton.addMouseListener(getML(aip));
					buttonAndInfo.addMouseListener(getML(aip));
					filePanel.add(
							buttonAndInfo
							);
					filePanel.validate();
					filePanel.repaint();
					filePanel.getScrollpane().validate();
					if (previewLoadAndConstructNeededF) {
						MyThread t = new MyThread(new Runnable() {
							@Override
							public void run() {
								if (mt == expTree.getSelectionPath().getLastPathComponent() &&
													DataSetFileButton.ICON_WIDTH == tw) {
									final MyImageIcon myImage;
									try {
										myImage = new MyImageIcon(MainFrame.getInstance(), DataSetFileButton.ICON_WIDTH,
															DataSetFileButton.ICON_HEIGHT,
															binaryFileInfo.getFileNameMain(),
															binaryFileInfo.getFileNameLabel(), binaryFileInfo);
										myImage.imageAvailable = 1;
										try {
											SwingUtilities.invokeAndWait(new Runnable() {
												@Override
												public void run() {
													imageButton.updateLayout(null, myImage, myImage);
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
						
					}
					BackgroundTaskHelper.executeLaterOnSwingTask(10, new Runnable() {
						@Override
						public void run() {
							boolean isLast = fIsLast;
							if (isLast)
								for (MyThread ttt : executeLater)
									BackgroundThreadDispatcher.addTask(ttt, -1 + 1000, 0);
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
	
	private static void clearPanel(final DataSetFilePanel filePanel, final MongoTreeNode mt, final JTree expTree) {
		try {
			Runnable r = new Runnable() {
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
				SwingUtilities.invokeAndWait(r);
		} catch (InterruptedException e2) {
			SupplementaryFilePanelMongoDB.showError("InterruptedException", e2);
		} catch (InvocationTargetException e2) {
			SupplementaryFilePanelMongoDB.showError("InvocationTargetException", e2);
		}
	}
	
	// private static File getPreviewFileFromDatabase(BlobPropertyExtended
	// blobInfo) {
	//
	// try {
	// InputStream bis = new
	// BufferedInputStream(blobInfo.previewImage.getBinaryStream());
	// File previewFile = File.createTempFile("dbe_preview_", ".png");
	// SupplementaryFilePanel.addTempFileToBeDeletedLater(previewFile);
	// FileOutputStream fos = new FileOutputStream(previewFile);
	// int readBytes = 0;
	// int pos = 0;
	// byte[] readBuff = new byte[1024];
	// while (0 < (readBytes = bis.read(readBuff))) {
	// fos.write(readBuff, 0, readBytes);
	// pos += readBytes;
	// }
	// bis.close();
	// fos.close();
	// return previewFile;
	// } catch (Exception e1) {
	// SupplementaryFilePanel.showError("IOException", e1);
	// return null;
	// }
	// }
	//
	// public static ImageResult getImageInfo(String imageMD5, String
	// targetTable, String targetTablePrimaryKeyName,
	// Object targetTablePrimaryKeyValue) {
	// BlobPropertyExtended[] bpea = null;
	// try {
	// bpea = CallDBE2WebService.getAllSupplFileInfos("", "",
	// DBTable.valueOf(targetTable),
	// targetTablePrimaryKeyValue.toString());
	// } catch (Exception e) {
	// ErrorMsg.addErrorMessage(e);
	// }
	//
	// return null;
	// }
	//
	// public static void removeAllImagesForOneTargetNodeFromDataBase(String
	// user, String pass, ImageResult imageResult) {
	// try {
	// for (BlobPropertyExtended bpe :
	// CallDBE2WebService.getAllSupplFileInfos(user, pass, imageResult
	// .getTargetTable(), "" + imageResult.getTargetTablePrimaryKeyValue())) {
	// String id = imageResult.getTargetTablePrimaryKeyValue() + "";
	// CallDBE2WebService.setDeleteBlobSupplementaryFile(user, pass, bpe.md5,
	// imageResult.getTargetTable(), id);
	// }
	// } catch (Exception e) {
	// SupplementaryFilePanel.showError("Error while deleting files!", e);
	// }
	// }
	//
	// public static void removeSingleImageFromDataBase(String user, String pass,
	// ImageResult imageResult) {
	// try {
	// CallDBE2WebService.setDeleteBlobSupplementaryFile(user, pass,
	// imageResult.getMd5(), imageResult
	// .getTargetTable(), imageResult.getTargetTablePrimaryKeyValue() + "");
	// } catch (Exception e) {
	// SupplementaryFilePanel.showError("Can't delete file from database!", e);
	// }
	// }
}
