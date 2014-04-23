// ==============================================================================
//
// FileSaveAction.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: FileSaveAction.java,v 1.2 2013-05-21 19:11:11 klukas Exp $

package org.graffiti.editor.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.ErrorMsg;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.MessageType;
import org.graffiti.help.HelpContext;
import org.graffiti.managers.IOManager;
import org.graffiti.plugin.actions.GraffitiAction;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;
import org.graffiti.plugin.io.OutputSerializer;
import org.graffiti.plugin.io.resources.IOurl;
import org.graffiti.plugin.io.resources.ResourceIOManager;
import org.graffiti.plugin.view.SuppressSaveActionsView;
import org.graffiti.session.EditorSession;
import org.graffiti.session.SessionManager;

/**
 * The action for saving a graph.
 * 
 * @version $Revision: 1.2 $
 */
public class FileSaveAction
		extends GraffitiAction {
	// ~ Instance fields ========================================================
	
	private static final long serialVersionUID = 1L;
	
	/** DOCUMENT ME! */
	private final IOManager ioManager;
	
	/** DOCUMENT ME! */
	private final SessionManager sessionManager;
	
	// ~ Constructors ===========================================================
	
	/**
	 * Creates a new FileSaveAction object.
	 * 
	 * @param mainFrame
	 *           DOCUMENT ME!
	 * @param ioManager
	 *           DOCUMENT ME!
	 * @param sessionManager
	 *           DOCUMENT ME!
	 */
	public FileSaveAction(MainFrame mainFrame, IOManager ioManager,
			SessionManager sessionManager) {
		super("file.save", mainFrame, "filemenu_save");
		this.ioManager = ioManager;
		this.sessionManager = sessionManager;
	}
	
	// ~ Methods ================================================================
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @return <code>true</code>, if the io manager contains a working output
	 *         serializer and if the file is writeable.
	 */
	@Override
	public boolean isEnabled() {
		String fullName;
		EditorSession session = null;
		try {
			// these commands fail if the session has not yet been saved to
			// a file
			session = (EditorSession) mainFrame.getActiveSession();
			fullName = session.getFileNameFull();
		} catch (Exception e) {
			return false;
		}
		
		if (session != null && session.getActiveView() instanceof SuppressSaveActionsView)
			return false;
		
		try {
			String ext = getFileExt(fullName);
			
			File file = new File(fullName);
			
			if (file.canWrite()) {
				ioManager.createOutputSerializer("." + ext);
				// runtime error check, if exception, ioManager can not
				// handle current file for saving.
			} else {
				IOurl url = new IOurl(fullName);
				if (ResourceIOManager.getHandlerFromPrefix(url.getPrefix()) != null)
					return true;
				else
					return false;
			}
		} catch (Exception e) {
			return false;
		}
		
		return (ioManager.hasOutputSerializer() && sessionManager.isSessionActive());
	}
	
	/**
	 * @see org.graffiti.plugin.actions.GraffitiAction#getHelpContext()
	 */
	@Override
	public HelpContext getHelpContext() {
		return null;
	}
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @param e
	 *           DOCUMENT ME!
	 */
	public void actionPerformed(ActionEvent e) {
		// CK, 1.Juli.2003 Copied and modified from SaveAsAction
		EditorSession session;
		String fullName;
		
		try {
			session = (EditorSession) mainFrame.getActiveSession();
			fullName = session.getFileNameFull();
			if (session != null && session.getUndoManager() != null)
				session.getUndoManager().discardAllEdits();
		} catch (Exception err) {
			MainFrame.showMessageDialog("Could not save graph.", "Error");
			ErrorMsg.addErrorMessage(err);
			return;
		}
		
		String ext = getFileExt(fullName);
		
		File file = new File(fullName);
		
		try {
			OutputSerializer os = ioManager.createOutputSerializer(ext);
			if (os == null) {
				MainFrame.showMessageDialog("Unknown outputserializer for file extension " + ext, "Error");
			} else {
				String target;
				final ThreadSafeOptions fs = new ThreadSafeOptions();
				if (file.canWrite()) {
					OutputStream out = new FileOutputStream(file);
					target = file.getAbsolutePath();
					os.write(out, getGraph());
					out.close();
					fs.setLong(file.length());
				} else {
					IOurl url = new IOurl(fullName);
					final OutputStream ooo = url.getOutputStream();
					OutputStream out = new OutputStream() {
						@Override
						public void write(int b) throws IOException {
							ooo.write(b);
							fs.addLong(1);
						}
						
						@Override
						public void close() throws IOException {
							ooo.close();
						}
						
					};
					target = url.toString();
					os.write(out, getGraph());
					out.close();
				}
				FileHandlingManager.getInstance().throwFileSaved(file, ext, getGraph());
				getGraph().setModified(false);
				MainFrame.showMessage("Graph saved to " + target + " (" + (fs.getLong() / 1024) + " KB)", MessageType.INFO);
				// a recent menu entry will be already built, if a graph is loaded or "saved as"... so we dont need to add a menu entry here
				// MainFrame.getInstance().addNewRecentFileMenuItem(file);
				
			}
			mainFrame.fireSessionDataChanged(session);
		} catch (Exception ioe) {
			MainFrame.getInstance().warnUserAboutFileSaveProblem(ioe);
		}
	}
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @param fileName
	 * @return Returns file extension from a given filename.
	 */
	public static String getFileExt(String fileName) {
		String workName;
		
		int lastSep = fileName.lastIndexOf(File.pathSeparator);
		
		if (lastSep == -1) {
			// no extension
			workName = fileName;
		} else {
			workName = fileName.substring(lastSep + 1);
		}
		
		int lastDot = workName.lastIndexOf('.');
		
		if (lastDot == -1) {
			return "";
		} else {
			boolean gz = false;
			if (workName.toUpperCase().endsWith(".GZ")) {
				workName = workName.substring(0, workName.length() - ".gz".length());
				gz = true;
			}
			lastDot = workName.lastIndexOf('.');
			String extension = workName.substring(lastDot);
			if (gz)
				extension = extension + ".gz";
			return extension;
		}
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
