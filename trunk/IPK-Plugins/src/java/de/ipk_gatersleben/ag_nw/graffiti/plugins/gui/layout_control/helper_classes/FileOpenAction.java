/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
// ==============================================================================
//
// FileOpenAction.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: FileOpenAction.java,v 1.2 2013-05-21 19:11:47 klukas Exp $

package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.helper_classes;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JScrollPane;

import org.OpenFileDialogService;
import org.graffiti.core.GenericFileFilter;
import org.graffiti.core.StringBundle;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.Graph;
import org.graffiti.help.HelpContext;
import org.graffiti.managers.IOManager;
import org.graffiti.managers.MyInputStreamCreator;
import org.graffiti.managers.ViewManager;
import org.graffiti.plugin.actions.GraffitiAction;
import org.graffiti.plugin.io.InputSerializer;
import org.graffiti.session.EditorSession;

/**
 * The action for the file open dialog.
 */
public class FileOpenAction
		extends GraffitiAction {
	// ~ Instance fields ========================================================
	
	private static final long serialVersionUID = 1L;
	
	/** DOCUMENT ME! */
	private IOManager ioManager;
	
	/** DOCUMENT ME! */
	private StringBundle sBundle;
	
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
	public FileOpenAction(MainFrame mainFrame) {
		super("file.open", mainFrame, "filemenu_open");
		this.ioManager = mainFrame.getIoManager();
		this.viewManager = mainFrame.getViewManager();
		this.sBundle = StringBundle.getInstance();
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
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @param e
	 *           DOCUMENT ME!
	 */
	public JScrollPane loadFile(java.awt.event.ActionEvent evt) // String fileNameOrNull
	{
		JFileChooser fc = ioManager.createOpenFileChooser();
		
		OpenFileDialogService.setActiveDirectoryFor(fc);
		
		int returnVal = fc.showDialog(mainFrame,
				sBundle.getString("menu.file.open"));
		
		OpenFileDialogService.setActiveDirectoryFrom(fc.getCurrentDirectory());
		
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			String fileName = file.getName();
			
			// System.err.println(fileName);
			if (fileName.indexOf(".") == -1) {
				fileName = file.getName() +
						((GenericFileFilter) fc.getFileFilter()).getExtension();
			}
			
			// System.err.println(fileName);
			String ext = fileName.substring(fileName.lastIndexOf("."));
			
			try {
				MyInputStreamCreator ic = new MyInputStreamCreator(file);
				InputSerializer is = ioManager.createInputSerializer(ic.getNewInputStream(), ext);
				
				Graph g = null;
				g = is.read(ic.getNewInputStream());
				
				EditorSession es = new EditorSession(g);
				es.setFileName(file.getAbsolutePath());
				return mainFrame.showViewChooserDialog(es, true, evt);
			} catch (Exception pe) {
				showError(pe.getLocalizedMessage());
			}
		}
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		//
		
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
