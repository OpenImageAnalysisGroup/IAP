/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * $Id: FileSaveAsAction.java,v 1.1 2011-01-31 09:00:09 klukas Exp $
 */

package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.helper_classes;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.ErrorMsg;
import org.graffiti.core.GenericFileFilter;
import org.graffiti.core.StringBundle;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.actions.FileSaveAction;
import org.graffiti.help.HelpContext;
import org.graffiti.plugin.actions.GraffitiAction;
import org.graffiti.plugin.io.OutputSerializer;
import org.graffiti.session.SessionManager;

/**
 * The action for saving a graph to a named file.
 * 
 * @version $Revision: 1.1 $
 */
public class FileSaveAsAction extends GraffitiAction {
	
	private static final long serialVersionUID = 1L;
	
	private org.graffiti.managers.IOManager ioManager;
	
	private StringBundle sBundle;
	
	private SessionManager sessionManager;
	
	// private JFileChooser fc;
	
	public FileSaveAsAction(MainFrame mainFrame, org.graffiti.managers.IOManager ioManager,
						SessionManager sessionManager, StringBundle sBundle) {
		
		super("file.saveAs", mainFrame, "filemenu_saveas");
		this.ioManager = ioManager;
		this.sessionManager = sessionManager;
		this.sBundle = sBundle;
		
		// fc = new JFileChooser();
	}
	
	@Override
	public boolean isEnabled() {
		return ioManager.hasOutputSerializer() &&
							sessionManager.isSessionActive();
	}
	
	public void actionPerformed(ActionEvent e) {
		JFileChooser fc = ioManager.createSaveFileChooser();
		
		boolean needFile = true;
		while (needFile) {
			
			int returnVal = fc.showDialog
								(mainFrame, sBundle.getString("menu.file.saveAs"));
			
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				String fileName = file.getName();
				// System.err.println(fileName);
				String ext = "";
				if (fileName.indexOf(".") == -1) {
					ext = ((GenericFileFilter) fc.getFileFilter()).getExtension();
					fileName = file.getName() + ext;
					file = new File(file.getAbsolutePath() + ext);
				} else {
					ext = FileSaveAction.getFileExt(fileName);
				}
				// System.err.println(fileName);
				
				if (file.exists()) {
					if (JOptionPane.showConfirmDialog(mainFrame,
										"<html>Do you want to overwrite the existing file <i>" +
															fileName + "</i>?</html>",
										"Overwrite File?",
										JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
						needFile = false;
					}
				} else {
					needFile = false;
				}
				
				if (!needFile) {
					try {
						OutputSerializer os =
											ioManager.createOutputSerializer(ext);
						
						os.write(new FileOutputStream(file), getGraph());
						
					} catch (Exception ioe) {
						ErrorMsg.addErrorMessage(ioe);
						MainFrame.getInstance().warnUserAboutFileSaveProblem(ioe);
					}
					
					org.graffiti.session.EditorSession session =
										(org.graffiti.session.EditorSession) mainFrame.getActiveSession();
					
					session.setFileName(file.getAbsolutePath());
					mainFrame.fireSessionDataChanged(session);
				}
			} else {
				// leave loop
				needFile = false;
			}
		}
	}
	
	/**
	 * @see org.graffiti.plugin.actions.GraffitiAction#getHelpContext()
	 */
	@Override
	public HelpContext getHelpContext() {
		return null;
	}
	
}