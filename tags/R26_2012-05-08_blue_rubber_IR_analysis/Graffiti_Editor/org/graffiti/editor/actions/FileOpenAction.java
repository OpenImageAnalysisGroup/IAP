// ==============================================================================
//
// FileOpenAction.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: FileOpenAction.java,v 1.1 2011-01-31 09:04:23 klukas Exp $

package org.graffiti.editor.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JFileChooser;

import org.ErrorMsg;
import org.MultipleFileLoader;
import org.OpenFileDialogService;
import org.Release;
import org.graffiti.core.StringBundle;
import org.graffiti.editor.MainFrame;
import org.graffiti.help.HelpContext;
import org.graffiti.managers.IOManager;
import org.graffiti.managers.ViewManager;
import org.graffiti.plugin.actions.GraffitiAction;

/**
 * The action for the file open dialog.
 */
public class FileOpenAction extends GraffitiAction {
	// ~ Instance fields ========================================================
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/** DOCUMENT ME! */
	private static IOManager ioManager;
	
	/** DOCUMENT ME! */
	private static StringBundle sBundle;
	
	/** DOCUMENT ME! */
	private ViewManager viewManager;
	
	// ~ Constructors ===========================================================
	
	/**
	 * Creates a new FileOpenAction object.
	 * 
	 * @param mainFrame
	 *           DOCUMENT ME!
	 * @param ioManager
	 *           DOCUMENT ME!
	 * @param viewManager
	 *           DOCUMENT ME!
	 * @param sBundle
	 *           DOCUMENT ME!
	 */
	public FileOpenAction(MainFrame mainFrame, IOManager ioManager,
						ViewManager viewManager, StringBundle sBundle) {
		super("file.open", mainFrame, "filemenu_open");
		FileOpenAction.ioManager = ioManager;
		this.viewManager = viewManager;
		FileOpenAction.sBundle = sBundle;
	}
	
	// ~ Methods ================================================================
	
	/**
	 * This action is enabled, if the editor's io manager contains an input
	 * serializer.
	 * 
	 * @return <code>true</code>, if the io manager contains at least one input
	 *         serializer.
	 */
	@Override
	public boolean isEnabled() {
		return ioManager.hasInputSerializer() && viewManager.hasViews();
	}
	
	/**
	 * @see org.graffiti.plugin.actions.GraffitiAction#getHelpContext()
	 */
	@Override
	public HelpContext getHelpContext() {
		return null;
	}
	
	public void actionPerformed(ActionEvent e) {
		JFileChooser fc = ioManager.createOpenFileChooser();
		OpenFileDialogService.setActiveDirectoryFor(fc);
		fc.setMultiSelectionEnabled(true);
		// fc.resetChoosableFileFilters();
		int returnVal = fc.showDialog(mainFrame, sBundle
							.getString("menu.file.open"));
		
		OpenFileDialogService.setActiveDirectoryFrom(fc.getCurrentDirectory());
		
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File[] selfiles = fc.getSelectedFiles();
			try {
				if (Release.getFileLoaderHelper() != null) {
					MultipleFileLoader mfl = Release.getFileLoaderHelper();
					mfl.loadGraphInBackground(selfiles, e);
				} else
					MainFrame.getInstance().loadGraphInBackground(selfiles, e, false);
				for (File f : selfiles)
					FileHandlingManager.getInstance().throwFileOpened(f);
			} catch (IllegalAccessException e1) {
				ErrorMsg.addErrorMessage(e1);
			} catch (InstantiationException e1) {
				ErrorMsg.addErrorMessage(e1);
			}
		}
		fc.setMultiSelectionEnabled(false);
	}
	
	public static Collection<File> getGraphFilesFromUser() {
		ArrayList<File> result = new ArrayList<File>();
		JFileChooser fc = ioManager.createOpenFileChooser();
		OpenFileDialogService.setActiveDirectoryFor(fc);
		fc.setMultiSelectionEnabled(true);
		// fc.resetChoosableFileFilters();
		int returnVal = fc.showDialog(MainFrame.getInstance(), sBundle
							.getString("menu.file.open"));
		
		OpenFileDialogService.setActiveDirectoryFrom(fc.getCurrentDirectory());
		
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File[] selfiles = fc.getSelectedFiles();
			for (File f : selfiles)
				result.add(f);
			fc.setMultiSelectionEnabled(false);
		} else {
			fc.setMultiSelectionEnabled(false);
			return null;
		}
		return result;
	}
	
	// private void openFile(File sf, JFileChooser fc, boolean loadInBackground) {
	// File selfile = sf;
	// File file = sf;
	// if ((selfile != null) && (selfile.getName() != null)
	// && (selfile.getName().indexOf(".") == -1)) {
	// String extension = null;
	// if (((GenericFileFilter) fc.getFileFilter()).getExtension() == null) {
	// // no file filter selected, check if a file with any of the given extensions is available
	// FileFilter[] ff = fc.getChoosableFileFilters();
	// for (int i = 0; i < ff.length; i++) {
	// if (ff[i] instanceof GenericFileFilter) {
	// extension = ((GenericFileFilter) ff[i]).getExtension();
	// file = new File(selfile + extension);
	// } else {
	// ErrorMsg.addErrorMessage("Invalid File Filter Type Found ("+ff[i]+" not instance of GenericFileFilter)");
	// }
	// if (file.exists()) break;
	// }
	// } else {
	// extension = ((GenericFileFilter) fc.getFileFilter())
	// .getExtension();
	// }
	// file = new File(selfile + extension);
	// }
	// if (!file.exists()) {
	// MainFrame.showMessageDialog(
	// "Can not load graph file: file not found.", "Error");
	// } else {
	// try {
	// if (loadInBackground)
	// mainFrame.loadGraphInBackground(file);
	// else
	// mainFrame.loadGraph(file);
	// } catch (IllegalAccessException e1) {
	// e1.printStackTrace();
	// ErrorMsg.addErrorMessage("Could not load file ("
	// + file.getName() + "): " + e1.getLocalizedMessage());
	// } catch (InstantiationException e2) {
	// e2.printStackTrace();
	// ErrorMsg.addErrorMessage("Could not load file ("
	// + file.getName() + "): " + e2.getLocalizedMessage());
	// }
	// }
	// }
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
