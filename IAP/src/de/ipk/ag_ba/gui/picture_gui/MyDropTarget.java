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
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProviderSupportingExternalCallImpl;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.BinaryMeasurement;

/**
 * @author Christian Klukas
 */
public class MyDropTarget extends DropTarget implements DropTargetListener {
	private static final long serialVersionUID = 1L;
	DataSetFilePanel panel;
	MongoTreeNode targetTreeNode;
	JTree expTree;
	private final MongoDB m;
	
	public MyDropTarget(MongoDB m, DataSetFilePanel panel, MongoTreeNode targetTreeNode, JTree expTree) {
		this.panel = panel;
		this.targetTreeNode = targetTreeNode;
		this.expTree = expTree;
		this.m = m;
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
			MyThread t = new MyThread(new Runnable() {
				public void run() {
					for (int i = 0; i < ((java.util.List) data).size(); i++) {
						File file = (File) ((java.util.List) data).get(i);
						status.setCurrentStatusText2("Process " + file.getName());
						if (file.isDirectory())
							processDirectory(file);
						else {
							if (file.length() > 0) {
								addImageToDatabase(file, false);
							}
						}
						status.setCurrentStatusValueFine(100d * i / ((java.util.List) data).size());
					}
					tso.setBval(0, true); // finished
				}
			}, "process dropped files");
			BackgroundThreadDispatcher.addTask(t, 1);
		}
		MyThread t = new MyThread(new Runnable() {
			
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
									addImageToDatabase(file, false);
							} catch (URISyntaxException e2) {
								SupplementaryFilePanelMongoDB.showError("URL Syntax Error.", e2);
							}
							status.setCurrentStatusValueFine(100d * i / files.length);
						}
					tso.setBval(0, true); // finished
				}
			}
		}, "add image to database");
		BackgroundThreadDispatcher.addTask(t, 1);
		e.dropComplete(true);
	}
	
	public void addImageToDatabase(final File file, final boolean deleteUponCompletion) {
		final DataSetFileButton imageButton = new DataSetFileButton(m, targetTreeNode,
							"<html><body><b>" + DataSetFileButton.getMaxString(file.getName()) + //$NON-NLS-1$
									"</b><br>" + file.length() / 1024 + " KB</body></html>", null, null); //$NON-NLS-1$//$NON-NLS-2$
		imageButton.setProgressValue(-1);
		imageButton.showProgressbar();
		
		if (targetTreeNode == expTree.getSelectionPath().getLastPathComponent()) {
			panel.add(imageButton);
			panel.validate();
			panel.getScrollpane().validate();
		}
		
		MyThread t = new MyThread(new Runnable() {
			public void run() {
				MyImageIcon iconA;
				BinaryFileInfo bif = new BinaryFileInfo(FileSystemHandler.getURL(file), false, targetTreeNode
									.getTargetEntity());
				try {
					iconA = new MyImageIcon(panel.getParent(), DataSetFileButton.ICON_WIDTH, DataSetFileButton.ICON_HEIGHT,
										FileSystemHandler.getURL(file), bif);
				} catch (MalformedURLException e) {
					SupplementaryFilePanelMongoDB.showError("Malformed URL Exception.", e);
					iconA = null;
				}
				imageButton.myImage = iconA;
				final MyImageIcon icon = iconA;
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						// imageButton.setIcon(icon);
						imageButton.updateLayout(imageButton.mmlbl.getText(), icon, icon);
					}
				});
				if (DataSetFileButton.getMaxString(file.getName()).endsWith("..."))
					imageButton.setToolTipText(file.getName());
				
				if (file.canRead()) {
					imageButton.setProgressValue(-1);
					DatabaseStorageResult md5 = DataExchangeHelperForExperiments.insertHashedFile(m, file,
										imageButton.createTempPreviewImage(), imageButton.getIsJavaImage(), imageButton, targetTreeNode
															.getTargetEntity());
					((BinaryMeasurement) bif.getEntity()).getURL().setDetail(md5.getMD5());
					if (md5 == DatabaseStorageResult.IO_ERROR_SEE_ERRORMSG) {
						SupplementaryFilePanelMongoDB.showError("The file " + file.getName()
											+ " could not be stored to the Database (Target-Table " + targetTreeNode.getTargetEntity(),
											null);
					}
					imageButton.setDownloadNeeded(true);
					imageButton.downloadInProgress = false;
					
					BinaryFileInfo bfi = new BinaryFileInfo(FileSystemHandler.getURL(file), false, targetTreeNode
										.getTargetEntity());
					
					imageButton.imageResult = new ImageResult(icon, bfi);
					imageButton.setProgressValue(100);
					targetTreeNode.setSizeDirty(true);
					targetTreeNode.updateSizeInfo(m, targetTreeNode.getSizeChangedListener());
					imageButton.hideProgressbar();
				}
				if (deleteUponCompletion)
					file.delete();
			}
		}, "store image in database");
		BackgroundThreadDispatcher.addTask(t, -1);
	}
	
	public void processDirectory(File file) {
		File[] list = file.listFiles();
		for (int j = 0; j < list.length; j++) {
			File currentFile = list[j];
			if (currentFile.isDirectory())
				processDirectory(currentFile);
			else
				addImageToDatabase(currentFile, false);
		}
	}
	
	@Override
	public void dragExit(DropTargetEvent arg0) {
		System.out.println("DRAG EXIT " + arg0); //$NON-NLS-1$
	}
}
