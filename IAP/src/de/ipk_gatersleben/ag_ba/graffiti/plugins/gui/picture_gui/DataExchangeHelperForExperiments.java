/* Copyright (c) 2003 IPK Gatersleben
 * $Id: DataExchangeHelperForExperiments.java,v 1.3 2010-10-12 12:10:35 klukas Exp $
 */
package de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.picture_gui;

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
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.AttributeHelper;
import org.ErrorMsg;
import org.graffiti.editor.MainFrame;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;

import com.mongodb.DB;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;

import de.ipk_gatersleben.ag_ba.mongo.DatabaseStorageResult;
import de.ipk_gatersleben.ag_ba.mongo.MongoDB;
import de.ipk_gatersleben.ag_ba.mongo.RunnableOnDB;
import de.ipk_gatersleben.ag_ba.postgresql.LemnaTecFTPhandler;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.MappingDataEntity;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.FileSystemHandler;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.IOurl;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.ImageData;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Sample3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.VolumeData;

/**
 * @author Klukas
 */
public class DataExchangeHelperForExperiments {

	public static int getSizeOfExperiment(String user, String pass, ExperimentInterface experimentName) {
		return -1;
	}

	public static void downloadFile(String user, String pass, final ImageResult imageResult, final File targetFile,
			final DataSetFileButton button, final MongoCollection collection) {
		try {
			new MongoDB().processDB(new RunnableOnDB() {

				private DB db;

				@Override
				public void run() {
					try {
						// Blob b = CallDBE2WebService.getBlob(user, pass,
						// imageResult.getMd5());

						GridFS gridfs_images = new GridFS(db, collection.toString());
						System.out.println("Look for " + collection.toString() + "-MD5: " + imageResult.getMd5());
						GridFSDBFile fff = gridfs_images.findOne(imageResult.getMd5());
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

	public static DatabaseStorageResult insertMD5checkedFile(String user, String pass, final File file,
			File createTempPreviewImage, int isJavaImage, DataSetFileButton imageButton, MappingDataEntity tableName) {

		final ThreadSafeOptions tso = new ThreadSafeOptions();
		try {
			new MongoDB().processDB(new RunnableOnDB() {
				private DB db;

				public void run() {
					String md5;
					try {
						md5 = AttributeHelper.getMD5fromFile(file);
						GridFS gridfs_annotation = new GridFS(db, "annotations");
						new MongoDB().saveAnnotationFile(gridfs_annotation, md5, file);
						// GridFS gridfs_images = new GridFS(db, "images");
						GridFSDBFile fff = gridfs_annotation.findOne(md5);
						if (fff != null) {
							tso.setParam(0, DatabaseStorageResult.EXISITING_NO_STORAGE_NEEDED);
							tso.setParam(1, md5);
							return;
						} else {
							try {
								new MongoDB().saveAnnotationFile(gridfs_annotation, md5, file);
								tso.setParam(0, DatabaseStorageResult.STORED_IN_DB);
								tso.setParam(1, md5);
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

	public static void fillFilePanel(final JMyFilePanel filePanel, final MongoTreeNode mtdbe, final JTree expTree,
			final String login, final String password) {
		Thread r = new Thread(new Runnable() {
			public void run() {
				addFilesToPanel(filePanel, mtdbe, expTree, login, password);
			}
		});
		BackgroundThreadDispatcher.addTask(r, 0);
	}

	static synchronized void addFilesToPanel(final JMyFilePanel filePanel, final MongoTreeNode mt, final JTree expTree,
			String dbeUser, String dbePass) {
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
					primary = new BinaryFileInfo(id.getURL(), true, id);
				} else if (mde instanceof VolumeData) {
					VolumeData id = (VolumeData) mde;
					primary = new BinaryFileInfo(id.getURL(), true, id);
				} else {
					if (mde instanceof Sample3D) {
						Sample3D s3d = (Sample3D) mde;
						primary = null;
						for (NumericMeasurementInterface nmi : s3d.getBinaryMeasurements()) {
							if (nmi instanceof ImageData) {
								ImageData id = (ImageData) nmi;
								primary = new BinaryFileInfo(id.getURL(), true, id);
							} else if (nmi instanceof VolumeData) {
								VolumeData id = (VolumeData) nmi;
								primary = new BinaryFileInfo(id.getURL(), true, id);
							}
							if (primary != null)
								bbb.add(primary);
						}
						primary = null;
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
						bbb.add(new BinaryFileInfo(new IOurl(fileName), false, mt.getTargetEntity()));
					}
				}
			}

			for (final BinaryFileInfo binaryFileInfo : bbb) {
				ImageResult imageResult = new ImageResult(null, binaryFileInfo);
				boolean lemna = false;
				if (binaryFileInfo.getFileName() == null)
					binaryFileInfo.setFileName(null);
				ImageIcon previewImage = null;
				if (FileSystemHandler.isFileUrl(binaryFileInfo.getFileName())) {
					MyImageIcon myImage = new MyImageIcon(MainFrame.getInstance(), DataSetFileButton.ICON_WIDTH,
							DataSetFileButton.ICON_HEIGHT, binaryFileInfo.getFileName(), binaryFileInfo);
					myImage.imageAvailable = 1;
					previewImage = myImage;
				} else if (LemnaTecFTPhandler.isLemnaTecFtpUrl(binaryFileInfo.getFileName())) {
					previewImage = null;
					lemna = true;
				} else {
					byte[] pi = new MongoDB().getPreviewData(binaryFileInfo.getMD5());
					if (pi != null)
						previewImage = new ImageIcon(pi);
				}
				final DataSetFileButton imageButton = new DataSetFileButton(dbeUser, dbePass, mt, imageResult, previewImage, mt
						.isReadOnly());
				if (binaryFileInfo.isPrimary())
					imageButton.setIsPrimaryDatabaseEntity();

				imageButton.setDownloadNeeded(!FileSystemHandler.isFileUrl(binaryFileInfo.getFileName()));
				imageButton.setVerticalTextPosition(SwingConstants.BOTTOM);
				imageButton.setHorizontalTextPosition(SwingConstants.CENTER);

				if (!cleared) {
					cleared = true;
					clearPanel(filePanel, mt, expTree);
				}

				final boolean lemnaF = lemna;

				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						// nur falls der Zielknoten immer noch ausgewählt ist,
						// soll der Button hinzugefügt werden
						if (mt == expTree.getSelectionPath().getLastPathComponent()) {
							filePanel.add(imageButton);
							filePanel.validate();
							filePanel.repaint();
							filePanel.scrollpane.validate();
							if (lemnaF) {
								BackgroundThreadDispatcher.addTask(new Thread(new Runnable() {
									@Override
									public void run() {
										if (mt == expTree.getSelectionPath().getLastPathComponent()) {
											MyImageIcon myImage;
											try {
												myImage = new MyImageIcon(MainFrame.getInstance(), DataSetFileButton.ICON_WIDTH,
														DataSetFileButton.ICON_HEIGHT, binaryFileInfo.getFileName(), binaryFileInfo);
												myImage.imageAvailable = 1;
												imageButton.updateLayout(null, myImage, myImage);
											} catch (MalformedURLException e) {
												// empty
											}
										}
									}
								}), -1);
							}
						} else
							stop.setStopWanted(true);
					}
				});
			}
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
		}
	}

	private static void clearPanel(final JMyFilePanel filePanel, final MongoTreeNode mt, final JTree expTree) {
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					if (mt == expTree.getSelectionPath().getLastPathComponent()) {
						filePanel.removeAll();
						filePanel.validate();
						filePanel.repaint();
						filePanel.scrollpane.validate();
					}
				}
			});
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
