package de.ipk.ag_ba.gui.picture_gui;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JTree;
import javax.swing.SwingUtilities;

import org.graffiti.plugin.algorithm.ThreadSafeOptions;
import org.graffiti.plugin.io.resources.FileSystemHandler;

import de.ipk.ag_ba.mongo.DatabaseStorageResult;
import de.ipk.ag_ba.mongo.DatabaseStorageResultWithURL;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.MappingDataEntity;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProviderSupportingExternalCallImpl;

/**
 * @author Christian Klukas
 */
public class MyDropTarget extends DropTarget implements DropTargetListener {
	private static final long serialVersionUID = 1L;
	DataSetFilePanel panel;
	MongoTreeNode targetTreeNode;
	JTree expTree;
	
	public MyDropTarget(DataSetFilePanel panel, MongoTreeNode targetTreeNode, JTree expTree) {
		this.panel = panel;
		this.targetTreeNode = targetTreeNode;
		this.expTree = expTree;
	}
	
	public boolean isTargetReadOnly() {
		// in case it is no DBE-TreeNode or if it is one and it is Read-Only
		// then reject drop
		// in case it is null, it is also readOnly
		return targetTreeNode == null || (targetTreeNode != null && targetTreeNode.isReadOnly());
	}
	
	/**
	 * (non-Javadoc)
	 * 
	 * @see java.awt.dnd.DropTargetListener#drop(java.awt.dnd.DropTargetDropEvent)
	 */
	@Override
	public void drop(final DropTargetDropEvent e) {
		if (isTargetReadOnly()) {
			e.rejectDrop();
			return;
		}
		
		e.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
		
		final ThreadSafeOptions tso = new ThreadSafeOptions();
		tso.setBval(0, false); // not yet finished
		
		final BackgroundTaskStatusProviderSupportingExternalCallImpl status = new BackgroundTaskStatusProviderSupportingExternalCallImpl(
				"Drag & Drop in progress...", "please wait");
		BackgroundTaskHelper.issueSimpleTask("Drag & Drop", "Drag & Drop in progress...", new Runnable() {
			@Override
			public void run() {
				while (!tso.getBval(0, false)) {
					try {
						Thread.sleep(100);
						if (status.wantsToStop())
							break;
					} catch (InterruptedException e) {
						// empty
					}
				}
				status.setCurrentStatusText2("Uploading to database...");
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					// empty
				}
			}
		}, null);
		
		String s0 = null;
		if (e.isDataFlavorSupported(DataFlavor.stringFlavor)) {
			try {
				s0 = (String) e.getTransferable().getTransferData(DataFlavor.stringFlavor);
			} catch (UnsupportedFlavorException e1) {
				SupplementaryFilePanelMongoDB.showError("Drag+Drop 'unsupported flavor'.", e1);
				tso.setBval(0, true); // finished
			} catch (IOException e1) {
				SupplementaryFilePanelMongoDB.showError("Drag+Drop IO error.", e1);
				tso.setBval(0, true); // finished
			}
			s0 = MyTools.stringReplace(s0, "" + "\n", "" + "\r");
		}
		final String s = s0;
		
		Object data0 = null;
		if (e.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
			try {
				data0 = e.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
			} catch (UnsupportedFlavorException e1) {
				SupplementaryFilePanelMongoDB.showError("Drag+Drop 'unsupported flavor' (2).", e1);
				tso.setBval(0, true); // finished
			} catch (IOException e1) {
				SupplementaryFilePanelMongoDB.showError("Drag+Drop IO exception (2).", e1);
				tso.setBval(0, true); // finished
			}
		}
		final Object data = data0;
		
		if (data != null) {
			LocalComputeJob t;
			try {
				t = new LocalComputeJob(new Runnable() {
					@Override
					public void run() {
						for (int i = 0; i < ((java.util.List) data).size(); i++) {
							File file = (File) ((java.util.List) data).get(i);
							status.setCurrentStatusText2("Process " + file.getName());
							if (file.isDirectory())
								try {
									processDirectory(file);
								} catch (InterruptedException e1) {
									e1.printStackTrace();
								}
							else {
								if (file.length() > 0) {
									try {
										addImageOrFileToDatabase(file, false);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
								}
							}
							status.setCurrentStatusValueFine(100d * i / ((java.util.List) data).size());
						}
						tso.setBval(0, true); // finished
					}
				}, "process dropped files");
				BackgroundThreadDispatcher.addTask(t);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
		LocalComputeJob t;
		try {
			t = new LocalComputeJob(new Runnable() {
				
				@Override
				public void run() {
					if (s != null) {
						System.out.println(e.getCurrentDataFlavorsAsList().toString());
						final String[] files = s.split("\r"); //$NON-NLS-1$
						if (files != null)
							for (int i = 0; i < files.length; i++) {
								File file;
								try {
									files[i] = MyTools.stringReplace(files[i], " ", "" + "%20");
									file = new File(new URI(files[i]));
									status.setCurrentStatusText2("Process " + file.getName());
									if (file.isDirectory())
										processDirectory(file);
									else
										addImageOrFileToDatabase(file, false);
								} catch (URISyntaxException e2) {
									SupplementaryFilePanelMongoDB.showError("URL Syntax Error.", e2);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
								status.setCurrentStatusValueFine(100d * i / files.length);
							}
						tso.setBval(0, true); // finished
					}
				}
			}, "add image to database");
			BackgroundThreadDispatcher.addTask(t);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		e.dropComplete(true);
	}
	
	public void addImageOrFileToDatabase(final File file, final boolean deleteUponCompletion) throws InterruptedException {
		final DataSetFileButton imageButton = new DataSetFileButton(targetTreeNode,
				"<html><body><b>" + DataSetFileButton.getMaxString(file.getName()) + //$NON-NLS-1$
						"</b><br>" + file.length() / 1024 + " KB</body></html>", null, null, false, null); //$NON-NLS-1$//$NON-NLS-2$
		imageButton.setProgressValue(-1);
		imageButton.showProgressbar();
		imageButton.setIsAttachment();
		
		if (targetTreeNode == expTree.getSelectionPath().getLastPathComponent()) {
			panel.add(imageButton);
			panel.validate();
			panel.getScrollpane().validate();
		}
		
		LocalComputeJob t = new LocalComputeJob(new Runnable() {
			@Override
			public void run() {
				MyImageIcon iconA;
				BinaryFileInfo bif = new BinaryFileInfo(FileSystemHandler.getURL(file), null, false, targetTreeNode
						.getTargetEntity());
				try {
					iconA = new MyImageIcon(panel.getParent(), DataSetFileButton.ICON_WIDTH, DataSetFileButton.ICON_HEIGHT,
							FileSystemHandler.getURL(file), null, bif);
				} catch (MalformedURLException e) {
					SupplementaryFilePanelMongoDB.showError("Malformed URL Exception.", e);
					iconA = null;
				}
				imageButton.myImage = iconA;
				final MyImageIcon icon = iconA;
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						// imageButton.setIcon(icon);
						imageButton.updateLayout(imageButton.mmlbl.getText(), icon, icon, false);
					}
				});
				if (DataSetFileButton.getMaxString(file.getName()).endsWith("..."))
					imageButton.setToolTipText(file.getName());
				
				if (file.canRead()) {
					if (targetTreeNode.getExperiment().getM() == null) {
						MappingDataEntity target = targetTreeNode.getTargetEntity();
						BinaryFileInfo bfi = new BinaryFileInfo(FileSystemHandler.getURL(file), null, false,
								targetTreeNode.getTargetEntity());
						imageButton.imageResult = new ImageResult(icon, bfi);
						System.out.println("TARGET: " + target);
						imageButton.setProgressValue(100);
						imageButton.hideProgressbar();
					} else {
						imageButton.setProgressValue(-1);
						DatabaseStorageResultWithURL res = targetTreeNode.getExperiment().getIoHelper()
								.insertHashedFile(file,
										imageButton.createTempPreviewImage(), imageButton.getIsJavaImage(),
										imageButton, targetTreeNode.getTargetEntity());
						// ((BinaryMeasurement) bif.getEntity()).getURL().setDetail(md5.getMD5());
						if (res.getResult() == DatabaseStorageResult.IO_ERROR_SEE_ERRORMSG) {
							SupplementaryFilePanelMongoDB.showError("The file " + file.getName()
									+ " could not be stored!",
									null);
						} else {
							DataExchangeHelperForExperiments.attachFileToEntity(targetTreeNode.getTargetEntity(),
									res, file.getName());
						}
						imageButton.setDownloadNeeded(true);
						imageButton.downloadInProgress = false;
						
						BinaryFileInfo bfi = new BinaryFileInfo(FileSystemHandler.getURL(file), null, false,
								targetTreeNode.getTargetEntity());
						bfi.setIsAttachment(true);
						imageButton.imageResult = new ImageResult(icon, bfi);
						imageButton.setProgressValue(100);
						imageButton.hideProgressbar();
					}
					targetTreeNode.setSizeDirty(true);
					try {
						targetTreeNode.updateSizeInfo(targetTreeNode.getSizeChangedListener());
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				if (deleteUponCompletion)
					file.delete();
			}
		}, "store image in database");
		BackgroundThreadDispatcher.addTask(t, true, -Integer.MAX_VALUE);
	}
	
	public void processDirectory(File file) throws InterruptedException {
		File[] list = file.listFiles();
		for (int j = 0; j < list.length; j++) {
			File currentFile = list[j];
			if (currentFile.isDirectory())
				processDirectory(currentFile);
			else
				addImageOrFileToDatabase(currentFile, false);
		}
	}
	
	@Override
	public void dragExit(DropTargetEvent arg0) {
		System.out.println("DRAG EXIT " + arg0); //$NON-NLS-1$
	}
}
